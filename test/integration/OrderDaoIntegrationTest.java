package integration;

import dao.OrderDao;
import entity.Orders;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp OrderDao.
 * Class này thiết lập kết nối tới DB test, đảm bảo dữ liệu nền tảng (User) tồn tại,
 * và kiểm thử các nghiệp vụ CRUD và tìm kiếm của OrderDao.
 */
public class OrderDaoIntegrationTest {

    private Connection connection;
    private OrderDao orderDao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String dbName = "shopduck_test";
    private final String userID = "sa";
    private final String password = "123456";

    /** ID của User hợp lệ được sử dụng cho tất cả các đơn hàng test (đảm bảo khóa ngoại). */
    private int validUserId = 1;

    // --- THÔNG TIN GHI LOG TEST CASE (DÙNG CHO ExcelTestExporter) ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     * Thông tin này sẽ được ghi vào file Excel báo cáo.
     *
     * @param id ID của Test Case (Ví dụ: ORD_DAO_01)
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
     * 1. Thiết lập kết nối đến DB test.
     * 2. Đảm bảo User mẫu tồn tại để tránh lỗi khóa ngoại.
     * 3. Reset dữ liệu Orders và OrderDetails.
     * 4. Inject Connection vào OrderDao.
     * @throws Exception nếu có lỗi kết nối hoặc thao tác DB.
     */
    @Before
    public void setUp() throws Exception {
        // 1) Kết nối DB Test
        String url =
                "jdbc:sqlserver://" + serverName + ":" + portNumber +
                        ";databaseName=" + dbName +
                        ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // 2) Đảm bảo tồn tại User hợp lệ trước khi Insert Order
        try (Statement s = connection.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT TOP 1 id FROM Users");
            if (rs.next()) {
                validUserId = rs.getInt(1);
            } else {
                // Nếu bảng Users trống → thêm user test để có FK hợp lệ
                String insertSql = "INSERT INTO Users (username, password, role) VALUES ('user_test_order', '123', 0)";
                try {
                    // Thử chèn thêm field email nếu có
                    insertSql = "INSERT INTO Users (username, password, role, email) VALUES ('user_test_order', '123', 0, 'test@order.com')";
                    s.executeUpdate(insertSql);
                } catch (Exception e) {
                    // Nếu lỗi (ví dụ: không có field email)
                    s.executeUpdate("INSERT INTO Users (username, password, role) VALUES ('user_test_order', '123', 0)");
                }
                rs = s.executeQuery("SELECT TOP 1 id FROM Users WHERE username = 'user_test_order'");
                if (rs.next()) validUserId = rs.getInt(1);
            }
        }

        // 3) Reset dữ liệu Orders để môi trường test sạch
        try (Statement stmt = connection.createStatement()) {
            // Xóa OrderDetails trước do ràng buộc khóa ngoại
            try { stmt.execute("DELETE FROM OrderDetails"); } catch (Exception ignored) {}
            stmt.execute("DELETE FROM Orders");
        }

        // 4) Inject Connection vào DAO
        orderDao = new OrderDao(connection);
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ====================== TEST CASE ======================
    // Lưu ý: Các test case được đặt tên rõ ràng theo chức năng của DAO.

    @Test
    public void testAddOrder_Success() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_01",
                "Thêm đơn hàng",
                "Tạo Order → addOrder → kiểm tra ID & dữ liệu",
                "UserID=" + validUserId + ", Total=500k",
                "Insert thành công (ID > 0)"
        );

        Orders order = new Orders();
        order.setUserId(validUserId);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setTotal(500000);
        order.setAddress("Hanoi");
        order.setPhone("0988888888");
        order.setStatus("Pending");

        // Gọi phương thức DAO
        int id = orderDao.addOrder(connection, order);

        // Kiểm tra kết quả
        Assert.assertTrue("ID đơn hàng phải lớn hơn 0 sau khi insert", id > 0);

