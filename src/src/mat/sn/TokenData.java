package mat.sn;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/*
 * Created by Oleg Braginsky on 16/9/14.
 * Class contents access_token, it's expiration time and refresh_token
 */

public class TokenData {

    //Default HTTP transport to use to make HTTP requests.
    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    //Default JSON factory to use to deserialize JSON.
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

    //Collection of scopes to work with
    private static ArrayList<String> scopes = new ArrayList<String>();

    private static final String CLIENT_ID = "830872460833-bq38m67qbe2iqk60pjlab70oih7vld8v.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "BEH6NhFjH_KFRxY7N0BspfNY";


    //milliseconds in one hour (1 hour is a standard token expiration time)
    private static final long HOUR_MS = 2 * 60000;//60 * 60000;
    //milliseconds in 15 minutes
    private static final long DELTA_MS = 1 * 60000;//15 * 60000;

    private String access;
    private String refresh;
    private Date expires;

    protected TokenData(String access_token, String refresh_token) {
        setExpires();
        access = access_token;
        refresh = refresh_token;
    }

    private void setExpires() {
        this.expires = new Date(new Date().getTime() + HOUR_MS - DELTA_MS);
    }

    protected String getAccessToken() {
        return access;
    }

    protected void setToken(String access_token) {
        access = access_token;
        setExpires();
    }

    protected boolean isExpired() {
        return (new Date()).after(expires);
    }

    protected boolean refreshToken() {
        try {
            GoogleTokenResponse tr =
                    new GoogleRefreshTokenRequest(TRANSPORT, JSON_FACTORY, refresh, CLIENT_ID, CLIENT_SECRET)
                            .setScopes(scopes).execute();
            access = tr.getAccessToken();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
