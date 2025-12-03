<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${empty USERS}">
    <c:redirect url="/admin/UsersManagerServlet?action=List" />
</c:if>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Người dùng</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f1f5f9; color: #334155; padding: 30px; }
        .container-box { background: white; padding: 30px; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); max-width: 1000px; margin: auto; }
        h2 { font-weight: 700; color: #0f172a; }
        .table thead th { background-color: #1e293b; color: #fff; font-weight: 600; padding: 12px; vertical-align: middle; }
        .table tbody td { vertical-align: middle; padding: 12px; font-size: 0.95rem; }
        .table-hover tbody tr:hover { background-color: #f8fafc; }
        .badge-role { font-size: 0.8rem; padding: 6px 12px; border-radius: 6px; font-weight: 600; }
        .role-admin { background-color: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
        .role-user { background-color: #dbeafe; color: #2563eb; border: 1px solid #bfdbfe; }
        .btn-action { padding: 6px 12px; border-radius: 6px; font-size: 0.85rem; text-decoration: none; display: inline-block; transition: 0.2s; font-weight: 500; }
        .btn-edit { background-color: #3b82f6; color: white; }
        .btn-edit:hover { background-color: #2563eb; color: white; }
        .btn-delete { background-color: #ef4444; color: white; }
        .btn-delete:hover { background-color: #dc2626; color: white; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container-box">
        <h2 class="mb-4 text-center">👥 Danh sách Người dùng</h2>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div class="input-group" style="max-width: 300px;">
                <input type="text" placeholder="🔍 Tìm tên hoặc email..." class="form-control" id="searchInput">
            </div>
            
            <a href="${context}/admin/UsersManagerServlet?action=AddOrEdit" class="btn btn-primary">
                + Thêm người dùng
            </a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle text-center" id="userTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên đăng nhập</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Quyền</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty USERS}">
                            <c:forEach var="u" items="${USERS}">
                                <tr>
                                    <td class="text-muted fw-bold">#${u.id}</td>
                                    <td class="fw-bold text-dark">${u.username}</td>
                                    <td class="text-start">${u.fullname}</td>
                                    <td class="text-start text-muted small">${u.email}</td>
                                    
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.role == 'admin'}">
                                                <span class="badge-role role-admin">Admin</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-role role-user">User</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <a href="${context}/admin/UsersManagerServlet?action=AddOrEdit&id=${u.id}" 
                                               class="btn-action btn-edit" title="Sửa">
                                                ✏️
                                            </a>
                                            
                                            <a href="${context}/admin/UsersManagerServlet?action=Delete&id=${u.id}" 
                                               class="btn-action btn-delete" 
                                               onclick="return confirm('Bạn có chắc muốn xoá user ${u.username} không?')"
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
                                    <div style="font-size: 3rem; margin-bottom: 10px;">👥</div>
                                    Chưa có người dùng nào.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
    
    <script>
        document.getElementById("searchInput").addEventListener("keyup", function () {
            let filter = this.value.toLowerCase();
            let rows = document.querySelectorAll("#userTable tbody tr");
            
            rows.forEach(row => {
                let text = row.innerText.toLowerCase();
                row.style.display = text.includes(filter) ? "" : "none";
            });
        });
    </script>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>