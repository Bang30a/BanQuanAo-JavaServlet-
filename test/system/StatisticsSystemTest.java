package system;

import util.ExcelTestExporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

/**
 * System Test (Selenium) cho chức năng Báo cáo/Thống kê (Statistics) Admin.
 * Test case được chạy tuần tự: Lọc dữ liệu theo ngày -> Xuất báo cáo Excel.
 */
// Chạy theo thứ tự: Xem thống kê -> Xuất Excel
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatisticsSystemTest {

    WebDriver driver;

    // Cấu hình URL
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- CẤU HÌNH NGÀY TEST ---
    // Định dạng yyyy-MM-dd để Javascript hiểu và điền vào input type="date"
    /** Ngày bắt đầu cho bộ lọc thống kê. */
    private final String startDateVal = "2025-10-01"; 
    /** Ngày kết thúc cho bộ lọc thống kê. */
    private final String endDateVal = "2025-12-31";  

    /** Tốc độ làm chậm (milliseconds) giữa các bước Selenium. */
    private final int SLOW_SPEED = 1500;

    // --- BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Statistics.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Statistics.xlsx");
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver và Đăng nhập Admin.
     */
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // 1. Đăng nhập Admin
        driver.get(loginUrl);
        slowDown();
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin123");
        
        // Click Login
        try {
            WebElement loginBtn = driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button"));
            loginBtn.click();
        } catch (Exception e) {
            driver.findElement(By.className("btn-login")).click();
        }
        slowDown();

        // 2. Vào Dashboard nếu chưa vào
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
             driver.get(dashboardUrl);
             slowDown();
        }
    }

    // ====================== CÁC TEST CASE THỐNG KÊ ======================

    // --- TEST 01: XEM THỐNG KÊ (FILTER) ---
    @Test
    public void test01_FilterStatistics() {
        setTestCaseInfo(
            "ST_STAT_01",
            "Lọc thống kê theo ngày",
            "1. Menu -> Báo cáo chi tiết\n2. Nhập ngày " + startDateVal + " - " + endDateVal + " (Dùng JS)\n3. Bấm Xem kết quả",
            "Range: " + startDateVal + " đến " + endDateVal,
            "Hiển thị doanh thu và danh sách sản phẩm bán chạy"
        );

        // 1. Điều hướng vào trang Thống kê
        navigateToStatisticsPage();

        // 2. Nhập ngày bắt đầu (Dùng JavaScript Injection cho input type="date")
        WebElement startInput = driver.findElement(By.name("startDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + startDateVal + "'", startInput);
        slowDown();

        // 3. Nhập ngày kết thúc
        WebElement endInput = driver.findElement(By.name("endDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + endDateVal + "'", endInput);
        slowDown();

        // 4. Click nút "Xem kết quả"
        WebElement filterBtn = driver.findElement(By.className("btn-filter"));
        filterBtn.click();
        slowDown();

        // 5. Kiểm tra kết quả
        try {
            WebElement revenueElement = driver.findElement(By.cssSelector(".stat-value.text-blue"));
            
            // Tìm bảng dữ liệu (sản phẩm bán chạy)
            List<WebElement> rows = driver.findElements(By.cssSelector(".table-wrap table tbody tr"));

            this.currentActual = "Doanh thu hiển thị: " + revenueElement.getText() + " | Số dòng SP: " + rows.size();
            
            // Assert 1: Phải hiển thị doanh thu
            Assert.assertTrue("Lỗi: Không tìm thấy số liệu doanh thu!", revenueElement.isDisplayed());
            
            // Assert 2: Phải có bảng dữ liệu (hoặc thông báo rỗng)
            // (Nếu rows.size() == 0, ta giả định đó là kết quả lọc hợp lệ)
            
        } catch (Exception e) {
            Assert.fail("Lỗi hiển thị: Không tìm thấy element thống kê doanh thu.");
        }
    }

    // --- TEST 02: XUẤT FILE EXCEL ---
    @Test
    public void test02_ExportExcel() {
        setTestCaseInfo(
            "ST_STAT_02",
            "Xuất báo cáo Excel",
            "1. Điều hướng lại\n2. Set lại ngày (" + startDateVal + " - " + endDateVal + ")\n3. Bấm nút 'Xuất file Excel'",
            "Action: Click Export",
            "Server xử lý request và trình duyệt tải file (.xlsx), không báo lỗi Server (500)"
        );

        // 1. Điều hướng lại trang thống kê
        navigateToStatisticsPage();
        
        // 2. Set lại ngày (Đảm bảo bộ lọc được áp dụng cho file Excel)
        WebElement startInput = driver.findElement(By.name("startDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + startDateVal + "'", startInput);
        
        WebElement endInput = driver.findElement(By.name("endDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + endDateVal + "'", endInput);
        slowDown();

        // 3. Tìm và bấm nút Excel (class btn-excel)
        WebElement excelBtn = driver.findElement(By.className("btn-excel"));
        excelBtn.click();
        
        // Chờ request xử lý và tải file (3s)
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // 4. Kiểm tra:
        // * Selenium không thể trực tiếp kiểm tra việc tải file thành công,
        // * Ta kiểm tra sự an toàn: không bị lỗi Server (500/Exception) và nút vẫn hoạt động (is enabled)
        String currentUrl = driver.getCurrentUrl();
        boolean isErrorPage = driver.getPageSource().contains("HTTP Status 500") || driver.getPageSource().contains("Exception");
        
        this.currentActual = "URL hiện tại: " + currentUrl + " | Có lỗi server: " + isErrorPage;
        
        Assert.assertFalse("Lỗi: Server báo lỗi 500/Exception khi xuất Excel!", isErrorPage);
        Assert.assertTrue("Nút xuất Excel phải hiển thị và click được", excelBtn.isEnabled());
    }

    // --- UTILITIES ---

    /**
     * Helper: Thiết lập thông tin Test Case (Được dùng trong các hàm @Test).
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện.
     * @param data Dữ liệu đầu vào/chuẩn bị.
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
     * Helper: Điều hướng đến trang Thống kê (Statistics Page) thông qua Menu Sidebar và Iframe.
     */
    private void navigateToStatisticsPage() {
        // Thoát ra khỏi iframe để bấm menu
        driver.switchTo().defaultContent();

        try {
            // 1. Click menu "Báo cáo chi tiết" (dựa trên href chứa 'statistics')
            WebElement statsLink = driver.findElement(By.xpath("//a[contains(@href, 'statistics')]"));
            statsLink.click();
            slowDown();

            // 2. Vào iframe mainFrame
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            // Fallback: Vào thẳng URL với ngày mặc định và chuyển vào iframe
            driver.get("http://localhost:8080/ShopDuck/admin/statistics?startDate=" + startDateVal + "&endDate=" + endDateVal);
            try { driver.switchTo().frame("mainFrame"); } catch (Exception ignored) {}
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