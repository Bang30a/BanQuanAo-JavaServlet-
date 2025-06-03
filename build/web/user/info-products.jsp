<%@page import="java.util.ArrayList"%>
<%@page import="dao.ProductDao"%>
<%@page import="dao.ProductVariantDao"%>
<%@page import="dao.SizeDao"%>
<%@page import="entity.Products"%>
<%@page import="entity.ProductVariants"%>
<%@page import="entity.Size"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết sản phẩm</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>

<%@ include file="header.jsp" %>

<%
    String idParam = request.getParameter("id");
    Products product = null;
    List<ProductVariants> variants = new ArrayList<ProductVariants>();
    HashMap<Integer, String> sizeMap = new HashMap<Integer, String>();
    
    if (idParam != null) {
        try {
            int id = Integer.parseInt(idParam);
            ProductDao dao = new ProductDao();
            product = dao.findById(id);
            
            if (product != null) {
                ProductVariantDao variantDao = new ProductVariantDao();
                variants = variantDao.findByProductId(id);

                SizeDao sizeDao = new SizeDao();
                List<Size> sizes = sizeDao.getAllSizes();
                for (Size s : sizes) {
                    sizeMap.put(s.getId(), s.getSizeLabel());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
%>

<div class="container mt-5">
    <% if (product != null) { %>
        <div class="row">
            <div class="col-md-6">
                <img src="<%= (product.getImage() != null ? product.getImage() : "images/default.jpg") %>" class="img-fluid" alt="<%= product.getName() %>">
            </div>
            <div class="col-md-6">
                <h2><%= product.getName() %></h2>
                <p><strong>Mô tả:</strong> <%= product.getDescription() %></p>
                <p><strong>Giá:</strong> 
                    <span class="text-danger font-weight-bold">
                        <%= String.format("%,d", (int) product.getPrice()) %> VNĐ
                    </span>
                </p>
                <form method="post" action="<%= request.getContextPath() %>/user/add-to-cart">
                    <input type="hidden" name="action" value="AddCart">

                    <div class="form-group">
                        <label for="size">Chọn kích thước:</label>
                        <select name="variantId" id="size" class="form-control" required>
                            <% if (variants != null && !variants.isEmpty()) {
                                for (ProductVariants variant : variants) {
                                    String sizeLabel = sizeMap.getOrDefault(variant.getSizeId(), "Không xác định");
                            %>
                                <option value="<%= variant.getId() %>">
                                    Size: <%= sizeLabel %> - Còn lại: <%= variant.getStock() %> sản phẩm
                                </option>
                            <% }} else { %>
                                <option disabled>Không có size nào khả dụng</option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Số lượng:</label>
                        <input type="number" name="quantity" id="quantity" class="form-control" min="1" value="1" required>
                    </div>

                    <button type="submit" class="btn btn-success">Thêm vào giỏ hàng</button>
                </form>
            </div>
        </div>
    <% } else { %>
        <div class="alert alert-danger text-center mt-5">Không tìm thấy sản phẩm.</div>
    <% } %>
</div>

<%@ include file="footer.jsp" %>

</body>
</html>