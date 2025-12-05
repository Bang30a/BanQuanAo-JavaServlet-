package unit;

// === IMPORT LOGIC CHÍNH ===
import control.user.RemoveFromCartServlet;
import entity.CartBean;
import service.CartService;
import util.ExcelTestExporter;

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

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new RemoveFromCartServlet();
        // Inject Mock Service
        try {
            Field serviceField = RemoveFromCartServlet.class.getDeclaredField("cartService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, cartService);
        } catch (NoSuchFieldException e) {
            System.err.println("Không tìm thấy biến cartService trong Servlet");
        }
    }

    // --- CASE 1: XÓA THÀNH CÔNG ---
    @Test
    public void testDoGet_RemoveSuccess() throws Exception {
        setTestCaseInfo("REMOVE_01", "Xóa thành công", 
                "1. Session có Cart\n2. Index=0\n3. Call service remove", 
                "Index=0", "Update Cart & Redirect");

        List<CartBean> mockCart = new ArrayList<>();
        mockCart.add(new CartBean()); 
        
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("index")).thenReturn("0");

        invokeDoGet();

        verify(cartService).removeFromCart(eq(mockCart), eq(0)); 
        verify(session).setAttribute(eq("cart"), eq(mockCart)); 
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 2: GIỎ HÀNG NULL ---
    @Test
    public void testDoGet_CartNull() throws Exception {
        setTestCaseInfo("REMOVE_02", "Xóa khi giỏ rỗng (Null)", 
                "1. Session cart = null\n2. Index=0", 
                "Cart=null", "Redirect ngay, ko gọi Service");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(cartService, never()).removeFromCart(any(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 3: INDEX SAI ĐỊNH DẠNG (NumberFormat) ---
    @Test
    public void testDoGet_InvalidIndex_String() throws Exception {
        setTestCaseInfo("REMOVE_03", "Index lỗi (Chuỗi)", 
                "1. Index='abc'\n2. Catch Exception", 
                "Index='abc'", "Ko gọi Service, Redirect");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("index")).thenReturn("abc");

        invokeDoGet();

        verify(cartService, never()).removeFromCart(any(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 4: INDEX NULL ---
    @Test
    public void testDoGet_NullIndex() throws Exception {
        setTestCaseInfo("REMOVE_04", "Index Null", 
                "1. Param index = null", 
                "Index=null", "Ko gọi Service, Redirect");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("index")).thenReturn(null);

        invokeDoGet();

        verify(cartService, never()).removeFromCart(any(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- [MỚI] CASE 5: LỖI SERVICE (Exception General) ---
    @Test
    public void testDoGet_ServiceException() throws Exception {
        setTestCaseInfo("REMOVE_05", "Lỗi hệ thống (Exception)", 
                "1. Service ném RuntimeException\n2. Servlet catch", 
                "Index=1, Error", "Catch & Redirect an toàn");

        List<CartBean> mockCart = new ArrayList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        
        when(request.getParameter("index")).thenReturn("1");

        // Giả lập Service bị lỗi (ví dụ index vượt quá size thật của list trong logic service)
        doThrow(new RuntimeException("Index Out Of Bounds")).when(cartService).removeFromCart(anyList(), anyInt());

        invokeDoGet();

        // Verify: Servlet phải bắt lỗi và vẫn redirect về trang view-cart chứ không chết trang
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // === HELPER ===
    private void invokeDoGet() throws Exception {
        Method doGet = RemoveFromCartServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    // === EXCEL EXPORT ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_RemoveFromCart.xlsx");
    }
}