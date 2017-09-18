package www.tecjaunt.com.masterfit.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Omer Habib on 9/5/2017.
 */

public class Link {

    @SerializedName("link_thumbnail_url")
    @Expose
    private String image;

    @SerializedName("link_name")
    @Expose
    private String url;

    @SerializedName("link_yt_video_title")
    @Expose
    private String title;

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
