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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rampro.activitydiary.R;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.model.Achievement;
import de.rampro.activitydiary.model.DiaryActivity;

import static android.app.SearchManager.SUGGEST_COLUMN_ICON_1;
import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_QUERY;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
import static de.rampro.activitydiary.model.conditions.Condition.mOpenHelper;

/*
 * Why a new Content Provider for Diary Activities?
 *
 * According https://developer.android.com/guide/topics/providers/content-provider-creating.html
 * we need it to do searching, syncing or widget use of the data -> which in the long we all want to do.
 *
 * Additionally it is used as SearchProvider these days.
 * */
public class ActivityDiaryContentProvider extends ContentProvider {

    private static final int activities = 1;
    private static final int activities_ID = 2;
    private static final int conditions = 3;
    private static final int conditions_ID = 4;
    private static final int diary = 5;
    private static final int diary_ID = 6;
    private static final int diary_image = 7;
    private static final int diary_image_ID = 8;
    private static final int diary_location = 9;
    private static final int diary_location_ID = 10;
    private static final int diary_stats = 11;
    private static final int search_recent_suggestion = 12;
    private static final int search_suggestion = 13;
    private static final int diary_suggestion = 14;

    private static final String TAG = ActivityDiaryContentProvider.class.getName();

    public static final String SEARCH_ACTIVITY = "de.rampro.activitydiary.action.SEARCH_ACTIVITY";
    public static final String SEARCH_NOTE = "de.rampro.activitydiary.action.SEARCH_NOTE";
    public static final String SEARCH_GLOBAL = "de.rampro.activitydiary.action.SEARCH_GLOBAL";
    public static final String SEARCH_DATE = "de.rampro.activitydiary.action.SEARCH_DATE";

    // TODO: isn't this already somewhere else?
    public static final Uri SEARCH_URI = Uri.parse("content://" + ActivityDiaryContract.AUTHORITY);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryActivity.CONTENT_URI.getPath().replaceAll("^/+", ""), activities);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryActivity.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#", activities_ID);

        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.Diary.CONTENT_URI.getPath().replaceAll("^/+", ""), diary);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.Diary.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#", diary_ID);

        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryImage.CONTENT_URI.getPath().replaceAll("^/+", ""), diary_image);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryImage.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#", diary_image_ID);

        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryStats.CONTENT_URI.getPath().replaceAll("^/+", ""), diary_stats);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryStats.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#/#", diary_stats);

        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryLocation.CONTENT_URI.getPath().replaceAll("^/+", ""), diary_location);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryLocation.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#", diary_location_ID);

        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryLocation.CONTENT_URI.getPath().replaceAll("^/+", ""), diary_location);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiaryLocation.CONTENT_URI.getPath().replaceAll("^/+", "") + "/#", diary_location_ID);
