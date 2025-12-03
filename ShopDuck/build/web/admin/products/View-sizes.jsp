<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Redirect nếu chưa có dữ liệu -->
<c:if test="${empty list}">
    <c:redirect url="/admin/SizesManagerServlet?action=List" />
</c:if>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Kích thước</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f1f5f9; font-family: 'Inter', sans-serif; color: #334155; }
        .container-box { background: #ffffff; padding: 35px; border-radius: 16px; margin: 50px auto; max-width: 800px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
        h2 { font-weight: 700; margin-bottom: 25px; text-align: center; color: #0f172a; }
        .search-input { border-radius: 8px; border: 1px solid #cbd5e1; padding: 8px 15px; width: 250px; transition: 0.2s; }
        .search-input:focus { border-color: #3b82f6; outline: none; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); }
        .table thead th { background-color: #1e293b; color: #fff; font-weight: 600; padding: 12px; vertical-align: middle; }
        .table tbody td { vertical-align: middle; padding: 12px; }
        .badge-size { display: inline-block; min-width: 40px; padding: 6px 12px; border-radius: 6px; background-color: #eff6ff; color: #2563eb; font-weight: 700; border: 1px solid #bfdbfe; }
        .btn-add { background-color: #10b981; color: white; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 600; text-decoration: none; }
        .btn-add:hover { background-color: #059669; color: white; }
        .btn-action { padding: 6px 12px; border-radius: 6px; font-size: 0.85rem; text-decoration: none; display: inline-block; transition: 0.2s; }
        .btn-edit { background-color: #3b82f6; color: white; }
        .btn-edit:hover { background-color: #2563eb; color: white; }
        .btn-delete { background-color: #ef4444; color: white; }
        .btn-delete:hover { background-color: #dc2626; color: white; }
    </style>
</head>
<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container container-box">
        <h2>📏 Danh sách Kích thước (Size)</h2>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <input type="text" id="searchInput" class="search-input" placeholder="🔍 Tìm nhanh size...">
            <a href="${context}/admin/SizesManagerServlet?action=AddOrEdit" class="btn-add">+ Thêm Size</a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover table-bordered text-center" id="sizeTable">
                <thead>
                    <tr>
                        <th width="100">ID</th>
                        <th>Tên Size</th>
                        <th width="150">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- TUYỆT ĐỐI KHÔNG ĐỂ COMMENT HTML VÀO GIỮA THẺ C:CHOOSE -->
                    <c:choose>
                        <c:when test="${not empty list}">
                            <c:forEach var="item" items="${list}">
                                <tr>
                                    <td class="text-muted fw-bold">#${item.id}</td>
                                    <td><span class="badge-size">${item.sizeLabel}</span></td>
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <a href="${context}/admin/SizesManagerServlet?action=AddOrEdit&id=${item.id}" class="btn-action btn-edit" title="Sửa">✏️</a>
                                            <a href="${context}/admin/SizesManagerServlet?action=Delete&id=${item.id}" class="btn-action btn-delete" onclick="return confirm('Xóa size ${item.sizeLabel}?')" title="Xóa">🗑️</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="3" class="text-center py-4 text-muted">Không có dữ liệu size.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        document.getElementById("searchInput").addEventListener("keyup", function () {
            let filter = this.value.toLowerCase();
            let rows = document.querySelectorAll("#sizeTable tbody tr");
            rows.forEach(row => {
                let text = row.cells[1].innerText.toLowerCase();
                row.style.display = text.includes(filter) ? "" : "none";
            });
        });
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>