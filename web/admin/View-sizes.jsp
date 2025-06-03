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
            background-color: #f5f7fa;
            margin: 0;
            padding: 0;
            color: #333;
        }

        h2 {
            text-align: center;
            margin-top: 30px;
            color: #1e293b;
        }

        .center {
            text-align: center;
            margin-bottom: 20px;
        }

        .btn {
            display: inline-block;
            padding: 8px 16px;
            margin: 10px 5px;
            background-color: #3b82f6;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            transition: background-color 0.3s ease;
        }

        .btn:hover {
            background-color: #2563eb;
        }

        .btn.delete {
            background-color: #ef4444;
        }

        .btn.delete:hover {
            background-color: #dc2626;
        }

        table {
            width: 60%;
            margin: 0 auto 40px;
            border-collapse: collapse;
            background-color: white;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            border-radius: 8px;
            overflow: hidden;
        }

        th, td {
            padding: 12px 16px;
            text-align: center;
            border-bottom: 1px solid #e5e7eb;
        }

        th {
            background-color: #f1f5f9;
            color: #334155;
            font-weight: 600;
        }

        tr:hover {
            background-color: #f9fafb;
        }

        .no-size {
            text-align: center;
            padding: 20px;
            color: #64748b;
        }
    </style>
</head>
<body>

    <h2>Danh sách Size</h2>

    <div class="center">
        <a href="SizesManagerServlet?action=AddOrEdit" class="btn">+ Thêm size mới</a>
    </div>

    <table>
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
                List<Size> arrPro = dao.getAllSizes();

                if (arrPro != null && !arrPro.isEmpty()) {
                    for (Size item : arrPro) {
            %>
            <tr>
                <td><%= item.getId() %></td>
                <td><%= item.getSizeLabel() %></td>
                <td>
                    <a class="btn" href="SizesManagerServlet?action=AddOrEdit&id=<%= item.getId() %>">Sửa</a>
                    <a class="btn delete" href="SizesManagerServlet?action=Delete&id=<%= item.getId() %>" onclick="return confirm('Bạn có chắc muốn xoá size này không?')">Xoá</a>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="3" class="no-size">Không có size nào.</td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>

</body>
</html>
