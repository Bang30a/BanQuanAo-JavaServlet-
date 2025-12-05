package unit;

// === IMPORT LOGIC ===
import control.admin.OrderDetailManagerServlet;
import entity.OrderDetails;
import service.OrderAdminService;
import util.ExcelTestExporter;

// === IMPORT JUNIT & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// === EXCEL REPORT ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class OrderDetailManagerServletTest {

    private OrderDetailManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private OrderAdminService adminService;

    // === INFO CHO EXCEL ===
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
        servlet = new OrderDetailManagerServlet();
        try {
            Field serviceField = OrderDetailManagerServlet.class.getDeclaredField("adminService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, adminService);
        } catch (NoSuchFieldException ignored) {}
    }

    // --- HÀM HỖ TRỢ GỌI DOGET ---
    private void invokeDoGet() throws Exception {
        Method doGet = OrderDetailManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    // --- CASE 1: XEM CHI TIẾT THÀNH CÔNG ---
    @Test
    public void testDoGet_ViewDetails() throws Exception {
        setTestCaseInfo("ORD_DET_01", "Xem chi tiết đơn hàng", 
                "1. orderId=100\n2. Gọi service getDetails\n3. Forward JSP", 
                "orderId = 100", "Forward → View-order-detail.jsp");

        // Input
        when(request.getParameter("orderId")).thenReturn("100");
        when(request.getRequestDispatcher(contains("View-order-detail.jsp"))).thenReturn(dispatcher);

        // Service mock
        List<OrderDetails> details = new ArrayList<>();
        when(adminService.getDetailsForOrder(100)).thenReturn(details);

        invokeDoGet();

        // Verify
        verify(request).setAttribute(eq("ORDER_ID"), eq(100));
        verify(request).setAttribute(eq("DETAILS"), eq(details));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 2: ID KHÔNG HỢP LỆ (CHỮ) ---
    @Test
    public void testDoGet_InvalidId() throws Exception {
        setTestCaseInfo("ORD_DET_02", "ID lỗi (Chữ)", 
                "1. orderId='abc'\n2. ParseInt lỗi\n3. Catch & Forward Error", 
                "orderId = abc", "Forward → error.jsp");

        // Input gây lỗi NumberFormat
        when(request.getParameter("orderId")).thenReturn("abc");
        // Khi lỗi, servlet sẽ forward về error.jsp
        when(request.getRequestDispatcher(contains("error.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        // Verify: Phải set attribute error và forward sang trang lỗi
        verify(request).setAttribute(eq("error"), contains("For input string"));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 3: LỖI HỆ THỐNG (SERVICE EXCEPTION) ---
    @Test
    public void testDoGet_SystemError() throws Exception {
        setTestCaseInfo("ORD_DET_03", "Lỗi hệ thống (Service)", 
                "1. orderId=10\n2. Service ném lỗi\n3. Catch & Forward Error", 
                "Service Exception", "Forward → error.jsp");

        when(request.getParameter("orderId")).thenReturn("10");
        when(request.getRequestDispatcher(contains("error.jsp"))).thenReturn(dispatcher);

        // Giả lập Service bị lỗi
        doThrow(new RuntimeException("DB Connection Lost")).when(adminService).getDetailsForOrder(10);

        invokeDoGet();

        // Verify
        verify(request).setAttribute(eq("error"), contains("DB Connection Lost"));
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
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDetailManagerServlet.xlsx");
    }
}