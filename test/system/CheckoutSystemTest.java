// Đặt trong "Test Packages/system/"
package system;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT SELENIUM ===
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
// === IMPORT ĐỂ "CHỜ" ===
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
// ===================================

/**
 * Đây là lớp SYSTEM TEST (Kiểm thử Hệ thống) cho luồng "Thanh toán".
 * (Phiên bản chạy chậm để quan sát)
 */
public class CheckoutSystemTest {

    private WebDriver driver;
    private String baseUrl;
    private WebDriverWait wait; // Bộ "Chờ"

    // Hàm "Ngủ" (để chạy chậm)
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize(); 
        baseUrl = "http://localhost:8080/ShopDuck/";
        wait = new WebDriverWait(driver, 10); 
    }


    /**
     * Test Case: Đăng nhập -> Thêm vào giỏ -> Điền thông tin -> Đặt hàng
     */
    @Test
    public void testFullCheckoutFlow() {
        
        // --- PHẦN 1: ĐĂNG NHẬP ---
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000);
        
        driver.findElement(By.name("username")).sendKeys("user");
        driver.findElement(By.name("password")).sendKeys("user123");
        driver.findElement(By.id("loginButton")).click();
        wait.until(ExpectedConditions.urlContains("user/view-products"));
        
        
        // --- PHẦN 2: THÊM VÀO GIỎ HÀNG ---
        try {
            // Nhấp vào sản phẩm đầu tiên
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".card a"))).click();
            sleep(1000);
            
            // Nhấp nút Thêm vào giỏ
            wait.until(ExpectedConditions.elementToBeClickable(By.className("btn-add-cart"))).click();
            sleep(1000);
            
            // Chờ thông báo thành công
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
            
        } catch (Exception e) {
            fail("Lỗi ở bước Thêm vào giỏ. Test thất bại. " + e.getMessage());
            return;
        }

        // --- PHẦN 3: ĐI ĐẾN TRANG THANH TOÁN ---
        try {
            // Tìm nút "Giỏ hàng" 
            WebElement cartLink = driver.findElement(By.cssSelector("a[href*='view-cart.jsp']"));
            cartLink.click();
            
            // Chờ trang giỏ hàng (view-cart.jsp) tải
            wait.until(ExpectedConditions.urlContains("user/view-cart.jsp"));
            sleep(1000);
            
            // Tìm nút "Thanh toán" 
            WebElement checkoutButton = driver.findElement(By.className("cart-btn-success"));
            checkoutButton.click();
            
            // Chờ trang thanh toán (Checkout.jsp) tải
            wait.until(ExpectedConditions.urlContains("user/checkout")); 
            sleep(1000);

        } catch (Exception e) {
            fail("Lỗi ở bước đi đến trang thanh toán. " + e.getMessage());
            return;
        }
        
        // --- PHẦN 4: ĐIỀN FORM VÀ ĐẶT HÀNG ---
        try {
            WebElement addressInput = driver.findElement(By.name("address"));
            WebElement phoneInput = driver.findElement(By.name("phone"));
            
            // Tìm nút "Xác nhận đặt hàng" 
            WebElement confirmButton = driver.findElement(By.cssSelector("button.btn-confirm"));

            // Điền thông tin
            addressInput.sendKeys("123 Đường Test, Quận 1, TP.HCM");
            sleep(500);
            phoneInput.sendKeys("0987654321");
            sleep(1000);
            
            // Nhấn nút Xác nhận
            confirmButton.click();
            
        } catch (Exception e) {
            fail("Lỗi ở bước điền form. " + e.getMessage());
            return;
        }
        
        // --- PHẦN 5: XÁC MINH KẾT QUẢ (ĐÃ SỬA) ---
        try {
            // Sửa 1: Chờ trang đích thực tế của ứng dụng (view-products)
            wait.until(ExpectedConditions.urlContains("user/view-products?success=true"));
            sleep(1000);

            // Sửa 2: Tìm thông báo thành công trên trang view-products
            WebElement successAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("alert-success"))
            );
            
            assertTrue("Phải hiển thị thông báo đặt hàng thành công",
                        successAlert.getText().contains("Đặt hàng thành công"));
            
        } catch (Exception e) {
            // Nếu không tìm thấy, báo lỗi và in ra URL thực tế
            fail("Đặt hàng thất bại. Không tìm thấy thông báo 'Đặt hàng thành công' trên trang đích. URL hiện tại: " + driver.getCurrentUrl());
        }
    }


    // === CHẠY SAU KHI TẤT CẢ TEST HOÀN THÀNH ===
    @After
    public void tearDown() {
        sleep(2000); 
        
        if (driver != null) {
            driver.quit();
        }
    }
}