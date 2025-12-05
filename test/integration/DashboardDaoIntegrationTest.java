package integration;

import dao.DashboardDao;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import util.ExcelTestExporter;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp DashboardDao.
 * Class này thiết lập kết nối DB test (shopduck_test) và dữ liệu mẫu để
 * kiểm tra các phương thức báo cáo (thống kê doanh thu, đơn hàng, sản phẩm).
 */
public class DashboardDaoIntegrationTest {

    private Connection connection;
    private DashboardDao dashboardDao;

    // --- CẤU HÌNH KẾT NỐI DB TEST ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck_test";

    // --- TRẠNG THÁI ĐƠN HÀNG DÙNG TRONG TEST ---
    private final String STATUS_SUCCESS = "Đã giao";
    private final String STATUS_PROCESSING = "Chờ xử lý";

    // --- BIẾN GHI LOG TEST CASE (DÙNG CHO ExcelTestExporter) ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     * Thông tin này sẽ được ghi vào file Excel báo cáo.
     *
     * @param id ID của Test Case (Ví dụ: DASH_01)
     * @param name Tên của Test Case
     * @param steps Các bước thực hiện
     * @param data Dữ liệu đầu vào
     * @param expected Kết quả mong đợi
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case:
     * 1. Mở kết nối đến DB test.
     * 2. Xóa dữ liệu cũ (reset) trong Orders và OrderDetails.
     * 3. Đảm bảo dữ liệu nền tảng (User, Product, Size) tồn tại.
     * 4. Chèn dữ liệu Order mẫu phục vụ cho việc kiểm thử Dashboard.
     * @throws Exception nếu có lỗi kết nối hoặc thao tác DB.
     */
    @Before
    public void setUp() throws Exception {
        // 1. Mở kết nối
        String url = "jdbc:sqlserver://" + serverName + ":" + portNumber +
                ";databaseName=" + dbName + ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // 2. Reset dữ liệu test (quan trọng: xóa chi tiết trước Orders do khóa ngoại)
        try (Statement stmt = connection.createStatement()) {
            try { stmt.execute("DELETE FROM OrderDetails"); } catch (Exception ignored) {}
            stmt.execute("DELETE FROM Orders");
        }

        // 3. Chuẩn bị dữ liệu nền (User, Product, Variant…)
        ensureUserExists(1);
        ensureProductExists();

        dashboardDao = new DashboardDao();
        dashboardDao.setConnection(connection); // Dependency Injection: Sử dụng kết nối test

        // 4. Insert dữ liệu mẫu (Data Setup)
        insertOrder(1000000, STATUS_SUCCESS, LocalDateTime.now());                       // 1.000.000 (Hôm nay - Tính doanh thu)
        insertOrder(500000, STATUS_SUCCESS, LocalDateTime.now().minusDays(1));       // 0.500.000 (Hôm qua - Tính doanh thu)
        insertOrder(2000000, STATUS_SUCCESS, LocalDateTime.now().minusMonths(1));    // 2.000.000 (Tháng trước - Tính doanh thu)
        insertOrder(5000000, STATUS_PROCESSING, LocalDateTime.now());                   // 5.000.000 (Hôm nay - KHÔNG tính doanh thu)
        // Tổng doanh thu thành công: 3.500.000
    }

    /**
     * Tạo một User mẫu để đảm bảo khóa ngoại (FK) Orders.user_id hợp lệ.
     * Sử dụng SET IDENTITY_INSERT ON/OFF để cố gắng chèn user_id cố định.
     *
     * @param userId ID của User cần đảm bảo tồn tại
     */
    private void ensureUserExists(int userId) {
        try (Statement s = connection.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT id FROM Users WHERE id = " + userId);

            if (!rs.next()) {
                try {
                    s.executeUpdate("SET IDENTITY_INSERT Users ON");
                    s.executeUpdate("INSERT INTO Users (id, username, password, role) " +
                            "VALUES (" + userId + ", 'testuser', '123', 'user')");
                    s.executeUpdate("SET IDENTITY_INSERT Users OFF");
                } catch (Exception e) {
                    // Fallback nếu không được cấp quyền SET IDENTITY_INSERT
                    s.executeUpdate("INSERT INTO Users (username, password, role) VALUES ('testuser', '123', 'user')");
                }
            }
        } catch (Exception ignored) {
            // Bỏ qua lỗi trong quá trình setup dữ liệu nền tảng.
        }
    }

