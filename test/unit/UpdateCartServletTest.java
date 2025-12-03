package unit;

// === IMPORT LOGIC CHÍNH ===
import control.user.UpdateCartServlet;
import entity.CartBean;
import service.CartService;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCartServletTest {

    private UpdateCartServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private CartService cartService;

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new UpdateCartServlet();

        // Inject Mock Service
        try {
            Field serviceField = UpdateCartServlet.class.getDeclaredField("cartService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, cartService);
        } catch (NoSuchFieldException e) {
            System.err.println("CẢNH BÁO: Chưa khai báo cartService trong Servlet");
        }
    }

    @Test
    public void testDoPost_UpdateSuccess() throws Exception {
        setTestCaseInfo("UPDATE_01", "Cập nhật thành công", 
                "1. Session có Cart\n2. Input ID=1, Qty=5", 
                "ID=1, Qty=5", "Call Service update -> Redirect");

        // 1. Mock Session & Cart
        List<CartBean> mockCart = new ArrayList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Mock Input
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("5");

        // 3. Run doPost
        Method doPost = UpdateCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 4. Verify
        verify(cartService).updateQuantity(eq(mockCart), eq(1), eq(5)); // Quan trọng: Check xem service có được gọi đúng ko
        verify(session).setAttribute(eq("cart"), eq(mockCart));
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    @Test
    public void testDoPost_CartNull() throws Exception {
        setTestCaseInfo("UPDATE_02", "Cập nhật khi giỏ Null", 
                "1. Cart = null", 
                "Cart=null", "Redirect ngay, Service không chạy");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(null); // Giỏ rỗng
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // Run
        Method doPost = UpdateCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify
        verify(cartService, never()).updateQuantity(any(), anyInt(), anyInt()); // Đảm bảo không gọi service
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    @Test
    public void testDoPost_InvalidInput() throws Exception {
        setTestCaseInfo("UPDATE_03", "Input lỗi (Chữ)", 
                "1. Qty = 'abc'", 
                "Qty='abc'", "Bắt lỗi, không gọi Service");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // Mock Input Rác
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("abc"); // Lỗi ở đây

        // Run
        Method doPost = UpdateCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify
        verify(cartService, never()).updateQuantity(any(), anyInt(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }
    
    @Test
    public void testDoPost_ZeroQuantity() throws Exception {
        setTestCaseInfo("UPDATE_04", "Cập nhật về 0", 
                "1. Input Qty = 0", 
                "Qty=0", "Service được gọi với 0");

        List<CartBean> mockCart = new ArrayList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("0"); // User nhập 0

        // Run
        Method doPost = UpdateCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify: Service phải được gọi với số 0 (Logic xóa do Service lo)
        verify(cartService).updateQuantity(eq(mockCart), eq(1), eq(0));
    }

    // === XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_UpdateCart.xlsx");
    }
}