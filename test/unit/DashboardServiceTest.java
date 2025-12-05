package unit;

import dao.DashboardDao;
import service.DashboardService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceTest {

    @Mock private DashboardDao dashboardDao;
    @InjectMocks private DashboardService service;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    // --- CASE 1: LẤY DOANH THU ---
    @Test
    public void testGetTotalRevenue() {
        setTestCaseInfo("DASH_SVC_01", "Lấy tổng doanh thu", 
                "Call DAO", "100.0", "Return 100.0");

        when(dashboardDao.getTotalRevenue()).thenReturn(100.0);
        assertEquals(100.0, service.getTotalRevenue(), 0.01);
    }

    @Test
    public void testGetTotalRevenue_Error() {
        setTestCaseInfo("DASH_SVC_02", "Lỗi lấy doanh thu", 
                "DAO Exception", "Error", "Return 0.0");

        when(dashboardDao.getTotalRevenue()).thenThrow(new RuntimeException("DB Fail"));
        assertEquals(0.0, service.getTotalRevenue(), 0.01);
    }

    // --- CASE 2: LẤY SỐ ĐƠN HÀNG ---
    @Test
    public void testGetTotalOrders() {
        setTestCaseInfo("DASH_SVC_03", "Lấy tổng đơn hàng", 
                "Call DAO", "50", "Return 50");

        when(dashboardDao.getTotalOrders()).thenReturn(50);
        assertEquals(50, service.getTotalOrders());
    }

    @Test
    public void testGetTotalOrders_Error() {
        setTestCaseInfo("DASH_SVC_04", "Lỗi lấy đơn hàng", 
                "DAO Exception", "Error", "Return 0");

        when(dashboardDao.getTotalOrders()).thenThrow(new RuntimeException("DB Fail"));
        assertEquals(0, service.getTotalOrders());
    }

    // --- CASE 3: LẤY SỐ USER ---
    @Test
    public void testGetTotalUsers() {
        setTestCaseInfo("DASH_SVC_05", "Lấy tổng user", 
                "Call DAO", "20", "Return 20");

        when(dashboardDao.getTotalUsers()).thenReturn(20);
        assertEquals(20, service.getTotalUsers());
    }

    @Test
    public void testGetTotalUsers_Error() {
        setTestCaseInfo("DASH_SVC_06", "Lỗi lấy user", 
                "DAO Exception", "Error", "Return 0");

        when(dashboardDao.getTotalUsers()).thenThrow(new RuntimeException("DB Fail"));
        assertEquals(0, service.getTotalUsers());
    }

    // --- CASE 4: TOP SẢN PHẨM ---
    @Test
    public void testGetTopSellingProducts() {
        setTestCaseInfo("DASH_SVC_07", "Lấy Top SP bán chạy", 
                "Call DAO getTop(5)", "Limit=5", "Return List size > 0");

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Collections.emptyMap());
        when(dashboardDao.getTopSellingProducts(5)).thenReturn(list);

        List<Map<String, Object>> result = service.getTopSellingProducts(5);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTopSellingProducts_Error() {
        setTestCaseInfo("DASH_SVC_08", "Lỗi lấy Top SP", 
                "DAO Exception", "Error", "Return Empty List");

        when(dashboardDao.getTopSellingProducts(anyInt())).thenThrow(new RuntimeException("DB Fail"));
        
        List<Map<String, Object>> result = service.getTopSellingProducts(5);
        assertNotNull(result);
        assertTrue(result.isEmpty());
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


    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_DashboardService.xlsx"); }
}