// TODO:
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, "history/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/", search_recent_suggestion);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, "history/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", search_suggestion);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, ActivityDiaryContract.DiarySearchSuggestion.CONTENT_URI.getPath().replaceAll("^/+", ""), diary_suggestion);

        /* TODO #18 */
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, "conditions", conditions);
        sUriMatcher.addURI(ActivityDiaryContract.AUTHORITY, "conditions/#", conditions_ID);

    }

    private static LocalDBHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new LocalDBHelper(getContext());
        return true; /* successfully loaded */
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        boolean useRawQuery = false;
        String grouping = null;
        String sql = "";
        Cursor c;
        int id = 0;
        if(selection == null){
            selection = "";
        }

        MatrixCursor result = new MatrixCursor(new String[]{
                BaseColumns._ID,
                SUGGEST_COLUMN_TEXT_1,
                SUGGEST_COLUMN_ICON_1,
                SUGGEST_COLUMN_INTENT_ACTION,
                SUGGEST_COLUMN_INTENT_DATA,
                SUGGEST_COLUMN_QUERY
        });

        if (sUriMatcher.match(uri) < 1) {
            /* URI is not recognized, return an empty Cursor */
            return null;
        }
        switch (sUriMatcher.match(uri)) {
            case activities_ID:
            case conditions_ID:
            case diary_ID:
            case diary_image_ID:
            case diary_location_ID:
                if (selection != null) {
                    selection = selection + " AND ";
                } else {
                    selection = "";
                }
                selection = selection + "_id=" + uri.getLastPathSegment();
            default:
                /* empty */
        }

        switch (sUriMatcher.match(uri)) {
            case activities_ID: /* intended fall through */
            case activities:
                int n;
                boolean hasDiaryJoin = false;
                String tables = ActivityDiaryContract.DiaryActivity.TABLE_NAME;
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ActivityDiaryContract.DiaryActivity.SORT_ORDER_DEFAULT;
                }
                n = 0;
                while(n < projection.length){
                    if(ActivityDiaryContract.DiaryActivity.X_AVG_DURATION.equals(projection[n])){
                        projection[n] = "AVG(" + ActivityDiaryContract.Diary.END + " - "
                                + ActivityDiaryContract.Diary.START + ") AS "
                                + ActivityDiaryContract.DiaryActivity.X_AVG_DURATION;
                        hasDiaryJoin = true;
                    }
                    if(ActivityDiaryContract.DiaryActivity.X_START_OF_LAST.equals(projection[n])){
                        projection[n] = "xx_start AS "
                                + ActivityDiaryContract.DiaryActivity.X_START_OF_LAST;
                        hasDiaryJoin = true;
                    }
                    n++;
                }
                if(hasDiaryJoin){
                    n = 0;
                    while(n < projection.length) {
                        if(ActivityDiaryContract.DiaryActivity._ID.equals(projection[n])){
                            projection[n] = ActivityDiaryContract.DiaryActivity.TABLE_NAME + "."
                                    + ActivityDiaryContract.DiaryActivity._ID;
                        }
                        n++;
                    }
                    selection = selection.replaceAll(" " + ActivityDiaryContract.DiaryActivity._ID, " " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID);
                    selection = selection.replaceAll(ActivityDiaryContract.DiaryActivity._DELETED, ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._DELETED);

                    tables = tables + ", " + ActivityDiaryContract.Diary.TABLE_NAME;
                    tables = tables + ", (SELECT xx_ref, " + ActivityDiaryContract.Diary.START + " as xx_start FROM " + ActivityDiaryContract.Diary.TABLE_NAME + ","
                                    +     "(SELECT " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID + " AS xx_ref,"
                                                 + " MAX(" + ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.END + ") AS xx_ref_end"
                                    +     " FROM " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + ", " + ActivityDiaryContract.Diary.TABLE_NAME
                                    +     " WHERE " +  ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.ACT_ID
                                    +           " = " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID
                                    +     " GROUP BY " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID
                                    +     ")"
                                    +    " WHERE " +  ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.END + " = xx_ref_end"
                                    +  ")"
                                        ;

                    selection = selection + " AND " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID + " = " + ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.ACT_ID
                                          + " AND " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID + " = xx_ref";

                    grouping = ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID;

                }
                qBuilder.setTables(tables);
                break;

            case diary_image_ID: /* intended fall through */
            case diary_image:
                qBuilder.setTables(ActivityDiaryContract.DiaryImage.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = ActivityDiaryContract.DiaryImage.SORT_ORDER_DEFAULT;
                break;
            case diary_location_ID: /* intended fall through */
            case diary_location:
                qBuilder.setTables(ActivityDiaryContract.DiaryLocation.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = ActivityDiaryContract.DiaryLocation.SORT_ORDER_DEFAULT;
                break;
            case diary_ID: /* intended fall through */
            case diary:
                /* rewrite projection, to prefix with tables */
                qBuilder.setTables(ActivityDiaryContract.Diary.TABLE_NAME + " INNER JOIN " +
                        ActivityDiaryContract.DiaryActivity.TABLE_NAME + " ON " +
                        ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.ACT_ID + " = " +
                        ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID
                );
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = ActivityDiaryContract.Diary.SORT_ORDER_DEFAULT;
                break;
            case diary_stats:
                useRawQuery = true;
                List<String> l = uri.getPathSegments();
                String start;
                String end;

                if(l.size() == 3){
                    // we have a range query with start and end timestamps here
                    start = l.get(1);
                    end = l.get(2);
                }else{
                    start = "0";
                    end = "6156000000000"; // this is roughly 200 year since epoch, congratulations if this lasted so long...
                }

                String subselect = "SELECT SUM(MIN(IFNULL(" + ActivityDiaryContract.Diary.END + ",strftime('%s','now') * 1000), " + end + ") - "
                        + "MAX(" + ActivityDiaryContract.Diary.START + ", " + start + ")) from " + ActivityDiaryContract.Diary.TABLE_NAME
                        + " WHERE ((start >= " + start + " AND start < " + end + ") OR (end > " + start + " AND end <= " + end + ") OR (start < " + start + " AND end > " + end + "))";

                if (selection != null && selection.length() > 0) {
                    subselect += " AND (" + selection + ")";
                }

                sql = "SELECT " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity.NAME + " as " + ActivityDiaryContract.DiaryStats.NAME
                        + ", " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity.COLOR + " as " + ActivityDiaryContract.DiaryStats.COLOR
                        + ", SUM(MIN(IFNULL(" + ActivityDiaryContract.Diary.END + ",strftime('%s','now') * 1000), " + end + ") - MAX(" + start + ", " + ActivityDiaryContract.Diary.START + ")) as " + ActivityDiaryContract.DiaryStats.DURATION
                        + ", (SUM(MIN(IFNULL(" + ActivityDiaryContract.Diary.END + ",strftime('%s','now') * 1000), " + end + ") - MAX(" + start + ", " + ActivityDiaryContract.Diary.START + ")) * 100.0 " +
                        "/ (" + subselect + ")) as " + ActivityDiaryContract.DiaryStats.PORTION
                        + " FROM " + ActivityDiaryContract.Diary.TABLE_NAME + ", " + ActivityDiaryContract.DiaryActivity.TABLE_NAME
                        + " WHERE " + ActivityDiaryContract.Diary.TABLE_NAME + "." + ActivityDiaryContract.Diary.ACT_ID + " = " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID + " AND"
                        + " ((start >= " + start + " AND start < " + end + ") OR (end > " + start + " AND end <= " + end + ") OR (start < " + start + " AND end > " + end + "))"
                ;
                if(selection != null && selection.length() > 0) {
                    sql += " AND (" + selection + ")";
                    String[] newArgs = Arrays.copyOf(selectionArgs, selectionArgs.length * 2);
                    System.arraycopy(selectionArgs, 0, newArgs, selectionArgs.length, selectionArgs.length);
                    selectionArgs = newArgs;
                }
                sql += " GROUP BY " + ActivityDiaryContract.DiaryActivity.TABLE_NAME + "." + ActivityDiaryContract.DiaryActivity._ID;
                if (sortOrder != null && sortOrder.length() > 0) {
                    sql += " ORDER by " + sortOrder;
                }
                break;

            case search_recent_suggestion:

                sql = "SELECT " + ActivityDiaryContract.DiarySearchSuggestion.SUGGESTION + ", " +
                        ActivityDiaryContract.DiarySearchSuggestion.ACTION + " FROM " +
                        ActivityDiaryContract.DiarySearchSuggestion.TABLE_NAME +
                        " ORDER BY " + ActivityDiaryContract.DiarySearchSuggestion._ID + " DESC";

                c = mOpenHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
                if (c != null && c.moveToFirst()) {
                    do {
                        Object icon = null;
                        String action = c.getString(1);
                        String q = c.getString(0); // what do we want to display

                        if(action.equals(SEARCH_ACTIVITY)) {
                            /* icon stays null */
                            int i = Integer.parseInt(q);
                            q = ActivityHelper.helper.activityWithId(i).getName();
                        }else if(action.equals(SEARCH_NOTE)){
                            q = getContext().getResources().getString(R.string.search_notes, q);
                            icon = R.drawable.ic_search;
                        }else if(action.equals(SEARCH_GLOBAL) || action.equals(Intent.ACTION_SEARCH)){
                            q = getContext().getResources().getString(R.string.search_diary, q);
                            icon = R.drawable.ic_search;
                        }else if(action.equals(SEARCH_DATE)){
                            q = getContext().getResources().getString(R.string.search_date, q);
                            icon = R.drawable.ic_calendar;
                        }

                        result.addRow(new Object[]{id++,
                                q,
                                /* icon */ icon,
                                /* intent action */ action,
                                /* intent data */ Uri.withAppendedPath(SEARCH_URI, c.getString(0)),
                                /* rewrite query */c.getString(0)
                        });
                    } while (c.moveToNext());
                }

                return result;


            case search_suggestion:
                String query = uri.getLastPathSegment(); //.toLowerCase();

                if (query != null && query.length() > 0) {
                    // activities matching the current search
                    ArrayList<DiaryActivity> filtered = ActivityHelper.helper.sortedActivities(query);

                    // TODO: make the amount of activities shown configurable
                    for (int i = 0; i < 3; i++) {
                        if (i < filtered.size()) {
                            result.addRow(new Object[]{id++,
                                    filtered.get(i).getName(),
                                    /* icon */ null,
                                    /* intent action */ SEARCH_ACTIVITY,
                                    /* intent data */ Uri.withAppendedPath(SEARCH_URI, Integer.toString(filtered.get(i).getId())),
                                    /* rewrite query */filtered.get(i).getName()
                            });
                        }
                    }
                    // Notes
                    result.addRow(new Object[]{id++,
                            getContext().getResources().getString(R.string.search_notes, query),
                            /* icon */ R.drawable.ic_search,
                            /* intent action */ SEARCH_NOTE,
                            /* intent data */ Uri.withAppendedPath(SEARCH_URI, query),
                            /* rewrite query */ query
                    });

                    // Global search
                    result.addRow(new Object[]{id++,
                            getContext().getResources().getString(R.string.search_diary, query),
                            /* icon */ R.drawable.ic_search,
                            /* intent action */ SEARCH_GLOBAL,
                            /* intent data */ Uri.withAppendedPath(SEARCH_URI, query),
                            /* rewrite query */ query
                    });

                    // Date
                    result.addRow(new Object[]{id++,
                            getContext().getResources().getString(R.string.search_date, query),
                            /* icon */ R.drawable.ic_calendar,
                            /* intent action */ SEARCH_DATE,
                            /* intent data */ Uri.withAppendedPath(SEARCH_URI, query),
                            /* rewrite query */ query
                    });

                    // has Pictures
                    // TODO: add picture search

                    // Location (GPS)
                    // TODO: add location search

                }
                return result;

            case conditions_ID:
                /* intended fall through */
            case conditions:
//                qBuilder.setTables(ActivityDiaryContract.Condition.TABLE_NAME);
                /* TODO #18               if (TextUtils.isEmpty(sortOrder)) sortOrder = ActivityDiaryContract.Conditions.SORT_ORDER_DEFAULT; */
            default:
                /* empty */
        }

        if (useRawQuery) {
            c = mOpenHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
        } else {
            c = qBuilder.query(mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    grouping,
                    null,
                    sortOrder);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case activities:
                return ActivityDiaryContract.DiaryActivity.CONTENT_TYPE;
            case activities_ID:
                return ActivityDiaryContract.DiaryActivity.CONTENT_ITEM_TYPE;
            case diary:
                return ActivityDiaryContract.Diary.CONTENT_TYPE;
            case diary_ID:
                return ActivityDiaryContract.Diary.CONTENT_ITEM_TYPE;
            case diary_location:
                return ActivityDiaryContract.DiaryLocation.CONTENT_TYPE;
            case diary_location_ID:
                return ActivityDiaryContract.DiaryLocation.CONTENT_ITEM_TYPE;
            case diary_stats:
                return ActivityDiaryContract.DiaryStats.CONTENT_TYPE;
            // TODO #18: add other types
            default:
                Log.e(TAG, "MIME type for " + uri + " not defined.");
                return "";
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table;
        Uri resultUri;

        switch (sUriMatcher.match(uri)) {
            case activities:
                table = ActivityDiaryContract.DiaryActivity.TABLE_NAME;
                resultUri = ActivityDiaryContract.DiaryActivity.CONTENT_URI;
                break;
            case diary:
                table = ActivityDiaryContract.Diary.TABLE_NAME;
                resultUri = ActivityDiaryContract.Diary.CONTENT_URI;
                break;
            case diary_image:
                table = ActivityDiaryContract.DiaryImage.TABLE_NAME;
                resultUri = ActivityDiaryContract.DiaryImage.CONTENT_URI;
                break;
            case diary_location:
                table = ActivityDiaryContract.DiaryLocation.TABLE_NAME;
                resultUri = ActivityDiaryContract.DiaryLocation.CONTENT_URI;
                break;
            case diary_suggestion:
                table = ActivityDiaryContract.DiarySearchSuggestion.TABLE_NAME;
                resultUri = ActivityDiaryContract.DiarySearchSuggestion.CONTENT_URI;
                break;
            case conditions:
//                table = ActivityDiaryContract.Condition.TABLE_NAME;
// TODO #18               resultUri = ActivityDiaryContract.Condition.CONTENT_URI;
//                break;
            case diary_stats: /* intended fall-through */
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for insertion: " + uri);
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long id = db.insertOrThrow(table,
                null,
                values);
        if(id > 0) {
            resultUri = ContentUris.withAppendedId(resultUri, id);
            getContext().
                    getContentResolver().
                    notifyChange(resultUri, null);

            return resultUri;
        } else {
            throw new SQLException(
                    "Problem while inserting into uri: " + uri + " values " + values.toString());
        }
    }

    /**
     * Implement this to handle requests to delete one or more rows.
     * The implementation should apply the selection clause when performing
     * deletion, allowing the operation to affect multiple rows in a directory.
     * As a courtesy, call ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()
     * after deleting.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     * <p>
     * <p>The implementation is responsible for parsing out a row ID at the end
     * of the URI, if a specific row is being deleted. That is, the client would
     * pass in <code>content://contacts/people/22</code> and the implementation is
     * responsible for parsing the record number (22) when creating a SQL statement.
     *
     * @param uri           The full URI to query, including a row ID (if a specific record is requested).
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs
     * @return The number of rows affected.
     * @throws SQLException
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        boolean isGlobalDelete = false;
        String table;
        ContentValues values = new ContentValues();
        switch (sUriMatcher.match(uri)) {
            case activities_ID:
                table = ActivityDiaryContract.DiaryActivity.TABLE_NAME;
                break;
            case diary:
                isGlobalDelete = true;
                /* fall though */
            case diary_ID:
                table = ActivityDiaryContract.Diary.TABLE_NAME;
                break;
            case diary_image:
                isGlobalDelete = true;
                /* fall though */
            case diary_image_ID:
                table = ActivityDiaryContract.DiaryImage.TABLE_NAME;
                break;
            case diary_location:
                isGlobalDelete = true;
                /* fall though */
            case diary_location_ID:
                table = ActivityDiaryContract.DiaryLocation.TABLE_NAME;
                break;
            case diary_suggestion:
                table = ActivityDiaryContract.DiarySearchSuggestion.TABLE_NAME;
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                return db.delete(table, selection, selectionArgs);
            case conditions_ID:
//                table = ActivityDiaryContract.Condition.TABLE_NAME;
//                break;
            case diary_stats: /* intended fall-through */
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for deletion: " + uri);
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (!isGlobalDelete) {
            if (selection != null) {
                selection = selection + " AND ";
            } else {
                selection = "";
            }
            selection = selection + "_id=" + uri.getLastPathSegment();
        }
        values.put(ActivityDiaryContract.DiaryActivity._DELETED, "1");

        int upds = db.update(table,
                values,
                selection,
                selectionArgs);
        if (upds > 0) {
            getContext().
                    getContentResolver().
                    notifyChange(uri, null);

        } else {
            Log.i(TAG, "Could not delete anything for uri: " + uri + " with selection '" + selection + "'");
        }
        return upds;
    }

    /**
     * Implement this to handle requests to update one or more rows.
     * The implementation should update all rows matching the selection
     * to set the columns according to the provided values map.
     * As a courtesy, call ContentResolver#notifyChange(Uri, ContentObserver) notifyChange()
     * after updating.
     * This method can be called from multiple threads, as described in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
     * and Threads</a>.
     *
     * @param uri           The URI to query. This can potentially have a record ID if this
     *                      is an update request for a specific record.
     * @param values        A set of column_name/value pairs to update in the database.
     *                      This must not be {@code null}.
     * @param selection     An optional filter to match rows to update.
     * @param selectionArgs
     * @return the number of rows affected.
     */
    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table;
        boolean isID = false;
        switch (sUriMatcher.match(uri)) {
            case activities_ID:
                isID = true;
                table = ActivityDiaryContract.DiaryActivity.TABLE_NAME;
                break;
            case diary_ID:
                isID = true;
            case diary:
                table = ActivityDiaryContract.Diary.TABLE_NAME;
                break;
            case diary_image:
                table = ActivityDiaryContract.DiaryImage.TABLE_NAME;
                break;
            case diary_location_ID:
                isID = true;
            case diary_location:
                table = ActivityDiaryContract.DiaryLocation.TABLE_NAME;
                break;
            case conditions_ID:
                isID = true;
//                table = ActivityDiaryContract.Condition.TABLE_NAME;
//                break;
            case diary_stats: /* intended fall-through */
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for update: " + uri);
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (isID) {
            if (selection != null) {
                selection = selection + " AND ";
            } else {
                selection = "";
            }
            selection = selection + "_id=" + uri.getLastPathSegment();
        }

        int upds = db.update(table,
                values,
                selection,
                selectionArgs);
        if (upds > 0) {
            getContext().
                    getContentResolver().
                    notifyChange(uri, null);

        } else if (isID) {
            throw new SQLException(
                    "Problem while updating uri: " + uri + " with selection '" + selection + "'");
        }
        return upds;
    }

    public void resetDatabase() {
        mOpenHelper.close();
    }


    /**
     * Search for all dates in database which match start/end date or are in range (between start and end date)
     * @param dateInMillis - date is searched
     * @return query (string) with ids that fulfills defined conditions
     */
    public String searchDate(Long dateInMillis) {
        // TODO: move this into the method query, for the case diary,
        // similar to diary_stats, we can modify selection and selection args there
        // or maybe better, invent a new URI like "diary/number" where number is the dateInMillis
        // Alternative: move all this directly into HistoryActivity.onCreateLoader

        String querySelection = " ", id;
        long searchedValue = dateInMillis;
        long searchedValuePlusDay = searchedValue + 86400000; // TODO: replace magic numbers by the formula to calculate them...
        long searchSpecialCase = searchedValue + 86399999;  //used for searching for still running activity
        Cursor allRowsStart = null;

        try {
// TODO: -> this query should not be executed outside of the method ActivityDiaryContentProvider.query
            allRowsStart = mOpenHelper.getReadableDatabase().rawQuery(
                "SELECT " + ActivityDiaryContract.Diary._ID
                    + " FROM " + ActivityDiaryContract.Diary.TABLE_NAME
                        + " WHERE " + "(" + searchedValue + " >= " + ActivityDiaryContract.Diary.START + " AND " + searchedValue + " <= " + ActivityDiaryContract.Diary.END + ")" + " OR " +
                                      "(" + searchedValuePlusDay + " >= " + ActivityDiaryContract.Diary.START + " AND " + searchedValuePlusDay + " <= " + ActivityDiaryContract.Diary.END + ")" + " OR " +
                                      "(" + searchedValue + " < " + ActivityDiaryContract.Diary.START + " AND " + searchedValuePlusDay + " > " + ActivityDiaryContract.Diary.END + ")" + " OR " +
                                      "(" + searchSpecialCase + " >= " + ActivityDiaryContract.Diary.START + " AND " + ActivityDiaryContract.Diary.END + " IS NULL" + ")", null);

            if (allRowsStart.moveToFirst()) {
                do {
                    for (String name : allRowsStart.getColumnNames()) {
                           id =  (allRowsStart.getString(allRowsStart.getColumnIndex(name)));
                           querySelection += querySelection.equals(" ") ?  ActivityDiaryContract.Diary.TABLE_NAME + "." +  ActivityDiaryContract.Diary._ID + " =" + id : " OR " + ActivityDiaryContract.Diary.TABLE_NAME + "." +  ActivityDiaryContract.Diary._ID + " =" + id ;
                    }
                } while (allRowsStart.moveToNext());
            }
        } catch (Exception e) {
            // TODO: add proper exception handling. Also "Exception" seems quite generic -> catch all exceptions that can occur directly
        }finally{
            if(allRowsStart != null){
                allRowsStart.close();
            }
        }

        // if there is no matching dates it returns query which links to find nothings
        // otherwise it will return query with IDs of matching dates
        return querySelection.equals(" ") ?  " start=null" : querySelection;
    }

    public static int countSleepActivitiesInLast24Hours() {
        int count = 0;
//        if(mOpenHelper == null)onCreate();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // 获取当前时间和24小时前的时间戳
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;

        // 构建查询
        String query = "SELECT COUNT(*) FROM diary d " +
                "JOIN activity a ON d.act_id = a._id " +
                "WHERE d.start >= ? AND d.'end' <= ? AND a.name = 'Sleeping' AND a._deleted = 0";

        // 执行查询
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(oneDayAgo), String.valueOf(currentTime) });

        // 处理结果
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

