package system;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class ProductDetailTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @Before
    public void setUp() {
        // Khởi tạo WebDriver và cài đặt các tùy chọn
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        baseUrl = "http://localhost:8080/ShopDuck/user/view-products";  // Đường dẫn trang chủ
        wait = new WebDriverWait(driver, 10);
    }

    /**
     * Kiểm thử tìm kiếm và xem chi tiết sản phẩm từ kết quả tìm kiếm
     */
    @Test
    public void testSearchAndViewProductDetails() {
        driver.get(baseUrl); // Điều hướng đến trang sản phẩm
        sleep(1000);

        // Tìm kiếm sản phẩm
        WebElement searchInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("keyword"))
        );
        WebElement searchButton = driver.findElement(By.cssSelector("form[action*='/user/search-products'] button[type='submit']"));

        String keyword = "Áo Thun";  // Từ khóa tìm kiếm
        searchInput.sendKeys(keyword);
        sleep(1000);
        searchButton.click();
        sleep(1000);

        // Kiểm tra kết quả tìm kiếm có sản phẩm
        try {
            WebElement firstProductCard = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card"))
            );
            firstProductCard.click();
            sleep(1000);

            // Kiểm tra URL thay đổi sau khi nhấp vào sản phẩm
            wait.until(ExpectedConditions.urlContains("product-detail?id=")); // Kiểm tra URL để xác minh trang chi tiết sản phẩm đã tải

            // Kiểm tra phần tiêu đề sản phẩm xuất hiện
            WebElement productDetailTitle = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")) // Sửa lại selector từ .product-title thành h2
            );
            assertTrue("Phải hiển thị tiêu đề sản phẩm", productDetailTitle.isDisplayed());

        } catch (Exception e) {
            fail("Test tìm kiếm và xem chi tiết sản phẩm thất bại. Lỗi: " + e.getMessage());
        }
    }

    /**
     * Kiểm thử xem chi tiết sản phẩm từ trang chủ
     */
    @Test
    public void testViewProductDetailsFromHomePage() {
        driver.get(baseUrl); // Điều hướng đến trang sản phẩm
        sleep(1000);

        // Nhấp vào sản phẩm đầu tiên từ trang chủ
        try {
            WebElement firstProductCard = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card"))
            );
            firstProductCard.click();
            sleep(1000);

            // Kiểm tra URL thay đổi sau khi nhấp vào sản phẩm
            wait.until(ExpectedConditions.urlContains("product-detail?id=")); // Kiểm tra URL để xác minh trang chi tiết sản phẩm đã tải

            // Kiểm tra phần tiêu đề sản phẩm xuất hiện
            WebElement productDetailTitle = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")) // Sửa lại selector từ .product-title thành h2
            );
            assertTrue("Phải hiển thị tiêu đề sản phẩm", productDetailTitle.isDisplayed());

        } catch (Exception e) {
            fail("Test xem chi tiết sản phẩm từ trang chủ thất bại. Lỗi: " + e.getMessage());
        }
    }

    /**
     * Kiểm thử xem chi tiết sản phẩm từ menu (Bộ sưu tập, Áo nam, Quần nam, ...)
     */
    @Test
    public void testViewProductDetailsFromMenu() {
        driver.get(baseUrl); // Điều hướng đến trang sản phẩm
        sleep(1000);

        // Nhấp vào mục menu "BỘ SƯU TẬP"
        try {
            WebElement collectionMenu = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("BỘ SƯU TẬP"))
            );
            collectionMenu.click();
            sleep(1000);

            // Nhấp vào sản phẩm từ danh sách sản phẩm trong menu
            WebElement firstProductCard = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card"))
            );
            firstProductCard.click();
            sleep(1000);

            // Kiểm tra URL thay đổi sau khi nhấp vào sản phẩm
            wait.until(ExpectedConditions.urlContains("product-detail?id=")); // Kiểm tra URL để xác minh trang chi tiết sản phẩm đã tải

            // Kiểm tra phần tiêu đề sản phẩm xuất hiện
            WebElement productDetailTitle = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")) // Sửa lại selector từ .product-title thành h2
            );
            assertTrue("Phải hiển thị tiêu đề sản phẩm", productDetailTitle.isDisplayed());

        } catch (Exception e) {
            fail("Test xem chi tiết sản phẩm từ menu thất bại. Lỗi: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        sleep(2000);  // Dừng 2 giây sau khi test hoàn thành
        if (driver != null) {
            driver.quit();  // Đóng trình duyệt
        }
    }

    // Hàm ngủ tạm dừng trong 1 khoảng thời gian nhất định (ms)
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
