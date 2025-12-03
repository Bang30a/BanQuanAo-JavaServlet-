// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.Products;
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
 * INTEGRATION TEST cho ProductDao (Chạy trên H2 Database).
 * Xuất kết quả ra file Excel (CSV).
 */
public class ProductDaoTest {

    private static ProductDao productDao;
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
        
        // 2. Tạo kết nối H2 (AUTOCOMMIT=OFF để kiểm soát giao dịch thủ công)
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
                                " category_id INT" + 
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
    public void testInsertAndFindById() throws Exception {
        setTestCaseInfo(
            "DAO_PROD_01", 
            "Thêm sản phẩm & Tìm theo ID", 
            "1. Insert 'Áo Sơ Mi'\n2. GetById lấy lại", 
            "Name: Áo Sơ Mi\nPrice: 50.0", 
            "Insert -> ID > 0\nGetById -> Đúng Name/Price"
        );

        // --- ARRANGE ---
        Products newProduct = new Products(0, "Áo Sơ Mi", "Vải lụa", 50.0, "ao.jpg");
        
        // --- ACT ---
        productDao.insert(newProduct);
        h2Connection.commit(); // Phải commit vì DAO không tự commit
        
        int newId = newProduct.getId();
        
        Products foundProduct = productDao.findById(newId);

        // --- ASSERT ---
        assertTrue("ID phải lớn hơn 0", newId > 0);
        assertNotNull("Phải tìm thấy sản phẩm", foundProduct);
        assertEquals("Tên sản phẩm phải là 'Áo Sơ Mi'", "Áo Sơ Mi", foundProduct.getName());
        assertEquals("Giá phải là 50.0", 50.0, foundProduct.getPrice(), 0.001);
    }
    
    @Test
    public void testFindById_NotFound() {
        setTestCaseInfo(
            "DAO_PROD_02", 
            "Tìm sản phẩm không tồn tại", 
            "Gọi GetById với ID lạ (999)", 
            "ID: 999", 
            "Trả về Null"
        );

        // --- ACT ---
        Products foundProduct = productDao.findById(999);
        
        // --- ASSERT ---
        assertNull("Không được tìm thấy sản phẩm", foundProduct);
    }

    @Test
    public void testUpdate() throws Exception {
        setTestCaseInfo(
            "DAO_PROD_03", 
            "Cập nhật thông tin sản phẩm", 
            "1. Insert 'Áo Cũ'\n2. Update thành 'Áo Mới'\n3. Check lại", 
            "Old: Áo Cũ ($10)\nNew: Áo Mới ($99)", 
            "GetById ra 'Áo Mới', $99"
        );

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
        setTestCaseInfo(
            "DAO_PROD_04", 
            "Xóa sản phẩm", 
            "1. Insert SP\n2. Delete\n3. Check lại", 
            "SP ID vừa tạo", 
            "Delete -> 1 dòng\nGetById -> Null"
        );

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
        setTestCaseInfo(
            "DAO_PROD_05", 
            "Lấy danh sách tất cả SP", 
            "1. Insert 2 SP\n2. GetAllProducts", 
            "SP1, SP2", 
            "List size = 2"
        );

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
        String fileName = "KetQuaTest_ProductDao.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo ProductDao ra file (" + fileName + ")...");

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