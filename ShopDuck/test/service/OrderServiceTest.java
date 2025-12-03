package service;

// === IMPORT CƠ BẢN ===
import dao.*;
import entity.*;
import context.DBContext;
import java.sql.Connection;
import java.sql.SQLException;
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

// === IMPORT ĐỂ TẠO BÁO CÁO EXCEL/CSV ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Unit Test cho OrderService (Chức năng Đặt hàng).
 * Xuất kết quả ra file Excel (CSV).
 */
public class OrderServiceTest {

    // 1. Tạo MOCK cho TẤT CẢ các phụ thuộc
    @Mock private OrderDao orderDaoMock;
    @Mock private OrderDetailDao detailDaoMock;
    @Mock private ProductDao productDaoMock; 
    @Mock private ProductVariantDao variantDaoMock; 
    @Mock private SizeDao sizeDaoMock; 
    @Mock private DBContext dbContextMock; 
    
    // 2. Tạo MOCK cho Connection (để kiểm soát transaction)
    @Mock private Connection connectionMock;

    // 3. Đối tượng Service sẽ được test
    private OrderService orderService;

    // 4. Dữ liệu "giả" để test
    private Users fakeUser;
    private List<CartBean> fakeCart;
    private ProductVariants fakeVariant1;

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    private static final List<String[]> finalReportData = new ArrayList<>();
    
    @Before
    public void setUp() throws Exception { 
        MockitoAnnotations.openMocks(this); // Dùng openMocks thay cho initMocks (mới hơn)

        // Khởi tạo service
        orderService = new OrderService(
            orderDaoMock, 
            detailDaoMock, 
            productDaoMock, 
            variantDaoMock, 
            sizeDaoMock, 
            dbContextMock
        );
        
        // Setup Connection giả
        when(orderDaoMock.getMockConnection()).thenReturn(connectionMock);
        
        // Setup Dữ liệu test
        fakeUser = new Users();
        fakeUser.setId(1);
        
        fakeVariant1 = new ProductVariants();
        fakeVariant1.setId(10);
        fakeVariant1.setPrice(100.0);
        
        fakeCart = new ArrayList<>();
        fakeCart.add(new CartBean(fakeVariant1, 2)); // 2 sản phẩm, 100.0 mỗi cái
    }

    // Hàm điền thông tin Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testPlaceOrder_Success() throws Exception {
        setTestCaseInfo(
            "ORDER_01", 
            "Đặt hàng thành công (Happy Path)", 
            "1. Có giỏ hàng\n2. Đủ thông tin Address/Phone", 
            "User: 1\nCart: 1 item (Price: 100)", 
            "Trả về SUCCESS\nCommit Transaction"
        );

        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class))).thenReturn(123);
        when(detailDaoMock.addDetail(eq(connectionMock), any(OrderDetails.class))).thenReturn(true);

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.SUCCESS, result);
        
        // Verify Transaction flow
        verify(connectionMock).setAutoCommit(false); 
        verify(connectionMock).commit(); 
        verify(connectionMock, never()).rollback(); 
    }

    @Test
    public void testPlaceOrder_EmptyCart() throws Exception {
        setTestCaseInfo(
            "ORDER_02", 
            "Đặt hàng thất bại (Giỏ rỗng)", 
            "Giỏ hàng không có sản phẩm nào", 
            "Cart: [] (Empty)", 
            "Trả về EMPTY_CART\nKhông mở Connection"
        );

        List<CartBean> emptyCart = new ArrayList<>();
        OrderResult result = orderService.placeOrder(fakeUser, emptyCart, "123 Street", "09090909");
        
        assertEquals(OrderResult.EMPTY_CART, result);
        verify(connectionMock, never()).setAutoCommit(false);
    }
    
    @Test
    public void testPlaceOrder_MissingInfo() throws Exception {
        setTestCaseInfo(
            "ORDER_03", 
            "Đặt hàng thất bại (Thiếu địa chỉ)", 
            "Truyền Address là chuỗi rỗng/space", 
            "Address: '   '", 
            "Trả về MISSING_INFO"
        );

        String emptyAddress = "   "; 
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, emptyAddress, "09090909");
        
        assertEquals(OrderResult.MISSING_INFO, result);
    }
    
    @Test
    public void testPlaceOrder_OrderFailed_ThrowsException() throws Exception {
        setTestCaseInfo(
            "ORDER_04", 
            "Lỗi Database khi Insert Order", 
            "DAO addOrder ném ra SQLException", 
            "Simulate SQL Exception", 
            "Trả về EXCEPTION\nRollback Transaction"
        );

        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class)))
            .thenThrow(new SQLException("DAO Lỗi: Không thể thêm order"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        
        verify(connectionMock).setAutoCommit(false);
        verify(connectionMock, never()).commit();
        verify(connectionMock).rollback(); // Phải Rollback
    }
    
    @Test
    public void testPlaceOrder_DetailFailed_ThrowsException() throws Exception {
        setTestCaseInfo(
            "ORDER_05", 
            "Lỗi Database khi Insert Detail", 
            "1. Insert Order OK\n2. Insert Detail ném lỗi", 
            "Simulate Detail SQL Error", 
            "Trả về EXCEPTION\nRollback Transaction"
        );

        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class))).thenReturn(123);
        when(detailDaoMock.addDetail(eq(connectionMock), any(OrderDetails.class)))
            .thenThrow(new SQLException("DAO Lỗi: Không thể thêm detail"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        verify(connectionMock).rollback();
    }
    
    @Test
    public void testPlaceOrder_Exception() throws Exception {
        setTestCaseInfo(
            "ORDER_06", 
            "Lỗi Runtime không mong muốn", 
            "Hệ thống gặp lỗi lạ (Deadlock, NullPointer...)", 
            "RuntimeException", 
            "Trả về EXCEPTION\nRollback Transaction"
        );

        // --- ARRANGE ---
        when(orderDaoMock.addOrder(eq(connectionMock), any(Orders.class)))
            .thenThrow(new RuntimeException("Lỗi SQL (Giả lập)! Deadlock!"));

        // --- ACT ---
        OrderResult result = orderService.placeOrder(fakeUser, fakeCart, "123 Street", "09090909");

        // --- ASSERT ---
        assertEquals(OrderResult.EXCEPTION, result);
        verify(connectionMock).rollback();
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT FILE EXCEL (CSV) ===
    // ==========================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, currentExpected, "PASS"
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMsg = (e != null) ? e.getMessage() : "Unknown Error";
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, errorMsg, "FAIL"
            });
        }
    };

    @AfterClass
    public static void exportToExcelCSV() {
        String fileName = "KetQuaTest_DonHang.csv"; // Tên file riêng
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo Đơn Hàng ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            writer.write('\ufeff'); // BOM cho Tiếng Việt

            // Header
            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            // Data
            for (String[] row : finalReportData) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialChars(row[0]),
                        escapeSpecialChars(row[1]),
                        escapeSpecialChars(row[2]),
                        escapeSpecialChars(row[3]),
                        escapeSpecialChars(row[4]),
                        escapeSpecialChars(row[5]),
                        escapeSpecialChars(row[6])
                );
                writer.println(line);
            }
            
            System.out.println("XONG! File 'KetQuaTest_DonHang.csv' đã được tạo.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }

    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}