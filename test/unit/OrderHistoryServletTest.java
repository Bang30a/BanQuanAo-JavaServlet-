package unit;

// === IMPORT LOGIC CHÍNH ===
import control.user.OrderHistoryServlet;
import service.OrderService;
import entity.Users;
import entity.Orders;
import util.ExcelTestExporter;

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class OrderHistoryServletTest {

    private OrderHistoryServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private OrderService orderService;

    // === THÔNG TIN TEST CASE (giống AddToCart) ===
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
        servlet = new OrderHistoryServlet();

        // Inject OrderService
        try {
            Field serviceField = OrderHistoryServlet.class.getDeclaredField("orderService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, orderService);
        } catch (NoSuchFieldException ignored) {}
    }

    // =====================================================================
    // TEST 1: Xem lịch sử đơn hàng
    // =====================================================================
    @Test
    public void testDoGet_ListOrders() throws Exception {
        setTestCaseInfo(
                "HIST_01",
                "Lịch sử đơn hàng",
                "1. User có session\n2. action=null\n3. Gọi service getOrdersForUser()",
                "UserID=1",
                "Forward: order-history.jsp"
        );

        Users u = new Users(); u.setId(1);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(u);

        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher(contains("order-history.jsp"))).thenReturn(dispatcher);

        List<Orders> list = new ArrayList<>();
        when(orderService.getOrdersForUser(1)).thenReturn(list);

        Method doGet = OrderHistoryServlet.class.getDeclaredMethod(
                "doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(request).setAttribute("orderList", list);
        verify(dispatcher).forward(request, response);
    }

    // =====================================================================
    // TEST 2: Xem chi tiết đơn hàng
    // =====================================================================
    @Test
    public void testDoGet_OrderDetail() throws Exception {
        setTestCaseInfo(
                "HIST_02",
                "Chi tiết đơn hàng",
                "1. action=detail\n2. id=10\n3. Service getSecuredOrder()",
                "OrderID=10",
                "Forward: order-detail.jsp"
        );

        Users u = new Users(); u.setId(1);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(u);

        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getRequestDispatcher(contains("order-detail.jsp"))).thenReturn(dispatcher);

        Orders mockOrder = new Orders();
        when(orderService.getSecuredOrder(10, 1)).thenReturn(mockOrder);
        when(orderService.getRichOrderDetails(10)).thenReturn(new ArrayList<>());

        Method doGet = OrderHistoryServlet.class.getDeclaredMethod(
                "doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(request).setAttribute("order", mockOrder);
        verify(dispatcher).forward(request, response);
    }

    // =====================================================================
    // RULE — THÊM DỮ LIỆU VÀO EXCEL
    // =====================================================================
    @Rule
    public TestWatcher watcher = new TestWatcher() {

        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName,
                    currentSteps, currentData, currentExpected,
                    "OK", "PASS"
            );
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName,
                    currentSteps, currentData, currentExpected,
                    e.getMessage(), "FAIL"
            );
        }
    };

    // =====================================================================
    // XUẤT FILE EXCEL SAU CÙNG
    // =====================================================================
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderHistoryServlet.xlsx");
    }
}
