package com.example.anyjob;

import android.app.Activity;
import android.util.Patterns;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class Util {
    public Util(){/* */}

    public static void startToast(Activity activity, String msg) {
        Toast.makeText(activity, msg, LENGTH_SHORT).show();
    }

    public static boolean isStorageUrl(String url){
        return Patterns.WEB_URL.matcher(url).matches() && url.contains("https://firebasestorage.googleapis.com/v0/b/anyjob-project.appspot.com/o/posts");
    }

    public static String storageUrlToName(String url){
        return url.split("\\?")[0].split("%2F")[url.split("\\?")[0].split("%2F").length-1];
    }
}
