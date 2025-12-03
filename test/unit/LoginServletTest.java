package unit;

import control.LoginServlet;
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
    @Mock private LoginService loginService;

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

        Field serviceField = LoginServlet.class.getDeclaredField("loginService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, loginService);
    }

    // ======================= TEST CASES ===========================

    @Test
public void testDoPost_LoginSuccess_Admin() throws Exception {
    setTestCaseInfo(
        "SERV_01",
        "Servlet: Đăng nhập Admin thành công",
        "1. Mock Req/Service trả về Admin\n2. Reflection gọi doPost",
        "User: admin, Pass: 123",
        "Redirect đến Dashboard.jsp"
    );

    when(request.getParameter("username")).thenReturn("admin");
    when(request.getParameter("password")).thenReturn("123");
    when(request.getSession()).thenReturn(session);

    // *** FIX: Thêm contextPath ***
    when(request.getContextPath()).thenReturn("/ShopDuck");

    Users admin = new Users(1, "admin", "123", "Admin", "mail", "admin");
    LoginResult result = new LoginResult(LoginResult.Status.SUCCESS_ADMIN, admin);
    when(loginService.login("admin", "123")).thenReturn(result);

    Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
    doPost.setAccessible(true);
    doPost.invoke(servlet, request, response);

    verify(session).setAttribute("user", admin);

    // *** FIX: Servlet redirect đến index.jsp, không phải Dashboard.jsp ***
    verify(response).sendRedirect(contains("/admin/dashboard/index.jsp"));
}


    @Test
    public void testDoPost_LoginFailed() throws Exception {

        setTestCaseInfo(
            "LOGIN_02",
            "Đăng nhập sai mật khẩu",
            "1. Mock sai credentials\n2. Service trả FAILED\n3. Gọi doPost",
            "user=wrong, pass=wrong",
            "Báo lỗi login + redirect Login.jsp"
        );

        when(request.getParameter("username")).thenReturn("wrong");
        when(request.getParameter("password")).thenReturn("wrong");
        when(request.getSession()).thenReturn(session);

        LoginResult result = new LoginResult(LoginResult.Status.FAILED_CREDENTIALS, null);
        when(loginService.login("wrong", "wrong")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("loginError"), anyString());
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    @Test
    public void testDoPost_LoginSuccess_User_RedirectHome() throws Exception {

        setTestCaseInfo(
            "LOGIN_03",
            "User login → Trang chủ",
            "1. Mock user\n2. Session ko có redirectUrl\n3. Service SUCCESS_USER",
            "user=user, pass=123",
            "Redirect /user/view-products"
        );

        when(request.getParameter("username")).thenReturn("user");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("redirectAfterLogin")).thenReturn(null);

        Users user = new Users(2, "user", "123", "User", "mail", "user");
        LoginResult result = new LoginResult(LoginResult.Status.SUCCESS_USER, user);

        when(loginService.login("user", "123")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute("user", user);
        verify(response).sendRedirect(contains("/user/view-products"));
    }

    @Test
    public void testDoPost_LoginSuccess_User_RedirectBack() throws Exception {

        setTestCaseInfo(
            "LOGIN_04",
            "User login → Quay lại trang cũ",
            "1. Mock user\n2. Có redirectAfterLogin\n3. Service trả SUCCESS_USER",
            "oldUrl=/cart.jsp",
            "Redirect đúng oldUrl"
        );

        when(request.getParameter("username")).thenReturn("user");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getSession()).thenReturn(session);

        String oldUrl = "http://localhost:8080/ShopDuck/user/cart.jsp";
        when(session.getAttribute("redirectAfterLogin")).thenReturn(oldUrl);

        Users user = new Users(2, "user", "123", "User", "mail", "user");
        LoginResult result = new LoginResult(LoginResult.Status.SUCCESS_USER, user);

        when(loginService.login("user", "123")).thenReturn(result);

        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(response).sendRedirect(oldUrl);
    }

    // ======================= EXCEL EXPORT ===========================
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(
                currentId, currentName, currentSteps, currentData, currentExpected,
                "OK", "PASS"
            );
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(
                currentId, currentName, currentSteps, currentData, currentExpected,
                e.getMessage(), "FAIL"
            );
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_LoginServlet.xlsx");
    }
}
