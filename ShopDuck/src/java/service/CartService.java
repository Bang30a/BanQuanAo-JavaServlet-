package service;

import entity.CartBean;
import entity.ProductVariants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lớp này chứa toàn bộ logic nghiệp vụ cho Giỏ hàng.
 */
public class CartService {

    /**
     * Thêm một sản phẩm vào giỏ hàng hoặc tăng số lượng.
     * @param cart Giỏ hàng hiện tại (có thể null)
     * @param variant Sản phẩm cần thêm (lấy từ DAO)
     * @return Giỏ hàng đã được cập nhật.
     */
    public List<CartBean> addToCart(List<CartBean> cart, ProductVariants variant) {
        return addToCart(cart, variant, 1); // Gọi hàm có tham số quantity
    }

    /**
     * Thêm sản phẩm vào giỏ hàng với số lượng tùy chọn.
     * @param cart Giỏ hàng hiện tại (có thể null)
     * @param variant Sản phẩm cần thêm (lấy từ DAO)
     * @param quantity Số lượng cần thêm
     * @return Giỏ hàng đã được cập nhật
     */
    public List<CartBean> addToCart(List<CartBean> cart, ProductVariants variant, int quantity) {
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Nếu sản phẩm không tồn tại (DAO trả về null), không làm gì cả
        if (variant == null) {
            return cart;
        }

        boolean found = false;

        // Tìm xem sản phẩm đã có trong giỏ chưa
        for (CartBean item : cart) {
            if (item.getProductVariant().getId() == variant.getId()) {
                item.setQuantity(item.getQuantity() + quantity); // ✅ Tăng số lượng
                found = true;
                break;
            }
        }

        // Nếu chưa có, thêm mới vào giỏ
        if (!found) {
            cart.add(new CartBean(variant, quantity));
        }

        return cart;
    }

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ.
     * @param cart Giỏ hàng hiện tại
     * @param variantId ID sản phẩm cần cập nhật
     * @param newQuantity Số lượng mới (nếu <= 0 thì sẽ xóa sản phẩm)
     */
    public void updateQuantity(List<CartBean> cart, int variantId, int newQuantity) {
        if (cart == null) return;

        Iterator<CartBean> iterator = cart.iterator();
        while (iterator.hasNext()) {
            CartBean item = iterator.next();
            if (item.getProductVariant().getId() == variantId) {
                if (newQuantity <= 0) {
                    iterator.remove(); // Xóa nếu số lượng <= 0
                } else {
                    item.setQuantity(newQuantity);
                }
                break;
            }
        }
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng bằng vị trí (index).
     * @param cart Giỏ hàng hiện tại
     * @param index Vị trí của sản phẩm cần xóa
     */
    public void removeFromCart(List<CartBean> cart, int index) {
        if (cart != null && index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
    }

    /**
     * Xóa toàn bộ sản phẩm khỏi giỏ hàng.
     * @param cart Giỏ hàng hiện tại
     */
    public void clearCart(List<CartBean> cart) {
        if (cart != null) {
            cart.clear();
        }
    }

    /**
     * Tính tổng giá trị của giỏ hàng.
     * @param cart Giỏ hàng hiện tại
     * @return Tổng tiền (double)
     */
    public double calculateTotal(List<CartBean> cart) {
        if (cart == null || cart.isEmpty()) return 0.0;

        double total = 0.0;
        for (CartBean item : cart) {
            double price = item.getProductVariant().getPrice();
            total += price * item.getQuantity();
        }
        return total;
    }
}
