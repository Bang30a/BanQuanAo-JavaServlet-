<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trang chủ - ShopDuck</title>
    
    <!-- 1. BOOTSTRAP 4 (BẮT BUỘC) -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    
    <!-- 2. ICON BOOTSTRAP (Cho giỏ hàng) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- 3. FONT AWESOME (BẮT BUỘC CHO ICON FOOTER) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    
    <!-- 4. CÁC FILE CSS CỦA BẠN (SỬ DỤNG CONTEXT PATH ĐÚNG) -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/layout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">
    
    <!-- 5. SCRIPT BOOTSTRAP 4 (BẮT BUỘC CHO DROPDOWN) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>

    <!-- Inline CSS để đảm bảo slideshow hoạt động ngay cả khi style ngoài chưa cập nhật -->
    <style>
        /* SLIDESHOW (hỗ trợ .slide div với background-image) */
        #slideshow {
            position: relative;
            height: 520px;
            overflow: hidden;
        }
        #slideshow .slide {
            position: absolute;
            inset: 0;
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            opacity: 0;
            transition: opacity 1.2s ease-in-out, transform 1.2s ease-in-out;
            transform: scale(1.02);
        }
        #slideshow .slide.active {
            opacity: 1;
            z-index: 2;
            transform: scale(1);
        }

        /* overlay gradient to make text readable */
        #slideshow::after {
            content: "";
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(0,0,0,0.15), rgba(0,0,0,0.6));
            z-index: 3;
            pointer-events: none;
        }

        .slide-text {
            position: absolute;
            z-index: 4;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: #fff;
            width: 100%;
            padding: 0 20px;
        }
        .slide-text h1 {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 10px;
            text-shadow: 0 6px 20px rgba(0,0,0,0.5);
        }
        .slide-text p {
            font-size: 1.15rem;
            margin-bottom: 18px;
            text-shadow: 0 4px 14px rgba(0,0,0,0.45);
        }
    .slide-btn {
    display: inline-block;
    padding: 12px 32px;
    background: #ffffff;
    color: #000000 !important; /* ÉP MÀU ĐEN KHÔNG BỊ KẾ THỪA */
    font-size: 18px;
    font-weight: 600;
    border-radius: 40px;
    border: none;
    text-decoration: none !important;
    transition: 0.3s ease;
}

.slide-btn:hover {
    background: #000000;
    color: #ffffff !important; /* Khi hover đổi sang trắng */
    text-decoration: none !important;
}

