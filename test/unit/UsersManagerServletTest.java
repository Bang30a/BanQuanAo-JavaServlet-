package unit;

import control.admin.UsersManagerServlet;
import service.UserService;
import entity.Users;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    // === BIẾN GHI REPORT ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new UsersManagerServlet();
        // Inject Mock Service
        try {
            Field serviceField = UsersManagerServlet.class.getDeclaredField("userService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, userService);
        } catch (NoSuchFieldException e) {
            System.err.println("Warning: Không tìm thấy userService trong Servlet");
        }
    }

    // --- CASE 1: XEM DANH SÁCH ---
    @Test
    public void testDoGet_ListUsers() throws Exception {
        setTestCaseInfo("USER_MGR_01", "Xem danh sách User", 
                "Action='List'", "List size=1", "Forward View-users.jsp");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-users.jsp"))).thenReturn(dispatcher);
        
        List<Users> mockList = new ArrayList<>();
        mockList.add(new Users());
        when(userService.getAllUsers()).thenReturn(mockList);

        invokeDoGet();

        verify(request).setAttribute(eq("USERS"), eq(mockList));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 2: HIỆN FORM SỬA ---
    @Test
    public void testDoGet_AddOrEdit() throws Exception {
        setTestCaseInfo("USER_MGR_02", "Hiện form sửa User", 
                "Action='AddOrEdit', ID=5", "ID=5", "Forward UsersManager.jsp");

        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher(contains("UsersManager.jsp"))).thenReturn(dispatcher);

        Users mockUser = new Users(); mockUser.setId(5);
        when(userService.getUserForEdit(5)).thenReturn(mockUser);

        invokeDoGet();

        verify(request).setAttribute(eq("USER"), eq(mockUser));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 3: LƯU THÀNH CÔNG (Happy Path) ---
    @Test
    public void testDoPost_Save_Success() throws Exception {
        setTestCaseInfo("USER_MGR_03", "Lưu User thành công", 
                "Action='SaveOrUpdate', Full Data", "User: test", "Redirect List");

        setupMockPostParams("testadmin", "123");
        when(request.getContextPath()).thenReturn("/ShopDuck");
        
        // Mock Service trả về SUCCESS
        when(userService.saveOrUpdateUser(any(Users.class))).thenReturn("SUCCESS");

        invokeDoPost();

        verify(response).sendRedirect(contains("/admin/UsersManagerServlet?action=List"));
    }

    // --- CASE 4: LƯU THẤT BẠI - DỮ LIỆU RỖNG (Negative Path) ---
    @Test
    public void testDoPost_Save_EmptyData() throws Exception {
        setTestCaseInfo("USER_MGR_04", "Lỗi: Dữ liệu rỗng", 
                "Action='SaveOrUpdate', User=''", "Username rỗng", "Forward lại trang nhập + Báo lỗi");

        // Mock param rỗng
        setupMockPostParams("", ""); 
        when(request.getRequestDispatcher(contains("UsersManager.jsp"))).thenReturn(dispatcher);

        // Mock Service trả về lỗi
        when(userService.saveOrUpdateUser(any(Users.class))).thenReturn("Vui lòng nhập Username!");

        invokeDoPost();

        // Verify: Không được redirect, phải forward về trang cũ để hiện lỗi
        verify(request).setAttribute(eq("ERROR"), eq("Vui lòng nhập Username!"));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 5: LƯU THẤT BẠI - TRÙNG USERNAME (Negative Path) ---
    @Test
    public void testDoPost_Save_Duplicate() throws Exception {
        setTestCaseInfo("USER_MGR_05", "Lỗi: Trùng Username", 
                "Action='SaveOrUpdate'", "User: admin (đã có)", "Báo lỗi 'Đã tồn tại'");

        setupMockPostParams("admin", "123");
        when(request.getRequestDispatcher(contains("UsersManager.jsp"))).thenReturn(dispatcher);

        // Mock Service trả về lỗi trùng
        when(userService.saveOrUpdateUser(any(Users.class))).thenReturn("Username đã tồn tại!");

        invokeDoPost();

        verify(request).setAttribute(eq("ERROR"), eq("Username đã tồn tại!"));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 6: XÓA ---
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("USER_MGR_06", "Xóa User", "Action='Delete', ID=10", "ID=10", "Redirect List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(userService).deleteUser(10);
        verify(response).sendRedirect(contains("action=List"));
    }

    // === HELPER METHODS ===
    private void invokeDoGet() throws Exception {
        Method doGet = UsersManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }
    
    private void invokeDoPost() throws Exception {
        Method doPost = UsersManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    private void setupMockPostParams(String user, String pass) {
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("username")).thenReturn(user);
        when(request.getParameter("password")).thenReturn(pass);
        when(request.getParameter("fullname")).thenReturn("Test Name");
        when(request.getParameter("email")).thenReturn("test@mail.com");
        when(request.getParameter("role")).thenReturn("user");
    }

    // === XUẤT EXCEL ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS"); }
        @Override protected void failed(Throwable e, Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL"); }
    };

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_UsersManagerServlet.xlsx"); }
}