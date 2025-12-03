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

public class LoginSystemTest {
    
    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp"; 

    // [CẤU HÌNH] Tốc độ test chậm (3 giây) để dễ quan sát
    final int SLOW_SPEED = 3000;

    // === 1. CÁC BIẾN ĐỂ GHI BÁO CÁO EXCEL ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = ""; 

    // Hàm set thông tin đầu vào cho mỗi Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa chạy xong"; 
    }

    // === 2. CẤU HÌNH RULE ĐỂ BẮT KẾT QUẢ PASS/FAIL ===
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, currentActual, "PASS");
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi code/Assert: " + e.getMessage(), "FAIL");
        }
    };

    // === 3. XUẤT FILE EXCEL KHI CHẠY XONG TẤT CẢ TEST ===
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Login.xlsx");
        System.out.println(">> Đã xuất file báo cáo: BaoCao_SystemTest_Login.xlsx");
    }

    // === SETUP SELENIUM ===
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    // ==========================================
    // === CÁC TEST CASE ===
    // ==========================================

    @Test
    public void testLoginSuccess_Admin() {
        setTestCaseInfo(
            "ST_LOGIN_01",
            "Đăng nhập Admin thành công",
            "1. Nhập admin/admin123\n2. Click Login\n3. Check URL",
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
        
        // [FIX] Dùng submit() thay vì click()
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        // 2. Lấy kết quả thực tế
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL hiện tại: " + currentUrl; 

        // 3. Assert
        boolean isCorrectPage = currentUrl.contains("admin/dashboard") || currentUrl.contains("admin");
        Assert.assertTrue("Lỗi: Không vào được trang Dashboard Admin!", isCorrectPage);
    }

    @Test
    public void testLoginSuccess_User() {
        setTestCaseInfo(
            "ST_LOGIN_02",
            "Đăng nhập User thường thành công",
            "1. Nhập user/user123\n2. Click Login\n3. Check URL",
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

        boolean isCorrectPage = currentUrl.contains("user/view-products");
        Assert.assertTrue("Lỗi: Không vào được trang Sản phẩm!", isCorrectPage);
    }

    @Test
    public void testLoginFail_WrongPassword() {
        setTestCaseInfo(
            "ST_LOGIN_03",
            "Đăng nhập sai mật khẩu",
            "1. Nhập admin/sai_pass\n2. Click Login\n3. Check URL & Thông báo lỗi",
            "User: admin, Pass: sai_pass",
            "Ở lại trang Login & Hiện lỗi 'alert-danger'"
        );

        driver.get(loginUrl);
        slowDown();

        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("sai_pass_roi_nhe"); 
        slowDown();
        
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();

        String currentUrl = driver.getCurrentUrl();
        
        // Kiểm tra URL (phải ở lại trang login)
        if (!currentUrl.contains("Login.jsp")) {
            this.currentActual = "Lỗi: Trang đã chuyển đi: " + currentUrl;
            Assert.fail("Trang đã bị chuyển đi nơi khác!");
        }

        // Kiểm tra thông báo lỗi
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            
            this.currentActual = "URL: Login.jsp | Lỗi: " + errorText;

            Assert.assertTrue("Thông báo lỗi không hiển thị!", errorAlert.isDisplayed());
            // Kiểm tra nội dung (có thể là 'Sai tên đăng nhập' hoặc 'Mật khẩu không đúng' tùy code backend)
            Assert.assertTrue("Nội dung lỗi sai!", errorText.contains("Sai") || errorText.contains("không đúng"));
            
        } catch (Exception e) {
            this.currentActual = "Không tìm thấy thẻ lỗi .alert-danger";
            throw e; 
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}