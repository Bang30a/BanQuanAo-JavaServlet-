package entity;

public class OrderDetails {
    private int id;
    private int orderId;
    private int productVariantId;
    private int quantity;
    private double price;
    private String productName; // ✅ Thêm thuộc tính productName

    public OrderDetails() {}

    // Constructor đầy đủ (không gồm productName)
    public OrderDetails(int id, int orderId, int productVariantId, int quantity, double price) {
        this.id = id;
        this.orderId = orderId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.price = price;
    }

    // Constructor không có id
    public OrderDetails(int orderId, int productVariantId, int quantity, double price) {
        this.orderId = orderId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.price = price;
    }

    // ✅ Constructor có productName (nếu dùng khi join để hiển thị)
    public OrderDetails(int id, int orderId, int productVariantId, int quantity, double price, String productName) {
        this.id = id;
        this.orderId = orderId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.price = price;
        this.productName = productName;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductVariantId() {
        return productVariantId;
    }
    public void setProductVariantId(int productVariantId) {
        this.productVariantId = productVariantId;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", productVariantId=" + productVariantId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", productName='" + productName + '\'' +
                '}';
    }
}
