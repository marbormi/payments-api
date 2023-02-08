package com.marbormi.paymentsapi.domain;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum CurrencyCode {
    AED("United Arab Emirates Dirham"),

    AFN("Afghanistan Afghani"),

    EUR("Euro Member Countries"),

    GBP("United Kingdom Pound"),

    JPY("Japan Yen"),

    KWD("Kuwait Dinar"),

    MAD("Morocco Dirham"),

    MXN("Mexico Peso"),

    NZD("New Zealand Dollar"),

    RUB("Russia Ruble"),

    SAR("Saudi Arabia Riyal"),

    SVC("El Salvador Colon"),

    USD("United States Dollar"),

    UYU("Uruguay Peso"),

    VEF("Venezuela Bolivar"),

    ZWD("Zimbabwe Dollar");

    private String description;

    CurrencyCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public List<CurrencyCode> getAllCurrencies() {
        return new ArrayList<>(EnumSet.allOf(CurrencyCode.class));
    }
}