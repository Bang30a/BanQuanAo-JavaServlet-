<%@page import="entity.Products"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Products Manager</title>
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
            margin-bottom: 30px;
            text-align: center;
        }
    </style>
</head>
<body>
<%
    String action = (String) request.getAttribute("ACTION");
    Products product = (Products) request.getAttribute("PRODUCTS");
    if (action == null) action = "SaveOrUpdate";
    if (product == null) product = new Products();
%>

<div class="container">
    <div class="form-container">
        <h2>Quản lý sản phẩm</h2>
        <form action="<%=request.getContextPath()%>/admin/ProductsManager" method="post">
            <input type="hidden" name="action" value="<%= action %>">

            <div class="mb-3">
                <label class="form-label">ID</label>
            <input type="text" class="form-control" name="id" value="<%= product.getId() != 0 ? product.getId() : "" %>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên sản phẩm</label>
                <input type="text" class="form-control" name="name" value="<%= product.getName() != null ? product.getName() : "" %>" placeholder="Nhập tên sản phẩm" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Mô tả</label>
                <textarea class="form-control" name="description" rows="3" placeholder="Nhập mô tả sản phẩm"><%= product.getDescription() != null ? product.getDescription() : "" %></textarea>
            </div>

            <div class="mb-3">
                <label class="form-label">Giá (VNĐ)</label>
                <input type="number" step="0.01" class="form-control" name="price" value="<%= product.getPrice() %>" placeholder="Nhập giá sản phẩm">
            </div>

            <div class="mb-3">
                <label class="form-label">Link ảnh</label>
                <input type="text" class="form-control" name="image" value="<%= product.getImage() != null ? product.getImage() : "" %>" placeholder="Nhập đường dẫn ảnh">
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-success w-45">Lưu</button>
                <button type="reset" class="btn btn-outline-secondary w-45">Xóa trắng</button>
            </div>

            <div class="text-center mt-4">
                <a href="View-products.jsp" class="btn btn-info w-100">Xem danh sách sản phẩm</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
