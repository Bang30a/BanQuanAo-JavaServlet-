// Đặt trong "Test Packages/service/"
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
import org.mockito.ArgumentCaptor; // Import công cụ "bắt" đối tượng
import org.mockito.MockitoAnnotations;

// === IMPORT ĐỂ TẠO BÁO CÁO (ĐÃ THÊM VÀO) ===
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp Unit Test cho RegisterService.
 * Nó kiểm tra tất cả logic validation và nghiệp vụ của RegisterService.
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

    // 3. Hàm chạy trước mỗi @Test
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    /**
     * Test Case 1: Đăng ký thành công (Happy Path).
     */
    @Test
    public void testRegister_Success() {
        // --- ARRANGE (Chuẩn bị) ---
        // "Dạy" mock: Khi kiểm tra tồn tại, trả về false (user chưa tồn tại)
        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(false);
        // "Dạy" mock: Khi gọi đăng ký, trả về true (đăng ký thành công)
        when(usersDaoMock.register(any(Users.class))).thenReturn(true);
        
        // Tạo một công cụ để "bắt" đối tượng Users được truyền cho DAO
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // --- ACT (Hành động) ---
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        // --- ASSERT (Xác minh) ---
        // 1. Kiểm tra kết quả trả về
        assertEquals(RegisterResult.SUCCESS, result);
        
        // 2. Xác minh rằng hàm register() CÓ được gọi
        verify(usersDaoMock).register(userCaptor.capture());
        
        // 3. Kiểm tra đối tượng Users đã được tạo chính xác
        Users capturedUser = userCaptor.getValue();
        assertEquals(VALID_USER, capturedUser.getUsername());
        assertEquals(VALID_PASS, capturedUser.getPassword());
        assertEquals(VALID_NAME, capturedUser.getFullname());
        assertEquals(VALID_EMAIL, capturedUser.getEmail());
        assertEquals("user", capturedUser.getRole()); // Kiểm tra vai trò mặc định
    }

    /**
     * Test Case 2: Đăng ký thất bại do Username đã tồn tại.
     */
    @Test
    public void testRegister_UsernameExists() {
        // --- ARRANGE ---
        // "Dạy" mock: Khi kiểm tra tồn tại, trả về true (user ĐÃ tồn tại)
        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(true);

        // --- ACT ---
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        // --- ASSERT ---
        // 1. Kiểm tra kết quả
        assertEquals(RegisterResult.USERNAME_EXISTS, result);
        
        // 2. Xác minh rằng hàm register() KHÔNG BAO GIỜ được gọi
        verify(usersDaoMock, never()).register(any(Users.class));
    }

    /**
     * Test Case 3: Đăng ký thất bại do Lỗi DAO (ví dụ: mất kết nối DB).
     */
    @Test
    public void testRegister_DaoFails() {
        // --- ARRANGE ---
        // "Dạy" mock: User chưa tồn tại
        when(usersDaoMock.checkUserExists(VALID_USER)).thenReturn(false);
        // "Dạy" mock: Nhưng khi đăng ký thì thất bại (trả về false)
        when(usersDaoMock.register(any(Users.class))).thenReturn(false);

        // --- ACT ---
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, VALID_EMAIL);

        // --- ASSERT ---
        assertEquals(RegisterResult.REGISTRATION_FAILED, result);
    }
    
    /**
     * Test Case 4: Đăng ký thất bại do Input rỗng (empty string).
     */
    @Test
    public void testRegister_InvalidInput_Empty() {
        // --- ACT ---
        // Test với fullname là rỗng (chỉ chứa khoảng trắng)
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, "   ", VALID_EMAIL);
        
        // --- ASSERT ---
        assertEquals(RegisterResult.INVALID_INPUT, result);
        // Xác minh không có hàm DAO nào được gọi
        verify(usersDaoMock, never()).checkUserExists(anyString());
        verify(usersDaoMock, never()).register(any(Users.class));
    }
    
    /**
     * Test Case 5: Đăng ký thất bại do Input là null.
     */
    @Test
    public void testRegister_InvalidInput_Null() {
        // --- ACT ---
        // Test với email là null
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, null);
        
        // --- ASSERT ---
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }
    
    /**
     * Test Case 6: Đăng ký thất bại do Mật khẩu quá ngắn.
     */
    @Test
    public void testRegister_InvalidInput_PasswordTooShort() {
        // --- ACT ---
        // Test với mật khẩu "12345" (5 ký tự)
        RegisterResult result = registerService.registerUser(VALID_USER, "12345", VALID_NAME, VALID_EMAIL);
        
        // --- ASSERT ---
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    /**
     * Test Case 7: Đăng ký thất bại do Email sai định dạng.
     */
    @Test
    public void testRegister_InvalidInput_BadEmail() {
        // --- ACT ---
        // Test với email sai định dạng
        RegisterResult result = registerService.registerUser(VALID_USER, VALID_PASS, VALID_NAME, "this-is-not-an-email");
        
        // --- ASSERT ---
        assertEquals(RegisterResult.INVALID_INPUT, result);
    }

    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (GIỮ NGUYÊN) ===
    // =================================================================

    // 1. Danh sách lưu kết quả (mỗi phần tử là 1 mảng 4 cột)
    private static final List<String[]> testResults = new ArrayList<>();

    // 2. Sử dụng @Rule và TestWatcher để "theo dõi" từng test case
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        
        private String getModuleName(Description d) {
            String className = d.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);
        }

        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "PASS",
                ""
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMessage = (e == null) ? "Unknown Error" : e.getMessage();
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "FAIL",
                errorMessage
            });
        }
    };

    // 3. Sử dụng @AfterClass để in bảng và ghi ra file
    @AfterClass
    public static void writeTestReport() {
        String filePath = "test-report.txt";
        
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        
        // Sửa lỗi .repeat() cho Java 8
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'), 
            new String(new char[30]).replace('\0', '-')
        ) + "-+-%-45s-+-%-8s-+-%s\n".substring(1); // Cần điều chỉnh một chút cho đúng format

        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath, true), StandardCharsets.UTF_8)) { // 'true' để NỐI VÀO FILE
            
            // Ghi header nếu file mới (hoặc bạn có thể quản lý việc này)
            // Lần này, chúng ta sẽ ghi nối tiếp vào file
            writer.write("\n--- Kết quả chạy " + RegisterServiceTest.class.getName() + " ---\n");
            
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                
                System.out.print(line);
                writer.write(line);
            }
            
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được NỐI VÀO file: " + filePath);

        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());        }
    }
}