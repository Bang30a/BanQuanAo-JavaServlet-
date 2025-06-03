<%@page import="dao.ProductVariantDao"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*, entity.ProductVariants"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Biến thể sản phẩm</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8fafc;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .container {
            background-color: #ffffff;
            padding: 30px;
            border-radius: 12px;
            margin-top: 40px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.05);
        }

        h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #1e293b;
        }

        .btn-success {
            background-color: #22c55e;
            border: none;
        }

        .btn-success:hover {
            background-color: #16a34a;
        }

        .btn-primary {
            background-color: #3b82f6;
            border: none;
        }

        .btn-primary:hover {
            background-color: #2563eb;
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

        .text-muted {
            color: #9ca3af !important;
        }
    </style>
</head>
<body>

<div class="container">
    <h2>Danh sách Biến thể sản phẩm</h2>

    <div class="mb-3 text-end">
        <a href="ProductVariantsManagerServlet?action=AddOrEdit" class="btn btn-success">+ Thêm biến thể mới</a>
    </div>

    <table class="table table-bordered table-hover text-center align-middle">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Tên sản phẩm</th>
                <th>Size</th>
                <th>Tồn kho</th>
                <th>Giá</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <%
                ProductVariantDao dao = new ProductVariantDao();
                List<ProductVariants> arrPro = dao.getAllVariants();

                if (arrPro != null && !arrPro.isEmpty()) {
                    for (ProductVariants item : arrPro) {
            %>
            <tr>
                <td><%= item.getId() %></td>
                <td><%= item.getProductName() %></td>
                <td><%= item.getSizeName() %></td>
                <td><%= item.getStock() %></td>
                <td><%= String.format("%,.0f", item.getPrice()) %> VNĐ</td>
                <td>
                    <a class="btn btn-sm btn-primary" href="ProductVariantsManagerServlet?action=AddOrEdit&id=<%= item.getId() %>">Sửa</a>
                    <a class="btn btn-sm btn-danger" href="ProductVariantsManagerServlet?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá không?')">Xoá</a>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="6" class="text-center text-muted">Không có biến thể nào.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</div>

</body>
</html>
