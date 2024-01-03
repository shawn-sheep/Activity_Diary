/*
 * ActivityDiary
 *
 * Copyright (C) 2023 Raphael Mack http://www.raphael-mack.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.rampro.activitydiary.ui.statistics;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContract;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.helpers.GraphicsHelper;
import de.rampro.activitydiary.helpers.JaroWinkler;
import de.rampro.activitydiary.model.DetailViewModel;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.ui.generic.BaseActivity;


public class DetailActivity extends BaseActivity implements ActivityHelper.DataChangedListener {
    @Nullable
    private DiaryActivity currentActivity; /* null is for creating a new object */

    private final int QUERY_NAMES = 1;
    private final int RENAME_DELETED_ACTIVITY = 2;
    private final int TEST_DELETED_NAME = 3;
    private final int SIMILAR_ACTIVITY = 4;

    private final String[] NAME_TEST_PROJ = new String[]{ActivityDiaryContract.DiaryActivity.NAME};

    private final String COLOR_KEY = "COLOR";
    private final String NAME_KEY = "NAME";

    private EditText mActivityName;

    private DetailViewModel mViewModel;


    private TextInputLayout mActivityNameTIL;

    private int mActivityColor;

    private int linkCol; /* accent color -> to be sued for links */
    private ImageButton mQuickFixBtn1;
    private ImageButton mBtnRenameDeleted;


    private int checkState = CHECK_STATE_CHECKING;
    private static final int CHECK_STATE_CHECKING = 0;
    private static final int CHECK_STATE_OK = 1;
    private static final int CHECK_STATE_WARNING = 2;
    private static final int CHECK_STATE_ERROR = 3;

    JaroWinkler mJaroWinkler = new JaroWinkler(0.8);
    private Timer timer; // 将 Timer 定义为成员变量
    private TimerTask timerTask; // 定义 TimerTask 为成员变量
    private long pauseTime = 0; // 记录暂停时的时间
    private LinearLayout rlContent;
    private TextInputLayout editActivityNameTil;
    private TextInputEditText editActivityName;
    private ImageButton quickFixButton1;
    private ImageButton quickFixButtonRename;
    private TextView tvTime;
    private ImageView ivPlay;
    private TextView tvTips;

    private int getCheckState() {
        return checkState;
    }

    private long baseTimer;
    private boolean isStart = false;

    private void setCheckState(int checkState) {
        this.checkState = checkState;
        if (checkState == CHECK_STATE_CHECKING && mActivityNameTIL != null) {
            mActivityNameTIL.setError("...");
        }
    }


    private void setBtnTooltip(View view, @Nullable CharSequence tooltipText) {
        if (Build.VERSION.SDK_INT < 26) {
            TooltipCompat.setTooltipText(view, tooltipText);
        } else {
            view.setTooltipText(tooltipText);
        }
    }

    /* refresh all view elements depending on currentActivity */
    private void refreshElements() {
        if (currentActivity != null) {
            mActivityName.setText(currentActivity.getName());
            getSupportActionBar().setTitle(currentActivity.getName());
            String name = currentActivity.getName();

            switch (name) {
                case "Cinema":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.cinema);
                    break;
                case "Cooking":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.cooking);
                    break;
                case "Sleeping":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.sleeping);
                    break;
                case "Cleaning":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.cleaning);
                    break;
                case "Woodworking":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.woodworking);
                    break;
                case "Gardening":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.gardening);
                    break;
                case "Officework":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.officework);
                    break;
                case "Relaxing":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.relaxing);
                    break;
                case "Swimming":
                    findViewById(R.id.rl_content).setBackgroundResource(R.mipmap.swimming);
                    break;
                default:
                    break;
            }
            mActivityColor = currentActivity.getColor();


        } else {
            currentActivity = null;
            mActivityColor = GraphicsHelper.prepareColorForNextActivity();


        }

    }

    final Handler startTimehandler = new Handler() {
        public void handleMessage(Message msg) {
            tvTime.setText(String.valueOf(msg.obj));

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            linkCol = getResources().getColor(R.color.colorAccent, null);
        } else {
            linkCol = getResources().getColor(R.color.colorAccent);
        }
        setCheckState(CHECK_STATE_CHECKING);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Intent i = getIntent();
        int actId = i.getIntExtra("activityID", -1);
        if (actId == -1) {
            currentActivity = null;
        } else {
            currentActivity = ActivityHelper.helper.activityWithId(actId);
        }

        View contentView = inflater.inflate(R.layout.activity_details_content, null, false);

        setContent(contentView);
        initView();
        mActivityName = (EditText) contentView.findViewById(R.id.edit_activity_name);

        mActivityNameTIL = (TextInputLayout) findViewById(R.id.edit_activity_name_til);
        mQuickFixBtn1 = (ImageButton) findViewById(R.id.quickFixButton1);
        mBtnRenameDeleted = (ImageButton) findViewById(R.id.quickFixButtonRename);

        mViewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(NAME_KEY);
            mActivityColor = savedInstanceState.getInt(COLOR_KEY);
            mActivityName.setText(name);
            getSupportActionBar().setTitle(name);
            setActivityBackground(name);
        } else {
            refreshElements();
            startTimer();
        }
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_cancel);
        tvTips.setText(getIntent().getStringExtra("tips"));
        isStart = true; // 假设页面打开时开始计时
        baseTimer = SystemClock.elapsedRealtime();
        startTimer(); // 开始计时
        ivPlay.setImageResource(R.drawable.baseline_not_started_24);
        isStart = true;

