package service;

// === IMPORT CƠ BẢN ===
import entity.CartBean;
import entity.ProductVariants;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT ĐỂ TẠO BÁO CÁO EXCEL/CSV ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Unit Test cho CartService (Chức năng Giỏ hàng).
 * Xuất kết quả ra file Excel (CSV).
 */
public class CartServiceTest {

    private CartService cartService;
    private List<CartBean> cart;
    private ProductVariants fakeProduct1;
    private ProductVariants fakeProduct2;

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách lưu kết quả để xuất Excel
    private static final List<String[]> finalReportData = new ArrayList<>();

    @Before
    public void setUp() {
        // 1. Khởi tạo Service và Data giả
        cartService = new CartService();
        cart = new ArrayList<>();
        
        fakeProduct1 = new ProductVariants();
        fakeProduct1.setId(1);
        fakeProduct1.setPrice(10.0);
        
        fakeProduct2 = new ProductVariants();
        fakeProduct2.setId(2);
        fakeProduct2.setPrice(5.0);
    }

    // Hàm điền thông tin Test Case (Gọi đầu tiên trong mỗi @Test)
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // ==========================================================
    // === CÁC TEST CASE (Đã thêm thông tin báo cáo) ===
    // ==========================================================

    @Test
    public void testAddToCart_NewItem() {
        setTestCaseInfo(
            "CART_01", 
            "Thêm mới sản phẩm vào giỏ", 
            "1. Giỏ rỗng\n2. Thêm SP1 (SL: 2)", 
            "SP1 (ID=1, Giá=10)\nSố lượng: 2", 
            "Giỏ có 1 SP, SL=2"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);

        assertEquals("Giỏ hàng phải có 1 sản phẩm", 1, cart.size());
        assertEquals("Sản phẩm phải có số lượng là 2", 2, cart.get(0).getQuantity());
        assertEquals("ID sản phẩm đúng", 1, cart.get(0).getProductVariant().getId());
    }
    
    @Test
    public void testAddToCart_ExistingItem() {
        setTestCaseInfo(
            "CART_02", 
            "Cộng dồn số lượng sản phẩm cũ", 
            "1. Có sẵn SP1 (SL: 2)\n2. Thêm tiếp SP1 (SL: 3)", 
            "SP1\nSố lượng thêm: 3", 
            "Giỏ vẫn 1 SP, SL tổng=5"
        );

        // Arrange
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        
        // Act
        cart = cartService.addToCart(cart, fakeProduct1, 3);

        // Assert
        assertEquals(1, cart.size());
        assertEquals(5, cart.get(0).getQuantity());
    }

    @Test
    public void testAddToCart_MultipleItems() {
        setTestCaseInfo(
            "CART_03", 
            "Thêm nhiều loại sản phẩm khác nhau", 
            "1. Có SP1\n2. Thêm SP2", 
            "SP1 (SL:2)\nSP2 (SL:1)", 
            "Giỏ có 2 dòng sản phẩm"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cart = cartService.addToCart(cart, fakeProduct2, 1);

        assertEquals(2, cart.size());
    }
    
    @Test
    public void testAddToCart_NullCart() {
        setTestCaseInfo(
            "CART_04", 
            "Xử lý khi giỏ hàng bị Null", 
            "Truyền vào list giỏ hàng là null", 
            "Cart = null\nSP1, SL:1", 
            "Tự khởi tạo giỏ mới"
        );

        List<CartBean> newCart = cartService.addToCart(null, fakeProduct1, 1);
        
        assertNotNull(newCart);
        assertEquals(1, newCart.size());
    }

    @Test
    public void testUpdateQuantity_Positive() {
        setTestCaseInfo(
            "CART_05", 
            "Cập nhật số lượng (Tăng/Giảm)", 
            "Update ID 1 thành 5", 
            "ID: 1\nNew Qty: 5", 
            "Qty cập nhật thành 5"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cartService.updateQuantity(cart, 1, 5);
        
        assertEquals(1, cart.size());
        assertEquals(5, cart.get(0).getQuantity());
    }
    
    @Test
    public void testUpdateQuantity_RemoveByZero() {
        setTestCaseInfo(
            "CART_06", 
            "Xóa SP khi cập nhật số lượng về 0", 
            "Update ID 1 thành 0", 
            "ID: 1\nNew Qty: 0", 
            "Sản phẩm bị xóa khỏi giỏ"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cartService.updateQuantity(cart, 1, 0);
        
        assertEquals(0, cart.size());
    }
    
    @Test
    public void testUpdateQuantity_RemoveByNegative() {
        setTestCaseInfo(
            "CART_07", 
            "Xóa SP khi cập nhật số lượng âm", 
            "Update ID 1 thành -5", 
            "ID: 1\nNew Qty: -5", 
            "Sản phẩm bị xóa khỏi giỏ"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cartService.updateQuantity(cart, 1, -5);
        
        assertEquals(0, cart.size());
    }
    
    @Test
    public void testRemoveFromCart_ByIndex() {
        setTestCaseInfo(
            "CART_08", 
            "Xóa sản phẩm theo vị trí (Index)", 
            "Xóa phần tử tại index 0", 
            "Giỏ: [SP1, SP2]\nXóa index 0", 
            "Còn lại SP2"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cart = cartService.addToCart(cart, fakeProduct2, 1);
        
        cartService.removeFromCart(cart, 0);
        
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getProductVariant().getId());
    }

    @Test
    public void testCalculateTotal() {
        setTestCaseInfo(
            "CART_09", 
            "Tính tổng tiền giỏ hàng", 
            "2 SP1 ($10) + 3 SP2 ($5)", 
            "2*10 + 3*5", 
            "Tổng = 35.0"
        );

        cart = cartService.addToCart(cart, fakeProduct1, 2); // 20.0
        cart = cartService.addToCart(cart, fakeProduct2, 3); // 15.0
        
        double total = cartService.calculateTotal(cart);
        
        assertEquals(35.0, total, 0.001);
    }
    
    @Test
    public void testCalculateTotal_EmptyCart() {
        setTestCaseInfo(
            "CART_10", 
            "Tính tiền khi giỏ rỗng", 
            "Giỏ không có sp nào", 
            "Cart empty", 
            "Tổng = 0.0"
        );

        double total = cartService.calculateTotal(cart);
        assertEquals(0.0, total, 0.001);
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT FILE EXCEL (CSV) ===
    // ==========================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, currentExpected, "PASS"
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMsg = (e != null) ? e.getMessage() : "Unknown Error";
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, errorMsg, "FAIL"
            });
        }
    };

    @AfterClass
    public static void exportToExcelCSV() {
        String fileName = "KetQuaTest_GioHang.csv"; // Tên file riêng cho Cart
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo Giỏ Hàng ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            // Thêm BOM để Excel hiển thị tiếng Việt đúng
            writer.write('\ufeff');

            // Header
            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            // Data
            for (String[] row : finalReportData) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialChars(row[0]),
                        escapeSpecialChars(row[1]),
                        escapeSpecialChars(row[2]),
                        escapeSpecialChars(row[3]),
                        escapeSpecialChars(row[4]),
                        escapeSpecialChars(row[5]),
                        escapeSpecialChars(row[6])
                );
                writer.println(line);
            }
            
            System.out.println("XONG! File 'KetQuaTest_GioHang.csv' đã được tạo.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}