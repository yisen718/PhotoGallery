package com.example.yisen614.photogallery;

public class Image {

    private int id;
    private String pic_url;
    private boolean loaded = false;
    private String page_url;
    private String title;
    private Object publish_time;
    private String tags;

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getPage_url() {
        return page_url;
    }

    public void setPage_url(String page_url) {
        this.page_url = page_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getPublish_time() {
        return publish_time;
    }

    public void setPublish_time(Object publish_time) {
        this.publish_time = publish_time;
    }


    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", pic_url='" + pic_url + '\'' +
                ", page_url='" + page_url + '\'' +
                ", title='" + title + '\'' +
                ", publish_time=" + publish_time +
                '}';
    }
}
