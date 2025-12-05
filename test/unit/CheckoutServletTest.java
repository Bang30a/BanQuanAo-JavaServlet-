package unit;

import control.user.CheckoutServlet;
import entity.CartBean;
import entity.Users;
import util.ExcelTestExporter;

// === IMPORT REFLECTION ===
import java.lang.reflect.Method; 

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
    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGetMethod = CheckoutServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, req, resp);
    }

    // [MỚI] HÀM HỖ TRỢ: GỌI PROTECTED doPost
    private void invokeDoPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doPostMethod = CheckoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(servlet, req, resp);
    }

    // TEST 1: Chưa đăng nhập
    @Test
    public void testDoGet_NotLoggedIn() throws Exception {
        setTestCaseInfo("CHECKOUT_01", "Chưa đăng nhập", 
                "User=null -> gọi doGet()", "User=null", "Lưu RedirectUrl & Chuyển Login");

        when(session.getAttribute("user")).thenReturn(null);
        // Giả lập request URI hiện tại để test logic lưu trang cũ
        when(request.getRequestURI()).thenReturn("/user/checkout"); 

        invokeDoGet(request, response);

        // Verify: Phải set thông báo lỗi
        verify(session).setAttribute(eq("loginError"), anyString());
        // [MỚI] Verify: Phải lưu lại URL trang hiện tại để login xong quay lại
        verify(session).setAttribute(eq("redirectAfterLogin"), eq("/user/checkout"));
        // Verify: Redirect
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
    }

    // TEST 2: Giỏ hàng Null
    @Test
    public void testDoGet_CartNull() throws Exception {
        setTestCaseInfo("CHECKOUT_02", "Giỏ hàng Null", 
                "User ok, Cart=null", "Cart=null", "Redirect view-cart");

        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(null);

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

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("/user/order/view-cart.jsp"));
    }

    // TEST 4: Success (doGet)
    @Test
    public void testDoGet_Success() throws Exception {
        setTestCaseInfo("CHECKOUT_04", "Vào trang Checkout (GET)", 
                "User ok, Cart ok", "User, Cart(1)", "Forward Checkout.jsp");

        when(session.getAttribute("user")).thenReturn(new Users());
        List<CartBean> validCart = new ArrayList<>();
        validCart.add(new CartBean());
        when(session.getAttribute("cart")).thenReturn(validCart);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        invokeDoGet(request, response);

        verify(request).getRequestDispatcher(contains("Checkout.jsp"));
        verify(dispatcher).forward(request, response);
    }

    // [MỚI] TEST 5: Success (doPost)
    // Kiểm tra xem doPost có gọi sang doGet đúng logic không
    @Test
    public void testDoPost_Success() throws Exception {
        setTestCaseInfo("CHECKOUT_05", "Vào trang Checkout (POST)", 
                "Gọi doPost -> Delegated to doGet", "User, Cart(1)", "Forward Checkout.jsp");

        when(session.getAttribute("user")).thenReturn(new Users());
        List<CartBean> validCart = new ArrayList<>();
        validCart.add(new CartBean());
        when(session.getAttribute("cart")).thenReturn(validCart);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Gọi doPost thay vì doGet
        invokeDoPost(request, response);

        verify(request).getRequestDispatcher(contains("Checkout.jsp"));
        verify(dispatcher).forward(request, response);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_CheckoutServlet.xlsx");
    }
}