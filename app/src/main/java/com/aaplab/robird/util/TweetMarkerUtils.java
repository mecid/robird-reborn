package com.aaplab.robird.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;
import twitter4j.HttpParameter;
import twitter4j.auth.OAuthAuthorization;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 02.12.12
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */
public class TweetMarkerUtils {

    public static final String API_KEY = "RO-D31C990389DC";

    public static final String TIMELINE = "timeline";
    public static final String MENTIONS = "mentions";
    public static final String FAVORITES = "favorites";
    public static final String RETWEETS = "retweets";

    private static final String TWITTER_VERIFY_CREDENTIALS_JSON = "https://api.twitter.com/1/account/verify_credentials.json";

    public static void save(String collection, long lastRead, String user, OAuthAuthorization oauth) {
        String jsonString;
        try {
            JSONObject idObject = new JSONObject();
            idObject.put("id", lastRead);
            JSONObject baseObject = new JSONObject();
            baseObject.put(collection, idObject);
            jsonString = baseObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String urlString = "https://api.tweetmarker.net/v2/lastread?api_key=" + API_KEY + "&username=" + user;

        String auth = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON, oauth);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(false);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON);
            conn.addRequestProperty("X-Verify-Credentials-Authorization", auth);
            OutputStream out = conn.getOutputStream();
            out.write(jsonString.getBytes());
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            if (code != 200) {
                Timber.d("error code = %d", code);
            }
        } catch (Exception e) {
            Timber.d(e, "");
        }
    }

    // Copied from Twiter4j's media helper AbstractImageUploadImpl
    static protected String generateVerifyCredentialsAuthorizationHeader(String verifyCredentialsUrl, OAuthAuthorization oauth) {
        List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthSignatureParams, ",", true);
    }

    public static long get(String collection, String user) {

        String urlString = "https://api.tweetmarker.net/v2/lastread?collection=" + collection + "&username=" + user + "&api_key=" + API_KEY;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            int code = conn.getResponseCode();
            if (code == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNext())
                    sb.append(scanner.nextLine());
                scanner.close();

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONObject timeline = jsonObject.getJSONObject(collection);
                return Long.parseLong(timeline.getString("id"));
            }
        } catch (IOException | JSONException e) {
            Timber.w(e, "");
        }

        return -1;
    }
}