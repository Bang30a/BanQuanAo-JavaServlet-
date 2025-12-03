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

import java.util.List;

public class AdminOrderSystemTest {

    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String adminDashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp"; 

    // [CẤU HÌNH] Tốc độ test chậm lại (3 giây) để dễ quan sát
    final int SLOW_SPEED = 3000;

    // --- BIẾN BÁO CÁO EXCEL ---
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_AdminOrder.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_AdminOrder.xlsx");
    }

    // Hàm làm chậm tiến trình test
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
        slowDown();
        
        // Dùng submit() ở ô password để tránh click nhầm nút khác
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("admin123");
        slowDown();
        
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click(); 
        }
        slowDown();
        
        // Đảm bảo vào Dashboard
        if (!driver.getCurrentUrl().contains("admin")) {
            driver.get(adminDashboardUrl);
            slowDown();
        }
    }

    // ================================================================
    // CÁC TEST CASE QUẢN LÝ ĐƠN HÀNG (ADMIN)
    // ================================================================

    // --- CASE 1: XEM & LỌC ĐƠN HÀNG ---
    @Test
    public void testAdmin_ViewAndFilterOrders() {
        setTestCaseInfo(
            "ST_ADMIN_ORDER_01", 
            "Xem & Lọc đơn hàng", 
            "1. Click Menu 'Quản lý hóa đơn'\n2. Chuyển vào iframe\n3. Lọc 'Chờ xử lý'", 
            "Filter: Chờ xử lý", 
            "Hiển thị danh sách các đơn có trạng thái 'Chờ xử lý'"
        );

        // 1. Click Menu bên Sidebar
        try {
            driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy menu 'Quản lý hóa đơn'!");
        }
        slowDown(); 

        // 2. Chuyển vào iframe chứa nội dung chính
        try {
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            Assert.fail("Không tìm thấy iframe mainFrame!");
        }

        // 3. Thực hiện lọc đơn hàng
        try {
            Select select = new Select(driver.findElement(By.name("status")));
            select.selectByVisibleText("Chờ xử lý"); // Chọn status
            slowDown();
            
            driver.findElement(By.className("btn-filter")).click(); // Bấm lọc
            slowDown();
        } catch (Exception e) {
            Assert.fail("Lỗi thao tác với bộ lọc status!");
        }

        // 4. Kiểm tra kết quả trong bảng
        List<WebElement> statusDropdowns = driver.findElements(By.cssSelector("table tbody select[name='status']"));
        
        if (statusDropdowns.isEmpty()) {
            this.currentActual = "Danh sách rỗng.";
        } else {
            // Lấy status của dòng đầu tiên để kiểm tra
            String selectedOption = new Select(statusDropdowns.get(0)).getFirstSelectedOption().getText();
            this.currentActual = "Trạng thái hiển thị: " + selectedOption;
            Assert.assertTrue("Lọc sai! Mong đợi 'Chờ xử lý'", selectedOption.contains("Chờ xử lý"));
        }
        
        driver.switchTo().defaultContent(); // Thoát iframe
    }

    // --- CASE 2: CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG ---
    @Test
    public void testAdmin_UpdateOrderStatus() {
        setTestCaseInfo(
            "ST_ADMIN_ORDER_02", 
            "Cập nhật trạng thái đơn hàng", 
            "1. Vào Menu -> iframe\n2. Chọn đơn đầu tiên\n3. Đổi sang 'Đang giao hàng'", 
            "Status: Đang giao hàng", 
            "Trạng thái được cập nhật thành công"
        );

        // 1. Vào trang quản lý
        driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        slowDown();
        driver.switchTo().frame("mainFrame");

        List<WebElement> statusDropdowns = driver.findElements(By.cssSelector("table tbody select[name='status']"));
        if (statusDropdowns.isEmpty()) {
            this.currentActual = "Không có đơn hàng.";
            driver.switchTo().defaultContent();
            return; 
        }

        // 2. Đổi trạng thái đơn đầu tiên
        Select select = new Select(statusDropdowns.get(0));
        select.selectByValue("Đang giao hàng"); 
        slowDown(); // Chờ trang reload sau khi đổi (do onchange="submit")

        // 3. Kiểm tra lại giá trị sau khi reload
        WebElement updatedDropdown = driver.findElements(By.cssSelector("table tbody select[name='status']")).get(0);
        String newStatus = new Select(updatedDropdown).getFirstSelectedOption().getAttribute("value");

        this.currentActual = "Trạng thái mới: " + newStatus;
        Assert.assertEquals("Cập nhật thất bại!", "Đang giao hàng", newStatus);
        
        driver.switchTo().defaultContent();
    }

    // --- CASE 3: XEM CHI TIẾT ĐƠN HÀNG ---
    @Test
    public void testAdmin_ViewOrderDetail() {
        setTestCaseInfo(
            "ST_ADMIN_ORDER_03", 
            "Admin xem chi tiết đơn hàng", 
            "1. Vào Menu -> iframe\n2. Click nút 'Chi tiết' (Mắt)\n3. Check trang chi tiết", 
            "Click icon 👁️", 
            "Chuyển trang & Hiển thị đúng thông tin"
        );

        driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        slowDown();
        driver.switchTo().frame("mainFrame");

        List<WebElement> viewButtons = driver.findElements(By.className("btn-view"));
        if (viewButtons.isEmpty()) {
            this.currentActual = "Không có đơn hàng.";
            driver.switchTo().defaultContent();
            return;
        }

        // Lấy ID đơn hàng để đối chiếu
        String orderIdText = driver.findElement(By.cssSelector("table tbody tr:first-child td:first-child")).getText();
        
        // 1. Click nút xem chi tiết
        viewButtons.get(0).click();
        slowDown();

        // 2. Kiểm tra tiêu đề trang chi tiết
        try {
            String titleText = driver.findElement(By.tagName("h2")).getText();
            this.currentActual = "Tiêu đề: " + titleText;
            // So sánh ID trong tiêu đề với ID ở danh sách
            Assert.assertTrue("Sai trang chi tiết!", titleText.contains(orderIdText.replace("#", "")));
        } catch (Exception e) {
            Assert.fail("Không tìm thấy tiêu đề trang chi tiết!");
        }

        // 3. Kiểm tra bảng sản phẩm
        boolean hasTable = driver.findElements(By.cssSelector("table tbody tr")).size() > 0;
        Assert.assertTrue("Bảng chi tiết trống!", hasTable);
        
        // 4. Test nút Quay lại
        try {
            driver.findElement(By.className("btn-back")).click();
            slowDown();
            boolean hasFilter = driver.findElements(By.className("btn-filter")).size() > 0;
            Assert.assertTrue("Nút quay lại lỗi!", hasFilter);
        } catch (Exception e) {
            System.out.println("Lỗi nút quay lại.");
        }

        driver.switchTo().defaultContent();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}