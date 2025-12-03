package unit;

// === IMPORT LOGIC CHÍNH ===
import control.user.RemoveFromCartServlet;
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
public class RemoveFromCartServletTest {

    private RemoveFromCartServlet servlet;

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
        servlet = new RemoveFromCartServlet();

        // Inject Mock Service vào Servlet bằng Reflection
        try {
            Field serviceField = RemoveFromCartServlet.class.getDeclaredField("cartService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, cartService);
        } catch (NoSuchFieldException e) {
            System.err.println("Không tìm thấy biến cartService trong Servlet");
        }
    }

    @Test
    public void testDoGet_RemoveSuccess() throws Exception {
        setTestCaseInfo("REMOVE_01", "Xóa thành công", 
                "1. Session có Cart\n2. Index=0\n3. Call service remove", 
                "Index=0", "Update Cart & Redirect");

        // 1. Mock Session & Cart
        List<CartBean> mockCart = new ArrayList<>(); // Giả lập giỏ có đồ
        mockCart.add(new CartBean()); 
        
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck"); // Mock context path cho redirect

        // 2. Mock Input
        when(request.getParameter("index")).thenReturn("0");

        // 3. Run doGet
        Method doGet = RemoveFromCartServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(cartService).removeFromCart(eq(mockCart), eq(0)); // Phải gọi hàm xóa
        verify(session).setAttribute(eq("cart"), eq(mockCart)); // Phải cập nhật session
        verify(response).sendRedirect(contains("view-cart.jsp")); // Phải chuyển trang
    }

    @Test
    public void testDoGet_CartNull() throws Exception {
        setTestCaseInfo("REMOVE_02", "Xóa khi giỏ rỗng (Null)", 
                "1. Session cart = null\n2. Index=0", 
                "Cart=null", "Redirect ngay, ko gọi Service");

        // 1. Mock Session returns NULL
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doGet = RemoveFromCartServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 3. Verify
        verify(cartService, never()).removeFromCart(any(), anyInt()); // Không được gọi service
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    @Test
    public void testDoGet_InvalidIndex_String() throws Exception {
        setTestCaseInfo("REMOVE_03", "Index lỗi (Chuỗi)", 
                "1. Index='abc'\n2. Catch Exception", 
                "Index='abc'", "Ko gọi Service, Redirect");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // Mock Input "Rác"
        when(request.getParameter("index")).thenReturn("abc");

        // Run
        Method doGet = RemoveFromCartServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify: Servlet sẽ bắt lỗi NumberFormatException, nên service không được gọi
        verify(cartService, never()).removeFromCart(any(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    @Test
    public void testDoGet_NullIndex() throws Exception {
        setTestCaseInfo("REMOVE_04", "Index Null", 
                "1. Param index = null", 
                "Index=null", "Ko gọi Service, Redirect");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // Mock Input Null
        when(request.getParameter("index")).thenReturn(null);

        // Run
        Method doGet = RemoveFromCartServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(cartService, never()).removeFromCart(any(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_RemoveFromCart.xlsx");
    }
}