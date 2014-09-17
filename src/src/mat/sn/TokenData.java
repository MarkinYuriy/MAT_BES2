package mat.sn;

import java.util.Date;

/*
 * Created by Oleg Braginsky on 16/9/14.
 * Class contents access_token, it's expiration time and refresh_token
 */

public class TokenData {

    //milliseconds in one hour (1 hour is a standard token expiration time)
    private static final long HOUR_MS = 60 * 60000;
    //milliseconds in 15 minutes
    private static final long DELTA_MS = 15 * 60000;

    private String access;
    private String refresh;
    private Date expires;
//****************************************************************************************************************

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

    protected String getRefreshToken() {
        return refresh;
    }

    protected void setAccessToken(String accessToken) {
        access = accessToken;
        setExpires();
    }

    protected boolean isExpired() {
        return (new Date()).after(expires);
    }
}
