package unit;

import control.user.ProductDetailServlet;
import entity.Products;
import entity.ProductVariants;
import service.ProductService;
import util.ExcelTestExporter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailServletTest {

    private ProductDetailServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ProductService mockService;

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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        servlet = new ProductDetailServlet();

        // Inject Mock Service
        Field serviceField = ProductDetailServlet.class.getDeclaredField("productService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, mockService);

        // Mock hành vi chung
        when(request.getContextPath()).thenReturn("/shopduck");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGetMethod = ProductDetailServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, req, resp);
    }

    // TEST 1: Xem chi tiết thành công
    @Test
    public void testDoGet_Success() throws Exception {
        setTestCaseInfo("DETAIL_01", "Xem SP thành công", 
                "ID=1 tồn tại -> Forward JSP", "ID=1", "Forward info-products.jsp");

        String validId = "1";
        Products mockProduct = new Products();
        mockProduct.setId(1);
        mockProduct.setName("Ao Test");

        List<ProductVariants> mockVariants = new ArrayList<>();
        Map<Integer, String> mockSizeMap = new HashMap<>();

        when(request.getParameter("id")).thenReturn(validId);
        when(mockService.getProductDetails(1)).thenReturn(mockProduct);
        when(mockService.getVariantsByProductId(1)).thenReturn(mockVariants);
        when(mockService.getSizeMap()).thenReturn(mockSizeMap);

        invokeDoGet(request, response);

        verify(request).setAttribute("product", mockProduct);
        verify(request).setAttribute("variants", mockVariants);
        verify(request).getRequestDispatcher(contains("info-products.jsp"));
        verify(dispatcher).forward(request, response);
    }

    // TEST 2: Sản phẩm không tồn tại
    @Test
    public void testDoGet_ProductNotFound() throws Exception {
        setTestCaseInfo("DETAIL_02", "SP không tồn tại", 
                "ID=999 -> Service trả về null", "ID=999", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("999");
        when(mockService.getProductDetails(999)).thenReturn(null); 

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
        verify(dispatcher, never()).forward(request, response); 
    }

    // TEST 3: ID bị lỗi định dạng (NumberFormat)
    @Test
    public void testDoGet_InvalidIdFormat() throws Exception {
        setTestCaseInfo("DETAIL_03", "ID lỗi format (chữ)", 
                "ID='abc' -> ParseInt lỗi", "ID='abc'", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("abc");

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    // TEST 4: ID rỗng
    @Test
    public void testDoGet_EmptyId() throws Exception {
        setTestCaseInfo("DETAIL_04", "ID rỗng", 
                "ID='' -> Validate fail", "ID=''", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("");

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    // --- [MỚI] TEST 5: ID LÀ NULL ---
    @Test
    public void testDoGet_NullId() throws Exception {
        setTestCaseInfo("DETAIL_05", "ID là Null", 
                "Param id = null", "ID=null", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn(null);

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
        verify(mockService, never()).getProductDetails(anyInt()); // Đảm bảo không gọi service
    }

    // --- [MỚI] TEST 6: ID LÀ KHOẢNG TRẮNG ---
    @Test
    public void testDoGet_WhitespaceId() throws Exception {
        setTestCaseInfo("DETAIL_06", "ID toàn khoảng trắng", 
                "Param id = '   '", "ID='   '", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("   ");

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
        verify(mockService, never()).getProductDetails(anyInt());
    }

    // --- [MỚI] TEST 7: LỖI HỆ THỐNG (EXCEPTION) ---
    @Test
    public void testDoGet_SystemError() throws Exception {
        setTestCaseInfo("DETAIL_07", "Lỗi hệ thống bất ngờ", 
                "Service ném RuntimeException", "Crash", "Catch & Redirect an toàn");

        when(request.getParameter("id")).thenReturn("1");
        // Giả lập Service bị lỗi (ví dụ DB sập)
        doThrow(new RuntimeException("DB Connection Failed")).when(mockService).getProductDetails(1);

        invokeDoGet(request, response);

        // Verify: Servlet phải bắt lỗi và redirect chứ không để user thấy trang lỗi 500
        verify(response).sendRedirect(contains("searchResult.jsp"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductDetailServlet.xlsx");
    }
}