package com.clxmhcs.dianshivideo.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads only bundled catalog data. Production code may replace this class with
 * an HTTPS API implementation after validating all input and media ownership.
 */
public final class CatalogRepository {
    private CatalogRepository() {
    }

    public static List<CatalogSection> load(Context context) {
        try (InputStream input = context.getAssets().open("catalog.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return parse(new JSONObject(json.toString()));
        } catch (Exception ignored) {
            return fallback();
        }
    }

    private static List<CatalogSection> parse(JSONObject root) throws Exception {
        List<CatalogSection> sections = new ArrayList<>();
        JSONArray sectionArray = root.optJSONArray("sections");
        if (sectionArray == null) {
            throw new IllegalArgumentException("Missing sections");
        }

        for (int index = 0; index < sectionArray.length(); index++) {
            JSONObject section = sectionArray.getJSONObject(index);
            String title = section.optString("title", "未命名分区");
            JSONArray itemArray = section.optJSONArray("items");
            List<VideoItem> items = new ArrayList<>();
            if (itemArray != null) {
                for (int itemIndex = 0; itemIndex < itemArray.length(); itemIndex++) {
                    JSONObject item = itemArray.getJSONObject(itemIndex);
                    String id = item.optString("id");
                    String url = item.optString("url");
                    if (id.isEmpty() || url.isEmpty() || !url.startsWith("https://")) {
                        continue;
                    }
                    items.add(new VideoItem(
                            id,
                            item.optString("title", "未命名视频"),
                            item.optString("subtitle", ""),
                            item.optString("description", ""),
                            url,
                            item.optString("accent", "#4A5568")
                    ));
                }
            }
            if (!items.isEmpty()) {
                sections.add(new CatalogSection(title, items));
            }
        }
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("No playable entries");
        }
        return sections;
    }

    private static List<CatalogSection> fallback() {
        VideoItem item = new VideoItem(
                "fallback",
                "示例内容",
                "请在 assets/catalog.json 中配置你的 HTTPS 媒体地址",
                "目录读取失败时显示的本地兜底条目。",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                "#4A5568"
        );
        return Arrays.asList(new CatalogSection("本地目录", Arrays.asList(item)));
    }
}
