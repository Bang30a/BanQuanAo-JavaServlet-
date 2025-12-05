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
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Random;

/**
 * System Test (Selenium) cho chức năng Quản lý Người dùng Admin (CRUD).
 * Đảm bảo các nghiệp vụ Tạo, Sửa, Xóa người dùng hoạt động chính xác.
 */
// Chạy tuần tự: Tạo -> Sửa -> Xóa (Quan trọng cho chuỗi CRUD)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminUserSystemTest {

    WebDriver driver;

    // URL Config
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- TEST DATA (DÙNG DỮ LIỆU NGẪU NHIÊN ĐỂ ĐẢM BẢO TÍNH ĐỘC LẬP) ---
    /** Giá trị ngẫu nhiên duy nhất cho mỗi lần chạy. */
    static int uniqueSeed = new Random().nextInt(9000) + 1000;

    // Dữ liệu tạo mới
    private final String newUsername = "user_test_" + uniqueSeed;
    private final String newFullname = "Nguyen Van Test " + uniqueSeed;
    private final String newEmail = "test" + uniqueSeed + "@gmail.com";
    private final String password = "123";
    
    // Dữ liệu cập nhật
    private final String updatedFullname = "Updated Name " + uniqueSeed;

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Users.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Users.xlsx");
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
        try {
            // Cố gắng click nút submit bằng xpath/button
            driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button")).click();
        } catch (Exception e) {
            // Fallback: click bằng class
            driver.findElement(By.className("btn-login")).click();
        }
        slowDown();

        // 2. Vào Dashboard (Đảm bảo đang ở trang Admin)
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
             driver.get(dashboardUrl);
             slowDown();
        }
    }

    // --- TEST 01: CREATE USER (Chạy đầu tiên) ---
    @Test
    public void test01_CreateUser() {
        setTestCaseInfo(
            "ST_USER_01",
            "Tạo người dùng mới",
            "1. Vào Quản lý người dùng\n2. Bấm Thêm\n3. Điền form (Username, Fullname, Email, Pass, Role)\n4. Lưu",
            "User: " + newUsername,
            "User mới xuất hiện trong danh sách"
        );

        navigateToUserPage();

        // Click nút Thêm người dùng
        try {
            driver.findElement(By.xpath("//a[contains(text(), 'Thêm người dùng')]")).click();
        } catch (Exception e) {
            driver.findElement(By.className("btn-primary")).click(); // Fallback nếu không tìm thấy text
        }
        slowDown();

        // Điền form
        fillInput("username", newUsername);
        fillInput("fullname", newFullname);
        fillInput("email", newEmail);
        fillInput("password", password);
        
        // Chọn Role = User
        new Select(driver.findElement(By.name("role"))).selectByValue("user");
        slowDown();

        // Submit form tạo mới
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        // Verify: Tìm username vừa tạo trong bảng danh sách
        boolean found = findRowByText(newUsername);
        this.currentActual = "Tìm thấy username '" + newUsername + "': " + found;
        Assert.assertTrue("Lỗi: Tạo user mới nhưng không tìm thấy trong danh sách!", found);
    }

    // --- TEST 02: UPDATE USER (Chạy thứ hai) ---
    @Test
    public void test02_UpdateUser() {
        setTestCaseInfo(
            "ST_USER_02",
            "Sửa thông tin User",
            "1. Tìm user '" + newUsername + "'\n2. Bấm Sửa\n3. Đổi tên thành '" + updatedFullname + "' & Role thành 'admin'\n4. Lưu",
            "New Fullname: " + updatedFullname + ", New Role: admin",
            "Tên hiển thị và Role được cập nhật thành công"
        );

        navigateToUserPage();

        // Tìm row chứa username vừa tạo và bấm nút Sửa
        boolean clickEdit = findAndClickButton(newUsername, "btn-edit");
        Assert.assertTrue("Lỗi: Không tìm thấy nút Sửa cho user: " + newUsername, clickEdit);
        slowDown();

        // Logic Sửa: Sửa Fullname
        fillInput("fullname", updatedFullname);
        
        // Sửa Role thành Admin
        new Select(driver.findElement(By.name("role"))).selectByValue("admin");
        slowDown();

        // Submit form cập nhật
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        // Verify: Kiểm tra Fullname mới và Username phải còn tồn tại
        boolean foundUsername = findRowByText(newUsername);
        boolean foundNewName = findRowByText(updatedFullname);
        
        this.currentActual = "Username còn: " + foundUsername + " | Tên mới hiện: " + foundNewName;
        Assert.assertTrue("Lỗi: Username bị mất sau khi sửa!", foundUsername);
        Assert.assertTrue("Lỗi: Fullname mới không được cập nhật!", foundNewName);
    }

    // --- TEST 03: DELETE USER (Chạy cuối cùng) ---
    @Test
    public void test03_DeleteUser() {
        setTestCaseInfo(
            "ST_USER_03",
            "Xóa User",
            "1. Tìm user '" + newUsername + "'\n2. Bấm Xóa\n3. Xác nhận Alert (Confirm)\n4. Kiểm tra",
            "Target: " + newUsername,
            "User không còn trong danh sách"
        );

        navigateToUserPage();

        // Tìm row chứa username và bấm nút Xóa
        boolean clickDelete = findAndClickButton(newUsername, "btn-delete");
        Assert.assertTrue("Lỗi: Không tìm thấy nút Xóa cho user: " + newUsername, clickDelete);

        // Handle Alert (Xác nhận hành động xóa)
        try {
            slowDown();
            driver.switchTo().alert().accept(); // Bấm OK trên hộp thoại xác nhận
            slowDown();
        } catch (Exception ignored) {
            // Bỏ qua nếu không có alert hiện ra
        }

        // Verify: Kiểm tra xem username đã bị xóa chưa
        boolean found = findRowByText(newUsername);
        this.currentActual = "Vẫn tìm thấy user '" + newUsername + "': " + found;
        Assert.assertFalse("Lỗi: Xóa xong nhưng username vẫn còn trong danh sách!", found);
    }

    // --- UTILITIES (HÀM HỖ TRỢ) ---

    /**
     * Helper: Điều hướng đến trang quản lý người dùng (Users Manager).
     * Bao gồm cả việc chuyển đổi sang iframe (nếu cần).
     */
    private void navigateToUserPage() {
        driver.switchTo().defaultContent(); // Thoát iframe trước
        try {
            // Click menu "Quản lý người dùng" từ Dashboard sidebar
            WebElement menu = driver.findElement(By.xpath("//a[contains(text(), 'Quản lý người dùng')]"));
            menu.click();
            slowDown();
            driver.switchTo().frame("mainFrame"); // Chuyển vào iframe
        } catch (Exception e) {
            // Fallback URL nếu click menu lỗi
            driver.get("http://localhost:8080/ShopDuck/admin/UsersManagerServlet?action=List");
        }
    }

    /**
     * Helper: Điền giá trị vào trường input dựa trên thuộc tính 'name'.
     *
     * @param name Tên thuộc tính 'name' của input.
     * @param value Giá trị cần nhập.
     */
    private void fillInput(String name, String value) {
        try {
            WebElement input = driver.findElement(By.name(name));
            // Chỉ clear và gõ nếu input không phải là readonly
            if (input.getAttribute("readonly") == null) {
                input.click();
                // Chọn toàn bộ nội dung hiện có và xóa (Ctrl+A, Backspace)
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.BACK_SPACE);
                input.sendKeys(value);
                slowDown();
            }
        } catch (Exception e) {
            System.out.println("Không nhập được field: " + name);
        }
    }

    /**
     * Helper: Kiểm tra xem có dòng nào trong bảng chứa đoạn text cụ thể hay không.
     *
     * @param text Đoạn text cần tìm (Username, Fullname...).
     * @return True nếu tìm thấy, ngược lại False.
     */
    private boolean findRowByText(String text) {
        try {
            // Dùng XPath: tìm kiếm các thẻ <tr> có chứa thẻ <td> mà nội dung của nó chứa 'text'
            int count = driver.findElements(By.xpath("//tr[td[contains(., '" + text + "')]]")).size();
            return count > 0;
        } catch (Exception e) { return false; }
    }

    /**
     * Helper: Tìm dòng chứa đoạn text cụ thể và bấm nút có class tương ứng (Sửa/Xóa).
     *
     * @param rowText Đoạn text để xác định dòng (ví dụ: Username).
     * @param btnClass Class của nút (ví dụ: 'btn-edit' hoặc 'btn-delete').
     * @return True nếu nút được tìm thấy và click thành công, ngược lại False.
     */
    private boolean findAndClickButton(String rowText, String btnClass) {
        try {
            // Tìm các dòng chứa đoạn text xác định
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + rowText + "')]]"));
            if (!rows.isEmpty()) {
                // Trong dòng đầu tiên tìm thấy, tìm nút có class tương ứng
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + btnClass + "')]"));
                btn.click();
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
    
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
        this.currentId = id; this.currentName = name; this.currentSteps = steps;
        this.currentData = data; this.currentExpected = expected; this.currentActual = "Chưa hoàn thành";
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() { if (driver != null) driver.quit(); }
}