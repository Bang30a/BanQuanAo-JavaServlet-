<%@page import="dao.UsersDao"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*, entity.Users"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Người dùng</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #eef2f6;
            margin: 0;
            padding: 35px 0;
            color: #333;
        }

        h2 {
            text-align: center;
            margin-bottom: 15px;
            font-weight: 600;
            color: #1e293b;
        }

        /* Wrapper */
        .container {
            width: 92%;
            margin: auto;
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.07);
        }

        /* Header actions */
        .top-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
        }

        .search-input {
            padding: 8px 14px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            width: 260px;
            transition: 0.2s;
        }
        .search-input:focus {
            border-color: #3b82f6;
            outline: none;
        }

        .btn {
            background-color: #3b82f6;
            color: white;
            text-decoration: none;
            padding: 8px 14px;
            border-radius: 8px;
            font-weight: 600;
            transition: 0.2s;
        }
        .btn:hover { background-color: #2563eb; }

        table {
            width: 100%;
            border-collapse: collapse;
            border-radius: 8px;
            overflow: hidden;
        }

        th {
            background-color: #f1f5f9;
            padding: 12px;
            color: #334155;
            font-weight: 600;
            border-bottom: 1px solid #e2e8f0;
        }

        td {
            padding: 12px;
            text-align: center;
            border-bottom: 1px solid #e2e8f0;
        }

        tr:hover {
            background-color: #f9fafb;
        }

        /* Badge Roles */
        .badge-admin {
            background: #dc2626;
            color: white;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
        }
        .badge-user {
            background: #3b82f6;
            color: white;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
        }

        /* Action buttons */
        .btn.edit {
            background-color: #14b8a6;
        }
        .btn.edit:hover {
            background-color: #0d9488;
        }

        .btn.delete {
            background-color: #ef4444;
        }
        .btn.delete:hover {
            background-color: #dc2626;
        }
    </style>
</head>
<body>

    <h2>Quản lý Người dùng</h2>

    <div class="container">

        <div class="top-actions">
            <input type="text" placeholder="🔍 Tìm kiếm người dùng..." class="search-input" id="searchInput">
            <a href="UsersManagerServlet?action=AddOrEdit" class="btn">+ Thêm người dùng</a>
        </div>

        <table id="userTable">
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
                <%
                    UsersDao dao = new UsersDao();
                    List<Users> arrPro = dao.getAllUsers();

                    if (arrPro != null && !arrPro.isEmpty()) {
                        for (Users item : arrPro) {
                %>
                <tr>
                    <td><%= item.getId() %></td>
                    <td><%= item.getUsername() %></td>
                    <td><%= item.getFullname() %></td>
                    <td><%= item.getEmail() %></td>
                    <td>
                        <% if("admin".equals(item.getRole())) { %>
                            <span class="badge-admin">Admin</span>
                        <% } else { %>
                            <span class="badge-user">User</span>
                        <% } %>
                    </td>
                    <td>
                        <a class="btn edit" href="UsersManagerServlet?action=AddOrEdit&id=<%= item.getId() %>">Sửa</a>
                        <a class="btn delete" href="UsersManagerServlet?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá người dùng này không?')">Xoá</a>
                    </td>
                </tr>
                <%      }
                    } else { %>
                <tr><td colspan="6">Không có người dùng nào.</td></tr>
                <% } %>
            </tbody>
        </table>

    </div>

    <!-- Search Script -->
    <script>
        document.getElementById("searchInput").addEventListener("keyup", function () {
            let filter = this.value.toLowerCase();
            document.querySelectorAll("#userTable tbody tr").forEach(row => {
                row.style.display = row.innerText.toLowerCase().includes(filter) ? "" : "none";
            });
        });
    </script>

</body>
</html>
