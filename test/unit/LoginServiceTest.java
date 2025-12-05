package unit;

// === IMPORT LOGIC CHÍNH ===
import dao.UsersDao;
import entity.Users;
import service.LoginResult;
import service.LoginService;
import util.ExcelTestExporter; // <--- IMPORT TIỆN ÍCH EXCEL MỚI

// === IMPORT JUNIT & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Unit Test cho LoginService.
 * Kết hợp Mockito và Xuất báo cáo Excel (.xlsx).
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginServiceTest {

    // === 1. KHAI BÁO MOCK OBJECTS ===
    @Mock
    private UsersDao usersDao; // Giả lập DAO

    @InjectMocks
    private LoginService loginService; // Inject DAO giả vào Service

    // === 2. CÁC BIẾN HỖ TRỢ BÁO CÁO (Dùng biến cục bộ, không cần list) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    // Hàm điền thông tin Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    // ==========================================================
    // === CÁC TEST CASE (Logic Login) ===
    // ==========================================================

    @Test
    public void testLogin_EmptyInput_ShouldFail() {
        setTestCaseInfo(
            "AUTH_01", 
            "Đăng nhập với input rỗng", 
            "1. Nhập username rỗng\n2. Gọi hàm login", 
            "User: \"\", Pass: \"123\"", 
            "Trạng thái: FAILED_CREDENTIALS"
        );

        LoginResult result = loginService.login("", "123");

        assertEquals(LoginResult.Status.FAILED_CREDENTIALS, result.getStatus());
        assertNull(result.getUser());
    }

    @Test
    public void testLogin_WrongCredentials_ShouldFail() {
        setTestCaseInfo(
            "AUTH_02", 
            "Đăng nhập sai thông tin", 
            "1. Mock DAO trả về null (không tìm thấy)\n2. Gọi login", 
            "User: wrongUser, Pass: 123", 
            "Trạng thái: FAILED_CREDENTIALS"
        );

        // Giả lập: DAO trả về null khi gọi login
        try {
            Mockito.when(usersDao.login("wrongUser", "123")).thenReturn(null);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        LoginResult result = loginService.login("wrongUser", "123");
        
        assertEquals(LoginResult.Status.FAILED_CREDENTIALS, result.getStatus());
    }

    @Test
    public void testLogin_Admin_Success() {
        setTestCaseInfo(
            "AUTH_03", 
            "Đăng nhập thành công (Admin)", 
            "1. Mock DAO trả về User Admin\n2. Check Role", 
            "User: admin, Role: admin", 
            "Trạng thái: SUCCESS_ADMIN"
        );

        // Chuẩn bị dữ liệu giả
        Users adminUser = new Users(1, "admin", "123", "Admin Name", "admin@mail.com", "admin");
        
        // Mock hành vi
        try {
            Mockito.when(usersDao.login("admin", "123")).thenReturn(adminUser);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        // Thực thi
        LoginResult result = loginService.login("admin", "123");
        
        // Kiểm tra
        assertEquals(LoginResult.Status.SUCCESS_ADMIN, result.getStatus());
        assertNotNull(result.getUser());
        assertEquals("admin", result.getUser().getUsername());
    }

    @Test
    public void testLogin_User_Success() {
        setTestCaseInfo(
            "AUTH_04", 
            "Đăng nhập thành công (User thường)", 
            "1. Mock DAO trả về User thường\n2. Check Role", 
            "User: user1, Role: user", 
            "Trạng thái: SUCCESS_USER"
        );

        Users normalUser = new Users(2, "user1", "123", "User Name", "user@mail.com", "user");
        try {
            Mockito.when(usersDao.login("user1", "123")).thenReturn(normalUser);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        LoginResult result = loginService.login("user1", "123");

        assertEquals(LoginResult.Status.SUCCESS_USER, result.getStatus());
    }

    @Test
    public void testLogin_InvalidRole_ShouldFail() {
        setTestCaseInfo(
            "AUTH_05", 
            "Đăng nhập role lạ (Hacker/Lỗi data)", 
            "1. Mock DAO trả về role 'hacker'\n2. Switch case check role", 
            "User: hacker, Role: hacker_role", 
            "Trạng thái: FAILED_INVALID_ROLE"
        );

        Users hacker = new Users(3, "hacker", "123", "Hacker", "hacker@mail.com", "hacker_role");
        try {
            Mockito.when(usersDao.login("hacker", "123")).thenReturn(hacker);
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        LoginResult result = loginService.login("hacker", "123");

        assertEquals(LoginResult.Status.FAILED_INVALID_ROLE, result.getStatus());
    }

    // --- [MỚI] TEST CASE: DAO NÉM EXCEPTION (SYSTEM ERROR) ---
    @Test
    public void testLogin_SystemError_ShouldFail() {
        setTestCaseInfo(
            "AUTH_06", 
            "Lỗi hệ thống (Database Error)", 
            "1. Mock DAO ném Exception\n2. Service catch và trả về lỗi hệ thống", 
            "User: any, Pass: any", 
            "Trạng thái: FAILED_SYSTEM_ERROR"
        );

        try {
            // Giả lập DAO ném ra một RuntimeException hoặc Exception bất kỳ
            Mockito.when(usersDao.login("errorUser", "123"))
                   .thenThrow(new RuntimeException("DB Connection Failed"));
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        LoginResult result = loginService.login("errorUser", "123");

        assertEquals(LoginResult.Status.FAILED_SYSTEM_ERROR, result.getStatus());
        assertNull(result.getUser());
    }

     // === EXCEL EXPORT ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps để khớp với file Excel
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };


    @AfterClass
    public static void exportReport() {
        // Xuất ra file Excel (.xlsx) thay vì CSV
        ExcelTestExporter.exportToExcel("KetQuaTest_LoginService.xlsx");
    }
}