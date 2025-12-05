package unit;

import control.user.UpdateCartServlet;
import dao.ProductVariantDao; // [MỚI]
import entity.ProductVariants; // [MỚI]
import entity.CartBean;
import service.CartService;
import util.ExcelTestExporter;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    @Mock private ProductVariantDao variantDao; // [MỚI] Mock DAO

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new UpdateCartServlet();

        // Inject Mock Service
        Field serviceField = UpdateCartServlet.class.getDeclaredField("cartService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, cartService);

        // [MỚI] Inject Mock DAO (dùng setter vừa tạo)
        servlet.setVariantDao(variantDao);
    }

    // --- CASE 1: UPDATE THÀNH CÔNG (KHO ĐỦ HÀNG) ---
    @Test
    public void testDoPost_UpdateSuccess() throws Exception {
        setTestCaseInfo("UPDATE_01", "Cập nhật thành công (Kho đủ)", 
                "1. Input ID=1, Qty=5\n2. Mock Stock=100", 
                "ID=1, Qty=5", "Update=5, No Error");

        List<CartBean> mockCart = new ArrayList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("5");

        // [MỚI] Mock tồn kho đủ (100 > 5)
        ProductVariants mockVariant = new ProductVariants();
        mockVariant.setStock(100);
        when(variantDao.findById(1)).thenReturn(mockVariant);

        invokeDoPost();

        // Verify: Service phải được gọi với số lượng 5 (như khách nhập)
        verify(cartService).updateQuantity(eq(mockCart), eq(1), eq(5));
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 2: UPDATE QUÁ TỒN KHO ---
    @Test
    public void testDoPost_ExceedStock() throws Exception {
        setTestCaseInfo("UPDATE_02", "Cập nhật quá tồn kho", 
                "1. Input Qty=10\n2. Mock Stock=3", 
                "Qty=10, Stock=3", "Update=3, Set Error");

        List<CartBean> mockCart = new ArrayList<>();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(mockCart);
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("10"); // Khách muốn mua 10

        // [MỚI] Mock tồn kho thiếu (chỉ còn 3)
        ProductVariants mockVariant = new ProductVariants();
        mockVariant.setStock(3);
        when(variantDao.findById(1)).thenReturn(mockVariant);

        invokeDoPost();

        // Verify 1: Service chỉ được gọi với số 3 (bằng tồn kho)
        verify(cartService).updateQuantity(eq(mockCart), eq(1), eq(3));
        
        // Verify 2: Phải set thông báo lỗi vào session
        verify(session).setAttribute(eq("cartError"), contains("chỉ còn 3"));
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 3: GIỎ RỖNG (NULL) ---
    @Test
    public void testDoPost_CartNull() throws Exception {
        setTestCaseInfo("UPDATE_03", "Cập nhật khi giỏ Null", 
                "1. Cart = null", "Cart=null", "Redirect ngay");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(null); 
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoPost();

        verify(cartService, never()).updateQuantity(any(), anyInt(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // --- CASE 4: INPUT SAI FORMAT ---
    @Test
    public void testDoPost_InvalidInput() throws Exception {
        setTestCaseInfo("UPDATE_04", "Input lỗi (Chữ)", 
                "1. Qty = 'abc'", "Qty='abc'", "Catch lỗi, không gọi Service");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("cart")).thenReturn(new ArrayList<>());
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(request.getParameter("variantId")).thenReturn("1");
        when(request.getParameter("quantity")).thenReturn("abc"); 

        invokeDoPost();

        verify(cartService, never()).updateQuantity(any(), anyInt(), anyInt());
        verify(response).sendRedirect(contains("view-cart.jsp"));
    }

    // === HELPER ===
    private void invokeDoPost() throws Exception {
        Method doPost = UpdateCartServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
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


    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_UpdateCart.xlsx"); }
}