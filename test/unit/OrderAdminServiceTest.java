package unit;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductVariantDao;
import entity.OrderDetails;
import entity.Orders;
import entity.ProductVariants;
import service.OrderAdminService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class OrderAdminServiceTest {

    @Mock private OrderDao orderDao;
    @Mock private OrderDetailDao detailDao;
    @Mock private ProductVariantDao variantDao;

    private OrderAdminService service;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() {
        // Khởi tạo Service với 3 Mock DAO
        service = new OrderAdminService(orderDao, detailDao, variantDao);
    }

    // ==========================================
    // 1. TEST LẤY DANH SÁCH (GET)
    // ==========================================

    @Test
    public void testGetAllOrders() {
        setTestCaseInfo("ORD_ADMIN_01", "Lấy tất cả đơn hàng", 
                "Call DAO getAll", "DB has 1 order", "Return List size 1");

        List<Orders> list = new ArrayList<>();
        list.add(new Orders());
        when(orderDao.getAllOrders()).thenReturn(list);

        List<Orders> result = service.getAllOrders();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetOrdersByStatus() {
        setTestCaseInfo("ORD_ADMIN_02", "Lọc đơn theo trạng thái", 
                "Call DAO getByStatus", "Status='Pending'", "Return List");

        List<Orders> list = new ArrayList<>();
        when(orderDao.getOrdersByStatus("Pending")).thenReturn(list);

        List<Orders> result = service.getOrdersByStatus("Pending");
        assertNotNull(result);
        verify(orderDao).getOrdersByStatus("Pending");
    }

    @Test
    public void testGetOrderForEdit_Existing() {
        setTestCaseInfo("ORD_ADMIN_03", "Lấy đơn hàng để sửa", 
                "ID=5 -> Found", "ID=5", "Return Order Obj");

        Orders o = new Orders(); o.setId(5);
        when(orderDao.getOrderById(5)).thenReturn(o);

        Orders result = service.getOrderForEdit(5);
        assertEquals(5, result.getId());
    }

    @Test
    public void testGetOrderForEdit_New() {
        setTestCaseInfo("ORD_ADMIN_04", "Lấy form thêm mới", 
                "ID=0", "ID=0", "Return New Order");

        Orders result = service.getOrderForEdit(0);
        assertEquals(0, result.getId());
        verify(orderDao, never()).getOrderById(anyInt());
    }

    // ==========================================
    // 2. TEST LƯU / CẬP NHẬT (SAVE)
    // ==========================================

    @Test
    public void testSaveOrder_New() {
        setTestCaseInfo("ORD_ADMIN_05", "Lưu đơn hàng mới", 
                "ID=0 -> Insert", "Order ID=0", "Result: True");

        Orders newOrder = new Orders(); newOrder.setId(0);
        
        // Giả lập insert thành công (hàm insert trong DAO có thể void hoặc int, service trả boolean)
        // Ở service code: orderDao.addOrder(order); return true;
        
        boolean result = service.saveOrUpdateOrder(newOrder);
        
        verify(orderDao).addOrder(newOrder);
        assertTrue(result);
    }

    @Test
    public void testSaveOrder_Update() {
        setTestCaseInfo("ORD_ADMIN_06", "Cập nhật thông tin đơn", 
                "ID=10 -> Update", "Order ID=10", "Result: True");

        Orders existing = new Orders(); existing.setId(10);
        // Giả lập tìm thấy ID=10
        when(orderDao.getOrderById(10)).thenReturn(existing);

        boolean result = service.saveOrUpdateOrder(existing);
        
        verify(orderDao).updateOrder(existing);
        assertTrue(result);
    }

    // ==========================================
    // 3. TEST CẬP NHẬT TRẠNG THÁI & TRỪ KHO (QUAN TRỌNG)
    // ==========================================

    @Test
    public void testUpdateStatus_Simple() {
        setTestCaseInfo("ORD_ADMIN_07", "Đổi trạng thái thường", 
                "Pending -> Processing", "No Stock Change", "Status Changed");

        Orders o = new Orders(); o.setId(1); o.setStatus("Pending");
        when(orderDao.getOrderById(1)).thenReturn(o);

        boolean result = service.updateOrderStatus(1, "Processing");

        assertTrue(result);
        assertEquals("Processing", o.getStatus());
        verify(orderDao).updateOrder(o);
        // Verify KHÔNG gọi trừ kho
        verify(detailDao, never()).getDetailsByOrderId(anyInt());
    }

    @Test
    public void testUpdateStatus_ToShipped_DeductStock() {
        setTestCaseInfo("ORD_ADMIN_08", "Đổi sang 'Đã giao' -> Trừ kho", 
                "1. Pending -> Đã giao\n2. Get Details\n3. Update Variant Stock", 
                "Stock=50, Qty=2", "New Stock=48");

        // 1. Mock Order cũ
        Orders o = new Orders(); o.setId(1); o.setStatus("Pending");
        when(orderDao.getOrderById(1)).thenReturn(o);

        // 2. Mock Chi tiết đơn hàng (Mua 2 cái của Variant ID 10)
        List<OrderDetails> details = new ArrayList<>();
        OrderDetails d = new OrderDetails(); d.setProductVariantId(10); d.setQuantity(2);
        details.add(d);
        when(detailDao.getDetailsByOrderId(1)).thenReturn(details);

        // 3. Mock Biến thể (Tồn kho 50)
        ProductVariants v = new ProductVariants(); v.setId(10); v.setStock(50);
        when(variantDao.findById(10)).thenReturn(v);

        // 4. Thực thi
        boolean result = service.updateOrderStatus(1, "Đã giao");

        // 5. Verify
        assertTrue(result);
        // Verify trạng thái đổi
        assertEquals("Đã giao", o.getStatus());
        
        // Verify trừ kho: Stock mới phải là 50 - 2 = 48
        ArgumentCaptor<ProductVariants> captor = ArgumentCaptor.forClass(ProductVariants.class);
        verify(variantDao).updateVariant(captor.capture());
        
        assertEquals(48, captor.getValue().getStock());
    }

    @Test
    public void testUpdateStatus_NotFound() {
        setTestCaseInfo("ORD_ADMIN_09", "Đổi trạng thái đơn ko tồn tại", 
                "ID=999 -> Null", "ID=999", "Return False");

        when(orderDao.getOrderById(999)).thenReturn(null);
        boolean result = service.updateOrderStatus(999, "Done");
        assertFalse(result);
    }

    // ==========================================
    // 4. TEST DELETE & EXCEPTION
    // ==========================================

    @Test
    public void testDeleteOrder() {
        setTestCaseInfo("ORD_ADMIN_10", "Xóa đơn hàng", "Call DAO delete", "ID=1", "Result: True");
        
        // Giả lập DAO xóa ok (không ném lỗi)
        boolean result = service.deleteOrder(1);
        
        verify(orderDao).deleteOrder(1);
        assertTrue(result);
    }

    @Test
    public void testExceptionHandling() {
        setTestCaseInfo("ORD_ADMIN_11", "Lỗi hệ thống (DB Error)", 
                "DAO throw Exception", "Exception", "Return Safe Value");

        // Test getAll lỗi -> return Empty List
        when(orderDao.getAllOrders()).thenThrow(new RuntimeException("DB Error"));
        assertTrue(service.getAllOrders().isEmpty());

        // Test updateStatus lỗi -> return False
        when(orderDao.getOrderById(anyInt())).thenThrow(new RuntimeException("DB Error"));
        assertFalse(service.updateOrderStatus(1, "Done"));
    }
    
    @Test
    public void testGetDetailsForOrder() {
        setTestCaseInfo("ORD_ADMIN_12", "Lấy chi tiết đơn", 
                "Call DAO detail", "ID=1", "Return List");
        
        when(detailDao.getDetailsByOrderId(1)).thenReturn(new ArrayList<>());
        service.getDetailsForOrder(1);
        verify(detailDao).getDetailsByOrderId(1);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_OrderAdminService.xlsx"); }
}