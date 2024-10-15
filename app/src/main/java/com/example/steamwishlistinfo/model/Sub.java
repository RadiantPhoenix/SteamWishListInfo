package com.example.steamwishlistinfo.model;

public class Sub {
    public static final String NOT_DEFINED = "Not defined";
    private String price;
    private String discount_pct;

    public Sub(String price, String discount_pct) {
        this.price = price;
        this.discount_pct = discount_pct;
    }

    public Sub() {
    }

    public String getPrice() {
        return !NOT_DEFINED.equals(price) ? formatPrice(price) : price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getDiscount() {
        if (discount_pct == null) {
            return 0;
        }
        return Integer.parseInt(discount_pct);
    }

    public String getDiscount_pct() {
        return discount_pct;
    }

    public void setDiscount_pct(String discount_pct) {
        this.discount_pct = discount_pct;
    }

    private String formatPrice(String price) {

        if (price.length() < 3) {
            price = String.format("%03d", Integer.parseInt(price));
        }

        String d = price.substring(0, price.length() - 2);
        String c = price.substring(price.length() - 2);

        return d + "." + c;
    }

}
