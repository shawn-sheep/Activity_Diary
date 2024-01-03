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

import static de.rampro.activitydiary.model.conditions.Condition.mOpenHelper;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.rampro.activitydiary.ActivityDiaryApplication;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContract;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.helpers.GraphicsHelper;
import de.rampro.activitydiary.helpers.JaroWinkler;
import de.rampro.activitydiary.model.DetailViewModel;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.ui.generic.BaseActivity;
import de.rampro.activitydiary.ui.main.JsonParser;
import de.rampro.activitydiary.ui.main.MainActivity;
import de.rampro.activitydiary.ui.main.NoteEditDialog;


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
    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mSharedPreferences;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String resultType = "json";//结果内容数据格式
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    private SearchView searchView;
    private DetailViewModel viewModel;

    private void setCheckState(int checkState) {
        this.checkState = checkState;
        if (checkState == CHECK_STATE_CHECKING && mActivityNameTIL != null) {
            mActivityNameTIL.setError("...");
        }
    }
    private void showMsg(String msg){
        Toast.makeText(DetailActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    private void setBtnTooltip(View view, @Nullable CharSequence tooltipText) {
        if (Build.VERSION.SDK_INT < 26) {
            TooltipCompat.setTooltipText(view, tooltipText);
        } else {
            view.setTooltipText(tooltipText);
        }
    }
    private static final String TAG = DetailActivity.class.getSimpleName();

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
    public void setParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);
        mIat.setParameter(SpeechConstant.LANGUAGE,"en_us");
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }

    private FloatingActionButton fabVocalHelper;
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS){
                showMsg("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MediaPlayer.create(this, R.raw.kiring);
        viewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        mIat = SpeechRecognizer.createRecognizer(DetailActivity.this, mInitListener);
        mIatDialog = new RecognizerDialog(DetailActivity.this,mInitListener);
        mSharedPreferences = getSharedPreferences("ASR", Activity.MODE_PRIVATE);
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
        this.baseTimer = SystemClock.elapsedRealtime();

        ivPlay.setImageResource(R.drawable.baseline_not_started_24);
        isStart = true;

        // 设置播放按钮的点击监听器

        if (savedInstanceState != null) {
            // 恢复保存的状态
            // ...
        } else {
            // 从头开始计时
            startTimer();
        }
        findViewById(R.id.iv_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimer();
            }
        });
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

    private String[] format(String[] params){
        List<String> res = new ArrayList<>();
        for(String param: params){
            param = param.trim();
            if(param.length() > 0 && (param.charAt(param.length()-1) == '.' || param.charAt(param.length()-1) == ','))
                param = param.substring(0, param.length()-1);
            if(!param.isEmpty()) {
                param = param.toLowerCase();
                param = Character.toUpperCase(param.charAt(0)) + param.substring(1);
                res.add(param);
            }
        }
        return res.toArray(new String[0]);
    }
    private static long convertEnglishToArabic(String englishNumber) {
        if(englishNumber.equals("One"))
            return 1;
        else if(englishNumber.equals("Two"))
            return 2;
        else if(englishNumber.equals("Three"))
            return 3;
        else if(englishNumber.equals("Four"))
            return 4;
        else if(englishNumber.equals("Five"))
            return 5;
        else if(englishNumber.equals("Six"))
            return 6;
        else if(englishNumber.equals("Seven"))
            return 7;
        else if(englishNumber.equals("Eight"))
            return 8;
        else if(englishNumber.equals("Nine"))
            return 9;
        else
            return -1;
    }
    private String recoverToSentence(String[] params){
        StringBuilder res = new StringBuilder();
        for(int i = 1; i < params.length; i++){
            String tmp;
            if(i == 1)
                tmp = Character.toUpperCase(params[i].charAt(0))+params[i].substring(1);
            else
                tmp = params[i].toLowerCase();
            res.append(tmp);
            if(i != params.length - 1)
                res.append(" ");
            else
                res.append(".");
        }
        return res.toString();
    }
    private boolean process(String res){
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] params = format(res.split(" "));
        if(params[0].equals("Stop")){
            if(params[1].equals("Current") && params[2].equals("Activity")){
                if(ActivityHelper.helper.getCurrentActivity() != null){
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    ActivityHelper.helper.setCurrentActivity(null);
                    finish();
                }
                else
                    showMsg("No current running activity.");
            }
        }
        else if(params[0].equals("Countdown") && params.length == 3){
            if(ActivityHelper.helper.getCurrentActivity() != null){
                double n = 0;
                try{
                    n = Double.parseDouble(params[1]);
                }
                catch (NumberFormatException e) {
                    n = convertEnglishToArabic(params[1]);
                    if(n == -1){
                        showMsg("You must countdown an integer number of time.");
                        return false;
                    }
                }
                int f=1000;
                if(params[2].equals("Seconds"))
                    f*=1;
                else if(params[2].equals("Minutes"))
                    f*=60;
                else if(params[2].equals("Hours"))
                    f*=3600;
                else{
                    showMsg("The unit of time must be seconds, minutes or hours");
                    return false;
                }
                startCountDown((long)(n*f));
                showMsg("Countdown  start.");
            }
            else{
                showMsg("No activity running now.");
            }
        }
        else if(params[0].equals("Note")){
            if(ActivityHelper.helper.getCurrentActivity() != null){
                String content = recoverToSentence(params);
                NoteEditDialog dialog = new NoteEditDialog();
                dialog.setText(viewModel.mNote.getValue() + content);
                dialog.show(getSupportFragmentManager(), "NoteEditDialogFragment");
            } else
                showMsg("No current running activity!");
        }
        else{
            showMsg("Undefined Option");
            return false;
        }
        return true;
    }
    private void startCountDown(long millisInFuture) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        initCountDownTimer(millisInFuture);
        countDownTimer.start();
    }
    private void initCountDownTimer(long initialMillis) {
        countDownTimer = new CountDownTimer(initialMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                playMusic();
            }
        };
    }
    private void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        showAlertDialog();
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The countdown time has expired. Stop current activity?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopMusic();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                ActivityHelper.helper.setCurrentActivity(null);
                dialog.dismiss();
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopMusic();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, R.raw.kiring);
        }
    }
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

//        tvResult.setText(resultBuffer.toString());//听写结果显示
//        showMsg(resultBuffer.toString());


        String res = resultBuffer.toString();
        if(!process(res)){
            final View DialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edit_iat_res,null);
            final EditText editText= (EditText) DialogView.findViewById(R.id.iat_res);
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
            builder.setTitle("IatResult").setView(DialogView);
            editText.setText(res);
            editText.setSelection(editText.getText().length());
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    String final_res = editText.getText().toString();
                    process(final_res);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    dialog.cancel();
                }
            });

            builder.create().show();
        }
    }
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            if(!isLast){
                printResult(results);
            }
        }
        public void onError(SpeechError error){
            showMsg(error.getPlainDescription(true));
        }
    };
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        ActivityHelper.helper.setCurrentActivity(null);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIat != null) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mediaPlayer.release();
        if(ActivityHelper.helper.getCurrentActivity()!=null)
            ActivityHelper.helper.setCurrentActivity(null);

        stopTimer(); // 在销毁活动时停止计时器

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
        fabVocalHelper = (FloatingActionButton) findViewById(R.id.vocal_helper_2);
        fabVocalHelper.setOnClickListener(v->{
            if( null == mIat){
                showMsg("wrong");
                return;
            }
            mIatResults.clear();
            setParam();
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
        });
    }
}
