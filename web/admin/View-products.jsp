<%@page import="java.util.ArrayList"%>
<%@page import="dao.ProductDao"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="entity.Products" %>
<%@page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý sản phẩm</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f9fafb;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .container {
            background-color: #fff;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.05);
            margin-top: 40px;
        }

        h2 {
            color: #1f2937;
            margin-bottom: 20px;
            text-align: center;
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

        table img {
            border-radius: 6px;
            object-fit: cover;
        }

        .no-img {
            color: #9ca3af;
            font-style: italic;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>Danh sách sản phẩm</h2>

    <div class="mb-3 text-end">
        <a href="<%= request.getContextPath() %>/admin/ProductsManager?action=AddOrEdit" class="btn btn-success">+ Thêm sản phẩm</a>
    </div>

    <table class="table table-bordered table-hover align-middle text-center">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Tên</th>
                <th>Mô tả</th>
                <th>Giá</th>
                <th>Hình ảnh</th>
                <th>Hành động</th>
            </tr>
        </thead>
        <tbody>
            <%
                ProductDao dao = new ProductDao();
                List<Products> arrPro = dao.getAllProducts();

                if (arrPro != null && !arrPro.isEmpty()) {
                    for (Products item : arrPro) {
            %>
            <tr>
                <td><%= item.getId() %></td>
                <td><%= item.getName() %></td>
                <td><%= item.getDescription() %></td>
                <td><%= String.format("%,.0f", item.getPrice()) %> VNĐ</td>
                <td>
                    <% if (item.getImage() != null && !item.getImage().isEmpty()) { %>
                        <img src="images/<%= item.getImage() %>" alt="Ảnh" width="80" height="80">
                    <% } else { %>
                        <span class="no-img">Không có ảnh</span>
                    <% } %>
                </td>
                <td>
                   <a class="btn btn-sm btn-primary" href="<%= request.getContextPath() %>/admin/ProductsManager?action=AddOrEdit&id=<%= item.getId() %>">Sửa</a>
                   <a class="btn btn-sm btn-danger" href="<%= request.getContextPath() %>/admin/ProductsManager?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá sản phẩm này không?')">Xóa</a>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="6">Không có sản phẩm nào.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</div>
</body>
</html>
