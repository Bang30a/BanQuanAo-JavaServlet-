package entity;

public class CartBean {
    private ProductVariants productVariant;
    private int quantity;

    public CartBean() {}

    public CartBean(ProductVariants productVariant, int quantity) {
        this.productVariant = productVariant;
        this.quantity = quantity > 0 ? quantity : 1; // bảo vệ số lượng
    }

    public ProductVariants getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariants productVariant) {
        this.productVariant = productVariant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = (quantity < 1) ? 1 : quantity;
    }

    public double getTotalPrice() {
        if (productVariant != null) {
            return productVariant.getPrice() * quantity;
        }
        return 0;
    }
}
