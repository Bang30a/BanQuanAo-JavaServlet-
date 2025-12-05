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

/**
 * System Test (Selenium) cho chức năng Quản lý Đơn hàng trong trang Admin.
 * Kiểm thử các nghiệp vụ xem, lọc và cập nhật trạng thái đơn hàng.
 */
public class AdminOrderSystemTest {

    WebDriver driver;
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String adminDashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp"; 

    // [CẤU HÌNH] Tốc độ làm chậm (milliseconds) để dễ dàng quan sát quá trình chạy test
    private final int SLOW_SPEED = 3000;

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại (trước khi chạy).
     *
     * @param id ID của Test Case (Ví dụ: ST_ADMIN_ORDER_01).
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
        this.currentActual = "Chưa hoàn thành"; // Reset trạng thái thực tế
    }

    /**
     * Rule giúp ghi kết quả Test Case (PASS/FAIL) vào Excel sau khi mỗi @Test hoàn thành.
     */
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            // Ghi kết quả thành công, lấy giá trị currentActual đã được set trong @Test
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_AdminOrder.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_AdminOrder.xlsx");
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
        
        // 1. Điều hướng đến trang Login và đăng nhập Admin
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        
        // Nhập mật khẩu và thực hiện submit form (thay vì click nút)
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("admin123");
        slowDown();
        
        try {
            passField.submit();
        } catch (Exception e) {
            // Fallback nếu submit không hoạt động: click nút login
            driver.findElement(By.cssSelector(".btn-login")).click(); 
        }
        slowDown();
        
        // Đảm bảo đang ở trong khu vực Admin (Admin Dashboard)
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
            "1. Click Menu 'Quản lý hóa đơn'\n2. Chuyển vào iframe\n3. Chọn và Lọc 'Chờ xử lý'", 
            "Filter: Chờ xử lý", 
            "Hiển thị danh sách các đơn có trạng thái 'Chờ xử lý'"
        );

        // 1. Click Menu Sidebar "Quản lý hóa đơn"
        try {
            driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy menu 'Quản lý hóa đơn'!");
        }
        slowDown(); 

        // 2. Chuyển ngữ cảnh sang iframe chứa nội dung chính
        try {
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            Assert.fail("Không tìm thấy iframe mainFrame!");
        }

        // 3. Thực hiện lọc đơn hàng theo trạng thái "Chờ xử lý"
        try {
            Select select = new Select(driver.findElement(By.name("status")));
            select.selectByVisibleText("Chờ xử lý"); // Chọn trạng thái
            slowDown();
            
            driver.findElement(By.className("btn-filter")).click(); // Bấm nút lọc
            slowDown();
        } catch (Exception e) {
            Assert.fail("Lỗi thao tác với bộ lọc trạng thái!");
        }

        // 4. Kiểm tra kết quả trong bảng
        List<WebElement> statusDropdowns = driver.findElements(By.cssSelector("table tbody select[name='status']"));
        
        if (statusDropdowns.isEmpty()) {
            this.currentActual = "Danh sách rỗng (Không có đơn hàng 'Chờ xử lý').";
        } else {
            // Lấy trạng thái của đơn hàng đầu tiên để kiểm tra tính chính xác của bộ lọc
            String selectedOption = new Select(statusDropdowns.get(0)).getFirstSelectedOption().getText();
            this.currentActual = "Trạng thái hiển thị: " + selectedOption;
            Assert.assertTrue("Lọc sai! Trạng thái đơn hàng đầu tiên phải là 'Chờ xử lý'", selectedOption.contains("Chờ xử lý"));
        }
        
        driver.switchTo().defaultContent(); // Thoát iframe
    }

    // --- CASE 2: CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG ---
    @Test
    public void testAdmin_UpdateOrderStatus() {
        setTestCaseInfo(
            "ST_ADMIN_ORDER_02", 
            "Cập nhật trạng thái đơn hàng", 
            "1. Vào Menu -> iframe\n2. Chọn dropdown đơn đầu tiên\n3. Đổi trạng thái sang 'Đang giao hàng'", 
            "Status: Đang giao hàng", 
            "Trạng thái được cập nhật thành công và hiển thị 'Đang giao hàng'"
        );

        // 1. Điều hướng và chuyển vào iframe
        driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        slowDown();
        driver.switchTo().frame("mainFrame");

        List<WebElement> statusDropdowns = driver.findElements(By.cssSelector("table tbody select[name='status']"));
        if (statusDropdowns.isEmpty()) {
            this.currentActual = "Không có đơn hàng để cập nhật.";
            driver.switchTo().defaultContent();
            return; 
        }

        // 2. Đổi trạng thái đơn đầu tiên
        Select select = new Select(statusDropdowns.get(0));
        select.selectByValue("Đang giao hàng"); // Chọn trạng thái mới
        slowDown(); // Chờ trang reload (Giả định trang reload sau khi thay đổi trạng thái)

        // 3. Kiểm tra lại giá trị sau khi reload/cập nhật
        WebElement updatedDropdown = driver.findElements(By.cssSelector("table tbody select[name='status']")).get(0);
        String newStatus = new Select(updatedDropdown).getFirstSelectedOption().getAttribute("value");

        this.currentActual = "Trạng thái mới: " + newStatus;
        Assert.assertEquals("Cập nhật trạng thái thất bại!", "Đang giao hàng", newStatus);
        
        driver.switchTo().defaultContent();
    }

    // --- CASE 3: XEM CHI TIẾT ĐƠN HÀNG ---
    @Test
    public void testAdmin_ViewOrderDetail() {
        setTestCaseInfo(
            "ST_ADMIN_ORDER_03", 
            "Admin xem chi tiết đơn hàng", 
            "1. Vào Menu -> iframe\n2. Click nút 'Chi tiết' (Mắt) đơn đầu tiên\n3. Kiểm tra nội dung trang", 
            "Click icon 👁️", 
            "Chuyển trang thành công, tiêu đề chứa Order ID và Bảng chi tiết sản phẩm không trống"
        );

        driver.findElement(By.partialLinkText("Quản lý hóa đơn")).click();
        slowDown();
        driver.switchTo().frame("mainFrame");

        List<WebElement> viewButtons = driver.findElements(By.className("btn-view"));
        if (viewButtons.isEmpty()) {
            this.currentActual = "Không có đơn hàng để xem chi tiết.";
            driver.switchTo().defaultContent();
            return;
        }

        // Lấy ID đơn hàng từ bảng danh sách để đối chiếu trong trang chi tiết
        String orderIdText = driver.findElement(By.cssSelector("table tbody tr:first-child td:first-child")).getText();
        
        // 1. Click nút xem chi tiết (Mắt)
        viewButtons.get(0).click();
        slowDown();

        // 2. Kiểm tra tiêu đề trang chi tiết
        try {
            String titleText = driver.findElement(By.tagName("h2")).getText();
            this.currentActual = "Tiêu đề: " + titleText;
            // Xác minh tiêu đề chứa ID đơn hàng
            Assert.assertTrue("Tiêu đề trang chi tiết không chứa Order ID hoặc sai cấu trúc!", titleText.contains(orderIdText.replace("#", "")));
        } catch (Exception e) {
            Assert.fail("Không tìm thấy tiêu đề trang chi tiết!");
        }

        // 3. Kiểm tra bảng sản phẩm (Chi tiết đơn hàng)
        boolean hasTable = driver.findElements(By.cssSelector("table tbody tr")).size() > 0;
        Assert.assertTrue("Bảng chi tiết sản phẩm (Order Details) trống!", hasTable);
        
        // 4. Kiểm tra nút Quay lại (Navigation test)
        try {
            driver.findElement(By.className("btn-back")).click();
            slowDown();
            // Sau khi quay lại, kiểm tra xem có thấy bộ lọc status không
            boolean hasFilter = driver.findElements(By.className("btn-filter")).size() > 0;
            Assert.assertTrue("Nút quay lại không điều hướng về trang danh sách đơn hàng!", hasFilter);
        } catch (Exception e) {
            System.out.println("Lỗi nút quay lại.");
        }

        driver.switchTo().defaultContent();
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}