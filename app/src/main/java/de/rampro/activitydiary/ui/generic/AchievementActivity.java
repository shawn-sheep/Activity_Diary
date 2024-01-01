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
import android.database.Cursor;
import android.os.Bundle;

import androidx.loader.content.Loader;


import de.rampro.activitydiary.R;
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

public class AchievementActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_achievement);

        //mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onResume(){
        mNavigationView.getMenu().findItem(R.id.nav_achievement).setChecked(true);
        super.onResume();
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // 在这里创建并返回一个 Loader<Cursor> 对象
        // 你需要根据你的需求实现这个方法，加载需要的数据
        return null; // 返回 null 是为了示例，实际需要根据你的需求返回一个 Loader 对象
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // 当数据加载完成后，你可以在这里处理加载的数据
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // 当 Loader 被重置时，可以执行一些清理操作
    }
}

