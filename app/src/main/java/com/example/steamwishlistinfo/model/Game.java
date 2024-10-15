package com.example.steamwishlistinfo.model;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;


public class Game {
    private String name;
    private List<Sub> subs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sub> getSubs() {
        return (subs == null || subs.isEmpty())
                ? Collections.singletonList(new Sub(Sub.NOT_DEFINED, null))
                : subs;
    }


    public void setSubs(List<Sub> subs) {
        this.subs = subs;
    }

    @NonNull
    @Override
    public String toString() {
        return name.replace("amp;", "")
                + "\n" + "Price=" + getSubs().get(0).getPrice()
                + " Discount=" + getSubs().get(0).getDiscount();
    }
}
