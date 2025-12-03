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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

// QUAN TRỌNG: Chạy theo thứ tự test01 -> test02 -> test03 -> test04
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductAdminSystemTest {

    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // Dữ liệu mẫu (Random)
    // Lưu ý: Dùng static để giữ nguyên giá trị tên SP qua các test case khác nhau
    static String testProdName = "Ao Test Auto " + System.currentTimeMillis(); 
    String testPrice = "500000";
    
    // [ĐÃ SỬA] Đổi giá thành số chẵn chia hết cho 1000 để tránh lỗi validation "step mismatch"
    String updatedPrice = "1000000"; 
    String testImg = "https://placehold.co/600x400"; 

    final int SLOW_SPEED = 2000;

    // --- REPORT VARIABLES ---
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_ProductAdmin.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_ProductAdmin.xlsx");
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
        slowDown();
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("admin123");
        slowDown();

        // [FIX LOGIN] Dùng submit() trực tiếp trên ô password để chắc chắn form được gửi đi
        // Cách này an toàn hơn việc tìm nút button vì không sợ click nhầm nút Search
        try {
            passField.submit();
        } catch (Exception e) {
            // Fallback nếu không submit được
            driver.findElement(By.className("btn-login")).click();
        }
        
        slowDown();
        
        // Vào Dashboard
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
            driver.get(dashboardUrl);
            slowDown();
        }
    }

    // --- CASE 1: THÊM SẢN PHẨM MỚI ---
    @Test
    public void test01_Product_CreateNew() {
        setTestCaseInfo(
            "ST_PROD_01", 
            "Thêm sản phẩm mới", 
            "1. Menu SP -> Thêm mới\n2. Điền form\n3. Lưu & Tìm ở trang cuối", 
            "Name: " + testProdName, 
            "Sản phẩm mới xuất hiện trong danh sách"
        );

        navigateToProductPage();

        driver.findElement(By.className("btn-add")).click();
        slowDown();

        driver.findElement(By.name("name")).sendKeys(testProdName);
        driver.findElement(By.name("price")).sendKeys(testPrice);
        driver.findElement(By.name("description")).sendKeys("Mô tả tự động");
        driver.findElement(By.name("image")).sendKeys(testImg);
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try { Thread.sleep(4000); } catch (Exception e) {} 

        // [QUAN TRỌNG] Tìm sản phẩm (có hỗ trợ phân trang)
        boolean found = findRowByTextWithPagination(testProdName);
        
        this.currentActual = "Tìm thấy SP: " + found;
        Assert.assertTrue("Lỗi: Thêm xong nhưng không thấy SP (đã duyệt hết các trang)!", found);
    }

    // --- CASE 2: TÌM KIẾM SẢN PHẨM ---
    @Test
    public void test02_Product_Search() {
        setTestCaseInfo(
            "ST_PROD_02", 
            "Tìm kiếm sản phẩm", 
            "1. Vào trang DS\n2. Nhập tên SP\n3. Check kết quả", 
            "Keyword: " + testProdName, 
            "Hiển thị đúng sản phẩm"
        );

        navigateToProductPage();

        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys(testProdName);
        slowDown();
        
        // Submit form tìm kiếm
        searchInput.submit(); 
        slowDown();

        // Kiểm tra
        boolean found = findRowByTextWithPagination(testProdName);
        this.currentActual = "Tìm thấy kết quả: " + found;
        Assert.assertTrue("Tìm kiếm thất bại!", found);
    }

    // --- CASE 3: SỬA SẢN PHẨM ---
    @Test
    public void test03_Product_Edit() {
        setTestCaseInfo(
            "ST_PROD_03", 
            "Sửa giá sản phẩm", 
            "1. Tìm SP (có phân trang)\n2. Click Sửa\n3. Đổi giá -> Lưu", 
            "Price mới: " + updatedPrice, 
            "Giá tiền cập nhật thành công"
        );

        navigateToProductPage();

        // 1. Tìm và Click nút Sửa (Duyệt qua các trang để tìm)
        boolean clicked = clickButtonInRowWithPagination(testProdName, "btn-edit");
        Assert.assertTrue("Không tìm thấy dòng chứa SP để bấm Sửa", clicked);
        slowDown();

        // 2. Sửa giá tiền
        WebElement priceInput = driver.findElement(By.name("price"));
        priceInput.clear();
        // Dùng Ctrl+A -> Backspace cho chắc chắn sạch
        priceInput.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
        priceInput.sendKeys(org.openqa.selenium.Keys.BACK_SPACE);
        priceInput.sendKeys(updatedPrice);
        slowDown();
        
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try { Thread.sleep(4000); } catch (Exception e) {} 

        // 4. Kiểm tra giá mới (Cần tìm lại SP đó)
        // Để nhanh, ta dùng chức năng Search của web
        driver.findElement(By.name("keyword")).clear();
        driver.findElement(By.name("keyword")).sendKeys(testProdName);
        driver.findElement(By.name("keyword")).submit();
        slowDown();

        String pageSource = driver.getPageSource();
        // [ĐÃ SỬA] Kiểm tra giá 1,000,000 (định dạng có dấu phẩy hoặc chấm)
        this.currentActual = "Page chứa giá mới: " + pageSource.contains("1,000,000");
        Assert.assertTrue("Giá tiền chưa cập nhật! Mong đợi 1,000,000", pageSource.contains("1,000,000") || pageSource.contains("1.000.000"));
    }

    // --- CASE 4: XÓA SẢN PHẨM ---
    @Test
    public void test04_Product_Delete() {
        setTestCaseInfo(
            "ST_PROD_04", 
            "Xóa sản phẩm", 
            "1. Tìm SP (có phân trang)\n2. Click Xóa\n3. Confirm Alert", 
            "Target: " + testProdName, 
            "Sản phẩm biến mất"
        );

        navigateToProductPage();

        // 1. Tìm và Click nút Xóa
        boolean clicked = clickButtonInRowWithPagination(testProdName, "btn-delete");
        Assert.assertTrue("Không tìm thấy nút xóa", clicked);
        
        // 2. Xử lý Alert
        try {
            slowDown();
            driver.switchTo().alert().accept();
            try { Thread.sleep(4000); } catch (Exception e) {} 
        } catch (Exception e) {
            System.out.println("Không thấy Alert.");
        }

        // 3. Kiểm tra lại (Phải KHÔNG tìm thấy)
        // Reset về trang đầu
        driver.navigate().refresh();
        slowDown();
        
        boolean found = findRowByTextWithPagination(testProdName);
        this.currentActual = "Vẫn tìm thấy sau khi xóa: " + found;
        
        Assert.assertFalse("Lỗi: Sản phẩm vẫn còn trong danh sách!", found);
    }

    // --- UTILITIES ---

    private void navigateToProductPage() {
        driver.switchTo().defaultContent();
        try {
            driver.findElement(By.xpath("//a[contains(text(), 'Quản lý sản phẩm')]")).click();
            slowDown();
            driver.findElement(By.linkText("Sản phẩm")).click();
            slowDown();
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            driver.get("http://localhost:8080/ShopDuck/admin/ProductsManagerServlet?action=List");
        }
    }

    // [QUAN TRỌNG] Hàm tìm kiếm hỗ trợ phân trang (Pagination)
    private boolean findRowByTextWithPagination(String text) {
        while (true) {
            // Tìm ở trang hiện tại
            int count = driver.findElements(By.xpath("//td[contains(text(), '" + text + "')]")).size();
            if (count > 0) return true;

            // Nếu không thấy, thử tìm nút Next ("Sau »" hoặc class page-link next)
            if (!goToNextPage()) break; // Hết trang
        }
        return false;
    }

    // [QUAN TRỌNG] Hàm click nút trong dòng (hỗ trợ phân trang)
    private boolean clickButtonInRowWithPagination(String rowText, String btnClass) {
        while (true) {
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(text(), '" + rowText + "')]]"));
            if (!rows.isEmpty()) {
                // Tìm thấy dòng -> Click nút
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + btnClass + "')]"));
                btn.click();
                return true;
            }
            
            // Không thấy -> Sang trang sau
            if (!goToNextPage()) return false;
        }
    }

    // Hàm chuyển trang Next
    private boolean goToNextPage() {
        try {
            // Tìm thẻ a chứa chữ "Sau" nằm trong li KHÔNG có class disabled
            List<WebElement> nextBtns = driver.findElements(By.xpath("//li[not(contains(@class, 'disabled'))]/a[contains(text(), 'Sau') or contains(text(), 'Next') or contains(text(), '»')]"));
            if (!nextBtns.isEmpty()) {
                System.out.println(">> Chuyển trang kế tiếp...");
                nextBtns.get(0).click();
                slowDown();
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}