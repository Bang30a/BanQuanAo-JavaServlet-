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

// === IMPORT HỖ TRỢ GHI FILE ===
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

public class LoginServiceTest {

    @Mock
    private UsersDao usersDaoMock;

    @InjectMocks
    private LoginService loginService;

    // Các biến lưu thông tin tạm thời
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách kết quả tổng
    private static final List<String[]> finalReportData = new ArrayList<>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Hàm điền thông tin đầu vào
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // ================= TEST CASES =================

    @Test
    public void testLogin_AdminSuccess() {
        setTestCaseInfo("TC01", "Đăng nhập Admin thành công", "1. Nhập User/Pass đúng\n2. Bấm Login", "User: admin\nPass: 123", "SUCCESS_ADMIN");

        Users fakeAdmin = new Users();
        fakeAdmin.setRole("admin");
        when(usersDaoMock.login("admin", "123")).thenReturn(fakeAdmin);

        LoginResult result = loginService.login("admin", "123");
        assertEquals(Status.SUCCESS_ADMIN, result.getStatus());
    }

    @Test
    public void testLogin_UserSuccess() {
        setTestCaseInfo("TC02", "Đăng nhập User thành công", "1. Nhập User/Pass đúng\n2. Bấm Login", "User: user\nPass: 456", "SUCCESS_USER");

        Users fakeUser = new Users();
        fakeUser.setRole("user");
        when(usersDaoMock.login("user", "456")).thenReturn(fakeUser);

        LoginResult result = loginService.login("user", "456");
        assertEquals(Status.SUCCESS_USER, result.getStatus());
    }

    @Test
    public void testLogin_FailedCredentials() {
        setTestCaseInfo("TC03", "Đăng nhập sai mật khẩu", "1. Nhập sai Pass\n2. Bấm Login", "User: wrong\nPass: wrong", "FAILED_CREDENTIALS");

        when(usersDaoMock.login("wrong", "wrong")).thenReturn(null);

        LoginResult result = loginService.login("wrong", "wrong");
        assertEquals(Status.FAILED_CREDENTIALS, result.getStatus());
    }
    
    @Test
    public void testLogin_InvalidRole() {
        setTestCaseInfo("TC04", "Lỗi phân quyền (Role)", "1. Đăng nhập nick Guest", "User: guest", "FAILED_INVALID_ROLE");

        Users fakeGuest = new Users();
        fakeGuest.setRole("guest");
        when(usersDaoMock.login("guest", "789")).thenReturn(fakeGuest);

        LoginResult result = loginService.login("guest", "789");
        assertEquals(Status.FAILED_INVALID_ROLE, result.getStatus());
    }

    // ================= XỬ LÝ REPORT EXCEL/CSV =================

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
        // Tên file sẽ xuất ra
        String fileName = "KetQuaKiemThu.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo ra file Excel (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            // QUAN TRỌNG: Thêm BOM (Byte Order Mark) để Excel nhận diện đúng Tiếng Việt
            writer.write('\ufeff');

            // 1. Viết tiêu đề cột
            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            // 2. Viết dữ liệu
            for (String[] row : finalReportData) {
                // Hàm escapeSpecialChars giúp xử lý việc xuống dòng trong ô Excel
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
            
            System.out.println("XONG! File đã được lưu tại thư mục gốc của Project.");
            System.out.println("Hãy mở file 'KetQuaKiemThu.csv' bằng Excel.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    /**
     * Hàm này giúp format dữ liệu chuẩn CSV để Excel không bị lỗi cột:
     * - Bao quanh nội dung bằng dấu ngoặc kép ""
     * - Nếu trong nội dung có dấu " thì thay bằng ""
     */
    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}