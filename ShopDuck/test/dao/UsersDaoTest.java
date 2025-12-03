// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.Users;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.AfterClass;
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
 * INTEGRATION TEST cho UsersDao (Chạy trên H2 Database).
 * Xuất kết quả ra file Excel (CSV).
 */
public class UsersDaoTest {

    // Đối tượng DAO thật
    private static UsersDao usersDao;
    
    // Connection H2 (dùng chung)
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
        
        // 2. Tạo kết nối H2 in-memory
        String dbUrl = "jdbc:h2:mem:testdb;MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", ""); 
        
        // 3. Inject connection vào DAO
        usersDao = new UsersDao(h2Connection);
        
        // 4. Tạo bảng Users giả lập
        String createTableSql = "CREATE TABLE Users (" +
                                " id INT PRIMARY KEY AUTO_INCREMENT," +
                                " username VARCHAR(255) NOT NULL UNIQUE," +
                                " password VARCHAR(255) NOT NULL," +
                                " fullname VARCHAR(255)," + 
                                " email VARCHAR(255)," +
                                " role VARCHAR(50)" +
                                ")";
        
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(createTableSql);
        }
    }

    // === CHẠY SAU MỖI TEST CASE ===
    @After
    public void tearDown() throws Exception {
        // Xóa sạch dữ liệu để test case sau không bị ảnh hưởng
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE Users"); 
        }
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
    public void testRegisterAndLogin_Success() {
        setTestCaseInfo(
            "DAO_USER_01", 
            "Đăng ký & Đăng nhập thành công", 
            "1. Register User mới\n2. Login lại", 
            "User: user1\nPass: pass123", 
            "Register -> True\nLogin -> Object != null"
        );

        // --- ARRANGE ---
        Users newUser = new Users(0, "user1", "pass123", "Test User", "test@gmail.com", "user");
        
        // --- ACT ---
        boolean registerSuccess = usersDao.register(newUser);
        Users loggedInUser = usersDao.login("user1", "pass123");

        // --- ASSERT ---
        assertTrue("Register phải trả về true", registerSuccess);
        assertNotNull("Login phải tìm thấy user", loggedInUser);
        assertEquals("Username khớp", "user1", loggedInUser.getUsername());
        assertEquals("Fullname khớp", "Test User", loggedInUser.getFullname());
    }

    @Test
    public void testLogin_Failed() {
        setTestCaseInfo(
            "DAO_USER_02", 
            "Đăng nhập thất bại (Sai info)", 
            "1. Login sai pass\n2. Login sai user", 
            "User: wrong / Pass: wrong", 
            "Cả 2 trường hợp trả về Null"
        );

        Users user1 = usersDao.login("user1", "wrongpass"); // Sai pass
        Users user2 = usersDao.login("wronguser", "pass123"); // Sai user

        assertNull(user1);
        assertNull(user2);
    }
    
    @Test
    public void testCheckUserExists() {
        setTestCaseInfo(
            "DAO_USER_03", 
            "Kiểm tra tồn tại (CheckExists)", 
            "1. Tạo user 'existingUser'\n2. Check nó và check user lạ", 
            "User: existingUser", 
            "Tồn tại -> True\nKhông tồn tại -> False"
        );

        // Arrange
        Users newUser = new Users(0, "existingUser", "pass", "Test", "test@g.com", "user");
        usersDao.register(newUser);
        
        // Act
        boolean exists = usersDao.checkUserExists("existingUser");
        boolean notExists = usersDao.checkUserExists("ghostUser");

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    public void testUpdateUser() {
        setTestCaseInfo(
            "DAO_USER_04", 
            "Cập nhật thông tin User", 
            "1. Tạo user\n2. Sửa fullname/email\n3. Lấy lại từ DB check", 
            "New Name: New Full Name", 
            "Dữ liệu trong DB thay đổi đúng"
        );

        // Arrange
        Users newUser = new Users(0, "userToUpdate", "pass", "Old Name", "old@g.com", "user");
        usersDao.register(newUser);
        Users userToUpdate = usersDao.login("userToUpdate", "pass");
        
        // Act
        userToUpdate.setFullname("New Full Name");
        userToUpdate.setEmail("new@g.com");
        boolean updateSuccess = usersDao.updateUser(userToUpdate);
        
        Users updatedUser = usersDao.getUserById(userToUpdate.getId());

        // Assert
        assertTrue(updateSuccess);
        assertNotNull(updatedUser);
        assertEquals("New Full Name", updatedUser.getFullname());
        assertEquals("new@g.com", updatedUser.getEmail());
    }
    
    @Test
    public void testDeleteUser() {
        setTestCaseInfo(
            "DAO_USER_05", 
            "Xóa User", 
            "1. Tạo user\n2. Gọi delete\n3. GetById check lại", 
            "User ID cần xóa", 
            "GetById trả về Null"
        );

        // Arrange
        Users newUser = new Users(0, "userToDelete", "pass", "Test", "test@g.com", "user");
        usersDao.register(newUser);
        Users userToDelete = usersDao.login("userToDelete", "pass");
        
        // Act
        boolean deleteSuccess = usersDao.deleteUser(userToDelete.getId());
        Users deletedUser = usersDao.getUserById(userToDelete.getId());
        
        // Assert
        assertTrue(deleteSuccess);
        assertNull(deletedUser);
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

    @AfterClass
    public static void tearDownClass() throws Exception {
        // 1. Đóng kết nối H2
        if (h2Connection != null) {
            h2Connection.close();
        }
        
        // 2. Xuất ra file Excel CSV
        String fileName = "KetQuaTest_UsersDao.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo UsersDao ra file (" + fileName + ")...");

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
            
            System.out.println("XONG! File 'KetQuaTest_UsersDao.csv' đã được tạo.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    // Hàm xử lý ký tự đặc biệt cho CSV
    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}