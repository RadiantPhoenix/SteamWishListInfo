package com.example.steamwishlistinfo;
import static com.example.steamwishlistinfo.SettingsActivity.PREFS_NAME;
import static com.example.steamwishlistinfo.SettingsActivity.PREF_USER_ID;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.example.steamwishlistinfo.api.DataCallback;
import com.example.steamwishlistinfo.api.SteamApi;
import com.example.steamwishlistinfo.model.Game;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class WishlistRemoteViewsService extends RemoteViewsService {

    public static final String FAILED = "Failed to load wishlist data";
    public static final String BASE_STORE_URL = "https://store.steampowered.com/";
    public static final String BASE_COMMUNITY_URL = "https://steamcommunity.com/";
    public static final String LOADING_WISHLIST_DATA = "Loading wishlist data...";
    public static final String DEFAULT_USER_ID = "1000";
    public static final String DEFINE_USER_ID = "Define your Steam User ID in Settings";
    public static final String PRESS_REFRESH_BUTTON = "Press Refresh button";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        return new WishlistRemoteViewsFactory(getApplicationContext(), appWidgetManager, appWidgetIds);
    }

    class WishlistRemoteViewsFactory implements RemoteViewsFactory {

        private List<Game> gamesList;
        private Calendar lastDataUpdate;
        private String userId;
        private final Context context;
        private AppWidgetManager appWidgetManager;
        private int[] appWidgetIds;
        private final int dataUpdateInterval = 10;


        WishlistRemoteViewsFactory(Context context, AppWidgetManager appWidgetManager,
                                   int[] appWidgetIds) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetIds = appWidgetIds;
        }

        @Override
        public void onCreate() {
            gamesList = new ArrayList<>();
            lastDataUpdate = Calendar.getInstance();
            lastDataUpdate.add(Calendar.SECOND, -2 * dataUpdateInterval);
            //loadSettings();
        }

        @Override
        public void onDataSetChanged() {
            loadSettings();
         //   loadProfileData();
            loadData();
        }

        @Override
        public void onDestroy() {
            gamesList.clear();
        }

        @Override
        public int getCount() {
            return gamesList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item);
            if (gamesList.size() > position) {
                if (LOADING_WISHLIST_DATA.equals(gamesList.get(position).getName())
                        || DEFINE_USER_ID.equals(gamesList.get(position).getName())
                        || PRESS_REFRESH_BUTTON.equals(gamesList.get(position).getName())) {
                    views.setTextViewText(R.id.tvItemText, gamesList.get(position).getName());
                    views.setTextColor(R.id.tvItemText, Color.BLUE);
                } else if (FAILED.equals(gamesList.get(position).getName())) {
                    views.setTextViewText(R.id.tvItemText, FAILED);
                    views.setTextColor(R.id.tvItemText, Color.RED);
                } else {
                    views.setTextViewText(R.id.tvItemText, gamesList.get(position).toString());
                    if (gamesList.get(position).getSubs().get(0).getDiscount() > 0) {
                        views.setTextColor(R.id.tvItemText, Color.GREEN);
                    } else {
                        views.setTextColor(R.id.tvItemText, Color.LTGRAY);
                    }
                }
            }
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void loadSettings() {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            userId = sharedPreferences.getString(PREF_USER_ID, DEFAULT_USER_ID);
        }

        private void loadProfileData() {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.profile_name, "SteamId: " + userId);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }

        private void loadProfileDataNew() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_COMMUNITY_URL)  // Steam community base URL
                    .addConverterFactory(ScalarsConverterFactory.create())  // For fetching raw HTML as string
                    .callbackExecutor(Executors.newSingleThreadExecutor()) // Non-main thread callback execution
                    .build();
            SteamApi api = retrofit.create(SteamApi.class);

            // Create a call to get the raw HTML of the profile page
            Call<String> call = api.getProfilePage(userId);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String html = response.body();
                        Document doc = Jsoup.parse(html);

                        // Extract the username from the <title> tag
                        Element titleElement = doc.selectFirst("title");
                        String profileName = "";
                        if (titleElement != null) {
                            profileName = titleElement.text().replace("Steam Community :: ", "");
                        }

                        Element avatarElement = doc.selectFirst(".playerAvatarAutoSizeInner img");
                        String avatarUrl = "";
                        if (avatarElement != null) {
                            avatarUrl = avatarElement.attr("src");
                        }

                        updateProfileInfo(profileName, avatarUrl);

                    } else {
                        // Handle the case where the HTML could not be fetched
                        // Show error message or set default profile info
                        handleProfileDataError();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // Handle the failure case
                    t.printStackTrace();
                    handleProfileDataError();
                }
            });
        }

        private void updateProfileInfo(String profileName, String avatarUrl) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.profile_name, profileName);

 /*           AppWidgetTarget appWidgetTarget =
                    new AppWidgetTarget(context, R.id.logo_image, remoteViews, appWidgetIds);
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(avatarUrl)
                    .into(appWidgetTarget);

            // Notify the AppWidgetManager of the change
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);*/
        }

        private void handleProfileDataError() {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.profile_name, "Profile Data Not Available");
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }

        private void loadData() {
            Calendar checkDate = (Calendar) lastDataUpdate.clone();
            checkDate.add(Calendar.SECOND, dataUpdateInterval);
            if (checkDate.before(Calendar.getInstance())
                    && !DEFAULT_USER_ID.equals(userId)) {
                lastDataUpdate = Calendar.getInstance();
                setDefaultMessage(LOADING_WISHLIST_DATA);

                refreshData(new DataCallback() {
                    @Override
                    public void onDataUpdated(List<Game> updatedGamesList) {
                        gamesList.clear();
                        gamesList.addAll(updatedGamesList);

                        gamesList.sort((g1, g2) ->
                                Integer.compare(g2.getSubs().get(0).getDiscount(),
                                        g1.getSubs().get(0).getDiscount()));

                    }
                });
            } else if (DEFAULT_USER_ID.equals(userId)) {
                setDefaultMessage(DEFINE_USER_ID);
            } else if (gamesList.isEmpty()) {
                setDefaultMessage(PRESS_REFRESH_BUTTON);
            }
        }

        private void setDefaultMessage(String message) {
            gamesList.clear();
            Game emptyGame = new Game();
            emptyGame.setName(message);
            gamesList.add(emptyGame);
        }

        private void refreshData(DataCallback callback) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_STORE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SteamApi api = retrofit.create(SteamApi.class);
            Call<Map<String, Game>> call = api.getWishlistData(userId); //"76561198138285528"

            call.enqueue(new Callback<Map<String, Game>>() {
                @Override
                public void onResponse(Call<Map<String, Game>> call, Response<Map<String, Game>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Game> updatedGames = new ArrayList<>();
                        Map<String, Game> games = response.body();
                        for (String key : games.keySet()) {
                            updatedGames.add(games.get(key));
                        }
                        callback.onDataUpdated(updatedGames);

                    } else {
                        processErrorResponse();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Game>> call, Throwable t) {
                    processErrorResponse();
                }

                private void processErrorResponse() {
                    List<Game> failedList = new ArrayList<>();
                    Game emptyGame = new Game();
                    emptyGame.setName(FAILED);
                    failedList.add(emptyGame);
                    callback.onDataUpdated(failedList);
                }
            });

        }

    }

}
