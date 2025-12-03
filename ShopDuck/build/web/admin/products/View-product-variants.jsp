<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Biến thể</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .container-box {
            background: #ffffff;
            padding: 30px;
            border-radius: 16px;
            margin: 40px auto;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        h2 {
            font-weight: 700;
            margin-bottom: 25px;
            text-align: center;
            color: #0f172a;
        }

        /* Table Styling */
        .table thead th {
            background-color: #1e293b;
            color: #fff;
            font-weight: 600;
            border: none;
            padding: 12px;
        }
        .table tbody td {
            vertical-align: middle;
            padding: 12px;
        }
        
        /* Badges */
        .badge-size {
            font-size: 0.9rem;
            padding: 6px 12px;
            border-radius: 6px;
            background-color: #e2e8f0;
            color: #475569;
            font-weight: 600;
        }
        
        /* Stock Alert */
        .stock-low {
            color: #dc2626;
            font-weight: 700;
            background-color: #fee2e2;
            padding: 4px 8px;
            border-radius: 4px;
        }
        .stock-ok {
            color: #16a34a;
            font-weight: 600;
        }

        /* Action Buttons */
        .btn-action {
            padding: 5px 12px;
            border-radius: 6px;
            font-size: 0.85rem;
            text-decoration: none;
            font-weight: 500;
            display: inline-block;
            transition: 0.2s;
        }
        .btn-edit { background-color: #3b82f6; color: white; }
        .btn-edit:hover { background-color: #2563eb; color: white; }
        .btn-delete { background-color: #ef4444; color: white; }
        .btn-delete:hover { background-color: #dc2626; color: white; }
        .btn-add { background-color: #10b981; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; }
        .btn-add:hover { background-color: #059669; color: white; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container container-box">
        <h2>📦 Danh sách Biến thể Sản phẩm</h2>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <span class="text-muted">Quản lý các phiên bản (Size/Màu) của sản phẩm</span>
            <a href="${context}/admin/ProductVariantsManagerServlet?action=Create" class="btn-add text-decoration-none">
                + Thêm biến thể mới
            </a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover table-bordered text-center">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên sản phẩm</th>
                        <th>Size</th>
                        <th>Tồn kho</th>
                        <th>Giá bán</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requestScope.VARIANTS}">
                            <c:forEach var="item" items="${requestScope.VARIANTS}">
                                <tr>
                                    <td class="text-muted">#${item.id}</td>
                                    
                                    <td class="text-start fw-bold text-primary">
                                        ${item.productName}
                                    </td>
                                    
                                    <td>
                                        <span class="badge-size">${item.sizeName}</span>
                                    </td>
                                    
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.stock < 10}">
                                                <span class="stock-low">⚠️ ${item.stock}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="stock-ok">${item.stock}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <td class="text-end">
                                        <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true"/> đ
                                    </td>
                                    
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <a href="${context}/admin/ProductVariantsManagerServlet?action=Edit&id=${item.id}" 
                                               class="btn-action btn-edit" title="Sửa">
                                                ✏️
                                            </a>
                                            <a href="${context}/admin/ProductVariantsManagerServlet?action=Delete&id=${item.id}" 
                                               class="btn-action btn-delete" 
                                               onclick="return confirm('Bạn có chắc muốn xoá biến thể #${item.id} không?')"
                                               title="Xóa">
                                                🗑️
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <div style="font-size: 40px; margin-bottom: 10px;">📦</div>
                                    Chưa có biến thể nào được tạo.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>