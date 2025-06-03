package entity;

import java.sql.Timestamp;

public class Orders {
    private int id;
    private int userId;
    private Timestamp orderDate;
    private double total;
    private String address;
    private String phone;
    private String status;

    // Constructor không tham số
    public Orders() {}

    // Constructor đầy đủ tham số (có id và status)
    public Orders(int id, int userId, Timestamp orderDate, double total, String address, String phone, String status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.total = total;
        this.address = address;
        this.phone = phone;
        this.status = status;
    }

    // Constructor không có id (dùng khi tạo mới đơn hàng)
    public Orders(int userId, Timestamp orderDate, double total, String address, String phone, String status) {
        this.userId = userId;
        this.orderDate = orderDate;
        this.total = total;
        this.address = address;
        this.phone = phone;
        this.status = status;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderDate=" + orderDate +
                ", total=" + total +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
