<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty product ? product.name : 'Chi tiết sản phẩm'} - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/assets/css/style.css">

    <style>
        /* ... (Giữ nguyên CSS cũ của bạn) ... */
        body { background-color: #f8fafc; font-family: 'Inter', sans-serif; color: #334155; display: flex; flex-direction: column; min-height: 100vh; }
        main { flex: 1; padding: 40px 0; }
        .product-detail-container { background: white; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); padding: 40px; margin-bottom: 50px; }
        .main-image-box { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; position: relative; padding-top: 100%; }
        .main-image-box img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: contain; transition: transform 0.3s; }
        .main-image-box:hover img { transform: scale(1.05); }
        .product-title { font-size: 2rem; font-weight: 700; color: #0f172a; margin-bottom: 15px; }
        .product-price { font-size: 1.75rem; color: #ef4444; font-weight: 700; margin-bottom: 20px; }
        .product-desc { font-size: 1rem; color: #64748b; line-height: 1.6; margin-bottom: 30px; border-bottom: 1px solid #f1f5f9; padding-bottom: 20px; }
        .form-label { font-weight: 600; color: #334155; }
        .form-select-custom { padding: 12px; border-radius: 8px; border: 1px solid #cbd5e1; cursor: pointer; }
        .form-select-custom:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); }
        .qty-input-group { width: 140px; border: 1px solid #cbd5e1; border-radius: 8px; overflow: hidden; }
        .btn-qty { background: #f8fafc; border: none; width: 40px; color: #334155; font-weight: 700; }
        .btn-qty:hover { background: #e2e8f0; }
        .input-qty { border: none; text-align: center; width: 60px; font-weight: 600; }
        .input-qty:focus { outline: none; }
        .btn-add-cart { background-color: #2563eb; color: white; font-weight: 600; padding: 14px 30px; border-radius: 8px; border: none; transition: 0.2s; width: 100%; text-transform: uppercase; letter-spacing: 0.5px; }
        .btn-add-cart:hover { background-color: #1d4ed8; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2); }
        .section-title { font-weight: 700; color: #1e293b; margin-bottom: 25px; text-transform: uppercase; font-size: 1.25rem; border-left: 5px solid #2563eb; padding-left: 15px; }
        .product-card { border: none; border-radius: 12px; overflow: hidden; transition: transform 0.3s, box-shadow 0.3s; background: white; height: 100%; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        .product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }
        .product-img-wrap { position: relative; padding-top: 100%; overflow: hidden; display: block; }
        .product-img-wrap img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; }
        .card-body { padding: 15px; text-align: center; }
        .card-title a { text-decoration: none; color: #334155; font-weight: 600; transition: 0.2s; }
        .card-title a:hover { color: #2563eb; }
        .card-price { color: #ef4444; font-weight: 700; }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container">
            
            <!-- Breadcrumb -->
            <nav aria-label="breadcrumb" class="mb-4">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="${context}/user/view-products" class="text-decoration-none">Trang chủ</a></li>
                    <li class="breadcrumb-item"><a href="${context}/user/search-products" class="text-decoration-none">Sản phẩm</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${not empty product ? product.name : 'Chi tiết'}</li>
                </ol>
            </nav>

            <!-- Alert Messages -->
            <c:if test="${not empty sessionScope.addCartSuccess}">
                <div class="alert alert-success alert-dismissible fade show mb-4 shadow-sm" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i> ${sessionScope.addCartSuccess}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="addCartSuccess" scope="session" />
            </c:if>
            <c:if test="${not empty sessionScope.addCartError}">
                <div class="alert alert-danger alert-dismissible fade show mb-4 shadow-sm" role="alert">
                    <i class="bi bi-exclamation-circle-fill me-2"></i> ${sessionScope.addCartError}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="addCartError" scope="session" />
            </c:if>

            <!-- MAIN PRODUCT AREA -->
            <c:choose>
                <c:when test="${not empty product}">
                    <div class="product-detail-container">
                        <div class="row">
                            <!-- LEFT: Image -->
                            <div class="col-md-5 mb-4 mb-md-0">
                                <div class="main-image-box">
                                    <c:choose>
                                        <c:when test="${not empty product.image && product.image.startsWith('http')}">
                                            <img src="${product.image}" alt="${product.name}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${context}/user/assets/images/products/${product.image}" 
                                                 onerror="this.src='https://placehold.co/500x500?text=No+Image'" alt="${product.name}">
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- RIGHT: Info & Form -->
                            <div class="col-md-7 ps-md-5">
                                <h1 class="product-title">${product.name}</h1>
                                <div class="product-price">
                                    <fmt:formatNumber value="${product.price}" type="number"/> đ
                                </div>
                                
                                <div class="product-desc">
                                    <label class="form-label text-uppercase small text-muted mb-2">Mô tả sản phẩm:</label>
                                    <p>${not empty product.description ? product.description : 'Đang cập nhật mô tả...'}</p>
                                </div>

                                <!-- ADD TO CART FORM -->
                                <form action="${context}/user/add-to-cart" method="post">
                                    <input type="hidden" name="productId" value="${product.id}">

                                    <!-- Chọn Size (CÓ XỬ LÝ LỖI) -->
                                    <div class="mb-4">
                                        <label for="variantSelect" class="form-label">Chọn kích thước (Size):</label>
                                        
                                        <!-- Dùng c:catch để tránh trang trắng nếu code Java bị lỗi -->
                                        <c:catch var="sizeError">
                                            <select name="variantId" id="variantSelect" class="form-select form-select-custom" required>
                                                <option value="" selected disabled>-- Vui lòng chọn size --</option>
                                                <c:choose>
                                                    <c:when test="${not empty variants}">
                                                        <c:forEach var="v" items="${variants}">
                                                            <option value="${v.id}" ${v.stock <= 0 ? 'disabled' : ''}>
                                                                Size ${sizeMap[v.sizeId]} 
                                                                (
                                                                <c:choose>
                                                                    <c:when test="${v.stock > 0}">Còn ${v.stock}</c:when>
                                                                    <c:otherwise>Hết hàng</c:otherwise>
                                                                </c:choose>
                                                                )
                                                            </option>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <option disabled>Đang cập nhật size...</option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </select>
                                        </c:catch>

                                        <!-- Hiển thị lỗi nếu có -->
                                        <c:if test="${not empty sizeError}">
                                            <div class="alert alert-danger mt-2 small">
                                                <strong>Lỗi hiển thị Size:</strong> ${sizeError} <br>
                                                <em>Vui lòng "Clean and Build" lại dự án để cập nhật code Java mới nhất.</em>
                                            </div>
                                        </c:if>
                                    </div>

                                    <!-- Chọn Số lượng -->
                                    <div class="mb-4">
                                        <label class="form-label">Số lượng:</label>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="input-group qty-input-group">
                                                <button class="btn btn-qty" type="button" onclick="updateQty(-1)">-</button>
                                                <input type="number" name="quantity" id="quantityInput" class="form-control input-qty" value="1" min="1" max="10">
                                                <button class="btn btn-qty" type="button" onclick="updateQty(1)">+</button>
                                            </div>
                                            <span class="text-muted small">(Tối đa 10 sp/đơn)</span>
                                        </div>
                                    </div>

                                    <!-- Button Add -->
                                    <button type="submit" class="btn-add-cart shadow-sm">
                                        <i class="bi bi-cart-plus me-2"></i> Thêm vào giỏ hàng
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- RELATED PRODUCTS -->
                    <c:if test="${not empty relatedProducts}">
                        <h3 class="section-title">Sản phẩm liên quan</h3>
                        <div class="row g-4">
                            <c:forEach var="rp" items="${relatedProducts}">
                                <c:if test="${rp.id != product.id}">
                                    <div class="col-6 col-md-3">
                                        <div class="product-card h-100">
                                            <a href="${context}/user/product-detail?id=${rp.id}" class="product-img-wrap">
                                                <img src="${not empty rp.image && rp.image.startsWith('http') ? rp.image : context + '/user/assets/images/products/' + rp.image}" 
                                                     onerror="this.src='https://placehold.co/300x300?text=ShopDuck'" alt="${rp.name}">
                                            </a>
                                            <div class="card-body">
                                                <div class="card-title text-truncate">
                                                    <a href="${context}/user/product-detail?id=${rp.id}">${rp.name}</a>
                                                </div>
                                                <div class="card-price">
                                                    <fmt:formatNumber value="${rp.price}" type="number"/> đ
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </c:if>
                </c:when>
                
                <c:otherwise>
                    <div class="text-center py-5">
                        <div class="display-1 text-muted mb-3"><i class="bi bi-search"></i></div>
                        <h3>Không tìm thấy sản phẩm!</h3>
                        <p class="text-muted">Sản phẩm này có thể đã bị xóa hoặc đường dẫn không đúng.</p>
                        <a href="${context}/user/search-products" class="btn btn-primary mt-3">
                            Xem tất cả sản phẩm
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </main>

    <!-- JS Xử lý số lượng -->
    <script>
        function updateQty(change) {
            const input = document.getElementById('quantityInput');
            let val = parseInt(input.value);
            val += change;
            if (val < 1) val = 1;
            if (val > 10) val = 10; // Giới hạn max
            input.value = val;
        }
    </script>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>