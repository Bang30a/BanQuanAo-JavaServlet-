package unit;

import control.admin.SizesManagerServlet;
import entity.Size;
import service.SizeService;
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
public class SizesManagerServletTest {

    private SizesManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private SizeService sizeService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new SizesManagerServlet();
        // Inject Mock Service vào Servlet bằng Reflection
        try {
            Field serviceField = SizesManagerServlet.class.getDeclaredField("sizeService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, sizeService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'sizeService' trong Servlet");
        }
    }

    // ==========================================
    // 1. TEST LIST (Xem danh sách Size)
    // ==========================================
    @Test
    public void testDoGet_ListSizes() throws Exception {
        setTestCaseInfo("SIZE_MGR_01", "Xem danh sách Size", 
                "Action='List'", "List size=2", "Forward View-sizes.jsp");

        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-sizes.jsp"))).thenReturn(dispatcher);

        List<Size> mockList = new ArrayList<>();
        mockList.add(new Size(1, "S"));
        mockList.add(new Size(2, "M"));
        when(sizeService.getAllSizes()).thenReturn(mockList);

        invokeDoGet();

        // Kiểm tra xem servlet setAttribute tên là "list" hay "SIZES" (theo code cũ của bạn là "list")
        verify(request).setAttribute(eq("list"), eq(mockList)); 
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST PREPARE ADD/EDIT (Hiện form)
    // ==========================================
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("SIZE_MGR_02", "Hiện form sửa Size", 
                "Action='AddOrEdit', ID=1", "ID=1", "Forward SizesManager.jsp");

        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        Size s = new Size(1, "S");
        when(sizeService.getSizeForEdit(1)).thenReturn(s);

        invokeDoGet();

        verify(request).setAttribute(eq("SIZE"), eq(s));
        // Kiểm tra action set vào attribute để form biết đường submit
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 3. TEST SAVE SUCCESS (Lưu thành công)
    // ==========================================
    @Test
    public void testDoPost_SaveNew_Success() throws Exception {
        setTestCaseInfo("SIZE_MGR_03", "Lưu Size mới thành công", 
                "Action='SaveOrUpdate', Label='XL'", "Service returns SUCCESS", "Redirect List");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn("XL");
        when(request.getParameter("id")).thenReturn(""); 
        when(request.getContextPath()).thenReturn("/ShopDuck");

        // QUAN TRỌNG: Giả lập Service trả về "SUCCESS" (String)
        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("SUCCESS");

        invokeDoPost();

        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // ==========================================
    // 4. TEST SAVE FAIL - EMPTY (Lỗi rỗng)
    // ==========================================
    @Test
    public void testDoPost_Save_Empty() throws Exception {
        setTestCaseInfo("SIZE_MGR_04", "Lỗi: Tên Size rỗng", 
                "Label=''", "Service returns Error", "Báo lỗi & Forward lại");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn(""); // Rỗng
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        // QUAN TRỌNG: Giả lập Service trả về lỗi
        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("Tên Size không được để trống!");

        invokeDoPost();

        // Verify: Không redirect, mà phải forward kèm thông báo lỗi
        verify(request).setAttribute(eq("ERROR"), eq("Tên Size không được để trống!"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 5. TEST SAVE FAIL - DUPLICATE (Lỗi trùng)
    // ==========================================
    @Test
    public void testDoPost_Save_Duplicate() throws Exception {
        setTestCaseInfo("SIZE_MGR_05", "Lỗi: Trùng Size", 
                "Label='S' (đã có)", "Service returns Error", "Báo lỗi & Forward lại");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn("S");
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        // QUAN TRỌNG: Giả lập Service trả về lỗi trùng
        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("Size 'S' đã tồn tại!");

        invokeDoPost();

        verify(request).setAttribute(eq("ERROR"), eq("Size 'S' đã tồn tại!"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 6. TEST DELETE (Xóa)
    // ==========================================
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("SIZE_MGR_06", "Xóa Size", 
                "Action='Delete', ID=5", "ID=5", "Call Service Delete -> Redirect");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("5");

        invokeDoGet();

        verify(sizeService).deleteSize(5); 
        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // === HELPER METHODS ===
    private void invokeDoGet() throws Exception {
        Method doGet = SizesManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);
    }

    private void invokeDoPost() throws Exception {
        Method doPost = SizesManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // === EXCEL EXPORT ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS"); }
        @Override protected void failed(Throwable e, Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL"); }
    };

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_SizesManagerServlet.xlsx"); }
}