package integration;

import dao.OrderDetailDao;
import entity.OrderDetails;
import util.ExcelTestExporter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp OrderDetailDao.
 * Class này đảm bảo các bảng Orders và ProductVariants có dữ liệu nền tảng 
 * (Self-Healing) trước khi kiểm thử các thao tác CRUD chi tiết đơn hàng.
 */
public class OrderDetailDaoIntegrationTest {

    private Connection connection;
    private OrderDetailDao orderDetailDao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String dbName = "shopduck_test"; 
    private final String userID = "sa";
    private final String password = "123456";

    // --- BIẾN LƯU ID KHÓA NGOẠI HỢP LỆ (ĐƯỢC TỰ ĐỘNG TÌM/TẠO TRONG setUp) ---
    /** ID của đơn hàng (Orders) hợp lệ để chèn OrderDetail. */
    private int validOrderId = 0;
    /** ID của biến thể sản phẩm (ProductVariants) hợp lệ để chèn OrderDetail. */
    private int validVariantId = 0;

    // --- BIẾN GHI LOG TEST CASE (DÙNG CHO ExcelTestExporter) ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại, phục vụ cho việc xuất báo cáo Excel.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện.
     * @param data Dữ liệu đầu vào.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps;
        this.currentData = data; this.currentExpected = expected;
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case:
     * 1. Kết nối DB.
     * 2. Thực hiện cơ chế Self-Healing để đảm bảo các khóa ngoại (Orders, ProductVariants)
     * luôn có giá trị hợp lệ.
     * @throws Exception nếu có lỗi kết nối hoặc không thể tạo dữ liệu nền tảng.
     */
    @Before
    public void setUp() throws Exception {
        // 1. Kết nối DB
        String url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName 
                   + ";encrypt=false;trustServerCertificate=true;loginTimeout=30";
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);
        orderDetailDao = new OrderDetailDao(connection);

        // =================================================================
        // 2. CƠ CHẾ SELF-HEALING (Đảm bảo khóa ngoại tồn tại)
        // =================================================================

        // --- BƯỚC A: ĐẢM BẢO CÓ BIẾN THỂ SẢN PHẨM (ProductVariants) ---
        validVariantId = getAnyExistingId("ProductVariants");
        if (validVariantId == -1) {
            System.out.println("⚠️ Bảng ProductVariants trống. Đang tự động tạo dữ liệu...");
            
            // 1. Check Product -> Create if missing
            int pId = getAnyExistingId("Products");
            if (pId == -1) {
                executeSQL("INSERT INTO Products (name, price, description, image) VALUES (N'Auto Test Item', 50000, 'Desc', 'img.jpg')");
                pId = getAnyExistingId("Products");
            }

            // 2. Check Size -> Create if missing
            int sId = getAnyExistingId("Sizes");
            if (sId == -1) {
                executeSQL("INSERT INTO Sizes (size_label) VALUES ('TEST-SIZE')");
                sId = getAnyExistingId("Sizes");
            }

            // 3. Create Variant
            if (pId != -1 && sId != -1) {
                executeSQL("INSERT INTO ProductVariants (product_id, size_id, stock) VALUES (" + pId + ", " + sId + ", 100)");
                validVariantId = getAnyExistingId("ProductVariants");
            }
        }

        // --- BƯỚC B: ĐẢM BẢO CÓ ĐƠN HÀNG (Orders) ---
        validOrderId = getAnyExistingId("Orders");
        if (validOrderId == -1) {
            System.out.println("⚠️ Bảng Orders trống. Đang tự động tạo dữ liệu...");
            
            // 1. Check User (Cần User để tạo Order) -> Create if missing
            int uId = getAnyExistingId("Users");
            if (uId == -1) {
                // Thử chèn User tối đa field, nếu lỗi thì fallback
                try {
                    executeSQL("INSERT INTO Users (username, password, role, email) VALUES ('autotest', '123', 1, 'test@mail.com')");
                } catch (Exception e) {
                    executeSQL("INSERT INTO Users (username, password, role) VALUES ('autotest', '123', 1)");
                }
                uId = getAnyExistingId("Users");
            }

            // 2. Create Order
            if (uId != -1) {
                // Thử chèn Order với field created_at, nếu lỗi thì fallback
                try {
                    executeSQL("INSERT INTO Orders (user_id, total_money, status, created_at) VALUES (" + uId + ", 0, 1, GETDATE())");
                } catch (Exception e) {
                    executeSQL("INSERT INTO Orders (user_id, total_money, status) VALUES (" + uId + ", 0, 1)");
                }
                validOrderId = getAnyExistingId("Orders");
            }
        }

