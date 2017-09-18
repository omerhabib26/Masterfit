package www.tecjaunt.com.masterfit.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Omer Habib on 9/5/2017.
 */

public class Categories {

    @SerializedName("categories_id")
    @Expose
    private String uid;

    @SerializedName("categories_title")
    @Expose
    private String title;

    @SerializedName("categories_description")
    @Expose
    private String description = null;

    @SerializedName("photo_path")
    @Expose
    private String image;

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
