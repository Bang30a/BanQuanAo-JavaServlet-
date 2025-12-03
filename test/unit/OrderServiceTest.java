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
    //                     TEST CASES
    // ============================================================

    @Test
    public void testPlaceOrder_Success() throws Exception {
        setTestCaseInfo(
                "ORD_SVC_01",
                "Đặt hàng thành công",
                "1. Add Order OK\n2. Add Detail OK\n3. Commit",
                "Cart: 1 item",
                "Result = SUCCESS"
        );

        // Mock
        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenReturn(99);
        when(detailDao.addDetail(eq(mockConn), any(OrderDetails.class))).thenReturn(true);

        // Run
        OrderResult result = orderService.placeOrder(user, cart, "123 St", "0909");

        // Verify
        assertEquals(OrderResult.SUCCESS, result);
        verify(mockConn).commit();
        verify(mockConn, never()).rollback();
    }

    @Test
    public void testPlaceOrder_DetailFailed_Rollback() throws Exception {
        setTestCaseInfo(
                "ORD_SVC_02",
                "Lỗi chi tiết -> Rollback",
                "1. Order OK\n2. Detail FAIL",
                "Cart: 1 item",
                "Result = DETAIL_FAILED & rollback"
        );

        when(orderDao.addOrder(eq(mockConn), any(Orders.class))).thenReturn(99);
        when(detailDao.addDetail(eq(mockConn), any(OrderDetails.class))).thenReturn(false);

        OrderResult result = orderService.placeOrder(user, cart, "ABC", "090");

        assertEquals(OrderResult.DETAIL_FAILED, result);
        verify(mockConn).rollback();
        verify(mockConn, never()).commit();
    }

    @Test
    public void testPlaceOrder_EmptyCart() {
        setTestCaseInfo(
                "ORD_SVC_03",
                "Giỏ hàng rỗng",
                "Cart empty",
                "Cart = []",
                "Result = EMPTY_CART"
        );

        OrderResult result = orderService.placeOrder(user, new ArrayList<>(), "A", "B");
        assertEquals(OrderResult.EMPTY_CART, result);
    }

    @Test
    public void testPlaceOrder_NotLoggedIn() {
        setTestCaseInfo(
                "ORD_SVC_04",
                "Chưa đăng nhập",
                "User = null",
                "User=null",
                "Result = NOT_LOGGED_IN"
        );

        OrderResult result = orderService.placeOrder(null, cart, "X", "Y");
        assertEquals(OrderResult.NOT_LOGGED_IN, result);
    }

    // ============================================================
    //                    EXCEL EXPORT CONFIG
    // ============================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps,
                    currentData, currentExpected, "OK", "PASS"
            );
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps,
                    currentData, currentExpected, e.getMessage(), "FAIL"
            );
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_OrderService.xlsx");
    }
}
