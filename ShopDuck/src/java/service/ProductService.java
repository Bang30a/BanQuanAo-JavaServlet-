package service;

import dao.ProductDao;
import dao.ProductVariantDao; 
import dao.SizeDao;
import entity.Products;
import entity.ProductVariants;
import entity.Size; 
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; 
import java.util.Map; 
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp này chứa TOÀN BỘ logic nghiệp vụ liên quan đến sản phẩm
 * (Bao gồm cả User và Admin).
 */
public class ProductService {

    // Service này cần cả 3 DAO
    private final ProductDao productDao;
    private final ProductVariantDao variantDao; // 
    private final SizeDao sizeDao; //
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    // --- SỬA LỖI 500: Hàm khởi tạo (constructor) PHẢI NHẬN 3 DAO ---
    public ProductService(ProductDao productDao, ProductVariantDao variantDao, SizeDao sizeDao) {
        this.productDao = productDao;
        this.variantDao = variantDao;
        this.sizeDao = sizeDao;
    }
    
    // --- HÀM CŨ (Dùng cho Admin) ---
    // (Constructor 1 tham số này bây giờ không cần nữa, 
    // nhưng chúng ta có thể giữ lại nếu servlet admin đang dùng nó)
    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
        this.variantDao = new ProductVariantDao(); // Tạm
        this.sizeDao = new SizeDao(); // Tạm
    }

    // --- CÁC HÀM CỦA USER (ĐÃ CÓ TỪ TRƯỚC) ---

    public List<Products> searchProducts(String keyword) {
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                return productDao.searchByKeyword(keyword.trim());
            } else {
                return productDao.getAllProducts();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm sản phẩm", e);
            return Collections.emptyList();
        }
    }

    public Products getProductDetails(int id) {
        try {
            return productDao.findById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy chi tiết sản phẩm: " + id, e);
            return null;
        }
    }
    
    // --- CÁC HÀM MỚI (Dùng cho trang info-products.jsp) ---

    /**
     * Lấy danh sách biến thể theo ID sản phẩm.
     */
    public List<ProductVariants> getVariantsByProductId(int productId) {
        try {
            return variantDao.findByProductId(productId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy variants cho product ID: " + productId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Lấy Map của (SizeID -> SizeLabel) để hiển thị tên Size.
     */
    public Map<Integer, String> getSizeMap() {
        HashMap<Integer, String> sizeMap = new HashMap<>();
        try {
            List<Size> sizes = sizeDao.getAllSizes();
            for (Size s : sizes) {
                sizeMap.put(s.getId(), s.getSizeLabel());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy size map", e);
        }
        return sizeMap;
    }


    // --- CÁC HÀM CỦA ADMIN (ĐÃ CÓ TỪ TRƯỚC) ---

    public List<Products> getAllProducts() {
        try {
            return productDao.getAllProducts();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tất cả sản phẩm", e);
            return Collections.emptyList();
        }
    }

    public Products getProductForEdit(int productId) {
        try {
            if (productId == 0) {
                return new Products();
            }
            Products product = productDao.findById(productId);
            return (product != null) ? product : new Products();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy sản phẩm để sửa: " + productId, e);
            return new Products();
        }
    }

    public boolean saveOrUpdateProduct(Products product) {
        try {
            if (product.getId() == 0 || productDao.findById(product.getId()) == null) {
                productDao.insert(product);
            } else {
                productDao.update(product);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật sản phẩm", e);
            return false;
        }
    }
    
public boolean deleteProduct(int id) {
    try {
        // 1. Kiểm tra xem sản phẩm còn variants không
        List<ProductVariants> variants = variantDao.findByProductId(id);

        // Nếu vẫn còn biến thể → KHÔNG được xóa
        if (variants != null && !variants.isEmpty()) {
            System.out.println("DEBUG: Product còn variants → không thể xóa");
            return false;
        }

        // 2. Nếu không có variants → cho phép xóa
        int result = productDao.delete(id);
        System.out.println("DEBUG: deleteProduct() result = " + result);

        return result > 0;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}