package de.rampro.activitydiary.ui.generic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import de.rampro.activitydiary.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.model.Achievement;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {

    private final List<Achievement> achievementList;

    AchievementsAdapter(List<Achievement> achievementList) {
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.achievement_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final ImageView unlockImageView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.achievement_title);
            descriptionTextView = itemView.findViewById(R.id.achievement_description);
            unlockImageView = itemView.findViewById(R.id.achievement_unlock_icon);
        }

        void bind(Achievement achievement) {
            titleTextView.setText(achievement.getName());
            descriptionTextView.setText(achievement.getDescription());
            if (achievement.isUnlocked()) {
                unlockImageView.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f); // 不透明显示
            } else {
                unlockImageView.setVisibility(View.INVISIBLE);
                itemView.setAlpha(0.5f); // 半透明显示
            }
        }
    }
}
