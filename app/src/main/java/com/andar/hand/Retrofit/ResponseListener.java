package com.andar.hand.Retrofit;

import org.json.JSONArray;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */
public interface ResponseListener {
    void getJSONArrayResult(String strTag, JSONArray arrayResponse);
}
