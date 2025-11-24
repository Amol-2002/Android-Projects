package com.example.studentdairy;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

public class SessionManager {
    private static final String PREFS_NAME = "Session";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LAST_ACTIVE = "lastActiveTime";

    // 15 minutes timeout
    public static final long TIMEOUT_MS = 15 * 60 * 1000L;

    private final SharedPreferences prefs;
    private final Context context;

    public SessionManager(Context ctx) {
        context = ctx;
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
        if (loggedIn) updateLastActiveTime();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void updateLastActiveTime() {
        prefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply();
    }

    public long getLastActiveTime() {
        return prefs.getLong(KEY_LAST_ACTIVE, 0);
    }

    public boolean isSessionExpired() {
        long last = getLastActiveTime();
        if (last == 0) return true;
        return (System.currentTimeMillis() - last) >= TIMEOUT_MS;
    }

    /** LOGOUT — Delete everything */
    public void clearSession() {

        // 1) Delete Session prefs
        prefs.edit().clear().apply();

        // 2) Delete StudentProfile prefs
        context.getSharedPreferences("StudentProfile", Context.MODE_PRIVATE).edit().clear().apply();

        // 3) Delete ParentProfile
        context.getSharedPreferences("ParentProfile", Context.MODE_PRIVATE).edit().clear().apply();

        // 4) Delete Homework prefs
        context.getSharedPreferences("Homework", Context.MODE_PRIVATE).edit().clear().apply();

        // 5) Delete Timetable prefs
        context.getSharedPreferences("Timetable", Context.MODE_PRIVATE).edit().clear().apply();

        // 6) Delete any cache
        context.getCacheDir().delete();

        // 7) Delete ALL Web cookies (Volley + WebView + browser cache)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
    }
}
