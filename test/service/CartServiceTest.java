// Đặt trong "Test Packages/service/"
package service;

// === IMPORT CƠ BẢN ===
import entity.CartBean;
import entity.ProductVariants;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT ĐỂ TẠO BÁO CÁO (ĐÃ THÊM VÀO) ===
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp Unit Test cho CartService.
 * Nó kiểm tra logic nghiệp vụ của giỏ hàng (thêm, xóa, sửa, tính toán).
 */
public class CartServiceTest {

    // Đối tượng service mà chúng ta sẽ test
    private CartService cartService;
    
    // Giỏ hàng (dùng chung cho các test)
    private List<CartBean> cart;
    
    // Các sản phẩm "giả" để test
    private ProductVariants fakeProduct1;
    private ProductVariants fakeProduct2;

    // 3. Hàm chạy trước mỗi @Test
    @Before
    public void setUp() {
        // Khởi tạo service
        cartService = new CartService();
        
        // Khởi tạo giỏ hàng rỗng
        cart = new ArrayList<>();
        
        // Tạo sản phẩm 1 (ID 1, Giá 10.0)
        fakeProduct1 = new ProductVariants();
        fakeProduct1.setId(1);
        fakeProduct1.setPrice(10.0);
        
        // Tạo sản phẩm 2 (ID 2, Giá 5.0)
        fakeProduct2 = new ProductVariants();
        fakeProduct2.setId(2);
        fakeProduct2.setPrice(5.0);
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    /**
     * TC1: Thêm sản phẩm MỚI vào giỏ hàng.
     */
    @Test
    public void testAddToCart_NewItem() {
        // --- ARRANGE --- (Giỏ hàng đang rỗng)
        
        // --- ACT ---
        // Thêm 2 sản phẩm 1
        cart = cartService.addToCart(cart, fakeProduct1, 2);

        // --- ASSERT ---
        assertEquals("Giỏ hàng phải có 1 sản phẩm", 1, cart.size());
        assertEquals("Sản phẩm phải có số lượng là 2", 2, cart.get(0).getQuantity());
        assertEquals("Sản phẩm phải là fakeProduct1", 1, cart.get(0).getProductVariant().getId());
    }
    
    /**
     * TC1 (Mở rộng): Thêm sản phẩm ĐÃ CÓ vào giỏ hàng.
     */
    @Test
    public void testAddToCart_ExistingItem() {
        // --- ARRANGE ---
        // Thêm 2 sản phẩm 1 vào trước
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        
        // --- ACT ---
        // Thêm 3 sản phẩm 1 NỮA
        cart = cartService.addToCart(cart, fakeProduct1, 3);

        // --- ASSERT ---
        assertEquals("Giỏ hàng vẫn chỉ có 1 loại sản phẩm", 1, cart.size());
        assertEquals("Số lượng phải được cộng dồn (2 + 3 = 5)", 5, cart.get(0).getQuantity());
    }

    /**
     * TC1 (Mở rộng): Thêm sản phẩm khác vào giỏ hàng.
     */
    @Test
    public void testAddToCart_MultipleItems() {
        // --- ARRANGE ---
        cart = cartService.addToCart(cart, fakeProduct1, 2); // 2 x SP1
        
        // --- ACT ---
        cart = cartService.addToCart(cart, fakeProduct2, 1); // 1 x SP2

        // --- ASSERT ---
        assertEquals("Giỏ hàng phải có 2 loại sản phẩm", 2, cart.size());
    }
    
    /**
     * TC1 (Mở rộng): Thêm sản phẩm vào giỏ hàng bị null.
     */
    @Test
    public void testAddToCart_NullCart() {
        // --- ACT ---
        // Gọi hàm với cart = null
        List<CartBean> newCart = cartService.addToCart(null, fakeProduct1, 1);
        
        // --- ASSERT ---
        assertNotNull("Giỏ hàng phải được tự động tạo mới", newCart);
        assertEquals("Giỏ hàng mới phải có 1 sản phẩm", 1, newCart.size());
    }

    /**
     * TC3: Cập nhật số lượng sản phẩm.
     */
    @Test
    public void testUpdateQuantity_Positive() {
        // --- ARRANGE ---
        cart = cartService.addToCart(cart, fakeProduct1, 2); // 2 x SP1
        
        // --- ACT ---
        // Cập nhật SP1 (ID 1) lên số lượng 5
        cartService.updateQuantity(cart, 1, 5);
        
        // --- ASSERT ---
        assertEquals(1, cart.size());
        assertEquals("Số lượng phải được cập nhật thành 5", 5, cart.get(0).getQuantity());
    }
    
    /**
     * TC2 (Biến thể): Xóa sản phẩm bằng cách cập nhật số lượng về 0.
     */
    @Test
    public void testUpdateQuantity_RemoveByZero() {
        // --- ARRANGE ---
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        
        // --- ACT ---
        // Cập nhật SP1 (ID 1) về số lượng 0
        cartService.updateQuantity(cart, 1, 0);
        
        // --- ASSERT ---
        assertEquals("Sản phẩm phải bị xóa khỏi giỏ", 0, cart.size());
    }
    
    /**
     * TC2 (Biến thể): Xóa sản phẩm bằng cách cập nhật số lượng âm.
     */
    @Test
    public void testUpdateQuantity_RemoveByNegative() {
        // --- ARRANGE ---
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        
        // --- ACT ---
        cartService.updateQuantity(cart, 1, -5); // Cập nhật số lượng âm
        
        // --- ASSERT ---
        assertEquals("Sản phẩm phải bị xóa khỏi giỏ", 0, cart.size());
    }
    
    /**
     * TC2: Xóa sản phẩm khỏi giỏ (theo index).
     */
    @Test
    public void testRemoveFromCart_ByIndex() {
        // --- ARRANGE ---
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        cart = cartService.addToCart(cart, fakeProduct2, 1);
        // Giỏ hàng: [SP1, SP2]
        
        // --- ACT ---
        cartService.removeFromCart(cart, 0); // Xóa SP1 ở vị trí 0
        
        // --- ASSERT ---
        assertEquals("Giỏ hàng chỉ còn 1 sản phẩm", 1, cart.size());
        assertEquals("Sản phẩm còn lại phải là SP2", 2, cart.get(0).getProductVariant().getId());
    }

    /**
     * Test logic tính tổng tiền giỏ hàng.
     */
    @Test
    public void testCalculateTotal() {
        // --- ARRANGE ---
        // Thêm 2 sản phẩm 1 (2 * 10.0 = 20.0)
        cart = cartService.addToCart(cart, fakeProduct1, 2);
        // Thêm 3 sản phẩm 2 (3 * 5.0 = 15.0)
        cart = cartService.addToCart(cart, fakeProduct2, 3);
        
        // --- ACT ---
        double total = cartService.calculateTotal(cart);
        
        // --- ASSERT ---
        // 20.0 + 15.0 = 35.0
        // Dùng delta (0.001) để so sánh số double
        assertEquals("Tổng tiền phải là 35.0", 35.0, total, 0.001);
    }
    
    /**
     * Test logic tính tổng tiền khi giỏ hàng rỗng.
     */
    @Test
    public void testCalculateTotal_EmptyCart() {
        // --- ARRANGE --- (Giỏ hàng rỗng)
        
        // --- ACT ---
        double total = cartService.calculateTotal(cart);
        
        // --- ASSERT ---
        assertEquals("Tổng tiền phải là 0.0", 0.0, total, 0.001);
    }

    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (GIỮ NGUYÊN) ===
    // =================================================================

    // 1. Danh sách lưu kết quả (mỗi phần tử là 1 mảng 4 cột)
    private static final List<String[]> testResults = new ArrayList<>();

    // 2. Sử dụng @Rule và TestWatcher để "theo dõi" từng test case
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        
        private String getModuleName(Description d) {
            String className = d.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);
        }

        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "PASS",
                ""
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMessage = (e == null) ? "Unknown Error" : e.getMessage();
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "FAIL",
                errorMessage
            });
        }
    };

    // 3. Sử dụng @AfterClass để in bảng và ghi ra file
    @AfterClass
    public static void writeTestReport() {
        String filePath = "test-report.txt";
        
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        
        // Sửa lỗi .repeat() cho Java 8
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'),  
            new String(new char[30]).replace('\0', '-')
        );
        separator = separator.replace("|", "+");


        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath, true), StandardCharsets.UTF_8)) { 
            
            writer.write("\n--- Kết quả chạy " + CartServiceTest.class.getName() + " ---\n");
            
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                
                System.out.print(line);
                writer.write(line);
            }
            
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được NỐI VÀO file: " + filePath);

        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());
        }
    }
}