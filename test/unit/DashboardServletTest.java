package unit;

import control.admin.DashboardServlet;
import dao.DashboardDao;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServletTest {

    private DashboardServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private DashboardDao dashboardDao; // Mock DAO

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new DashboardServlet();
        // [MỚI] Sử dụng setter để inject Mock DAO (đơn giản hơn Reflection)
        servlet.setDao(dashboardDao);
    }

    // --- CASE 1: LOAD DASHBOARD THÀNH CÔNG ---
    @Test
    public void testDoGet_LoadDashboardSuccess() throws Exception {
        setTestCaseInfo("DASH_SERV_01", "Load trang Dashboard thành công", 
                "1. Gọi DAO lấy số liệu\n2. Set attributes\n3. Forward JSP", 
                "Data: Rev=10tr, Orders=50", "Forward Dashboard.jsp & Data OK");

        // 1. Mock DAO behavior
        when(dashboardDao.getTotalRevenue()).thenReturn(10000000.0);
        when(dashboardDao.getTotalOrders()).thenReturn(50);
        when(dashboardDao.getTotalUsers()).thenReturn(20);
        
        List<Map<String, Object>> topProducts = new ArrayList<>();
        when(dashboardDao.getTopSellingProducts(5)).thenReturn(topProducts);
        
        List<Map<String, Object>> revenueChart = new ArrayList<>();
        when(dashboardDao.getRevenueLast7Days()).thenReturn(revenueChart);
        
        List<Map<String, Object>> statusChart = new ArrayList<>();
        when(dashboardDao.getOrderStatusStats()).thenReturn(statusChart);

        // 2. Mock Request
        when(request.getRequestDispatcher(contains("Dashboard.jsp"))).thenReturn(dispatcher);

        // 3. Run doGet (dùng Reflection vì protected)
        Method doGet = DashboardServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute("totalRevenue", 10000000.0);
        verify(request).setAttribute("totalOrders", 50);
        verify(request).setAttribute("totalUsers", 20);
        verify(request).setAttribute("topSellingProducts", topProducts);
        verify(request).setAttribute("revenueChartData", revenueChart);
        verify(request).setAttribute("statusChartData", statusChart);
        
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 2: LỖI HỆ THỐNG (EXCEPTION) ---
    @Test
    public void testDoGet_SystemError() throws Exception {
        setTestCaseInfo("DASH_SERV_02", "Lỗi hệ thống (Exception)", 
                "DAO ném lỗi RuntimeException", "Error", "Log Error & Forward JSP (Safe Mode)");

        // Giả lập lỗi DB
        when(dashboardDao.getTotalRevenue()).thenThrow(new RuntimeException("DB Connection Failed"));
        when(request.getRequestDispatcher(contains("Dashboard.jsp"))).thenReturn(dispatcher);

        Method doGet = DashboardServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify: Vẫn forward về trang dashboard (không chết trang)
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
        ExcelTestExporter.exportToExcel("KetQuaTest_DashboardServlet.xlsx");
    }
}