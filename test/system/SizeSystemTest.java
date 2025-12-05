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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Random;

/**
 * System Test (Selenium) cho chức năng Quản lý Kích cỡ (Sizes) Admin.
 * Test case được chạy tuần tự: Tạo -> Sửa -> Xóa.
 */
// QUAN TRỌNG: Chạy theo thứ tự Create -> Update -> Delete
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SizeSystemTest {

    WebDriver driver;
    
    // Cấu hình đường dẫn
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- CẤU HÌNH TEST DATA (Dùng số ngẫu nhiên để đảm bảo tính duy nhất) ---
    /** Số ngẫu nhiên duy nhất cho mỗi lần chạy test. */
    static int uniqueSeed = new Random().nextInt(9000) + 1000;
    
    /** Tên Size ban đầu (dùng cho Create). */
    private final String initialSizeName = "XXL_" + uniqueSeed; 
    /** Tên Size sau khi cập nhật (dùng cho Update). */
    private final String updatedSizeName = "XXL_Updated_" + uniqueSeed;

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Sizes.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Sizes.xlsx");
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
        
        // --- 1. LUỒNG ĐĂNG NHẬP ---
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        driver.findElement(By.name("password")).sendKeys("admin123");
        slowDown();
        
        try {
            WebElement loginBtn;
            try {
                // Tìm nút submit bằng xpath/button
                loginBtn = driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button"));
            } catch (Exception e) {
                // Fallback: click nút login bằng class
                loginBtn = driver.findElement(By.className("btn-login"));
            }
            loginBtn.click();
        } catch (Exception e) {
            System.out.println("Không click được nút login: " + e.getMessage());
        }
        slowDown();
        
        // Đảm bảo đã vào Dashboard
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
            driver.get(dashboardUrl);
            slowDown();
        }
    }

    // ====================== CÁC TEST CASE CRUD (TUẦN TỰ) ======================

    // --- TEST 01: CREATE SIZE XXL ---
    @Test
    public void test01_Size_Create() {
        setTestCaseInfo(
            "ST_SIZE_01", 
            "Thêm Size Mới", 
            "1. Dashboard -> Menu Quản lý SP -> Kích cỡ\n2. Click Thêm Size\n3. Nhập '" + initialSizeName + "'\n4. Lưu", 
            "Input: " + initialSizeName, 
            "Size mới xuất hiện trong danh sách"
        );

        navigateToSizePageViaMenu(); // Điều hướng đến trang quản lý Size

        // Click nút Thêm
        driver.findElement(By.className("btn-add")).click();
        slowDown();

        // Điền input
        fillInput("sizeLabel", initialSizeName);
        slowDown();

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown(); 

        // [Verify] Kiểm tra sự tồn tại của size mới
        boolean found = findRowByText(initialSizeName);
        this.currentActual = "Tìm thấy size '" + initialSizeName + "': " + found;
        Assert.assertTrue("Lỗi: Thêm xong nhưng không tìm thấy size trong danh sách!", found);
    }

    // --- TEST 02: UPDATE SIZE ---
    @Test
    public void test02_Size_Update() {
        setTestCaseInfo(
            "ST_SIZE_02", 
            "Sửa tên Size", 
            "1. Tìm size '" + initialSizeName + "'\n2. Click Sửa\n3. Đổi thành '" + updatedSizeName + "'\n4. Lưu", 
            "Old: " + initialSizeName + " -> New: " + updatedSizeName, 
            "Tên size được cập nhật thành công (Tên cũ biến mất)"
        );

        navigateToSizePageViaMenu(); // Điều hướng đến trang quản lý Size

        // 1. Tìm và Click nút Sửa (dựa trên tên size ban đầu)
        boolean clickEditSuccess = findAndClickButton(initialSizeName, "btn-edit");
        Assert.assertTrue("Lỗi: Không tìm thấy nút Sửa cho size: " + initialSizeName, clickEditSuccess);
        
        slowDown(); 

        // 2. Sửa tên size
        fillInput("sizeLabel", updatedSizeName);
        slowDown();

        // 3. Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        // [Verify] Kiểm tra tên size mới và tên size cũ
        boolean foundNew = findRowByText(updatedSizeName);
        boolean foundOld = findRowByText(initialSizeName);
        
        this.currentActual = "Tìm thấy mới: " + foundNew + " | Tìm thấy cũ: " + foundOld;
        Assert.assertTrue("Lỗi: Không tìm thấy tên size mới sau khi sửa!", foundNew);
        Assert.assertFalse("Lỗi: Tên size cũ vẫn còn tồn tại trong danh sách!", foundOld);
    }

    // --- TEST 03: DELETE SIZE ---
    @Test
    public void test03_Size_Delete() {
        setTestCaseInfo(
            "ST_SIZE_03", 
            "Xóa Size", 
            "1. Tìm size '" + updatedSizeName + "'\n2. Click Xóa\n3. Xác nhận Alert\n4. Kiểm tra biến mất", 
            "Target: " + updatedSizeName, 
            "Size bị xóa khỏi danh sách (Không còn tồn tại)"
        );

        navigateToSizePageViaMenu(); // Điều hướng đến trang quản lý Size

        // 1. Tìm và Click nút Xóa (dựa trên tên size đã update)
        boolean clickDeleteSuccess = findAndClickButton(updatedSizeName, "btn-delete");
        Assert.assertTrue("Lỗi: Không tìm thấy nút Xóa cho size: " + updatedSizeName, clickDeleteSuccess);

        // 2. Xử lý Alert xác nhận
        try {
            slowDown(); 
            driver.switchTo().alert().accept(); // Bấm OK
            slowDown(); 
        } catch (Exception ignored) {}

        // [Verify] Kiểm tra size không còn tồn tại
        boolean found = findRowByText(updatedSizeName);
        this.currentActual = "Vẫn tìm thấy size '" + updatedSizeName + "': " + found;
        Assert.assertFalse("Lỗi: Xóa xong nhưng vẫn tìm thấy dữ liệu trong bảng!", found);
    }

    // --- CÁC HÀM HỖ TRỢ (UTILITIES) ---

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
     * Helper: Điều hướng đến trang Quản lý Kích cỡ thông qua Menu Sidebar và Iframe.
     */
    private void navigateToSizePageViaMenu() {
        // 1. Luôn thoát ra khỏi iframe để tương tác với menu
        driver.switchTo().defaultContent();

        // 2. Chuyển hướng đến Dashboard nếu cần (Đảm bảo đang ở trang admin)
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
             driver.get(dashboardUrl);
             slowDown();
        }

        try {
            // 3. Click menu cha "Quản lý sản phẩm"
            WebElement productMenu = driver.findElement(By.xpath("//a[contains(text(), 'Quản lý sản phẩm')]"));
            productMenu.click();
            slowDown();

            // 4. Click menu con "Kích cỡ"
            WebElement sizeLink = driver.findElement(By.xpath("//a[contains(text(), 'Kích cỡ')]"));
            sizeLink.click(); 
            slowDown();

            // 5. QUAN TRỌNG: Chuyển driver vào iframe "mainFrame"
            driver.switchTo().frame("mainFrame");

        } catch (Exception e) {
            System.out.println("Lỗi điều hướng menu: " + e.getMessage());
            // Fallback: Vào trực tiếp URL và chuyển vào iframe
            driver.get("http://localhost:8080/ShopDuck/admin/SizesManagerServlet?action=List");
            try { driver.switchTo().frame("mainFrame"); } catch (Exception ignored) {}
        }
    }

    /**
     * Helper: Điền giá trị vào trường input dựa trên thuộc tính 'name' (nameAttr).
     * Bao gồm cả logic xóa nội dung cũ (Ctrl+A, Backspace).
     *
     * @param nameAttr Tên thuộc tính 'name' của input.
     * @param value Giá trị cần nhập.
     */
    private void fillInput(String nameAttr, String value) {
        try {
            WebElement input = driver.findElement(By.name(nameAttr));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.BACK_SPACE);
            slowDown();
            input.sendKeys(value);
        } catch (Exception e) {
            System.out.println("Lỗi nhập liệu vào field " + nameAttr + ": " + e.getMessage());
        }
    }

    /**
     * Helper: Tìm dòng chứa đoạn text cụ thể trong bảng và kiểm tra sự tồn tại.
     * (Không cần hỗ trợ phân trang do bảng Size thường không lớn).
     *
     * @param textValue Đoạn text cần tìm (tên size).
     * @return True nếu tìm thấy, ngược lại False.
     */
    private boolean findRowByText(String textValue) {
        try {
            // Tìm trong bảng (giả định có id="sizeTable") hoặc tìm trong body
            int count = driver.findElements(By.xpath("//table//td[contains(., '" + textValue + "')]")).size();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper: Tìm dòng chứa đoạn text cụ thể và bấm nút có class tương ứng (Sửa/Xóa).
     *
     * @param textToFind Đoạn text để xác định dòng (tên size).
     * @param buttonClass Class của nút cần click (ví dụ: 'btn-edit' hoặc 'btn-delete').
     * @return True nếu click thành công, ngược lại False.
     */
    private boolean findAndClickButton(String textToFind, String buttonClass) {
        try {
            // Tìm dòng chứa text
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + textToFind + "')]]"));
            
            if (!rows.isEmpty()) {
                System.out.println(">> Đã tìm thấy row chứa '" + textToFind + "'. Click nút class='" + buttonClass + "'");
                // Tìm nút trong dòng
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + buttonClass + "')]"));
                btn.click();
                return true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi tìm nút: " + e.getMessage());
        }
        return false;
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}