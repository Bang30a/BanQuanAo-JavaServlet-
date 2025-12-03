<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Sản phẩm</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-container {
            max-width: 800px;
            margin: 50px auto;
            padding: 40px;
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        h2 {
            font-weight: 700;
            margin-bottom: 30px;
            text-align: center;
            color: #0f172a;
        }
        .form-label {
            font-weight: 600;
            color: #475569;
        }
        .form-control {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #cbd5e1;
        }
        .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }
        
        /* Image Preview Box */
        .img-preview-box {
            width: 100%;
            height: 250px;
            border: 2px dashed #cbd5e1;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f8fafc;
            overflow: hidden;
            position: relative;
        }
        .img-preview-box img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        }
        .placeholder-text {
            color: #94a3b8;
            font-size: 0.9rem;
        }
        
        .btn-custom {
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 600;
        }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <c:set var="p" value="${requestScope.PRODUCTS}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-container">
            <h2>
                <c:choose>
                    <c:when test="${not empty p.id && p.id != 0}">🛠️ Cập nhật sản phẩm</c:when>
                    <c:otherwise>✨ Thêm sản phẩm mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/ProductsManager" method="post">
                <input type="hidden" name="action" value="${action}">

                <div class="row">
                    <div class="col-md-7">
                        <div class="mb-3">
                            <label class="form-label">ID Sản phẩm</label>
                            <input type="text" class="form-control" name="id" 
                                   value="${(not empty p.id && p.id != 0) ? p.id : ''}" 
                                   readonly placeholder="Tự động tạo (Auto Increment)" 
                                   style="background-color: #f1f5f9; color: #64748b;">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Tên sản phẩm <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="name" 
                                   value="${p.name}" placeholder="Nhập tên sản phẩm..." required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Giá bán (VNĐ) <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" name="price" 
                                   value="${p.price}" min="0" step="1000" placeholder="0" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Mô tả chi tiết</label>
                            <textarea class="form-control" name="description" rows="5" 
                                      placeholder="Nhập mô tả về chất liệu, kiểu dáng...">${p.description}</textarea>
                        </div>
                    </div>

                    <div class="col-md-5">
                        <div class="mb-3">
                            <label class="form-label">Link ảnh (URL)</label>
                            <input type="text" class="form-control" name="image" id="imgInput"
                                   value="${p.image}" placeholder="https://..." oninput="previewImage()">
                        </div>
                        
                        <label class="form-label">Xem trước ảnh:</label>
                        <div class="img-preview-box">
                            <img id="imgPreview" src="${not empty p.image ? p.image : ''}" 
                                 style="display: ${not empty p.image ? 'block' : 'none'};" 
                                 onerror="this.style.display='none'; document.querySelector('.placeholder-text').style.display='block';">
                            <span class="placeholder-text" style="display: ${not empty p.image ? 'none' : 'block'};">
                                Chưa có ảnh
                            </span>
                        </div>
                    </div>
                </div>

                <div class="d-flex justify-content-center gap-3 mt-4 pt-3 border-top">
                    <a href="${context}/admin/ProductsManager?action=List" class="btn btn-outline-secondary btn-custom">
                        Hủy / Xem danh sách
                    </a>
                    
                    <button type="submit" class="btn btn-primary btn-custom px-5">
                        Lưu Sản Phẩm
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function previewImage() {
            const input = document.getElementById('imgInput');
            const preview = document.getElementById('imgPreview');
            const placeholder = document.querySelector('.placeholder-text');
            
            const url = input.value.trim();

            if (url) {
                preview.src = url;
                preview.style.display = 'block';
                placeholder.style.display = 'none';
            } else {
                preview.src = '';
                preview.style.display = 'none';
                placeholder.style.display = 'block';
            }
        }
    </script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>