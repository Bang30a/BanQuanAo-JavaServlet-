// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.ProductVariants;
import entity.Products;
import entity.Size;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT ĐỂ TẠO BÁO CÁO EXCEL/CSV ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * INTEGRATION TEST cho ProductVariantDao (Chạy trên H2 Database).
 * Xuất kết quả ra file Excel (CSV).
 */
public class ProductVariantDaoTest {

    private static ProductVariantDao variantDao;
    private static ProductDao productDao; 
    private static SizeDao sizeDao; 
    
    private static Connection h2Connection;

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách lưu kết quả để xuất Excel
    private static final List<String[]> finalReportData = new ArrayList<>();

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (AUTOCOMMIT=OFF để kiểm soát transaction thủ công)
        String dbUrl = "jdbc:h2:mem:variant_testdb;DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", "");
        
        // 3. Tiêm (Inject) connection H2 vào các DAO
        variantDao = new ProductVariantDao(h2Connection);
        productDao = new ProductDao(h2Connection);
        sizeDao = new SizeDao(h2Connection);
        
        // 4. TẠO CÁC BẢNG (Schema)
        try (Statement stmt = h2Connection.createStatement()) {
            // Phải tạo bảng cha trước
            stmt.execute("CREATE TABLE Products (" +
                         " id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255)," +
                         " description VARCHAR(1000), price DOUBLE," +
                         " image VARCHAR(255), category_id INT)");
                         
            stmt.execute("CREATE TABLE Sizes (" +
                         " id INT PRIMARY KEY AUTO_INCREMENT, size_label VARCHAR(50))");
            
            // Tạo bảng chính để test (có khóa ngoại)
            stmt.execute("CREATE TABLE ProductVariants (" +
                         " id INT PRIMARY KEY AUTO_INCREMENT, product_id INT," +
                         " size_id INT, stock INT," +
                         " FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE," +
                         " FOREIGN KEY (size_id) REFERENCES Sizes(id) ON DELETE CASCADE)");
        }
        
        // 5. Commit (lưu) việc tạo bảng
        h2Connection.commit();
    }

    // === CHẠY TRƯỚC MỖI TEST CASE ===
    @Before
    public void setUp() throws Exception {
        // Dọn dẹp dữ liệu (dùng DELETE thay vì TRUNCATE do ràng buộc khóa ngoại)
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM ProductVariants"); // Xóa bảng CON trước
            stmt.execute("DELETE FROM Products"); // Xóa bảng CHA
            stmt.execute("DELETE FROM Sizes"); // Xóa bảng CHA
            
            // Reset ID về 1
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Sizes ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE ProductVariants ALTER COLUMN id RESTART WITH 1");
        }
        
        // CHÈN DỮ LIỆU "MỒI" (Dependencies)
        // Cần 1 Product (ID=1) và 1 Size (ID=1) để Variant có thể tham chiếu
        productDao.insert(new Products(0, "Áo Thun", "Áo", 100.0, "img.jpg"));
        sizeDao.insertSize(new Size(0, "M"));
        
        h2Connection.commit(); // Commit (lưu) việc dọn dẹp và chèn "mồi"
    }

    // Hàm điền thông tin Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testInsertVariantAndFindById() throws Exception {
        setTestCaseInfo(
            "DAO_VAR_01", 
            "Thêm biến thể & Lấy theo ID", 
            "1. Có sẵn SP(1) và Size(1)\n2. Insert Variant\n3. GetById check Info", 
            "Stock: 50, ProdID: 1, SizeID: 1", 
            "Insert -> True\nGetById -> Stock=50, Name='Áo Thun', Size='M'"
        );

        // --- ARRANGE ---
        ProductVariants newVariant = new ProductVariants(0, 1, 1, 50); // (id, prod_id, size_id, stock)
        
        // --- ACT ---
        boolean insertSuccess = variantDao.insertVariant(newVariant);
        h2Connection.commit();
        
        ProductVariants foundVariant = variantDao.findById(1);

        // --- ASSERT ---
        assertTrue("Insert phải trả về true", insertSuccess);
        assertNotNull("Phải tìm thấy biến thể", foundVariant);
        assertEquals("Stock phải là 50", 50, foundVariant.getStock());
        
        // Kiểm tra xem JOIN có hoạt động không (Lấy tên sản phẩm và tên size)
        assertEquals("Tên sản phẩm phải là 'Áo Thun'", "Áo Thun", foundVariant.getProductName());
        assertEquals("Tên size phải là 'M'", "M", foundVariant.getSizeName());
        assertEquals("Giá phải là 100.0", 100.0, foundVariant.getPrice(), 0.001);
    }
    
    @Test
    public void testFindByProductId() throws Exception {
        setTestCaseInfo(
            "DAO_VAR_02", 
            "Lấy danh sách biến thể theo ProductID", 
            "1. Thêm Size 'L'(2)\n2. Insert Variant size M & L cho SP(1)\n3. FindByProductId(1)", 
            "SP(1) có 2 variants", 
            "List size = 2\nCheck SizeName item 2"
        );

        // --- ARRANGE ---
        sizeDao.insertSize(new Size(0, "L"));
        h2Connection.commit();
        
        // Tạo 2 variant cho cùng Product ID=1
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (ID=1, Prod=1, Size=M)
        variantDao.insertVariant(new ProductVariants(0, 1, 2, 30)); // (ID=2, Prod=1, Size=L)
        h2Connection.commit();
        
        // --- ACT ---
        List<ProductVariants> list = variantDao.findByProductId(1);

        // --- ASSERT ---
        assertNotNull(list);
        assertEquals("Phải tìm thấy 2 biến thể", 2, list.size());
        assertEquals("Size của biến thể thứ 2 phải là 'L'", "L", list.get(1).getSizeName());
    }

    @Test
    public void testUpdateVariant() throws Exception {
        setTestCaseInfo(
            "DAO_VAR_03", 
            "Cập nhật số lượng tồn kho (Stock)", 
            "1. Insert Variant (Stock 50)\n2. Update Stock -> 99", 
            "Old: 50 -> New: 99", 
            "GetById -> Stock = 99"
        );

        // --- ARRANGE ---
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (ID=1)
        h2Connection.commit();
        
        ProductVariants variantToUpdate = variantDao.findById(1);
        
        // --- ACT ---
        variantToUpdate.setStock(99);
        boolean updateSuccess = variantDao.updateVariant(variantToUpdate);
        h2Connection.commit();
        
        ProductVariants updatedVariant = variantDao.findById(1);

        // --- ASSERT ---
        assertTrue("Update phải trả về true", updateSuccess);
        assertEquals("Stock phải được cập nhật thành 99", 99, updatedVariant.getStock());
    }
    
    @Test
    public void testDeleteVariant() throws Exception {
        setTestCaseInfo(
            "DAO_VAR_04", 
            "Xóa biến thể", 
            "1. Insert Variant\n2. Delete ID 1\n3. Check lại", 
            "Variant ID 1", 
            "Delete -> True\nGetById -> Null"
        );

        // --- ARRANGE ---
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (ID=1)
        h2Connection.commit();
        
        // --- ACT ---
        boolean deleteSuccess = variantDao.deleteVariant(1);
        h2Connection.commit();
        
        ProductVariants deletedVariant = variantDao.findById(1);

        // --- ASSERT ---
        assertTrue("Delete phải trả về true", deleteSuccess);
        assertNull("Biến thể sau khi xóa phải là null", deletedVariant);
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT FILE EXCEL (CSV) ===
    // ==========================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, currentExpected, "PASS"
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMsg = (e != null) ? e.getMessage() : "Unknown Error";
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, errorMsg, "FAIL"
            });
        }
    };
    
    // === CHẠY MỘT LẦN DUY NHẤT KHI KẾT THÚC ===
    @AfterClass
    public static void tearDownClass() throws Exception {
        // 1. Đóng kết nối H2
        if (h2Connection != null) {
            h2Connection.close();
        }
        
        // 2. Xuất ra file Excel CSV
        String fileName = "KetQuaTest_ProductVariantDao.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo ProductVariantDao ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            writer.write('\ufeff'); // BOM cho Tiếng Việt

            // Header chuẩn
            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            // Data
            for (String[] row : finalReportData) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialChars(row[0]),
                        escapeSpecialChars(row[1]),
                        escapeSpecialChars(row[2]),
                        escapeSpecialChars(row[3]),
                        escapeSpecialChars(row[4]),
                        escapeSpecialChars(row[5]),
                        escapeSpecialChars(row[6])
                );
                writer.println(line);
            }
            
            System.out.println("XONG! File '" + fileName + "' đã được tạo.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }
    
    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}