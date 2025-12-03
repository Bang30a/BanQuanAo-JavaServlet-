package entity;

public class Size {
    private int id;
    private String sizeLabel;

    public Size() {}

    public Size(int id, String sizeLabel) {
        this.id = id;
        this.sizeLabel = sizeLabel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public void setSizeLabel(String sizeLabel) {
        this.sizeLabel = sizeLabel;
    }
}
