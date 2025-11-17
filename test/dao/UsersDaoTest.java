// Đặt trong "Test Packages/dao/"
package dao;

// === IMPORT CƠ BẢN ===
import entity.Users;
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
 * Đây là lớp INTEGRATION TEST (Kiểm thử Tích hợp) cho UsersDao.
 * Nó sẽ chạy test trên một CSDL H2 (in-memory) thật.
 */
public class UsersDaoTest {

    // Đối tượng DAO thật, không phải mock
    private static UsersDao usersDao;
    
    // Connection H2 (dùng chung cho tất cả test)
    private static Connection h2Connection;

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối tới H2 in-memory (ĐÃ SỬA LỖI MODE)
        // MODE=SQLServer -> MODE=MSSQLServer
        String dbUrl = "jdbc:h2:mem:testdb;MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", ""); // user/pass H2
        
        // 3. Tiêm (Inject) connection H2 vào DAO
        usersDao = new UsersDao(h2Connection);
        
        // 4. TẠO BẢNG (Schema) cho CSDL H2 (ĐÃ SỬA LỖI NVARCHAR)
        String createTableSql = "CREATE TABLE Users (" +
                                " id INT PRIMARY KEY AUTO_INCREMENT," +
                                " username VARCHAR(255) NOT NULL UNIQUE," +
                                " password VARCHAR(255) NOT NULL," +
                                " fullname VARCHAR(255)," + // ✅ SỬA TỪ NVARCHAR
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
        // Dọn dẹp dữ liệu (xóa hết) sau MỖI test để các test độc lập
        // TRUNCATE TABLE nhanh hơn DELETE FROM
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE Users"); 
        }
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testRegisterAndLogin_Success() {
        // --- ARRANGE (Chuẩn bị) ---
        Users newUser = new Users(0, "user1", "pass123", "Test User", "test@gmail.com", "user");
        
        // --- ACT (Hành động) ---
        // 1. Test hàm register (insert)
        boolean registerSuccess = usersDao.register(newUser);
        
        // 2. Test hàm login
        Users loggedInUser = usersDao.login("user1", "pass123");

        // --- ASSERT (Xác minh) ---
        assertTrue("Register phải trả về true", registerSuccess);
        assertNotNull("Login phải tìm thấy user", loggedInUser);
        assertEquals("Username phải là 'user1'", "user1", loggedInUser.getUsername());
        assertEquals("Fullname phải là 'Test User'", "Test User", loggedInUser.getFullname());
    }

    @Test
    public void testLogin_Failed() {
        // --- ARRANGE ---
        // (Không có user nào trong DB)
        
        // --- ACT ---
        Users user1 = usersDao.login("user1", "wrongpass"); // Sai pass
        Users user2 = usersDao.login("wronguser", "pass123"); // Sai user

        // --- ASSERT ---
        assertNull("Login sai pass phải trả về null", user1);
        assertNull("Login sai user phải trả về null", user2);
    }
    
    @Test
    public void testCheckUserExists() {
        // --- ARRANGE ---
        Users newUser = new Users(0, "existingUser", "pass", "Test", "test@g.com", "user");
        usersDao.register(newUser);
        
        // --- ACT ---
        boolean exists = usersDao.checkUserExists("existingUser");
        boolean notExists = usersDao.checkUserExists("ghostUser");

        // --- ASSERT ---
        assertTrue("'existingUser' phải tồn tại", exists);
        assertFalse("'ghostUser' không được tồn tại", notExists);
    }

    @Test
    public void testUpdateUser() {
        // --- ARRANGE ---
        Users newUser = new Users(0, "userToUpdate", "pass", "Old Name", "old@g.com", "user");
        usersDao.register(newUser);
        
        // Lấy lại user (để có ID)
        Users userToUpdate = usersDao.login("userToUpdate", "pass");
        
        // --- ACT ---
        userToUpdate.setFullname("New Full Name");
        userToUpdate.setEmail("new@g.com");
        boolean updateSuccess = usersDao.updateUser(userToUpdate);
        
        // Lấy lại user từ DB sau khi update
        Users updatedUser = usersDao.getUserById(userToUpdate.getId());

        // --- ASSERT ---
        assertTrue("Update phải trả về true", updateSuccess);
        assertNotNull(updatedUser);
        assertEquals("Fullname phải là 'New Full Name'", "New Full Name", updatedUser.getFullname());
        assertEquals("Email phải là 'new@g.com'", "new@g.com", updatedUser.getEmail());
    }
    
    @Test
    public void testDeleteUser() {
        // --- ARRANGE ---
        Users newUser = new Users(0, "userToDelete", "pass", "Test", "test@g.com", "user");
        usersDao.register(newUser);
        Users userToDelete = usersDao.login("userToDelete", "pass");
        
        // --- ACT ---
        boolean deleteSuccess = usersDao.deleteUser(userToDelete.getId());
        Users deletedUser = usersDao.getUserById(userToDelete.getId());
        
        // --- ASSERT ---
        assertTrue("Delete phải trả về true", deleteSuccess);
        assertNull("User sau khi xóa phải là null", deletedUser);
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
        
        // 2. Ghi báo cáo (code từ các test trước)
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
            
            writer.write("\n--- Kết quả chạy " + UsersDaoTest.class.getName() + " ---\n");
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