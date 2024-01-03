/*
 * ActivityDiary
 *
 * Copyright (C) 2024 Raphael Mack http://www.raphael-mack.de
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

package de.rampro.activitydiary.helpers;

import static android.os.Environment.getExternalStorageDirectory;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.rampro.activitydiary.ActivityDiaryApplication;
import de.rampro.activitydiary.BuildConfig;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContract;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.helpers.DateHelper;
import de.rampro.activitydiary.helpers.GraphicsHelper;
import de.rampro.activitydiary.helpers.TimeSpanFormatter;
import de.rampro.activitydiary.model.DetailViewModel;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.ui.generic.BaseActivity;
import de.rampro.activitydiary.ui.generic.EditActivity;
import de.rampro.activitydiary.ui.history.HistoryDetailActivity;
import de.rampro.activitydiary.ui.settings.SettingsActivity;
import de.rampro.activitydiary.ui.statistics.DetailActivity;

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import de.rampro.activitydiary.ui.main.JsonParser;

import static de.rampro.activitydiary.model.conditions.Condition.mOpenHelper;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class VoiceAssistantHelper extends AppCompatActivity {
    private SpeechRecognizer mIat;// 语音听写对象
    private RecognizerDialog mIatDialog;// 语音听写UI

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mSharedPreferences;//缓存

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private String resultType = "json";//结果内容数据格式
    private static final String TAG = "114514";
    private Context thiscontext;
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS){
                showMsg("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };
    public VoiceAssistantHelper(android.content.Context context){
        thiscontext = context;
        mIat = SpeechRecognizer.createRecognizer(thiscontext, mInitListener);
        mIatDialog = new RecognizerDialog(thiscontext,mInitListener);
        mSharedPreferences = getSharedPreferences("ASR", Activity.MODE_PRIVATE);

    }
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
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
        String res = resultBuffer.toString();

    }
    private void showMsg(String msg){
        Toast.makeText(thiscontext,msg,Toast.LENGTH_SHORT).show();
    }
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results,boolean isLast) {
            if(!isLast){
                printResult(results);
            }
        }
        public void onError(SpeechError error){
            showMsg(error.getPlainDescription(true));
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
}
