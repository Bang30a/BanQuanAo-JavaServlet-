// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.ProductVariants;
import entity.Products;
import entity.Size;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT ĐỂ TẠO BÁO CÁO ===
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp INTEGRATION TEST (Kiểm thử Tích hợp) cho ProductVariantDao.
 */
public class ProductVariantDaoTest {

    private static ProductVariantDao variantDao;
    private static ProductDao productDao; // Cần để tạo "mồi"
    private static SizeDao sizeDao; // Cần để tạo "mồi"
    
    private static Connection h2Connection;

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (AUTOCOMMIT=OFF để kiểm soát)
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
        // =================================================================
        // Dọn dẹp dữ liệu (dùng DELETE thay vì TRUNCATE)
        try (Statement stmt = h2Connection.createStatement()) {
            // (Phải xóa theo thứ tự ngược lại của Khóa Ngoại)
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

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testInsertVariantAndFindById() throws Exception {
        // --- ARRANGE (Chuẩn bị) ---
        // (Chúng ta đã có Product ID=1 và Size ID=1 từ @Before)
        ProductVariants newVariant = new ProductVariants(0, 1, 1, 50); // (id, prod_id, size_id, stock)
        
        // --- ACT (Hành động) ---
        boolean insertSuccess = variantDao.insertVariant(newVariant);
        h2Connection.commit();
        
        // 2. Test hàm findById (ID sẽ là 1 vì đã reset)
        ProductVariants foundVariant = variantDao.findById(1);

        // --- ASSERT (Xác minh) ---
        assertTrue("Insert phải trả về true", insertSuccess);
        assertNotNull("Phải tìm thấy biến thể", foundVariant);
        assertEquals("Stock phải là 50", 50, foundVariant.getStock());
        
        // Kiểm tra xem JOIN có hoạt động không
        assertEquals("Tên sản phẩm phải là 'Áo Thun'", "Áo Thun", foundVariant.getProductName());
        assertEquals("Tên size phải là 'M'", "M", foundVariant.getSizeName());
        assertEquals("Giá phải là 100.0", 100.0, foundVariant.getPrice(), 0.001);
    }
    
    @Test
    public void testFindByProductId() throws Exception {
        // --- ARRANGE ---
        // Thêm 1 Size nữa (ID=2)
        sizeDao.insertSize(new Size(0, "L"));
        h2Connection.commit();
        
        // Tạo 2 variant cho cùng Product ID=1
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (ID=1, Prod=1, Size=M)
        variantDao.insertVariant(new ProductVariants(0, 1, 2, 30)); // (ID=2, Prod=1, Size=L)
        h2Connection.commit();
        
        // --- ACT ---
        List<ProductVariants> list = variantDao.findByProductId(1); // Tìm các variant của Product 1

        // --- ASSERT ---
        assertNotNull(list);
        assertEquals("Phải tìm thấy 2 biến thể", 2, list.size());
        assertEquals("Size của biến thể thứ 2 phải là 'L'", "L", list.get(1).getSizeName());
    }

    @Test
    public void testUpdateVariant() throws Exception {
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


    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (ĐÃ KẾT HỢP VỚI @AfterClass) ===
    // =================================================================

    private static final List<String[]> testResults = new ArrayList<>();

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        private String getModuleName(Description d) {
            String className = d.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);
        }
        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description), description.getMethodName(), "PASS", ""
            });
        }
        @Override
        protected void failed(Throwable e, Description description) {
            testResults.add(new String[]{
                getModuleName(description), description.getMethodName(), "FAIL", e.getMessage()
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
        
        // 2. Ghi báo cáo
        String filePath = "test-report.txt";
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'), 
            new String(new char[30]).replace('\0', '-')
        ).replace("|", "+");

        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath, true), StandardCharsets.UTF_8)) {
            
            writer.write("\n--- Kết quả chạy " + ProductVariantDaoTest.class.getName() + " ---\n");
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                System.out.print(line);
                writer.write(line);
            }
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được NỐI VÀO file: " + filePath);
        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());
        }
    }
}