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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp ProductVariantDao.
 * Class này kiểm thử các nghiệp vụ CRUD của Biến thể Sản phẩm và đảm bảo
 * dữ liệu nền tảng (Products, Sizes) tồn tại thông qua cơ chế Self-Healing.
 */
public class ProductVariantDaoIntegrationTest {

    private Connection connection;
    private ProductVariantDao dao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String fullDbUrl = "jdbc:sqlserver://BANGGG:1433;databaseName=shopduck_test;encrypt=false;trustServerCertificate=true;loginTimeout=30";
    private final String userID = "sa";
    private final String password = "123456";

    // --- BIẾN LƯU ID KHÓA NGOẠI HỢP LỆ ---
    private int validProductId = 0;
    private int validSizeId = 0;
    /** ID biến thể được tạo trong các test case, dùng để cleanup trong tearDown. */
    private int insertedVariantId = 0; 

    // --- BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ---
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện.
     * @param data Dữ liệu đầu vào/chuẩn bị.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case:
     * 1. Mở kết nối DB và khởi tạo DAO.
     * 2. Thực hiện cơ chế Self-Healing: Đảm bảo Product và Size hợp lệ tồn tại.
     * 3. Cleanup: Xóa dữ liệu tạm thời/rác để môi trường sạch.
     * @throws Exception nếu có lỗi kết nối hoặc không thể tạo dữ liệu mẫu.
     */
    @Before
    public void setUp() throws Exception {
        // 1. Kết nối DB
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(fullDbUrl, userID, password);
        dao = new ProductVariantDao(connection);

        // 2. TỰ ĐỘNG LẤY ID TỒN TẠI (Self-Healing)
        // Đảm bảo Product ID hợp lệ
        validProductId = getAnyExistingId("Products");
        if (validProductId == -1) {
            createDummyProduct(); 
            validProductId = getAnyExistingId("Products");
        }

        // Đảm bảo Size ID hợp lệ
        validSizeId = getAnyExistingId("Sizes");
        if (validSizeId == -1) {
            createDummySize(); 
            validSizeId = getAnyExistingId("Sizes");
        }

        // Kiểm tra lần cuối
        if (validProductId == -1) throw new RuntimeException("❌ Không thể tạo dữ liệu mẫu cho Products!");
        if (validSizeId == -1) throw new RuntimeException("❌ Không thể tạo dữ liệu mẫu cho Sizes!");
        
        // 3. Cleanup tạm thời (Xóa hết data test cũ để sạch sẽ)
        cleanupTempData();
    }
    
