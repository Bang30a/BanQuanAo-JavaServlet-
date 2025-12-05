package unit;

import control.admin.ProductsManagerServlet;
import entity.Products;
import service.ProductService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentCaptor;

@RunWith(MockitoJUnitRunner.class)
public class ProductsManagerServletTest {

    private ProductsManagerServlet servlet;

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
    public void setUp() throws Exception {
        servlet = new ProductsManagerServlet();
        try {
            Field serviceField = ProductsManagerServlet.class.getDeclaredField("productService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, productService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'productService' trong Servlet");
        }
    }

    // --- CASE 1: XEM DANH SÁCH (Mặc định) ---
    @Test
    public void testDoGet_ListProducts() throws Exception {
        setTestCaseInfo("PROD_MGR_01", "Xem danh sách SP", 
                "1. Action='List'\n2. Service trả list", 
                "List size=1", "Forward View-products.jsp");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        List<Products> mockList = new ArrayList<>();
        mockList.add(new Products(1, "Ao Test", "Desc", 100.0, "img.jpg"));
        when(productService.getAllProducts()).thenReturn(mockList);

        invokeDoGet();

        verify(request).setAttribute(eq("PRODUCTS"), eq(mockList));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 2: TÌM KIẾM SẢN PHẨM ---
    @Test
    public void testDoGet_SearchProducts() throws Exception {
        setTestCaseInfo("PROD_MGR_02", "Tìm kiếm Sản phẩm", 
                "Action='List', Keyword='Ao'", "Key='Ao'", "Gọi searchProducts, Forward");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("keyword")).thenReturn("Ao"); // Có keyword
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        List<Products> searchResults = new ArrayList<>();
        searchResults.add(new Products(1, "Ao Thun", "", 100.0, ""));
        
        // Mock Service: Phải gọi search chứ không phải getAll
        when(productService.searchProducts("Ao")).thenReturn(searchResults);

        invokeDoGet();

        verify(productService).searchProducts("Ao"); // Verify service call
        verify(request).setAttribute(eq("PRODUCTS"), eq(searchResults));
        verify(request).setAttribute(eq("keyword"), eq("Ao")); // Verify keyword được giữ lại
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 3: PHÂN TRANG (Pagination) ---
    @Test
    public void testDoGet_Pagination() throws Exception {
        setTestCaseInfo("PROD_MGR_03", "Phân trang (Page 2)", 
                "Data=20 items, Page=2", "Page=2", "Cắt list từ index 8->16");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("page")).thenReturn("2"); // Xin trang 2
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        // Giả lập 20 sản phẩm
        List<Products> hugeList = new ArrayList<>();
        for(int i=0; i<20; i++) hugeList.add(new Products());
        
        when(productService.getAllProducts()).thenReturn(hugeList);

        invokeDoGet();

        // Verify logic cắt list: Trang 2 (size 8) -> Lấy từ index 8 đến 16
        // Dùng ArgumentCaptor để bắt lấy cái list được gửi đi
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("PRODUCTS"), captor.capture());
        
        List capturedList = captor.getValue();
        if (capturedList.size() != 8) {
             throw new Exception("Lỗi phân trang: Size thực tế là " + capturedList.size());
        }
        verify(request).setAttribute(eq("currentPage"), eq(2));
    }

    // --- CASE 4: HIỆN FORM SỬA ---
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("PROD_MGR_04", "Hiện form sửa SP", 
                "Action='AddOrEdit', ID=5", "ID=5", "Forward ProductsManager.jsp");

        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("ProductsManager.jsp"))).thenReturn(dispatcher);

        Products p = new Products(); p.setId(5);
        when(productService.getProductForEdit(5)).thenReturn(p);

        invokeDoGet();

        verify(request).setAttribute(eq("PRODUCTS"), eq(p));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 5: LƯU THÀNH CÔNG ---
    @Test
    public void testDoPost_SaveNew() throws Exception {
        setTestCaseInfo("PROD_MGR_05", "Lưu Sản phẩm mới", 
                "Action='SaveOrUpdate'", "Full Data", "Call Service Save -> Redirect List");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("id")).thenReturn(""); 
        when(request.getParameter("name")).thenReturn("Ao Moi");
        when(request.getParameter("price")).thenReturn("200000");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoPost();

        verify(productService).saveOrUpdateProduct(any(Products.class));
        verify(response).sendRedirect(contains("/admin/ProductsManagerServlet?action=List"));
    }

    // --- CASE 6: XÓA ---
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("PROD_MGR_06", "Xóa Sản phẩm", 
                "Action='Delete', ID=10", "ID=10", "Call Service Delete -> Redirect List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(productService).deleteProduct(10);
        verify(response).sendRedirect(contains("/admin/ProductsManagerServlet?action=List"));
    }

    // --- [MỚI] CASE 7: XÓA ID RÁC ---
    @Test
    public void testDoGet_DeleteInvalidId() throws Exception {
        setTestCaseInfo("PROD_MGR_07", "Xóa ID rác (Chữ)", 
                "ID='abc'", "ID='abc'", "Ko gọi service -> Redirect");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("abc");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        // Verify: Không gọi deleteProduct với bất kỳ số nào
        verify(productService, never()).deleteProduct(anyInt());
        verify(response).sendRedirect(contains("action=List"));
    }

    // --- [MỚI] CASE 8: LỖI HỆ THỐNG ---
    @Test
    public void testProcessRequest_SystemError() throws Exception {
        setTestCaseInfo("PROD_MGR_08", "Lỗi hệ thống (Exception)", 
                "Service ném lỗi", "DB Error", "Set Error Attribute & Reload List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        // Giả lập lỗi
        doThrow(new RuntimeException("DB Down")).when(productService).deleteProduct(10);
        
        // Khi lỗi xảy ra, catch block sẽ gọi handleList -> cần mock requestDispatcher cho List
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        verify(request).setAttribute(contains("error"), contains("DB Down"));
        verify(productService).getAllProducts(); // Phải load lại list
    }

    // --- [MỚI] CASE 9: ACTION NULL ---
    @Test
    public void testDoGet_DefaultAction() throws Exception {
        setTestCaseInfo("PROD_MGR_09", "Action Null", 
                "Action=null", "Null", "Mặc định là List");

        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher(contains("View-products.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        verify(productService).getAllProducts();
    }

    // === HELPER METHODS ===
    private void invokeDoGet() throws Exception {
        Method doGet = ProductsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void invokeDoPost() throws Exception {
        Method doPost = ProductsManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ProductsManagerServlet.xlsx"); }
}