package com.clxmhcs.dianshivideo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.clxmhcs.dianshivideo.data.VideoItem;

/** Full-screen player with basic remote controls and local resume positions. */
public final class PlayerActivity extends Activity {
    private static final String PREFS = "watch_history";
    private static final long PROGRESS_INTERVAL_MS = 2_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private VideoView videoView;
    private View overlay;
    private Button playPauseButton;
    private TextView statusText;
    private VideoItem item;
    private boolean prepared;
    private boolean overlayVisible = true;
    private final Runnable progressSaver = new Runnable() {
        @Override
        public void run() {
            saveProgress();
            handler.postDelayed(this, PROGRESS_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemUi();

        Object extra = getIntent().getSerializableExtra(MainActivity.EXTRA_VIDEO);
        if (!(extra instanceof VideoItem)) {
            finish();
            return;
        }
        item = (VideoItem) extra;
        setContentView(buildContent());
        prepareVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        handler.post(progressSaver);
    }

    @Override
    protected void onPause() {
        saveProgress();
        handler.removeCallbacks(progressSaver);
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(progressSaver);
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (overlayVisible) {
                setOverlayVisible(false);
            } else {
                finish();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            togglePlayback();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private View buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        videoView = new VideoView(this);
        videoView.setBackgroundColor(Color.BLACK);
        root.addView(videoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.BOTTOM);
        panel.setPadding(dp(52), dp(36), dp(52), dp(42));
        panel.setBackgroundColor(Color.argb(200, 0, 0, 0));
        panel.setFocusable(false);
        overlay = panel;

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        panel.addView(title);

        TextView description = new TextView(this);
        description.setText(item.description);
        description.setTextColor(Color.rgb(210, 214, 220));
        description.setTextSize(15);
        description.setMaxLines(2);
        description.setPadding(0, dp(8), 0, dp(14));
        panel.addView(description);

        statusText = new TextView(this);
        statusText.setTextColor(Color.rgb(220, 224, 230));
        statusText.setTextSize(14);
        statusText.setPadding(0, 0, 0, dp(14));
        panel.addView(statusText);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        playPauseButton = button("播放");
        playPauseButton.setOnClickListener(view -> togglePlayback());
        controls.addView(playPauseButton, buttonParams());

        Button restart = button("从头播放");
        restart.setOnClickListener(view -> {
            if (prepared) {
                videoView.seekTo(0);
                videoView.start();
                updatePlayButton();
            }
        });
        controls.addView(restart, buttonParams());

        Button close = button("返回首页");
        close.setOnClickListener(view -> finish());
        controls.addView(close, buttonParams());
        panel.addView(controls);

        root.addView(panel, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        ));
        return root;
    }

    private void prepareVideo() {
        statusText.setText("正在加载…");
        videoView.setVideoURI(Uri.parse(item.url));
        videoView.setOnPreparedListener(mediaPlayer -> {
            prepared = true;
            int savedPosition = history().getInt(item.id, 0);
            if (savedPosition > 5_000) {
                videoView.seekTo(savedPosition);
                statusText.setText("已恢复上次观看进度");
            } else {
                statusText.setText("按确认键播放或暂停");
            }
            videoView.start();
            updatePlayButton();
            playPauseButton.requestFocus();
        });
        videoView.setOnCompletionListener(mediaPlayer -> {
            history().edit().remove(item.id).apply();
            statusText.setText("播放完成");
            updatePlayButton();
            setOverlayVisible(true);
        });
        videoView.setOnErrorListener((MediaPlayer mediaPlayer, int what, int extra) -> {
            statusText.setText(getString(R.string.player_error));
            setOverlayVisible(true);
            return true;
        });
        videoView.setOnTouchListener((view, motionEvent) -> {
            setOverlayVisible(!overlayVisible);
            return true;
        });
    }

    private void togglePlayback() {
        if (!prepared) {
            return;
        }
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
        }
        setOverlayVisible(true);
        updatePlayButton();
    }

    private void updatePlayButton() {
        if (playPauseButton == null) {
            return;
        }
        playPauseButton.setText(videoView != null && videoView.isPlaying() ? "暂停" : "播放");
    }

    private void setOverlayVisible(boolean visible) {
        overlayVisible = visible;
        overlay.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible && playPauseButton != null) {
            playPauseButton.requestFocus();
        }
        hideSystemUi();
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setFocusable(true);
        button.setBackground(createButtonBackground());
        button.setOnFocusChangeListener((view, focused) -> {
            view.animate().scaleX(focused ? 1.06f : 1f).scaleY(focused ? 1.06f : 1f).setDuration(100).start();
        });
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(150), dp(50));
        params.setMargins(0, 0, dp(16), 0);
        return params;
    }

    private GradientDrawable createButtonBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(54, 58, 66));
        background.setCornerRadius(dp(8));
        return background;
    }

    private SharedPreferences history() {
        return getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private void saveProgress() {
        if (item == null || videoView == null || !prepared) {
            return;
        }
        int position = videoView.getCurrentPosition();
        int duration = videoView.getDuration();
        SharedPreferences.Editor editor = history().edit();
        if (duration > 0 && position >= duration - 5_000) {
            editor.remove(item.id);
        } else if (position > 0) {
            editor.putInt(item.id, position);
        }
        editor.apply();
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
