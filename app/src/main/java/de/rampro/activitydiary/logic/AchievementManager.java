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

package de.rampro.activitydiary.logic;

import de.rampro.activitydiary.db.LocalDBHelper;
import de.rampro.activitydiary.model.Achievement;

public class AchievementManager {
    private LocalDBHelper dbHelper;

    public AchievementManager(LocalDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void unlockAchievement(int achievementId) {
        // 实现解锁成就的逻辑，更新数据库
        // 这里省略了实际的数据库更新代码
    }

    public void checkAndUnlockAchievements() {
        // 检查解锁条件，如果满足条件，则调用 unlockAchievement()
    }
}
