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

    <!-- Bootstrap & CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/layout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>
</head>
<body>

<%@ include file="header.jsp" %>

<%
    String idParam = request.getParameter("id");
    Products product = null;
    List<ProductVariants> variants = new ArrayList(); // ❌ bỏ <>
    HashMap<Integer, String> sizeMap = new HashMap();  // ❌ bỏ <>
    List<Products> relatedProducts = new ArrayList();  // ❌ bỏ <>

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
                    sizeMap.put(new Integer(s.getId()), s.getSizeLabel());
                }

                // 🔹 Gợi ý sản phẩm cùng loại (VD: cùng categoryId)
                relatedProducts = dao.findByCategoryId(product.getCategoryId(), product.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String successMsg = (String) session.getAttribute("addCartSuccess");
    String errorMsg = (String) session.getAttribute("addCartError");
    session.removeAttribute("addCartSuccess");
    session.removeAttribute("addCartError");
%>

<div class="container mt-5">
    <% if (product != null) { %>
        <div class="row">
            <div class="col-md-6">
                <img src="<%= (product.getImage() != null ? product.getImage() : "images/default.jpg") %>"
                     class="img-fluid rounded"
                     alt="<%= product.getName() %>">
            </div>
            <div class="col-md-6">
                <h2><%= product.getName() %></h2>
                <p><strong>Mô tả:</strong> <%= product.getDescription() %></p>
                <p><strong>Giá:</strong> 
                    <span class="text-danger font-weight-bold">
                        <%= String.format("%,d", (int) product.getPrice()) %> VNĐ
                    </span>
                </p>

                <%-- ✅ Hiển thị thông báo --%>
                <% if (successMsg != null) { %>
                    <div class="alert alert-success"><%= successMsg %></div>
                <% } else if (errorMsg != null) { %>
                    <div class="alert alert-danger"><%= errorMsg %></div>
                <% } %>

                <form method="post" action="<%= request.getContextPath() %>/user/add-to-cart">
                    <input type="hidden" name="productId" value="<%= product.getId() %>">

                    <div class="form-group">
                        <label for="size">Chọn kích thước:</label>
                        <select name="variantId" id="size" class="form-control" required>
                            <% if (variants != null && !variants.isEmpty()) {
                                for (ProductVariants variant : variants) {
                                    String sizeLabel = (String) sizeMap.get(variant.getSizeId());
                                    if (sizeLabel == null) sizeLabel = "Không xác định";
                            %>
                                <option value="<%= variant.getId() %>">
                                    Size <%= sizeLabel %> — Còn <%= variant.getStock() %> sp
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

<button type="submit" class="btn-add-cart">
    <i class="bi bi-cart-plus-fill"></i> Thêm vào giỏ hàng
</button>

                </form>
            </div>
        </div>

        <hr class="my-5">

        <%-- 🔹 GỢI Ý SẢN PHẨM LIÊN QUAN --%>
        <% if (relatedProducts != null && !relatedProducts.isEmpty()) { %>
            <h4 class="text-center mb-4">Sản phẩm liên quan</h4>
            <div class="row">
                <% for (Products p : relatedProducts) { %>
                    <div class="col-md-3 col-sm-6 mb-4">
                        <div class="card shadow-sm h-100">
                            <img src="<%= (p.getImage() != null ? p.getImage() : "images/default.jpg") %>"
                                 class="card-img-top"
                                 alt="<%= p.getName() %>">
                            <div class="card-body text-center">
                                <h6><%= p.getName() %></h6>
                                <p class="text-danger mb-2"><%= String.format("%,d", (int)p.getPrice()) %> VNĐ</p>
                                <a href="<%= request.getContextPath() %>/user/product-detail.jsp?id=<%= p.getId() %>"
                                   class="btn btn-outline-primary btn-sm">Xem chi tiết</a>
                            </div>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>

    <% } else { %>
        <div class="alert alert-danger text-center mt-5">Không tìm thấy sản phẩm.</div>
    <% } %>
</div>

<%@ include file="footer.jsp" %>

</body>
</html>
