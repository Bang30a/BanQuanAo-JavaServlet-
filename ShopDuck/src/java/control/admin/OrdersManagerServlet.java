package control.admin;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductVariantDao;
import entity.Orders;
import service.OrderAdminService; // <-- Import service

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/OrdersManagerServlet")
public class OrdersManagerServlet extends HttpServlet {

    private OrderAdminService adminService; // <-- Tham chiếu đến service

    @Override
    public void init() throws ServletException {
        // Khởi tạo tất cả DAO và "tiêm" vào Service
        OrderDao orderDao = new OrderDao();
        OrderDetailDao detailDao = new OrderDetailDao();
        ProductVariantDao variantDao = new ProductVariantDao();
        
        this.adminService = new OrderAdminService(orderDao, detailDao, variantDao);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        // KHÔNG tạo DAO ở đây nữa

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
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    // --- Tách các action ra thành các hàm riêng cho rõ ràng ---

    private void handleSaveOrUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        int userId = parseInt(request.getParameter("userId"));
        double total = Double.parseDouble(request.getParameter("total"));
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        Timestamp orderDate = Timestamp.valueOf(request.getParameter("orderDate")); // Cẩn thận: có thể ném lỗi nếu format sai
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "Đang chờ";
        }

        Orders order = new Orders(id, userId, orderDate, total, address, phone, status);
        
        // Gọi Service
        adminService.saveOrUpdateOrder(order);

        response.sendRedirect("OrdersManagerServlet?action=List");
    }

    private void handleAddOrEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int id = parseInt(request.getParameter("id"));
        
        // Service tự xử lý logic (nếu id=0 trả về order rỗng)
        Orders order = adminService.getOrderForEdit(id); 

        request.setAttribute("ORDER", order);
        request.setAttribute("ACTION", "SaveOrUpdate");
        request.getRequestDispatcher("/admin/OrdersManager.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        
        // Gọi Service
        adminService.deleteOrder(id);
        
        response.sendRedirect("OrdersManagerServlet?action=List");
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int id = parseInt(request.getParameter("id"));
        String newStatus = request.getParameter("status");

        // Gọi Service (nơi chứa logic phức tạp)
        adminService.updateOrderStatus(id, newStatus); 
        
        response.sendRedirect("OrdersManagerServlet?action=List");
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
    request.setAttribute("list", list);
    request.getRequestDispatcher("/admin/View-orders.jsp").forward(request, response);
}



    private int parseInt(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 0;
    }

    // --- Các hàm doGet, doPost giữ nguyên ---

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