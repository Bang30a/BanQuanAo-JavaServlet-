<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng</title>
    
    <!-- HEAD CHUẨN (Dùng Bootstrap 4) -->
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
    <jsp:include page="header.jsp" />
    
    <main>
        <div class="container mt-4">
            <a href="${pageContext.request.contextPath}/user/order-history" class="btn btn-outline-secondary btn-sm mb-3">
                &laquo; Quay lại Lịch sử
            </a>
            
            <c:if test="${not empty order}">
                <div class="card shadow-sm">
                    <div class="card-header">
                        <h4>Chi tiết đơn hàng #${order.id}</h4>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <strong>Trạng thái:</strong> 
                                <span class="badge badge-success">${order.status}</span>
                            </div>
                            <div class="col-md-6 text-md-right">
                                <strong>Ngày đặt:</strong> 
                                <fmt:formatDate value="${order.orderDate}" pattern="HH:mm dd/MM/yyyy" />
                            </div>
                        </div>
                        <hr>
                        <p><strong>Địa chỉ giao hàng:</strong> ${order.address}</p>
                        <p><strong>Số điện thoại:</strong> ${order.phone}</p>
                        <hr>
                        
                        <h5>Các sản phẩm:</h5>
                        <ul class="list-group list-group-flush">
                            <c:forEach var="item" items="${detailsList}">
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                        <img src="${item.productImage}" alt="${item.productName}" style="width: 60px; height: 60px; object-fit: cover; margin-right: 15px;">
                                        <div>
                                            <strong>${item.productName}</strong>
                                            <br>
                                            <small class="text-muted">Size: ${item.sizeLabel} | SL: ${item.quantity}</small>
                                        </div>
                                    </div>
                                    <span class="text-danger font-weight-bold">
                                        <fmt:formatNumber value="${item.price * item.quantity}" type="number" groupingUsed="true" /> VNĐ
                                    </span>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                    <div class="card-footer text-right">
                        <h4>
                            Tổng tiền: 
                            <span class="text-danger font-weight-bold">
                                <fmt:formatNumber value="${order.total}" type="number" groupingUsed="true" /> VNĐ
                            </span>
                        </h4>
                    </div>
                </div>
            </c:if>
        </div>
    </main>
    
    <jsp:include page="footer.jsp" />
</body>
</html>