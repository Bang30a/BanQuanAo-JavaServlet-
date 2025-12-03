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

    @Test
    public void testDoGet_ViewDetails() throws Exception {

        setTestCaseInfo(
                "ORD_DET_01",
                "Xem chi tiết đơn hàng",
                "1. orderId=100\n2. Gọi service getDetails\n3. Forward JSP",
                "orderId = 100",
                "Forward → View-order-detail.jsp"
        );

        // Input
        when(request.getParameter("orderId")).thenReturn("100");
        when(request.getRequestDispatcher(contains("View-order-detail.jsp"))).thenReturn(dispatcher);

        // Service mock
        List<OrderDetails> details = new ArrayList<>();
        when(adminService.getDetailsForOrder(100)).thenReturn(details);

        // Run doGet bằng reflection
        Method doGet = OrderDetailManagerServlet.class
                .getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(request).setAttribute(eq("ORDER_ID"), eq(100));
        verify(request).setAttribute(eq("DETAILS"), eq(details));
        verify(dispatcher).forward(request, response);
    }

    // === THU THẬP KẾT QUẢ TEST ===
    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS"
            );
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps, currentData, currentExpected,
                    e.getMessage(), "FAIL"
            );
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderDetailManagerServlet.xlsx");
    }
}
