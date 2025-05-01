package org.example.model.giphy;

import java.util.List;

public class GiphyResponse {
    private List<GifData> data;
    private Pagination pagination;
    private Meta meta;

    // Getters and setters
    public List<GifData> getData() { return data; }
    public void setData(List<GifData> data) { this.data = data; }
    public Pagination getPagination() { return pagination; }
    public Meta getMeta() { return meta; }
}