<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả tìm kiếm: ${param.keyword} - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/assets/css/style.css">

    <style>
        body {
            background-color: #f8fafc;
            font-family: 'Inter', sans-serif;
            color: #334155;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        main { flex: 1; padding: 40px 0; }

        /* Product Card Styles (Đồng bộ với index.jsp) */
        .product-card {
            border: none;
            border-radius: 12px;
            overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s;
            background: white;
            height: 100%;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
        }
        .product-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }

        .product-img-wrap {
            position: relative;
            padding-top: 100%;
            overflow: hidden;
            display: block;
        }
        
        .product-img-wrap img {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s;
        }
        
        .product-card:hover .product-img-wrap img {
            transform: scale(1.08);
        }

        .view-btn {
            position: absolute;
            bottom: -50px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(255, 255, 255, 0.95);
            color: #111;
            padding: 8px 20px;
            border-radius: 30px;
            font-weight: 600;
            font-size: 0.85rem;
            box-shadow: 0 4px 10px rgba(0,0,0,0.2);
            transition: bottom 0.3s;
            text-decoration: none;
            white-space: nowrap;
        }
        .product-card:hover .view-btn {
            bottom: 15px;
        }
        .view-btn:hover {
            background: #2563eb;
            color: white;
        }

        .card-body {
            padding: 15px;
            text-align: center;
        }
        
        .product-title {
            font-size: 1rem;
            font-weight: 600;
            color: #334155;
            margin-bottom: 8px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            height: 48px;
            text-decoration: none;
            transition: color 0.2s;
        }
        .product-title:hover { color: #2563eb; }

        .product-price {
            font-size: 1.1rem;
            color: #ef4444;
            font-weight: 700;
        }

        /* Result Header */
        .search-header {
            margin-bottom: 30px;
            text-align: center;
        }
        .keyword-highlight {
            color: #2563eb;
            font-style: italic;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container">
            
            <!-- Tiêu đề kết quả -->
            <div class="search-header">
                <h2 class="fw-bold text-dark">Kết quả tìm kiếm</h2>
                <p class="text-muted">
                    Từ khóa: <span class="keyword-highlight">"${param.keyword}"</span>
                    <c:if test="${not empty productList}">
                        - Tìm thấy <strong>${productList.size()}</strong> sản phẩm
                    </c:if>
                </p>
            </div>

            <!-- Danh sách sản phẩm -->
            <div class="row g-4">
                <c:choose>
                    <c:when test="${not empty productList}">
                        <c:forEach var="p" items="${productList}">
                            <div class="col-6 col-md-4 col-lg-3">
                                <div class="product-card">
                                    <!-- Link chi tiết [ĐÃ SỬA: Trỏ về Servlet thay vì JSP] -->
                                    <a href="${context}/user/product-detail?id=${p.id}" class="product-img-wrap">
                                        <!-- Logic hiển thị ảnh thông minh -->
                                        <c:choose>
                                            <c:when test="${not empty p.image && p.image.startsWith('http')}">
                                                <img src="${p.image}" alt="${p.name}">
                                            </c:when>
                                            <c:otherwise>
                                                <!-- Ảnh nội bộ -->
                                                <img src="${context}/user/assets/images/products/${p.image}" 
                                                     onerror="this.src='https://placehold.co/300x300?text=No+Img'" alt="${p.name}">
                                            </c:otherwise>
                                        </c:choose>
                                        
                                        <span class="view-btn">Xem chi tiết</span>
                                    </a>

                                    <div class="card-body">
                                        <!-- Link tiêu đề [ĐÃ SỬA] -->
                                        <a href="${context}/user/product-detail?id=${p.id}" class="product-title" title="${p.name}">
                                            ${p.name}
                                        </a>
                                        <div class="product-price">
                                            <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" /> đ
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    
                    <c:otherwise>
                        <!-- Không tìm thấy -->
                        <div class="col-12 text-center py-5">
                            <div style="font-size: 4rem; margin-bottom: 15px; opacity: 0.3;">🔍</div>
                            <h4 class="text-muted">Không tìm thấy sản phẩm nào phù hợp</h4>
                            <p class="text-secondary mb-4">Hãy thử tìm kiếm với từ khóa khác hoặc xem các danh mục sản phẩm.</p>
                            <a href="${context}/index.jsp" class="btn btn-primary px-4 py-2 rounded-pill">
                                Quay về trang chủ
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>