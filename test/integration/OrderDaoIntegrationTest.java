package integration;

import dao.OrderDao;
import entity.Orders;
import util.ExcelTestExporter; // <-- Import class tiện ích

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

// Import JUNIT
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class OrderDaoIntegrationTest {

    private Connection connection;
    private OrderDao orderDao;

    // --- CẤU HÌNH DB ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String instance = "";
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck_test"; 

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        String url;
        if (instance == null || instance.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName;
        } else {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + "\\" + instance + ";databaseName=" + dbName;
        }
        url += ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // Clean State
        try (Statement stmt = connection.createStatement()) {
            try { stmt.execute("DELETE FROM OrderDetails"); } catch (Exception e) {}
            stmt.execute("DELETE FROM Orders");
        }

        orderDao = new OrderDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testAddOrder_Success() throws Exception {
        setTestCaseInfo("ORD_DAO_01", "Thêm đơn hàng (DB thật)", 
                "1. Tạo Order Object\n2. Call addOrder\n3. Check ID trả về", 
                "User: 1, Total: 500k", "Insert thành công, ID > 0");

        Orders order = new Orders();
        order.setUserId(1);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setTotal(500000);
        order.setAddress("Hanoi");
        order.setPhone("0988888888");
        order.setStatus("Pending");

        int id = orderDao.addOrder(connection, order);

        Assert.assertTrue("Phải trả về ID > 0", id > 0);
        
        // Verify lại bằng cách query
        Orders inserted = orderDao.getOrderById(id);
        Assert.assertNotNull(inserted);
        Assert.assertEquals(500000, inserted.getTotal(), 0.01);
    }

    @Test
    public void testGetAllOrders() throws Exception {
        setTestCaseInfo("ORD_DAO_02", "Lấy danh sách đơn hàng", 
                "1. Insert 2 đơn\n2. Call getAllOrders", 
                "2 Orders", "Size = 2");

        Orders o1 = new Orders(1, Timestamp.valueOf(LocalDateTime.now()), 100, "A", "1", "New");
        Orders o2 = new Orders(1, Timestamp.valueOf(LocalDateTime.now()), 200, "B", "2", "New");
        
        orderDao.addOrder(connection, o1);
        orderDao.addOrder(connection, o2);

        List<Orders> list = orderDao.getAllOrders();
        Assert.assertEquals(2, list.size());
    }

    // === XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDao.xlsx");
    }
}