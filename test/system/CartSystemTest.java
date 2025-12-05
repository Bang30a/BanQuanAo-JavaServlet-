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

/**
 * System Test (Selenium) cho chức năng Giỏ hàng (Shopping Cart) của người dùng.
 * Kiểm thử quy trình thêm, cập nhật, xóa sản phẩm và các ràng buộc về số lượng.
 */
public class CartSystemTest {

    WebDriver driver;
    private final String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    private final String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";
    private final String cartUrl = "http://localhost:8080/ShopDuck/user/order/view-cart.jsp";

    /** Tốc độ làm chậm (milliseconds) giữa các bước Selenium để dễ dàng quan sát. */
    private final int SLOW_SPEED = 3000;

    // --- BIẾN BÁO CÁO EXCEL ---
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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_FullCart.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_FullCart.xlsx");
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver và Đăng nhập User.
     */
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // 1. Điều hướng đến trang Login
        driver.get(loginUrl);
        slowDown();
        
        // 2. Đăng nhập với tài khoản User
        driver.findElement(By.name("username")).sendKeys("user");
        slowDown();
        
        WebElement passField = driver.findElement(By.name("password"));
        passField.sendKeys("user123");
        slowDown();

        try {
            passField.submit(); // Cố gắng submit qua trường password
        } catch (Exception e) {
            driver.findElement(By.cssSelector(".btn-login")).click(); // Fallback click nút login
        }
        slowDown();
    }

    // ================================================================
    // CÁC TEST CASE CHỨC NĂNG GIỎ HÀNG
    // ================================================================

    // --- CASE 1: FULL FLOW (Thêm -> Cập nhật SL -> Tính tiền -> Xóa) ---
    @Test
    public void testCart_FullFlow() {
        setTestCaseInfo(
            "ST_CART_FULL", 
            "Quy trình Mua hàng trọn vẹn (CRUD giỏ hàng)", 
            "1. Home -> Xem chi tiết SP\n2. Chọn Size -> Thêm vào giỏ\n3. Vào giỏ -> Update SL lên 2\n4. Kiểm tra tổng tiền\n5. Xóa SP", 
            "SP ngẫu nhiên", 
            "Thêm thành công, tính tổng tiền đúng (Price * 2), giỏ hàng trống sau khi xóa"
        );

        System.out.println("Step 1: Vào trang chủ & Chọn sản phẩm đầu tiên...");
        driver.get(homeUrl);
        slowDown();
        
        // Click vào sản phẩm đầu tiên
        try {
            WebElement productLink = driver.findElement(By.cssSelector(".product-card .product-img-wrap"));
            productLink.click();
        } catch (Exception e) {
            Assert.fail("Không tìm thấy sản phẩm nào trên trang chủ!");
        }
        slowDown(); 

        System.out.println("Step 2: Chọn Size & Thêm vào giỏ...");
        try {
            // Xử lý chọn biến thể (Size/Variant)
            if (driver.findElements(By.id("variantSelect")).size() > 0) {
                Select select = new Select(driver.findElement(By.id("variantSelect")));
                List<WebElement> options = select.getOptions();
                if (options.size() > 1) {
                    select.selectByIndex(1); // Chọn size thứ hai (size đầu thường là placeholder)
                    System.out.println(">> Đã chọn Size: " + options.get(1).getText());
                } else {
                    Assert.fail("Sản phẩm hết hàng/không có size để chọn!");
                }
            }
            slowDown();

            // Click nút Thêm vào giỏ
            driver.findElement(By.className("btn-add-cart")).click();
            System.out.println(">> Đã click nút Thêm vào giỏ!");
            slowDown();
            
        } catch (Exception e) {
            Assert.fail("Lỗi thao tác tại trang chi tiết: " + e.getMessage());
        }

        System.out.println("Step 3: Vào giỏ hàng kiểm tra và Update số lượng lên 2...");
        driver.get(cartUrl);
        slowDown();
        
        boolean hasTable = driver.findElements(By.className("table")).size() > 0;
        Assert.assertTrue("Lỗi: Đã thêm nhưng giỏ hàng vẫn trống!", hasTable);

        // Lấy giá đơn vị
        String priceText = driver.findElement(By.xpath("//tbody/tr[1]/td[3]")).getText();
        long price = parseMoney(priceText); 

        // Nhập số lượng là 2 và nhấn ENTER (giả định tự động cập nhật)
        WebElement qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        qtyInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), "2", Keys.ENTER);
        slowDown(); 

        // 4. Kiểm tra tổng tiền
        WebElement totalElement = driver.findElement(By.className("total-amount"));
        long totalActual = parseMoney(totalElement.getText());
        long totalExpected = price * 2;
        
        System.out.println("Check tiền: Mong đợi=" + totalExpected + ", Thực tế=" + totalActual);
        Assert.assertEquals("Lỗi: Tổng tiền tính sai sau khi update số lượng!", totalExpected, totalActual);

        System.out.println("Step 5: Xóa sản phẩm...");
        driver.findElement(By.className("btn-remove")).click();
        slowDown();

        // Xử lý Alert xác nhận xóa (nếu có)
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
            slowDown();
        } catch (Exception e) {
            System.out.println("Không thấy Alert xác nhận xóa.");
        }

        // 5. Kiểm tra giỏ hàng trống
        boolean isEmptyState = driver.findElements(By.className("empty-state")).size() > 0;
        this.currentActual = "Xong luồng. Trạng thái Giỏ hàng trống: " + isEmptyState;
        Assert.assertTrue("Lỗi: Xóa xong nhưng không hiện màn hình Giỏ hàng trống!", isEmptyState);
    }

    // --- CASE 2: CẬP NHẬT SỐ LƯỢNG KHÔNG HỢP LỆ (0) ---
    @Test
    public void testCart_Update_InvalidQuantity() {
        setTestCaseInfo(
            "ST_CART_INV_QTY", 
            "Cập nhật số lượng về 0 (Kiểm tra ràng buộc tối thiểu)", 
            "1. Đảm bảo giỏ có hàng\n2. Nhập số lượng 0\n3. Check Alert JS và giá trị input", 
            "Quantity: 0", 
            "Hiện Browser Alert và số lượng trong input tự động reset về 1"
        );

        ensureCartHasItem(); // Setup: Đảm bảo có ít nhất 1 sản phẩm
        driver.get(cartUrl);
        slowDown();

        WebElement qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        // Nhập số 0
        qtyInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), "0", Keys.ENTER);
        slowDown(); 

        // [MỚI] Kiểm tra Alert từ Javascript (Ràng buộc min=1)
        boolean hasAlert = false;
        try {
            Alert alert = driver.switchTo().alert();
            String text = alert.getText();
            System.out.println(">> Alert JS: " + text);
            alert.accept(); // Đóng alert
            hasAlert = true;
            
            this.currentActual = "Có Alert: " + text;
            Assert.assertTrue("Alert không đúng nội dung (mong đợi cảnh báo tối thiểu là 1)!", text.contains("tối thiểu là 1"));
        } catch (Exception e) {
            System.out.println("Không thấy Alert JS.");
        }
        
        Assert.assertTrue("Lỗi: Nhập 0 nhưng không hiện cảnh báo!", hasAlert);

        // Kiểm tra giá trị input phải reset về 1
        qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        String val = qtyInput.getAttribute("value");
        this.currentActual += " | Value reset về: " + val;
        Assert.assertEquals("Lỗi: Sau khi cảnh báo, số lượng phải về 1!", "1", val);
    }

    // --- CASE 3: CẬP NHẬT SỐ LƯỢNG LỚN (QUÁ TỒN KHO) ---
    @Test
    public void testCart_Update_LargeQuantity() {
        setTestCaseInfo(
            "ST_CART_LARGE_QTY", 
            "Cập nhật số lượng lớn (Kiểm tra ràng buộc tối đa/Tồn kho)", 
            "1. Đảm bảo giỏ có hàng\n2. Nhập số lượng lớn (9999)\n3. Check thông báo HTML và giá trị input", 
            "Quantity: 9999", 
            "Input reset về Max Stock & Hiện thông báo HTML về tồn kho"
        );

        ensureCartHasItem(); // Setup: Đảm bảo có ít nhất 1 sản phẩm
        driver.get(cartUrl);
        slowDown();

        WebElement qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        
        // Nhập số cực lớn
        qtyInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), "9999", Keys.ENTER);
        slowDown(); // Chờ reload/cập nhật

        // 1. Kiểm tra giá trị input phải KHÔNG LÀ 9999 (đã bị giới hạn về max stock)
        qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
        String val = qtyInput.getAttribute("value");
        
        this.currentActual = "Giá trị sau khi nhập: " + val;
        Assert.assertNotEquals("Lỗi: Vẫn cho phép nhập 9999! (Chưa bị giới hạn Max Stock)", "9999", val);

        // 2. Kiểm tra thông báo lỗi HTML (.alert-warning) hiển thị
        boolean hasHtmlAlert = false;
        try {
            WebElement alertDiv = driver.findElement(By.className("alert-warning"));
            String alertText = alertDiv.getText();
            
            if (alertDiv.isDisplayed() && alertText.contains("còn")) {
                hasHtmlAlert = true;
                this.currentActual += " | Alert: " + alertText;
            }
        } catch (Exception e) {}

        Assert.assertTrue("Lỗi: Không hiện thông báo HTML cảnh báo tồn kho (alert-warning)!", hasHtmlAlert);
    }

    // --- HÀM PHỤ TRỢ ---
    /**
     * Helper: Đảm bảo giỏ hàng có ít nhất một sản phẩm để các Test Case khác chạy.
     * Nếu giỏ rỗng, sẽ tự động điều hướng và thêm sản phẩm đầu tiên có variant.
     */
    private void ensureCartHasItem() {
        driver.get(cartUrl);
        // Kiểm tra giỏ hàng có rỗng không
        if (driver.findElements(By.className("table")).isEmpty()) {
            System.out.println(">> Setup: Giỏ rỗng, đang thêm hàng...");
            // Đi tới trang chủ
            driver.get(homeUrl);
            slowDown();
            try {
                // Click SP đầu tiên
                driver.findElement(By.cssSelector(".product-card .product-img-wrap")).click();
                slowDown();
                // Chọn variant (nếu có)
                if (driver.findElements(By.id("variantSelect")).size() > 0) {
                    new Select(driver.findElement(By.id("variantSelect"))).selectByIndex(1);
                }
                // Thêm vào giỏ
                driver.findElement(By.className("btn-add-cart")).click();
                slowDown();
            } catch (Exception e) {} // Bỏ qua lỗi setup nếu không thể thêm hàng
        }
    }

    /**
     * Helper: Chuyển chuỗi tiền tệ (ví dụ: "100.000đ") thành giá trị số nguyên Long.
     * @param moneyText Chuỗi tiền tệ.
     * @return Giá trị số Long.
     */
    private long parseMoney(String moneyText) {
        try {
            // Loại bỏ các ký tự không phải số (đ, ,, .)
            return Long.parseLong(moneyText.replace("đ", "").replace(",", "").replace(".", "").trim());
        } catch (Exception e) { return 0; }
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}