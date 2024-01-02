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

package de.rampro.activitydiary.ui.generic;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.rampro.activitydiary.R;

public class UserManualActivity extends BaseActivity {

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_manual);

        /*
        Uri data = getIntent().getData();
        so far we do not interpret the URI, because we only use
        de.rampro.activitydiary.privacy_policy://show
        to open this activity
        */
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if(bar != null) bar.setDisplayHomeAsUpEnabled(true);

        TextView manualText = findViewById(R.id.manualTextView);

        String mergedmanualText = "<h1>" + "Vocal Helper" + "</h1>";
        mergedmanualText += "<p>" + "You can click the microphone icon in the main page to use this function. It supports the options as follow:" + "</p>";
        mergedmanualText += "<p><b>" + "Start + activity_name: start some activity." + "</b></p>";
        mergedmanualText += "<p><b>" + "Stop current activity: stop current running activity." + "</b></p>";
        mergedmanualText += "<p><b>" + "Create + activity_name: create a new activity with random color." + "</b></p>";
        mergedmanualText += "<p><b>" + "Delete + activity_name: delete an existing activity." + "</b></p>";
        mergedmanualText += "<p><b>" + "Note + sentence: Add note to current running activity." + "</b></p>";
        mergedmanualText += "<p>" + "Once your voice command is successfully recognized, it will be executed. On the other hand, a pop-up window will pop up with recognizing result in it. You can edit it and execute it again." + "</p>";



//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_intro_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_intro_text1) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_intro_text2) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_intro_text3) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_intro_text4) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_intro_text5) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_what_title) + "</h2>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle1) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText1a) + "</p>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle2) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText2a) + "</p>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle3) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText3a) + "</p>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle4) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText4a) + "</p>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle5) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText5a) + "</p>";
//        mergedmanualText += "<h3>" + getResources().getString(R.string.privacy_what_subTitle6) + "</h3>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText6a) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText6b) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText6c) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_what_subText6d) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_why_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_why_text1) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_why_text2) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_why_text3) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_how_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_how_text1) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_security_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_security_text) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_rights_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_rights_text) + "</p>";
//        mergedmanualText += "<h2>" + getResources().getString(R.string.privacy_contact_title) + "</h2>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_contact_address) + "</p>";
//        mergedmanualText += "<p>" + getResources().getString(R.string.privacy_contact_email) + "</p>";

        if (Build.VERSION.SDK_INT >= 24) {
            manualText.setText(Html.fromHtml(mergedmanualText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            manualText.setText(Html.fromHtml(mergedmanualText));
        }

        manualText.setMovementMethod(LinkMovementMethod.getInstance());

        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onResume(){
        mNavigationView.getMenu().findItem(R.id.nav_manual).setChecked(true);
        super.onResume();
    }
}
