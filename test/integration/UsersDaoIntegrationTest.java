package integration;

import dao.UsersDao;
import entity.Users;
import util.ExcelTestExporter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp UsersDao.
 * Class này kiểm thử các nghiệp vụ xác thực (Login) và quản lý tài khoản (CRUD, Get All).
 */
public class UsersDaoIntegrationTest {

    private Connection connection;
    private UsersDao usersDao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String dbUrl = "jdbc:sqlserver://BANGGG:1433;databaseName=shopduck_test;encrypt=false;trustServerCertificate=true";
    private final String dbUser = "sa";
    private final String dbPass = "123456";

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
     * 1. Mở kết nối DB.
     * 2. Dọn dẹp dữ liệu (xóa toàn bộ bảng Users).
     * 3. Khởi tạo UsersDao.
     * @throws Exception nếu có lỗi kết nối hoặc thao tác DB.
     */
    @Before
    public void setUp() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        
        // Clean DB trước khi test để đảm bảo môi trường sạch
        try (Statement stmt = connection.createStatement()) {
            // Xóa hết dữ liệu bảng Users. Lưu ý: Cần xử lý ràng buộc FK nếu có
            stmt.execute("DELETE FROM Users"); 
        }
        usersDao = new UsersDao(connection);
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ====================== CÁC TEST CASE XÁC THỰC ======================

    // --- CASE 1: LOGIN THÀNH CÔNG ---
    @Test
    public void testLogin_Success() {
        setTestCaseInfo("DAO_01", "Login thành công", "Insert user hợp lệ -> Gọi login()", "User: test, Pass: 123", "User Object != NULL");
        // Chuẩn bị dữ liệu
        usersDao.insert(new Users(0, "test", "123", "Test", "t@m.c", "user"));
        // Kiểm tra
        Assert.assertNotNull("Login phải trả về đối tượng User", usersDao.login("test", "123"));
    }

    // --- CASE 2: LOGIN THẤT BẠI ---
    @Test
    public void testLogin_Fail() {
        setTestCaseInfo("DAO_02", "Login thất bại", "Insert user -> Login sai pass/user", "Pass: WRONG", "NULL");
        // Chuẩn bị dữ liệu
        usersDao.insert(new Users(0, "test", "123", "Test", "t@m.c", "user"));
        // Kiểm tra đăng nhập sai mật khẩu
        Assert.assertNull("Login sai pass phải trả về NULL", usersDao.login("test", "WRONG"));
        // Kiểm tra đăng nhập sai username
        Assert.assertNull("Login sai user phải trả về NULL", usersDao.login("nonexistent", "123"));
    }

    // --- CASE 3: CHECK TỒN TẠI ---
    @Test
    public void testCheckUserExists() {
        setTestCaseInfo("DAO_03", "Check tồn tại username", "Insert A -> Check A", "User: A", "TRUE");
        // Chuẩn bị dữ liệu
        usersDao.insert(new Users(0, "A", "1", "A", "a@m.c", "user"));
        // Kiểm tra tồn tại
        Assert.assertTrue("CheckUserExists phải trả về TRUE", usersDao.checkUserExists("A"));
        // Kiểm tra không tồn tại
        Assert.assertFalse("CheckUserExists phải trả về FALSE cho user không tồn tại", usersDao.checkUserExists("B"));
    }

    // ====================== CÁC TEST CASE QUẢN LÝ (CRUD) ======================

    // --- CASE 4: FULL FLOW (THÊM - SỬA - XÓA) ---
    @Test
    public void testCRUD_Flow() {
        setTestCaseInfo("DAO_04", "Full Flow CRUD", "Insert -> Update -> Delete", "Data Change", "Success");
        
        // --- 1. Insert ---
        Users u = new Users(0, "crud", "1", "Original", "c@m.c", "user");
        usersDao.insert(u);
        Users saved = usersDao.login("crud", "1");
        Assert.assertNotNull("Insert phải thành công", saved);
        
        // --- 2. Update ---
        saved.setFullname("Changed");
        usersDao.updateUser(saved);
        // Kiểm tra sau khi update
        Assert.assertEquals("Fullname phải được cập nhật", "Changed", usersDao.login("crud", "1").getFullname());

        // --- 3. Delete ---
        usersDao.deleteUser(saved.getId());
        // Kiểm tra sau khi xóa (Login thất bại)
        Assert.assertNull("Delete phải thành công, Login phải trả về NULL", usersDao.login("crud", "1"));
    }

    // --- CASE 5: LẤY TẤT CẢ USER ---
    @Test
    public void testGetAllUsers() {
        setTestCaseInfo("DAO_05", "Get All Users", "Insert 2 users -> Get All", "2 Users", "List size = 2");
        
        // Chuẩn bị dữ liệu (môi trường sạch nên tổng cộng phải là 2)
        usersDao.insert(new Users(0, "u1", "1", "U1", "u1@m.c", "user"));
        usersDao.insert(new Users(0, "u2", "1", "U2", "u2@m.c", "admin"));
        
        List<Users> list = usersDao.getAllUsers();
        Assert.assertEquals("Tổng số User phải bằng 2", 2, list.size());
    }

    // --- CASE 6: LẤY USER THEO ID ---
    @Test
    public void testGetUserById() {
        setTestCaseInfo("DAO_06", "Get User By ID", "Insert -> Get ID", "ID exists", "Return User");
        
        // 1. Insert User (cần thiết để có ID tự tăng)
        usersDao.insert(new Users(0, "u3", "1", "U3", "u3@m.c", "user"));
        
        // 2. Lấy User vừa tạo để có ID
        Users inserted = usersDao.login("u3", "1");
        Assert.assertNotNull(inserted);
        
        // 3. Test hàm getUserById
        Users found = usersDao.getUserById(inserted.getId());
        
        // 4. Verify
        Assert.assertNotNull("Phải tìm thấy User theo ID", found);
        Assert.assertEquals("Kiểm tra username", "u3", found.getUsername());
        
        // Test trường hợp ID không tồn tại
        Assert.assertNull("Tìm ID không tồn tại phải trả về NULL", usersDao.getUserById(99999));
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
    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_UsersDao.xlsx"); }
}