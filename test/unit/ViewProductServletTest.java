package unit;

import control.user.ViewProductServlet;
import entity.Products;
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
import org.mockito.ArgumentCaptor; // Dùng để bắt dữ liệu gửi đi
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

        // 1. Inject Mock Service (Reflection)
        Field serviceField = ViewProductServlet.class.getDeclaredField("productService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, mockService);

        // 2. Mock hành vi chung
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // === HÀM HỖ TRỢ: GỌI PROTECTED doGet ===
    private void invokeDoGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method doGetMethod = ViewProductServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, req, resp);
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

        invokeDoGet(request, response);

        // Verify: Đảm bảo forward đúng chỗ
        verify(request).getRequestDispatcher(contains("View-products.jsp"));
        verify(dispatcher).forward(request, response);
        
        // Verify: Dữ liệu được set
        verify(request).setAttribute(eq("productList"), anyList());
        verify(request).setAttribute(eq("shirtList"), anyList());
        verify(request).setAttribute(eq("pantsList"), anyList());
    }

    // TEST 2: Kiểm tra Logic Cắt danh sách (SubList)
    // Code của bạn: allProducts.size() > 8 ? subList(0, 8)
    @Test
    public void testDoGet_LimitListSize() throws Exception {
        setTestCaseInfo("VIEW_02", "Giới hạn số lượng hiển thị", 
                "DB trả về 20 SP -> Code cắt còn 8", "Input=20 items", "Attribute=8 items");

        // 1. Giả lập DB trả về 20 sản phẩm
        List<Products> hugeList = createMockList(20);
        when(mockService.getAllProducts()).thenReturn(hugeList);
        when(mockService.searchProducts(anyString())).thenReturn(new ArrayList<>()); // Các list khác rỗng cho gọn

        // 2. Chạy
        invokeDoGet(request, response);

        // 3. Kỹ thuật ArgumentCaptor: Để bắt lấy cái List mà Servlet gửi sang JSP
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(request).setAttribute(eq("productList"), captor.capture());

        // 4. Kiểm tra xem List bắt được có đúng size = 8 không
        List capturedList = captor.getValue();
        if (capturedList.size() != 8) {
            throw new Exception("Lỗi Logic: List không được cắt đúng 8 phần tử. Size thực tế: " + capturedList.size());
        }
    }

    // TEST 3: Xử lý ngoại lệ (Exception Handling)
    // Code của bạn: catch (Exception e) { forward... }
    @Test
    public void testDoGet_ExceptionHandling() throws Exception {
        setTestCaseInfo("VIEW_03", "Xử lý lỗi Service", 
                "Service ném lỗi -> Catch & Forward", "Service Error", "Vẫn forward JSP");

        // 1. Giả lập Service bị lỗi kết nối DB
        when(mockService.getAllProducts()).thenThrow(new RuntimeException("DB Down"));

        // 2. Chạy
        invokeDoGet(request, response);

        // 3. Verify: Code vẫn phải forward về trang View (dù có thể trống trơn)
        verify(request).getRequestDispatcher(contains("View-products.jsp"));
        verify(dispatcher).forward(request, response);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_ViewProductServlet.xlsx");
    }
}