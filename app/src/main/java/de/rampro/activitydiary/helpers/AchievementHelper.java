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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.rampro.activitydiary.ActivityDiaryApplication;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContentProvider;
import de.rampro.activitydiary.db.ActivityDiaryContract;
import de.rampro.activitydiary.db.LocalDBHelper;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.ui.main.MainActivity;

public class AchievementHelper extends Activity {

    private static Context context;
    public AchievementHelper() {
        AchievementHelper.context = ActivityDiaryApplication.getAppContext();
    }

    public static Map<String, Long> mp = new HashMap<>();
    public void UpdateAchievements(DiaryActivity current_activity){
        if (current_activity != null) {
            List<Long> achievementIds = ActivityDiaryContentProvider.getAchievementIdsForActivity(current_activity.getId());

            for (Long achievementId : achievementIds) {
                // 对于其他成就的处理
                if (Objects.equals(achievementId, mp.get("睡眠大师"))) { // 假设这是睡眠大师成就的 ID
                    checkAndUnlockSleepMasterAchievement();
                } else if (Objects.equals(achievementId, mp.get("睡眠专家"))) { // 假设这是睡眠专家成就的 ID
                    checkAndUnlockSleepExpertAchievement();
                } else if (Objects.equals(achievementId, mp.get("睡眠传奇"))) { // 假设这是睡眠传奇成就的 ID
                    checkAndUnlockSleepLegendAchievement();
                }
            }
        }
    }



    private void checkAndUnlockSleepMasterAchievement() {
        // 检查过去24小时内的睡眠次数
        int sleepCount = ActivityDiaryContentProvider.countSleepActivitiesInLast24Hours();
        if (sleepCount == 3) {
            unlockAchievement("睡眠大师");
        }
    }

    private void checkAndUnlockSleepExpertAchievement() {
        // 检查过去48小时内的睡眠次数
        int sleepCount = ActivityDiaryContentProvider.countSleepActivitiesInLast48Hours();
        if (sleepCount == 5) {
            unlockAchievement("睡眠专家");
        }
    }

    private void checkAndUnlockSleepLegendAchievement() {
        // 检查过去一周内的总睡眠时间
        int totalSleepHours = ActivityDiaryContentProvider.getTotalSleepHoursInLastWeek();
        if (totalSleepHours >= 50) {
            unlockAchievement("睡眠传奇");
        }
    }


    // 解锁成就的方法
    private void unlockAchievement(String achievementName) {
        // 更新数据库
        ActivityDiaryContentProvider.unlockAchievement_by_Name(achievementName);
        // 显示通知
        Toast.makeText(context, "成就解锁: " + achievementName, Toast.LENGTH_LONG).show();
    }


}


