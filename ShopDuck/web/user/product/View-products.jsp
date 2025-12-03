<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
    <!-- Lưu ý đường dẫn ../ để lùi ra khỏi thư mục product -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/assets/css/style.css">

    <style>
        body {
            background-color: #f8fafc;
            font-family: 'Inter', sans-serif;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        main { flex: 1; }

        /* HERO CAROUSEL */
        .hero-carousel {
            margin-bottom: 40px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
        }
        .carousel-item { height: 500px; background-color: #000; }
        .carousel-item img { height: 100%; object-fit: cover; opacity: 0.7; }
        .carousel-caption { bottom: 30%; z-index: 2; }
        
        .hero-title {
            font-size: 3.5rem; font-weight: 700; text-shadow: 0 2px 10px rgba(0,0,0,0.5); margin-bottom: 15px;
        }
        .btn-hero {
            padding: 12px 35px; font-weight: 600; border-radius: 30px;
            background-color: white; color: black; transition: 0.3s; text-transform: uppercase;
        }
        .btn-hero:hover {
            background-color: #2563eb; color: white; transform: translateY(-3px);
        }

        /* SECTION TITLE */
        .section-title {
            position: relative; margin-bottom: 30px; font-weight: 700; color: #1e293b;
            text-transform: uppercase; letter-spacing: 0.5px; padding-bottom: 10px;
        }
        .section-title::after {
            content: ''; position: absolute; left: 0; bottom: 0; width: 60px; height: 4px; background-color: #2563eb; border-radius: 2px;
        }

        /* PRODUCT CARD */
        .product-card {
            border: none; border-radius: 12px; overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s; background: white; height: 100%;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
        }
        .product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }

        .product-img-wrap {
            position: relative; padding-top: 100%; overflow: hidden; display: block;
        }
        .product-img-wrap img {
            position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s;
        }
        .product-card:hover .product-img-wrap img { transform: scale(1.08); }

        /* Nút xem chi tiết */
        .view-btn {
            position: absolute; bottom: -50px; left: 50%; transform: translateX(-50%);
            background: rgba(255, 255, 255, 0.95); color: #111; padding: 8px 20px;
            border-radius: 30px; font-weight: 600; font-size: 0.85rem;
            box-shadow: 0 4px 10px rgba(0,0,0,0.2); transition: bottom 0.3s;
            text-decoration: none; white-space: nowrap;
        }
        .product-card:hover .view-btn { bottom: 15px; }
        .view-btn:hover { background: #2563eb; color: white; }

        .card-body { padding: 15px; text-align: center; }
        .product-title {
            font-size: 1rem; font-weight: 600; color: #334155; margin-bottom: 8px;
            display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
            overflow: hidden; height: 48px; text-decoration: none; transition: color 0.2s;
        }
        .product-title:hover { color: #2563eb; }
        .product-price { font-size: 1.1rem; color: #ef4444; font-weight: 700; }

        @media (max-width: 768px) {
            .carousel-item { height: 300px; }
            .hero-title { font-size: 2rem; }
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <!-- Lùi ra 1 cấp (../) để vào includes -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <!-- HERO SLIDER -->
        <div id="heroCarousel" class="carousel slide hero-carousel" data-bs-ride="carousel">
            <div class="carousel-inner">
                <div class="carousel-item active">
                    <img src="https://file.hstatic.net/1000304105/article/ao_khoac_jeans_nu_4033ef09b1cc45eb834dd6abc0789b70.jpg" class="d-block w-100" alt="Banner 1">
                    <div class="carousel-caption d-none d-md-block">
                        <h2 class="hero-title">Phong cách tối giản</h2>
                        <a href="#productList" class="btn btn-hero">Mua ngay</a>
                    </div>
                </div>
                <div class="carousel-item">
                    <img src="https://toplist.vn/images/800px/highway-menswear--1403561.jpg" class="d-block w-100" alt="Banner 2">
                    <div class="carousel-caption d-none d-md-block">
                        <h2 class="hero-title">Streetwear Đậm Chất</h2>
                        <a href="#shirtList" class="btn btn-hero">Xem ngay</a>
                    </div>
                </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
                <span class="carousel-control-prev-icon"></span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
                <span class="carousel-control-next-icon"></span>
            </button>
        </div>

        <div class="container mb-5">
            
            <!-- THÔNG BÁO (Success) -->
            <c:if test="${param.success == 'true'}">
                <div class="alert alert-success alert-dismissible fade show text-center mb-5 shadow-sm" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i> 
                    <strong>Đặt hàng thành công!</strong> Cảm ơn bạn đã mua hàng.
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <!-- DANH SÁCH SẢN PHẨM -->
            <div id="productList">
                <h3 class="section-title">Sản phẩm nổi bật</h3>
                <div class="row g-4">
                    <c:forEach var="p" items="${productList}">
                        <div class="col-6 col-md-4 col-lg-3">
                            <div class="product-card">
                                <!-- [QUAN TRỌNG] Link trỏ đến Servlet /user/product-detail -->
                                <a href="${context}/user/product-detail?id=${p.id}" class="product-img-wrap">
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
                </div>
            </div>

            <!-- DANH MỤC ÁO -->
            <div id="shirtList" class="mt-5">
                <h3 class="section-title">Thời trang Áo</h3>
                <div class="row g-4">
                    <c:forEach var="p" items="${shirtList}">
                        <div class="col-6 col-md-4 col-lg-3">
                            <div class="product-card">
                                <a href="${context}/user/product-detail?id=${p.id}" class="product-img-wrap">
                                    <img src="${not empty p.image && p.image.startsWith('http') ? p.image : context + '/user/assets/images/products/' + p.image}" 
                                         onerror="this.src='https://placehold.co/300x300?text=Shirt'" alt="${p.name}">
                                    <span class="view-btn">Xem chi tiết</span>
                                </a>
                                <div class="card-body">
                                    <a href="${context}/user/product-detail?id=${p.id}" class="product-title">${p.name}</a>
                                    <div class="product-price">
                                        <fmt:formatNumber value="${p.price}" type="number" /> đ
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- DANH MỤC QUẦN -->
            <div id="pantsList" class="mt-5">
                <h3 class="section-title">Thời trang Quần</h3>
                <div class="row g-4">
                    <c:forEach var="p" items="${pantsList}">
                        <div class="col-6 col-md-4 col-lg-3">
                            <div class="product-card">
                                <a href="${context}/user/product-detail?id=${p.id}" class="product-img-wrap">
                                    <img src="${not empty p.image && p.image.startsWith('http') ? p.image : context + '/user/assets/images/products/' + p.image}" 
                                         onerror="this.src='https://placehold.co/300x300?text=Pants'" alt="${p.name}">
                                    <span class="view-btn">Xem chi tiết</span>
                                </a>
                                <div class="card-body">
                                    <a href="${context}/user/product-detail?id=${p.id}" class="product-title">${p.name}</a>
                                    <div class="product-price">
                                        <fmt:formatNumber value="${p.price}" type="number" /> đ
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>