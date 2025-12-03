package integration;

import dao.DashboardDao;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import util.ExcelTestExporter;

public class DashboardDaoIntegrationTest {

    private Connection connection;
    private DashboardDao dashboardDao;

    // --- CẤU HÌNH DB (Bạn giữ nguyên như cũ) ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck"; 

    // --- [QUAN TRỌNG] CẤU HÌNH TRẠNG THÁI ĐƠN HÀNG ---
    // Dựa trên hình ảnh bạn gửi, "Đã giao" là chính xác.
    // Tôi đổi "Đang xử lý" thành "Chờ xử lý". Nếu chạy vẫn lỗi, bạn hãy thử đổi thành "Chờ xác nhận"
    private final String STATUS_SUCCESS = "Đã giao";      
    private final String STATUS_PROCESSING = "Chờ xử lý"; 

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        // 1. Kết nối
        String url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName + ";encrypt=false;trustServerCertificate=true;loginTimeout=30";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // 2. Dọn dẹp DB
        try (Statement stmt = connection.createStatement()) {
            try { stmt.execute("DELETE FROM OrderDetails"); } catch (Exception e) {}
            stmt.execute("DELETE FROM Orders");
        }
        
        // 3. Đảm bảo User tồn tại
        ensureUserExists(1);
        dashboardDao = new DashboardDao(); 
        
        // 4. INSERT DỮ LIỆU MẪU (Dùng biến STATUS đã khai báo ở trên)
        insertOrder(1000000, STATUS_SUCCESS, LocalDateTime.now()); 
        insertOrder(500000, STATUS_SUCCESS, LocalDateTime.now().minusDays(1));
        insertOrder(2000000, STATUS_SUCCESS, LocalDateTime.now().minusMonths(1));
        
        // Đơn này dùng trạng thái khác để test biểu đồ tròn
        insertOrder(5000000, STATUS_PROCESSING, LocalDateTime.now());
    }
    
    private void ensureUserExists(int userId) {
        String checkSql = "SELECT count(*) FROM Users WHERE id = ?";
        String insertSql = "SET IDENTITY_INSERT Users ON; " +
                           "INSERT INTO Users (id, username, password, role) VALUES (?, 'testuser', '123', 'user'); " +
                           "SET IDENTITY_INSERT Users OFF;";
        try {
            PreparedStatement ps = connection.prepareStatement(checkSql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement psInsert = connection.prepareStatement(insertSql);
                psInsert.setInt(1, userId);
                psInsert.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Lỗi tạo user giả (có thể bỏ qua): " + e.getMessage());
        }
    }

    private void insertOrder(double total, String status, LocalDateTime date) throws Exception {
        String sqlOrder = "INSERT INTO Orders (user_id, order_date, total, status, address, phone) VALUES (1, ?, ?, ?, N'HCM', '0909')";
        
        try (PreparedStatement ps = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(date));
            ps.setDouble(2, total); 
            
            // [FIX QUAN TRỌNG] Dùng setNString để gửi tiếng Việt có dấu chuẩn xác vào Nvarchar
            ps.setNString(3, status); 
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if(rs.next()) {
                    int orderId = rs.getInt(1);
                    String sqlDetail = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) VALUES (?, 1, 1, ?)";
                    try(PreparedStatement ps2 = connection.prepareStatement(sqlDetail)) {
                        ps2.setInt(1, orderId);
                        ps2.setDouble(2, total); 
                        ps2.executeUpdate();
                    }
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // --- CÁC TEST CASE ---

    @Test
    public void testGetTotalRevenue() {
        setTestCaseInfo("DASH_01", "Tổng doanh thu toàn thời gian", 
                "1tr + 500k + 2tr (" + STATUS_SUCCESS + ")", 
                "Chỉ tính đơn thành công", "Total = 3.500.000");

        double revenue = dashboardDao.getTotalRevenue();
        Assert.assertEquals(3500000.0, revenue, 0.01);
    }
    
    @Test
    public void testGetRevenueByDate_Today() {
        setTestCaseInfo("DASH_02", "Doanh thu hôm nay (Lọc)", 
                "Lọc startDate = endDate = Hôm nay", 
                "Today", "1.000.000");

        String today = LocalDateTime.now().toLocalDate().toString();
        double revenue = dashboardDao.getRevenueByDate(today, today);
        Assert.assertEquals(1000000.0, revenue, 0.01);
    }

    @Test
    public void testGetOrderCountByDate() {
        setTestCaseInfo("DASH_03", "Đếm đơn thành công hôm nay", 
                "Có 1 đơn Success, 1 đơn Processing", 
                "Today", "Count = 1");

        String today = LocalDateTime.now().toLocalDate().toString();
        int count = dashboardDao.getOrderCountByDate(today, today);
        Assert.assertEquals(1, count);
    }

    @Test
    public void testGetRevenueLast7Days() {
        setTestCaseInfo("DASH_04", "Dữ liệu biểu đồ 7 ngày", 
                "Có đơn hôm nay và hôm qua", 
                "Data Setup", "List size >= 2");

        List<Map<String, Object>> list = dashboardDao.getRevenueLast7Days();
        Assert.assertTrue("Phải có dữ liệu cho biểu đồ", list.size() >= 2);
    }

    @Test
    public void testGetOrderStatusStats() {
        setTestCaseInfo("DASH_05", "Thống kê trạng thái (Pie Chart)", 
                "3 Success, 1 Processing", 
                "Data Setup", "List size = 2 nhóm");

        List<Map<String, Object>> list = dashboardDao.getOrderStatusStats();
        // Nếu DB của bạn chỉ cho phép 1 trạng thái duy nhất thì test này sẽ fail.
        // Hy vọng là nó cho phép "Chờ xử lý".
        Assert.assertEquals("Phải có 2 nhóm trạng thái khác nhau", 2, list.size());
    }

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_DashboardDao.xlsx");
    }
}