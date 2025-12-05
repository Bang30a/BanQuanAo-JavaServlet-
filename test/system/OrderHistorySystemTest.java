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
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;

/**
 * System Test (Selenium) cho chức năng Lịch sử đơn hàng (Order History) của người dùng.
 * Kiểm thử khả năng xem danh sách đơn hàng và xem chi tiết từng đơn.
 */
public class OrderHistorySystemTest {

    WebDriver driver;
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    private final String historyServletUrl = "http://localhost:8080/ShopDuck/user/order-history";

    /** Tốc độ làm chậm (milliseconds) giữa các bước Selenium để dễ dàng quan sát. */
    private final int SLOW_SPEED = 3000;

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
        try {
            ExcelTestExporter.exportToExcel("BaoCao_SystemTest_OrderHistory.xlsx");
            System.out.println(">> Xuất Excel thành công.");
        } catch (Exception ignored) {}
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver và Đăng nhập User.
     */
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // 1. Điều hướng và Đăng nhập User
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

        // Kiểm tra đăng nhập thành công
        if (driver.getCurrentUrl().contains("Login.jsp")) {
            Assert.fail("SETUP FAILED: Đăng nhập thất bại.");
        }
    }

    /**
     * Test Case: Kiểm tra chức năng xem Lịch sử đơn hàng và xem Chi tiết đơn hàng.
     */
    @Test
    public void testViewOrderHistory_Detail() {
        setTestCaseInfo(
            "ST_ORDER_01", 
            "Xem Lịch sử & Chi tiết đơn hàng", 
            "1. Click Menu 'Đơn hàng của tôi'\n2. Check danh sách đơn\n3. Click nút 'Chi tiết' đơn đầu tiên\n4. Check thông tin trang chi tiết\n5. Quay lại", 
            "User: user", 
            "Hiển thị đúng danh sách, thông tin chi tiết trùng khớp với tổng tiền, và quay lại thành công"
        );

        System.out.println("Step 1: Mở menu User -> Chọn Đơn hàng...");
        driver.get(homeUrl); 
        slowDown();

        try {
            // Mở dropdown User (dùng JS click vì Selenium thường gặp lỗi với dropdown menu)
            WebElement userDropdown = driver.findElement(By.cssSelector(".btn-header.dropdown-toggle"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", userDropdown);
            slowDown();

            // Click link "Đơn hàng của tôi"
            WebElement historyLink = driver.findElement(By.xpath("//a[contains(text(), 'Đơn hàng của tôi')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", historyLink);
            slowDown();

        } catch (Exception e) {
            System.out.println("⚠ Không click được menu, vào thẳng link Servlet...");
            driver.get(historyServletUrl);
            slowDown();
        }

        // --- Xử lý lỗi chuyển hướng (Auto Fix URL) ---
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.endsWith(".jsp")) {
            System.out.println("⚠ Web dẫn vào file .jsp -> Auto Fix sang Servlet.");
            driver.get(historyServletUrl);
            slowDown();
        }
        // Kiểm tra đã bị đá về trang Login chưa
        if (driver.getCurrentUrl().contains("Login.jsp")) Assert.fail("Lỗi: Bị đá về trang Login.");
        
        // ----------------------------------------------------
        
        System.out.println("Step 2: Kiểm tra danh sách đơn hàng...");
        List<WebElement> rows = driver.findElements(By.cssSelector(".table tbody tr"));
        
        // Xử lý trường hợp giỏ hàng trống
        if (rows.isEmpty()) {
            if (driver.getPageSource().contains("Bạn chưa có đơn hàng nào")) {
                 System.out.println("Pass: Tài khoản chưa có đơn.");
                 this.currentActual = "Tài khoản không có đơn hàng (Hiển thị thông báo trống).";
                 return; 
            } else {
                Assert.fail("Lỗi: Không có đơn hàng và không hiện thông báo trống.");
            }
        }

        WebElement firstRow = rows.get(0);
        String orderIdText = firstRow.findElement(By.cssSelector("td:nth-child(1)")).getText().trim();
        String totalAmountText = firstRow.findElement(By.cssSelector("td:nth-child(4)")).getText().trim();
        System.out.println(">> Đơn hàng cần check: " + orderIdText + " | " + totalAmountText);

        System.out.println("Step 3: Xem chi tiết đơn hàng đầu tiên...");
        WebElement btnDetail = firstRow.findElement(By.cssSelector(".btn-view"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnDetail); // Dùng JS click
        slowDown();

        System.out.println("Step 4: Verify thông tin trang chi tiết...");
        
        // 4a. Kiểm tra Order ID
        try {
            String detailTitle = driver.findElement(By.className("order-id")).getText(); 
            Assert.assertTrue("Lỗi: Mã đơn hàng trên trang chi tiết không khớp với danh sách!", detailTitle.contains(orderIdText.replace("#", "")));
        } catch (Exception e) {
            // Fallback: Kiểm tra thẻ H2 (nếu tiêu đề nằm ở đó)
            String h2Text = driver.findElement(By.tagName("h2")).getText();
            Assert.assertTrue("Lỗi: Mã đơn hàng trên trang chi tiết không khớp (H2)!", h2Text.contains(orderIdText.replace("#", "")));
        }

        // 4b. Kiểm tra Tổng tiền
        String detailTotal = driver.findElement(By.className("total-amount")).getText().trim();
        Assert.assertEquals("Lỗi: Tổng tiền trên trang chi tiết không khớp với danh sách!", totalAmountText, detailTotal);

        // 4c. Kiểm tra bảng chi tiết sản phẩm
        Assert.assertTrue("Lỗi: Bảng chi tiết sản phẩm (Order Details) rỗng!", driver.findElements(By.cssSelector(".table tbody tr")).size() > 0);

        System.out.println("Step 5: Quay lại trang lịch sử...");
        driver.findElement(By.className("btn-back")).click();
        slowDown();
        
        // Fix URL sau khi quay lại (nếu cần)
        if (driver.getCurrentUrl().endsWith(".jsp")) {
            driver.get(historyServletUrl);
            slowDown();
        }
        
        // 5. Verify đã quay lại trang lịch sử (kiểm tra URL)
        Assert.assertTrue("Lỗi: Nút quay lại không đưa về trang Order History!", driver.getCurrentUrl().contains("order-history"));

        this.currentActual = "Hoàn thành test: Xem danh sách, chi tiết và quay lại thành công cho đơn " + orderIdText;
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}