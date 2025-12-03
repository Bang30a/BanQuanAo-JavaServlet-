<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hướng dẫn chọn size Áo - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
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
            font-size: 2rem;
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

        .intro-text {
            text-align: center;
            color: #64748b;
            margin-bottom: 40px;
            max-width: 700px;
            margin-left: auto;
            margin-right: auto;
        }

        /* Section Styles */
        .size-section {
            margin-bottom: 60px;
            border-bottom: 1px solid #f1f5f9;
            padding-bottom: 40px;
        }
        .size-section:last-child { border-bottom: none; }

        .section-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 20px;
            border-left: 5px solid #2563eb;
            padding-left: 15px;
            display: flex;
            align-items: center;
        }

        /* Image Box */
        .product-preview {
            text-align: center;
            margin-bottom: 20px;
        }
        .product-preview img {
            max-height: 200px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }
        .product-preview img:hover { transform: scale(1.05); }

        /* Table Styles */
        .table-size {
            width: 100%;
            text-align: center;
            border-collapse: separate;
            border-spacing: 0;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            overflow: hidden;
        }
        .table-size th {
            background-color: #1e293b;
            color: white;
            font-weight: 600;
            padding: 12px;
            vertical-align: middle;
        }
        .table-size td {
            padding: 10px;
            vertical-align: middle;
            border-bottom: 1px solid #e2e8f0;
            color: #334155;
        }
        .table-size tr:last-child td { border-bottom: none; }
        .table-size tr:nth-child(even) { background-color: #f8fafc; }
        .table-size tr:hover { background-color: #eff6ff; }

        /* Back Button */
        .btn-back {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 30px;
            background-color: #2563eb;
            color: white;
            font-weight: 600;
            border-radius: 30px;
            text-decoration: none;
            transition: 0.2s;
            box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3);
        }
        .btn-back:hover {
            background-color: #1d4ed8;
            transform: translateY(-2px);
            color: white;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="header.jsp" />

    <main>
        <div class="container">
            <div class="guide-container">
                
                <!-- Breadcrumb -->
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="${context}/user/view-products">Trang chủ</a></li>
                        <li class="breadcrumb-item active">Hướng dẫn chọn size áo</li>
                    </ol>
                </nav>

                <h1>📏 Hướng Dẫn Chọn Size Áo Nam</h1>
                <p class="intro-text">
                    Chọn đúng size áo giúp bạn thoải mái và tự tin hơn. Hãy tham khảo bảng thông số dưới đây cho từng loại áo nhé!
                </p>

                <!-- 1. Áo Thun -->
                <div class="size-section">
                    <h3 class="section-title">1. Áo Thun (T-Shirt)</h3>
                    <div class="row align-items-center">
                        <div class="col-md-4 product-preview">
                            <!-- Ảnh minh họa -->
                            <img src="${context}/user/assets/images/products/áo thun.jpg" 
                                 onerror="this.src='https://placehold.co/200x200?text=Ao+Thun'" alt="Áo Thun">
                        </div>
                        <div class="col-md-8">
                            <div class="table-responsive">
                                <table class="table table-size table-bordered mb-0">
                                    <thead>
                                        <tr>
                                            <th>Size</th><th>Dài áo</th><th>Ngực</th><th>Tay áo</th><th>Cân nặng</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr><td>XS</td><td>62</td><td>43</td><td>41</td><td>&lt; 50kg</td></tr>
                                        <tr><td>S</td><td>64</td><td>46</td><td>42.5</td><td>&lt; 55kg</td></tr>
                                        <tr><td>M</td><td>67</td><td>49</td><td>44.5</td><td>&lt; 60kg</td></tr>
                                        <tr><td>L</td><td>70</td><td>52</td><td>46.5</td><td>&lt; 65kg</td></tr>
                                        <tr><td>XL</td><td>73</td><td>56</td><td>48.5</td><td>&lt; 75kg</td></tr>
                                        <tr><td>2XL</td><td>75</td><td>60</td><td>49.5</td><td>&lt; 80kg</td></tr>
                                        <tr><td>3XL</td><td>75</td><td>64</td><td>50.5</td><td>&lt; 85kg</td></tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 2. Áo Polo -->
                <div class="size-section">
                    <h3 class="section-title">2. Áo Polo</h3>
                    <div class="row align-items-center">
                        <div class="col-md-4 product-preview">
                            <img src="${context}/user/assets/images/products/áo polo.jpg" 
                                 onerror="this.src='https://placehold.co/200x200?text=Polo'" alt="Áo Polo">
                        </div>
                        <div class="col-md-8">
                            <div class="table-responsive">
                                <table class="table table-size table-bordered mb-0">
                                    <thead>
                                        <tr>
                                            <th>Size</th><th>Dài</th><th>Vai</th><th>Ngực</th><th>Tay</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr><td>XS</td><td>63.5</td><td>41</td><td>46</td><td>41</td></tr>
                                        <tr><td>S</td><td>65.5</td><td>42.5</td><td>49</td><td>42.5</td></tr>
                                        <tr><td>M</td><td>68.5</td><td>44</td><td>52</td><td>44.5</td></tr>
                                        <tr><td>L</td><td>71.5</td><td>45.5</td><td>55</td><td>46.5</td></tr>
                                        <tr><td>XL</td><td>74.5</td><td>47.5</td><td>59</td><td>48.5</td></tr>
                                        <tr><td>2XL</td><td>76.5</td><td>49.5</td><td>63</td><td>49.5</td></tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 3. Áo Sơ mi -->
                <div class="size-section">
                    <h3 class="section-title">3. Áo Sơ Mi</h3>
                    <div class="row align-items-center">
                        <div class="col-md-4 product-preview">
                            <img src="${context}/user/assets/images/products/áo sơ mi.jpg" 
                                 onerror="this.src='https://placehold.co/200x200?text=Shirt'" alt="Áo Sơ Mi">
                        </div>
                        <div class="col-md-8">
                            <div class="table-responsive">
                                <table class="table table-size table-bordered mb-0">
                                    <thead>
                                        <tr>
                                            <th>Size</th><th>Dài</th><th>Vai</th><th>Ngực (1/2)</th><th>Eo (1/2)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr><td>S</td><td>69</td><td>42</td><td>46</td><td>44</td></tr>
                                        <tr><td>M</td><td>69</td><td>43.5</td><td>48</td><td>46</td></tr>
                                        <tr><td>L</td><td>71</td><td>45</td><td>50</td><td>48</td></tr>
                                        <tr><td>XL</td><td>73</td><td>46.5</td><td>52</td><td>50</td></tr>
                                        <tr><td>2XL</td><td>73</td><td>48</td><td>54</td><td>52</td></tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 4. Áo Khoác -->
                <div class="size-section">
                    <h3 class="section-title">4. Áo Khoác</h3>
                    <div class="row align-items-center">
                        <div class="col-md-4 product-preview">
                            <img src="${context}/user/assets/images/products/áo khoác.png" 
                                 onerror="this.src='https://placehold.co/200x200?text=Jacket'" alt="Áo Khoác">
                        </div>
                        <div class="col-md-8">
                            <div class="table-responsive">
                                <table class="table table-size table-bordered mb-0">
                                    <thead>
                                        <tr>
                                            <th>Size</th><th>Dài áo</th><th>Rộng áo</th><th>Tay áo</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr><td>S</td><td>65</td><td>51</td><td>83</td></tr>
                                        <tr><td>M</td><td>68</td><td>54</td><td>85.5</td></tr>
                                        <tr><td>L</td><td>71</td><td>57</td><td>88</td></tr>
                                        <tr><td>XL</td><td>71</td><td>61</td><td>90.5</td></tr>
                                        <tr><td>2XL</td><td>76</td><td>65</td><td>91.5</td></tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Back Button -->
                <div class="text-center mt-5">
                    <a href="${context}/user/view-products" class="btn-back">
                        <i class="bi bi-arrow-left-circle"></i> Tiếp tục mua sắm
                    </a>
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