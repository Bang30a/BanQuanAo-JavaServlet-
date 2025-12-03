package control.admin;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductVariantDao;
import entity.Orders;
import service.OrderAdminService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// 1. URL Pattern: Đã CHUẨN (khớp với URL trình duyệt)
@WebServlet("/admin/OrdersManagerServlet")
public class OrdersManagerServlet extends HttpServlet {

    private OrderAdminService adminService;

    @Override
    public void init() throws ServletException {
        OrderDao orderDao = new OrderDao();
        OrderDetailDao detailDao = new OrderDetailDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        
        this.adminService = new OrderAdminService(orderDao, detailDao, variantDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "List";
        }

        try {
            switch (action) {
                case "SaveOrUpdate":
                    handleSaveOrUpdate(request, response);
                    break;
                case "AddOrEdit":
                    handleAddOrEdit(request, response);
                    break;
                case "Delete":
                    handleDelete(request, response);
                    break;
                case "UpdateStatus":
                    handleUpdateStatus(request, response);
                    break;
                case "List":
                default:
                    handleList(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            // Đảm bảo bạn có file error.jsp ở thư mục web root
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        int userId = parseInt(request.getParameter("userId"));
        double total = Double.parseDouble(request.getParameter("total"));
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        
        // Lưu ý: Timestamp.valueOf yêu cầu format chuẩn "yyyy-mm-dd hh:mm:ss"
        // Nếu form gửi lên sai format sẽ gây lỗi 500 tại đây.
        Timestamp orderDate = Timestamp.valueOf(request.getParameter("orderDate")); 
        
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "Đang chờ";
        }

        Orders order = new Orders(id, userId, orderDate, total, address, phone, status);
        
        adminService.saveOrUpdateOrder(order);

        // Dùng getContextPath để redirect an toàn tuyệt đối
        response.sendRedirect(request.getContextPath() + "/admin/OrdersManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = parseInt(request.getParameter("id"));
        Orders order = adminService.getOrderForEdit(id); 

        request.setAttribute("ORDER", order);
        request.setAttribute("ACTION", "SaveOrUpdate");
        
        // --- SỬA QUAN TRỌNG: Thêm thư mục /order/ ---
        request.getRequestDispatcher("/admin/order/OrdersManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        adminService.deleteOrder(id);
        
        response.sendRedirect(request.getContextPath() + "/admin/OrdersManagerServlet?action=List");
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        String newStatus = request.getParameter("status");

        adminService.updateOrderStatus(id, newStatus); 
        
        response.sendRedirect(request.getContextPath() + "/admin/OrdersManagerServlet?action=List");
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String status = request.getParameter("status");
        List<Orders> list;

        if (status == null || status.trim().isEmpty()) {
            list = adminService.getAllOrders();
        } else {
            list = adminService.getOrdersByStatus(status);
        }

        request.setAttribute("selectedStatus", status);
        request.setAttribute("list", list); // <-- Gửi biến tên là "list"
        
        // --- SỬA QUAN TRỌNG: Thêm thư mục /order/ ---
        // Dựa trên ảnh cấu trúc folder bạn gửi: admin -> order -> View-orders.jsp
        request.getRequestDispatcher("/admin/order/View-orders.jsp").forward(request, response);
    }

    private int parseInt(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}