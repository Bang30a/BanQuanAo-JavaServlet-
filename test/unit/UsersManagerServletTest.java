package unit;

// === IMPORT LOGIC ===
import control.admin.UsersManagerServlet;
import service.UserService;
import entity.Users;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class UsersManagerServletTest {

    private UsersManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private UserService userService;

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new UsersManagerServlet();

        // Inject Mock Service vào Servlet bằng Reflection
        try {
            Field serviceField = UsersManagerServlet.class.getDeclaredField("userService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, userService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'userService' trong Servlet");
        }
    }

    // ==========================================
    // 1. TEST CHỨC NĂNG LIST (Xem danh sách)
    // ==========================================
    @Test
    public void testDoGet_ListUsers() throws Exception {
        setTestCaseInfo("USER_MGR_01", "Xem danh sách User", 
                "1. Action='List'\n2. Service trả list", 
                "List size=1", "Forward View-users.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-users.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        List<Users> mockList = new ArrayList<>();
        mockList.add(new Users());
        when(userService.getAllUsers()).thenReturn(mockList);

        // 3. Run (Reflection calling doGet -> processRequest)
        Method doGet = UsersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("USERS"), eq(mockList)); // Kiểm tra setAttribute đúng tên "USERS"
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST CHỨC NĂNG PREPARE ADD/EDIT (Hiện form)
    // ==========================================
    @Test
    public void testDoGet_AddOrEdit() throws Exception {
        setTestCaseInfo("USER_MGR_02", "Hiện form sửa User", 
                "1. Action='AddOrEdit', ID=5\n2. Service trả User", 
                "ID=5", "Forward UsersManager.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("UsersManager.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        Users mockUser = new Users(); 
        mockUser.setId(5);
        when(userService.getUserForEdit(5)).thenReturn(mockUser);

        // 3. Run
        Method doGet = UsersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("USER"), eq(mockUser));
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 3. TEST CHỨC NĂNG SAVE/UPDATE (Lưu)
    // ==========================================
    @Test
    public void testDoPost_SaveOrUpdate() throws Exception {
        setTestCaseInfo("USER_MGR_03", "Lưu User mới", 
                "1. Action='SaveOrUpdate'\n2. Params đầy đủ", 
                "User: test, Pass: 123", "Call Service Save -> Redirect List");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("username")).thenReturn("testadmin");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getParameter("fullname")).thenReturn("Test Admin");
        when(request.getParameter("email")).thenReturn("admin@test.com");
        when(request.getParameter("role")).thenReturn("admin");
        
        when(request.getContextPath()).thenReturn("/ShopDuck"); // Mock context path

        // 2. Run
        Method doPost = UsersManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 3. Verify
        // Kiểm tra xem service có được gọi với đúng object user không
        verify(userService).saveOrUpdateUser(any(Users.class)); 
        // Kiểm tra redirect về đúng trang list
        verify(response).sendRedirect(contains("/admin/UsersManagerServlet?action=List"));
    }

    // ==========================================
    // 4. TEST CHỨC NĂNG DELETE (Xóa)
    // ==========================================
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("USER_MGR_04", "Xóa User", 
                "1. Action='Delete'\n2. ID=10", 
                "ID=10", "Call Service Delete -> Redirect List");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // 2. Run
        Method doGet = UsersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 3. Verify
        verify(userService).deleteUser(10); // Phải gọi hàm xóa với ID 10
        verify(response).sendRedirect(contains("/admin/UsersManagerServlet?action=List"));
    }

    // === XUẤT EXCEL MỚI ===
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_UsersManagerServlet.xlsx");
    }
}