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
    // (Ta chỉ cần các biến lưu trạng thái tạm thời, không cần list finalReportData nữa)
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
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
    // CÁC TEST CASE (LOGIC GIỮ NGUYÊN)
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

    @Test
    public void testCalculateTotal() {
        setTestCaseInfo("CART_SVC_03", "Service: Tính tổng tiền", 
                "P1($100 * 2) + P2($200 * 1)", "Data setup", "Total=400.0");

        cart = cartService.addToCart(cart, p1, 2);
        cart = cartService.addToCart(cart, p2, 1);

        double total = cartService.calculateTotal(cart);
        assertEquals(400.0, total, 0.01);
    }

    @Test
    public void testRemoveFromCart() {
        setTestCaseInfo("CART_SVC_04", "Service: Xóa SP theo index", 
                "Giỏ [P1, P2] -> Xóa index 0", "Index=0", "Size=1, Còn P2");

        cart = cartService.addToCart(cart, p1, 1);
        cart = cartService.addToCart(cart, p2, 1);
        
        cartService.removeFromCart(cart, 0); // Xóa P1

        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getProductVariant().getId());
    }

    @Test
    public void testUpdateQuantity_Zero() {
        setTestCaseInfo("CART_SVC_05", "Service: Update về 0 -> Xóa", 
                "Giỏ có P1 -> Update P1 = 0", "Qty=0", "Size=0");

        cart = cartService.addToCart(cart, p1, 5);
        cartService.updateQuantity(cart, 1, 0);

        assertEquals(0, cart.size());
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT EXCEL (DÙNG CLASS TIỆN ÍCH) ===
    // ==========================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            // Gọi Utility Class để thêm kết quả PASS
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS");
        }

        @Override
        protected void failed(Throwable e, Description description) {
            // Gọi Utility Class để thêm kết quả FAIL
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        // Xuất ra file Excel (.xlsx) thay vì CSV
        ExcelTestExporter.exportToExcel("KetQuaTest_CartService.xlsx");
    }
}