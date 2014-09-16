package mat.sn;

import java.util.Date;

/*
 * Created by Oleg Braginsky on 16/9/14.
 * Class contents token and it's expiration time (just for access_token relevant)
 */

public class TokenData {
    //milliseconds in one hour (1 hour is a standard token expiration time)
    private static final long HOUR_MS = 60 * 60000;
    //milliseconds in 15 minutes
    private static final long DELTA_MS = 15 * 60000;

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
        return true;
    }
}
