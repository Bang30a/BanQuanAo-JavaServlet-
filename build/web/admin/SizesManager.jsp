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
            background-color: #f1f3f5;
        }
        .form-container {
            max-width: 500px;
            margin: 50px auto;
            background-color: #ffffff;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }
        h2 {
            margin-bottom: 25px;
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
    <div class="form-container">
        <h2 class="text-center">Quản lý kích thước</h2>
        <form action="<%=request.getContextPath()%>/admin/SizesManagerServlet" method="post">
            <input type="hidden" name="action" value="<%= action %>">

            <div class="mb-3">
                <label class="form-label">ID</label>
                <input type="text" class="form-control" name="id" value="<%= size.getId() != 0 ? size.getId() : "" %>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên kích thước</label>
                <input type="text" class="form-control" name="sizeLabel" value="<%= size.getSizeLabel() != null ? size.getSizeLabel() : "" %>" required>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-success w-45">Lưu</button>
                <a href="View-sizes.jsp" class="btn btn-outline-secondary w-45">Xem danh sách</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
