package unit;

import control.user.SearchProductServlet;
import service.ProductService;
import entity.Products;
import entity.ProductVariants;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class SearchProductServletTest {

    private SearchProductServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ProductService productService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() {
        servlet = new SearchProductServlet();
        servlet.setProductService(productService); 
    }

    // ==========================================
    // 1. TEST CHỨC NĂNG TÌM KIẾM (Search Action)
    // ==========================================

    @Test
    public void testDoGet_Search_Normal() throws Exception {
        setTestCaseInfo("SEARCH_01", "Tìm kiếm thông thường", 
                "1. Action=null\n2. Key='ao'\n3. Service trả list", 
                "Key='ao'", "Forward searchResult.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("keyword")).thenReturn("ao");
        when(request.getRequestDispatcher(contains("searchResult.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        List<Products> results = new ArrayList<>();
        results.add(new Products(1, "Ao Thun", "Dep", 100.0, "img.jpg"));
        when(productService.searchProducts("ao")).thenReturn(results);

        // 3. Run
        servlet.doGet(request, response);

        // 4. Verify
        verify(request).setAttribute(eq("productList"), eq(results)); 
        verify(request).setAttribute(eq("keyword"), eq("ao"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoGet_Search_NoResult() throws Exception {
        setTestCaseInfo("SEARCH_02", "Tìm kiếm không thấy", 
                "Key='xyz' -> Service trả list rỗng", "List size=0", "Forward searchResult.jsp");

        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("keyword")).thenReturn("xyz");
        when(request.getRequestDispatcher(contains("searchResult.jsp"))).thenReturn(dispatcher);

        when(productService.searchProducts("xyz")).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("productList"), eq(Collections.emptyList()));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST CHỨC NĂNG CHI TIẾT (Detail Action)
    // ==========================================

    @Test
    public void testDoGet_Detail_Found() throws Exception {
        setTestCaseInfo("DETAIL_01", "Xem chi tiết thành công", 
                "1. Action='detail', ID=1\n2. Service tìm thấy SP", 
                "ID=1", "Forward info-products.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getRequestDispatcher(contains("info-products.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service Data
        Products p = new Products(1, "Ao X", "Mo ta", 200.0, "pic.jpg");
        List<ProductVariants> vars = new ArrayList<>();
        Map<Integer, String> sizes = Collections.emptyMap();

        when(productService.getProductDetails(1)).thenReturn(p);
        when(productService.getVariantsByProductId(1)).thenReturn(vars);
        when(productService.getSizeMap()).thenReturn(sizes);

        // 3. Run
        servlet.doGet(request, response);

        // 4. Verify
        verify(request).setAttribute("product", p);
        verify(request).setAttribute("variants", vars);
        verify(request).setAttribute("sizeMap", sizes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoGet_Detail_NotFound() throws Exception {
        setTestCaseInfo("DETAIL_02", "SP không tồn tại", 
                "1. Action='detail', ID=999\n2. Service return null", 
                "ID=999", "Redirect searchResult.jsp");

        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("999");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // Mock Service trả về null
        when(productService.getProductDetails(999)).thenReturn(null);

        servlet.doGet(request, response);

        // Verify: Redirect về trang searchResult (theo logic code của bạn)
        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    @Test
    public void testDoGet_Detail_InvalidID() throws Exception {
        setTestCaseInfo("DETAIL_03", "ID lỗi (chữ)", 
                "1. Action='detail', ID='abc'", 
                "ID='abc'", "Catch NumberFormat -> Redirect");

        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("abc"); 
        when(request.getContextPath()).thenReturn("/ShopDuck");

        servlet.doGet(request, response);

        // Verify: Catch block trong handleDetailAction sẽ redirect
        verify(response).sendRedirect(contains("searchResult.jsp"));
    }

    // ==========================================
    // 3. TEST NGOẠI LỆ TOÀN CỤC
    // ==========================================

    @Test
    public void testDoGet_GlobalException() throws Exception {
        setTestCaseInfo("SEARCH_EX_01", "Lỗi hệ thống bất ngờ", 
                "1. Service ném RuntimeException", 
                "Crash", "Redirect view-products");

        when(request.getParameter("action")).thenReturn("search");
        when(request.getContextPath()).thenReturn("/ShopDuck");
        
        // Giả lập lỗi
        when(productService.searchProducts(anyString())).thenThrow(new RuntimeException("DB Down"));

        servlet.doGet(request, response);

        // Verify: Catch block ở doGet to nhất sẽ redirect về trang chủ
        verify(response).sendRedirect(contains("view-products"));
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_SearchProductServlet.xlsx"); }
}