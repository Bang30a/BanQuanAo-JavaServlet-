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
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    private final String checkoutDirectUrl = "http://localhost:8080/ShopDuck/user/checkout";
    private final String cartUrl = "http://localhost:8080/ShopDuck/user/order/view-cart.jsp";

    // [CẤU HÌNH] Tốc độ test chậm (2 giây) để quan sát từng bước
    private final int SLOW_SPEED = 2000;

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện Selenium.
     * @param data Dữ liệu/điều kiện đầu vào.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

    /**
     * Rule giúp ghi kết quả Test Case (PASS/FAIL) vào Excel sau khi mỗi @Test hoàn thành.
     */
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            // Ghi kết quả thành công
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, currentActual, "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            // Ghi kết quả thất bại, sử dụng thông báo lỗi của Exception
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi: " + e.getMessage(), "FAIL");
        }
    };

    /**
     * Phương thức được gọi một lần sau khi tất cả các Test Case hoàn thành.
     * Dùng để xuất dữ liệu đã thu thập được ra file Excel cuối cùng.
     */
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Checkout.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Checkout.xlsx");
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver, Đăng nhập User và thêm hàng vào giỏ.
     */
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

        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        // 2. Thêm hàng vào giỏ để chuẩn bị test (trừ case Empty Cart)
        addToCartForSetup();
    }

    /**
     * Helper: Thêm một sản phẩm bất kỳ vào giỏ hàng.
     * Đảm bảo giỏ hàng không trống trước khi thực hiện Checkout.
     */
    private void addToCartForSetup() {
        driver.get(homeUrl);
        slowDown();
        try {
            // Click vào sản phẩm đầu tiên
            driver.findElement(By.cssSelector(".product-card .product-img-wrap")).click();
            slowDown();
            // Chọn variant (nếu có)
            if (driver.findElements(By.id("variantSelect")).size() > 0) {
                new Select(driver.findElement(By.id("variantSelect"))).selectByIndex(1);
            }
            // Thêm vào giỏ
            driver.findElement(By.className("btn-add-cart")).click();
            slowDown();
        } catch (Exception e) {
            System.out.println("Setup: Giỏ hàng có thể đã có sẵn hàng.");
        }
    }

    // ================================================================
    // CÁC TEST CASE THANH TOÁN
    // ================================================================

    // --- CASE 1: ĐẶT HÀNG THÀNH CÔNG ---
    @Test
    public void testCheckout_Success() {
        setTestCaseInfo(
            "ST_CHECKOUT_01", 
            "Đặt hàng thành công", 
            "1. Vào Giỏ -> Thanh toán\n2. Điền thông tin Address & Phone hợp lệ\n3. Submit & Check Success", 
            "Address: HCM, Phone: 0987654321", 
            "Chuyển về trang chủ (view-products) và hiển thị thông báo thành công"
        );

        // 1. Vào trang Checkout
        goToCheckoutPage();

        // 2. Điền thông tin
        WebElement addressInput = driver.findElement(By.name("address"));
        WebElement phoneInput = driver.findElement(By.name("phone"));

        addressInput.clear();
        addressInput.sendKeys("123 Đường Test, Quận Selenium");
        slowDown();
        
        phoneInput.clear();
        phoneInput.sendKeys("0987654321");
        slowDown();

        // 3. Submit form Đặt hàng
        driver.findElement(By.className("btn-confirm")).click();
        
        // Chờ server xử lý (tăng thời gian chờ)
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        // 4. Kiểm tra URL và thông báo
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        Assert.assertTrue("Lỗi: Không chuyển về trang chủ (view-products) sau khi đặt hàng!", currentUrl.contains("view-products"));
        Assert.assertTrue("Lỗi: Không có tham số success=true trên URL!", currentUrl.contains("success=true"));
        
        // Kiểm tra thông báo thành công
        boolean hasSuccessMsg = driver.findElements(By.className("alert-success")).size() > 0;
        Assert.assertTrue("Lỗi: Không hiện thông báo thành công!", hasSuccessMsg);
    }

    // --- CASE 2: THIẾU THÔNG TIN ---
    @Test
    public void testCheckout_Fail_MissingInfo() {
        setTestCaseInfo(
            "ST_CHECKOUT_02", 
            "Bỏ trống thông tin bắt buộc", 
            "1. Vào Giỏ -> Thanh toán\n2. Để trống Address & Phone\n3. Click Đặt hàng", 
            "Input: Rỗng", 
            "Vẫn ở trang checkout do trình duyệt/HTML5 validation chặn submit"
        );

        goToCheckoutPage();

        // Xóa sạch thông tin
        WebElement addressInput = driver.findElement(By.name("address"));
        WebElement phoneInput = driver.findElement(By.name("phone"));
        addressInput.clear();
        phoneInput.clear();
        slowDown();

        // Cố gắng submit
        driver.findElement(By.className("btn-confirm")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        // Mong đợi: Vẫn ở trang checkout (do validation)
        Assert.assertTrue("Lỗi: Hệ thống cho phép submit khi thiếu thông tin!", currentUrl.contains("checkout"));
    }

    // --- [MỚI] CASE 3: SỐ ĐIỆN THOẠI KHÔNG HỢP LỆ ---
    @Test
    public void testCheckout_Fail_InvalidPhone() {
        setTestCaseInfo(
            "ST_CHECKOUT_03", 
            "Số điện thoại sai định dạng", 
            "1. Nhập SĐT là chữ 'abc'\n2. Click Đặt hàng\n3. Check validation", 
            "Phone: abc_khong_phai_so", 
            "Không cho submit/Vẫn ở trang checkout"
        );

        goToCheckoutPage();

        // Điền thông tin hợp lệ (trừ SĐT)
        driver.findElement(By.name("address")).sendKeys("Dia chi test");
        WebElement phoneInput = driver.findElement(By.name("phone"));
        phoneInput.clear();
        phoneInput.sendKeys("abc_khong_phai_so");
        slowDown();

        // Cố gắng submit
        driver.findElement(By.className("btn-confirm")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        // Mong đợi: Vẫn ở lại trang checkout
        Assert.assertTrue("Lỗi: Hệ thống chấp nhận SĐT sai định dạng!", currentUrl.contains("checkout"));
    }

    // --- [MỚI] CASE 4: QUAY LẠI GIỎ HÀNG ---
    @Test
    public void testCheckout_BackToCart() {
        setTestCaseInfo(
            "ST_CHECKOUT_04", 
            "Quay lại giỏ hàng", 
            "1. Vào trang Thanh toán\n2. Click link/nút Quay lại Giỏ hàng\n3. Check URL", 
            "Action: Back to Cart", 
            "Về trang view-cart.jsp"
        );

        goToCheckoutPage();

        // Tìm link/nút quay lại
        try {
            // Thử tìm theo text Giỏ hàng (Breadcrumb/Link)
            driver.findElement(By.partialLinkText("Giỏ hàng")).click();
        } catch (Exception e) {
            // Fallback: Sử dụng lịch sử trình duyệt
            driver.navigate().back();
        }
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        Assert.assertTrue("Lỗi: Không quay lại được trang giỏ hàng (view-cart.jsp)!", currentUrl.contains("view-cart") || currentUrl.contains("cart"));
    }

    // --- [MỚI] CASE 5: TRUY CẬP TRỰC TIẾP KHI GIỎ RỖNG ---
    @Test
    public void testCheckout_Fail_EmptyCart() {
        setTestCaseInfo(
            "ST_CHECKOUT_05", 
            "Truy cập Checkout khi giỏ rỗng", 
            "1. Xóa hết hàng trong giỏ\n2. Truy cập thẳng URL /checkout\n3. Check chuyển hướng", 
            "Cart: Empty", 
            "Bị đá về trang sản phẩm hoặc giỏ hàng (Không được phép vào trang checkout)"
        );

        // 1. Làm trống giỏ hàng
        driver.get(cartUrl);
        slowDown();
        // Lặp để xóa hết các item
        while (driver.findElements(By.className("btn-remove")).size() > 0) {
            driver.findElement(By.className("btn-remove")).click();
            try { driver.switchTo().alert().accept(); } catch (Exception ignored) {} // Xử lý alert xóa
            slowDown();
        }

        // 2. Cố tình truy cập trang thanh toán
        System.out.println(">> Cố tình truy cập: " + checkoutDirectUrl);
        driver.get(checkoutDirectUrl);
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        // Mong đợi: Không được ở trang checkout.jsp. Backend phải redirect.
        boolean isRedirected = !currentUrl.contains("checkout") && 
                             (currentUrl.contains("view-products") || currentUrl.contains("view-cart"));
                             
        Assert.assertTrue("Lỗi: Giỏ rỗng vẫn vào được trang thanh toán!", isRedirected);
    }

    /**
     * Helper: Điều hướng đến trang Thanh toán (Checkout) thông qua nút Checkout từ Giỏ hàng.
     */
    private void goToCheckoutPage() {
        try {
            driver.get(cartUrl);
            slowDown();
            // Bấm nút Checkout
            driver.findElement(By.cssSelector(".btn-checkout")).click();
            slowDown();
        } catch (Exception e) {
            // Nếu lỗi, thử setup lại giỏ hàng và đi đến trang checkout
            addToCartForSetup();
            driver.get(cartUrl);
            slowDown();
            driver.findElement(By.cssSelector(".btn-checkout")).click();
            slowDown();
        }
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}