package integration;

import dao.ProductVariantDao;
import entity.ProductVariants;
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

public class ProductVariantDaoIntegrationTest {

    private Connection connection;
    private ProductVariantDao dao;

    // --- CẤU HÌNH DB (Bạn check lại tên DB nhé) ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String dbName = "shopduck"; 
    private final String userID = "sa";
    private final String password = "123456";

    // --- BIẾN LƯU ID TÌM ĐƯỢC ---
    private int validProductId = 0;
    private int validSizeId = 0;

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
        
        // Inject connection vào DAO để test
        dao = new ProductVariantDao(connection);

        // 2. TỰ ĐỘNG LẤY ID TỒN TẠI (Để tránh lỗi Foreign Key)
        validProductId = getAnyExistingId("Products");
        validSizeId = getAnyExistingId("Sizes");

        if (validProductId == -1) throw new RuntimeException("❌ Bảng Products đang trống!");
        if (validSizeId == -1) throw new RuntimeException("❌ Bảng Sizes đang trống! Cần thêm dữ liệu mẫu (S, M, L).");
    }

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

    // TEST 1: Thêm mới biến thể
    @Test
    public void testInsertVariant() {
        setTestCaseInfo("PV_DAO_01", "Thêm biến thể mới", 
                "Insert ProductId=" + validProductId + ", SizeId=" + validSizeId, 
                "Stock=50", "Return True");

        ProductVariants v = new ProductVariants();
        v.setProductId(validProductId);
        v.setSizeId(validSizeId);
        v.setStock(50);

        boolean result = dao.insertVariant(v);
        Assert.assertTrue("Insert phải thành công", result);
    }

    // TEST 2: Tìm kiếm theo Product ID
    @Test
    public void testFindByProductId() {
        setTestCaseInfo("PV_DAO_02", "Tìm biến thể theo SP", 
                "findByProductId(" + validProductId + ")", 
                "ProductId=" + validProductId, "List > 0");

        List<ProductVariants> list = dao.findByProductId(validProductId);
        Assert.assertFalse("Danh sách không được rỗng (vì vừa insert ở trên hoặc có sẵn)", list.isEmpty());
        
        // Kiểm tra dữ liệu join
        ProductVariants item = list.get(0);
        Assert.assertNotNull("Phải join được tên SP", item.getProductName());
        Assert.assertNotNull("Phải join được tên Size", item.getSizeName());
    }

    // TEST 3: Cập nhật tồn kho
    @Test
    public void testUpdateStock() {
        setTestCaseInfo("PV_DAO_03", "Cập nhật tồn kho", 
                "Lấy item đầu tiên -> Update stock=999", 
                "New Stock=999", "Return True");

        // Lấy đại 1 cái để sửa
        List<ProductVariants> list = dao.findByProductId(validProductId);
        if (list.isEmpty()) return;

        ProductVariants item = list.get(0);
        int oldStock = item.getStock();
        
        item.setStock(999);
        boolean updateResult = dao.updateVariant(item);
        Assert.assertTrue("Update phải thành công", updateResult);

        // Check lại
        ProductVariants updatedItem = dao.findById(item.getId());
        Assert.assertEquals("Stock phải là 999", 999, updatedItem.getStock());

        // Revert (trả lại như cũ)
        item.setStock(oldStock);
        dao.updateVariant(item);
    }

    // TEST 4: Xóa biến thể
    @Test
    public void testDeleteVariant() {
        setTestCaseInfo("PV_DAO_04", "Xóa biến thể", 
                "Thêm nháp -> Lấy ID -> Xóa", 
                "Temp Item", "FindById trả về null");

        // 1. Thêm nháp
        ProductVariants temp = new ProductVariants();
        temp.setProductId(validProductId);
        temp.setSizeId(validSizeId);
        temp.setStock(1);
        dao.insertVariant(temp);

        // 2. Lấy ID thằng vừa thêm (thằng cuối cùng trong list)
        List<ProductVariants> list = dao.findByProductId(validProductId);
        ProductVariants itemToDelete = list.get(list.size() - 1);

        // 3. Xóa
        boolean deleteResult = dao.deleteVariant(itemToDelete.getId());
        Assert.assertTrue("Delete phải thành công", deleteResult);

        // 4. Check lại
        ProductVariants deletedItem = dao.findById(itemToDelete.getId());
        Assert.assertNull("Đã xóa thì find phải null", deletedItem);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductVariantDao.xlsx");
    }
}