//    public static List<Achievement> getAllAchievements() {
//        List<Achievement> achievements = new ArrayList<>();
//        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//
//        Cursor cursor = db.query(
//                LocalDBHelper.AchievementEntry.TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null);
//
//        while(cursor.moveToNext()) {
//            int id = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry._ID));
//            String name = cursor.getString(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_TITLE));
//            String description = cursor.getString(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_DESCRIPTION));
//            boolean isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED)) == 1;
//            long unlockTime = cursor.getLong(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME));
//
//            Achievement achievement = new Achievement(id, name, description);
//            achievement.setUnlocked(isUnlocked);
//            achievement.setUnlockTime(unlockTime);
//
//            achievements.add(achievement);
//        }
//        cursor.close();
//
//        return achievements;
//    }
public static List<Achievement> getAllAchievements() {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    List<Achievement> achievements = new ArrayList<>();
    final String[] projection = {
            LocalDBHelper.AchievementEntry._ID,
            LocalDBHelper.AchievementEntry.COLUMN_NAME_TITLE,
            LocalDBHelper.AchievementEntry.COLUMN_NAME_DESCRIPTION,
            LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED,
            LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME
    };

    // 查询数据库
    try (Cursor cursor = db.query(
            LocalDBHelper.AchievementEntry.TABLE_NAME,  // 表名
            projection,                  // 列名
            null,                        // 列的选择标准
            null,                        // 选择标准的参数
            null,                        // group by
            null,                        // having
            null                         // order by
    )) {
        // 遍历查询结果
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_DESCRIPTION));
            boolean unlocked = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED)) > 0;
            long unlockTime = cursor.getLong(cursor.getColumnIndexOrThrow(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME));

            // 创建成就对象并添加到列表
            Achievement achievement = new Achievement(id, title, description, unlocked, unlockTime);
            achievements.add(achievement);
        }
    } catch (Exception e) {
        // 处理可能发生的异常，如数据库未打开等
        e.printStackTrace();
    }

    return achievements;
}

