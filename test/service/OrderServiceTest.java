// Đặt trong "Test Packages/service/"
package service;

// === IMPORT CƠ BẢN ===
import dao.*;
import entity.*;
import context.DBContext;
import java.sql.Connection; // QUAN TRỌNG
import java.sql.SQLException; // QUAN TRỌNG
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT MOCKITO ===
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

// === IMPORT ĐỂ TẠO BÁO CÁO ===
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp Unit Test cho OrderService.
 * Nó tập trung vào logic GIAO DỊCH (Transaction) của phương thức placeOrder.
 */
public class OrderServiceTest {

    // 1. Tạo MOCK cho TẤT CẢ các phụ thuộc
    @Mock
    private OrderDao orderDaoMock;
    @Mock
    private OrderDetailDao detailDaoMock;
    @Mock
    private ProductDao productDaoMock; 
    @Mock
    private ProductVariantDao variantDaoMock; 
    @Mock
    private SizeDao sizeDaoMock; 
    @Mock
    private DBContext dbContextMock; 
    
    // 2. Tạo MOCK cho Connection (để kiểm soát transaction)
    @Mock
    private Connection connectionMock;

    // 3. Đối tượng Service sẽ được test
    private OrderService orderService;

    // 4. Dữ liệu "giả" để test
    private Users fakeUser;
    private List<CartBean> fakeCart;
    private ProductVariants fakeVariant1;
    
    @Before
    public void setUp() throws Exception { 
        // Kích hoạt tất cả các @Mock
        MockitoAnnotations.initMocks(this);

        // Khởi tạo service bằng constructor test
        orderService = new OrderService(
            orderDaoMock, 
            detailDaoMock, 
            productDaoMock, 
            variantDaoMock, 
            sizeDaoMock, 
            dbContextMock
        );
        
        // =================================================================
        // === ĐÃ SỬA LỖI Ở ĐÂY ===
        // =================================================================
        // "Dạy" orderDaoMock (giả):
        // "Khi ai đó gọi .getMockConnection(), HÃY trả về connectionMock (giả)"
        // (Trước đây chúng ta mock hàm "getInjectedConnection()")
        when(orderDaoMock.getMockConnection()).thenReturn(connectionMock);
        
        // Chuẩn bị dữ liệu test
        fakeUser = new Users();
        fakeUser.setId(1);
        
        fakeVariant1 = new ProductVariants();
        fakeVariant1.setId(10);
        fakeVariant1.setPrice(100.0);
        
        fakeCart = new ArrayList<>();
        fakeCart.add(new CartBean(fakeVariant1, 2)); // 2 sản phẩm, 100.0 mỗi cái
    }

    // -- BẮT ĐẦU CÁC TEST CASE (ĐÃ SỬA LỖI) --

    /**
     * Test Case 1: Đặt hàng THÀNH CÔNG (Happy Path).
     */
    @Test
    public void testPlaceOrder_Success() throws Exception {
        // --- ARRANGE (Chuẩn bị) ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class))).thenReturn(123);
        // (Chúng ta giả định file DAO đã sửa, ném lỗi thay vì trả về false)
        when(detailDaoMock.addDetail(eq(connectionMock), any(OrderDetails.class))).thenReturn(true);

        // --- ACT (Hành động) ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT (Xác minh) ---
        assertEquals(OrderResult.SUCCESS, result);
        
        verify(connectionMock).setAutoCommit(false); 
        verify(orderDaoMock).addOrder(eq(connectionMock), any(Orders.class)); 
        verify(detailDaoMock).addDetail(eq(connectionMock), any(OrderDetails.class)); 
        verify(connectionMock).commit(); 
        verify(connectionMock, never()).rollback(); 
    }

    /**
     * Test Case 2: Đặt hàng thất bại do Giỏ hàng rỗng (TC2).
     */
    @Test
    public void testPlaceOrder_EmptyCart() throws Exception {
        List<CartBean> emptyCart = new ArrayList<>();
        OrderResult result = orderService.placeOrder(fakeUser, emptyCart, "123 Street", "09090909");
        assertEquals(OrderResult.EMPTY_CART, result);
        verify(orderDaoMock, never()).getMockConnection(); // Sửa thành getMockConnection
        verify(connectionMock, never()).setAutoCommit(false);
    }
    
