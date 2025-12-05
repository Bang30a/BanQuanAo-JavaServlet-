package unit;

// === IMPORT LOGIC ===
import entity.CartBean;
import entity.ProductVariants;
import service.CartService;
import util.ExcelTestExporter; // <--- IMPORT TIỆN ÍCH EXCEL MỚI

// === IMPORT TEST ===
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT JUNIT RULES ===
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class CartServiceTest {

    private CartService cartService;
    private List<CartBean> cart;
    private ProductVariants p1;
    private ProductVariants p2;

    // === CẤU HÌNH BÁO CÁO ===
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
    public void setUp() {
        cartService = new CartService();
        cart = new ArrayList<>();
        
        // Tạo dữ liệu giả
        p1 = new ProductVariants(); p1.setId(1); p1.setPrice(100.0);
        p2 = new ProductVariants(); p2.setId(2); p2.setPrice(200.0);
    }

    // ==========================================
    // 1. TEST ADD TO CART
    // ==========================================

    @Test
    public void testAddToCart_New() {
        setTestCaseInfo("CART_SVC_01", "Service: Thêm mới SP", 
                "Giỏ rỗng -> Thêm P1 (SL: 2)", "P1, Qty=2", "Size=1, Qty=2");

        cart = cartService.addToCart(cart, p1, 2);

        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getQuantity());
        assertEquals(1, cart.get(0).getProductVariant().getId());
    }

    @Test
    public void testAddToCart_Existing() {
        setTestCaseInfo("CART_SVC_02", "Service: Cộng dồn SP cũ", 
                "Giỏ có P1(2) -> Thêm P1(3)", "P1, Qty=3", "Size=1, Qty=5");

        cart = cartService.addToCart(cart, p1, 2);
        cart = cartService.addToCart(cart, p1, 3);

        assertEquals(1, cart.size());
        assertEquals(5, cart.get(0).getQuantity());
    }

    // [MỚI] Test thêm sản phẩm Null
    @Test
    public void testAddToCart_NullVariant() {
        setTestCaseInfo("CART_SVC_03", "Service: Thêm SP Null", 
                "Input Variant = null", "Variant=null", "Size không đổi");

        cart = cartService.addToCart(cart, null, 1);
        assertEquals(0, cart.size());
    }

    // [MỚI] Test giỏ hàng Null (tự khởi tạo)
    @Test
    public void testAddToCart_NullCart() {
        setTestCaseInfo("CART_SVC_04", "Service: Giỏ hàng Null", 
                "Input Cart = null", "Cart=null", "Tự tạo List mới, Size=1");

        List<CartBean> newCart = cartService.addToCart(null, p1, 1);
        assertNotNull(newCart);
        assertEquals(1, newCart.size());
    }

    // ==========================================
    // 2. TEST UPDATE QUANTITY
    // ==========================================

    @Test
    public void testUpdateQuantity_Zero() {
        setTestCaseInfo("CART_SVC_05", "Service: Update về 0 -> Xóa", 
                "Giỏ có P1 -> Update P1 = 0", "Qty=0", "Size=0");

        cart = cartService.addToCart(cart, p1, 5);
        cartService.updateQuantity(cart, 1, 0);

        assertEquals(0, cart.size());
    }

    // [MỚI] Test Update số lượng bình thường
    @Test
    public void testUpdateQuantity_Normal() {
        setTestCaseInfo("CART_SVC_06", "Service: Update số lượng", 
                "Giỏ có P1(1) -> Update P1 = 5", "Qty=5", "Qty=5");

        cart = cartService.addToCart(cart, p1, 1);
        cartService.updateQuantity(cart, 1, 5);

        assertEquals(5, cart.get(0).getQuantity());
    }

    // [MỚI] Test Update sản phẩm không tồn tại
    @Test
    public void testUpdateQuantity_NotFound() {
        setTestCaseInfo("CART_SVC_07", "Service: Update ID không có", 
                "Giỏ có P1(1) -> Update ID=99", "ID=99", "Không thay đổi");

        cart = cartService.addToCart(cart, p1, 1);
        cartService.updateQuantity(cart, 99, 5); // ID 99 không có

        assertEquals(1, cart.get(0).getQuantity()); // Vẫn là 1
    }

    // ==========================================
    // 3. TEST REMOVE & CLEAR
    // ==========================================

    @Test
    public void testRemoveFromCart() {
        setTestCaseInfo("CART_SVC_08", "Service: Xóa SP theo index", 
                "Giỏ [P1, P2] -> Xóa index 0", "Index=0", "Size=1, Còn P2");

        cart = cartService.addToCart(cart, p1, 1);
        cart = cartService.addToCart(cart, p2, 1);
        
        cartService.removeFromCart(cart, 0); // Xóa P1

        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getProductVariant().getId());
    }

    // [MỚI] Test Xóa index sai
    @Test
    public void testRemoveFromCart_InvalidIndex() {
        setTestCaseInfo("CART_SVC_09", "Service: Xóa Index sai", 
                "Giỏ [P1] -> Xóa index 10", "Index=10", "Size không đổi");

        cart = cartService.addToCart(cart, p1, 1);
        cartService.removeFromCart(cart, 10); // Index quá lớn
        cartService.removeFromCart(cart, -1); // Index âm

        assertEquals(1, cart.size());
    }

    // [MỚI] Test Xóa tất cả (Clear)
    @Test
    public void testClearCart() {
        setTestCaseInfo("CART_SVC_10", "Service: Xóa toàn bộ", 
                "Giỏ [P1, P2] -> Clear", "Clear", "Size=0");

        cart = cartService.addToCart(cart, p1, 1);
        cart = cartService.addToCart(cart, p2, 1);
        
        cartService.clearCart(cart);

        assertEquals(0, cart.size());
    }

    // ==========================================
    // 4. TEST CALCULATE TOTAL
    // ==========================================

    @Test
    public void testCalculateTotal() {
        setTestCaseInfo("CART_SVC_11", "Service: Tính tổng tiền", 
                "P1($100 * 2) + P2($200 * 1)", "Data setup", "Total=400.0");

        cart = cartService.addToCart(cart, p1, 2);
        cart = cartService.addToCart(cart, p2, 1);

        double total = cartService.calculateTotal(cart);
        assertEquals(400.0, total, 0.01);
    }

    // [MỚI] Test tính tiền giỏ rỗng
    @Test
    public void testCalculateTotal_Empty() {
        setTestCaseInfo("CART_SVC_12", "Service: Tính tiền giỏ rỗng", 
                "Giỏ rỗng hoặc null", "Empty", "Total=0.0");

        double totalEmpty = cartService.calculateTotal(new ArrayList<>());
        double totalNull = cartService.calculateTotal(null);

        assertEquals(0.0, totalEmpty, 0.01);
        assertEquals(0.0, totalNull, 0.01);
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT EXCEL (DÙNG CLASS TIỆN ÍCH) ===
    // ==========================================================

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
        ExcelTestExporter.exportToExcel("KetQuaTest_CartService.xlsx");
    }
}