package integration;

import dao.UsersDao;
import entity.Users;
import util.ExcelTestExporter; // <-- Import class tiện ích

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

// Import JUNIT
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class UsersDaoIntegrationTest {

    private Connection connection;
    private UsersDao usersDao;

    // --- CẤU HÌNH TỪ DBContext CỦA BẠN ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String instance = ""; 
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck_test"; 

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        String url;
        if (instance == null || instance.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName;
        } else {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + "\\" + instance + ";databaseName=" + dbName;
        }
        url += ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // Clean State: Xóa dữ liệu cũ để test sạch sẽ
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Users"); 
        }

        usersDao = new UsersDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testLogin_Success() {
        setTestCaseInfo(
            "DAO_01", 
            "Integration: Login thành công (DB thật)", 
            "1. Insert User vào DB Test\n2. Gọi hàm login", 
            "User: testuser, Pass: pass123", 
            "Trả về Object User đầy đủ"
        );

        // 1. Test tự tạo dữ liệu giả
        Users newUser = new Users(0, "testuser", "pass123", "Test User", "test@mail.com", "user");
        usersDao.insert(newUser);

        // 2. Test hàm login
        Users foundUser = usersDao.login("testuser", "pass123");

        // 3. Kiểm tra
        Assert.assertNotNull("Phải tìm thấy user trong DB", foundUser);
        Assert.assertEquals("Test User", foundUser.getFullname());
        Assert.assertEquals("user", foundUser.getRole());
    }

    @Test
    public void testLogin_Failure_WrongPass() {
        setTestCaseInfo(
            "DAO_02", 
            "Integration: Login sai pass (DB thật)", 
            "1. Insert User\n2. Login sai pass", 
            "User: testuser, Pass: WRONG_PASS", 
            "Trả về null"
        );

        // 1. Tạo user
        Users newUser = new Users(0, "testuser", "pass123", "Test User", "test@mail.com", "user");
        usersDao.insert(newUser);

        // 2. Login sai pass
        Users foundUser = usersDao.login("testuser", "WRONG_PASS");

        // 3. Phải trả về null
        Assert.assertNull(foundUser);
    }

    @Test
    public void testCheckUserExists() {
        setTestCaseInfo("DAO_03", "Kiểm tra user tồn tại", "Insert 'userA' -> Check 'userA'", "username: userA", "Result: true");
        
        usersDao.insert(new Users(0, "userA", "1", "A", "a@m.c", "user"));
        boolean exists = usersDao.checkUserExists("userA");
        boolean notExists = usersDao.checkUserExists("userB");

        Assert.assertTrue("User A phải tồn tại", exists);
        Assert.assertFalse("User B không được tồn tại", notExists);
    }

    @Test
    public void testUpdateUser() {
        setTestCaseInfo("DAO_04", "Cập nhật thông tin User", "Insert -> Update Pass -> Login lại", "Old: 123, New: 456", "Login được bằng 456");

        // 1. Tạo user ban đầu
        Users u = new Users(0, "updateUser", "123", "Old Name", "old@mail.com", "user");
        usersDao.insert(u);
        
        // Lấy ID của user vừa tạo (để update chính xác)
        Users insertedUser = usersDao.login("updateUser", "123");
        
        // 2. Sửa thông tin
        insertedUser.setPassword("456");
        insertedUser.setFullname("New Name");
        boolean updateResult = usersDao.updateUser(insertedUser);

        // 3. Kiểm tra
        Assert.assertTrue("Update phải trả về true", updateResult);
        Users updatedUser = usersDao.login("updateUser", "456"); // Login bằng pass mới
        Assert.assertNotNull("Phải login được bằng pass mới", updatedUser);
        Assert.assertEquals("New Name", updatedUser.getFullname());
    }

    @Test
    public void testDeleteUser() {
        setTestCaseInfo("DAO_05", "Xóa User", "Insert -> Delete -> Login lại", "User: deleteMe", "Login trả về null");

        // 1. Tạo
        usersDao.insert(new Users(0, "deleteMe", "1", "Delete Me", "d@m.c", "user"));
        Users u = usersDao.login("deleteMe", "1");
        
        // 2. Xóa
        boolean deleteResult = usersDao.deleteUser(u.getId());
        
        // 3. Kiểm tra
        Assert.assertTrue(deleteResult);
        Assert.assertNull("User đã xóa không thể login", usersDao.login("deleteMe", "1"));
    }

    @Test
    public void testRegister_Integration_Success() {
        setTestCaseInfo(
            "DAO_REG_01", 
            "Integration: Đăng ký User mới (DB thật)", 
            "1. Gọi hàm register\n2. Gọi checkUserExists để verify", 
            "User: regUser", 
            "Insert thành công (True)"
        );

        // 1. Dữ liệu mới
        Users newUser = new Users(0, "regUser", "pass123", "Reg User", "reg@mail.com", "user");
        
        // 2. Thực thi đăng ký
        boolean isRegistered = usersDao.register(newUser);

        // 3. Kiểm tra
        Assert.assertTrue("Hàm register phải trả về true", isRegistered);
        
        // Kiểm tra lại bằng cách query xuống DB lần nữa
        boolean exists = usersDao.checkUserExists("regUser");
        Assert.assertTrue("User vừa đăng ký phải tồn tại trong DB", exists);
    }

    @Test
    public void testRegister_Integration_Duplicate() {
        setTestCaseInfo(
            "DAO_REG_02", 
            "Integration: Chặn đăng ký trùng (DB Constraints)", 
            "1. Insert user A\n2. Insert tiếp user A", 
            "User: duplicateUser", 
            "Lần 2 có thể lỗi hoặc false tùy DB"
        );

        // 1. Đăng ký lần 1
        Users u1 = new Users(0, "duplicateUser", "1", "U1", "d@m.c", "user");
        usersDao.register(u1);

        // 2. Đăng ký lần 2 (Trùng username)
        try {
            boolean result = usersDao.register(u1);
            // Nếu code DAO của bạn catch Exception và trả về false thì dòng này đúng:
            Assert.assertFalse("Không được đăng ký trùng user", result);
        } catch (Exception e) {
            // Nếu DAO ném lỗi ra ngoài thì cũng tính là chặn thành công
            Assert.assertTrue("Đã bắt được lỗi trùng lặp", true);
        }
    }

    // === XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_UsersDao.xlsx");
    }
}