package service;

// === IMPORT CƠ BẢN ===
import dao.UsersDao;
import entity.Users;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT MOCKITO ===
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

// === IMPORT ĐỂ TẠO BÁO CÁO EXCEL/CSV ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Unit Test cho RegisterService (Chức năng Đăng ký).
 * Xuất kết quả ra file Excel (CSV).
 */
public class RegisterServiceTest {

    // 1. Tạo DAO "giả"
    @Mock
    private UsersDao usersDaoMock;

    // 2. Tiêm mock DAO vào service
    @InjectMocks
    private RegisterService registerService;

    // Dữ liệu hợp lệ để test
    private final String VALID_USER = "testuser";
    private final String VALID_PASS = "ValidPass123";
    private final String VALID_NAME = "Test User";
    private final String VALID_EMAIL = "test@example.com";

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách lưu kết quả để xuất Excel
    private static final List<String[]> finalReportData = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
    public void testRegister_Success() {
        setTestCaseInfo(
            "REG_01", 
            "Đăng ký thành công (Happy Path)", 
            "1. Nhập đủ thông tin\n2. User chưa tồn tại\n3. DAO Register OK", 
            "User: testuser\nPass: ValidPass123", 
            "Trả về SUCCESS\nGọi hàm register"
        );

        // --- ARRANGE ---
        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(false);
        when(usersDaoMock.register(any(Users.class))).thenReturn(true);
        
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // --- ACT ---
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        // --- ASSERT ---
        assertEquals(RegisterResult.SUCCESS, result);
        verify(usersDaoMock).register(userCaptor.capture());
        
        Users capturedUser = userCaptor.getValue();
        assertEquals(VALID_USER, capturedUser.getUsername());
        assertEquals(VALID_PASS, capturedUser.getPassword());
        assertEquals("user", capturedUser.getRole());
    }

    @Test
    public void testRegister_UsernameExists() {
        setTestCaseInfo(
            "REG_02", 
            "Đăng ký thất bại (Trùng Username)", 
            "1. Nhập thông tin\n2. User ĐÃ tồn tại", 
            "User: testuser (Existed)", 
            "Trả về USERNAME_EXISTS\nKhông gọi register"
        );

        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(true);

        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        assertEquals(RegisterResult.USERNAME_EXISTS, result);
        verify(usersDaoMock, never()).register(any(Users.class));
    }

    @Test
    public void testRegister_DaoFails() {
        setTestCaseInfo(
            "REG_03", 
            "Đăng ký thất bại (Lỗi DAO/DB)", 
            "1. User chưa tồn tại\n2. DAO Register trả về FALSE", 
            "Simulate DB Error", 
            "Trả về REGISTRATION_FAILED"
        );

        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(false);
        when(usersDaoMock.register(any(Users.class))).thenReturn(false);

        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        assertEquals(RegisterResult.REGISTRATION_FAILED, result);
    }
    
    @Test
    public void testRegister_InvalidInput_Empty() {
        setTestCaseInfo(
            "REG_04", 
            "Đăng ký lỗi (Input Rỗng)", 
            "Nhập Fullname là chuỗi trắng", 
            "Fullname: '   '", 
            "Trả về INVALID_INPUT"
        );

        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, "   ", VALID_EMAIL);
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
        verify(usersDaoMock, never()).checkUserExists(anyString());
    }
    
    @Test
    public void testRegister_InvalidInput_Null() {
        setTestCaseInfo(
            "REG_05", 
            "Đăng ký lỗi (Input Null)", 
            "Nhập Email là Null", 
            "Email: null", 
            "Trả về INVALID_INPUT"
        );

        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, null);
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }
    
    @Test
    public void testRegister_InvalidInput_PasswordTooShort() {
        setTestCaseInfo(
            "REG_06", 
            "Đăng ký lỗi (Pass ngắn)", 
            "Nhập Pass < 6 ký tự", 
            "Pass: 12345", 
            "Trả về INVALID_INPUT"
        );

        RegisterResult result = registerService.registerUser(VALID_USER, "12345", VALID_NAME, VALID_EMAIL);
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    @Test
    public void testRegister_InvalidInput_BadEmail() {
        setTestCaseInfo(
            "REG_07", 
            "Đăng ký lỗi (Email sai định dạng)", 
            "Nhập Email không có @", 
            "Email: this-is-not-an-email", 
            "Trả về INVALID_INPUT"
        );

        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, "this-is-not-an-email");
        
        assertEquals(RegisterResult.INVALID_INPUT, result);
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
    public static void exportToExcelCSV() {
        String fileName = "KetQuaTest_DangKy.csv"; // Tên file riêng
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo Đăng Ký ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            writer.write('\ufeff'); // BOM cho Tiếng Việt

            // Header
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
            
            System.out.println("XONG! File 'KetQuaTest_DangKy.csv' đã được tạo.");
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