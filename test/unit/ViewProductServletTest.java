package unit;

import control.user.ViewProductServlet;
import entity.Products;
import service.ProductService;
import util.ExcelTestExporter;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

public class ViewProductServletTest {

    private ViewProductServlet servlet;

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
        servlet = new ViewProductServlet();

        // 1. Inject Mock Service (Dùng Setter mới thêm)
        servlet.setProductService(mockService);

        // 2. Mock hành vi chung
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // === HÀM HỖ TRỢ: TẠO LIST GIẢ ===
    private List<Products> createMockList(int size) {
        List<Products> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Products());
        }
        return list;
    }

    // TEST 1: Hiển thị bình thường (List ít hơn giới hạn)
    @Test
    public void testDoGet_NormalFlow() throws Exception {
        setTestCaseInfo("VIEW_01", "Hiển thị danh sách (ít)", 
                "List All=3, Áo=2, Quần=2", "Data < Limit", "Forward đủ số lượng");

        // Giả lập DB trả về ít dữ liệu
        when(mockService.getAllProducts()).thenReturn(createMockList(3));
        when(mockService.searchProducts("áo")).thenReturn(createMockList(2));
        when(mockService.searchProducts("quần")).thenReturn(createMockList(2));

        servlet.doGet(request, response);

        // Verify: Đảm bảo forward đúng chỗ
        verify(request).getRequestDispatcher(contains("View-products.jsp"));
        verify(dispatcher).forward(request, response);
        
        // Verify: Dữ liệu được set
        verify(request).setAttribute(eq("productList"), anyList());
        verify(request).setAttribute(eq("shirtList"), anyList());
        verify(request).setAttribute(eq("pantsList"), anyList());
    }

    // TEST 2: Kiểm tra Logic Cắt danh sách (SubList)
    // Logic: All > 8 cắt còn 8. Áo/Quần > 4 cắt còn 4.
    @Test
    public void testDoGet_LimitListSize() throws Exception {
        setTestCaseInfo("VIEW_02", "Giới hạn số lượng hiển thị", 
                "DB trả về nhiều (20 item)", "Input=20 items", "All=8, Shirt=4, Pants=4");

        // 1. Giả lập DB trả về 20 sản phẩm cho tất cả các hàm
        List<Products> hugeList = createMockList(20);
        when(mockService.getAllProducts()).thenReturn(hugeList);
        when(mockService.searchProducts("áo")).thenReturn(hugeList);
        when(mockService.searchProducts("quần")).thenReturn(hugeList);

        // 2. Chạy
        servlet.doGet(request, response);

        // 3. Bắt dữ liệu gửi đi bằng ArgumentCaptor
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        
        // Verify productList (Limit 8)
        verify(request).setAttribute(eq("productList"), listCaptor.capture());
        List caughtProductList = listCaptor.getValue();
        if (caughtProductList.size() != 8) {
            throw new Exception("Lỗi: Danh sách chính không cắt đúng 8. Size: " + caughtProductList.size());
        }

        // Verify shirtList (Limit 4)
        // Cần reset captor hoặc dùng getAllValues nếu verify gọi nhiều lần, 
        // ở đây để đơn giản ta verify riêng lẻ từng cái và dùng captor mới
        ArgumentCaptor<List> shirtCaptor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("shirtList"), shirtCaptor.capture());
        if (shirtCaptor.getValue().size() != 4) {
            throw new Exception("Lỗi: Danh sách Áo không cắt đúng 4. Size: " + shirtCaptor.getValue().size());
        }

        // Verify pantsList (Limit 4)
        ArgumentCaptor<List> pantsCaptor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("pantsList"), pantsCaptor.capture());
        if (pantsCaptor.getValue().size() != 4) {
            throw new Exception("Lỗi: Danh sách Quần không cắt đúng 4. Size: " + pantsCaptor.getValue().size());
        }
    }

    // TEST 3: Xử lý ngoại lệ (Exception Handling)
    @Test
    public void testDoGet_ExceptionHandling() throws Exception {
        setTestCaseInfo("VIEW_03", "Xử lý lỗi Service", 
                "Service ném lỗi -> Catch & Forward", "Service Error", "Vẫn forward JSP");

        // 1. Giả lập Service bị lỗi kết nối DB
        when(mockService.getAllProducts()).thenThrow(new RuntimeException("DB Down"));

        // 2. Chạy
        servlet.doGet(request, response);

        // 3. Verify: Code vẫn phải forward về trang View (dù có thể trống trơn)
        verify(request).getRequestDispatcher(contains("View-products.jsp"));
        verify(dispatcher).forward(request, response);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ViewProductServlet.xlsx");
    }
}