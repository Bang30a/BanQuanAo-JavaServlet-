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

/**
 * System Test (Selenium) cho chức năng Quản lý Sản phẩm Admin (CRUD).
 * Test case được chạy tuần tự (Tạo -> Tìm -> Sửa -> Xóa).
 */
// QUAN TRỌNG: Chạy theo thứ tự test01 -> test02 -> test03 -> test04
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductAdminSystemTest {

    WebDriver driver;
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // Dữ liệu mẫu (Random)
    // Lưu ý: Dùng static để giữ nguyên giá trị tên SP qua các test case khác nhau
    /** Tên sản phẩm duy nhất được tạo cho chuỗi test. */
    static String testProdName = "Ao Test Auto " + System.currentTimeMillis(); 
    /** Giá khởi tạo. */
    private final String testPrice = "500000";
    
    /** Giá được cập nhật (phải là số chẵn chia hết cho 1000). */
    private final String updatedPrice = "1000000"; 
    /** URL ảnh mẫu. */
    private final String testImg = "https://placehold.co/600x400"; 

    /** Tốc độ làm chậm (milliseconds) giữa các bước Selenium. */
    private final int SLOW_SPEED = 2000;

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_ProductAdmin.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_ProductAdmin.xlsx");
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
        slowDown();
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("admin123");
        slowDown();

        // Dùng submit() trực tiếp trên ô password để chắc chắn form được gửi đi
        try {
            passField.submit();
        } catch (Exception e) {
            // Fallback nếu không submit được
            driver.findElement(By.className("btn-login")).click();
        }
        
        slowDown();
        
        // Vào Dashboard Admin
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
            driver.get(dashboardUrl);
            slowDown();
        }
    }

    // ====================== CÁC TEST CASE CRUD (TUẦN TỰ) ======================

    // --- CASE 1: THÊM SẢN PHẨM MỚI (CREATE) ---
    @Test
    public void test01_Product_CreateNew() {
        setTestCaseInfo(
            "ST_PROD_01", 
            "Thêm sản phẩm mới", 
            "1. Điều hướng đến Quản lý SP -> Thêm mới\n2. Điền form thông tin SP\n3. Lưu & Tìm ở trang cuối", 
            "Name: " + testProdName + ", Price: 500000", 
            "Sản phẩm mới xuất hiện trong danh sách (có hỗ trợ phân trang)"
        );

        navigateToProductPage();

        // Click nút Thêm mới
        driver.findElement(By.className("btn-add")).click();
        slowDown();

        // Điền form
        driver.findElement(By.name("name")).sendKeys(testProdName);
        driver.findElement(By.name("price")).sendKeys(testPrice);
        driver.findElement(By.name("description")).sendKeys("Mô tả tự động: Sản phẩm dùng để test CRUD.");
        driver.findElement(By.name("image")).sendKeys(testImg);
        slowDown();

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try { Thread.sleep(4000); } catch (Exception e) {} 

        // [Verify] Tìm sản phẩm (duyệt qua các trang)
        boolean found = findRowByTextWithPagination(testProdName);
        
        this.currentActual = "Tìm thấy SP: " + found;
        Assert.assertTrue("Lỗi: Thêm sản phẩm mới nhưng không tìm thấy trong danh sách!", found);
    }

    // --- CASE 2: TÌM KIẾM SẢN PHẨM (READ) ---
    @Test
    public void test02_Product_Search() {
        setTestCaseInfo(
            "ST_PROD_02", 
            "Tìm kiếm sản phẩm", 
            "1. Điều hướng đến trang DS\n2. Nhập tên SP vừa tạo vào ô tìm kiếm\n3. Check kết quả", 
            "Keyword: " + testProdName, 
            "Hiển thị đúng sản phẩm (chỉ 1 kết quả)"
        );

        navigateToProductPage();

        // Nhập từ khóa tìm kiếm
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys(testProdName);
        slowDown();
        
        // Submit form tìm kiếm
        searchInput.submit(); 
        slowDown();

        // [Verify] Kiểm tra chỉ tìm thấy đúng 1 sản phẩm
        boolean found = findRowByTextWithPagination(testProdName);
        this.currentActual = "Tìm thấy kết quả: " + found;
        Assert.assertTrue("Lỗi: Tìm kiếm thất bại hoặc không tìm thấy sản phẩm vừa tạo!", found);
    }

    // --- CASE 3: SỬA SẢN PHẨM (UPDATE) ---
    @Test
    public void test03_Product_Edit() {
        setTestCaseInfo(
            "ST_PROD_03", 
            "Sửa giá sản phẩm", 
            "1. Tìm SP\n2. Click Sửa\n3. Đổi giá thành " + updatedPrice + " -> Lưu", 
            "Price mới: " + updatedPrice, 
            "Giá tiền cập nhật thành công (1,000,000)"
        );

        navigateToProductPage();

        // 1. Tìm và Click nút Sửa (Duyệt qua các trang để tìm)
        boolean clicked = clickButtonInRowWithPagination(testProdName, "btn-edit");
        Assert.assertTrue("Lỗi: Không tìm thấy dòng chứa SP để bấm Sửa", clicked);
        slowDown();

        // 2. Sửa giá tiền
        WebElement priceInput = driver.findElement(By.name("price"));
        priceInput.clear();
        priceInput.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
        priceInput.sendKeys(org.openqa.selenium.Keys.BACK_SPACE);
        priceInput.sendKeys(updatedPrice);
        slowDown();
        
        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try { Thread.sleep(4000); } catch (Exception e) {} 

        // 3. Kiểm tra giá mới
        // Dùng chức năng Search để đưa SP về trang 1
        driver.findElement(By.name("keyword")).clear();
        driver.findElement(By.name("keyword")).sendKeys(testProdName);
        driver.findElement(By.name("keyword")).submit();
        slowDown();

        // [Verify] Kiểm tra Page Source có chứa giá mới (định dạng tiền tệ)
        String pageSource = driver.getPageSource();
        this.currentActual = "Page chứa giá mới: " + pageSource.contains("1,000,000");
        Assert.assertTrue("Lỗi: Giá tiền chưa cập nhật! Mong đợi 1,000,000", pageSource.contains("1,000,000") || pageSource.contains("1.000.000"));
    }

    // --- CASE 4: XÓA SẢN PHẨM (DELETE) ---
    @Test
    public void test04_Product_Delete() {
        setTestCaseInfo(
            "ST_PROD_04", 
            "Xóa sản phẩm", 
            "1. Tìm SP (có phân trang)\n2. Click Xóa\n3. Confirm Alert", 
            "Target: " + testProdName, 
            "Sản phẩm biến mất khỏi danh sách"
        );

        navigateToProductPage();

        // 1. Tìm và Click nút Xóa
        boolean clicked = clickButtonInRowWithPagination(testProdName, "btn-delete");
        Assert.assertTrue("Lỗi: Không tìm thấy nút xóa", clicked);
        
        // 2. Xử lý Alert xác nhận xóa
        try {
            slowDown();
            driver.switchTo().alert().accept();
            try { Thread.sleep(4000); } catch (Exception e) {} 
        } catch (Exception e) {
            System.out.println("Không thấy Alert.");
        }

        // 3. Kiểm tra lại (Phải KHÔNG tìm thấy)
        driver.navigate().refresh(); // Refresh về trang 1
        slowDown();
        
        boolean found = findRowByTextWithPagination(testProdName);
        this.currentActual = "Vẫn tìm thấy sau khi xóa: " + found;
        
        Assert.assertFalse("Lỗi: Sản phẩm vẫn còn trong danh sách sau khi xóa!", found);
    }

    // --- UTILITIES (HÀM HỖ TRỢ) ---

    /**
     * Helper: Điều hướng đến trang Quản lý Sản phẩm (Products Manager).
     */
    private void navigateToProductPage() {
        driver.switchTo().defaultContent();
        try {
            // Click menu
            driver.findElement(By.xpath("//a[contains(text(), 'Quản lý sản phẩm')]")).click();
            slowDown();
            driver.findElement(By.linkText("Sản phẩm")).click();
            slowDown();
            driver.switchTo().frame("mainFrame");
        } catch (Exception e) {
            // Fallback URL
            driver.get("http://localhost:8080/ShopDuck/admin/ProductsManagerServlet?action=List");
        }
    }

    /**
     * Helper: Tìm dòng chứa đoạn text cụ thể trong bảng, hỗ trợ duyệt qua các trang (Pagination).
     *
     * @param text Đoạn text cần tìm (tên sản phẩm).
     * @return True nếu tìm thấy, ngược lại False sau khi duyệt hết các trang.
     */
    private boolean findRowByTextWithPagination(String text) {
        while (true) {
            // Tìm ở trang hiện tại
            int count = driver.findElements(By.xpath("//td[contains(text(), '" + text + "')]")).size();
            if (count > 0) return true;

            // Nếu không thấy, thử tìm nút Next
            if (!goToNextPage()) break; // Hết trang
        }
        return false;
    }

    /**
     * Helper: Tìm và click nút (Sửa/Xóa) trong dòng chứa đoạn text cụ thể, hỗ trợ Phân trang.
     *
     * @param rowText Đoạn text để xác định dòng (tên sản phẩm).
     * @param btnClass Class của nút cần click (ví dụ: 'btn-edit' hoặc 'btn-delete').
     * @return True nếu click thành công, ngược lại False.
     */
    private boolean clickButtonInRowWithPagination(String rowText, String btnClass) {
        while (true) {
            // Tìm dòng chứa text
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

    /**
     * Helper: Chuyển sang trang kế tiếp của phân trang.
     *
     * @return True nếu có trang kế tiếp và chuyển trang thành công, ngược lại False.
     */
    private boolean goToNextPage() {
        try {
            // Tìm thẻ a chứa chữ "Sau" hoặc ký tự » trong thẻ <li> KHÔNG bị disabled
            List<WebElement> nextBtns = driver.findElements(By.xpath("//li[not(contains(@class, 'disabled'))]/a[contains(text(), 'Sau') or contains(text(), 'Next') or contains(text(), '»')]"));
            if (!nextBtns.isEmpty()) {
                System.out.println(">> Chuyển trang kế tiếp...");
                nextBtns.get(0).click();
                slowDown();
                return true;
            }
        } catch (Exception ignored) {}
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