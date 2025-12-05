package unit;

// === IMPORT LOGIC CHÍNH ===
import service.OrderService;
import service.OrderResult;
import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;

import entity.CartBean;
import entity.ProductVariants;
import entity.Users;
import entity.Orders;
import entity.OrderDetails;
import entity.Products;
import entity.Size;

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

// === IMPORT JUNIT RULES ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

// === IMPORT EXCEL EXPORT ===
import util.ExcelTestExporter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    // === MOCK DAO ===
    @Mock private OrderDao orderDao;
    @Mock private OrderDetailDao detailDao;
    @Mock private ProductDao productDao;
    @Mock private ProductVariantDao variantDao;
    @Mock private SizeDao sizeDao;
    @Mock private Connection mockConn;

    private OrderService orderService;
    private Users user;
    private List<CartBean> cart;

    // === BIẾN TEST-CASE LOCAL (KHÔNG STATIC) ===
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
        orderService = new OrderService(orderDao, detailDao, productDao, variantDao, sizeDao, null);

        when(orderDao.getMockConnection()).thenReturn(mockConn);

        user = new Users();
        user.setId(1);

        cart = new ArrayList<>();
        ProductVariants p = new ProductVariants();
        p.setId(10);
        p.setPrice(100.0);
        cart.add(new CartBean(p, 2)); // total = 200
    }

    // ============================================================
    //                      TEST PLACE ORDER
    // ============================================================

    @Test
    public void testPlaceOrder_Success() throws Exception {
        setTestCaseInfo("ORD_SVC_01", "Đặt hàng thành công", 
                "1. Add Order OK\n2. Add Detail OK\n3. Commit", "Cart: 1 item", "Result = SUCCESS");

        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenReturn(99);
        when(detailDao.addDetail(eq(mockConn), any(OrderDetails.class))).thenReturn(true);

        OrderResult result = orderService.placeOrder(user, cart, "123 St", "0909");

        assertEquals(OrderResult.SUCCESS, result);
        verify(mockConn).commit();
    }

    @Test
    public void testPlaceOrder_DetailFailed_Rollback() throws Exception {
        setTestCaseInfo("ORD_SVC_02", "Lỗi chi tiết -> Rollback", 
                "1. Order OK\n2. Detail FAIL", "Cart: 1 item", "Result = DETAIL_FAILED & Rollback");

        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenReturn(99);
        when(detailDao.addDetail(eq(mockConn), any(OrderDetails.class))).thenReturn(false);

        OrderResult result = orderService.placeOrder(user, cart, "ABC", "090");

        assertEquals(OrderResult.DETAIL_FAILED, result);
        verify(mockConn).rollback();
    }

    @Test
    public void testPlaceOrder_EmptyCart() {
        setTestCaseInfo("ORD_SVC_03", "Giỏ hàng rỗng", "Cart empty", "Cart = []", "Result = EMPTY_CART");
        OrderResult result = orderService.placeOrder(user, new ArrayList<>(), "A", "B");
        assertEquals(OrderResult.EMPTY_CART, result);
    }

    @Test
    public void testPlaceOrder_NotLoggedIn() {
        setTestCaseInfo("ORD_SVC_04", "Chưa đăng nhập", "User = null", "User=null", "Result = NOT_LOGGED_IN");
        OrderResult result = orderService.placeOrder(null, cart, "X", "Y");
        assertEquals(OrderResult.NOT_LOGGED_IN, result);
    }

    // --- [MỚI] Test Validation: Thiếu thông tin ---
    @Test
    public void testPlaceOrder_MissingInfo() {
        setTestCaseInfo("ORD_SVC_05", "Thiếu địa chỉ/SĐT", "Address/Phone rỗng", "Addr='', Phone=''", "Result = MISSING_INFO");
        
        OrderResult result = orderService.placeOrder(user, cart, "", ""); // Rỗng
        assertEquals(OrderResult.MISSING_INFO, result);
        
        result = orderService.placeOrder(user, cart, null, "090"); // Null
        assertEquals(OrderResult.MISSING_INFO, result);
    }

    // --- [MỚI] Test Database Error: Thêm Order thất bại ---
    @Test
    public void testPlaceOrder_OrderDaoFailed() throws Exception {
        setTestCaseInfo("ORD_SVC_06", "Lỗi lưu Order chính", "OrderDao return -1", "DAO Fail", "Result = ORDER_FAILED & Rollback");

        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenReturn(-1); // Giả lập fail

        OrderResult result = orderService.placeOrder(user, cart, "Add", "012");
        
        assertEquals(OrderResult.ORDER_FAILED, result);
        verify(mockConn).rollback();
    }

    // --- [MỚI] Test Exception (Lỗi kết nối) ---
    @Test
    public void testPlaceOrder_TransactionException() throws Exception {
        setTestCaseInfo("ORD_SVC_07", "Lỗi Exception (DB Crash)", "Throw RuntimeException", "Crash", "Result = EXCEPTION & Rollback");

        // Giả lập lỗi khi gọi commit hoặc addOrder
        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenThrow(new RuntimeException("DB Down"));

        OrderResult result = orderService.placeOrder(user, cart, "Add", "012");
        
        assertEquals(OrderResult.EXCEPTION, result);
        verify(mockConn).rollback();
    }

    // ============================================================
    //                      TEST CÁC HÀM GET (MỚI)
    // ============================================================

    @Test
    public void testGetOrdersForUser() throws Exception {
        setTestCaseInfo("ORD_SVC_08", "Lấy DS đơn của User", "Call DAO getOrdersByUserId", "UserID=1", "Return List");
        
        List<Orders> list = new ArrayList<>();
        when(orderDao.getOrdersByUserId(1)).thenReturn(list);

        List<Orders> result = orderService.getOrdersForUser(1);
        
        assertSame(list, result);
        verify(orderDao).getOrdersByUserId(1);
    }

    @Test
    public void testGetSecuredOrder() throws Exception {
        setTestCaseInfo("ORD_SVC_09", "Lấy đơn hàng bảo mật", "Call DAO getOrderByIdAndUserId", "OID=10, UID=1", "Return Order");
        
        Orders order = new Orders();
        when(orderDao.getOrderByIdAndUserId(10, 1)).thenReturn(order);

        Orders result = orderService.getSecuredOrder(10, 1);
        
        assertSame(order, result);
    }

    // ============================================================
    //                  TEST CHI TIẾT PHỨC TẠP (RICH DETAILS)
    // ============================================================

    @Test
    public void testGetRichOrderDetails_Success() throws Exception {
        setTestCaseInfo("ORD_SVC_10", "Lấy chi tiết đầy đủ", 
                "1. Get Details\n2. Get Variant & Product\n3. Map Size", 
                "Order has 1 item", "Return List Map");

        // 1. Mock Order Details
        List<OrderDetails> details = new ArrayList<>();
        OrderDetails d = new OrderDetails(); d.setProductVariantId(5); d.setPrice(100); d.setQuantity(2);
        details.add(d);
        when(detailDao.getDetailsByOrderId(99)).thenReturn(details);

        // 2. Mock Size Map
        List<Size> sizes = new ArrayList<>();
        sizes.add(new Size(1, "XL"));
        when(sizeDao.getAllSizes()).thenReturn(sizes);

        // 3. Mock Variant & Product
        ProductVariants v = new ProductVariants(); v.setProductId(10); v.setSizeId(1);
        when(variantDao.findById(5)).thenReturn(v);

        Products p = new Products(); p.setName("Ao Vip"); p.setImage("img.png");
        when(productDao.findById(10)).thenReturn(p);

        // Run
        List<Map<String, Object>> result = orderService.getRichOrderDetails(99);

        // Verify
        assertEquals(1, result.size());
        assertEquals("Ao Vip", result.get(0).get("productName"));
        assertEquals("XL", result.get(0).get("sizeLabel"));
    }

    @Test
    public void testGetRichOrderDetails_MissingVariant() throws Exception {
        setTestCaseInfo("ORD_SVC_11", "Chi tiết bị xóa (Null Variant)", 
                "Variant ID không tồn tại", "Variant=null", "Name='SP không tồn tại'");

        List<OrderDetails> details = new ArrayList<>();
        OrderDetails d = new OrderDetails(); d.setProductVariantId(999); // ID rác
        details.add(d);
        when(detailDao.getDetailsByOrderId(88)).thenReturn(details);
        when(sizeDao.getAllSizes()).thenReturn(Collections.emptyList());

        // Mock Variant trả về null
        when(variantDao.findById(999)).thenReturn(null);

        List<Map<String, Object>> result = orderService.getRichOrderDetails(88);

        assertEquals(1, result.size());
        assertEquals("Sản phẩm không còn tồn tại", result.get(0).get("productName"));
    }

    // === EXCEL EXPORT ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderService.xlsx");
    }
}