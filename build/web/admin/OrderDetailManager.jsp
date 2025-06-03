<%@ page import="entity.OrderDetails" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý chi tiết đơn hàng</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .form-container {
            max-width: 650px;
            margin: 50px auto;
            padding: 30px;
            background: #ffffff;
            border-radius: 15px;
            box-shadow: 0 0 15px rgba(0,0,0,0.1);
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
    OrderDetails detail = (OrderDetails) request.getAttribute("DETAIL");
    if (action == null) action = "SaveOrUpdate";
    if (detail == null) detail = new OrderDetails();
%>

<div class="container">
    <div class="form-container">
        <h2>Quản lý chi tiết đơn hàng</h2>
        <form action="<%=request.getContextPath()%>/admin/OrderDetailManagerServlet" method="post">
            <input type="hidden" name="action" value="<%=action%>">

            <div class="mb-3">
                <label class="form-label">Mã chi tiết</label>
                <input type="text" class="form-control" name="id" value="<%=detail.getId()%>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Mã đơn hàng</label>
                <input type="text" class="form-control" name="orderId" value="<%=detail.getOrderId()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Mã phiên bản sản phẩm</label>
                <input type="text" class="form-control" name="productVariantId" value="<%=detail.getProductVariantId()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên sản phẩm</label>
                <input type="text" class="form-control" value="<%=detail.getProductName() != null ? detail.getProductName() : "Không rõ"%>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Số lượng</label>
                <input type="number" class="form-control" name="quantity" value="<%=detail.getQuantity()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Giá</label>
                <input type="number" step="0.01" class="form-control" name="price" value="<%=detail.getPrice()%>" required>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-success w-45">Lưu</button>
                <a href="<%=request.getContextPath()%>/admin/OrderDetailManagerServlet?action=List&orderId=<%=detail.getOrderId()%>" class="btn btn-outline-secondary w-45">Quay lại danh sách</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
