<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hướng dẫn chọn size - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
    <!-- Lưu ý: Sửa đường dẫn css trỏ ra thư mục assets -->
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

        .guide-container {
            background: white;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            max-width: 900px;
            margin: 0 auto;
        }

        h1 {
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 20px;
            text-align: center;
        }

        .breadcrumb {
            background: transparent;
            padding: 0;
            margin-bottom: 30px;
            font-size: 0.9rem;
            justify-content: center;
        }
        .breadcrumb a {
            text-decoration: none;
            color: #64748b;
        }
        .breadcrumb .active { color: #2563eb; font-weight: 600; }

        .description {
            text-align: center;
            max-width: 700px;
            margin: 0 auto 40px;
            color: #475569;
            line-height: 1.6;
        }

        .guide-section {
            margin-bottom: 50px;
        }
        
        .section-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 20px;
            border-left: 4px solid #2563eb;
            padding-left: 15px;
        }

        .image-box {
            text-align: center;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 10px;
            background: #fff;
            transition: transform 0.3s;
        }
        .image-box:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        }
        
        .image-box img {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
        }

        .note-box {
            background-color: #eff6ff;
            border: 1px solid #bfdbfe;
            border-radius: 12px;
            padding: 25px;
            margin-top: 40px;
            color: #1e3a8a;
        }
        .note-title {
            font-weight: 700;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <!-- File này nằm trong user/includes/ nên gọi header cùng cấp -->
    <jsp:include page="header.jsp" />

    <main>
        <div class="container">
            <div class="guide-container">
                
                <!-- Breadcrumb -->
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="${context}/index.jsp">Trang chủ</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Hướng dẫn chọn size</li>
                    </ol>
                </nav>

                <h1>Bảng Hướng Dẫn Chọn Size</h1>

                <div class="description">
                    Bạn băn khoăn không biết chọn size nào phù hợp với vóc dáng của mình? <br>
                    Đừng lo lắng! Hãy tham khảo bảng thông số dưới đây để chọn được sản phẩm ưng ý nhất tại <strong>ShopDuck</strong>.
                </div>

                <!-- Phần 1: Size Áo -->
                <div class="guide-section">
                    <h3 class="section-title">1. Hướng dẫn chọn size Áo (Sơ mi, Thun, Polo)</h3>
                    <div class="image-box">
                        <!-- Ảnh minh họa: Dùng ảnh placeholder nếu chưa có ảnh thật -->
                        <img src="${context}/user/assets/images/ui/bang-size-ao.png" 
                             onerror="this.src='https://placehold.co/800x400/f1f5f9/334155?text=Bảng+Size+Áo+Nam'" 
                             alt="Bảng size áo nam">
                    </div>
                </div>

                <!-- Phần 2: Size Quần -->
                <div class="guide-section">
                    <h3 class="section-title">2. Hướng dẫn chọn size Quần (Jeans, Âu, Short)</h3>
                    <div class="image-box">
                        <img src="${context}/user/assets/images/ui/bang-size-quan.png" 
                             onerror="this.src='https://placehold.co/800x400/f1f5f9/334155?text=Bảng+Size+Quần+Nam'" 
                             alt="Bảng size quần nam">
                    </div>
                </div>

                <!-- Ghi chú -->
                <div class="note-box">
                    <div class="note-title">
                        <i class="bi bi-info-circle-fill"></i> Lưu ý quan trọng
                    </div>
                    <p class="mb-2">
                        - Bảng size trên chỉ mang tính chất tham khảo dựa trên số liệu trung bình của người Việt Nam.
                    </p>
                    <p class="mb-2">
                        - Nếu bạn có bụng hoặc thích mặc rộng rãi, vui lòng <strong>tăng thêm 1 size</strong>.
                    </p>
                    <p class="mb-0">
                        - Cần tư vấn kỹ hơn? Liên hệ ngay hotline <strong>0999.xxx.yyy</strong> để được hỗ trợ nhanh nhất.
                    </p>
                </div>

            </div>
        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>