package unit;

// === IMPORT LOGIC ===
import control.user.ConfirmCheckoutServlet;
import service.OrderService;
import service.OrderResult;
import entity.CartBean;
import entity.Users;
import util.ExcelTestExporter;

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmCheckoutServletTest {

    private ConfirmCheckoutServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private OrderService orderService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new ConfirmCheckoutServlet();
        try {
            Field serviceField = ConfirmCheckoutServlet.class.getDeclaredField("orderService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, orderService);
        } catch (NoSuchFieldException e) {}
    }

    // --- HELPER ---
    private void invokeDoPost() throws Exception {
        Method doPost = ConfirmCheckoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- CASE 1: THÀNH CÔNG ---
    @Test
    public void testDoPost_Success() throws Exception {
        setTestCaseInfo("CHECKOUT_01", "Đặt hàng thành công", 
                "1. Service return SUCCESS", "Addr: Hanoi", "Xóa Cart & Redirect Home");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(new ArrayList<CartBean>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.SUCCESS);

        invokeDoPost();

        verify(session).removeAttribute("cart");
        verify(response).sendRedirect(contains("/user/view-products?success=true"));
    }

    // --- CASE 2: CHƯA LOGIN ---
    @Test
    public void testDoPost_NotLoggedIn() throws Exception {
        setTestCaseInfo("CHECKOUT_02", "Chưa đăng nhập", 
                "1. Service return NOT_LOGGED_IN", "User=null", "Redirect Login");

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.NOT_LOGGED_IN);

        invokeDoPost();

        verify(response).sendRedirect(contains("Login.jsp"));
    }

    // --- CASE 3: THIẾU THÔNG TIN ---
    @Test
    public void testDoPost_MissingInfo() throws Exception {
        setTestCaseInfo("CHECKOUT_03", "Thiếu thông tin", 
                "1. Service return MISSING_INFO", "Addr=null", "Forward Checkout.jsp + Error");

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("Checkout.jsp"))).thenReturn(dispatcher);
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.MISSING_INFO);

        invokeDoPost();

        verify(request).setAttribute(eq("error"), contains("Vui lòng nhập đầy đủ"));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 4: GIỎ HÀNG RỖNG ---
    @Test
    public void testDoPost_EmptyCart() throws Exception {
        setTestCaseInfo("CHECKOUT_04", "Giỏ hàng rỗng", 
                "1. Service return EMPTY_CART", "Cart=[]", "Redirect view-cart.jsp");

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.EMPTY_CART);

        invokeDoPost();

        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- [MỚI] CASE 5: LỖI LƯU ĐƠN HÀNG (DB) ---
    @Test
    public void testDoPost_OrderFailed() throws Exception {
        setTestCaseInfo("CHECKOUT_05", "Lỗi lưu Order (DB)", 
                "1. Service return ORDER_FAILED", "DB Error", "Forward Checkout.jsp + Error");

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("Checkout.jsp"))).thenReturn(dispatcher);
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.ORDER_FAILED);

        invokeDoPost();

        verify(request).setAttribute(eq("error"), contains("thất bại"));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 6: LỖI LƯU CHI TIẾT ---
    @Test
    public void testDoPost_DetailFailed() throws Exception {
        setTestCaseInfo("CHECKOUT_06", "Lỗi lưu chi tiết", 
                "1. Service return DETAIL_FAILED", "DB Error", "Forward Checkout.jsp + Error");

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("Checkout.jsp"))).thenReturn(dispatcher);
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.DETAIL_FAILED);

        invokeDoPost();

        verify(request).setAttribute(eq("error"), contains("thất bại"));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 7: LỖI NGOẠI LỆ (EXCEPTION) ---
    @Test
    public void testDoPost_Exception() throws Exception {
        setTestCaseInfo("CHECKOUT_07", "Lỗi hệ thống", 
                "1. Service return EXCEPTION", "Crash", "Forward Checkout.jsp + Error");

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(contains("Checkout.jsp"))).thenReturn(dispatcher);
        when(orderService.placeOrder(any(), any(), any(), any())).thenReturn(OrderResult.EXCEPTION);

        invokeDoPost();

        verify(request).setAttribute(eq("error"), contains("thất bại"));
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ConfirmCheckoutServlet.xlsx"); }
}