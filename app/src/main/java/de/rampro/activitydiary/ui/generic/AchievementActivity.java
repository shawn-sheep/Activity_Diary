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
package de.rampro.activitydiary.ui.generic;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import de.rampro.activitydiary.R;
import de.rampro.activitydiary.db.ActivityDiaryContentProvider;
import de.rampro.activitydiary.db.LocalDBHelper;
import de.rampro.activitydiary.model.Achievement;
/*
 * ActivityDiary
 *
 * Copyright (C) 2018 Raphael Mack http://www.raphael-mack.de
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

public class AchievementActivity extends BaseActivity {

    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter achievementsAdapter;

//    private ActivityDiaryContentProvider provider; // 假设您有一个DBHelper类处理数据库操作

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_achievement, null, false);

        setContent(contentView); // 设置内容视图

        achievementsRecyclerView = contentView.findViewById(R.id.achievements_recycler_view);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAchievements();
    }

    private void loadAchievements() {
        try {
            // 从数据库中获取所有成就
            List<Achievement> achievements = ActivityDiaryContentProvider.getAllAchievements();
            achievementsAdapter = new AchievementsAdapter(achievements);
            achievementsRecyclerView.setAdapter(achievementsAdapter);
        } catch (Exception e) {
            Log.e("AchievementActivity", "Error loading achievements", e);
            Toast.makeText(this, "Error loading achievements", Toast.LENGTH_SHORT).show();
        }
    }


}


