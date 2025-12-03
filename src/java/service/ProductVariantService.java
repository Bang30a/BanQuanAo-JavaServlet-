    package service;

    import dao.ProductVariantDao;
    import entity.ProductVariants;
    import java.util.List;
    import java.util.Collections;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * Lớp này chứa toàn bộ logic nghiệp vụ để quản lý Biến thể Sản phẩm (CRUD).
     */
    public class ProductVariantService {

        private final ProductVariantDao variantDao;
        private static final Logger LOGGER = Logger.getLogger(ProductVariantService.class.getName());

        // Nhận DAO qua constructor
        public ProductVariantService(ProductVariantDao variantDao) {
            this.variantDao = variantDao;
        }

        /**
         * Lấy tất cả các biến thể một cách an toàn.
         */
        public List<ProductVariants> getAllVariants() {
            try {
                return variantDao.getAllVariants();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy tất cả biến thể", e);
                return Collections.emptyList(); // Trả về danh sách rỗng
            }
        }

        /**
         * Lấy một biến thể để chỉnh sửa.
         * Nếu ID = 0 hoặc không tìm thấy, trả về một đối tượng mới (rỗng).
         */
        public ProductVariants getVariantForEdit(int variantId) {
            try {
                if (variantId == 0) {
                    return new ProductVariants(); // Cho trường hợp "Thêm mới"
                }
                ProductVariants variant = variantDao.findById(variantId);
                return (variant != null) ? variant : new ProductVariants();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy biến thể theo ID: " + variantId, e);
                return new ProductVariants(); // Trả về rỗng nếu có lỗi
            }
        }

        /**
         * Lưu (Thêm mới) hoặc Cập nhật một biến thể.
         */
        public boolean saveOrUpdateVariant(ProductVariants variant) {
            try {
                // Logic "upsert" (update/insert)
                if (variant.getId() == 0 || variantDao.findById(variant.getId()) == null) {
                    variantDao.insertVariant(variant);
                } else {
                    variantDao.updateVariant(variant);
                }
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật biến thể", e);
                return false;
            }
        }

        /**
         * Xóa một biến thể theo ID.
         */
        public boolean deleteVariant(int variantId) {
            try {
                variantDao.deleteVariant(variantId);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi xóa biến thể: " + variantId, e);
                return false;
            }
        }
    }