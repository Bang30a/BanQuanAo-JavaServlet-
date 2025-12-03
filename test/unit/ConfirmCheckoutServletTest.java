package unit;

// === IMPORT LOGIC ===
import control.user.ConfirmCheckoutServlet;
import service.OrderService;
import service.OrderResult;
import entity.CartBean;
import entity.Users;
import util.ExcelTestExporter; // <--- ĐÃ IMPORT

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

    // === CẤU HÌNH BÁO CÁO (Dùng biến cục bộ để hứng dữ liệu) ===
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
        servlet = new ConfirmCheckoutServlet();
        
        // Inject Mock Service
        try {
            Field serviceField = ConfirmCheckoutServlet.class.getDeclaredField("orderService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, orderService);
        } catch (NoSuchFieldException e) {}
    }

    @Test
    public void testDoPost_Success() throws Exception {
        setTestCaseInfo("CHECKOUT_01", "Đặt hàng thành công", 
                "1. User, Cart OK\n2. Service return SUCCESS", 
                "Addr: Hanoi, Phone: 0909", "Xóa Cart & Redirect Home");

        // 1. Mock Session Data
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(new ArrayList<CartBean>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Mock Input
        when(request.getParameter("address")).thenReturn("Hanoi");
        when(request.getParameter("phone")).thenReturn("0909");

        // 3. Mock Service: Trả về SUCCESS
        when(orderService.placeOrder(any(), any(), eq("Hanoi"), eq("0909")))
                .thenReturn(OrderResult.SUCCESS);

        // 4. Run
        Method doPost = ConfirmCheckoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 5. Verify
        verify(session).removeAttribute("cart"); // Quan trọng: Phải xóa giỏ
        verify(response).sendRedirect(contains("/user/view-products"));
    }

    @Test
    public void testDoPost_NotLoggedIn() throws Exception {
        setTestCaseInfo("CHECKOUT_02", "Chưa đăng nhập", 
                "1. User session = null\n2. Service return NOT_LOGGED_IN", 
                "User=null", "Redirect Login");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(orderService.placeOrder(any(), any(), any(), any()))
                .thenReturn(OrderResult.NOT_LOGGED_IN);

        Method doPost = ConfirmCheckoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(response).sendRedirect(contains("Login.jsp"));
    }

    @Test
    public void testDoPost_MissingInfo() throws Exception {
        setTestCaseInfo("CHECKOUT_03", "Thiếu thông tin", 
                "1. Address/Phone null\n2. Service return MISSING_INFO", 
                "Addr=null", "Forward lại Checkout.jsp + Error");

        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        when(request.getRequestDispatcher(contains("Checkout.jsp"))).thenReturn(dispatcher);

        when(orderService.placeOrder(any(), any(), any(), any()))
                .thenReturn(OrderResult.MISSING_INFO);

        Method doPost = ConfirmCheckoutServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(request).setAttribute(eq("error"), anyString()); // Báo lỗi
        verify(dispatcher).forward(request, response); // Ở lại trang cũ
    }

    // === CẤU HÌNH XUẤT EXCEL (DÙNG CLASS TIỆN ÍCH) ===
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
        // Xuất ra file .xlsx xịn sò
        ExcelTestExporter.exportToExcel("KetQuaTest_ConfirmCheckoutServlet.xlsx");
    }
}