<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- [QUAN TRỌNG] Redirect nếu chưa có dữ liệu -->
<c:if test="${empty PRODUCTS}">
    <c:redirect url="/admin/ProductsManagerServlet?action=List" />
</c:if>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Sản phẩm</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body { background-color: #f1f5f9; font-family: 'Inter', sans-serif; color: #334155; }
        .container-box { background: #ffffff; padding: 30px; border-radius: 16px; margin: 40px auto; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
        h2 { font-weight: 700; color: #0f172a; margin-bottom: 25px; text-align: center; }
        .search-box { position: relative; max-width: 300px; }
        .search-input { padding-left: 15px; border-radius: 8px; border: 1px solid #cbd5e1; }
        .product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid #e2e8f0; transition: transform 0.2s; }
        .product-img:hover { transform: scale(1.1); }
        .desc-text { font-size: 0.9rem; color: #64748b; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: inline-block; }
        .btn-add { background-color: #10b981; color: white; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 600; text-decoration: none; }
        .btn-add:hover { background-color: #059669; color: white; }
        .btn-action { padding: 6px 12px; border-radius: 6px; font-size: 0.85rem; text-decoration: none; display: inline-block; transition: 0.2s; }
        .btn-edit { background-color: #3b82f6; color: white; }
        .btn-edit:hover { background-color: #2563eb; color: white; }
        .btn-delete { background-color: #ef4444; color: white; }
        .btn-delete:hover { background-color: #dc2626; color: white; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container container-box">
        <h2>🛍️ Danh sách Sản phẩm</h2>

        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
            
            <!-- Form tìm kiếm -->
            <form action="${context}/admin/ProductsManagerServlet" method="get" class="d-flex gap-2">
                <input type="hidden" name="action" value="List"> 
                <div class="search-box">
                    <input type="text" name="keyword" class="form-control search-input" 
                           placeholder="Tìm tên sản phẩm..." 
                           value="${param.keyword}">
                </div>
                <button type="submit" class="btn btn-primary" style="border-radius: 8px;">🔍</button>
            </form>

            <a href="${context}/admin/ProductsManagerServlet?action=AddOrEdit" class="btn-add">
                + Thêm sản phẩm
            </a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover table-bordered text-center align-middle">
                <thead class="table-dark">
                    <tr>
                        <th width="50">ID</th>
                        <th width="80">Hình ảnh</th>
                        <th>Tên sản phẩm</th>
                        <th>Mô tả</th>
                        <th>Giá bán</th>
                        <th width="120">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty PRODUCTS}">
                            <c:forEach var="p" items="${PRODUCTS}">
                                <tr>
                                    <td class="text-muted fw-bold">#${p.id}</td>
                                    
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty p.image}">
                                                <img src="${p.image}" class="product-img" 
                                                     onerror="this.onerror=null; this.src='https://placehold.co/60x60?text=No+Img';">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="https://placehold.co/60x60?text=No+Img" class="product-img" style="opacity: 0.6;">
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <td class="text-start fw-bold text-dark">${p.name}</td>
                                    
                                    <td class="text-start">
                                        <span class="desc-text" title="${p.description}">
                                            ${not empty p.description ? p.description : '<i class="text-muted">Không có mô tả</i>'}
                                        </span>
                                    </td>
                                    
                                    <td class="text-end fw-bold text-primary">
                                        <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true"/> đ
                                    </td>
                                    
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <a href="${context}/admin/ProductsManagerServlet?action=AddOrEdit&id=${p.id}" 
                                               class="btn-action btn-edit" title="Sửa">✏️</a>
                                            <a href="${context}/admin/ProductsManagerServlet?action=Delete&id=${p.id}" 
                                               class="btn-action btn-delete" 
                                               onclick="return confirm('⚠️ Cảnh báo: Bạn có chắc chắn muốn xoá sản phẩm [${p.name}] không?')"
                                               title="Xóa">🗑️</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <div style="font-size: 40px; margin-bottom: 10px;">📦</div>
                                    Không tìm thấy sản phẩm nào.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>