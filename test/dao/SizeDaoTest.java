// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
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
 * Đây là lớp INTEGRATION TEST (Kiểm thử Tích hợp) cho SizeDao.
 */
public class SizeDaoTest {

    private static SizeDao sizeDao;
    private static Connection h2Connection;

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (AUTOCOMMIT=OFF để kiểm soát)
        String dbUrl = "jdbc:h2:mem:size_testdb;DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", "");
        
        // 3. Tiêm (Inject) connection H2 vào DAO
        sizeDao = new SizeDao(h2Connection);
        
        // 4. TẠO BẢNG (Schema)
        String createTableSql = "CREATE TABLE Sizes (" +
                                " id INT PRIMARY KEY AUTO_INCREMENT," +
                                " size_label VARCHAR(50) NOT NULL" +
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
            stmt.execute("TRUNCATE TABLE Sizes");
            stmt.execute("ALTER TABLE Sizes ALTER COLUMN id RESTART WITH 1");
        }
        h2Connection.commit(); // Commit (lưu) việc dọn dẹp
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testInsertSizeAndGetById() throws Exception {
        // --- ARRANGE (Chuẩn bị) ---
        Size newSize = new Size(0, "M");
        
        // --- ACT (Hành động) ---
        boolean insertSuccess = sizeDao.insertSize(newSize);
        h2Connection.commit(); // Lưu thay đổi
        
        // (Chúng ta phải tự lấy ID vì hàm insert không trả về ID)
        Size foundSize = sizeDao.getSizeById(1); // Vì ID đã reset về 1

        // --- ASSERT (Xác minh) ---
        assertTrue("Insert phải trả về true", insertSuccess);
        assertNotNull("Phải tìm thấy size", foundSize);
        assertEquals("ID phải là 1", 1, foundSize.getId());
        assertEquals("Label phải là 'M'", "M", foundSize.getSizeLabel());
    }
    
    @Test
    public void testGetAllSizes() throws Exception {
        // --- ARRANGE ---
        sizeDao.insertSize(new Size(0, "S"));
        sizeDao.insertSize(new Size(0, "M"));
        sizeDao.insertSize(new Size(0, "L"));
        h2Connection.commit();
        
        // --- ACT ---
        List<Size> list = sizeDao.getAllSizes();

        // --- ASSERT ---
        assertNotNull(list);
        assertEquals("Danh sách phải có 3 size", 3, list.size());
        assertEquals("Size thứ 2 phải là 'M'", "M", list.get(1).getSizeLabel());
    }

    @Test
    public void testUpdateSize() throws Exception {
        // --- ARRANGE ---
        sizeDao.insertSize(new Size(0, "XL"));
        h2Connection.commit();
        
        Size sizeToUpdate = sizeDao.getSizeById(1);
        
        // --- ACT ---
        sizeToUpdate.setSizeLabel("XXL");
        boolean updateSuccess = sizeDao.updateSize(sizeToUpdate);
        h2Connection.commit();
        
        Size updatedSize = sizeDao.getSizeById(1);

        // --- ASSERT ---
        assertTrue("Update phải trả về true", updateSuccess);
        assertEquals("Label phải là 'XXL'", "XXL", updatedSize.getSizeLabel());
    }
    
    @Test
    public void testDeleteSize() throws Exception {
        // --- ARRANGE ---
        sizeDao.insertSize(new Size(0, "Size Xóa"));
        h2Connection.commit();
        
        // --- ACT ---
        boolean deleteSuccess = sizeDao.deleteSize(1);
        h2Connection.commit();
        
        Size deletedSize = sizeDao.getSizeById(1);

        // --- ASSERT ---
        assertTrue("Delete phải trả về true", deleteSuccess);
        assertNull("Size sau khi xóa phải là null", deletedSize);
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
            
            writer.write("\n--- Kết quả chạy " + SizeDaoTest.class.getName() + " ---\n");
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