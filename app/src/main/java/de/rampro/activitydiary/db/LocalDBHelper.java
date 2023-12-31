/*
 * ActivityDiary
 *
 * Copyright (C) 2017-2018 Raphael Mack http://www.raphael-mack.de
 * Copyright (C) 2018 Bc. Ondrej Janitor
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

package de.rampro.activitydiary.db;

import de.rampro.activitydiary.helpers.AchievementHelper;
import de.rampro.activitydiary.model.Achievement.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rampro.activitydiary.model.Achievement;


public class LocalDBHelper extends SQLiteOpenHelper {

    public LocalDBHelper(Context context) {
        super(context, ActivityDiaryContract.AUTHORITY, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTablesForVersion(db, CURRENT_VERSION);

        /* now fill some sample data */
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Gardening', '" + Color.parseColor("#388e3c") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Woodworking', '" + Color.parseColor("#5d4037") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Officework', '" + Color.parseColor("#00796b") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Swimming', '" + Color.parseColor("#0288d1") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Relaxing', '" + Color.parseColor("#fbc02d") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Cooking', '" + Color.parseColor("#e64a19") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Cleaning', '" + Color.parseColor("#CFD8DC") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Cinema', '" + Color.parseColor("#c2185b") + "');");
        db.execSQL("INSERT INTO " +
                ActivityDiaryContract.DiaryActivity.TABLE_NAME +
                "(" + ActivityDiaryContract.DiaryActivity.NAME + "," + ActivityDiaryContract.DiaryActivity.COLOR + ")" +
                " VALUES " +
                " ('Sleeping', '" + Color.parseColor("#303f9f") + "');");

        //初始化成就列表
        Init_Achievement(db);
    }

    public static final int CURRENT_VERSION = 5;
/*
    For debugging sometimes it is handy to drop a table again. This can easily be achieved in onDowngrade,
    after CURRENT_VERSION is decremented again

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE diary_search_suggestions");
    }
*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * The SQLite ALTER TABLE documentation can be found
         * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
         * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
         * you can use ALTER TABLE to rename the old table, then create the new table and then
         * populate the new table with the contents of the old table.
         */
        if (oldVersion == 1) {
            /* upgrade from 1 to current */
            /* still alpha, so just delete and restart */
            /* do not use synmbolic names here, because in case of later rename the old names shall be dropped */
            db.execSQL("DROP TABLE activity");
            db.execSQL("DROP TABLE activity_alias");
            db.execSQL("DROP TABLE condition");
            db.execSQL("DROP TABLE conditions_map");
            db.execSQL("DROP TABLE diary");
            db.execSQL("DROP TABLE achievement");
            onCreate(db);
            oldVersion = CURRENT_VERSION;
        }
