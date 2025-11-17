package service;

// === IMPORT CƠ BẢN ===
import dao.UsersDao;
import entity.Users;
import service.LoginResult.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT MOCKITO ===
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

// === IMPORT ĐỂ TẠO BÁO CÁO ===
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Thêm để hỗ trợ UTF-8 (tiếng Việt)
import java.io.OutputStreamWriter; // Thêm
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp Unit Test cho LoginService.
 * Nó chỉ kiểm tra logic nghiệp vụ của LoginService MỘT CÁCH CÔ LẬP.
 */
public class LoginServiceTest {

    // 1. Tạo một DAO "giả" (Mock)
    @Mock
    private UsersDao usersDaoMock;

    // 2. Tự động "tiêm" (Inject) usersDaoMock vào loginService
    @InjectMocks
    private LoginService loginService;

    // 3. Hàm này sẽ chạy trước MỖI hàm @Test
    @Before
public void setUp() {
    MockitoAnnotations.openMocks(this); // Thay vì initMocks
}

    // -- BẮT ĐẦU CÁC TEST CASE --

    /**
     * Test Case 1: Đăng nhập thành công với vai trò ADMIN.
     */
    @Test
    public void testLogin_AdminSuccess() {
        // --- ARRANGE (Chuẩn bị) ---
        String username = "admin";
        String password = "123";

        // Tạo một đối tượng Users "giả" mà DAO sẽ trả về
        Users fakeAdmin = new Users();
        fakeAdmin.setRole("admin"); // (Bạn cần có hàm setRole trong Users.java)

        // "Dạy" cho Mockito:
        // "KHI usersDaoMock.login('admin', '123') được gọi, THÌ hãy trả về fakeAdmin"
        when(usersDaoMock.login(username, password)).thenReturn(fakeAdmin);

        // --- ACT (Hành động) ---
        LoginResult result = loginService.login(username, password);

        // --- ASSERT (Xác minh) ---
        assertNotNull("Kết quả không được null", result);
        // Kiểm tra xem Status có phải là SUCCESS_ADMIN không
        assertEquals(Status.SUCCESS_ADMIN, result.getStatus());
        // Kiểm tra xem User trả về có phải là fakeAdmin không
        assertEquals(fakeAdmin, result.getUser());
        
        // (Tùy chọn) Xác minh rằng hàm usersDaoMock.login ĐÃ được gọi 1 lần
        verify(usersDaoMock, times(1)).login(username, password);
    }

    /**
     * Test Case 2: Đăng nhập thành công với vai trò USER.
     */
    @Test
    public void testLogin_UserSuccess() {
        // --- ARRANGE ---
        String username = "user";
        String password = "456";
        Users fakeUser = new Users();
        fakeUser.setRole("user"); // Đặt vai trò là user

        // "Dạy" cho Mockito
        when(usersDaoMock.login(username, password)).thenReturn(fakeUser);

        // --- ACT ---
        LoginResult result = loginService.login(username, password);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(Status.SUCCESS_USER, result.getStatus());
        assertEquals(fakeUser, result.getUser());
        verify(usersDaoMock).login(username, password);
    }

    /**
     * Test Case 3: Đăng nhập thất bại (Sai username hoặc password).
     */
    @Test
    public void testLogin_FailedCredentials() {
        // --- ARRANGE ---
        String username = "wrong";
        String password = "wrong";

        // "Dạy" cho Mockito:
        // "KHI usersDaoMock.login(...) được gọi, THÌ hãy trả về NULL"
        // (Vì đó là cách DAO báo rằng đăng nhập thất bại)
        when(usersDaoMock.login(username, password)).thenReturn(null);

        // --- ACT ---
        LoginResult result = loginService.login(username, password);

        // --- ASSERT ---
        assertNotNull(result);
        // Kiểm tra status
        assertEquals(Status.FAILED_CREDENTIALS, result.getStatus());
        // Kiểm tra xem User object phải là NULL
        assertNull(result.getUser());
        verify(usersDaoMock).login(username, password);
    }

    /**
     * Test Case 4: Đăng nhập thành công, nhưng vai trò không hợp lệ.
     */
    @Test
    public void testLogin_InvalidRole() {
        // --- ARRANGE ---
        String username = "guest";
        String password = "789";
        Users fakeGuest = new Users();
        fakeGuest.setRole("guest"); // Vai trò "guest" không có trong switch-case

        // "Dạy" cho Mockito
        when(usersDaoMock.login(username, password)).thenReturn(fakeGuest);

        // --- ACT ---
        LoginResult result = loginService.login(username, password);

        // --- ASSERT ---
        assertNotNull(result);
        // Kiểm tra status
        assertEquals(Status.FAILED_INVALID_ROLE, result.getStatus());
        // Kiểm tra xem User object phải là NULL (theo logic của bạn)
        assertNull(result.getUser());
        verify(usersDaoMock).login(username, password);
    }
    
    /**
     * Test Case 5 (Bonus): Kiểm tra vai trò không phân biệt chữ hoa/thường.
     */
    @Test
    public void testLogin_AdminSuccess_CaseInsensitive() {
        // --- ARRANGE ---
        Users fakeAdmin = new Users();
        fakeAdmin.setRole("Admin"); // Chữ 'A' viết hoa

        when(usersDaoMock.login(anyString(), anyString())).thenReturn(fakeAdmin);

        // --- ACT ---
        LoginResult result = loginService.login("admin", "123");

        // --- ASSERT ---
        // Vẫn phải ra SUCCESS_ADMIN vì bạn đã dùng .toLowerCase()
        assertEquals(Status.SUCCESS_ADMIN, result.getStatus());
    }

    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (ĐÃ THÊM VÀO) ===
    // =================================================================

    // 1. Danh sách lưu kết quả (mỗi phần tử là 1 mảng 4 cột)
    private static final List<String[]> testResults = new ArrayList<>();

    // 2. Sử dụng @Rule và TestWatcher để "theo dõi" từng test case
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        
        // Trích xuất tên Module (class)
        private String getModuleName(Description d) {
            String className = d.getClassName();
            // Lấy phần tên class (sau dấu chấm cuối cùng)
            return className.substring(className.lastIndexOf('.') + 1);
        }

        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description),     // Cột 1: Module
                description.getMethodName(),    // Cột 2: Tên Test Case
                "PASS",                         // Cột 3: Status
                ""                              // Cột 4: Details (trống)
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMessage = (e == null) ? "Unknown Error" : e.getMessage();
            testResults.add(new String[]{
                getModuleName(description),     // Cột 1: Module
                description.getMethodName(),    // Cột 2: Tên Test Case
                "FAIL",                         // Cột 3: Status
                errorMessage                    // Cột 4: Details (Lỗi)
            });
        }
    };

    // 3. Sử dụng @AfterClass để in bảng và ghi ra file
    @AfterClass
    public static void writeTestReport() {
        String filePath = "test-report.txt"; // Tên file .txt
        
        // Định dạng các cột
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'), 
            new String(new char[30]).replace('\0', '-')
        );

        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        // Ghi vào file (sử dụng UTF-8 để tránh lỗi font)
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            
            writer.write("=============== TEST EXECUTION REPORT ================\n");
            writer.write(header);
            writer.write(separator);
            
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                System.out.print(line);
                writer.write(line);
            }
            
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được xuất ra file: " + filePath);

        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());
        }
    }
}
