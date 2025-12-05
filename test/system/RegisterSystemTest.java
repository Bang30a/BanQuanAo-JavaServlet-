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

/**
 * System Test (Selenium) cho chức năng Đăng ký tài khoản (Register).
 * Kiểm thử các trường hợp thành công, thất bại do dữ liệu trùng lặp, mật khẩu yếu và validation.
 */
public class RegisterSystemTest {

    WebDriver driver;
    private final String registerUrl = "http://localhost:8080/ShopDuck/user/auth/Register.jsp"; 

    /** Tốc độ làm chậm (milliseconds) giữa các bước Selenium để dễ dàng quan sát. */
    private final int SLOW_SPEED = 2000;

    // --- BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ---
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Register.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Register.xlsx");
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver.
     */
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    // ================================================================
    // CÁC TEST CASE ĐĂNG KÝ
    // ================================================================

    // --- CASE 1: Đăng ký thành công ---
    @Test
    public void testRegister_Success() {
        long timestamp = System.currentTimeMillis();
        String uniqueUser = "user_" + timestamp;
        String uniqueEmail = "email_" + timestamp + "@test.com";

        setTestCaseInfo(
            "ST_REG_01", 
            "Đăng ký thành công (User mới)", 
            "1. Nhập full thông tin hợp lệ (unique)\n2. Click Đăng ký\n3. Check chuyển hướng và thông báo", 
            "User: " + uniqueUser, 
            "Chuyển về Login.jsp & Hiện thông báo Success"
        );

        driver.get(registerUrl);
        slowDown();

        // Điền form với dữ liệu duy nhất
        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        slowDown();
        driver.findElement(By.name("fullname")).sendKeys("Test User Auto");
        slowDown();
        driver.findElement(By.name("email")).sendKeys(uniqueEmail);
        slowDown();
        driver.findElement(By.name("password")).sendKeys("123456");
        slowDown();

        // Submit
        driver.findElement(By.cssSelector(".btn-register")).click();
        try { Thread.sleep(4000); } catch (InterruptedException e) {} 

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;

        // Verify 1: Chuyển hướng
        Assert.assertTrue("Lỗi: Không chuyển về trang Login.jsp sau khi đăng ký thành công!", currentUrl.contains("Login.jsp"));

        // Verify 2: Thông báo thành công
        try {
            WebElement successAlert = driver.findElement(By.className("alert-success"));
            Assert.assertTrue("Thông báo thành công không hiển thị!", successAlert.isDisplayed());
            this.currentActual += " | Msg: " + successAlert.getText();
        } catch (Exception e) {
            Assert.fail("Lỗi: Không tìm thấy thông báo .alert-success trên trang Login.");
        }
    }

