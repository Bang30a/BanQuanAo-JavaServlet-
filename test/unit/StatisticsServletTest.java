package unit;

import control.admin.StatisticsServlet;
import dao.DashboardDao;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServletTest {

    private StatisticsServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private DashboardDao dashboardDao;

    // === CONFIG REPORT ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() {
        servlet = new StatisticsServlet();
        servlet.setDao(dashboardDao); // Inject Mock
    }

    // --- CASE 1: CÓ CHỌN NGÀY ---
    @Test
    public void testDoGet_WithParams() throws Exception {
        setTestCaseInfo("STAT_01", "Xem báo cáo theo ngày", 
                "Date: 2023-01-01 -> 2023-01-31", "Start/End valid", "Forward JSP + Data");

        when(request.getParameter("startDate")).thenReturn("2023-01-01");
        when(request.getParameter("endDate")).thenReturn("2023-01-31");
        when(request.getRequestDispatcher(contains("Statistics.jsp"))).thenReturn(dispatcher);

        // Mock DAO returns
        when(dashboardDao.getRevenueByDate("2023-01-01", "2023-01-31")).thenReturn(500.0);
        when(dashboardDao.getOrderCountByDate("2023-01-01", "2023-01-31")).thenReturn(5);
        when(dashboardDao.getTopSellingProductsByDate("2023-01-01", "2023-01-31")).thenReturn(new ArrayList<>());

        // Run (Reflection vì protected)
        Method doGet = StatisticsServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(request).setAttribute("revenue", 500.0);
        verify(request).setAttribute("orders", 5);
        verify(request).setAttribute("start", "2023-01-01");
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 2: MẶC ĐỊNH (KHÔNG CHỌN NGÀY) ---
    @Test
    public void testDoGet_DefaultDate() throws Exception {
        setTestCaseInfo("STAT_02", "Xem báo cáo mặc định", 
                "Date: null -> Tự lấy ngày hiện tại", "Start/End null", "Call DAO with Today");

        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getRequestDispatcher(contains("Statistics.jsp"))).thenReturn(dispatcher);

        // Run
        Method doGet = StatisticsServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify: DAO được gọi với bất kỳ chuỗi nào (vì ngày thay đổi theo thời gian thực)
        verify(dashboardDao).getRevenueByDate(anyString(), anyString());
        
        // Kiểm tra xem có set attribute start/end không
        verify(request).setAttribute(eq("start"), anyString());
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_StatisticsServlet.xlsx"); }
}