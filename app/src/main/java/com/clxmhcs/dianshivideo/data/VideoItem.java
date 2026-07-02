package com.clxmhcs.dianshivideo.data;

import java.io.Serializable;

public final class VideoItem implements Serializable {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String description;
    public final String url;
    public final String accent;

    public VideoItem(String id, String title, String subtitle, String description, String url, String accent) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.url = url;
        this.accent = accent;
    }
}