    // --- CASE 2: Đăng ký thất bại (Trùng Username) ---
    @Test
    public void testRegister_Fail_DuplicateUsername() {
        setTestCaseInfo(
            "ST_REG_02", 
            "Đăng ký trùng tên đăng nhập", 
            "1. Nhập user 'admin' (đã tồn tại)\n2. Click Đăng ký\n3. Check lỗi", 
            "User: admin", 
            "Ở lại trang Register.jsp & Hiện lỗi 'đã tồn tại' (hoặc tương tự)"
        );

        driver.get(registerUrl);
        slowDown();

        // Nhập username đã tồn tại
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        driver.findElement(By.name("fullname")).sendKeys("Cố tình trùng");
        driver.findElement(By.name("email")).sendKeys("duplicate@test.com");
        driver.findElement(By.name("password")).sendKeys("123456");
        slowDown();

        // Submit
        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        // Verify 1: Ở lại trang Register
        if (!currentUrl.contains("Register.jsp")) {
            this.currentActual = "Chuyển trang sai: " + currentUrl;
            Assert.fail("Đáng lẽ phải ở lại trang Register do lỗi trùng lặp!");
        }

        // Verify 2: Thông báo lỗi
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            this.currentActual = "Lỗi hiển thị: " + errorText;
            Assert.assertTrue("Nội dung lỗi sai! Mong đợi 'đã tồn tại' hoặc 'Duplicate'", errorText.contains("đã tồn tại") || errorText.contains("Duplicate"));
        } catch (Exception e) {
            Assert.fail("Không tìm thấy thông báo lỗi .alert-danger");
        }
    }

    // --- CASE 3: Đăng ký thất bại (Mật khẩu yếu) ---
    @Test
    public void testRegister_Fail_WeakPassword() {
        long timestamp = System.currentTimeMillis();
        String uniqueUser = "weak_" + timestamp;

        setTestCaseInfo(
            "ST_REG_03", 
            "Đăng ký mật khẩu yếu (< 6 ký tự)", 
            "1. Nhập pass '123' (quá ngắn)\n2. Click Đăng ký\n3. Check lỗi", 
            "Pass: 123", 
            "Ở lại trang & Hiện lỗi 'Mật khẩu quá yếu' (hoặc tương tự: < 6 ký tự)"
        );

        driver.get(registerUrl);
        slowDown();

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("fullname")).sendKeys("Weak Pass User");
        driver.findElement(By.name("email")).sendKeys(uniqueUser + "@test.com");
        
        // Mật khẩu quá ngắn
        driver.findElement(By.name("password")).sendKeys("123");
        slowDown();

        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue("Trang bị chuyển đi sai!", currentUrl.contains("Register.jsp"));

        // Verify: Thông báo lỗi mật khẩu
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            this.currentActual = "Lỗi hiển thị: " + errorText;
            
            Assert.assertTrue("Không báo lỗi mật khẩu yếu! (Mong đợi chứa 'quá yếu' hoặc '6 ký tự')", errorText.contains("quá yếu") || errorText.contains("6 ký tự"));
        } catch (Exception e) {
            Assert.fail("Không tìm thấy thông báo lỗi mật khẩu.");
        }
    }

    // --- CASE 4: Bỏ trống trường bắt buộc ---
    @Test
    public void testRegister_Fail_EmptyFields() {
        setTestCaseInfo(
            "ST_REG_04", 
            "Bỏ trống trường bắt buộc", 
            "1. Không nhập gì\n2. Click Đăng ký\n3. Check URL", 
            "Input: Rỗng", 
            "Ở lại trang Register.jsp (Trình duyệt chặn submit)"
        );

        driver.get(registerUrl);
        slowDown();

        // Submit form rỗng
        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown(); 

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        // Verify: Form không submit được, vẫn ở lại trang đăng ký
        Assert.assertTrue("Trang đã bị chuyển đi! (Mong đợi ở lại Register.jsp do HTML5 validation)", currentUrl.contains("Register.jsp"));
    }

    // --- [MỚI] CASE 5: Email sai định dạng (Client Validation) ---
    @Test
    public void testRegister_Fail_InvalidEmail() {
        setTestCaseInfo(
            "ST_REG_05", 
            "Email không đúng định dạng", 
            "1. Nhập email 'abc' (thiếu @)\n2. Click Đăng ký\n3. Check URL", 
            "Email: email_nay_sai_dinh_dang", 
            "Vẫn ở trang Register.jsp (Trình duyệt chặn submit)"
        );

        driver.get(registerUrl);
        slowDown();

        driver.findElement(By.name("username")).sendKeys("test_email_fail");
        driver.findElement(By.name("fullname")).sendKeys("Test Email");
        // Nhập email sai (thiếu @)
        driver.findElement(By.name("email")).sendKeys("email_nay_sai_dinh_dang"); 
        driver.findElement(By.name("password")).sendKeys("123456");
        slowDown();

        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        // Verify: Nếu input type="email" hoạt động, URL sẽ không đổi
        Assert.assertTrue("Lỗi: Form cho phép email sai định dạng (Mong đợi ở lại Register.jsp)!", currentUrl.contains("Register.jsp"));
    }

    // --- [MỚI] CASE 6: Test Link chuyển sang Đăng nhập ---
    @Test
    public void testRegister_NavigateToLogin() {
        setTestCaseInfo(
            "ST_REG_06", 
            "Click link 'Đăng nhập'", 
            "1. Tại trang ĐK, click link 'Đăng nhập'\n2. Check chuyển trang", 
            "Action: Click Link", 
            "Chuyển sang Login.jsp"
        );

        driver.get(registerUrl);
        slowDown();

        // Tìm link có chữ "Đăng nhập" và click
        driver.findElement(By.partialLinkText("Đăng nhập")).click();
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        Assert.assertTrue("Lỗi: Không chuyển sang trang Login.jsp!", currentUrl.contains("Login.jsp"));
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}