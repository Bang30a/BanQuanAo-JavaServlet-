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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
        // Inject Mock Service
        try {
            Field serviceField = SizesManagerServlet.class.getDeclaredField("sizeService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, sizeService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'sizeService' trong Servlet");
        }
    }

    // --- CASE 1: XEM DANH SÁCH ---
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

        verify(request).setAttribute(eq("list"), eq(mockList)); 
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 2: HIỆN FORM SỬA ---
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
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 3: LƯU THÀNH CÔNG ---
    @Test
    public void testDoPost_SaveNew_Success() throws Exception {
        setTestCaseInfo("SIZE_MGR_03", "Lưu Size mới thành công", 
                "Action='SaveOrUpdate', Label='XL'", "Service returns SUCCESS", "Redirect List");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn("XL");
        when(request.getParameter("id")).thenReturn(""); 
        when(request.getContextPath()).thenReturn("/ShopDuck");

        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("SUCCESS");

        invokeDoPost();

        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // --- CASE 4: LƯU THẤT BẠI - RỖNG ---
    @Test
    public void testDoPost_Save_Empty() throws Exception {
        setTestCaseInfo("SIZE_MGR_04", "Lỗi: Tên Size rỗng", 
                "Label=''", "Service returns Error", "Báo lỗi & Forward lại");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn(""); 
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("Tên Size không được để trống!");

        invokeDoPost();

        verify(request).setAttribute(eq("ERROR"), eq("Tên Size không được để trống!"));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 5: LƯU THẤT BẠI - TRÙNG ---
    @Test
    public void testDoPost_Save_Duplicate() throws Exception {
        setTestCaseInfo("SIZE_MGR_05", "Lỗi: Trùng Size", 
                "Label='S' (đã có)", "Service returns Error", "Báo lỗi & Forward lại");

        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn("S");
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        when(sizeService.saveOrUpdateSize(any(Size.class))).thenReturn("Size 'S' đã tồn tại!");

        invokeDoPost();

        verify(request).setAttribute(eq("ERROR"), eq("Size 'S' đã tồn tại!"));
        verify(dispatcher).forward(request, response);
    }

    // --- CASE 6: XÓA ---
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("SIZE_MGR_06", "Xóa Size", 
                "Action='Delete', ID=5", "ID=5", "Call Service Delete -> Redirect");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        verify(sizeService).deleteSize(5); 
        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // --- [MỚI] CASE 7: ACTION MẶC ĐỊNH ---
    @Test
    public void testDoGet_DefaultAction() throws Exception {
        setTestCaseInfo("SIZE_MGR_07", "Action Null -> Mặc định List", 
                "Action=null", "Null", "Forward View-sizes.jsp");

        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher(contains("View-sizes.jsp"))).thenReturn(dispatcher);

        invokeDoGet();

        // Verify: Mặc định phải gọi getAllSizes
        verify(sizeService).getAllSizes();
        verify(dispatcher).forward(request, response);
    }

    // --- [MỚI] CASE 8: XÓA ID RÁC ---
    @Test
    public void testDoGet_Delete_InvalidId() throws Exception {
        setTestCaseInfo("SIZE_MGR_08", "Xóa ID rác", 
                "Action='Delete', ID='abc'", "ID='abc'", "Không gọi delete, Redirect List");

        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("abc"); // ID không phải số
        when(request.getContextPath()).thenReturn("/ShopDuck");

        invokeDoGet();

        // Verify: Service KHÔNG được gọi hàm delete với bất kỳ số nào
        verify(sizeService, never()).deleteSize(anyInt());
        // Verify: Vẫn phải redirect an toàn
        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // --- [MỚI] CASE 9: LỖI HỆ THỐNG (EXCEPTION) ---
    @Test
    public void testProcessRequest_SystemError() throws Exception {
        setTestCaseInfo("SIZE_MGR_09", "Lỗi hệ thống (500)", 
                "Service ném RuntimeException", "Exception", "Gửi lỗi 500");

        when(request.getParameter("action")).thenReturn("List");
        // Giả lập Service bị lỗi bất ngờ (NullPointer, DB Error...)
        doThrow(new RuntimeException("DB Connection Lost")).when(sizeService).getAllSizes();

        invokeDoGet();

        // Verify: Servlet phải bắt lỗi và trả về mã 500
        verify(response).sendError(eq(500), contains("DB Connection Lost"));
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
        @Override protected void succeeded(Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps để khớp với file Excel
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };


    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_SizesManagerServlet.xlsx"); }
}