package unit;

// === IMPORT LOGIC ===
import dao.UsersDao;
import entity.Users;
import service.RegisterService;
import service.RegisterResult;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class RegisterServiceTest {

    @Mock
    private UsersDao usersDao;

    @InjectMocks
    private RegisterService registerService;

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    // ==========================================
    // TEST CASES
    // ==========================================

    @Test
    public void testRegister_Success() {
        setTestCaseInfo("REG_SVC_01", "Đăng ký thành công", "1. Input hợp lệ\n2. Mock DAO trả về true", 
                "User: newuser, Email: new@mail.com", "Result: SUCCESS");

        Mockito.when(usersDao.checkUserExists("newuser")).thenReturn(false);
        // Mockito.when(usersDao.checkEmailExists("new@mail.com")).thenReturn(false); // (Bổ sung nếu có)
        Mockito.when(usersDao.register(any(Users.class))).thenReturn(true);

        RegisterResult result = registerService.registerUser("newuser", "123456", "Full Name", "new@mail.com");
        
        assertEquals(RegisterResult.SUCCESS, result);
    }

    @Test
    public void testRegister_UsernameExists() {
        setTestCaseInfo("REG_SVC_02", "Đăng ký trùng Username", "1. Mock DAO checkUserExists = true", 
                "User: existingUser", "Result: USERNAME_EXISTS");

        Mockito.when(usersDao.checkUserExists("existingUser")).thenReturn(true);

        RegisterResult result = registerService.registerUser("existingUser", "123456", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.USERNAME_EXISTS, result);
    }

    // --- BỔ SUNG 1: TEST USERNAME CHỨA KHOẢNG TRẮNG ---
    @Test
    public void testRegister_UsernameWithSpace() {
        setTestCaseInfo("REG_SVC_09", "Username chứa khoảng trắng", "Nhập 'my user'", 
                "User: 'my user'", "Result: INVALID_INPUT");

        RegisterResult result = registerService.registerUser("my user", "123456", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    // --- BỔ SUNG 2: TEST TRÙNG EMAIL (Nâng cao) ---
    // (Bỏ comment test này nếu bạn đã implement checkEmailExists trong DAO)
    /*
    @Test
    public void testRegister_EmailExists() {
        setTestCaseInfo("REG_SVC_10", "Đăng ký trùng Email", "Mock DAO checkEmailExists = true", 
                "Email: duplicate@mail.com", "Result: EMAIL_EXISTS");

        Mockito.when(usersDao.checkUserExists("newUser")).thenReturn(false);
        Mockito.when(usersDao.checkEmailExists("duplicate@mail.com")).thenReturn(true);

        RegisterResult result = registerService.registerUser("newUser", "123456", "Name", "duplicate@mail.com");
        
        // Bạn cần thêm EMAIL_EXISTS vào enum RegisterResult
        assertEquals(RegisterResult.EMAIL_EXISTS, result);
    }
    */

    @Test
    public void testRegister_ShortPassword() {
        setTestCaseInfo("REG_SVC_03", "Mật khẩu quá ngắn (<6)", "Nhập pass 3 ký tự", 
                "Pass: 123", "Result: INVALID_INPUT");

        RegisterResult result = registerService.registerUser("user", "123", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.INVALID_INPUT, result); 
    }

    @Test
    public void testRegister_InvalidEmail() {
        setTestCaseInfo("REG_SVC_04", "Email sai định dạng", "Nhập email không có @", 
                "Email: invalid-email", "Result: INVALID_INPUT");

        RegisterResult result = registerService.registerUser("user", "123456", "Name", "invalid-email");
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    @Test
    public void testRegister_DaoFails() {
        setTestCaseInfo("REG_SVC_05", "Lỗi DB khi Insert", "Mock DAO register trả về false", 
                "Data hợp lệ", "Result: REGISTRATION_FAILED");

        Mockito.when(usersDao.checkUserExists("user")).thenReturn(false);
        Mockito.when(usersDao.register(any(Users.class))).thenReturn(false);

        RegisterResult result = registerService.registerUser("user", "123456", "Name", "valid@mail.com");
        
        assertEquals(RegisterResult.REGISTRATION_FAILED, result);
    }
    
    @Test
    public void testRegister_NullInput() {
        setTestCaseInfo("REG_SVC_06", "Đăng ký với Input Null", "Truyền username = null", "User: null", "Result: INVALID_INPUT");

        RegisterResult result = registerService.registerUser(null, "123456", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    @Test
    public void testRegister_EmptyInput() {
        setTestCaseInfo("REG_SVC_07", "Đăng ký với Input Rỗng", "Truyền username = \"\" (chuỗi rỗng)", "User: \"\"", "Result: INVALID_INPUT");

        RegisterResult result = registerService.registerUser("", "123456", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    @Test
    public void testRegister_PasswordBoundary() {
        setTestCaseInfo("REG_SVC_08", "Mật khẩu đúng 6 ký tự (Biên)", "Pass dài 6 ký tự (hợp lệ)", "Pass: 123456", "Result: SUCCESS");

        Mockito.when(usersDao.checkUserExists("boundaryUser")).thenReturn(false);
        Mockito.when(usersDao.register(any(Users.class))).thenReturn(true);

        RegisterResult result = registerService.registerUser("boundaryUser", "123456", "Name", "mail@test.com");
        
        assertEquals(RegisterResult.SUCCESS, result);
    }

    // ==========================================
    // XUẤT EXCEL (MỚI)
    // ==========================================
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
        ExcelTestExporter.exportToExcel("KetQuaTest_RegisterService.xlsx");
    }
}