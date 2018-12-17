package shared_objects;

import java.io.Serializable;

public class Image implements Serializable {
    public int id;
    public byte[] image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
