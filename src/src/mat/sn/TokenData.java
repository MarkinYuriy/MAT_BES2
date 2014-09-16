package mat.sn;

import java.util.Date;

/*
 * Created by Oleg Braginsky on 16/9/14.
 * Class contents token and it's expiration time (just for access_token relevant)
 */

public class TokenData {
    //milliseconds in one hour (1 hour is a standard token expiration time)
    private static final long HOUR_MS = 3600000;
    //milliseconds in 15 minutes
    private static final long DELTA_MS = 900000;

    private String token;
    private Date expires;

    protected TokenData(String token) {
        this.token = token;
        setExpires();
    }

    private void setExpires() {
        this.expires = new Date(new Date().getTime() + HOUR_MS - DELTA_MS);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        setExpires();
    }

    protected boolean isExpired() {
        return (new Date()).after(expires);
    }
}
