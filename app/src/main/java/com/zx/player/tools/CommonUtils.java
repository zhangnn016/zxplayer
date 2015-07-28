package com.zx.player.tools;

import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 通用工具类
 * Created by niuniuzhang on 15/7/23.
 */
public class CommonUtils {
    public static void throwExceptionNotMainThread(){
        if(Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()){
            throw new RuntimeException("Please call this method in Main Thread");
        }
    }

    public static void throwExceptionIfInMainThread(){
        if(Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()){
            throw new RuntimeException("Please don't call this method in Main Thread");
        }
    }

    public static String map2JSONString(Map<String, String> data){
        String ret = null;

        try {
            Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
            JSONObject obj = new JSONObject();
            while(it.hasNext()){
                Map.Entry<String, String> entry = it.next();
                if(null != entry){
                    obj.put(entry.getKey(), entry.getValue());
                }
            }

            ret = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static Map<String, String> json2Map(String json){
        Map<String, String> ret = new HashMap<String, String>();

        if(false == TextUtils.isEmpty(json)){
            try {
                JSONObject obj = new JSONObject(json);

                if(null != obj){
                    Iterator<?> it = obj.keys();

                    while(it.hasNext()){
                        String key = (String)it.next();
                        ret.put(key, obj.optString(key, null));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public static String getStackMsg(Exception e) {

        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (int i = 0; i < stackArray.length; i++) {
            StackTraceElement element = stackArray[i];
            sb.append(element.toString() + "\n");
        }
        return sb.toString();
    }

    public static String getStackMsg(Throwable e) {

        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (int i = 0; i < stackArray.length; i++) {
            StackTraceElement element = stackArray[i];
            sb.append(element.toString() + "\n");
        }
        return sb.toString();
    }
}