//        //创建成就表
//        db.execSQL(SQL_CREATE_ACHIEVEMENTS_TABLE);
//        //初始化成就列表
//        Init_Achievement();
        if (oldVersion < 3) {
            /* upgrade from 2 to 3 */
            createDiaryImageTable(db);
        }
        if (oldVersion < 4) {
            /* upgrade from 3 to 4 */
            createDiaryLocationTable(db);
        }

        if (oldVersion < 5) {
            /* upgrade from 4 to 5 */
            createRecentSuggestionsTable(db);
        }

        if (newVersion > 5) {
            throw new RuntimeException("Database upgrade to version " + newVersion + " nyi.");
        }

        if (oldVersion < CURRENT_VERSION) {
            db.execSQL(SQL_CREATE_ACHIEVEMENTS_TABLE);
        }

    }

    private void createDiaryLocationTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                ActivityDiaryContract.DiaryLocation.TABLE_NAME + " " +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "_deleted INTEGER DEFAULT 0, " +
                "ts INTEGER NOT NULL, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "altitude REAL DEFAULT NULL, " +
                "speed INTEGER DEFAULT NULL," +
                "hacc INTEGER DEFAULT NULL, " +
                "vacc INTEGER DEFAULT NULL, " +
                "sacc INTEGER DEFAULT NULL " +
                ");");
    }

    private void createDiaryImageTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                "diary_image " +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "_deleted INTEGER DEFAULT 0, " +
                "diary_id INTEGER NOT NULL, " +
                "uri TEXT NOT NULL, " +
                " FOREIGN KEY(diary_id) REFERENCES diary(_id)" +
                ");");
    }

    private void createRecentSuggestionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                "diary_search_suggestions" +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "_deleted INTEGER DEFAULT 0, " +
                "action TEXT NOT NULL, " +
                "suggestion TEXT NOT NULL " +
                ");");
    }

    private void createTablesForVersion(SQLiteDatabase db, int version) {
        db.execSQL("CREATE TABLE " +
                "activity " +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "_deleted INTEGER DEFAULT 0, " +
                "name TEXT NOT NULL UNIQUE," +
                "color INTEGER," +
                "parent INTEGER " +
                ");");

        db.execSQL("CREATE TABLE " +
                "diary" +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "_deleted INTEGER DEFAULT 0," +
                "act_id INTEGER NOT NULL, " +
                "start INTEGER NOT NULL, " +
                "'end' INTEGER DEFAULT NULL, " +
                "note TEXT, " +
                " FOREIGN KEY(act_id) REFERENCES activity(_id) " +
                ");");

        //创建成就表
        db.execSQL(SQL_CREATE_ACHIEVEMENTS_TABLE);
        //创建事件关联成就表
        createCorrespondingAchievementsTable(db);
        if (version >= 3) {
            createDiaryImageTable(db);
        }

        if (version >= 4) {
            createDiaryLocationTable(db);
        }

        if (version >= 5) {
            createRecentSuggestionsTable(db);
        }

    }
    private static final String SQL_CREATE_ACHIEVEMENTS_TABLE =
            "CREATE TABLE " + AchievementEntry.TABLE_NAME + " (" +
                    AchievementEntry._ID + " INTEGER PRIMARY KEY," +
                    AchievementEntry.COLUMN_NAME_TITLE + " TEXT," +
                    AchievementEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    AchievementEntry.COLUMN_NAME_UNLOCKED + " INTEGER," +
                    AchievementEntry.COLUMN_NAME_UNLOCK_TIME + " INTEGER)";
    private void createCorrespondingAchievementsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                "CorrespondingAchievements " +
                "(" +
                "_id INTEGER PRIMARY KEY ASC, " +
                "activity_id INTEGER NOT NULL, " +
                "achievement_id INTEGER NOT NULL, " +
                "FOREIGN KEY(activity_id) REFERENCES activity(_id), " +
                "FOREIGN KEY(achievement_id) REFERENCES " + AchievementEntry.TABLE_NAME + "(" + AchievementEntry._ID + ")" +
                ");");
    }
    public static class AchievementEntry implements BaseColumns {
        public static final String TABLE_NAME = "achievement";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_UNLOCKED = "unlocked";
        public static final String COLUMN_NAME_UNLOCK_TIME = "unlock_time";
    }
    public long addAchievement(Achievement achievement) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AchievementEntry.COLUMN_NAME_TITLE, achievement.getName());
        values.put(AchievementEntry.COLUMN_NAME_DESCRIPTION, achievement.getDescription());
        values.put(AchievementEntry.COLUMN_NAME_UNLOCKED, achievement.isUnlocked() ? 1 : 0);
        values.put(AchievementEntry.COLUMN_NAME_UNLOCK_TIME, achievement.getUnlockTime());

        long newRowId = db.insert(AchievementEntry.TABLE_NAME, null, values);
        return newRowId;
    }
    private int getActivityID(SQLiteDatabase db, String activityName) {
        String[] projection = {
                "_id"
        };

        String selection = "name = ?";
        String[] selectionArgs = { activityName };

        Cursor cursor = db.query(
                "activity",   // The table to query
                projection,   // The columns to return
                selection,    // The columns for the WHERE clause
                selectionArgs,// The values for the WHERE clause
                null,         // don't group the rows
                null,         // don't filter by row groups
                null          // The sort order
        );

        int activityID = -1;
        if (cursor != null && cursor.moveToFirst()) {
            activityID = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            cursor.close();
        }
        return activityID;
    }

    private void Init_Achievement(SQLiteDatabase db) {
        // 原有的成就
        addAchievement(db, "睡眠大师", "24小时内连续睡觉三次", Arrays.asList("Sleeping"));
        addAchievement(db, "睡眠专家", "48小时内连续睡觉五次", Arrays.asList("Sleeping"));
        addAchievement(db, "睡眠传奇", "一周内累计睡眠超过50小时", Arrays.asList("Sleeping"));

        //联动成就
        addAchievement(db, "原神启动", "第一次启动原神", Arrays.asList("Genshin"));

        // 新添加的成就
        addAchievement(db, "早起者", "连续7天在早上6点前起床", Arrays.asList("EarlyRising"));
        addAchievement(db, "水下探险家", "一个月内进行5次潜水活动", Arrays.asList("Diving"));
        addAchievement(db, "山地征服者", "攀登三座不同的山峰", Arrays.asList("MountainClimbing"));
        addAchievement(db, "健身狂热", "连续30天进行健身训练", Arrays.asList("Fitness"));
        addAchievement(db, "烹饪新手", "尝试10种不同的烹饪食谱", Arrays.asList("Cooking"));
        addAchievement(db, "步行者", "一天内步行超过20000步", Arrays.asList("Walking"));
        addAchievement(db, "瑜伽达人", "进行一个月的瑜伽练习", Arrays.asList("Yoga"));
        addAchievement(db, "摄影师", "在不同地点拍摄50张照片", Arrays.asList("Photography"));
        addAchievement(db, "环境保护者", "参与5次环境清洁活动", Arrays.asList("EnvironmentalProtection"));
        addAchievement(db, "烘焙大师", "烘焙10种不同的蛋糕", Arrays.asList("Baking"));

    }

    private void addAchievement(SQLiteDatabase db, String title, String description, List<String> activityNames) {
        long achievementId = insert_Achievement_to_DB(db, title, description, 0, 0);
        for (String activityName : activityNames) {
            int activityId = getActivityID(db, activityName);
            if (activityId != -1) {
                insertCorrespondingAchievement(db, activityId, achievementId);
            }
        }
        AchievementHelper.mp.put(title, achievementId);
    }

    private long insert_Achievement_to_DB(SQLiteDatabase db, String title, String description, int unlocked, int unlockTime) {
        ContentValues values = new ContentValues();
        values.put(AchievementEntry.COLUMN_NAME_TITLE, title);
        values.put(AchievementEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(AchievementEntry.COLUMN_NAME_UNLOCKED, unlocked);
        values.put(AchievementEntry.COLUMN_NAME_UNLOCK_TIME, unlockTime);

        return db.insert(AchievementEntry.TABLE_NAME, null, values);
    }

    private void insertCorrespondingAchievement(SQLiteDatabase db, int activityId, long achievementId) {
        if (achievementId == -1) {
            // 如果成就未成功插入，则不插入对应关系
            return;
        }

        ContentValues values = new ContentValues();
        values.put("activity_id", activityId);
        values.put("achievement_id", achievementId);

        long newRowId = db.insert("CorrespondingAchievements", null, values);
        if(newRowId == -1) {
            Log.e("Init_Achievement", "Failed to insert corresponding achievement into the database.");
        } else {
            Log.i("Init_Achievement", "Corresponding achievement inserted with row ID: " + newRowId);
        }
    }

}
