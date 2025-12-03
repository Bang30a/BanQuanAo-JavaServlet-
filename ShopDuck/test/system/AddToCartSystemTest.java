// Đặt trong "Test Packages/system/"
package system;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import static org.junit.Assert.*;

// === IMPORT SELENIUM ===
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// === IMPORT JAVA UTILS ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SYSTEM TEST (Kiểm thử Hệ thống) cho luồng "Thêm vào giỏ hàng".
 * - Sử dụng Selenium WebDriver.
 * - Xuất báo cáo ra file Excel (CSV).
 */
public class AddToCartSystemTest {

    private WebDriver driver;
    private String baseUrl;
    private WebDriverWait wait;

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách lưu kết quả để xuất Excel (Static để dùng trong AfterClass)
    private static final List<String[]> finalReportData = new ArrayList<>();

    // Hàm "Ngủ" (để chạy chậm cho dễ nhìn)
    private void sleep(int milliseconds) {
        try { Thread.sleep(milliseconds); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Before
    public void setUp() {
        // 1. Cấu hình Driver (Sửa đường dẫn nếu cần)
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        
        // 2. Tùy chọn Chrome (Tắt save password, tắt thông báo)
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        
        // Tắt dòng log "Chrome is being controlled by automated software"
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        // 3. Khởi tạo Driver
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        
        // 4. URL gốc
        baseUrl = "http://localhost:8080/ShopDuck/";
        
        // 5. Bộ chờ (10s)
        wait = new WebDriverWait(driver, 10);
    }

    // Hàm điền thông tin báo cáo
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // -- TEST CASE --

    @Test
    public void testUserCanAddToCart() {
        setTestCaseInfo(
            "SYS_CART_01", 
            "System Test: Thêm vào giỏ hàng", 
            "1. Login\n2. Chọn SP đầu tiên\n3. Bấm Add to Cart\n4. Check Alert", 
            "User: user / Pass: user123", 
            "Hiện thông báo: 'Đã thêm sản phẩm vào giỏ hàng'"
        );

        // --- BƯỚC 1: ĐĂNG NHẬP ---
        driver.get(baseUrl + "user/Login.jsp");
        sleep(1000); 
        
        driver.findElement(By.name("username")).sendKeys("user");
        sleep(500);
        driver.findElement(By.name("password")).sendKeys("user123");
        sleep(1000);
        
        // Click Login
        driver.findElement(By.id("loginButton")).click();

        // Chờ chuyển hướng
        wait.until(ExpectedConditions.urlContains("user/view-products"));
        sleep(1000); 
        
        
        // --- BƯỚC 2: CHỌN SẢN PHẨM ---
        WebElement firstProductLink;
        try {
            firstProductLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".card a")));
        } catch (Exception e) {
            fail("Lỗi: Không tìm thấy sản phẩm nào (.card a)");
            return;
        }
        firstProductLink.click();
        
        
        // --- BƯỚC 3: THÊM VÀO GIỎ ---
        WebElement addToCartButton;
        try {
            addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("btn-add-cart")));
            sleep(1000);
        } catch (Exception e) {
            fail("Lỗi: Không tìm thấy nút 'Thêm vào giỏ' (btn-add-cart)");
            return;
        }
        addToCartButton.click();
        
        
        // --- BƯỚC 4: KIỂM TRA THÔNG BÁO ---
        try {
            WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
            sleep(1000);
            
            String successText = successAlert.getText();
            System.out.println("Thông báo thực tế: " + successText);
            
            assertTrue("Thông báo phải chứa text xác nhận", 
                       successText.contains("Đã thêm sản phẩm vào giỏ hàng"));
            
        } catch (Exception e) {
            fail("Lỗi: Không thấy thông báo thành công (alert-success)");
        }
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT FILE EXCEL (CSV) & DỌN DẸP ===
    // ==========================================================

    @After
    public void tearDown() {
        sleep(2000); // Đợi xíu cho người xem kịp nhìn
        if (driver != null) {
            driver.quit(); // Tắt trình duyệt
        }
    }

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, currentExpected, "PASS"
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMsg = (e != null) ? e.getMessage() : "Unknown Error";
            // Rút gọn lỗi Selenium cho đỡ dài dòng trong Excel
            if (errorMsg.length() > 100) errorMsg = errorMsg.substring(0, 97) + "...";
            
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, errorMsg, "FAIL"
            });
        }
    };
    
    @AfterClass
    public static void exportToExcelCSV() {
        String fileName = "KetQuaTest_System_AddToCart.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo System Test ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            writer.write('\ufeff'); // BOM cho Tiếng Việt
            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            for (String[] row : finalReportData) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialChars(row[0]),
                        escapeSpecialChars(row[1]),
                        escapeSpecialChars(row[2]),
                        escapeSpecialChars(row[3]),
                        escapeSpecialChars(row[4]),
                        escapeSpecialChars(row[5]),
                        escapeSpecialChars(row[6])
                );
                writer.println(line);
            }
            System.out.println("XONG! File đã được tạo.");
            System.out.println("-------------------------------------------------------");
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }
    
    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        // Thay thế ký tự xuống dòng bằng dấu cách để không vỡ dòng CSV
        String escapedData = data.replaceAll("\"", "\"\"").replaceAll("\n", " | ");
        return "\"" + escapedData + "\"";
    }
}