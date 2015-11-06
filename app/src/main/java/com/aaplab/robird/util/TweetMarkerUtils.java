package com.aaplab.robird.util;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

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

    private static final String API_KEY = "RO-D31C990389DC";

    public static final String TIMELINE = "timeline";
    public static final String MENTIONS = "mentions";
    public static final String FAVORITES = "favorites";
    public static final String RETWEETS = "retweets";

    private static final String TWITTER_VERIFY_CREDENTIALS_JSON = "https://api.twitter.com/1/account/verify_credentials.json";
    private static final String TWEETMARKER_API_URL = "https://api.tweetmarker.net/v2/lastread";

    public static long save(String collection, long lastRead, String user, OAuthAuthorization oauth) {
        try {
            collection = "lists." + Long.parseLong(collection);
        } catch (NumberFormatException ignored) {

        }

        try {
            final String auth = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON, oauth);

            JSONObject body = new JSONObject();
            JSONObject collectionJson = new JSONObject();
            collectionJson.put("id", lastRead);
            body.put(collection, collectionJson);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            Request request = new Request.Builder()
                    .url(TWEETMARKER_API_URL + "?api_key=" + API_KEY + "&username=" + user)
                    .addHeader("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON)
                    .addHeader("X-Verify-Credentials-Authorization", auth)
                    .post(RequestBody.create(JSON, body.toString()))
                    .build();

            final Response response = new OkHttpClient().newCall(request).execute();
            return new JSONObject(response.body().string()).getJSONObject(collection).getLong("id");
        } catch (JSONException | IOException e) {
            Timber.i(e, "");
        }

        return -1;
    }

    private static String generateVerifyCredentialsAuthorizationHeader(String verifyCredentialsUrl, OAuthAuthorization oauth) {
        List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthSignatureParams, ",", true);
    }

    public static long get(String collection, String user) {
        try {
            collection = "lists." + Long.parseLong(collection);
        } catch (NumberFormatException ignored) {

        }

        Request request = new Request.Builder()
                .url(TWEETMARKER_API_URL + "?api_key=" + API_KEY + "&username=" + user + "&collection" + collection)
                .get()
                .build();

        try {
            final Response response = new OkHttpClient().newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONObject(collection).getLong("id");
        } catch (IOException | JSONException e) {
            Timber.i(e, "");
        }

        return -1;
    }
}