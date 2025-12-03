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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.Alert;

import java.util.List;

public class CartSystemTest {

    WebDriver driver;
    // URL Configuration
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    String cartUrl = "http://localhost:8080/ShopDuck/user/order/view-cart.jsp";

    // [CẤU HÌNH] Tốc độ test chậm (3 giây) để dễ quan sát
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_FullCart.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_FullCart.xlsx");
    }

    // Hàm làm chậm
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // 1. Đăng nhập User
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("user");
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("user123");
        slowDown();

        // [FIX LOGIN] Dùng submit() để tránh click nhầm
        try {
            passField.submit();
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click();
        }
        slowDown();
    }

    // ================================================================
    // KỊCH BẢN: HOME -> DETAIL (Chọn Size) -> ADD -> UPDATE -> REMOVE
    // ================================================================
    @Test
    public void testCart_FullFlow() {
        setTestCaseInfo(
            "ST_CART_FULL", 
            "Quy trình Mua hàng trọn vẹn", 
            "1. Home -> Xem chi tiết\n2. Chọn Size -> Thêm vào giỏ\n3. Vào giỏ -> Update SL lên 2\n4. Xóa SP", 
            "SP ngẫu nhiên", 
            "Thêm thành công, tính tiền đúng, xóa sạch"
        );

        // --- BƯỚC 1: VÀO HOME & CLICK CHI TIẾT ---
        System.out.println("Step 1: Vào trang chủ & Chọn sản phẩm...");
        driver.get(homeUrl);
        slowDown();
        
        try {
            // Click vào sản phẩm đầu tiên (class .product-img-wrap)
            WebElement productLink = driver.findElement(By.cssSelector(".product-card .product-img-wrap"));
            productLink.click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy sản phẩm nào trên trang chủ!");
        }
        slowDown(); 

        // --- BƯỚC 2: TẠI DETAIL -> CHỌN SIZE -> THÊM VÀO GIỎ ---
        System.out.println("Step 2: Chọn Size & Thêm vào giỏ...");
        try {
            // 2.1 Chọn Size (Nếu có dropdown)
            if (driver.findElements(By.id("variantSelect")).size() > 0) {
                Select select = new Select(driver.findElement(By.id("variantSelect")));
                List<WebElement> options = select.getOptions();
                
                // Chọn option thứ 2 (bỏ qua 'Chọn size' mặc định)
                if (options.size() > 1) {
                    select.selectByIndex(1); 
                    System.out.println(">> Đã chọn Size: " + options.get(1).getText());
                } else {
                    Assert.fail("Sản phẩm hết hàng/không có size!");
                }
            }
            slowDown();

            // 2.2 Click nút Thêm (class .btn-add-cart)
            driver.findElement(By.className("btn-add-cart")).click();
            System.out.println(">> Đã click nút Thêm vào giỏ!");
            slowDown();
            
        } catch (Exception e) {
            Assert.fail("Lỗi thao tác tại trang chi tiết: " + e.getMessage());
        }

        // --- BƯỚC 3: VÀO GIỎ & KIỂM TRA ---
        System.out.println("Step 3: Vào giỏ hàng kiểm tra...");
        driver.get(cartUrl);
        slowDown();
        
        boolean hasTable = driver.findElements(By.className("table")).size() > 0;
        Assert.assertTrue("Lỗi: Đã thêm nhưng vào giỏ vẫn rỗng!", hasTable);

        // --- BƯỚC 4: CẬP NHẬT SỐ LƯỢNG (UPDATE) ---
        System.out.println("Step 4: Update số lượng lên 2...");
        
        // Lấy giá gốc để so sánh
        String priceText = driver.findElement(By.xpath("//tbody/tr[1]/td[3]")).getText();
        long price = parseMoney(priceText); 

        // Tìm ô nhập số lượng
        WebElement qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        
        // [FIX NHẬP LIỆU] Dùng Ctrl+A -> Nhập 2 -> Enter
        qtyInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), "2", Keys.ENTER);
        slowDown(); // Chờ reload lại trang giỏ hàng

        // Kiểm tra tổng tiền
        WebElement totalElement = driver.findElement(By.className("total-amount"));
        long totalActual = parseMoney(totalElement.getText());
        long totalExpected = price * 2;
        
        System.out.println("Check tiền: Mong đợi=" + totalExpected + ", Thực tế=" + totalActual);
        Assert.assertEquals("Lỗi: Tổng tiền tính sai sau khi update!", totalExpected, totalActual);

        // --- BƯỚC 5: XÓA SẢN PHẨM (REMOVE) ---
        System.out.println("Step 5: Xóa sản phẩm...");
        driver.findElement(By.className("btn-remove")).click();
        slowDown();

        // Xử lý Alert Confirm
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
            slowDown(); // Chờ trang load lại
        } catch (Exception e) {
            System.out.println("Không thấy Alert.");
        }

        // --- BƯỚC 6: KIỂM TRA GIỎ RỖNG ---
        boolean isEmptyState = driver.findElements(By.className("empty-state")).size() > 0;
        this.currentActual = "Xong luồng. Trạng thái Empty: " + isEmptyState;
        Assert.assertTrue("Lỗi: Xóa xong nhưng không hiện màn hình Giỏ hàng trống!", isEmptyState);
    }

    // Hàm parse tiền tệ (100,000 đ -> 100000)
    private long parseMoney(String moneyText) {
        try {
            return Long.parseLong(moneyText.replace("đ", "").replace(",", "").replace(".", "").trim());
        } catch (Exception e) { return 0; }
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}