/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bcb.utils;

/**
 *
 * @author Nguyen Minh Duc
 */
import org.json.JSONObject;

public class JsonUtil {

    public static String get(String json, String key) {
        if (json == null || json.isEmpty()) return null;

        JSONObject obj = new JSONObject(json);
        return obj.has(key) ? obj.get(key).toString() : null;
    }
}