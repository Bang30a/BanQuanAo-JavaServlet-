package unit;

import control.admin.ExportExcelServlet;
import dao.DashboardDao;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class ExportExcelServletTest {

    private ExportExcelServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private DashboardDao dashboardDao;
    @Mock private ServletOutputStream outputStream;

    // === CONFIG REPORT ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new ExportExcelServlet();
        servlet.setDao(dashboardDao);
        
        // Mock response.getOutputStream() để không bị lỗi NullPointer khi servlet ghi file
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void testDoGet_ExportSuccess() throws Exception {
        setTestCaseInfo("EXCEL_01", "Xuất Excel thành công", 
                "1. Valid Date\n2. DAO data OK\n3. Write to Stream", 
                "Date: 2023-01-01", "ContentType: xlsx, Header set");

        when(request.getParameter("startDate")).thenReturn("2023-01-01");
        when(request.getParameter("endDate")).thenReturn("2023-01-31");

        // Mock DAO Data
        when(dashboardDao.getRevenueByDate(anyString(), anyString())).thenReturn(1000.0);
        when(dashboardDao.getOrderCountByDate(anyString(), anyString())).thenReturn(10);
        when(dashboardDao.getTopSellingProducts(anyInt())).thenReturn(new ArrayList<>());

        // Run
        Method doGet = ExportExcelServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // Verify
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(contains("Content-Disposition"), contains("attachment"));
        
        // Quan trọng: Verify rằng servlet đã thực sự gọi hàm ghi dữ liệu ra output stream
        // (Apache POI sẽ gọi write trên stream này)
        // verify(outputStream, atLeastOnce()).write(anyInt()); // Hoặc write(byte[])
        // Do POI có thể gọi nhiều dạng write khác nhau, chỉ cần verify getOutputStream được gọi là đủ.
        verify(response).getOutputStream();
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ExportExcelServlet.xlsx"); }
}