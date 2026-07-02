package com.clxmhcs.dianshivideo.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clxmhcs.dianshivideo.data.VideoItem;

/** A lightweight focusable card designed for D-pad interaction. */
public final class VideoCardView extends LinearLayout {
    private static final int CARD_WIDTH_DP = 260;
    private static final int CARD_HEIGHT_DP = 146;

    private final GradientDrawable background;

    public VideoCardView(Context context, VideoItem item) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(18), dp(16), dp(18), dp(14));
        setFocusable(true);
        setClickable(true);
        setClipToOutline(true);
        setOutlineProvider(new RoundedOutlineProvider(dp(12)));

        background = new GradientDrawable();
        background.setColor(parseColor(item.accent));
        background.setCornerRadius(dp(12));
        setBackground(background);

        TextView title = new TextView(context);
        title.setText(item.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        addView(title, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        TextView subtitle = new TextView(context);
        subtitle.setText(item.subtitle);
        subtitle.setTextColor(Color.argb(210, 255, 255, 255));
        subtitle.setTextSize(13);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        addView(subtitle, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setOnFocusChangeListener((view, focused) -> {
            float scale = focused ? 1.08f : 1f;
            view.animate().scaleX(scale).scaleY(scale).setDuration(120).start();
            setElevation(focused ? dp(12) : 0);
            background.setStroke(focused ? dp(3) : 0, Color.WHITE);
        });
    }

    public LayoutParams cardLayoutParams() {
        LayoutParams params = new LayoutParams(dp(CARD_WIDTH_DP), dp(CARD_HEIGHT_DP));
        params.setMargins(0, 0, dp(22), 0);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static int parseColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (Exception ignored) {
            return Color.rgb(74, 85, 104);
        }
    }

    private static final class RoundedOutlineProvider extends ViewOutlineProvider {
        private final int radius;

        RoundedOutlineProvider(int radius) {
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
        }
    }
}