        // KIỂM TRA LẦN CUỐI
        if (validOrderId == -1) throw new RuntimeException("❌ KHÔNG THỂ TỰ TẠO ORDERS. Kiểm tra lại cấu trúc bảng Users/Orders.");
        if (validVariantId == -1) throw new RuntimeException("❌ KHÔNG THỂ TỰ TẠO VARIANTS. Kiểm tra lại cấu trúc bảng Products/Sizes.");
    }

    /**
     * Helper: Thực thi lệnh SQL (INSERT/UPDATE/DELETE) và in lỗi nếu có.
     * @param sql Câu lệnh SQL cần thực thi.
     */
    private void executeSQL(String sql) {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi executeSQL (" + sql + "): " + e.getMessage());
        }
    }

    /**
     * Helper: Lấy ID đầu tiên của một bảng.
     * @param tableName Tên bảng.
     * @return ID đầu tiên tìm thấy, hoặc -1 nếu bảng trống/lỗi.
     */
    private int getAnyExistingId(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM " + tableName)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {
            // Bỏ qua lỗi truy vấn nếu bảng không tồn tại hoặc cấu trúc khác
        }
        return -1;
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ================== CÁC TEST CASE ==================

    @Test
    public void testAddAndGetDetail() {
        setTestCaseInfo("IT_OD_01", "Thêm và Kiểm tra tồn tại", 
                "1. addDetail()\n2. Check list", 
                "Order=" + validOrderId, "List size > 0");

        OrderDetails newItem = new OrderDetails();
        newItem.setOrderId(validOrderId);
        newItem.setProductVariantId(validVariantId);
        newItem.setQuantity(2);
        newItem.setPrice(50000.0);

        try {
            orderDetailDao.addDetail(connection, newItem);
        } catch (Exception e) {
            Assert.fail("Insert chi tiết đơn hàng thất bại: " + e.getMessage());
        }

        // Kiểm tra sau khi insert
        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        Assert.assertFalse("Danh sách chi tiết đơn hàng không được rỗng sau khi thêm", list.isEmpty());
        
        OrderDetails lastItem = list.get(list.size() - 1);
        Assert.assertEquals("Kiểm tra Variant ID có đúng không", validVariantId, lastItem.getProductVariantId());
    }

    @Test
    public void testUpdateDetail() {
        setTestCaseInfo("IT_OD_02", "Update số lượng", 
                "Update item đầu tiên thành qty=99", "Qty=99", "Qty DB = 99");

        // Đảm bảo có item để update
        prepareDummyDetail();

        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        OrderDetails item = list.get(0);
        int targetId = item.getId();
        int oldQty = item.getQuantity();

        item.setQuantity(99);
        orderDetailDao.updateDetail(item);

        // Lấy lại từ DB để kiểm tra
        OrderDetails updated = orderDetailDao.getDetailById(targetId);
        Assert.assertEquals("Số lượng phải được cập nhật thành 99", 99, updated.getQuantity());

        // Revert (Quan trọng cho các test case sau)
        item.setQuantity(oldQty);
        orderDetailDao.updateDetail(item);
    }

    @Test
    public void testDeleteDetail() {
        setTestCaseInfo("IT_OD_03", "Xóa 1 chi tiết", 
                "Thêm item nháp -> Xóa -> Find lại", "Delete ID", "Get trả về null");

        // 1. Thêm item tạm thời
        OrderDetails temp = new OrderDetails(0, validOrderId, validVariantId, 1, 10000);
        try { orderDetailDao.addDetail(connection, temp); } catch (Exception e) {}

        // 2. Lấy ID và Xóa
        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        OrderDetails itemToDelete = list.get(list.size() - 1);
        
        orderDetailDao.deleteDetail(itemToDelete.getId());

        // 3. Kiểm tra kết quả
        OrderDetails deleted = orderDetailDao.getDetailById(itemToDelete.getId());
        Assert.assertNull("Không được tìm thấy chi tiết đơn hàng sau khi xóa", deleted);
    }

    @Test
    public void testJoinProductName() {
        setTestCaseInfo("IT_OD_04", "Kiểm tra JOIN Product Name", 
                "Lấy detail bất kỳ -> check field productName", 
                "Order=" + validOrderId, "ProductName != null");

        // Đảm bảo có item để test
        prepareDummyDetail();

        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        OrderDetails item = list.get(0);

        // Kiểm tra fieldProductName (chứng tỏ JOIN với bảng Products đã hoạt động)
        Assert.assertNotNull("Tên sản phẩm không được null (DAO phải JOIN với Product)", item.getProductName());
    }

    @Test
    public void testDeleteAllByOrderId() {
        setTestCaseInfo("IT_OD_05", "Xóa tất cả chi tiết theo OrderID", 
                "1. Add items\n2. deleteDetailsByOrderId\n3. Check list", 
                "Order=" + validOrderId, "List size = 0");

        // 1. Thêm ít nhất 1 item
        try {
            orderDetailDao.addDetail(connection, new OrderDetails(0, validOrderId, validVariantId, 1, 100));
        } catch (Exception ignored) {}

        // 2. Xóa tất cả theo Order ID
        try {
            orderDetailDao.deleteDetailsByOrderId(connection, validOrderId);
        } catch (Exception e) {
            Assert.fail("Lỗi khi gọi deleteDetailsByOrderId: " + e.getMessage());
        }

        // 3. Kiểm tra kết quả
        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        Assert.assertEquals("Danh sách chi tiết phải rỗng sau khi xóa tất cả", 0, list.size());
    }

    @Test
    public void testGetNonExistentOrderId() {
        setTestCaseInfo("IT_OD_06", "Lấy OrderID không tồn tại", 
                "getDetailsByOrderId(99999999)", 
                "ID Rác", "Trả về List rỗng (Ko null, ko crash)");

        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(99999999);
        // Đảm bảo hàm trả về List rỗng, không phải null và không crash
        Assert.assertNotNull("Phải trả về List, không được null", list);
        Assert.assertTrue("List phải rỗng", list.isEmpty());
    }

    /**
     * Helper: Đảm bảo có ít nhất 1 chi tiết đơn hàng tồn tại trong validOrderId.
     * Thường dùng trước các test case cần dựa vào dữ liệu đã có (ví dụ: Update, Join).
     */
    private void prepareDummyDetail() {
        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        if (list.isEmpty()) {
            try {
                orderDetailDao.addDetail(connection, new OrderDetails(0, validOrderId, validVariantId, 1, 100));
            } catch (Exception ignored) {}
        }
    }

    // === GHI LOG VÀ XUẤT BÁO CÁO ===

    /**
     * Rule giúp ghi kết quả Test Case vào Excel.
     * Rule này được gọi tự động sau khi mỗi @Test hoàn thành (thành công hoặc thất bại).
     */
    @Rule public TestWatcher watcher = new TestWatcher() {
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
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDetailDao.xlsx");
    }
}