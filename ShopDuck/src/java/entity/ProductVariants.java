package entity;

public class ProductVariants {
    private int id;
    private int productId;
    private int sizeId;
    private int stock;
    private double price;
    
    // Các trường bổ sung (để hiển thị tên sản phẩm và tên size)
    private String productName;
    private String sizeName;

    // Constructor mặc định
    public ProductVariants() {
    }

    // Constructor cơ bản (thường dùng khi insert/update)
    public ProductVariants(int id, int productId, int sizeId, int stock, double price) {
        this.id = id;
        this.productId = productId;
        this.sizeId = sizeId;
        this.stock = stock;
        this.price = price;
    }

    // Constructor đầy đủ (dùng khi select join bảng để hiển thị)
    public ProductVariants(int id, int productId, int sizeId, int stock, double price, String productName, String sizeName) {
        this.id = id;
        this.productId = productId;
        this.sizeId = sizeId;
        this.stock = stock;
        this.price = price;
        this.productName = productName;
        this.sizeName = sizeName;
    }

    // --- GETTERS & SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
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

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    @Override
    public String toString() {
        return "ProductVariants{" +
                "id=" + id +
                ", productId=" + productId +
                ", sizeId=" + sizeId +
                ", stock=" + stock +
                ", price=" + price +
                ", productName='" + productName + '\'' +
                ", sizeName='" + sizeName + '\'' +
                '}';
    }
}