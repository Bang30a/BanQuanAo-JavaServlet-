package unit;

import control.user.CheckoutServlet;
import entity.CartBean;
import entity.Users;
import util.ExcelTestExporter;

// === IMPORT REFLECTION ===
import java.lang.reflect.Method; // <--- Chìa khóa vạn năng

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

public class CheckoutServletTest {

    private CheckoutServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

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
        servlet = new CheckoutServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
    }

    // === HÀM HỖ TRỢ: GỌI PROTECTED METHOD ===
    // Đây là "chìa khóa" giúp bạn gọi hàm doGet đang bị khóa (protected)
    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1. Lấy thông tin hàm doGet từ class Servlet
        Method doGetMethod = CheckoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        
        // 2. Mở khóa (Cho phép truy cập dù là protected/private)
        doGetMethod.setAccessible(true);
        
        // 3. Thực thi hàm
        doGetMethod.invoke(servlet, req, resp);
    }

    // TEST 1: Chưa đăng nhập
    @Test
    public void testDoGet_NotLoggedIn() throws Exception {
        setTestCaseInfo("CHECKOUT_01", "Chưa đăng nhập", 
                "User=null -> gọi doGet()", "User=null", "Redirect Login");

        when(session.getAttribute("user")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/checkout");

        // THAY VÌ GỌI: servlet.doGet(...) -> GỌI QUA REFLECTION
        invokeDoGet(request, response);

        verify(session).setAttribute(eq("loginError"), anyString());
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // TEST 2: Giỏ hàng Null
    @Test
    public void testDoGet_CartNull() throws Exception {
        setTestCaseInfo("CHECKOUT_02", "Giỏ hàng Null", 
                "User ok, Cart=null", "Cart=null", "Redirect view-cart");

        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(null);

        // GỌI QUA REFLECTION
        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("/user/order/view-cart.jsp"));
    }

    // TEST 3: Giỏ hàng Empty
    @Test
    public void testDoGet_CartEmpty() throws Exception {
        setTestCaseInfo("CHECKOUT_03", "Giỏ hàng Rỗng", 
                "Cart size=0", "Size=0", "Redirect view-cart");

        List<CartBean> emptyCart = new ArrayList<>();
        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(emptyCart);

        // GỌI QUA REFLECTION
        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("/user/order/view-cart.jsp"));
    }

    // TEST 4: Success
    @Test
    public void testDoGet_Success() throws Exception {
        setTestCaseInfo("CHECKOUT_04", "Vào trang Checkout", 
                "User ok, Cart ok", "User, Cart(1)", "Forward Checkout.jsp");

        when(session.getAttribute("user")).thenReturn(new Users());
        List<CartBean> validCart = new ArrayList<>();
        validCart.add(new CartBean());
        when(session.getAttribute("cart")).thenReturn(validCart);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // GỌI QUA REFLECTION
        invokeDoGet(request, response);

        verify(request).getRequestDispatcher(contains("Checkout.jsp"));
        verify(dispatcher).forward(request, response);
    }

    // === EXCEL REPORT ===
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
        ExcelTestExporter.exportToExcel("KetQuaTest_CheckoutServlet.xlsx");
    }
}