        Orders inserted = orderDao.getOrderById(id);
        Assert.assertNotNull("Phải tìm thấy đơn hàng vừa insert", inserted);
        Assert.assertEquals("Kiểm tra giá trị Total", 500000, inserted.getTotal(), 0.01);
    }

    @Test
    public void testGetAllOrders() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_02",
                "Lấy danh sách đơn hàng",
                "Insert 2 đơn → getAllOrders",
                "2 Orders",
                "Danh sách trả về = 2"
        );

        // Setup dữ liệu
        Orders o1 = new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "A", "1", "New");
        Orders o2 = new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 200, "B", "2", "New");

        orderDao.addOrder(connection, o1);
        orderDao.addOrder(connection, o2);

        // Kiểm tra kết quả
        List<Orders> list = orderDao.getAllOrders();
        Assert.assertEquals("Tổng số đơn hàng phải bằng 2", 2, list.size());
    }

    @Test
    public void testUpdateOrder() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_03",
                "Cập nhật đơn hàng",
                "Insert đơn → Update → kiểm tra lại",
                "Pending → Shipped; Total 100 → 999",
                "Status cập nhật thành Shipped và Total = 999"
        );

        // 1. Insert đơn hàng ban đầu
        Orders order = new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "HN", "099", "Pending");
        int id = orderDao.addOrder(connection, order);
        order.setId(id);

        // 2. Cập nhật
        order.setStatus("Shipped");
        order.setTotal(999);
        orderDao.updateOrder(order);

        // 3. Kiểm tra kết quả
        Orders updated = orderDao.getOrderById(id);
        Assert.assertEquals("Trạng thái phải được cập nhật thành Shipped", "Shipped", updated.getStatus());
        Assert.assertEquals("Tổng tiền phải được cập nhật thành 999", 999, updated.getTotal(), 0.01);
    }

    @Test
    public void testDeleteOrder() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_04",
                "Xóa đơn hàng",
                "Insert → Delete → kiểm tra lại",
                "ID vừa tạo",
                "Không tìm thấy (null)"
        );

        // 1. Insert
        Orders order = new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "HN", "099", "Pending");
        int id = orderDao.addOrder(connection, order);

        // 2. Delete
        orderDao.deleteOrder(id);

        // 3. Kiểm tra kết quả
        Orders deleted = orderDao.getOrderById(id);
        Assert.assertNull("Đơn hàng phải không tồn tại sau khi xóa", deleted);
    }

    @Test
    public void testGetOrdersByStatus() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_05",
                "Lọc đơn theo trạng thái",
                "Insert: 1 Pending, 1 Done → filter Pending",
                "Status=Pending",
                "Trả về đúng 1 kết quả"
        );

        // Setup dữ liệu
        orderDao.addOrder(connection, new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "A", "1", "Pending"));
        orderDao.addOrder(connection, new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 200, "B", "2", "Done"));

        // Lọc
        List<Orders> list = orderDao.getOrdersByStatus("Pending");

        // Kiểm tra kết quả
        Assert.assertEquals("Chỉ có 1 đơn Pending", 1, list.size());
        Assert.assertEquals("Kiểm tra trạng thái của đơn hàng được trả về", "Pending", list.get(0).getStatus());
    }

    @Test
    public void testGetOrdersByUserId() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_06",
                "Lấy lịch sử đơn hàng theo User",
                "Insert 2 đơn của user → getByUserId",
                "UserID=" + validUserId,
                "Danh sách = 2"
        );

        // Setup dữ liệu
        orderDao.addOrder(connection, new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "A", "1", "Pending"));
        orderDao.addOrder(connection, new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 200, "B", "2", "Pending"));

        // Kiểm tra kết quả cho user hợp lệ
        List<Orders> list = orderDao.getOrdersByUserId(validUserId);
        Assert.assertEquals("Phải tìm thấy 2 đơn hàng", 2, list.size());

        // Kiểm tra kết quả cho user không hợp lệ (user 99999 không có đơn hàng)
        Assert.assertEquals("User không tồn tại hoặc không có đơn hàng phải trả về 0", 0, orderDao.getOrdersByUserId(99999).size());
    }

    @Test
    public void testGetOrderByIdAndUserId() throws Exception {
        setTestCaseInfo(
                "ORD_DAO_07",
                "Kiểm tra quyền sở hữu đơn hàng",
                "User A thấy đơn của mình → User B không thấy",
                "Order thuộc User A",
                "User khác phải nhận null"
        );

        // 1. Insert đơn hàng cho validUserId (User A)
        Orders order = new Orders(0, validUserId, Timestamp.valueOf(LocalDateTime.now()), 100, "A", "1", "Pending");
        int orderId = orderDao.addOrder(connection, order);

        // 2. Kiểm tra với User A (chủ sở hữu)
        Orders found = orderDao.getOrderByIdAndUserId(orderId, validUserId);
        Assert.assertNotNull("Chủ sở hữu phải tìm thấy đơn hàng", found);

        // 3. Kiểm tra với User B (user khác)
        int fakeUserId = validUserId + 100;
        Orders notFound = orderDao.getOrderByIdAndUserId(orderId, fakeUserId);
        Assert.assertNull("User khác không được phép tìm thấy đơn hàng này", notFound);
    }

    // --- LOGGING VÀ XUẤT BÁO CÁO ---

    /**
     * Rule giúp ghi kết quả Test Case vào Excel.
     * Rule này được gọi tự động sau khi mỗi @Test hoàn thành (thành công hoặc thất bại).
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
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDao.xlsx");
    }
}