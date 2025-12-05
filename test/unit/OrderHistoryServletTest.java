package unit;

import control.user.OrderHistoryServlet;
import service.OrderService;
import entity.Users;
import entity.Orders;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    // === REPORT CONFIG ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new OrderHistoryServlet();
        // Inject Mock Service via Reflection
        try {
            Field serviceField = OrderHistoryServlet.class.getDeclaredField("orderService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, orderService);
        } catch (NoSuchFieldException ignored) {}
        
        // Default behavior
        when(request.getContextPath()).thenReturn("/ShopDuck");
    }

    // --- CASE 1: XEM LỊCH SỬ THÀNH CÔNG ---
    @Test
    public void testDoGet_ListOrders() throws Exception {
        setTestCaseInfo("HIST_01", "Xem lịch sử đơn hàng", 
                "1. User login\n2. Action=null", "UserID=1", "Forward order-history.jsp");

        setupUserSession();
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher(contains("order-history.jsp"))).thenReturn(dispatcher);

        List<Orders> list = new ArrayList<>();
        when(orderService.getOrdersForUser(1)).thenReturn(list);

        invokeDoGet();

        verify(request).setAttribute("orderList", list);
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 2: XEM CHI TIẾT THÀNH CÔNG ---
    @Test
    public void testDoGet_OrderDetail() throws Exception {
        setTestCaseInfo("HIST_02", "Xem chi tiết đơn hàng", 
                "1. Action=detail, ID=10\n2. Service found order", "ID=10", "Forward order-detail.jsp");

        setupUserSession();
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getRequestDispatcher(contains("order-detail.jsp"))).thenReturn(dispatcher);

        Orders mockOrder = new Orders();
        when(orderService.getSecuredOrder(10, 1)).thenReturn(mockOrder);
        when(orderService.getRichOrderDetails(10)).thenReturn(Collections.emptyList());

        invokeDoGet();

        verify(request).setAttribute("order", mockOrder);
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 3: CHƯA ĐĂNG NHẬP ---
    @Test
    public void testDoGet_NotLoggedIn() throws Exception {
        setTestCaseInfo("HIST_03", "Chưa đăng nhập", 
                "Session User = null", "User=null", "Redirect Login.jsp");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null); // Chưa login

        invokeDoGet();

        verify(session).setAttribute(eq("loginError"), anyString());
        verify(response).sendRedirect(contains("/user/auth/Login.jsp"));
        // Đảm bảo không gọi service
        verify(orderService, never()).getOrdersForUser(anyInt());
    }

    // --- [MỚI] CASE 4: CHI TIẾT THIẾU ID ---
    @Test
    public void testDoGet_Detail_MissingId() throws Exception {
        setTestCaseInfo("HIST_04", "Xem chi tiết thiếu ID", 
                "Action=detail, ID=null", "ID=null", "Redirect order-history");

        setupUserSession();
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn(null); // Thiếu ID

        invokeDoGet();

        // Logic: response.sendRedirect(... + "/user/order-history");
        verify(response).sendRedirect(contains("/user/order-history"));
    }

    // --- [MỚI] CASE 5: KHÔNG TÌM THẤY ĐƠN (HOẶC KHÔNG QUYỀN) ---
    @Test
    public void testDoGet_Detail_NotFound() throws Exception {
        setTestCaseInfo("HIST_05", "Đơn hàng không tồn tại/Sai chủ", 
                "Service return null", "ID=99", "Redirect order-history");

        setupUserSession();
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("99");

        // Mock service trả về null (ko tìm thấy hoặc ko phải của user này)
        when(orderService.getSecuredOrder(99, 1)).thenReturn(null);

        invokeDoGet();

        verify(response).sendRedirect(contains("/user/order-history"));
    }

    // --- [MỚI] CASE 6: ID SAI ĐỊNH DẠNG (EXCEPTION) ---
    @Test
    public void testDoGet_Detail_InvalidId() throws Exception {
        setTestCaseInfo("HIST_06", "ID rác (Chữ)", 
                "ID='abc' -> NumberFormatEx", "ID='abc'", "Catch -> Redirect view-products");

        setupUserSession();
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("abc"); // Gây lỗi

        invokeDoGet();

        // Servlet catch Exception -> Redirect view-products
        verify(response).sendRedirect(contains("/user/view-products"));
    }

    // --- [MỚI] CASE 7: LỖI HỆ THỐNG ---
    @Test
    public void testDoGet_SystemError() throws Exception {
        setTestCaseInfo("HIST_07", "Lỗi hệ thống", 
                "Service ném RuntimeException", "Crash", "Catch -> Redirect view-products");

        setupUserSession();
        when(request.getParameter("action")).thenReturn(null);
        
        // Giả lập lỗi DB
        when(orderService.getOrdersForUser(1)).thenThrow(new RuntimeException("DB Fail"));

        invokeDoGet();

        verify(response).sendRedirect(contains("/user/view-products"));
    }

    // === HELPER ===
    private void invokeDoGet() throws Exception {
        Method doGet = OrderHistoryServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void setupUserSession() {
        Users u = new Users(); u.setId(1);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(u);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_OrderHistoryServlet.xlsx"); }
}