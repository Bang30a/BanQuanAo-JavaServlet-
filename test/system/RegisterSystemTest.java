package system;

import util.ExcelTestExporter; // Import tiện ích xuất Excel

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

public class RegisterSystemTest {

    WebDriver driver;
    // URL trang đăng ký
    String registerUrl = "http://localhost:8080/ShopDuck/user/auth/Register.jsp"; 

    // [CẤU HÌNH] Tốc độ test chậm (3 giây) để dễ quan sát
    final int SLOW_SPEED = 3000;

    // === 1. BIẾN GHI BÁO CÁO EXCEL ===
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
        this.currentActual = "Chưa chạy xong";
    }

    // === 2. RULE TỰ ĐỘNG GHI LOG PASS/FAIL ===
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

    // === 3. XUẤT EXCEL KHI HẾT CLASS ===
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Register.xlsx");
        System.out.println(">> Đã xuất file báo cáo: BaoCao_SystemTest_Register.xlsx");
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
    }

    // ==========================================
    // === CÁC TEST CASE ĐĂNG KÝ ===
    // ==========================================

    // --- CASE 1: Đăng ký thành công (Happy Path) ---
    @Test
    public void testRegister_Success() {
        // Tạo username ngẫu nhiên để không bị trùng khi chạy lại test nhiều lần
        long timestamp = System.currentTimeMillis();
        String uniqueUser = "user_" + timestamp;
        String uniqueEmail = "email_" + timestamp + "@test.com";

        setTestCaseInfo(
            "ST_REG_01", 
            "Đăng ký thành công (User mới)", 
            "1. Nhập full thông tin hợp lệ\n2. Click Đăng ký\n3. Check URL chuyển về Login", 
            "User: " + uniqueUser + ", Pass: 123456", 
            "Chuyển hướng đến trang Login.jsp"
        );

        // 1. Truy cập trang Đăng ký
        driver.get(registerUrl);
        slowDown();

        // 2. Điền thông tin vào form
        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        slowDown();
        driver.findElement(By.name("fullname")).sendKeys("Test User Auto");
        slowDown();
        driver.findElement(By.name("email")).sendKeys(uniqueEmail);
        slowDown();
        driver.findElement(By.name("password")).sendKeys("123456");
        slowDown();

        // 3. Click nút Đăng ký (class .btn-register)
        driver.findElement(By.cssSelector(".btn-register")).click();
        
        // Chờ server xử lý tạo user và redirect
        try { Thread.sleep(4000); } catch (InterruptedException e) {} 

        // 4. Kiểm tra kết quả
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL hiện tại: " + currentUrl;

        // Servlet redirect về: /user/auth/Login.jsp
        Assert.assertTrue("Lỗi: Không chuyển về trang Login!", currentUrl.contains("user/auth/Login.jsp") || currentUrl.contains("Login.jsp"));
    }

    // --- CASE 2: Đăng ký thất bại (Trùng Username) ---
    @Test
    public void testRegister_Fail_DuplicateUsername() {
        setTestCaseInfo(
            "ST_REG_02", 
            "Đăng ký trùng tên đăng nhập", 
            "1. Nhập username 'admin' (đã tồn tại)\n2. Click Đăng ký\n3. Check lỗi hiển thị", 
            "User: admin (đã có trong DB)", 
            "Ở lại trang Register & Hiện lỗi 'Tên đăng nhập đã tồn tại'"
        );

        // 1. Truy cập trang
        driver.get(registerUrl);
        slowDown();

        // 2. Điền form với user "admin" (đã tồn tại)
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        driver.findElement(By.name("fullname")).sendKeys("Cố tình trùng");
        driver.findElement(By.name("email")).sendKeys("duplicate@test.com");
        driver.findElement(By.name("password")).sendKeys("123456");
        slowDown();

        // 3. Click Đăng ký
        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown();

        // 4. Kiểm tra kết quả
        String currentUrl = driver.getCurrentUrl();
        
        // Kiểm tra URL (Phải ở lại trang Register)
        if (!currentUrl.contains("Register.jsp")) {
            this.currentActual = "Lỗi: Trang bị chuyển đi nơi khác: " + currentUrl;
            Assert.fail("Đáng lẽ phải ở lại trang Register nhưng lại chuyển đi!");
        }

        // Kiểm tra thông báo lỗi (class alert-danger)
        try {
            WebElement errorAlert = driver.findElement(By.className("alert-danger"));
            String errorText = errorAlert.getText();
            
            this.currentActual = "URL: Register.jsp | Lỗi: " + errorText;

            Assert.assertTrue("Thông báo lỗi không hiện!", errorAlert.isDisplayed());
            // Servlet trả về: "⚠️ Tên đăng nhập đã tồn tại!"
            Assert.assertTrue("Nội dung lỗi sai!", errorText.contains("đã tồn tại") || errorText.contains("Duplicate"));
            
        } catch (Exception e) {
            this.currentActual = "Không tìm thấy thông báo lỗi .alert-danger";
            throw e;
        }
    }

    // --- CASE 3: Bỏ trống trường bắt buộc (Client-side Validation) ---
    @Test
    public void testRegister_Fail_EmptyFields() {
        setTestCaseInfo(
            "ST_REG_03", 
            "Bỏ trống trường bắt buộc", 
            "1. Không nhập gì cả\n2. Click Đăng ký", 
            "Dữ liệu rỗng", 
            "Trình duyệt chặn submit (URL không đổi)"
        );

        driver.get(registerUrl);
        slowDown();

        // 1. Click luôn nút Đăng ký mà không nhập gì
        driver.findElement(By.cssSelector(".btn-register")).click();
        slowDown(); 

        // Nếu HTML5 'required' hoạt động, trình duyệt sẽ chặn và không gửi request -> URL không đổi
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL hiện tại: " + currentUrl;
        
        // Kiểm tra xem URL có đúng là vẫn ở trang Register không
        Assert.assertTrue("Trang không được chuyển đi", currentUrl.contains("Register.jsp"));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}