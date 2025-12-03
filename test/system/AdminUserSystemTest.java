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

// Chạy tuần tự: Tạo -> Sửa -> Xóa
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminUserSystemTest {

    WebDriver driver;

    // URL Config
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- TEST DATA (RANDOM) ---
    static int uniqueSeed = new Random().nextInt(9000) + 1000;

    // Dữ liệu tạo mới
    String newUsername = "user_test_" + uniqueSeed;
    String newFullname = "Nguyen Van Test " + uniqueSeed;
    String newEmail = "test" + uniqueSeed + "@gmail.com";
    String password = "123";
    
    // Dữ liệu cập nhật
    String updatedFullname = "Updated Name " + uniqueSeed;

    final int SLOW_SPEED = 1500;

    // --- REPORT VARIABLES ---
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Users.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Users.xlsx");
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
            driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button")).click();
        } catch (Exception e) {
            driver.findElement(By.className("btn-login")).click();
        }
        slowDown();

        // 2. Vào Dashboard
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
             driver.get(dashboardUrl);
             slowDown();
        }
    }

    // --- TEST 01: CREATE USER ---
    @Test
    public void test01_CreateUser() {
        setTestCaseInfo(
            "ST_USER_01",
            "Tạo người dùng mới",
            "1. Vào Quản lý người dùng\n2. Bấm Thêm\n3. Điền Username, Fullname, Email, Pass\n4. Lưu",
            "User: " + newUsername,
            "User mới xuất hiện trong danh sách"
        );

        navigateToUserPage();

        // Click nút Thêm (class btn-primary hoặc tìm theo text)
        try {
            driver.findElement(By.xpath("//a[contains(text(), 'Thêm người dùng')]")).click();
        } catch (Exception e) {
            driver.findElement(By.className("btn-primary")).click();
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

        // Submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        // Verify
        boolean found = findRowByText(newUsername);
        this.currentActual = "Tìm thấy username '" + newUsername + "': " + found;
        Assert.assertTrue("Lỗi: Tạo xong nhưng không tìm thấy user!", found);
    }

    // --- TEST 02: UPDATE USER ---
    @Test
    public void test02_UpdateUser() {
        setTestCaseInfo(
            "ST_USER_02",
            "Sửa thông tin User",
            "1. Tìm user '" + newUsername + "'\n2. Bấm Sửa\n3. Đổi tên thành '" + updatedFullname + "'\n4. Lưu",
            "New Fullname: " + updatedFullname,
            "Tên hiển thị được cập nhật"
        );

        navigateToUserPage();

        // Tìm và bấm Sửa
        boolean clickEdit = findAndClickButton(newUsername, "btn-edit");
        Assert.assertTrue("Không tìm thấy nút Sửa cho user: " + newUsername, clickEdit);
        slowDown();

        // Logic Sửa: Username bị readonly (theo JSP), nên chỉ sửa Fullname
        fillInput("fullname", updatedFullname);
        
        // Thử đổi Role sang Admin luôn
        new Select(driver.findElement(By.name("role"))).selectByValue("admin");
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        // Verify: Tìm theo tên mới (Fullname) hoặc Username vẫn phải còn đó
        boolean foundUsername = findRowByText(newUsername);
        boolean foundNewName = findRowByText(updatedFullname);
        
        this.currentActual = "Username còn: " + foundUsername + " | Tên mới hiện: " + foundNewName;
        Assert.assertTrue("Lỗi: Username bị mất sau khi sửa!", foundUsername);
        Assert.assertTrue("Lỗi: Fullname mới không được cập nhật!", foundNewName);
    }

    // --- TEST 03: DELETE USER ---
    @Test
    public void test03_DeleteUser() {
        setTestCaseInfo(
            "ST_USER_03",
            "Xóa User",
            "1. Tìm user '" + newUsername + "'\n2. Bấm Xóa\n3. Confirm Alert\n4. Kiểm tra",
            "Target: " + newUsername,
            "User bị xóa khỏi danh sách"
        );

        navigateToUserPage();

        // Tìm và bấm Xóa
        boolean clickDelete = findAndClickButton(newUsername, "btn-delete");
        Assert.assertTrue("Không tìm thấy nút Xóa cho user: " + newUsername, clickDelete);

        // Handle Alert
        try {
            slowDown();
            driver.switchTo().alert().accept();
            slowDown();
        } catch (Exception e) {}

        // Verify: Không tìm thấy username nữa
        boolean found = findRowByText(newUsername);
        this.currentActual = "Vẫn tìm thấy user '" + newUsername + "': " + found;
        Assert.assertFalse("Lỗi: Xóa xong nhưng dữ liệu vẫn còn!", found);
    }

    // --- UTILITIES ---

    private void navigateToUserPage() {
        driver.switchTo().defaultContent();
        try {
            // Click menu "Quản lý người dùng" từ Dashboard sidebar
            WebElement menu = driver.findElement(By.xpath("//a[contains(text(), 'Quản lý người dùng')]"));
            menu.click();
            slowDown();
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            // Fallback URL
            driver.get("http://localhost:8080/ShopDuck/admin/UsersManagerServlet?action=List");
        }
    }

    private void fillInput(String name, String value) {
        try {
            WebElement input = driver.findElement(By.name(name));
            // Chỉ clear và gõ nếu không phải readonly
            if (input.getAttribute("readonly") == null) {
                input.click();
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.BACK_SPACE);
                input.sendKeys(value);
                slowDown();
            }
        } catch (Exception e) {
            System.out.println("Không nhập được field: " + name);
        }
    }

    private boolean findRowByText(String text) {
        try {
            int count = driver.findElements(By.xpath("//tr[td[contains(., '" + text + "')]]")).size();
            return count > 0;
        } catch (Exception e) { return false; }
    }

    private boolean findAndClickButton(String rowText, String btnClass) {
        try {
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + rowText + "')]]"));
            if (!rows.isEmpty()) {
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + btnClass + "')]"));
                btn.click();
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps;
        this.currentData = data; this.currentExpected = expected; this.currentActual = "Chưa hoàn thành";
    }

    @After
    public void tearDown() { if (driver != null) driver.quit(); }
}