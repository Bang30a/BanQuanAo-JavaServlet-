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
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeOptions;
// === IMPORT THÊM ĐỂ TẮT POP-UP ===
import java.util.HashMap;
import java.util.Map;
// ===================================
// === IMPORT ĐỂ "CHỜ" ===
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
// ===================================

/**
 * Đây là lớp SYSTEM TEST (Kiểm thử Hệ thống) cho luồng "Thêm vào giỏ hàng".
 * (Phiên bản chạy chậm để quan sát)
 */
public class AddToCartSystemTest {

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
        // 1. Chỉ đường đến file chromedriver.exe
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        
        // 2. Tạo Tùy chọn (Options)
        ChromeOptions options = new ChromeOptions();
        
        // (Đây là code tắt pop-up bạn đã thêm - Rất tốt!)
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // =================================================================
        // === ĐÃ SỬA LỖI Ở ĐÂY ===
        // =================================================================
        // 3. Khởi tạo trình duyệt VỚI TÙY CHỌN (options)
        // (Xóa dòng "driver = new ChromeDriver();" cũ)
        driver = new ChromeDriver(options); 
        
        driver.manage().window().maximize(); // Mở toàn màn hình
        
        // 4. Đặt URL gốc của trang web
        baseUrl = "http://localhost:8080/ShopDuck/";
        
        // 5. Khởi tạo bộ "Chờ" (chờ tối đa 10 giây)
        wait = new WebDriverWait(driver, 10); 
    }

    /**
     * Test Case: Người dùng đăng nhập, thêm 1 sản phẩm vào giỏ,
     * và kiểm tra xem thông báo thành công có hiển thị không.
     */
    @Test
    public void testUserCanAddToCart() {
        
        // --- PHẦN 1: ĐĂNG NHẬP ---
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); // Dừng 1 giây
        
        driver.findElement(By.name("username")).sendKeys("user");
        sleep(500);
        driver.findElement(By.name("password")).sendKeys("user123");
        sleep(1000);
        driver.findElement(By.id("loginButton")).click();

        // Chờ cho đến khi đăng nhập thành công và vào trang sản phẩm
        wait.until(ExpectedConditions.urlContains("user/view-products"));
        sleep(1000); // Dừng 1 giây xem trang sản phẩm
        
        
        // --- PHẦN 2: CHỌN SẢN PHẨM ---
        WebElement firstProductLink;
        try {
            firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".card a"))
            );
        } catch (Exception e) {
            fail("Không tìm thấy sản phẩm nào trên trang. (Không tìm thấy .card a)");
            return;
        }
        
        firstProductLink.click(); // Click vào sản phẩm đầu tiên
        
        
        // --- PHẦN 3: THÊM VÀO GIỎ HÀNG ---
        WebElement addToCartButton;
        try {
            // Chờ trang chi tiết tải
            addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.className("btn-add-cart"))
            );
            sleep(1000); // Dừng 1 giây xem trang chi tiết
        } catch (Exception e) {
            fail("Không tìm thấy nút 'Thêm vào giỏ' (class='btn-add-cart') trên trang chi tiết.");
            return;
        }
        
        addToCartButton.click(); // Nhấn nút thêm
        
        
        // --- PHẦN 4: XÁC MINH KẾT QUẢ ---
        try {
            // Chờ cho đến khi thẻ "alert-success" XUẤT HIỆN
            WebElement successAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("alert-success"))
            );
            sleep(1000); // Dừng 1 giây xem thông báo
            
            String successText = successAlert.getText();
            
            // Sửa lại text để khớp với AddToCartServlet (dòng 66)
            assertTrue("Nội dung thông báo không đúng", 
                       successText.contains("Đã thêm sản phẩm vào giỏ hàng"));
            
        } catch (Exception e) {
            fail("Đã thêm vào giỏ nhưng không tìm thấy thông báo 'alert-success'.");
        }
    }

    // === CHẠY SAU KHI TẤT CẢ TEST HOÀN THÀNH ===
    @After
    public void tearDown() {
        // Dừng 2 giây cuối cùng để xem kết quả trước khi tắt
        sleep(2000); 
        
        if (driver != null) {
            driver.quit();
        }
    }
}