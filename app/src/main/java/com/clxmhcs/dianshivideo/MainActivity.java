package com.clxmhcs.dianshivideo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.clxmhcs.dianshivideo.data.CatalogRepository;
import com.clxmhcs.dianshivideo.data.CatalogSection;
import com.clxmhcs.dianshivideo.data.VideoItem;
import com.clxmhcs.dianshivideo.ui.VideoCardView;

import java.util.List;

/**
 * Rebuilt TV home screen: local catalog, D-pad focus, no accounts, ads,
 * plugins, dynamic code loading, trackers, or privileged permissions.
 */
public final class MainActivity extends Activity {
    public static final String EXTRA_VIDEO = "extra_video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemUi();
        setContentView(buildContent());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    private View buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 17, 20));
        root.setPadding(dp(64), dp(42), dp(64), dp(48));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, 0, 0, dp(28));

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        header.addView(title);

        TextView hint = new TextView(this);
        hint.setText("精简重建版 · 使用方向键选择，确认键播放");
        hint.setTextColor(Color.rgb(176, 181, 190));
        hint.setTextSize(15);
        hint.setPadding(0, dp(8), 0, 0);
        header.addView(hint);
        root.addView(header);

        ScrollView verticalScroll = new ScrollView(this);
        verticalScroll.setFillViewport(true);
        verticalScroll.setClipToPadding(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(16));

        List<CatalogSection> sections = CatalogRepository.load(this);
        for (CatalogSection section : sections) {
            content.addView(createSection(section));
        }
        verticalScroll.addView(content);
        root.addView(verticalScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        return root;
    }

    private View createSection(CatalogSection section) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, 0, 0, dp(34));

        TextView heading = new TextView(this);
        heading.setText(section.title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(22);
        heading.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        heading.setPadding(0, 0, 0, dp(14));
        block.addView(heading);

        HorizontalScrollView horizontalScroll = new HorizontalScrollView(this);
        horizontalScroll.setHorizontalScrollBarEnabled(false);
        horizontalScroll.setClipToPadding(false);
        horizontalScroll.setPadding(0, dp(10), dp(36), dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (VideoItem item : section.items) {
            VideoCardView card = new VideoCardView(this, item);
            card.setContentDescription(item.title);
            card.setOnClickListener(view -> openPlayer(item));
            row.addView(card, card.cardLayoutParams());
        }
        horizontalScroll.addView(row);
        block.addView(horizontalScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(170)
        ));
        return block;
    }

    private void openPlayer(VideoItem item) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO, item);
        startActivity(intent);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
