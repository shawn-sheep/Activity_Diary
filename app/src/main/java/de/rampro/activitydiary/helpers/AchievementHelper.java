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

package de.rampro.activitydiary.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.rampro.activitydiary.ActivityDiaryApplication;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContentProvider;
import de.rampro.activitydiary.db.ActivityDiaryContract;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.ui.main.MainActivity;

public class AchievementHelper extends Activity {

    private static Context context;
    public AchievementHelper() {
        AchievementHelper.context = ActivityDiaryApplication.getAppContext();
    }

    public void UpdateAchievements(DiaryActivity current_activity){
        // 当结束的活动是“睡觉”时检查成就
        if (current_activity != null && current_activity.getName().equals("Sleeping")) {
            checkAndUnlockSleepMasterAchievement();
        }
    }

    private void checkAndUnlockSleepMasterAchievement() {
        // 检查过去24小时内的睡眠次数
        int sleepCount = ActivityDiaryContentProvider.countSleepActivitiesInLast24Hours();
        if (sleepCount == 3) {
            unlockAchievement("睡觉大师");
        }
    }


    // 解锁成就的方法
    private void unlockAchievement(String achievementName) {
//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();  // 确保你有对数据库的引用
        // 获取当前时间戳
        long currentTime = System.currentTimeMillis();

        // 构建和执行更新语句
//        String sql = "UPDATE achievements SET unlocked = 1, unlock_time = ? WHERE name = ?";
//        db.execSQL(sql, new Object[]{currentTime, achievementName});

        // 显示通知
//        Context context = AchievementHelper.this;
        Toast.makeText(context, "成就解锁: " + achievementName, Toast.LENGTH_LONG).show();
    }


}


