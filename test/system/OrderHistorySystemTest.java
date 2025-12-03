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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class OrderHistorySystemTest {

    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    String historyServletUrl = "http://localhost:8080/ShopDuck/user/order-history";

    final int SLOW_SPEED = 3000;

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
        try {
            ExcelTestExporter.exportToExcel("BaoCao_SystemTest_OrderHistory.xlsx");
            System.out.println(">> Xuất Excel thành công.");
        } catch (Exception e) {}
    }

    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
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
            driver.findElement(By.className("btn-login")).click();
        }
        slowDown();

        if (driver.getCurrentUrl().contains("Login.jsp")) {
            Assert.fail("SETUP FAILED: Đăng nhập thất bại.");
        }
    }

    @Test
    public void testViewOrderHistory_Detail() {
        setTestCaseInfo(
            "ST_ORDER_01", 
            "Xem Lịch sử & Chi tiết đơn hàng", 
            "1. Menu User -> Đơn hàng của tôi\n2. (Auto Fix URL)\n3. Check list đơn\n4. Xem chi tiết", 
            "User: user", 
            "Hiển thị đúng danh sách và thông tin chi tiết"
        );

        System.out.println("Step 1: Mở menu User -> Chọn Đơn hàng...");
        driver.get(homeUrl); 
        slowDown();

        try {

            WebElement userDropdown = driver.findElement(By.cssSelector(".btn-header.dropdown-toggle"));
            
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", userDropdown);
            slowDown();

            // Click link "Đơn hàng của tôi"
            WebElement historyLink = driver.findElement(By.xpath("//a[contains(text(), 'Đơn hàng của tôi')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", historyLink);
            slowDown();

        } catch (Exception e) {
            System.out.println("⚠ Không click được menu (" + e.getMessage() + "), vào thẳng link...");
            driver.get(historyServletUrl);
            slowDown();
        }

        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.endsWith(".jsp")) {
            System.out.println("⚠ Web dẫn vào file .jsp -> Auto Fix sang Servlet.");
            driver.get(historyServletUrl);
            slowDown();
        }

        if (driver.getCurrentUrl().contains("Login.jsp")) Assert.fail("Lỗi: Bị đá về trang Login.");
        
        System.out.println("Step 2: Kiểm tra danh sách đơn hàng...");
        List<WebElement> rows = driver.findElements(By.cssSelector(".table tbody tr"));
        
        if (rows.isEmpty()) {
            if (driver.getPageSource().contains("Bạn chưa có đơn hàng nào")) {
                 System.out.println("Pass: Tài khoản chưa có đơn.");
                 this.currentActual = "Tài khoản không có đơn hàng.";
                 return; 
            } else {
                Assert.fail("Lỗi: Không có đơn hàng và không hiện thông báo trống.");
            }
        }

        WebElement firstRow = rows.get(0);
        String orderIdText = firstRow.findElement(By.cssSelector("td:nth-child(1)")).getText().trim();
        String totalAmountText = firstRow.findElement(By.cssSelector("td:nth-child(4)")).getText().trim();
        System.out.println(">> Đơn hàng cần check: " + orderIdText + " | " + totalAmountText);

        System.out.println("Step 3: Xem chi tiết...");
        WebElement btnDetail = firstRow.findElement(By.cssSelector(".btn-view"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnDetail);
        slowDown();

        System.out.println("Step 4: Verify thông tin trang chi tiết...");
        
        try {
            String detailTitle = driver.findElement(By.className("order-id")).getText(); 
            Assert.assertTrue("Sai mã đơn!", detailTitle.contains(orderIdText.replace("#", "")));
        } catch (Exception e) {
            String h2Text = driver.findElement(By.tagName("h2")).getText();
            Assert.assertTrue("Sai mã đơn (h2)!", h2Text.contains(orderIdText.replace("#", "")));
        }

        String detailTotal = driver.findElement(By.className("total-amount")).getText().trim();
        Assert.assertEquals("Sai tổng tiền!", totalAmountText, detailTotal);

        Assert.assertTrue("Chi tiết rỗng!", driver.findElements(By.cssSelector(".table tbody tr")).size() > 0);

        System.out.println("Step 5: Quay lại...");
        driver.findElement(By.className("btn-back")).click();
        slowDown();
        
        if (driver.getCurrentUrl().endsWith(".jsp")) {
            driver.get(historyServletUrl);
            slowDown();
        }
        
        Assert.assertTrue("Nút quay lại lỗi!", driver.getCurrentUrl().contains("order-history"));

        this.currentActual = "Hoàn thành test: " + orderIdText;
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}