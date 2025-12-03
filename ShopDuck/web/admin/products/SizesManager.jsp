<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Kích thước</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-wrapper {
            max-width: 500px; /* Form nhỏ gọn */
            margin: 60px auto;
            background: #ffffff;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        }

        h2 {
            text-align: center;
            margin-bottom: 30px;
            font-weight: 700;
            color: #0f172a;
        }

        label {
            font-weight: 600;
            color: #475569;
            margin-bottom: 8px;
        }

        .form-control {
            height: 45px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            padding: 10px 15px;
        }
        .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }

        .btn-custom {
            padding: 10px 0;
            border-radius: 8px;
            font-weight: 600;
            transition: 0.2s;
        }
        
        /* Input Readonly style */
        .bg-readonly {
            background-color: #f8fafc;
            color: #94a3b8;
            cursor: not-allowed;
        }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <c:set var="size" value="${requestScope.SIZE}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-wrapper">

            <h2>
                <c:choose>
                    <c:when test="${not empty size.id && size.id != 0}">🛠️ Cập nhật Size</c:when>
                    <c:otherwise>✨ Thêm Size mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/SizesManagerServlet" method="post">
                <input type="hidden" name="action" value="${action}">

                <div class="mb-3">
                    <label>Mã kích thước (ID)</label>
                    <input type="text" class="form-control bg-readonly" name="id"
                           value="${(not empty size.id && size.id != 0) ? size.id : ''}" 
                           readonly placeholder="Tự động tạo">
                </div>

                <div class="mb-4">
                    <label>Tên kích thước (Label) <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="sizeLabel"
                           value="${size.sizeLabel}" 
                           placeholder="VD: S, M, L, XL, 39, 40..." required>
                </div>

                <div class="d-flex justify-content-between gap-3">
                    <a href="${context}/admin/SizesManagerServlet?action=List" class="btn btn-outline-secondary btn-custom w-50 text-center text-decoration-none">
                        Quay lại
                    </a>
                    
                    <button type="submit" class="btn btn-primary btn-custom w-50">
                        Lưu thông tin
                    </button>
                </div>

            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>