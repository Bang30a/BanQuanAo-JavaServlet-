<%@ page import="java.util.List" %>
<%@ page import="entity.Products" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Kết quả tìm kiếm</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
<jsp:include page="header.jsp" />

<div class="container mt-4">
    <h2 class="text-center mb-4">Kết quả tìm kiếm</h2>
    <div class="row">
        <%
            List<Products> productList = (List<Products>) request.getAttribute("productList");
            if (productList != null && !productList.isEmpty()) {
                for (Products p : productList) {
        %>
        <div class="col-md-4 mb-4">
            <div class="card h-100 shadow-sm">
                <img src="<%= (p.getImage() != null ? p.getImage() : "images/default.jpg") %>" class="card-img-top" alt="<%= p.getName() %>">
                <div class="card-body">
                    <h5 class="card-title"><%= p.getName() %></h5>
                    <p class="card-text text-danger font-weight-bold">
                        <%= String.format("%,d", (int) p.getPrice()) %> VNĐ
                    </p>
                    <a href="<%= request.getContextPath() %>/user/search-products?action=detail&id=<%= p.getId() %>" class="btn btn-primary">Xem chi tiết</a>
                </div>
            </div>
        </div>
        <%
                }
            } else {
        %>
        <div class="col-12">
            <div class="alert alert-warning text-center" role="alert">
                Không tìm thấy sản phẩm nào phù hợp!
            </div>
        </div>
        <% } %>
    </div>
</div>
<jsp:include page="footer.jsp" />

</body>
</html>
