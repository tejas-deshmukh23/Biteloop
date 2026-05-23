package com.tiffin.common.constants;

/**
 * Centralized ID prefix constants.
 * Every entity across all services uses these — 
 * no magic strings scattered across the codebase.
 */
public final class PrefixConstants {

    private PrefixConstants() {}  // prevent instantiation

    public static final String USER         = "usr_";
    public static final String PROVIDER     = "prv_";
    public static final String MENU_ITEM    = "mnu_";
    public static final String ORDER        = "ord_";
    public static final String ORDER_ITEM   = "ori_";
    public static final String SUBSCRIPTION_PLAN = "spl_";
    public static final String SUBSCRIPTION = "sub_";
    public static final String PAYMENT      = "pay_";
}