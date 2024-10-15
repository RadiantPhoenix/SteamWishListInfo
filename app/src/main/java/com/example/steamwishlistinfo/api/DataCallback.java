package com.example.steamwishlistinfo.api;

import com.example.steamwishlistinfo.model.Game;

import java.util.List;

public interface DataCallback {
    void onDataUpdated(List<Game> updatedGamesList);
}