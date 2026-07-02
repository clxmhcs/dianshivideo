package com.clxmhcs.dianshivideo.data;

import java.util.Collections;
import java.util.List;

public final class CatalogSection {
    public final String title;
    public final List<VideoItem> items;

    public CatalogSection(String title, List<VideoItem> items) {
        this.title = title;
        this.items = Collections.unmodifiableList(items);
    }
}
