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

    // === CẤU HÌNH BÁO CÁO ===
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

        // Inject Mock
        try {
            Field serviceField = AddToCartServlet.class.getDeclaredField("cartService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, cartService);
            
            Field daoField = AddToCartServlet.class.getDeclaredField("variantDao");
            daoField.setAccessible(true);
            daoField.set(servlet, variantDao);
        } catch (NoSuchFieldException e) {}
    }

    // --- CASE 1: CHƯA ĐĂNG NHẬP ---
    @Test
    public void testDoPost_NotLoggedIn() throws Exception {
        setTestCaseInfo("CART_SERV_01", "Servlet: Chưa đăng nhập", 
                "1. User session = null\n2. Call doPost", 
                "User: null", "Redirect: Login.jsp");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck"); 

        invokeDoPost();

        verify(session).setAttribute(eq("loginError"), anyString());
        verify(response).sendRedirect(contains("Login.jsp"));
    }

    // --- CASE 2: THÊM THÀNH CÔNG ---
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
        
        when(cartService.addToCart(any(), any(ProductVariants.class), anyInt())).thenReturn(updatedCart);

        invokeDoPost();

        verify(session).setAttribute(eq("cart"), eq(updatedCart));
        verify(session).setAttribute(contains("addCartSuccess"), anyString());
        verify(response).sendRedirect("/shop/home");
    }

    // --- CASE 3: SẢN PHẨM KHÔNG TỒN TẠI ---
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

        invokeDoPost();

        verify(session).setAttribute(eq("addCartError"), contains("không tồn tại"));
        verify(cartService, never()).addToCart(any(), any(ProductVariants.class), anyInt());
    }

    // --- CASE 4: SỐ LƯỢNG SAI FORMAT (CHỮ) ---
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

        // Expect service gọi với qty = 1 (do try-catch trong Servlet gán mặc định)
        when(cartService.addToCart(any(), any(ProductVariants.class), eq(1))).thenReturn(new ArrayList<>());

        invokeDoPost();

        verify(cartService).addToCart(any(), any(ProductVariants.class), eq(1)); 
        verify(session).setAttribute(contains("addCartSuccess"), anyString());
    }

    // --- CASE 5: SỐ LƯỢNG ÂM ---
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

        invokeDoPost();

        // Verify gọi với -5 (Service sẽ lo việc chặn hoặc xử lý sau)
        verify(cartService).addToCart(any(), any(ProductVariants.class), eq(-5));
    }

    // --- CASE 6: LỖI DB ---
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

        invokeDoPost();

        verify(session).setAttribute(eq("addCartError"), contains("Lỗi"));
        verify(response).sendRedirect("/home");
    }

    // --- [MỚI] CASE 7: KHÔNG CÓ HEADER REFERER (Test Fallback URL) ---
    @Test
    public void testDoPost_NoReferer() throws Exception {
        setTestCaseInfo("CART_SERV_07", "Không có Referer Header", 
                "Header Referer = null", "Ref=null", "Redirect default path");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        
        // Mock Referer là null
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/ShopDuck");
        
        // Mock input lỗi để chạy nhanh ra redirect (hoặc mock success cũng được)
        when(request.getParameter("variantId")).thenReturn("abc"); // ID lỗi

        invokeDoPost();

        // Verify: Phải redirect về trang mặc định "/ShopDuck/user/view-products"
        verify(response).sendRedirect("/ShopDuck/user/view-products");
    }

    // --- [MỚI] CASE 8: ID SẢN PHẨM SAI FORMAT (Test Catch) ---
    @Test
    public void testDoPost_InvalidVariantId() throws Exception {
        setTestCaseInfo("CART_SERV_08", "ID Sản phẩm lỗi format", 
                "ID='abc' -> Exception", "ID='abc'", "Vào catch -> Báo lỗi");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/home");
        
        when(request.getParameter("variantId")).thenReturn("abc"); // Gây lỗi ParseInt

        invokeDoPost();

        verify(session).setAttribute(eq("addCartError"), contains("Lỗi"));
        verify(response).sendRedirect("/home");
    }

    // --- [MỚI] CASE 9: GIỎ HÀNG NULL (Test khởi tạo) ---
    @Test
    public void testDoPost_CartSessionNull() throws Exception {
        setTestCaseInfo("CART_SERV_09", "Giỏ hàng trong Session Null", 
                "Session.getAttribute('cart') = null", "Cart=null", "Service được gọi với null");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new Users());
        when(request.getHeader("Referer")).thenReturn("/home");
        
        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("1");
        
        // Mock session trả về null cho cart
        when(session.getAttribute("cart")).thenReturn(null);
        
        ProductVariants p = new ProductVariants();
        when(variantDao.findById(1)).thenReturn(p);

        invokeDoPost();

        // Verify: Service vẫn được gọi (nó sẽ tự xử lý null), quan trọng là Servlet không chết
        verify(cartService).addToCart(eq(null), any(ProductVariants.class), anyInt());
    }

    // === HELPER ===
    private void invokeDoPost() throws Exception {
        Method doPost = AddToCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_AddToCartServlet.xlsx");
    }
}