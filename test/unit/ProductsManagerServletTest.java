package unit;

// === IMPORT LOGIC ===
import control.admin.ProductsManagerServlet;
import entity.Products;
import service.ProductService;
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
public class ProductsManagerServletTest {

    private ProductsManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ProductService productService;

    // === CẤU HÌNH BÁO CÁO (Dùng biến instance) ===
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
        servlet = new ProductsManagerServlet();

        // Inject Mock Service vào Servlet bằng Reflection
        try {
            Field serviceField = ProductsManagerServlet.class.getDeclaredField("productService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, productService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'productService' trong Servlet");
        }
    }

    // ==========================================
    // 1. TEST LIST (Xem danh sách SP)
    // ==========================================
    @Test
    public void testDoGet_ListProducts() throws Exception {
        setTestCaseInfo("PROD_MGR_01", "Xem danh sách SP", 
                "1. Action='List'\n2. Service trả list", 
                "List size=1", "Forward View-products.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        List<Products> mockList = new ArrayList<>();
        mockList.add(new Products(1, "Ao Test", "Desc", 100.0, "img.jpg"));
        when(productService.getAllProducts()).thenReturn(mockList);

        // 3. Run (Reflection calling doGet)
        Method doGet = ProductsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("PRODUCTS"), eq(mockList)); // Check đúng tên attribute
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST PREPARE ADD/EDIT (Hiện form)
    // ==========================================
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("PROD_MGR_02", "Hiện form sửa SP", 
                "1. Action='AddOrEdit', ID=5\n2. Service trả SP", 
                "ID=5", "Forward ProductsManager.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("ProductsManager.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        Products p = new Products(); p.setId(5);
        when(productService.getProductForEdit(5)).thenReturn(p);

        // 3. Run
        Method doGet = ProductsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("PRODUCTS"), eq(p));
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 3. TEST SAVE/UPDATE (Lưu)
    // ==========================================
    @Test
    public void testDoPost_SaveNew() throws Exception {
        setTestCaseInfo("PROD_MGR_03", "Lưu Sản phẩm mới", 
                "1. Action='SaveOrUpdate'\n2. Params đầy đủ", 
                "Name='Ao Moi', Price=200", "Call Service Save -> Redirect List");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("id")).thenReturn(""); // ID rỗng = Thêm mới
        when(request.getParameter("name")).thenReturn("Ao Moi");
        when(request.getParameter("description")).thenReturn("Mo ta");
        when(request.getParameter("price")).thenReturn("200000");
        when(request.getParameter("image")).thenReturn("img.png");
        
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doPost = ProductsManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 3. Verify
        verify(productService).saveOrUpdateProduct(any(Products.class)); // Kiểm tra service được gọi
        verify(response).sendRedirect(contains("/admin/ProductsManagerServlet?action=List"));
    }

    // ==========================================
    // 4. TEST DELETE (Xóa)
    // ==========================================
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("PROD_MGR_04", "Xóa Sản phẩm", 
                "1. Action='Delete'\n2. ID=10", 
                "ID=10", "Call Service Delete -> Redirect List");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doGet = ProductsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 3. Verify
        verify(productService).deleteProduct(10); // Phải gọi hàm xóa với ID 10
        verify(response).sendRedirect(contains("/admin/ProductsManagerServlet?action=List"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductsManagerServlet.xlsx");
    }
}