<%@page import="entity.Orders"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý đơn hàng</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .form-container {
            max-width: 600px;
            margin: 50px auto;
            padding: 30px;
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }
        h2 {
            text-align: center;
            margin-bottom: 25px;
        }
    </style>
</head>
<body>
<%
    String action = (String) request.getAttribute("ACTION");
    Orders order = (Orders) request.getAttribute("ORDER");
    if (action == null) action = "SaveOrUpdate";
    if (order == null) order = new Orders();
%>

<div class="container">
    <div class="form-container">
        <h2>Quản lý đơn hàng</h2>
        <form action="<%=request.getContextPath()%>/admin/OrdersManager" method="post">
            <input type="hidden" name="action" value="<%=action%>">

            <div class="mb-3">
                <label class="form-label">ID Người dùng</label>
                <input type="text" class="form-control" name="userId" value="<%=order.getUserId()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày đặt hàng (yyyy-mm-dd hh:mm:ss)</label>
                <input type="text" class="form-control" name="orderDate" value="<%=order.getOrderDate()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Tổng tiền</label>
                <input type="number" step="0.01" class="form-control" name="total" value="<%=order.getTotal()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Địa chỉ giao hàng</label>
                <input type="text" class="form-control" name="address" value="<%=order.getAddress()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" name="phone" value="<%=order.getPhone()%>" required>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-success w-45">Lưu</button>
                <a href="View-orders.jsp" class="btn btn-outline-primary w-45">Xem danh sách</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
