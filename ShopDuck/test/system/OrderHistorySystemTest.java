package system;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đây là lớp SYSTEM TEST cho việc "Xem lịch sử đơn hàng" của người dùng.
 */
public class OrderHistorySystemTest {

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
        
        // Tắt pop-up khi login
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Khởi tạo trình duyệt với tùy chọn
        driver = new ChromeDriver(options); 
        driver.manage().window().maximize(); // Mở toàn màn hình
        
        // Đặt URL gốc của trang web
        baseUrl = "http://localhost:8080/ShopDuck/";
        
        // Khởi tạo bộ "Chờ" (chờ tối đa 10 giây)
        wait = new WebDriverWait(driver, 10); 
    }

    /**
     * Test Case: Người dùng đăng nhập và xem lịch sử đơn hàng của mình.
     */
    @Test
    public void testUserCanViewOrderHistory() {
        
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
        
        // --- PHẦN 2: XEM LỊCH SỬ ĐƠN HÀNG ---
        // Chuyển sang trang "Lịch sử đơn hàng"
        driver.get(baseUrl + "user/order-history");
        sleep(1000); // Dừng 1 giây để xem trang lịch sử đơn hàng
        
        // Kiểm tra xem có bảng hiển thị đơn hàng không
        WebElement orderHistoryTable;
        try {
            orderHistoryTable = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".table"))
            );
        } catch (Exception e) {
            fail("Không tìm thấy bảng lịch sử đơn hàng.");
            return;
        }

        // Kiểm tra nếu bảng lịch sử đơn hàng có ít nhất 1 dòng đơn hàng
        List<WebElement> rows = orderHistoryTable.findElements(By.tagName("tr"));
        assertTrue("Lịch sử đơn hàng không có đơn hàng nào", rows.size() > 1); // Bỏ qua dòng tiêu đề

        // Kiểm tra thông tin đơn hàng đầu tiên
        WebElement firstOrderRow = rows.get(1); // Dòng đơn hàng đầu tiên
        WebElement orderStatus = firstOrderRow.findElement(By.cssSelector(".badge"));
        assertNotNull("Trạng thái đơn hàng không tồn tại", orderStatus);
        
        // Kiểm tra trạng thái đơn hàng có hợp lệ không
        String statusText = orderStatus.getText();
        assertTrue("Trạng thái đơn hàng không hợp lệ", 
                   statusText.equals("Completed") || statusText.equals("Pending") || statusText.equals("Shipped"));

        // --- PHẦN 3: XEM CHI TIẾT ĐƠN HÀNG ---
        WebElement firstOrderDetailLink = firstOrderRow.findElement(By.cssSelector(".btn-primary"));
        firstOrderDetailLink.click(); // Click vào xem chi tiết đơn hàng

        // Chờ đến khi chuyển trang chi tiết đơn hàng
        wait.until(ExpectedConditions.urlContains("user/order-detail"));
        sleep(1000); // Dừng 1 giây để xem trang chi tiết

        // Kiểm tra xem trang chi tiết đơn hàng có hiển thị thông tin hợp lệ không
        WebElement orderDetailTitle = driver.findElement(By.cssSelector(".order-detail-title"));
        assertNotNull("Không thấy tiêu đề chi tiết đơn hàng", orderDetailTitle);

        WebElement productName = driver.findElement(By.cssSelector(".product-name"));
        assertNotNull("Không thấy tên sản phẩm", productName);
        
        // --- PHẦN 4: KIỂM TRA LẠI THÔNG TIN CHI TIẾT --- 
        // Kiểm tra có các thông tin như tên sản phẩm, giá, số lượng và tổng giá trị trong chi tiết đơn hàng
        WebElement productPrice = driver.findElement(By.cssSelector(".product-price"));
        WebElement productQuantity = driver.findElement(By.cssSelector(".product-quantity"));
        
        assertNotNull("Không thấy giá sản phẩm", productPrice);
        assertNotNull("Không thấy số lượng sản phẩm", productQuantity);
    }

    @After
    public void tearDown() {
        sleep(2000); // Dừng 2 giây cuối cùng để xem kết quả trước khi tắt
        if (driver != null) {
            driver.quit();
        }
    }
}
