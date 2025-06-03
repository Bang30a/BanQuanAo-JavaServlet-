<%@page import="dao.OrderDao"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*, entity.Orders"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Đơn hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8fafc;
        }
        .container {
            margin-top: 40px;
            background-color: #fff;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #1e293b;
        }
        .btn-danger {
            background-color: #ef4444;
            border: none;
        }
        .btn-danger:hover {
            background-color: #dc2626;
        }
        .table th, .table td {
            vertical-align: middle;
        }
        .filter-form select {
            max-width: 200px;
            display: inline-block;
        }
    </style>
</head>
<body>

<div class="container">
    <h2>Danh sách Đơn hàng</h2>

    <!-- BỘ LỌC TRẠNG THÁI -->
    <form method="get" action="View-orders.jsp" class="mb-4 d-flex justify-content-end align-items-center filter-form">
        <label for="status" class="me-2 fw-bold">Lọc theo trạng thái:</label>
        <select name="status" id="status" class="form-select me-2">
            <option value="">-- Tất cả --</option>
            <option value="Đã hủy" <%= "Đã hủy".equals(request.getParameter("status")) ? "selected" : "" %>>Đã hủy</option>
            <option value="Đã giao" <%= "Đã giao".equals(request.getParameter("status")) ? "selected" : "" %>>Đã giao</option>
            <option value="Đang giao hàng" <%= "Đang giao hàng".equals(request.getParameter("status")) ? "selected" : "" %>>Đang giao hàng</option>
            <option value="Chờ xử lý" <%= "Chờ xử lý".equals(request.getParameter("status")) ? "selected" : "" %>>Chờ xử lý</option>
        </select>
        <button type="submit" class="btn btn-primary">Lọc</button>
    </form>

    <table class="table table-bordered table-hover text-center">
        <thead class="table-dark">
            <tr>
                <th>Mã đơn</th>
                <th>Mã người dùng</th>
                <th>Ngày đặt</th>
                <th>Tổng tiền</th>
                <th>Địa chỉ</th>
                <th>Số điện thoại</th>
                <th>Trạng thái</th>
                <th>Chi tiết</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <%
                String statusFilter = request.getParameter("status");
                OrderDao dao = new OrderDao();
                List<Orders> arrPro = (statusFilter == null || statusFilter.isEmpty())
                    ? dao.getAllOrders()
                    : dao.getOrdersByStatus(statusFilter);

                if (arrPro != null && !arrPro.isEmpty()) {
                    for (Orders item : arrPro) {
            %>
            <tr>
                <td><%= item.getId() %></td>
                <td><%= item.getUserId() %></td>
                <td><%= item.getOrderDate() %></td>
                <td><%= String.format("%,.0f", item.getTotal()) %> VNĐ</td>
                <td><%= item.getAddress() %></td>
                <td><%= item.getPhone() %></td>
                <td>
                    <form action="OrdersManagerServlet" method="post" class="m-0">
                        <input type="hidden" name="action" value="UpdateStatus">
                        <input type="hidden" name="id" value="<%= item.getId() %>">
                        <select name="status" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="Chờ xử lý" <%= "Chờ xử lý".equals(item.getStatus()) ? "selected" : "" %>>Chờ xử lý</option>
                            <option value="Đang giao hàng" <%= "Đang giao hàng".equals(item.getStatus()) ? "selected" : "" %>>Đang giao hàng</option>
                            <option value="Đã giao" <%= "Đã giao".equals(item.getStatus()) ? "selected" : "" %>>Đã giao</option>
                            <option value="Đã hủy" <%= "Đã hủy".equals(item.getStatus()) ? "selected" : "" %>>Đã hủy</option>
                        </select>
                    </form>
                </td>
                <td>
                    <a class="btn btn-sm btn-info" href="OrderDetailManagerServlet?action=List&orderId=<%= item.getId() %>">Xem</a>
                </td>
                <td>
                    <a class="btn btn-sm btn-danger" href="OrdersManagerServlet?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá đơn hàng này?')">Xoá</a>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="9" class="text-center text-muted">Không có đơn hàng nào.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</div>

</body>
</html>