//        // 设置播放按钮的点击监听器
//        findViewById(R.id.iv_play).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleTimer();
//            }
//        });
    }

    private void toggleTimer() {
        if (isStart) {
            pauseTime = SystemClock.elapsedRealtime() - baseTimer;
            stopTimer();
            ivPlay.setImageResource(R.drawable.baseline_play_circle_24); // 切换到播放图标
        } else {
            baseTimer = SystemClock.elapsedRealtime() - pauseTime;
            startTimer();
            ivPlay.setImageResource(R.drawable.baseline_not_started_24); // 切换到暂停图标
        }
        isStart = !isStart;
    }


    private void setActivityBackground(String activityName) {
        if (activityName == null || rlContent == null) {
            return;
        }

        switch (activityName) {
            case "Cinema":
                rlContent.setBackgroundResource(R.mipmap.cinema);
                break;
            case "Cooking":
                rlContent.setBackgroundResource(R.mipmap.cooking);
                break;
            case "Sleeping":
                rlContent.setBackgroundResource(R.mipmap.sleeping);
                break;
            case "Cleaning":
                rlContent.setBackgroundResource(R.mipmap.cleaning);
                break;
            case "Woodworking":
                rlContent.setBackgroundResource(R.mipmap.woodworking);
                break;
            case "Gardening":
                rlContent.setBackgroundResource(R.mipmap.gardening);
                break;
            case "Officework":
                rlContent.setBackgroundResource(R.mipmap.officework);
                break;
            case "Relaxing":
                rlContent.setBackgroundResource(R.mipmap.relaxing);
                break;
            case "Swimming":
                rlContent.setBackgroundResource(R.mipmap.swimming);
                break;
            default:
                // 您可以选择一个默认的背景或者不设置
                break;
        }
    }

    private void handlePlayButton() {
        if (!isStart) {
            ivPlay.setImageResource(R.drawable.baseline_not_started_24);
            isStart = true;
            startTimer();
        } else {
            isStart = false;
            ivPlay.setImageResource(R.drawable.baseline_play_circle_24);
            stopTimer();
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer("计时器");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long elapsed = SystemClock.elapsedRealtime() - baseTimer;
                updateTimer(elapsed);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000L);
    }

    private void updateTimer(long elapsedTime) {
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;

        // 更新UI必须在主线程中进行
        int finalSeconds = seconds;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTime.setText(String.format("%02d:%02d:%02d", hours, minutes, finalSeconds));
            }
        });
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }


    @Override
    public void onResume() {
        if (currentActivity == null) {
            mNavigationView.getMenu().findItem(R.id.nav_add_activity).setChecked(true);
        }
        ActivityHelper.helper.registerDataChangeListener(this);

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ActivityHelper.helper.unregisterDataChangeListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(NAME_KEY, mActivityName.getText().toString());
        outState.putInt(COLOR_KEY, mActivityColor);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer(); // 在销毁活动时停止计时器
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_edit_delete:
                if (currentActivity != null) {
                    ActivityHelper.helper.deleteActivity(currentActivity);
                }
                finish();
                break;
            case R.id.action_edit_done:
                if (getCheckState() != CHECK_STATE_CHECKING) {
                    CharSequence error = mActivityNameTIL.getError();
                    if (getCheckState() == CHECK_STATE_ERROR) {
                        Toast.makeText(DetailActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        if (currentActivity == null) {
                            ActivityHelper.helper.insertActivity(new DiaryActivity(-1, mActivityName.getText().toString(), mActivityColor));
                        } else {
                            currentActivity.setName(mActivityName.getText().toString());
                            currentActivity.setColor(mActivityColor);
                            ActivityHelper.helper.updateActivity(currentActivity);
                        }
                        finish();
                    }
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the data has changed and no further specification is possible.
     * => everything needs to be refreshed!
     */
    @Override
    public void onActivityDataChanged() {
        refreshElements();
    }

    /**
     * Called when the data of one activity was changed.
     *
     * @param activity
     */
    @Override
    public void onActivityDataChanged(DiaryActivity activity) {
        if (activity == currentActivity) {
            refreshElements();
        }
    }

    /**
     * Called on addition of an activity.
     *
     * @param activity
     */
    @Override
    public void onActivityAdded(DiaryActivity activity) {
        if (activity == currentActivity) {
            refreshElements();
        }
    }

    /**
     * Called on removale of an activity.
     *
     * @param activity
     */
    @Override
    public void onActivityRemoved(DiaryActivity activity) {
        if (activity == currentActivity) {
            refreshElements();
            // TODO: handle deletion of the activity while in editing it...
        }
    }

    /**
     * Called on change of the current activity.
     */
    @Override
    public void onActivityChanged() {

    }

    /**
     * Called on change of the activity order due to likelyhood.
     */
    @Override
    public void onActivityOrderChanged() {

    }

    private void initView() {
        rlContent = (LinearLayout) findViewById(R.id.rl_content);
        editActivityNameTil = (TextInputLayout) findViewById(R.id.edit_activity_name_til);
        editActivityName = (TextInputEditText) findViewById(R.id.edit_activity_name);
        quickFixButton1 = (ImageButton) findViewById(R.id.quickFixButton1);
        quickFixButtonRename = (ImageButton) findViewById(R.id.quickFixButtonRename);
        tvTime = (TextView) findViewById(R.id.tv_time);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        tvTips = (TextView) findViewById(R.id.tv_tips);
    }
}
