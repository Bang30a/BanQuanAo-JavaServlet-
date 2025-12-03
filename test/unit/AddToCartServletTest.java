package unit;

// === IMPORT LOGIC CHÍNH ===
import control.user.AddToCartServlet;
import entity.CartBean;
import entity.ProductVariants;
import entity.Users;
import dao.ProductVariantDao;
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
import static org.mockito.ArgumentMatchers.anyString;
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

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class AddToCartServletTest {

    private AddToCartServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private CartService cartService;
    @Mock private ProductVariantDao variantDao;

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
        servlet = new AddToCartServlet();

        // Inject Mock CartService
        try {
            Field serviceField = AddToCartServlet.class.getDeclaredField("cartService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, cartService);
        } catch (NoSuchFieldException e) {}

        // Inject Mock ProductVariantDao
        try {
            Field daoField = AddToCartServlet.class.getDeclaredField("variantDao");
            daoField.setAccessible(true);
            daoField.set(servlet, variantDao);
        } catch (NoSuchFieldException e) {}
    }

    @Test
    public void testDoPost_NotLoggedIn() throws Exception {
        setTestCaseInfo("CART_SERV_01", "Servlet: Chưa đăng nhập", 
                "1. User session = null\n2. Call doPost", 
                "User: null", "Redirect: Login.jsp");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck"); 

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("loginError"), anyString());
        verify(response).sendRedirect(contains("Login.jsp"));
    }

    @Test
    public void testDoPost_Success() throws Exception {
        setTestCaseInfo("CART_SERV_02", "Servlet: Thêm thành công", 
                "1. User OK\n2. Mock DAO tìm thấy SP\n3. Service add OK", 
                "PID: 1, Qty: 2", "Redirect: Referer");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getHeader("Referer")).thenReturn("/shop/home");

        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("2");

        ProductVariants p = new ProductVariants(); 
        p.setProductName("Ao Test");
        when(variantDao.findById(1)).thenReturn(p);
        
        List<CartBean> updatedCart = new ArrayList<>();
        updatedCart.add(new CartBean(p, 2));
        
        // [FIXED] Dùng any(ProductVariants.class) để tránh lỗi Ambiguous
        when(cartService.addToCart(any(), any(ProductVariants.class), anyInt())).thenReturn(updatedCart);

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("cart"), eq(updatedCart));
        verify(session).setAttribute(contains("addCartSuccess"), anyString());
        verify(response).sendRedirect("/shop/home");
    }

    @Test
    public void testDoPost_ProductNotFound() throws Exception {
        setTestCaseInfo("CART_SERV_03", "Servlet: SP không tồn tại", 
                "1. User OK\n2. Mock DAO trả về null", 
                "PID: 999", "Báo lỗi addCartError");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/shop/home");
        
        when(request.getParameter("variantId")).thenReturn("999");
        when(request.getParameter("quantity")).thenReturn("1");

        when(variantDao.findById(999)).thenReturn(null);

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("addCartError"), contains("không tồn tại"));
        verify(cartService, never()).addToCart(any(), any(ProductVariants.class), anyInt());
    }

    @Test
    public void testDoPost_QuantityIsString() throws Exception {
        setTestCaseInfo("CART_SERV_04", "Servlet: Số lượng là chữ", 
                "Input quantity='abc'", "Qty='abc'", "Mặc định thêm 1");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/home");
        
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("abc"); // Input Rác

        ProductVariants p = new ProductVariants(); p.setId(1);
        when(variantDao.findById(1)).thenReturn(p);

        // Expect service gọi với qty = 1 (do try-catch mặc định)
        when(cartService.addToCart(any(), any(ProductVariants.class), eq(1))).thenReturn(new ArrayList<>());

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(cartService).addToCart(any(), any(ProductVariants.class), eq(1)); 
        verify(session).setAttribute(contains("addCartSuccess"), anyString());
    }

    @Test
    public void testDoPost_NegativeQuantity() throws Exception {
        setTestCaseInfo("CART_SERV_05", "Servlet: Số lượng âm", 
                "Input quantity='-5'", "Qty='-5'", "Vẫn gọi service");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/home");
        
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("-5");

        ProductVariants p = new ProductVariants(); p.setId(1);
        when(variantDao.findById(1)).thenReturn(p);

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // Verify gọi với -5
        verify(cartService).addToCart(any(), any(ProductVariants.class), eq(-5));
    }

    @Test
    public void testDoPost_DatabaseCrash() throws Exception {
        setTestCaseInfo("CART_SERV_06", "Servlet: Lỗi Database", 
                "DAO ném Exception", "DB Error", "Redirect & Báo lỗi");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/home");
        
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("1");

        when(variantDao.findById(1)).thenThrow(new RuntimeException("DB Down"));

        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        verify(session).setAttribute(eq("addCartError"), contains("Lỗi"));
        verify(response).sendRedirect("/home");
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
        ExcelTestExporter.exportToExcel("KetQuaTest_AddToCartServlet.xlsx");
    }
}