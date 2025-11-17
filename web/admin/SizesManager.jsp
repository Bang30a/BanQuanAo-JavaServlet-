<%@page import="entity.Size"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sizes Manager</title>

    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #eef2f7;
            font-family: 'Inter', sans-serif;
        }
        .form-wrapper {
            max-width: 520px;
            margin: 55px auto;
            background: #fff;
            padding: 35px 40px;
            border-radius: 18px;
            box-shadow: 0 6px 20px rgba(0,0,0,0.08);
            transition: 0.2s;
        }
        .form-wrapper:hover {
            transform: translateY(-3px);
        }

        .form-wrapper h2 {
            text-align: center;
            margin-bottom: 25px;
            font-weight: 600;
            color: #1e293b;
        }

        label {
            font-weight: 500;
            color: #334155;
        }

        input.form-control {
            height: 45px;
            border-radius: 10px;
        }

        .btn-custom-primary {
            background-color: #2563eb;
            border: none;
            padding: 10px 0;
            border-radius: 10px;
            width: 48%;
            font-weight: 500;
            color: white;
        }
        .btn-custom-primary:hover {
            background-color: #1d4ed8;
        }

        .btn-custom-outline {
            border: 1px solid #64748b;
            padding: 10px 0;
            width: 48%;
            border-radius: 10px;
            color: #475569;
            font-weight: 500;
        }
        .btn-custom-outline:hover {
            background-color: #e2e8f0;
        }
    </style>
</head>
<body>

<%
    String action = (String) request.getAttribute("ACTION");
    Size size = (Size) request.getAttribute("SIZE");
    if (action == null) action = "SaveOrUpdate";
    if (size == null) size = new Size();
%>

<div class="container">
    <div class="form-wrapper">

        <h2>Quản lý kích thước</h2>

        <form action="<%=request.getContextPath()%>/admin/SizesManagerServlet" method="post">
            <input type="hidden" name="action" value="<%= action %>">

            <div class="mb-3">
                <label>ID</label>
                <input type="text" class="form-control" name="id"
                       value="<%= size.getId() != 0 ? size.getId() : "" %>" readonly>
            </div>

            <div class="mb-4">
                <label>Tên kích thước</label>
                <input type="text" class="form-control" name="sizeLabel"
                       value="<%= size.getSizeLabel() != null ? size.getSizeLabel() : "" %>" required>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-custom-primary">💾 Lưu</button>
                <a href="View-sizes.jsp" class="btn btn-custom-outline">📃 Danh sách</a>
            </div>

        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