    /**
     * Tạo Product và Size mẫu nếu chưa tồn tại, để OrderDetails có thể được chèn hợp lệ
     * (thông qua ProductVariant).
     */
    private void ensureProductExists() {
        try (Statement s = connection.createStatement()) {
            // Product
            ResultSet rs = s.executeQuery("SELECT TOP 1 id FROM Products");
            if (!rs.next()) {
                s.executeUpdate("INSERT INTO Products (name, price, description, image) " +
                        "VALUES (N'Laptop Test', 1000, 'Desc', 'img.jpg')");
            }

            // Size
            rs = s.executeQuery("SELECT TOP 1 id FROM Sizes");
            if (!rs.next()) {
                s.executeUpdate("INSERT INTO Sizes (size_label) VALUES ('X')");
            }
        } catch (Exception e) {
            System.out.println("Lỗi tạo Product/Size test: " + e.getMessage());
        }
    }

    /**
     * Lấy ID của ProductVariant có sẵn, hoặc tạo mới nếu chưa có.
     *
     * @return ID của ProductVariant hợp lệ.
     */
    private int getValidVariantId() {
        try (Statement s = connection.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT TOP 1 id FROM ProductVariants");
            if (rs.next()) return rs.getInt(1);

            // Nếu chưa có variant → tạo mới từ Product và Size đã có
            int pId = 0, sId = 0;
            ResultSet rsP = s.executeQuery("SELECT TOP 1 id FROM Products"); if (rsP.next()) pId = rsP.getInt(1);
            ResultSet rsS = s.executeQuery("SELECT TOP 1 id FROM Sizes"); if (rsS.next()) sId = rsS.getInt(1);

            if (pId > 0 && sId > 0) {
                s.executeUpdate("INSERT INTO ProductVariants (product_id, size_id, stock) " +
                        "VALUES (" + pId + ", " + sId + ", 100)");
                rs = s.executeQuery("SELECT TOP 1 id FROM ProductVariants");
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 1; // Giá trị fallback
    }

    /**
     * Chèn một đơn hàng mới (Orders) và chi tiết đơn hàng (OrderDetails) mẫu.
     *
     * @param total Tổng giá trị đơn hàng.
     * @param status Trạng thái đơn hàng.
     * @param date Thời điểm đặt hàng.
     * @throws Exception nếu có lỗi DB.
     */
    private void insertOrder(double total, String status, LocalDateTime date) throws Exception {
        int variantId = getValidVariantId();

        String sqlOrder = "INSERT INTO Orders (user_id, order_date, total, status, address, phone) " +
                "VALUES (1, ?, ?, ?, N'HCM', '0909')";

        try (PreparedStatement ps = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(date));
            ps.setDouble(2, total);
            ps.setString(3, status);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    String sqlDetail = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) " +
                            "VALUES (?, ?, 1, ?)";

                    try (PreparedStatement ps2 = connection.prepareStatement(sqlDetail)) {
                        ps2.setInt(1, orderId);
                        ps2.setInt(2, variantId);
                        ps2.setDouble(3, total);
                        ps2.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ======================
    //     TEST CASES
    // ======================

    @Test
    public void testGetTotalRevenue() {
        setTestCaseInfo("DASH_01", "Tổng doanh thu",
                "3 đơn thành công", "Data Setup", "3.500.000");

        // Kiểm tra tổng doanh thu (chỉ tính đơn STATUS_SUCCESS)
        Assert.assertEquals(3500000.0, dashboardDao.getTotalRevenue(), 0.01);
    }

    @Test
    public void testGetRevenueByDate_Today() {
        setTestCaseInfo("DASH_02", "Doanh thu hôm nay",
                "1 đơn thành công", "Today", "1.000.000");

        String today = LocalDateTime.now().toLocalDate().toString();
        Assert.assertEquals(1000000.0, dashboardDao.getRevenueByDate(today, today), 0.01);
    }

    @Test
    public void testGetOrderCountByDate() {
        setTestCaseInfo("DASH_03", "Đếm đơn thành công hôm nay",
                "1 success + 1 processing", "Today", "Count = 1");

        String today = LocalDateTime.now().toLocalDate().toString();
        // Chỉ đơn STATUS_SUCCESS được tính
        Assert.assertEquals(1, dashboardDao.getOrderCountByDate(today, today));
    }

    @Test
    public void testGetRevenueLast7Days() {
        setTestCaseInfo("DASH_04", "Biểu đồ 7 ngày",
                "Có đơn hôm nay & hôm qua", "", "List >= 2");

        // Kiểm tra xem dữ liệu trả về có đủ các ngày có đơn (hôm nay và hôm qua)
        Assert.assertTrue(dashboardDao.getRevenueLast7Days().size() >= 2);
    }

    @Test
    public void testGetOrderStatusStats() {
        setTestCaseInfo("DASH_05", "Pie chart trạng thái",
                "3 success, 1 processing", "", "List >= 1");

        // Đảm bảo kết quả thống kê (key-value) trạng thái đơn hàng được trả về
        Assert.assertTrue(dashboardDao.getOrderStatusStats().size() >= 1);
    }

    @Test
    public void testGetTotalOrders() {
        setTestCaseInfo("DASH_06", "Tổng đơn thành công",
                "3 success", "", "3");

        // Chỉ đếm các đơn có STATUS_SUCCESS
        Assert.assertEquals(3, dashboardDao.getTotalOrders());
    }

    @Test
    public void testGetTotalUsers() {
        setTestCaseInfo("DASH_07", "Tổng khách hàng",
                "User test tồn tại", "", ">= 1");

        Assert.assertTrue(dashboardDao.getTotalUsers() >= 1);
    }

    @Test
    public void testGetTopSellingProducts() {
        setTestCaseInfo("DASH_08", "Top sản phẩm bán chạy",
                "Các đơn cùng 1 sản phẩm", "", "List > 0 + name");

        List<Map<String, Object>> list = dashboardDao.getTopSellingProducts(5);

        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.get(0).containsKey("name"));
        Assert.assertTrue(list.get(0).containsKey("total_sold"));
    }

    @Test
    public void testGetTopSellingProductsByDate() {
        setTestCaseInfo("DASH_09", "Top sản phẩm theo ngày",
                "1 đơn hôm nay = 1tr", "Today", "Doanh thu = 1tr");

        String today = LocalDateTime.now().toLocalDate().toString();
        List<Map<String, Object>> list = dashboardDao.getTopSellingProductsByDate(today, today);

        Assert.assertFalse(list.isEmpty());
        // Kiểm tra doanh thu của sản phẩm bán chạy nhất trong ngày hôm nay
        Assert.assertEquals(1000000.0, (double) list.get(0).get("revenue"), 0.01);
    }

    // --- GHI LOG KẾT QUẢ VÀO EXCEL ---
    /**
     * Rule giúp ghi kết quả Test Case vào Excel.
     * Sẽ được gọi tự động sau khi mỗi @Test hoàn thành (thành công hoặc thất bại).
     */
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) {
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS");
        }

        @Override protected void failed(Throwable e, Description d) {
            // Ghi lại thông báo lỗi (e.getMessage()) nếu test thất bại
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL");
        }
    };

    /**
     * Phương thức được gọi một lần sau khi tất cả các Test Case hoàn thành.
     * Dùng để xuất dữ liệu đã thu thập được ra file Excel cuối cùng.
     */
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_DashboardDao.xlsx");
    }
}