html {
  scroll-behavior: smooth;
}

        /* Responsive adjustments */
        @media (max-width: 768px) {
            #slideshow { height: 360px; }
            .slide-text h1 { font-size: 2rem; }
            .slide-text p { font-size: 1rem; }
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <main>
<section id="slideshow" class="mt-2">
    <!-- Đảm bảo ảnh đầu tiên có class active -->
    <div class="slide active" style="background-image: url('https://file.hstatic.net/1000304105/article/ao_khoac_jeans_nu_4033ef09b1cc45eb834dd6abc0789b70.jpg');"></div>
    <div class="slide" style="background-image: url('https://toplist.vn/images/800px/highway-menswear--1403561.jpg');"></div>

    <div class="slide-text">
        <h1>ShopDuck Fashion</h1>
        <p>Gu thời trang – Phong cách của riêng bạn</p>
<a href="#productList" class="slide-btn" style="text-decoration: none; color: inherit;">
    Khám phá ngay
</a>
    </div>
</section>

        <div class="container mt-4">
            <c:if test="${param.success == 'true'}">
                <div class="alert alert-success text-center">
                    ✅ Đặt hàng thành công! Cảm ơn bạn đã mua hàng.
                </div>
            </c:if>

        <h2 id="productList" class="mb-4">Danh sách sản phẩm</h2>
            <div class="row">
                <c:forEach var="p" items="${productList}">
                    <div class="col-md-3 mb-4">
                        <div class="card product-card h-100 shadow-sm">
                            <div class="position-relative">
                                <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}">
                                    
                                    <c:choose>
                                        <c:when test="${not empty p.image && (p.image.startsWith('http') || p.image.startsWith('user/uploads'))}">
                                            <c:choose>
                                                <c:when test="${p.image.startsWith('http')}">
                                                    <img src="${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="https://placehold.co/250x250/eee/ccc?text=No+Image" class="product-image card-img-top" alt="Ảnh bị lỗi">
                                        </c:otherwise>
                                    </c:choose>
                                    
                                    <div class="buy-overlay">Xem chi tiết</div>
                                </a>
                            </div>
                            <div class="card-body text-center">
                                <h5 class="card-title">
                                    <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}" style="text-decoration: none; color: inherit;">
                                        ${p.name}
                                    </a>
                                </h5>
                                <p class="card-text">${p.description}</p>
                                <p class="card-text text-danger font-weight-bold">
                                    <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" /> VND
                                </p>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <h2 class="mb-4">Sản phẩm - Áo</h2>
            <div class="row">
                <c:forEach var="p" items="${shirtList}">
                       <div class="col-md-3 mb-4">
                            <div class="card product-card h-100 shadow-sm">
                                <div class="position-relative">
                                    <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}">
                                        
                                        <c:choose>
                                            <c:when test="${not empty p.image && (p.image.startsWith('http') || p.image.startsWith('user/uploads'))}">
                                                <c:choose>
                                                    <c:when test="${p.image.startsWith('http')}">
                                                        <img src="${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="https://placehold.co/250x250/eee/ccc?text=No+Image" class="product-image card-img-top" alt="Ảnh bị lỗi">
                                            </c:otherwise>
                                        </c:choose>
                                        
                                        <div class="buy-overlay">Xem chi tiết</div>
                                    </a>
                                </div>
                                <div class="card-body text-center">
                                    <h5 class="card-title">
                                        <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}" style="text-decoration: none; color: inherit;">
                                            ${p.name}
                                        </a>
                                    </h5>
                                    <p class="card-text">${p.description}</p>
                                    <p class="card-text text-danger font-weight-bold">
                                        <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" /> VND
                                    </p>
                                </div>
                            </div>
                        </div>
                </c:forEach>
            </div>
            
            <h2 class="mb-4">Sản phẩm - Quần</h2>
            <div class="row">
                 <c:forEach var="p" items="${pantsList}">
                       <div class="col-md-3 mb-4">
                            <div class="card product-card h-100 shadow-sm">
                                <div class="position-relative">
                                    <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}">
                                        
                                        <c:choose>
                                            <c:when test="${not empty p.image && (p.image.startsWith('http') || p.image.startsWith('user/uploads'))}">
                                                <c:choose>
                                                    <c:when test="${p.image.startsWith('http')}">
                                                        <img src="${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/${p.image}" class="product-image card-img-top" alt="${p.name}">
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="https://placehold.co/250x250/eee/ccc?text=No+Image" class="product-image card-img-top" alt="Ảnh bị lỗi">
                                            </c:otherwise>
                                        </c:choose>
                                        
                                        <div class="buy-overlay">Xem chi tiết</div>
                                    </a>
                                </div>
                                <div class="card-body text-center">
                                    <h5 class="card-title">
                                        <a href="${pageContext.request.contextPath}/user/product-detail?id=${p.id}" style="text-decoration: none; color: inherit;">
                                            ${p.name}
                                        </a>
                                    </h5>
                                    <p class="card-text">${p.description}</p>
                                    <p class="card-text text-danger font-weight-bold">
                                        <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" /> VND
                                    </p>
                                </div>
                            </div>
                        </div>
                </c:forEach>
            </div>
        </div>
    </main>

    <!-- JS: Chạy slideshow sau khi DOM sẵn sàng -->
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const slides = document.querySelectorAll('#slideshow .slide');
        if (!slides || slides.length === 0) return;
        let index = 0;
        // ensure first has active
        slides.forEach((s, i) => { if (i === 0) s.classList.add('active'); else s.classList.remove('active'); });

        setInterval(() => {
            slides[index].classList.remove('active');
            index = (index + 1) % slides.length;
            slides[index].classList.add('active');
        }, 4500);
    });
    </script>

    <jsp:include page="footer.jsp" />
</body>
</html>
