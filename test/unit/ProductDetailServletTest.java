package unit;

import control.user.ProductDetailServlet;
import entity.Products;
import entity.ProductVariants;
import service.ProductService;
import util.ExcelTestExporter;

// === IMPORT REFLECTION ===
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// === IMPORT SERVLET API ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// === IMPORT JUNIT & MOCKITO ===
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
    @Mock private ProductService mockService; // Mock Service thay vì dùng thật

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

        // --- KỸ THUẬT QUAN TRỌNG: INJECT MOCK SERVICE ---
        // Vì Servlet của bạn tạo Service trong hàm init(), ta dùng Reflection 
        // để "nhét" cái mockService vào biến private 'productService'
        // giúp ta không cần kết nối Database thật.
        Field serviceField = ProductDetailServlet.class.getDeclaredField("productService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, mockService);

        // Mock hành vi chung
        when(request.getContextPath()).thenReturn("/shopduck");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // === HÀM HỖ TRỢ: GỌI PROTECTED doGet ===
    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGetMethod = ProductDetailServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, req, resp);
    }

    // TEST 1: Xem chi tiết thành công (Happy Case)
    @Test
    public void testDoGet_Success() throws Exception {
        setTestCaseInfo("DETAIL_01", "Xem SP thành công", 
                "ID=1 tồn tại -> Forward JSP", "ID=1", "Forward info-products.jsp");

        // 1. Giả lập dữ liệu
        String validId = "1";
        Products mockProduct = new Products();
        mockProduct.setId(1);
        mockProduct.setName("Ao Test");

        List<ProductVariants> mockVariants = new ArrayList<>();
        Map<Integer, String> mockSizeMap = new HashMap<>();

        // 2. Mock hành vi
        when(request.getParameter("id")).thenReturn(validId);
        when(mockService.getProductDetails(1)).thenReturn(mockProduct);
        when(mockService.getVariantsByProductId(1)).thenReturn(mockVariants);
        when(mockService.getSizeMap()).thenReturn(mockSizeMap);

        // 3. Chạy
        invokeDoGet(request, response);

        // 4. Verify
        verify(request).setAttribute("product", mockProduct);
        verify(request).setAttribute("variants", mockVariants);
        verify(request).getRequestDispatcher(contains("info-products.jsp"));
        verify(dispatcher).forward(request, response);
    }

    // TEST 2: Sản phẩm không tồn tại (ID đúng format nhưng DB không có)
    @Test
    public void testDoGet_ProductNotFound() throws Exception {
        setTestCaseInfo("DETAIL_02", "SP không tồn tại", 
                "ID=999 -> Service trả về null", "ID=999", "Redirect searchResult.jsp");

        // 1. Giả lập
        when(request.getParameter("id")).thenReturn("999");
        when(mockService.getProductDetails(999)).thenReturn(null); // Không tìm thấy

        // 2. Chạy
        invokeDoGet(request, response);

        // 3. Verify
        verify(response).sendRedirect(contains("searchResult.jsp"));
        verify(dispatcher, never()).forward(request, response); // Đảm bảo không forward
    }

    // TEST 3: ID bị lỗi định dạng (Chữ thay vì số)
    @Test
    public void testDoGet_InvalidIdFormat() throws Exception {
        setTestCaseInfo("DETAIL_03", "ID lỗi format (chữ)", 
                "ID='abc' -> ParseInt lỗi", "ID='abc'", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("abc");

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    // TEST 4: ID rỗng hoặc Null
    @Test
    public void testDoGet_EmptyId() throws Exception {
        setTestCaseInfo("DETAIL_04", "ID rỗng/Null", 
                "ID='' -> Validate fail", "ID=''", "Redirect searchResult.jsp");

        when(request.getParameter("id")).thenReturn("");

        invokeDoGet(request, response);

        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    // === EXCEL REPORT ===
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductDetailServlet.xlsx");
    }
}