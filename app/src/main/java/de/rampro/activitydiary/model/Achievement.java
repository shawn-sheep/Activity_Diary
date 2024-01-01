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

package de.rampro.activitydiary.model;

public class Achievement {
    private int id; // 成就的唯一标识符
    private String name; // 成就名称
    private String description; // 成就描述
    private boolean isUnlocked; // 成就是否已解锁
    private long unlockTime; // 成就解锁的时间（时间戳）

    // 构造函数
    public Achievement(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isUnlocked = false; // 初始时，成就未解锁
        this.unlockTime = 0; // 初始时，解锁时间为0
    }

    public Achievement(int id, String name, String description, boolean isUnlocked, long unlockTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isUnlocked = isUnlocked;
        this.unlockTime = unlockTime;
    }
    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
    }

    // 解锁成就的方法
    public void unlock(long currentTime) {
        if (!isUnlocked) {
            isUnlocked = true;
            unlockTime = currentTime;
            // 这里可以添加通知用户成就已解锁的代码
        }
    }

    // 格式化输出成就信息的方法
    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isUnlocked=" + isUnlocked +
                ", unlockTime=" + unlockTime +
                '}';
    }
}

