package unit;

// === IMPORT LOGIC ===
import control.admin.ProductVariantsManagerServlet;
import entity.ProductVariants;
import service.ProductVariantService;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class ProductVariantsManagerServletTest {

    private ProductVariantsManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ProductVariantService variantService;

    // === CẤU HÌNH BÁO CÁO (Dùng biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData và các biến static cũ

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new ProductVariantsManagerServlet();

        // Inject Mock Service
        try {
            Field serviceField = ProductVariantsManagerServlet.class.getDeclaredField("variantService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, variantService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'variantService' trong Servlet");
        }
    }

    // ==========================================
    // 1. TEST LIST (Xem danh sách)
    // ==========================================
    @Test
    public void testDoGet_ListVariants() throws Exception {
        setTestCaseInfo("VAR_MGR_01", "Xem danh sách biến thể", 
                "1. Action='List'\n2. Service trả list", 
                "List size=1", "Forward View-product-variants.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-product-variants.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        List<ProductVariants> mockList = new ArrayList<>();
        mockList.add(new ProductVariants(1, 1, 1, 10, 100.0, "Ao", "S"));
        when(variantService.getAllVariants()).thenReturn(mockList);

        // 3. Run
        Method doGet = ProductVariantsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("list"), eq(mockList)); 
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST PREPARE ADD/EDIT
    // ==========================================
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("VAR_MGR_02", "Hiện form sửa biến thể", 
                "1. Action='AddOrEdit', ID=5\n2. Service trả Variant", 
                "ID=5", "Forward ProductVariantsManager.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("ProductVariantsManager.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        ProductVariants v = new ProductVariants(); v.setId(5);
        when(variantService.getVariantForEdit(5)).thenReturn(v);

        // 3. Run
        Method doGet = ProductVariantsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("VARIANT"), eq(v));
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 3. TEST SAVE/UPDATE
    // ==========================================
    @Test
    public void testDoPost_SaveNew() throws Exception {
        setTestCaseInfo("VAR_MGR_03", "Lưu biến thể mới", 
                "1. Action='SaveOrUpdate'\n2. Params đầy đủ", 
                "PID=1, Size=2, Stock=10, Price=200", "Call Service Save -> Redirect");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("id")).thenReturn(""); // Thêm mới
        when(request.getParameter("productId")).thenReturn("1");
        when(request.getParameter("sizeId")).thenReturn("2");
        when(request.getParameter("stock")).thenReturn("10");
        when(request.getParameter("price")).thenReturn("200.0");
        
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doPost = ProductVariantsManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 3. Verify
        verify(variantService).saveOrUpdateVariant(any(ProductVariants.class)); 
        verify(response).sendRedirect(contains("ProductVariantsManagerServlet?action=List"));
    }

    // ==========================================
    // 4. TEST DELETE
    // ==========================================
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("VAR_MGR_04", "Xóa biến thể", 
                "1. Action='Delete'\n2. ID=10", 
                "ID=10", "Call Service Delete -> Redirect");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doGet = ProductVariantsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 3. Verify
        verify(variantService).deleteVariant(10); 
        verify(response).sendRedirect(contains("ProductVariantsManagerServlet?action=List"));
    }

    // === CẤU HÌNH XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx thay vì CSV
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductVariantsManagerServlet.xlsx");
    }
}