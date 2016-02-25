package com.aaplab.robird.util;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

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

            final Response response = createHttpClientWithoutSSL().newCall(request).execute();
            if (response.isSuccessful()) return lastRead;
        } catch (JSONException | IOException | KeyManagementException | NoSuchAlgorithmException e) {
            Timber.i(e, "");
        }

        return -1;
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
            final Response response = createHttpClientWithoutSSL().newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONObject(collection).getLong("id");
        } catch (IOException | JSONException | NoSuchAlgorithmException | KeyManagementException e) {
            Timber.i(e, "");
        }

        return -1;
    }

    private static String generateVerifyCredentialsAuthorizationHeader(String verifyCredentialsUrl, OAuthAuthorization oauth) {
        List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthSignatureParams, ",", true);
    }

    private static OkHttpClient createHttpClientWithoutSSL() throws KeyManagementException, NoSuchAlgorithmException {
        final OkHttpClient client = new OkHttpClient();

        final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());

        client.setHostnameVerifier(hostnameVerifier);
        client.setSslSocketFactory(context.getSocketFactory());

        return client;
    }
}