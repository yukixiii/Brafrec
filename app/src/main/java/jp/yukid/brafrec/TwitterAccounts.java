package jp.yukid.brafrec;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by yuki on 15/02/25.
 */
public class TwitterAccounts {
    public static final String ACCOUNTS_PREF = "accounts";
    public static ArrayList<Twitter> accounts = null;
    public static JSONArray accountsJSON;
    public static SharedPreferences pref;

    /**
     * 最初に呼ぶメソッド
     * すでにアクセストークンが保存してある分のTwitterインスタンスを作成
     * @param context
     */
    public static void initTwitterInstances(Context context) {
        accounts = new ArrayList<>();
        pref = context.getSharedPreferences(ACCOUNTS_PREF, Context.MODE_PRIVATE);
        if (pref.contains("accounts")) {
            try {
                accountsJSON = new JSONArray(pref.getString("accounts", ""));
                for (int i = 0; i < accountsJSON.length(); i++) {
                    // アカウントすべてのアクセストークンを用いてインスタンスを生成
                    TwitterFactory factory = new TwitterFactory();
                    String token = accountsJSON.getJSONObject(i).getString("token");
                    String tokenSecret = accountsJSON.getJSONObject(i).getString("tokenSecret");
                    Twitter twitter = factory.getInstance();
                    twitter.setOAuthConsumer(context.getString(R.string.twitter_consumer_key), context.getString(R.string.twitter_consumer_secret));
                    twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
                    accounts.add(twitter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static Twitter getInstance(Context context) {
        TwitterFactory factory;
        Twitter twitter;

        factory = new TwitterFactory();
        twitter = factory.getInstance();
        twitter.setOAuthConsumer(context.getString(R.string.twitter_consumer_key), context.getString(R.string.twitter_consumer_secret));

        return twitter;
    }

    public static void addAccount(AccessToken accessToken, Twitter twitter) {
        if (accounts != null) {
            try {
                JSONObject addJSON;
                SharedPreferences.Editor editor;

                if (accountsJSON == null) {
                    accountsJSON = new JSONArray();
                }

                // JSONにaccessTokenを追加
                addJSON = new JSONObject();
                addJSON.put("token", accessToken.getToken());
                addJSON.put("tokenSecret", accessToken.getTokenSecret());
                accountsJSON.put(addJSON);

                // SharedPreferencesにaccessTokenを追加
                editor = pref.edit();
                editor.putString("accounts", accountsJSON.toString());
                editor.apply();

                accounts.add(twitter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
