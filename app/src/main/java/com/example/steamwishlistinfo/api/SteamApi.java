package com.example.steamwishlistinfo.api;

import com.example.steamwishlistinfo.model.Game;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

import java.util.Map;

public interface SteamApi {
    @GET("wishlist/profiles/{profileId}/wishlistdata/")
    Call<Map<String, Game>> getWishlistData(@Path("profileId") String profileId);

    @GET
    Call<String> getProfilePage(@Url String url);
}
