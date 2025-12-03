package entity;

public class Products {
    private int id;
    private String name;
    private String description;
    private double price;
    private String image;
    private int categoryId;

    public Products() {
    }

    // ✅ Constructor 5 tham số – dùng cho ProductDao
    public Products(int id, String name, String description, double price, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    // ✅ Constructor 6 tham số – dùng trong test hoặc DAO có category
    public Products(int id, String name, double price, String description, String image, int categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
        this.categoryId = categoryId;
    }

    // Getter Setter đầy đủ
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}
