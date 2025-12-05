package unit;

// === IMPORT LOGIC ===
import control.RegisterServlet;
import dao.UsersDao;
import entity.Users;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT REFLECTION & SERVLET ===
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

// === IMPORT JUNIT ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class RegisterServletTest {

    private RegisterServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private UsersDao usersDao; // Mock DAO

    // === CẤU HÌNH BÁO CÁO (Dùng biến instance) ===
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
        servlet = new RegisterServlet();
        // Inject Mock DAO vào Servlet
        servlet.setUsersDao(usersDao); 
    }

    // --- CASE 1: Đăng ký thành công ---
    @Test
    public void testDoPost_RegisterSuccess() throws Exception {
        setTestCaseInfo("REG_SERV_01", "Servlet: Đăng ký thành công", 
                "1. Mock input hợp lệ\n2. Mock DAO register=true\n3. Call doPost", 
                "User: new, Pass: 123456", "Redirect: Login.jsp");

        // 1. Mock Request
        when(request.getParameter("username")).thenReturn("newuser");
        when(request.getParameter("password")).thenReturn("123456"); // Pass >= 6 chars
        when(request.getParameter("fullname")).thenReturn("New User");
        when(request.getParameter("email")).thenReturn("new@mail.com");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Mock DAO Behavior
        when(usersDao.checkUserExists("newuser")).thenReturn(false);
        when(usersDao.register(any(Users.class))).thenReturn(true);

        // 3. Run
        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 4. Verify
        verify(session).setAttribute(contains("registerSuccess"), any());
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // --- CASE 2: Mật khẩu yếu (MỚI) ---
    @Test
    public void testDoPost_WeakPassword() throws Exception {
        setTestCaseInfo("REG_SERV_02", "Servlet: Mật khẩu yếu", 
                "1. Mock pass ngắn (<6 ký tự)\n2. Call doPost", 
                "Pass: 123", "Báo lỗi 'Mật khẩu quá yếu'");

        when(request.getParameter("username")).thenReturn("user");
        when(request.getParameter("password")).thenReturn("123"); // Pass ngắn
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify: Phải set attribute lỗi mật khẩu và redirect lại trang register
        verify(session).setAttribute(eq("registerError"), contains("Mật khẩu quá yếu"));
        verify(response).sendRedirect(contains("/user/auth/Register.jsp"));
        
        // Verify phụ: Không được gọi tới DAO checkUserExists hay register
        verify(usersDao, never()).checkUserExists(anyString());
    }

    // --- CASE 3: Trùng tên đăng nhập ---
    @Test
    public void testDoPost_UserExists() throws Exception {
        setTestCaseInfo("REG_SERV_03", "Servlet: Trùng tên đăng nhập", 
                "1. Mock DAO checkExists=true", 
                "User: exist", "Redirect: Register.jsp + Error");

        when(request.getParameter("username")).thenReturn("exist");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(usersDao.checkUserExists("exist")).thenReturn(true);

        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("registerError"), contains("tồn tại"));
        verify(response).sendRedirect(contains("/user/auth/Register.jsp"));
    }

    // --- CASE 4: Lỗi Database ---
    @Test
    public void testDoPost_DbError() throws Exception {
        setTestCaseInfo("REG_SERV_04", "Servlet: Lỗi Database", 
                "1. Mock DAO register=false", 
                "User: valid", "Redirect: Register.jsp + Error");

        when(request.getParameter("username")).thenReturn("valid");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(usersDao.checkUserExists("valid")).thenReturn(false);
        when(usersDao.register(any(Users.class))).thenReturn(false);

        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("registerError"), contains("thất bại"));
        verify(response).sendRedirect(contains("/user/auth/Register.jsp"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_RegisterServlet.xlsx");
    }
}