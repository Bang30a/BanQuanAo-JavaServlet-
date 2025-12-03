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

public class OrderDetailDaoIntegrationTest {

    private Connection connection;
    private OrderDetailDao orderDetailDao;

    // --- CẤU HÌNH DB (Check kỹ tên DB của bạn) ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String instance = "";
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck"; // Đã đổi thành shopduck theo log lỗi của bạn

    // --- BIẾN LƯU ID TỰ ĐỘNG TÌM ĐƯỢC ---
    private int validOrderId = 0;
    private int validVariantId = 0;

    // --- REPORT VARS ---
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
        // 1. Kết nối DB
        String url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName 
                   + ";encrypt=false;trustServerCertificate=true;loginTimeout=30";
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);
        orderDetailDao = new OrderDetailDao(connection);

        // 2. TỰ ĐỘNG LẤY ID HỢP LỆ TỪ DB (Fix lỗi Foreign Key)
        validOrderId = getAnyExistingId("Orders");
        validVariantId = getAnyExistingId("ProductVariants");

        // Nếu DB trống trơn, báo lỗi ngay để bạn biết
        if (validOrderId == -1) {
            throw new RuntimeException("❌ LỖI: Bảng 'Orders' trong DB đang trống! Bạn cần tạo ít nhất 1 đơn hàng fake trong DB để chạy test.");
        }
        if (validVariantId == -1) {
            throw new RuntimeException("❌ LỖI: Bảng 'ProductVariants' đang trống! Cần có ít nhất 1 sản phẩm.");
        }
    }

    // Hàm hỗ trợ tìm ID đầu tiên thấy trong bảng
    private int getAnyExistingId(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM " + tableName)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return -1;
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    @Test
    public void testAddAndGetDetail() {
        setTestCaseInfo("IT_OD_01", "Thêm và Kiểm tra tồn tại", 
                "1. addDetail() với OrderID có thật\n2. Check list", 
                "Order=" + validOrderId, "List chứa item vừa thêm");

        OrderDetails newItem = new OrderDetails();
        newItem.setOrderId(validOrderId);
        newItem.setProductVariantId(validVariantId);
        newItem.setQuantity(2);
        newItem.setPrice(50000.0);

        try {
            orderDetailDao.addDetail(connection, newItem);
        } catch (Exception e) {
            Assert.fail("Insert lỗi: " + e.getMessage());
        }

        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        Assert.assertFalse("Danh sách không được rỗng", list.isEmpty());
        
        // Check phần tử cuối
        OrderDetails lastItem = list.get(list.size() - 1);
        Assert.assertEquals(validVariantId, lastItem.getProductVariantId());
    }

    @Test
    public void testUpdateDetail() {
        setTestCaseInfo("IT_OD_03", "Update số lượng", 
                "Update item đầu tiên thành qty=99", "Qty=99", "Qty DB = 99");

        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        if (list.isEmpty()) return; // Skip

        OrderDetails item = list.get(0);
        int targetId = item.getId();
        int oldQty = item.getQuantity();

        item.setQuantity(99);
        orderDetailDao.updateDetail(item);

        OrderDetails updated = orderDetailDao.getDetailById(targetId);
        Assert.assertEquals("Update thất bại", 99, updated.getQuantity());

        // Revert (Dọn rác)
        item.setQuantity(oldQty);
        orderDetailDao.updateDetail(item);
    }

    @Test
    public void testDeleteDetail() {
        setTestCaseInfo("IT_OD_04", "Xóa chi tiết", 
                "Thêm item nháp rồi xóa", "Delete ID", "Get trả về null");

        // 1. Thêm nháp
        OrderDetails temp = new OrderDetails(0, validOrderId, validVariantId, 1, 10000);
        try { orderDetailDao.addDetail(connection, temp); } catch (Exception e) {}

        // 2. Lấy ID thằng vừa thêm (thằng cuối cùng)
        List<OrderDetails> list = orderDetailDao.getDetailsByOrderId(validOrderId);
        if (list.isEmpty()) Assert.fail("Không thêm được item để test xóa");
        
        OrderDetails itemToDelete = list.get(list.size() - 1);
        
        // 3. Xóa
        orderDetailDao.deleteDetail(itemToDelete.getId());

        // 4. Check
        OrderDetails deleted = orderDetailDao.getDetailById(itemToDelete.getId());
        Assert.assertNull("Vẫn tìm thấy item sau khi xóa", deleted);
    }

    // === EXCEL EXPORT ===
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
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDetailDao.xlsx");
    }
}