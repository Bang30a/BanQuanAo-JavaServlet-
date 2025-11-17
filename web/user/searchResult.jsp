<%@ page import="java.util.List" %>
<%@ page import="entity.Products" %>
<%-- Thêm thư viện JSTL (bắt buộc) --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Kết quả tìm kiếm</title>

    <!-- 1. BOOTSTRAP 4 (BẮT BUỘC) -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    
    <!-- 2. ICON BOOTSTRAP (Cho giỏ hàng) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- 3. FONT AWESOME (BẮT BUỘC CHO ICON FOOTER) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    
    <!-- 4. CÁC FILE CSS CỦA BẠN -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/layout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">

    <!-- 5. SCRIPT BOOTSTRAP 4 (BẮT BUỘC CHO DROPDOWN) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>
</head>
<body>
    <%-- Dùng jsp:include (an toàn hơn) --%>
    <jsp:include page="header.jsp" />

    <main> <%-- Thêm thẻ main cho layout --%>
        <div class="container mt-4">
            <h2 class="text-center mb-4">Kết quả tìm kiếm cho: "${keyword}"</h2>
            <div class="row">
                <%-- Dùng JSTL (cách làm chuẩn) --%>
                <c:choose>
                    <c:when test="${not empty productList}">
                        <c:forEach var="p" items="${productList}">
                            <div class="col-md-4 mb-4">
                                <div class="card product-card h-100 shadow-sm"> <%-- Thêm class product-card --%>
                                    <div class="position-relative">
                                        <a href="<%= request.getContextPath() %>/user/product-detail?id=${p.id}">
                                            <img src="${p.image}" class="product-image card-img-top" alt="${p.name}">
                                            <div class="buy-overlay">Xem chi tiết</div>
                                        </a>
                                    </div>
                                    <div class="card-body text-center">
                                        <h5 class="card-title">
                                            <a href="<%= request.getContextPath() %>/user/product-detail?id=${p.id}" style="text-decoration: none; color: inherit;">
                                                ${p.name}
                                            </a>
                                        </h5>
                                        <p class="card-text text-danger font-weight-bold">
                                            <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" /> VNĐ
                                        </p>
                                        
                                        <%-- ĐÃ XÓA NÚT MÀU XANH Ở ĐÂY --%>
                                        
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="col-12">
                            <div class="alert alert-warning text-center" role="alert">
                                Không tìm thấy sản phẩm nào phù hợp!
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </main>
    
    <jsp:include page="footer.jsp" />
</body>
</html>