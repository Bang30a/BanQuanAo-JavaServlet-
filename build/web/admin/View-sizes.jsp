<%@page import="dao.SizeDao"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*, entity.Size"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Size</title>
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

        .container {
            width: 60%;
            background: white;
            margin: auto;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.07);
        }

        .top-actions {
            display: flex;
            justify-content: space-between;
            margin-bottom: 18px;
        }

        .search-input {
            padding: 8px 14px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            width: 230px;
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
            border-radius: 10px;
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

        tr:hover { background-color: #f9fafb; }

        /* Badge Size Label */
        .badge-size {
            background: #3b82f6;
            color: white;
            padding: 5px 12px;
            border-radius: 8px;
            font-weight: 600;
        }

        /* Action Buttons */
        .btn.edit { background: #14b8a6; }
        .btn.edit:hover { background: #0d9488; }

        .btn.delete { background: #ef4444; }
        .btn.delete:hover { background: #dc2626; }
    </style>
</head>
<body>

<h2>Quản lý Size</h2>

<div class="container">

    <div class="top-actions">
        <input type="text" id="searchInput" class="search-input" placeholder="🔍 Tìm size...">
        <a href="SizesManagerServlet?action=AddOrEdit" class="btn">+ Thêm size mới</a>
    </div>

    <table id="sizeTable">
        <thead>
            <tr>
                <th>ID</th>
                <th>Nhãn Size</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <%
                SizeDao dao = new SizeDao();
                List<Size> arr = dao.getAllSizes();

                if (arr != null && !arr.isEmpty()) {
                    for (Size item : arr) {
            %>
            <tr>
                <td><%= item.getId() %></td>
                <td><span class="badge-size"><%= item.getSizeLabel() %></span></td>
                <td>
                    <a class="btn edit" href="SizesManagerServlet?action=AddOrEdit&id=<%= item.getId() %>">Sửa</a>
                    <a class="btn delete" href="SizesManagerServlet?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá size này không?')">Xoá</a>
                </td>
            </tr>
            <%      }
                } else { %>
            <tr><td colspan="3" style="color:#64748b; padding:18px;">Không có size nào.</td></tr>
            <% } %>
        </tbody>
    </table>

</div>

<script>
document.getElementById("searchInput").addEventListener("keyup", function () {
    let filter = this.value.toLowerCase();
    document.querySelectorAll("#sizeTable tbody tr").forEach(row => {
        row.style.display = row.innerText.toLowerCase().includes(filter) ? "" : "none";
    });
});
</script>

</body>
</html>
