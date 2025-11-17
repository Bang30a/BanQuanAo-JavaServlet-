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
// === IMPORT THÊM ĐỂ TẮT POP-UP ===
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.Map;
// ===================================
import java.util.concurrent.TimeUnit;
// === IMPORT ĐỂ "CHỜ" ===asdasdasd  
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
// ===================================

/**
 * Đây là lớp SYSTEM TEST (Kiểm thử Hệ thống) cho luồng Đăng nhập.
 * (Phiên bản chạy chậm để quan sát)
 */
public class LoginSystemTest {

    private WebDriver driver;
    private String baseUrl;
    private WebDriverWait wait;

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

        // === THÊM CODE TẮT POP-UP MẬT KHẨU ===
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        driver = new ChromeDriver(options); // Khởi động Chrome với options
        // ======================================
        
        driver.manage().window().maximize();

        // driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); // Không cần thiết khi dùng WebDriverWait
        baseUrl = "http://localhost:8080/ShopDuck/";
        wait = new WebDriverWait(driver, 10);
    }

    @Test
    public void testLogin_UserSuccess() {

        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); // Dừng 1 giây

        WebElement userInput = driver.findElement(By.name("username"));
        WebElement passInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        userInput.sendKeys("user");
        sleep(500); // Dừng 0.5 giây
        passInput.sendKeys("user123");
        sleep(1000); // Dừng 1 giây
        
        loginButton.click();

        try {
            wait.until(ExpectedConditions.urlContains("user/view-products"));
            sleep(1000); // Dừng 1 giây để xem trang sản phẩm
            assertTrue(true);
        } catch (Exception e) {
            fail("Không chuyển được vào trang view-products. URL hiện tại: " + driver.getCurrentUrl());
        }
    }

    @Test
    public void testLogin_FailWrongPassword() {

        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); // Dừng 1 giây

        WebElement userInput = driver.findElement(By.name("username"));
        WebElement passInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        userInput.sendKeys("user");
        sleep(500); // Dừng 0.5 giây
        passInput.sendKeys("wrongpassword");
        sleep(1000); // Dừng 1 giây
        
        loginButton.click();

        try {
            WebElement errorAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".alert.alert-danger")
                )
            );
            sleep(1000); // Dừng 1 giây để xem lỗi

            String errorText = errorAlert.getText();
            assertTrue(errorText.contains("Sai") || errorText.contains("mật khẩu"));

        } catch (Exception e) {
            fail("Không tìm thấy alert-danger sau khi login sai.");
        }
    }

    @After
    public void tearDown() {
        // Dừng 2 giây cuối cùng để xem kết quả trước khi tắt
        sleep(2000); 
        
        if (driver != null) {
            driver.quit();
        }
    }
}