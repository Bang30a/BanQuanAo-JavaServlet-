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
 * System Test (Selenium) cho chức năng Đăng nhập (Login).
 * Kiểm thử các trường hợp đăng nhập thành công (Admin, User) và thất bại (Sai pass, user không tồn tại).
 */
public class LoginSystemTest {
    
    WebDriver driver;
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp"; 

    // [CẤU HÌNH] Tốc độ làm chậm (milliseconds) giữa các bước Selenium để dễ dàng quan sát
    private final int SLOW_SPEED = 2000;

    // === 1. BIẾN GHI BÁO CÁO EXCEL ===
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
        this.currentActual = "Chưa chạy xong"; 
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
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi code/Assert: " + e.getMessage(), "FAIL");
        }
    };

    /**
     * Phương thức được gọi một lần sau khi tất cả các Test Case hoàn thành.
     * Dùng để xuất dữ liệu đã thu thập được ra file Excel cuối cùng.
     */
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Login.xlsx");
        System.out.println(">> Đã xuất file báo cáo: BaoCao_SystemTest_Login.xlsx");
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

    // ==========================================
    // === CÁC TEST CASE XÁC THỰC ===
    // ==========================================

    // --- CASE 1: Đăng nhập Admin thành công ---
    @Test
    public void testLoginSuccess_Admin() {
        setTestCaseInfo(
            "ST_LOGIN_01",
            "Đăng nhập Admin thành công",
            "1. Nhập admin/admin123\n2. Click Login\n3. Check URL chuyển hướng",
            "User: admin, Pass: admin123",
            "Chuyển hướng đến /admin/dashboard"
        );

        driver.get(loginUrl);
        slowDown();

        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("admin123");
        slowDown();
        
        try {
            // Cố gắng submit qua trường password
            passField.submit();
        } catch (Exception e) {
            // Fallback: click nút login
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL hiện tại: " + currentUrl; 

        // Verify: Phải chuyển hướng đến khu vực admin
        boolean isCorrectPage = currentUrl.contains("admin/dashboard") || currentUrl.contains("admin");
        Assert.assertTrue("Lỗi: Không vào được trang Dashboard Admin!", isCorrectPage);
    }

    // --- CASE 2: Đăng nhập User thường thành công ---
    @Test
    public void testLoginSuccess_User() {
        setTestCaseInfo(
            "ST_LOGIN_02",
            "Đăng nhập User thường thành công",
            "1. Nhập user/user123\n2. Click Login\n3. Check URL chuyển hướng",
            "User: user, Pass: user123",
            "Chuyển hướng đến /user/view-products"
        );

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

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL hiện tại: " + currentUrl;

        // Verify: Phải chuyển hướng đến trang xem sản phẩm
        boolean isCorrectPage = currentUrl.contains("user/view-products");
        Assert.assertTrue("Lỗi: Không vào được trang Sản phẩm!", isCorrectPage);
    }

    // --- CASE 3: Đăng nhập thất bại do Sai mật khẩu ---
    @Test
    public void testLoginFail_WrongPassword() {
        setTestCaseInfo(
            "ST_LOGIN_03",
            "Đăng nhập sai mật khẩu",
            "1. Nhập user/sai_pass\n2. Click Login\n3. Check URL & Thông báo lỗi",
            "User: admin, Pass: sai_pass_roi_nhe",
            "Ở lại trang Login & Hiện lỗi 'Mật khẩu không chính xác'"
        );

        driver.get(loginUrl);
        slowDown();

        driver.findElement(By.name("username")).sendKeys("admin"); // User đúng
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("sai_pass_roi_nhe"); // Pass sai
        slowDown();
        
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        
        // 1. Verify: Vẫn ở lại trang Login.jsp
        if (!currentUrl.contains("Login.jsp")) {
            this.currentActual = "Lỗi: Trang đã chuyển đi: " + currentUrl;
            Assert.fail("Đăng nhập thất bại nhưng trang đã bị chuyển đi!");
        }

        // 2. Verify: Kiểm tra thông báo lỗi
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            
            this.currentActual = "URL: Login.jsp | Lỗi: " + errorText;

            Assert.assertTrue("Thông báo lỗi không hiển thị!", errorAlert.isDisplayed());
            // Kiểm tra nội dung lỗi
            Assert.assertTrue("Nội dung lỗi sai! Mong đợi 'Mật khẩu không chính xác'", errorText.contains("Mật khẩu"));
            
        } catch (Exception e) {
            this.currentActual = "Không tìm thấy thẻ lỗi .alert-danger";
            throw e; 
        }
    }

    // --- CASE 4: Đăng nhập thất bại do Tài khoản không tồn tại ---
    @Test
    public void testLoginFail_UserNotFound() {
        setTestCaseInfo(
            "ST_LOGIN_04",
            "Đăng nhập tài khoản không tồn tại",
            "1. Nhập user rác\n2. Nhập pass bất kỳ\n3. Click Login\n4. Check lỗi 'không tồn tại'",
            "User: user_ao_ma_canada",
            "Hiện lỗi 'Tài khoản không tồn tại'"
        );

        driver.get(loginUrl);
        slowDown();

        // Nhập tài khoản chắc chắn không có trong DB
        driver.findElement(By.name("username")).sendKeys("user_ao_ma_canada_" + System.currentTimeMillis());
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("123456"); 
        slowDown();
        
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        
        // 1. Verify: Vẫn ở lại trang Login.jsp
        if (!currentUrl.contains("Login.jsp")) {
            this.currentActual = "Lỗi: Trang đã chuyển đi: " + currentUrl;
            Assert.fail("Đăng nhập thất bại nhưng trang đã bị chuyển đi!");
        }

        // 2. Verify: Kiểm tra thông báo lỗi
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            
            this.currentActual = "URL: Login.jsp | Lỗi: " + errorText;

            Assert.assertTrue("Thông báo lỗi không hiển thị!", errorAlert.isDisplayed());
            // Kiểm tra nội dung lỗi
            Assert.assertTrue("Nội dung lỗi sai! Mong đợi 'không tồn tại'", errorText.contains("không tồn tại"));
            
        } catch (Exception e) {
            this.currentActual = "Không tìm thấy thẻ lỗi .alert-danger";
            throw e; 
        }
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}