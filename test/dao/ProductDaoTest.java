// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.Products;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
 * Đây là lớp INTEGRATION TEST (Kiểm thử Tích hợp) cho ProductDao.
 */
public class ProductDaoTest {

    private static ProductDao productDao;
    private static Connection h2Connection;

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (chế độ mặc định, không cần MODE)
        // Chúng ta dùng AUTOCOMMIT=OFF để kiểm soát giao dịch thủ công
        String dbUrl = "jdbc:h2:mem:product_testdb;DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", "");
        
        // 3. Tiêm (Inject) connection H2 vào DAO
        productDao = new ProductDao(h2Connection);
        
        // 4. TẠO BẢNG (Schema)
        String createTableSql = "CREATE TABLE Products (" +
                                " id INT PRIMARY KEY AUTO_INCREMENT," +
                                " name VARCHAR(255) NOT NULL," +
                                " description VARCHAR(1000)," +
                                " price DOUBLE," +
                                " image VARCHAR(255)," +
                                " category_id INT" + // Giả định không có khóa ngoại
                                ")";
        
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(createTableSql);
        }
        
        // 5. Commit (lưu) việc tạo bảng
        h2Connection.commit();
    }

    // === CHẠY TRƯỚC MỖI TEST CASE ===
    @Before
    public void setUp() throws Exception {
        // Dọn dẹp dữ liệu (xóa hết) trước MỖI test
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE Products");
            // Reset ID về 1
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
        }
        // Commit (lưu) việc dọn dẹp
        h2Connection.commit();
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testInsertAndFindById() throws Exception {
        // --- ARRANGE (Chuẩn bị) ---
        Products newProduct = new Products(0, "Áo Sơ Mi", "Vải lụa", 50.0, "ao.jpg");
        
        // --- ACT (Hành động) ---
        // 1. Test hàm insert
        productDao.insert(newProduct);
        h2Connection.commit(); // Phải commit vì DAO không tự commit
        
        // (Lưu ý: hàm insert của bạn cập nhật ID vào object 'newProduct')
        int newId = newProduct.getId();
        
        // 2. Test hàm findById
        Products foundProduct = productDao.findById(newId);

        // --- ASSERT (Xác minh) ---
        assertTrue("ID phải lớn hơn 0", newId > 0);
        assertNotNull("Phải tìm thấy sản phẩm", foundProduct);
        assertEquals("Tên sản phẩm phải là 'Áo Sơ Mi'", "Áo Sơ Mi", foundProduct.getName());
        assertEquals("Giá phải là 50.0", 50.0, foundProduct.getPrice(), 0.001);
    }
    
    @Test
    public void testFindById_NotFound() {
        // --- ARRANGE --- (Không có sản phẩm nào)
        
        // --- ACT ---
        Products foundProduct = productDao.findById(999);
        
        // --- ASSERT ---
        assertNull("Không được tìm thấy sản phẩm", foundProduct);
    }

    @Test
    public void testUpdate() throws Exception {
        // --- ARRANGE ---
        Products p = new Products(0, "Áo Cũ", "Vải cũ", 10.0, "cu.jpg");
        productDao.insert(p);
        h2Connection.commit();
        
        int id = p.getId();
        
        // --- ACT ---
        Products productToUpdate = productDao.findById(id);
        productToUpdate.setName("Áo Mới");
        productToUpdate.setPrice(99.0);
        
        int rowsAffected = productDao.update(productToUpdate);
        h2Connection.commit();
        
        Products updatedProduct = productDao.findById(id);

        // --- ASSERT ---
        assertEquals("Phải cập nhật 1 dòng", 1, rowsAffected);
        assertEquals("Tên phải là 'Áo Mới'", "Áo Mới", updatedProduct.getName());
        assertEquals("Giá phải là 99.0", 99.0, updatedProduct.getPrice(), 0.001);
    }
    
    @Test
    public void testDelete_Success() throws Exception {
        // --- ARRANGE ---
        Products p = new Products(0, "Sản phẩm sắp xóa", "...", 1.0, "xoa.jpg");
        productDao.insert(p);
        h2Connection.commit();
        int id = p.getId();
        
        // --- ACT ---
        int rowsAffected = productDao.delete(id);
        h2Connection.commit();
        
        Products deletedProduct = productDao.findById(id);

        // --- ASSERT ---
        assertEquals("Phải xóa 1 dòng", 1, rowsAffected);
        assertNull("Sản phẩm phải là null sau khi xóa", deletedProduct);
    }
    
    @Test
    public void testGetAllProducts() throws Exception {
        // --- ARRANGE ---
        productDao.insert(new Products(0, "Áo 1", "...", 1.0, "1.jpg"));
        productDao.insert(new Products(0, "Áo 2", "...", 2.0, "2.jpg"));
        h2Connection.commit();
        
        // --- ACT ---
        List<Products> list = productDao.getAllProducts();

        // --- ASSERT ---
        assertNotNull(list);
        assertEquals("Danh sách phải có 2 sản phẩm", 2, list.size());
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
            
            writer.write("\n--- Kết quả chạy " + ProductDaoTest.class.getName() + " ---\n");
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