    /**
     * Test Case 3: Đặt hàng thất bại do Thiếu thông tin.
     */
    @Test
    public void testPlaceOrder_MissingInfo() throws Exception {
        String emptyAddress = "   "; 
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, emptyAddress, "09090909");
        assertEquals(OrderResult.MISSING_INFO, result);
        verify(orderDaoMock, never()).getMockConnection(); // Sửa thành getMockConnection
    }
    
    /**
     * Test Case 4: Đặt hàng thất bại do addOrder NÉM LỖI.
     * (Giả định DAO đã được sửa để "throws Exception")
     */
    @Test
    public void testPlaceOrder_OrderFailed_ThrowsException() throws Exception {
        // --- ARRANGE ---
        // "Dạy" DAO: NÉM ra lỗi SQLException
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class)))
            .thenThrow(new SQLException("DAO Lỗi: Không thể thêm order"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        
        verify(connectionMock).setAutoCommit(false);
        verify(orderDaoMock).addOrder(eq(connectionMock), any(Orders.class));
        verify(connectionMock, never()).commit();
        verify(connectionMock).rollback();
        verify(detailDaoMock, never()).addDetail(any(), any());
    }
    
    /**
     * Test Case 5: Đặt hàng thất bại do addDetail NÉM LỖI.
     * (Giả định DAO đã được sửa để "throws Exception")
     */
    @Test
    public void testPlaceOrder_DetailFailed_ThrowsException() throws Exception {
        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class))).thenReturn(123);
        when(detailDaoMock.addDetail(eq(connectionMock), any(OrderDetails.class)))
            .thenThrow(new SQLException("DAO Lỗi: Không thể thêm detail"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        
        verify(connectionMock).setAutoCommit(false);
        verify(orderDaoMock).addOrder(eq(connectionMock), any(Orders.class));
        verify(detailDaoMock).addDetail(eq(connectionMock), any(OrderDetails.class));
        verify(connectionMock, never()).commit();
        verify(connectionMock).rollback();
    }
    
    /**
     * Test Case 6: Đặt hàng thất bại do Lỗi SQL (Đã sửa từ trước).
     */
    @Test
    public void testPlaceOrder_Exception() throws Exception {
        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class)))
            .thenThrow(new RuntimeException("Lỗi SQL (Giả lập)! Deadlock!"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock, never()).commit();
        verify(connectionMock).rollback();
    }


    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (GIỮ NGUYÊN) ===
    // =================================================================

    // (Giữ nguyên phần @Rule TestWatcher và @AfterClass writeTestReport)
        // 1. Danh sách lưu kết quả (mỗi phần tử là 1 mảng 4 cột)
    private static final List<String[]> testResults = new ArrayList<>();

    // 2. Sử dụng @Rule và TestWatcher để "theo dõi" từng test case
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        
        private String getModuleName(Description d) {
            String className = d.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);
        }

        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "PASS",
                ""
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMessage = (e == null) ? "Unknown Error" : e.getMessage();
            testResults.add(new String[]{
                getModuleName(description),
                description.getMethodName(),
                "FAIL",
                errorMessage
            });
        }
    };

    // 3. Sử dụng @AfterClass để in bảng và ghi ra file
    @AfterClass
    public static void writeTestReport() {
        String filePath = "test-report.txt";
        
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        
        // Sửa lỗi .repeat() cho Java 8
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'), 
            new String(new char[30]).replace('\0', '-')
        );
        separator = separator.replace("|", "+");


        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath, true), StandardCharsets.UTF_8)) { // 'true' để NỐI VÀO FILE
            
            writer.write("\n--- Kết quả chạy " + OrderServiceTest.class.getName() + " ---\n");
            
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                
                System.out.print(line);
                writer.write(line);
            }
            
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được NỐI VÀO file: " + filePath);

        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());
        }
    }
}