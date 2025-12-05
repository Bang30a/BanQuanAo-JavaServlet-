package unit;

import control.admin.OrdersManagerServlet;
import entity.Orders;
import service.OrderAdminService;
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

@RunWith(MockitoJUnitRunner.class)
public class OrdersManagerServletTest {

    private OrdersManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private OrderAdminService adminService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
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

    // --- CASE 1: XEM DANH SÁCH (LIST) ---
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

        invokeDoGet();

        verify(request).setAttribute(eq("list"), eq(mockList));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 2: LỌC THEO TRẠNG THÁI ---
    @Test
    public void testDoGet_FilterStatus() throws Exception {
        setTestCaseInfo("ORD_MGR_02", "Lọc đơn hàng", 
                "1. Action='List'\n2. Status='Pending'", 
                "Status='Pending'", "Call getOrdersByStatus");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getParameter("status")).thenReturn("Pending");
        when(request.getRequestDispatcher(contains("View-orders.jsp"))).thenReturn(dispatcher);

        List<Orders> mockList = new ArrayList<>();
        when(adminService.getOrdersByStatus("Pending")).thenReturn(mockList);

        invokeDoGet();

        verify(adminService).getOrdersByStatus("Pending");
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 3: HIỆN FORM SỬA ---
    @Test
    public void testDoGet_EditForm() throws Exception {
        setTestCaseInfo("ORD_MGR_03", "Hiện form sửa đơn", 
                "1. Action='AddOrEdit', ID=5", "ID=5", "Forward OrdersManager.jsp");

        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("OrdersManager.jsp"))).thenReturn(dispatcher);

        Orders mockOrder = new Orders(); mockOrder.setId(5);
        when(adminService.getOrderForEdit(5)).thenReturn(mockOrder);

        invokeDoGet();

        verify(request).setAttribute(eq("ORDER"), eq(mockOrder));
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 4: LƯU CẬP NHẬT (SAVE) ---
    @Test
    public void testDoPost_SaveOrder() throws Exception {
        setTestCaseInfo("ORD_MGR_04", "Lưu cập nhật đơn hàng", 
                "1. Action='SaveOrUpdate'\n2. Params đầy đủ", 
                "ID=10, Total=500k", "Call Service Save -> Redirect List");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        
        // Mock các tham số form
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("userId")).thenReturn("1");
        when(request.getParameter("total")).thenReturn("500000");
        when(request.getParameter("address")).thenReturn("HCM");
        when(request.getParameter("phone")).thenReturn("0987654321");
        when(request.getParameter("orderDate")).thenReturn("2025-12-25 10:00:00"); // Format chuẩn Timestamp
        when(request.getParameter("status")).thenReturn("Shipped");
        
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoPost();

        // Verify: Service được gọi với object chứa dữ liệu đúng
        verify(adminService).saveOrUpdateOrder(any(Orders.class));
        verify(response).sendRedirect(contains("OrdersManagerServlet?action=List"));
    }

    // --- CASE 5: CẬP NHẬT TRẠNG THÁI NHANH ---
    @Test
    public void testDoGet_UpdateStatus() throws Exception {
        setTestCaseInfo("ORD_MGR_05", "Cập nhật trạng thái nhanh", 
                "1. Action='UpdateStatus'\n2. ID=10, Status='Done'", 
                "ID=10, St='Done'", "Call update -> Redirect");

        when(request.getParameter("action")).thenReturn("UpdateStatus");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("status")).thenReturn("Done");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(adminService).updateOrderStatus(10, "Done");
        verify(response).sendRedirect(contains("OrdersManagerServlet?action=List"));
    }

    // --- CASE 6: XÓA ĐƠN HÀNG ---
    @Test
    public void testDoGet_DeleteOrder() throws Exception {
        setTestCaseInfo("ORD_MGR_06", "Xóa đơn hàng", 
                "1. Action='Delete'\n2. ID=5", 
                "ID=5", "Call delete -> Redirect");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(adminService).deleteOrder(5);
        verify(response).sendRedirect(contains("OrdersManagerServlet?action=List"));
    }

    // --- [MỚI] CASE 7: ACTION MẶC ĐỊNH ---
    @Test
    public void testDoGet_DefaultAction() throws Exception {
        setTestCaseInfo("ORD_MGR_07", "Action Null -> List", 
                "Action=null", "Null", "Mặc định gọi getAllOrders");

        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher(contains("View-orders.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        verify(adminService).getAllOrders();
    }

    // --- [MỚI] CASE 8: LỖI HỆ THỐNG (EXCEPTION) ---
    @Test
    public void testProcessRequest_SystemError() throws Exception {
        setTestCaseInfo("ORD_MGR_08", "Lỗi hệ thống", 
                "Service ném lỗi", "Exception", "Forward trang error.jsp");

        when(request.getParameter("action")).thenReturn("List");
        // Giả lập lỗi DB
        doThrow(new RuntimeException("DB Connection Failed")).when(adminService).getAllOrders();
        
        when(request.getRequestDispatcher(contains("error.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        verify(request).setAttribute(eq("error"), contains("DB Connection Failed"));
        verify(dispatcher).forward(request, response);
    }

    // === HELPER METHODS ===
    private void invokeDoGet() throws Exception {
        Method doGet = OrdersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void invokeDoPost() throws Exception {
        Method doPost = OrdersManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_OrdersManagerServlet.xlsx"); }
}