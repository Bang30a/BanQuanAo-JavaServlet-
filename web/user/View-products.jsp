<%@page import="dao.ProductDao"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="entity.Products"%>
<%@page import="control.user.ViewProductServlet"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="style.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <meta charset="UTF-8">
    <title>Trang chủ</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>
</head>
<body>
<%@ include file="header.jsp" %>

<section id="slideshow">
    <img src="https://file.hstatic.net/1000304105/article/ao_khoac_jeans_nu_4033ef09b1cc45eb834dd6abc0789b70.jpg" class="active" alt="Ảnh 1">
    <img src="https://toplist.vn/images/800px/highway-menswear--1403561.jpg" alt="Pelagos Slide 2">
</section>

<div class="container mt-4">
    <%
    String success = request.getParameter("success");
    if ("true".equals(success)) {
    %>
        <div class="alert alert-success text-center">
            ✅ Đặt hàng thành công! Cảm ơn bạn đã mua hàng.
        </div>
    <%
    }
    %>

    <h2 class="mb-4">Danh sách sản phẩm</h2>

    <div class="row">
        <%
            ProductDao dao = new ProductDao();
            List<Products> productList = dao.getAllProducts();
            int maxDisplay = Math.min(productList.size(), 8);
            for (int i = 0; i < maxDisplay; i++) {
                Products p = productList.get(i);
        %>
        <div class="col-md-3 mb-4">
            <div class="card product-card h-100 shadow-sm">
                <div class="position-relative">
                    <a href="info-products.jsp?id=<%= p.getId() %>">
                        <img src="<%= p.getImage() %>" class="product-image card-img-top" alt="Product Image">
                        <div class="buy-overlay">Xem chi tiết</div>
                    </a>
                </div>
                <div class="card-body text-center">
                    <h5 class="card-title">
                        <a href="info-products.jsp?id=<%= p.getId() %>" style="text-decoration: none; color: inherit;">
                            <%= p.getName() %>
                        </a>
                    </h5>
                    <p class="card-text"><%= p.getDescription() %></p>
                    <p class="card-text text-danger font-weight-bold"><%= p.getPrice() %> VND</p>
                </div>
            </div>
        </div>
        <% } %>
    </div>

    <h2 class="mb-4">Sản phẩm - Áo</h2>
    <div class="row">
        <%
            List<Products> shirtList = dao.getProductsByKeyword("áo");
            int maxShirt = Math.min(shirtList.size(), 6);
            for (int i = 0; i < maxShirt; i++) {
                Products p = shirtList.get(i);
        %>
        <div class="col-md-3 mb-4">
            <div class="card product-card h-100 shadow-sm">
                <div class="position-relative">
                    <a href="info-products.jsp?id=<%= p.getId() %>">
                        <img src="<%= p.getImage() %>" class="product-image card-img-top" alt="Product Image">
                        <div class="buy-overlay">Xem chi tiết</div>
                    </a>
                </div>
                <div class="card-body text-center">
                    <h5 class="card-title">
                        <a href="info-products.jsp?id=<%= p.getId() %>" style="text-decoration: none; color: inherit;">
                            <%= p.getName() %>
                        </a>
                    </h5>
                    <p class="card-text"><%= p.getDescription() %></p>
                    <p class="card-text text-danger font-weight-bold"><%= p.getPrice() %> VND</p>
                </div>
            </div>
        </div>
        <% } %>
    </div>
</div>
    
    <h2 class="mb-4">Sản phẩm - Quần</h2>
    <div class="row">
        <%
            List<Products> pantsList = dao.getProductsByKeyword("quần");
            int maxPants = Math.min(pantsList.size(), 6);
            for (int i = 0; i < maxPants; i++) {
                Products p = pantsList.get(i);
        %>
        <div class="col-md-3 mb-4">
            <div class="card product-card h-100 shadow-sm">
                <div class="position-relative">
                    <a href="info-products.jsp?id=<%= p.getId() %>">
                        <img src="<%= p.getImage() %>" class="product-image card-img-top" alt="Product Image">
                        <div class="buy-overlay">Xem chi tiết</div>
                    </a>
                </div>
                <div class="card-body text-center">
                    <h5 class="card-title">
                        <a href="info-products.jsp?id=<%= p.getId() %>" style="text-decoration: none; color: inherit;">
                            <%= p.getName() %>
                        </a>
                    </h5>
                    <p class="card-text"><%= p.getDescription() %></p>
                    <p class="card-text text-danger font-weight-bold"><%= p.getPrice() %> VND</p>
                </div>
            </div>
        </div>
        <% } %>
    </div>

<script>
const slides = document.querySelectorAll('#slideshow img');
let index = 0;
setInterval(() => {
  slides[index].classList.remove('active');
  index = (index + 1) % slides.length;
  slides[index].classList.add('active');
}, 4000);
</script>

<%@ include file="footer.jsp" %>
</body>
</html>
