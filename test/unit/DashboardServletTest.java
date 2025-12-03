package unit;

import control.admin.DashboardServlet;
import dao.DashboardDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ExcelTestExporter;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServletTest {

    private DashboardServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private DashboardDao dashboardDao; // Mock DAO

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new DashboardServlet();
        
        // Inject Mock DAO vào Servlet (cần sửa Servlet hoặc dùng Reflection nếu Servlet tự new DAO)
        // Ở đây giả định bạn dùng Reflection để set field private 'dao' trong DashboardServlet
        // Nếu trong DashboardServlet bạn khai báo: DashboardDao dao = new DashboardDao(); trong doGet
        // thì KHÔNG THỂ mock được bằng cách thông thường.
        // Bạn CẦN sửa DashboardServlet đưa biến dao ra ngoài làm thuộc tính class (private DashboardDao dao;) 
        // và khởi tạo trong init() hoặc constructor, hoặc dùng setter.
        
        // Giả sử bạn đã sửa DashboardServlet như sau:
        // public class DashboardServlet extends HttpServlet {
        //     private DashboardDao dao = new DashboardDao(); // Có thể set lại bằng Reflection
        //     ...
        
        try {
            Field daoField = DashboardServlet.class.getDeclaredField("dao"); // Tên biến trong Servlet phải là 'dao'
            daoField.setAccessible(true);
            daoField.set(servlet, dashboardDao);
        } catch (NoSuchFieldException e) {
            // Nếu chưa sửa Servlet, test này có thể fail ở bước verify DAO
            System.out.println("LƯU Ý: Cần đưa biến 'DashboardDao dao' ra làm thuộc tính class trong DashboardServlet để test.");
        }
    }

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
        when(request.getRequestDispatcher("/admin/dashboard/Dashboard.jsp")).thenReturn(dispatcher);

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

    // === XUẤT EXCEL ===
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
        ExcelTestExporter.exportToExcel("KetQuaTest_DashboardServlet.xlsx");
    }
}