    /**
     * Helper: Tự động tạo Product mẫu nếu bảng Products trống.
     */
    private void createDummyProduct() {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("INSERT INTO Products (name, price, description, image) VALUES (N'Auto Test Product', 10000, N'Description', 'test.jpg')");
        } catch (Exception e) { 
            System.err.println("Lỗi tạo Dummy Product: " + e.getMessage());
        }
    }

    /**
     * Helper: Tự động tạo Size mẫu nếu bảng Sizes trống.
     */
    private void createDummySize() {
        try (Statement s = connection.createStatement()) {
            // [FIX ERROR] Rút ngắn chuỗi 'TEST-SIZE' thành 'TS' để tránh lỗi truncation
            // do cột size_label trong DB có thể giới hạn ký tự (ví dụ varchar(5))
            s.executeUpdate("INSERT INTO Sizes (size_label) VALUES ('TS')");
        } catch (Exception e) {
            System.err.println("Lỗi tạo Dummy Size: " + e.getMessage());
        }
    }

    /**
     * Helper: Lấy ID đầu tiên của một bảng.
     * @param tableName Tên bảng (Ví dụ: Products, Sizes).
     * @return ID đầu tiên tìm thấy, hoặc -1 nếu bảng trống/lỗi.
     */
    private int getAnyExistingId(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM " + tableName)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return -1;
    }

    /**
     * Helper: Xóa các biến thể tạm thời được tạo ra từ các lần chạy test trước.
     * Xóa dựa trên giá trị stock=9999 hoặc cặp (validProductId, validSizeId).
     */
    private void cleanupTempData() {
        String sql = "DELETE FROM ProductVariants WHERE stock = 9999 OR product_id = ? AND size_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, validProductId);
            ps.setInt(2, validSizeId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /**
     * Helper: Lấy ID lớn nhất (ID mới nhất) từ bảng ProductVariants.
     * @return ID biến thể mới nhất, hoặc -1 nếu lỗi.
     */
    private int getLatestVariantId() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM ProductVariants ORDER BY id DESC")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return -1;
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case:
     * 1. Xóa biến thể vừa được tạo (nếu có).
     * 2. Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        // Cleanup biến thể đã insert trong Test Case hiện tại
        if (insertedVariantId > 0) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM ProductVariants WHERE id = " + insertedVariantId);
            } catch (Exception ignored) {}
        }
        // Đóng kết nối
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ========================== TEST CASES ===============================

    // TEST 1 - INSERT (CREATE)
    @Test
    public void testInsertVariant() throws Exception {
        setTestCaseInfo("PV_DAO_01", "Thêm biến thể mới", 
                "Insert ProductId=" + validProductId + ", Stock=99", 
                "Stock=99", "Return true");

        ProductVariants v = new ProductVariants();
        v.setProductId(validProductId);
        v.setSizeId(validSizeId);
        v.setStock(99);
        v.setPrice(1.0); 

        boolean result = dao.insertVariant(v);
        Assert.assertTrue("Insert phải thành công", result);

        insertedVariantId = getLatestVariantId();
        Assert.assertTrue("ID biến thể phải được sinh ra (> 0)", insertedVariantId > 0);
    }

    // TEST 2 - CHECK EXIST (READ)
    @Test
    public void testCheckExist() {
        setTestCaseInfo("PV_DAO_02", "Kiểm tra tồn tại (checkExist)", 
                "Insert ProductId=" + validProductId + ", SizeId=" + validSizeId, 
                "Item đã tồn tại", "Return Object (ID, Stock)");
        
        // 1. Insert tạm
        ProductVariants temp = new ProductVariants();
        temp.setProductId(validProductId);
        temp.setSizeId(validSizeId);
        temp.setStock(500); 
        dao.insertVariant(temp);
        
        insertedVariantId = getLatestVariantId();
        
        // 2. Check Exist
        ProductVariants found = dao.checkExist(validProductId, validSizeId);

        // 3. Verify
        Assert.assertNotNull("Phải tìm thấy biến thể đã tồn tại", found);
        Assert.assertEquals("Kiểm tra Stock", 500, found.getStock()); 
        Assert.assertEquals("Kiểm tra ID trả về", insertedVariantId, found.getId());
    }

    // TEST 3 - UPDATE
    @Test
    public void testUpdateStock() throws Exception {
        setTestCaseInfo("PV_DAO_03", "Cập nhật tồn kho", 
                "Update Stock 9999", 
                "New Stock=9999", "Stock = 9999");

        // 1. Insert tạm
        ProductVariants temp = new ProductVariants();
        temp.setProductId(validProductId);
        temp.setSizeId(validSizeId);
        temp.setStock(10); 
        dao.insertVariant(temp);
        insertedVariantId = getLatestVariantId();

        // 2. Update
        temp.setId(insertedVariantId);
        temp.setStock(9999);
        temp.setPrice(100.0); 

        boolean updateResult = dao.updateVariant(temp);
        Assert.assertTrue("Update phải thành công", updateResult);

        // 3. Verify bằng cách FindById
        ProductVariants updatedItem = dao.findById(insertedVariantId);
        Assert.assertEquals("Stock phải là 9999 sau khi update", 9999, updatedItem.getStock());
    }

    // TEST 4 - FIND BY PRODUCT ID (READ)
    @Test
    public void testFindByProductId() throws Exception {
        setTestCaseInfo("PV_DAO_04", "Tìm biến thể theo SP", 
                "findByProductId(" + validProductId + ")", 
                "ProductId=" + validProductId, "List size > 0 (và có join)");

        // 1. Insert một dòng dữ liệu giả để đảm bảo có cái để tìm
        ProductVariants temp = new ProductVariants();
        temp.setProductId(validProductId);
        temp.setSizeId(validSizeId);
        temp.setStock(5);
        dao.insertVariant(temp);
        
        // Lưu ID lại để tí nữa hàm tearDown còn biết đường xóa
        insertedVariantId = getLatestVariantId(); 

        // 2. Thực hiện tìm kiếm
        List<ProductVariants> list = dao.findByProductId(validProductId);
        
        // 3. Verify
        Assert.assertFalse("Danh sách không được rỗng", list.isEmpty());
        
        // Kiểm tra dữ liệu join (ProductName, SizeName)
        ProductVariants item = list.get(0);
        Assert.assertNotNull("Phải join được tên SP (ProductName)", item.getProductName());
        Assert.assertNotNull("Phải join được tên Size (SizeName)", item.getSizeName());
    }

    // TEST 5 - DELETE
    @Test
    public void testDeleteVariant() throws Exception {
        setTestCaseInfo("PV_DAO_05", "Xóa biến thể", 
                "Insert nháp -> Delete", 
                "Temp Item", "FindById trả về null");

        // 1. Insert tạm
        ProductVariants temp = new ProductVariants();
        temp.setProductId(validProductId);
        temp.setSizeId(validSizeId);
        temp.setStock(1);
        dao.insertVariant(temp);
        int deleteId = getLatestVariantId();

        // 2. Delete
        boolean deleteResult = dao.deleteVariant(deleteId);
        Assert.assertTrue("Delete phải thành công", deleteResult);

        // 3. Verify
        ProductVariants deletedItem = dao.findById(deleteId);
        Assert.assertNull("Đã xóa thì findById phải trả về null", deletedItem);
    }
    
    // TEST 6 - FOREIGN KEY VIOLATION (FAILURE SCENARIO)
    @Test
    public void testInsertVariant_ForeignKeyViolation() throws Exception {
        setTestCaseInfo("PV_DAO_06", "Lỗi Khóa ngoại (Insert)", 
                "Insert với ProductId rác (-1)", 
                "PID=-1", "DAO trả về False");

        ProductVariants v = new ProductVariants();
        v.setProductId(-1); // ID rác (vi phạm FK)
        v.setSizeId(validSizeId);
        v.setStock(1);
        
        // DAO phải bắt exception và trả về false (không crash)
        boolean result = dao.insertVariant(v); 
        Assert.assertFalse("Insert phải trả về false vì lỗi FK", result);
    }

    // ====================== XUẤT BÁO CÁO (JUNIT RULE) ======================
    /**
     * Rule giúp ghi kết quả Test Case (PASS/FAIL) vào Excel.
     * Rule này được Junit gọi tự động sau khi mỗi @Test hoàn thành.
     */
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            // Ghi kết quả thành công
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductVariantDao.xlsx");
    }
}