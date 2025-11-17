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
        background-color: #f1f5f9;
        font-family: 'Inter', sans-serif;
    }

    .container {
        background-color: #ffffff;
        padding: 35px;
        border-radius: 14px;
        box-shadow: 0 6px 25px rgba(0,0,0,0.06);
        margin-top: 50px;
        animation: fadeIn .4s ease;
    }

    @keyframes fadeIn {
        from {opacity: 0; transform: translateY(10px);}
        to {opacity: 1; transform: translateY(0);}
    }

    h2 {
        color: #0f172a;
        font-weight: 700;
        margin-bottom: 25px;
        text-align: center;
        letter-spacing: .5px;
    }

    .search-bar input {
        border-radius: 8px;
        border: 1px solid #cbd5e1;
        padding: 8px 12px;
        width: 250px;
        outline: none;
        transition: .25s;
    }

    .search-bar input:focus {
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59,130,246,0.2);
    }

    .btn-success {
        background-color: #10b981;
        border: none;
        border-radius: 6px;
        font-weight: 600;
    }

    .btn-success:hover {
        background-color: #059669;
    }

    .btn-primary {
        background-color: #3b82f6;
        border: none;
        border-radius: 6px;
        font-weight: 600;
    }

    .btn-primary:hover {
        background-color: #2563eb;
    }

    .btn-danger {
        background-color: #ef4444;
        border: none;
        border-radius: 6px;
        font-weight: 600;
    }

    .btn-danger:hover {
        background-color: #dc2626;
    }

    table {
        border-radius: 10px !important;
        overflow: hidden;
    }

    thead {
        background-color: #1e293b !important;
        color: #fff;
    }

    tbody tr:hover {
        background-color: #f1f5f9 !important;
    }

    table img {
        border-radius: 8px;
        width: 75px;
        height: 75px;
        object-fit: cover;
        border: 1px solid #e2e8f0;
        transition: .25s;
    }

    table img:hover {
        transform: scale(1.1);
    }

    .no-img {
        color: #94a3b8;
        font-style: italic;
    }
</style>

</head>
<body>
<div class="container">
    <h2>Danh sách sản phẩm</h2>

    <div class="d-flex justify-content-between align-items-center mb-3">
    <form method="get" class="search-bar">
        <input type="text" name="keyword" placeholder="Tìm sản phẩm..." value="<%= request.getParameter("keyword") != null ? request.getParameter("keyword") : "" %>">
    </form>

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
                String keyword = request.getParameter("keyword");
                List<Products> arrPro;

                if (keyword != null && !keyword.trim().isEmpty()) {
                    arrPro = dao.searchByKeyword(keyword.trim());
                } else {
                    arrPro = dao.getAllProducts();
                }

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
