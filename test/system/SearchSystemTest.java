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
import java.util.List; 
import java.util.concurrent.TimeUnit;
// === IMPORT ĐỂ "CHỜ" ===
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
// ===================================

/**
 * Đây là lớp SYSTEM TEST (Kiểm thử Hệ thống) cho luồng "Tìm kiếm sản phẩm".
 * (Phiên bản chạy chậm để quan sát)
 */
public class SearchSystemTest {

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
     * Test Case 1: Người dùng tìm kiếm bằng thanh SEARCH BAR
     */
    @Test
    public void testSearch_BySearchBar_ProductExists() {
        
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); 
        
        WebElement searchInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("keyword"))
        );
        WebElement searchButton = driver.findElement(By.cssSelector("form[action*='/user/search-products'] button[type='submit']"));
        
        String keyword = "Áo Thun"; 
        searchInput.sendKeys(keyword);
        sleep(1000); 
        searchButton.click();
        
        // --- PHẦN 3: XÁC MINH KẾT QUẢ ---
        try {
            // Chờ cho đến khi THẺ SẢN PHẨM (card) đầu tiên xuất hiện
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("card")));
            sleep(1000); 
            
            List<WebElement> results = driver.findElements(By.className("card"));
            assertTrue("Phải tìm thấy ít nhất 1 sản phẩm", results.size() > 0);
            
        } catch (Exception e) {
            fail("Test tìm kiếm thất bại. Không tìm thấy sản phẩm (class='card'). Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 2: Người dùng tìm kiếm bằng LINK MENU
     */
    @Test
    public void testSearch_ByMenuLink() {
        
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); 
        
        try {
            WebElement collectionMenu = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("BỘ SƯU TẬP"))
            );
            collectionMenu.click();
            sleep(1000); 
            
            WebElement hoodieLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Áo Hoodie"))
            );
            hoodieLink.click();
            
        } catch (Exception e) {
            fail("Không tìm thấy link 'BỘ SƯU TẬP' hoặc 'Áo Hoodie'. Lỗi: " + e.getMessage());
            return;
        }
        
        // --- PHẦN 3: XÁC MINH KẾT QUẢ ---
        try {
            // Chờ cho đến khi THẺ SẢN PHẨM (card) đầu tiên xuất hiện
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("card")));
            sleep(1000); 
            
            List<WebElement> results = driver.findElements(By.className("card"));
            assertTrue("Phải tìm thấy ít nhất 1 Áo Hoodie", results.size() > 0);
            
        } catch (Exception e) {
            fail("Test tìm kiếm (bằng menu) thất bại. Lỗi: " + e.getMessage());
        }
    }

    /**
     * Test Case 3: Người dùng tìm kiếm KHÔNG TÌM THẤY (Kết quả rỗng)
     */
    @Test
    public void testSearch_ProductNotFound() {
        
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); 
        
        WebElement searchInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("keyword"))
        );
        WebElement searchButton = driver.findElement(By.cssSelector("form[action*='/user/search-products'] button[type='submit']"));
        
        String keyword = "MOTSANPHAMKHONGTONTAI12345";
        
        searchInput.sendKeys(keyword);
        sleep(1000); 
        searchButton.click();
        
        // --- PHẦN 2: XÁC MINH KẾT QUẢ ---
        try {
            // Chờ thông báo lỗi xuất hiện
            WebElement noResultAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("alert-warning"))
            );
            sleep(1000); 
            
            assertTrue("Phải hiển thị thông báo không tìm thấy", 
                       noResultAlert.getText().contains("Không tìm thấy sản phẩm"));
            
        } catch (Exception e) {
            fail("Không tìm thấy thông báo 'Không tìm thấy sản phẩm' (class='alert-warning'). Lỗi: " + e.getMessage());
        }
    }

    // =================================================================
    // === TEST CASE MỚI (TÌM KIẾM RỖNG) ===
    // =================================================================
    /**
     * Test Case 4: Người dùng tìm kiếm RỖNG (Không nhập gì)
     */
    @Test
    public void testSearch_EmptyKeyword_ShouldReturnAllProducts() {
        
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); 
        
        // 1. Tìm ô tìm kiếm
        WebElement searchInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("keyword"))
        );
        WebElement searchButton = driver.findElement(By.cssSelector("form[action*='/user/search-products'] button[type='submit']"));
        
        // --- PHẦN 2: THỰC HIỆN TÌM KIẾM ---
        // Không .sendKeys() (để trống)
        sleep(1000); 
        searchButton.click();
        
        // --- PHẦN 3: XÁC MINH KẾT QUẢ ---
        try {
            // 1. Chờ cho đến khi trang kết quả tải xong (keyword=)
            wait.until(ExpectedConditions.urlContains("search-products?keyword="));
            
            // 2. Chờ cho đến khi THẺ SẢN PHẨM (card) đầu tiên xuất hiện
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("card")));
            sleep(1000); 
            
            // 3. Lấy tất cả sản phẩm
            List<WebElement> results = driver.findElements(By.className("card"));
            
            // 4. Xác minh (Giả sử tìm rỗng sẽ ra nhiều hơn 1 sản phẩm)
            assertTrue("Tìm kiếm rỗng phải trả về nhiều hơn 1 sản phẩm", results.size() > 1);
            
        } catch (Exception e) {
            fail("Test tìm kiếm rỗng thất bại. Không tìm thấy sản phẩm (class='card'). Lỗi: " + e.getMessage());
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