//有bug，不能使用
    public static void unlockAchievement_by_Name(String achievementName) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED, 1); // 将解锁状态设置为true
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME, System.currentTimeMillis()); // 设置解锁时间

        String selection = LocalDBHelper.AchievementEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { achievementName };

        try {
            int count = db.update(
                    LocalDBHelper.AchievementEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            if (count > 0) {
                Log.i("DBHelper", "Achievement unlocked: " + achievementName);
            } else {
                Log.e("DBHelper", "Failed to unlock achievement: " + achievementName);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error unlocking achievement", e);
        }
    }
    public static void unlockAchievement_by_ID(int achievementId) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED, 1); // 将解锁状态设置为true
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME, System.currentTimeMillis()); // 设置解锁时间

        String selection = LocalDBHelper.AchievementEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(achievementId) };

        try {
            int count = db.update(
                    LocalDBHelper.AchievementEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            if (count > 0) {
                Log.i("DBHelper", "Achievement unlocked with ID: " + achievementId);
            } else {
                Log.e("DBHelper", "Failed to unlock achievement with ID: " + achievementId);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error unlocking achievement", e);
        }
    }
    private static void testdb(){
        // 获取数据库实例
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // 创建一个新的ContentValues对象来存储成就的值
        ContentValues values = new ContentValues();
        // 添加成就名称
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_TITLE, "睡眠大师");
        // 添加成就描述
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_DESCRIPTION, "一天内连续睡觉三次");
        // 设置成就未解锁状态（0表示未解锁，1表示已解锁）
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCKED, 0);
        // 设置成就解锁时间，由于成就尚未解锁，这里可以设置为0或null
        values.put(LocalDBHelper.AchievementEntry.COLUMN_NAME_UNLOCK_TIME, 0); // 或者使用null
        // 插入成就到数据库表中
        long newRowId = db.insert(LocalDBHelper.AchievementEntry.TABLE_NAME, null, values);
        // 检查插入是否成功
        if(newRowId == -1) {
            // 如果是-1，表示插入失败
            Log.e("Init_Achievement", "Failed to insert new achievement into the database.");
        } else {
            // 插入成功
            Log.i("Init_Achievement", "Achievement inserted with row ID: " + newRowId);
        }
    }
}
