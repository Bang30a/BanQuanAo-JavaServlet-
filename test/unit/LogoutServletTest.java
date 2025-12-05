package unit;

import control.LogoutServlet;
import util.ExcelTestExporter;

// === IMPORT REFLECTION ===
import java.lang.reflect.Method;

// === IMPORT SERVLET API ===
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// === IMPORT JUNIT & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import static org.mockito.Mockito.*;

public class LogoutServletTest {

    private LogoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    // === REPORT VARS ===
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

    @Before
    public void setUp() {
        servlet = new LogoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        // Giả lập context path để redirect không bị lỗi
        when(request.getContextPath()).thenReturn("");
    }

    // === HÀM HỖ TRỢ: GỌI PROTECTED doGet ===
    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGetMethod = LogoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, req, resp);
    }

    // TEST 1: Đăng xuất khi ĐANG CÓ Session (Happy Case)
    @Test
    public void testLogout_SessionExists() throws Exception {
        setTestCaseInfo("LOGOUT_01", "Logout khi đang đăng nhập", 
                "Session != null -> invalidate()", 
                "Session tồn tại", 
                "Session bị hủy, Redirect Login");

        // 1. Giả lập: request.getSession(false) trả về session thật
        when(request.getSession(false)).thenReturn(session);

        // 2. Chạy
        invokeDoGet(request, response);

        // 3. Verify:
        // Quan trọng nhất: Kiểm tra hàm invalidate() có được gọi không?
        verify(session).invalidate();
        
        // Kiểm tra redirect về đúng trang Login
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // TEST 2: Đăng xuất khi KHÔNG CÓ Session (Đã thoát rồi mà bấm lại link logout)
    @Test
    public void testLogout_NoSession() throws Exception {
        setTestCaseInfo("LOGOUT_02", "Logout khi không có Session", 
                "Session == null -> Không làm gì", 
                "Session=null", 
                "Không lỗi, Redirect Login");

        // 1. Giả lập: Không tìm thấy session
        when(request.getSession(false)).thenReturn(null);

        // 2. Chạy
        invokeDoGet(request, response);

        // 3. Verify:
        // Đảm bảo KHÔNG được gọi invalidate() (vì session null mà gọi là NullPointerException ngay)
        verify(session, never()).invalidate();

        // Vẫn phải redirect về Login
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_LogoutServlet.xlsx");
    }
}