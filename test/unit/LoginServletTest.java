package unit;

import control.LoginServlet;
import dao.UsersDao;
import service.LoginService;
import service.LoginResult;
import entity.Users;

// JUnit + Mockito
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Excel Export
import util.ExcelTestExporter;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoginServletTest {

    private LoginServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    
    @Mock private LoginService loginService; // Mock Service
    @Mock private UsersDao usersDao;         // [QUAN TRỌNG] Mock DAO

    // ===== THÔNG TIN TEST CASE =====
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
    public void setUp() throws Exception {
        servlet = new LoginServlet();

        // 1. Inject Mock LoginService vào Servlet (dùng Reflection vì private)
        Field serviceField = LoginServlet.class.getDeclaredField("loginService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, loginService);

        // 2. [MỚI] Inject Mock UsersDao vào Servlet (dùng Setter bạn đã viết hoặc Reflection)
        // Vì trong Servlet bạn có hàm getUsersDao() lazy load, ta nên set trực tiếp vào field private
        Field daoField = LoginServlet.class.getDeclaredField("usersDao");
        daoField.setAccessible(true);
        daoField.set(servlet, usersDao);
    }

    // ======================= TEST CASES ===========================

    // --- CASE 1: Đăng nhập thành công (Admin) ---
    @Test
    public void testDoPost_LoginSuccess_Admin() throws Exception {
        setTestCaseInfo(
            "SERV_01",
            "Servlet: Đăng nhập Admin thành công",
            "1. Mock User tồn tại\n2. Mock Service trả về Admin",
            "User: admin, Pass: 123",
            "Redirect đến Dashboard"
        );

        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // [MỚI] Giả lập User có tồn tại trong DB
        when(usersDao.checkUserExists("admin")).thenReturn(true);

        // Giả lập Service trả về thành công
        Users admin = new Users(1, "admin", "123", "Admin", "mail", "admin");
        LoginResult result = new LoginResult(LoginResult.Status.SUCCESS_ADMIN, admin);
        when(loginService.login("admin", "123")).thenReturn(result);

        // Gọi doPost
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify
        verify(session).setAttribute("user", admin);
        verify(response).sendRedirect(contains("/admin/dashboard/index.jsp"));
    }

    // --- CASE 2: Tài khoản không tồn tại (MỚI) ---
    @Test
    public void testDoPost_UserNotFound() throws Exception {
        setTestCaseInfo(
            "SERV_02",
            "Servlet: Tài khoản không tồn tại",
            "1. Mock User KHÔNG tồn tại (DAO return false)\n2. Check lỗi",
            "User: not_exist",
            "Báo lỗi 'Tài khoản không tồn tại'"
        );

        when(request.getParameter("username")).thenReturn("not_exist");
        when(request.getParameter("password")).thenReturn("any");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // [QUAN TRỌNG] Giả lập DAO trả về false
        when(usersDao.checkUserExists("not_exist")).thenReturn(false);

        // Gọi doPost
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify: Phải set thông báo lỗi đúng
        verify(session).setAttribute("loginError", "Tài khoản không tồn tại!");
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
        
        // Verify phụ: Service login không được phép chạy
        verify(loginService, never()).login(anyString(), anyString());
    }

    // --- CASE 3: Sai mật khẩu ---
    @Test
    public void testDoPost_WrongPassword() throws Exception {
        setTestCaseInfo(
            "SERV_03",
            "Servlet: Sai mật khẩu",
            "1. Mock User CÓ tồn tại\n2. Service trả FAILED_CREDENTIALS",
            "User: user, Pass: wrong",
            "Báo lỗi 'Mật khẩu không chính xác'"
        );

        when(request.getParameter("username")).thenReturn("user");
        when(request.getParameter("password")).thenReturn("wrong");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 1. User có tồn tại
        when(usersDao.checkUserExists("user")).thenReturn(true);

        // 2. Nhưng Login Service báo sai
        LoginResult result = new LoginResult(LoginResult.Status.FAILED_CREDENTIALS, null);
        when(loginService.login("user", "wrong")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify: Thông báo lỗi phải là "Mật khẩu không chính xác"
        verify(session).setAttribute("loginError", "Mật khẩu không chính xác!");
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // --- CASE 4: Sai quyền truy cập ---
    @Test
    public void testDoPost_InvalidRole() throws Exception {
        setTestCaseInfo(
            "SERV_04",
            "Servlet: Quyền không hợp lệ",
            "1. Mock User CÓ tồn tại\n2. Service trả FAILED_INVALID_ROLE",
            "User: banned_user",
            "Báo lỗi 'Quyền truy cập không hợp lệ'"
        );

        when(request.getParameter("username")).thenReturn("banned");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(usersDao.checkUserExists("banned")).thenReturn(true);

        LoginResult result = new LoginResult(LoginResult.Status.FAILED_INVALID_ROLE, null);
        when(loginService.login("banned", "123")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(contains("loginError"), contains("không hợp lệ"));
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // --- CASE 5: Login User thành công (Redirect Home) ---
    @Test
    public void testDoPost_LoginSuccess_User() throws Exception {
        setTestCaseInfo(
            "SERV_05",
            "User login -> Trang chủ",
            "1. Mock User tồn tại\n2. Service SUCCESS_USER",
            "User: user",
            "Redirect /user/view-products"
        );

        when(request.getParameter("username")).thenReturn("user");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        
        when(usersDao.checkUserExists("user")).thenReturn(true);

        Users user = new Users(2, "user", "123", "User", "mail", "user");
        LoginResult result = new LoginResult(LoginResult.Status.SUCCESS_USER, user);
        when(loginService.login("user", "123")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute("user", user);
        verify(response).sendRedirect(contains("/user/view-products"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_LoginServlet.xlsx");
        System.out.println(">> Đã xuất file báo cáo: KetQuaTest_LoginServlet.xlsx");
    }
}