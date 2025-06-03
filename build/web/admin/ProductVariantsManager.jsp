<%@page import="entity.ProductVariants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý biến thể sản phẩm</title>
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
    ProductVariants pv = (ProductVariants) request.getAttribute("VARIANT");
    if (action == null) action = "SaveOrUpdate";
    if (pv == null) pv = new ProductVariants();
%>

<div class="container">
    <div class="form-container">
        <h2>Quản lý biến thể sản phẩm</h2>
        <form action="<%=request.getContextPath()%>/admin/ProductVariantsManagerServlet" method="post">
            <input type="hidden" name="action" value="<%=action%>">

            <div class="mb-3">
                <label class="form-label">ID</label>
                <input type="text" class="form-control" name="id" value="<%=pv.getId()%>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">ID Sản phẩm</label>
                <input type="text" class="form-control" name="productId" value="<%=pv.getProductId()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">ID Kích thước</label>
                <input type="text" class="form-control" name="sizeId" value="<%=pv.getSizeId()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Tồn kho</label>
                <input type="number" class="form-control" name="stock" value="<%=pv.getStock()%>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Giá</label>
                <input type="number" step="0.01" class="form-control" name="price" value="<%=pv.getPrice()%>" required>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-success w-45">Lưu</button>
                <a href="<%=request.getContextPath()%>/admin/ProductVariantsManagerServlet?action=List" class="btn btn-outline-primary w-45">Xem danh sách</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
