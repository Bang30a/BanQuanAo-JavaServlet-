// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
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
 * INTEGRATION TEST cho SizeDao (Chạy trên H2 Database).
 * Xuất kết quả ra file Excel (CSV).
 */
public class SizeDaoTest {

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
    public void testInsertSizeAndGetById() throws Exception {
        setTestCaseInfo(
            "DAO_SIZE_01", 
            "Thêm mới & Lấy theo ID", 
            "1. Insert size 'M'\n2. GetById(1)", 
            "Size: M", 
            "Insert -> True\nGetById -> Not Null, Label='M'"
        );

        // --- ARRANGE ---
        Size newSize = new Size(0, "M");
        
        // --- ACT ---
        boolean insertSuccess = sizeDao.insertSize(newSize);
        h2Connection.commit(); // Lưu thay đổi
        
        Size foundSize = sizeDao.getSizeById(1); // ID reset về 1

        // --- ASSERT ---
        assertTrue("Insert phải trả về true", insertSuccess);
        assertNotNull("Phải tìm thấy size", foundSize);
        assertEquals("ID phải là 1", 1, foundSize.getId());
        assertEquals("Label phải là 'M'", "M", foundSize.getSizeLabel());
    }
    
    @Test
    public void testGetAllSizes() throws Exception {
        setTestCaseInfo(
            "DAO_SIZE_02", 
            "Lấy danh sách tất cả Size", 
            "1. Insert S, M, L\n2. GetAllSizes", 
            "S, M, L", 
            "List size = 3\nElement[1] = M"
        );

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
        setTestCaseInfo(
            "DAO_SIZE_03", 
            "Cập nhật thông tin Size", 
            "1. Insert 'XL'\n2. Update thành 'XXL'", 
            "Old: XL -> New: XXL", 
            "GetById ra 'XXL'"
        );

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
        setTestCaseInfo(
            "DAO_SIZE_04", 
            "Xóa Size", 
            "1. Insert Size\n2. Delete\n3. Check lại", 
            "Size ID 1", 
            "Delete -> True\nGetById -> Null"
        );

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
        String fileName = "KetQuaTest_SizeDao.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo SizeDao ra file (" + fileName + ")...");

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