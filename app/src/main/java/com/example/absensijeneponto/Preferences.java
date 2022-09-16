package com.example.absensijeneponto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    /** Pendeklarasian key-data berupa String, untuk sebagai wadah penyimpanan data.
     * Jadi setiap data mempunyai key yang berbeda satu sama lain */
    static final String KEY_USER_TEREGISTER ="user", KEY_PASS_TEREGISTER ="pass";

    /** Pendlakarasian Shared Preferences yang berdasarkan paramater context */
    private static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setToken(Context context, String token){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString("TOKEN", token);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_USER_TEREGISTER berupa String */
    public static String getToken(Context context){
        return getSharedPreference(context).getString("TOKEN","");
    }

    /** Deklarasi Edit Preferences dan mengubah data
     *  yang memiliki key isi KEY_USER_TEREGISTER dengan parameter username */
    public static void setRegisteredUser(Context context, String username){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_USER_TEREGISTER, username);
        editor.apply();
    }
    /** Mengembalikan nilai dari key KEY_USER_TEREGISTER berupa String */
    public static String getRegisteredUser(Context context){
        return getSharedPreference(context).getString(KEY_USER_TEREGISTER,"");
    }
}
