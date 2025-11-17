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
            background-color: #eef2f7;
            font-family: 'Segoe UI', sans-serif;
        }

        .form-container {
            max-width: 650px;
            margin: 60px auto;
            padding: 35px;
            background: #ffffff;
            border-radius: 18px;
            border: 1px solid #dee2e6;
            box-shadow: 0 8px 25px rgba(0,0,0,0.08);
            transition: 0.3s;
        }

        .form-container:hover {
            box-shadow: 0 12px 40px rgba(0,0,0,0.12);
        }

        h2 {
            font-size: 26px;
            font-weight: 600;
            margin-bottom: 30px;
            text-align: center;
            color: #333;
        }

        .form-label {
            font-weight: 500;
        }

        .btn-custom {
            width: 48%;
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
                <label class="form-label">ID sản phẩm</label>
                <input type="text" class="form-control" name="id" value="<%= product.getId() != 0 ? product.getId() : "" %>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên sản phẩm</label>
                <input type="text" class="form-control" name="name" value="<%= product.getName() != null ? product.getName() : "" %>" placeholder="Nhập tên sản phẩm" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Mô tả</label>
                <textarea class="form-control" name="description" rows="4" placeholder="Nhập mô tả sản phẩm"><%= product.getDescription() != null ? product.getDescription() : "" %></textarea>
            </div>

            <div class="mb-3">
                <label class="form-label">Giá (VNĐ)</label>
                <input type="number" class="form-control" name="price" value="<%= product.getPrice() %>" placeholder="Nhập giá">
            </div>

            <div class="mb-3">
                <label class="form-label">Link ảnh sản phẩm</label>
                <input type="text" class="form-control" name="image" value="<%= product.getImage() != null ? product.getImage() : "" %>" placeholder="https://...">
            </div>

            <div class="d-flex justify-content-between mt-4">
                <button type="submit" class="btn btn-success btn-custom">Lưu</button>
                <button type="reset" class="btn btn-outline-secondary btn-custom">Xóa trắng</button>
            </div>

            <div class="text-center mt-4">
                <a href="View-products.jsp" class="btn btn-primary w-100">Xem danh sách sản phẩm</a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
