package system;

import util.ExcelTestExporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class CheckoutSystemTest {

    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    // Chúng ta sẽ không dùng driver.get(cartUrl) hay checkoutUrl trực tiếp nữa
    // Mà sẽ click chuột để đi tới đó.

    // [CẤU HÌNH] Tốc độ test chậm (3 giây) để dễ quan sát
    final int SLOW_SPEED = 3000;

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, currentActual, "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi: " + e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Checkout.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Checkout.xlsx");
    }

    // Hàm làm chậm
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // 1. Đăng nhập trước
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("user");
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("user123");
        slowDown();

        // [FIX LOGIN] Dùng submit() để tránh click nhầm
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        // 2. Thêm hàng vào giỏ để chuẩn bị test
        addToCartForSetup();
    }

    // Hàm thêm hàng nhanh (để đảm bảo giỏ không trống)
    private void addToCartForSetup() {
        driver.get(homeUrl);
        slowDown();
        try {
            // Click vào sản phẩm đầu tiên
            driver.findElement(By.cssSelector(".product-card .product-img-wrap")).click();
            slowDown();
            // Chọn Size (nếu có)
            if (driver.findElements(By.id("variantSelect")).size() > 0) {
                new Select(driver.findElement(By.id("variantSelect"))).selectByIndex(1);
            }
            // Click nút Thêm
            driver.findElement(By.className("btn-add-cart")).click();
            slowDown();
        } catch (Exception e) {
            System.out.println("Setup: Giỏ hàng có thể đã có sẵn hàng hoặc lỗi setup.");
        }
    }

    // ================================================================
    // CÁC TEST CASE ĐẶT HÀNG (LUỒNG CHUẨN)
    // ================================================================

    // --- CASE 1: ĐẶT HÀNG THÀNH CÔNG (Happy Path) ---
    @Test
    public void testCheckout_Success() {
        setTestCaseInfo(
            "ST_CHECKOUT_01", 
            "Đặt hàng thành công (Luồng chuẩn)", 
            "1. Click icon Giỏ hàng\n2. Click nút 'Thanh toán'\n3. Điền info & Submit", 
            "Đ/c: 123 Hanoi, SĐT: 0987654321", 
            "Chuyển về trang chủ & URL có '?success=true'"
        );

        // 1. Từ trang hiện tại, click vào Icon Giỏ hàng trên Header
        try {
            System.out.println("Step 1: Click icon Giỏ hàng...");
            WebElement cartIcon = driver.findElement(By.cssSelector(".btn-cart")); 
            cartIcon.click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy icon Giỏ hàng trên Header!");
        }
        slowDown();

        // 2. Tại trang Giỏ hàng, click nút "Thanh toán"
        try {
            System.out.println("Step 2: Click nút Thanh toán...");
            WebElement checkoutBtn = driver.findElement(By.cssSelector(".btn-checkout"));
            checkoutBtn.click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy nút 'Thanh toán' trong giỏ hàng (Hoặc giỏ hàng rỗng)!");
        }
        slowDown();

        // Kiểm tra xem đã vào đúng trang Checkout chưa
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue("Chưa vào được trang Checkout!", currentUrl.contains("checkout"));

        // 3. Điền thông tin giao hàng
        System.out.println("Step 3: Điền thông tin...");
        WebElement addressInput = driver.findElement(By.name("address"));
        WebElement phoneInput = driver.findElement(By.name("phone"));

        addressInput.clear();
        addressInput.sendKeys("123 Đường Test, Quận Selenium, TP Java");
        
        phoneInput.clear();
        phoneInput.sendKeys("0987654321");
        
        slowDown();

        // 4. Click nút Xác nhận Đặt hàng
        WebElement confirmBtn = driver.findElement(By.className("btn-confirm"));
        confirmBtn.click();
        
        // Chờ Server xử lý lâu hơn chút (5s)
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        // 5. Kiểm tra kết quả
        currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL sau khi đặt: " + currentUrl;

        // Servlet redirect về: /user/view-products?success=true
        Assert.assertTrue("Không chuyển về trang chủ!", currentUrl.contains("view-products"));
        Assert.assertTrue("Không có tham số success=true!", currentUrl.contains("success=true"));
        
        boolean hasSuccessMsg = driver.findElements(By.className("alert-success")).size() > 0;
        Assert.assertTrue("Không hiện thông báo 'Đặt hàng thành công'!", hasSuccessMsg);
    }

    // --- CASE 2: ĐẶT HÀNG THẤT BẠI (Thiếu thông tin) ---
    @Test
    public void testCheckout_Fail_MissingInfo() {
        setTestCaseInfo(
            "ST_CHECKOUT_02", 
            "Bỏ trống thông tin giao hàng", 
            "1. Vào Giỏ -> Thanh toán\n2. Để trống Đ/c & SĐT\n3. Click Đặt hàng", 
            "Input: Rỗng", 
            "Trình duyệt chặn submit hoặc hiện lỗi"
        );

        // 1. Đi luồng: Header -> Giỏ hàng -> Thanh toán
        driver.findElement(By.cssSelector(".btn-cart")).click();
        slowDown();
        
        try {
            driver.findElement(By.cssSelector(".btn-checkout")).click();
        } catch(Exception e) {
            Assert.fail("Không thấy nút Thanh toán (Giỏ rỗng?)");
        }
        slowDown();

        // 2. Xóa sạch dữ liệu (Cố tình để trống)
        WebElement addressInput = driver.findElement(By.name("address"));
        WebElement phoneInput = driver.findElement(By.name("phone"));
        addressInput.clear();
        phoneInput.clear();
        slowDown();

        // 3. Click nút Đặt hàng
        WebElement confirmBtn = driver.findElement(By.className("btn-confirm"));
        confirmBtn.click();
        slowDown();

        // Kiểm tra
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        // URL vẫn phải là trang checkout (vì chưa thành công)
        Assert.assertTrue("Trang bị chuyển đi nơi khác!", currentUrl.contains("checkout"));
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}