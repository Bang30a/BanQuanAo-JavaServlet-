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

// Chạy theo thứ tự: Xem thống kê -> Xuất Excel
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatisticsSystemTest {

    WebDriver driver;

    // Cấu hình URL
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- CẤU HÌNH NGÀY TEST ---
    // Định dạng yyyy-MM-dd để Javascript hiểu và điền vào input type="date"
    String startDateVal = "2025-10-01"; 
    String endDateVal = "2025-12-31";   

    // Tốc độ test (ms)
    final int SLOW_SPEED = 1500;

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Statistics.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Statistics.xlsx");
    }

    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

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

    // --- TEST 01: XEM THỐNG KÊ (FILTER) ---
    @Test
    public void test01_FilterStatistics() {
        setTestCaseInfo(
            "ST_STAT_01",
            "Lọc thống kê theo ngày",
            "1. Menu -> Báo cáo chi tiết\n2. Nhập ngày " + startDateVal + " - " + endDateVal + "\n3. Bấm Xem kết quả",
            "Range: " + startDateVal + " đến " + endDateVal,
            "Hiển thị doanh thu và danh sách sản phẩm"
        );

        // 1. Điều hướng vào trang Thống kê
        navigateToStatisticsPage();

        // 2. Nhập ngày bắt đầu (Dùng biến chung)
        WebElement startInput = driver.findElement(By.name("startDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + startDateVal + "'", startInput);
        slowDown();

        // 3. Nhập ngày kết thúc (Dùng biến chung)
        WebElement endInput = driver.findElement(By.name("endDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + endDateVal + "'", endInput);
        slowDown();

        // 4. Click nút "Xem kết quả" (btn-filter)
        WebElement filterBtn = driver.findElement(By.className("btn-filter"));
        filterBtn.click();
        slowDown();

        // 5. Kiểm tra kết quả
        try {
            WebElement revenueElement = driver.findElement(By.cssSelector(".stat-value.text-blue"));
            
            // Tìm bảng dữ liệu
            List<WebElement> rows = driver.findElements(By.cssSelector(".table-wrap table tbody tr"));

            this.currentActual = "Doanh thu hiển thị: " + revenueElement.getText() + " | Số dòng SP: " + rows.size();
            
            // Assert: Phải hiển thị doanh thu
            Assert.assertTrue("Không tìm thấy số liệu doanh thu!", revenueElement.isDisplayed());
            
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
            "1. Giữ nguyên bộ lọc ngày (" + startDateVal + " - " + endDateVal + ")\n2. Bấm nút 'Xuất file Excel'\n3. Kiểm tra không bị lỗi server",
            "Action: Click Export",
            "Trình duyệt tải file, không báo lỗi"
        );

        // 1. Điều hướng lại
        navigateToStatisticsPage();
        
        // Set lại ngày (Dùng biến chung)
        WebElement startInput = driver.findElement(By.name("startDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + startDateVal + "'", startInput);
        
        WebElement endInput = driver.findElement(By.name("endDate"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + endDateVal + "'", endInput);
        slowDown();

        // 2. Tìm và bấm nút Excel (class btn-excel)
        WebElement excelBtn = driver.findElement(By.className("btn-excel"));
        excelBtn.click();
        
        // Chờ request xử lý
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // 3. Kiểm tra:
        String currentUrl = driver.getCurrentUrl();
        boolean isErrorPage = driver.getPageSource().contains("HTTP Status 500") || driver.getPageSource().contains("Exception");
        
        this.currentActual = "URL hiện tại: " + currentUrl + " | Có lỗi server: " + isErrorPage;
        
        Assert.assertFalse("Lỗi: Server báo lỗi 500/Exception khi xuất Excel!", isErrorPage);
        Assert.assertTrue("Nút xuất Excel phải hiển thị và click được", excelBtn.isEnabled());
    }

    // --- UTILITIES ---

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

    private void navigateToStatisticsPage() {
        // Thoát ra khỏi iframe để bấm menu
        driver.switchTo().defaultContent();

        try {
            // Click menu "Báo cáo chi tiết"
            WebElement statsLink = driver.findElement(By.xpath("//a[contains(@href, 'statistics')]"));
            statsLink.click();
            slowDown();

            // Vào iframe mainFrame
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            // Fallback: Vào thẳng URL với ngày mặc định
            driver.get("http://localhost:8080/ShopDuck/admin/statistics?startDate=" + startDateVal + "&endDate=" + endDateVal);
        }
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}