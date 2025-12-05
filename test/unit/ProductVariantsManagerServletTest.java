package unit;

import control.admin.ProductVariantsManagerServlet;
import dao.ProductVariantDao;
import entity.ProductVariants;
import service.ProductVariantService;
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
public class ProductVariantsManagerServletTest {

    private ProductVariantsManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ProductVariantService variantService;
    @Mock private ProductVariantDao variantDao;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new ProductVariantsManagerServlet();

        // Inject Mock Service
        try {
            Field serviceField = ProductVariantsManagerServlet.class.getDeclaredField("variantService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, variantService);
        } catch (NoSuchFieldException e) {}
        
        // Inject Mock DAO
        servlet.setVariantDao(variantDao);

        // [FIX QUAN TRỌNG 1] Luôn trả về list rỗng thay vì null
        // Để tránh NPE khi handleList được gọi trong catch block của processRequest
        when(variantService.getAllVariants()).thenReturn(new ArrayList<>());
    }

    // --- CASE 1: XEM DANH SÁCH (PHÂN TRANG) ---
    @Test
    public void testDoGet_ListVariants_Pagination() throws Exception {
        setTestCaseInfo("VAR_MGR_01", "Phân trang biến thể", 
                "Data=20, Page=2", "Page 2", "Cắt list 8->16");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("page")).thenReturn("2");
        when(request.getRequestDispatcher(contains("View-product-variants.jsp"))).thenReturn(dispatcher);

        // Giả lập 20 item
        List<ProductVariants> mockList = new ArrayList<>();
        for(int i=0; i<20; i++) mockList.add(new ProductVariants());
        
        // Override lại setup mặc định cho case này
        when(variantService.getAllVariants()).thenReturn(mockList);

        invokeDoGet();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("list"), captor.capture());
        
        if (captor.getValue().size() != 8) {
            throw new Exception("Lỗi phân trang: Size = " + captor.getValue().size());
        }
    }

    // --- CASE 2: HIỆN FORM SỬA ---
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("VAR_MGR_02", "Hiện form sửa biến thể", 
                "Action='AddOrEdit', ID=5", "ID=5", "Forward JSP");

        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("ProductVariantsManager.jsp"))).thenReturn(dispatcher);

        ProductVariants v = new ProductVariants(); v.setId(5);
        when(variantService.getVariantForEdit(5)).thenReturn(v);

        invokeDoGet();

        verify(request).setAttribute(eq("VARIANT"), eq(v));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 3: LƯU MỚI (KHÔNG TRÙNG) ---
    @Test
    public void testDoPost_SaveNew_NoExist() throws Exception {
        setTestCaseInfo("VAR_MGR_03", "Lưu biến thể mới (Không trùng)", 
                "ID=0, CheckExist=Null", "Insert", "Redirect List");

        setupPostParams("", "1", "2", "10", "200.0");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(variantDao.checkExist(1, 2)).thenReturn(null);
        
        // [FIX QUAN TRỌNG 2] Stub kết quả trả về là true
        // Nếu không stub, mockito trả về false (hoặc null nếu là Wrapper Boolean), gây sai luồng hoặc NPE
        when(variantService.saveOrUpdateVariant(any(ProductVariants.class))).thenReturn(true);

        invokeDoPost();

        ArgumentCaptor<ProductVariants> captor = ArgumentCaptor.forClass(ProductVariants.class);
        verify(variantService).saveOrUpdateVariant(captor.capture());
        
        if (captor.getValue().getId() != 0) throw new Exception("Lỗi: ID phải là 0");
        
        // Verify chuyển trang thành công
        verify(response).sendRedirect(contains("action=List"));
    }

    // --- CASE 4: LƯU TRÙNG -> CỘNG DỒN ---
    @Test
    public void testDoPost_SaveNew_Exist_Accumulate() throws Exception {
        setTestCaseInfo("VAR_MGR_04", "Lưu trùng -> Cộng dồn", 
                "ID=0, CheckExist=True(Stock=5)", "Input Stock=10", "Update ID cũ, Stock=15");

        setupPostParams("", "1", "2", "10", "200.0"); 
        when(request.getContextPath()).thenReturn("/ShopDuck");

        ProductVariants oldVariant = new ProductVariants();
        oldVariant.setId(99);
        oldVariant.setStock(5);
        when(variantDao.checkExist(1, 2)).thenReturn(oldVariant);

        // [FIX QUAN TRỌNG 2] Stub kết quả trả về là true
        when(variantService.saveOrUpdateVariant(any(ProductVariants.class))).thenReturn(true);

        invokeDoPost();

        ArgumentCaptor<ProductVariants> captor = ArgumentCaptor.forClass(ProductVariants.class);
        verify(variantService).saveOrUpdateVariant(captor.capture());
        
        ProductVariants saved = captor.getValue();
        if (saved.getId() != 99) throw new Exception("Lỗi: Không update ID cũ");
        if (saved.getStock() != 15) throw new Exception("Lỗi: Không cộng dồn stock");
    }

    // --- CASE 5: XÓA ---
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("VAR_MGR_05", "Xóa biến thể", 
                "Action='Delete', ID=10", "ID=10", "Redirect List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(variantService).deleteVariant(10); 
        verify(response).sendRedirect(contains("action=List"));
    }

    // --- CASE 6: LỖI HỆ THỐNG ---
    @Test
    public void testProcessRequest_Exception() throws Exception {
        setTestCaseInfo("VAR_MGR_06", "Lỗi hệ thống (Exception)", 
                "Service ném lỗi", "Error", "Catch & Handle List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        
        // Giả lập lỗi
        doThrow(new RuntimeException("DB Error")).when(variantService).deleteVariant(10);
        
        when(request.getRequestDispatcher(contains("View-product-variants.jsp"))).thenReturn(dispatcher);

        invokeDoGet(); // Exception sẽ được catch trong Servlet và gọi handleList

        // handleList sẽ gọi variantService.getAllVariants() -> đã được stub ở setUp() trả về list rỗng
        // nên sẽ không bị NPE nữa.

        verify(request).setAttribute(contains("error"), contains("DB Error"));
        verify(dispatcher).forward(request, response);
    }

    // === HELPER ===
    private void invokeDoGet() throws Exception {
        Method doGet = ProductVariantsManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void invokeDoPost() throws Exception {
        Method doPost = ProductVariantsManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    private void setupPostParams(String id, String pid, String sid, String stock, String price) {
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("id")).thenReturn(id);
        when(request.getParameter("productId")).thenReturn(pid);
        when(request.getParameter("sizeId")).thenReturn(sid);
        when(request.getParameter("stock")).thenReturn(stock);
        when(request.getParameter("price")).thenReturn(price);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ProductVariantsManagerServlet.xlsx"); }
}