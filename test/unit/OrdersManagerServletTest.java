package unit;

import control.admin.OrdersManagerServlet;
import entity.Orders;
import service.OrderAdminService;
import util.ExcelTestExporter; // <-- Import class tiện ích

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

@RunWith(MockitoJUnitRunner.class)
public class OrdersManagerServletTest {

    private OrdersManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private OrderAdminService adminService;

    // === CẤU HÌNH BÁO CÁO (Dùng biến instance thay vì static để clean hơn) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData vì class tiện ích tự quản lý

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new OrdersManagerServlet();
        try {
            Field serviceField = OrdersManagerServlet.class.getDeclaredField("adminService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, adminService);
        } catch (NoSuchFieldException e) {}
    }

    @Test
    public void testDoGet_ListAll() throws Exception {
        setTestCaseInfo("ORD_MGR_01", "Xem tất cả đơn hàng", 
                "1. Action='List'\n2. Status=null", 
                "Status=null", "Forward View-orders.jsp");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("status")).thenReturn(null);
        when(request.getRequestDispatcher(contains("View-orders.jsp"))).thenReturn(dispatcher);

        List<Orders> mockList = new ArrayList<>();
        when(adminService.getAllOrders()).thenReturn(mockList);

        Method doGet = OrdersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(request).setAttribute(eq("list"), eq(mockList));
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoGet_FilterStatus() throws Exception {
        setTestCaseInfo("ORD_MGR_02", "Lọc đơn hàng theo trạng thái", 
                "1. Action='List'\n2. Status='Pending'", 
                "Status='Pending'", "Call getOrdersByStatus");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("status")).thenReturn("Pending");
        when(request.getRequestDispatcher(contains("View-orders.jsp"))).thenReturn(dispatcher);

        List<Orders> mockList = new ArrayList<>();
        when(adminService.getOrdersByStatus("Pending")).thenReturn(mockList);

        Method doGet = OrdersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(adminService).getOrdersByStatus("Pending");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoGet_UpdateStatus() throws Exception {
        setTestCaseInfo("ORD_MGR_03", "Cập nhật trạng thái đơn", 
                "1. Action='UpdateStatus'\n2. ID=10, Status='Done'", 
                "ID=10, St='Done'", "Call update -> Redirect");

        when(request.getParameter("action")).thenReturn("UpdateStatus");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("status")).thenReturn("Done");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        Method doGet = OrdersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(adminService).updateOrderStatus(10, "Done");
        verify(response).sendRedirect(contains("OrdersManagerServlet?action=List"));
    }

    @Test
    public void testDoGet_DeleteOrder() throws Exception {
        setTestCaseInfo("ORD_MGR_04", "Xóa đơn hàng", 
                "1. Action='Delete'\n2. ID=5", 
                "ID=5", "Call delete -> Redirect");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        Method doGet = OrdersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        verify(adminService).deleteOrder(5);
        verify(response).sendRedirect(contains("OrdersManagerServlet?action=List"));
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
        // Xuất ra file Excel (.xlsx) thay vì CSV
        ExcelTestExporter.exportToExcel("KetQuaTest_OrdersManagerServlet.xlsx");
    }
}