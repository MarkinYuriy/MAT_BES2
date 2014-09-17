/*
Created by Oleg Braginsky on 09.09.2014
 */

package mat.sn;

import java.util.HashMap;

public abstract class SocialNetworks implements IFesBes2 {
    protected static final String APPLICATION_NAME = "MyAvailableTime";

    //must contain MAT user's user name (email) as a key and user's social networks tokens
    private static HashMap<String, HashMap<String, TokenData>> userData =
            new HashMap<String, HashMap<String, TokenData>>();

    @Override
    public boolean setToken(String username, String socialName, String accessToken, String refreshToken) {
        if (username == null || socialName == null || accessToken == null || refreshToken == null)
            return false;
        HashMap<String, TokenData> socialNetworks = userData.get(username);
        if (socialNetworks == null) {
            socialNetworks = new HashMap<String, TokenData>();
        }
        TokenData tokens = new TokenData(accessToken, refreshToken);
        socialNetworks.put(socialName, tokens);
        userData.put(username, socialNetworks);
        return true;
    }

    @Override
    public String[] getContacts(String username, String[] socialNames) {
        for (String socialNetwork : socialNames) {
            if (socialNetwork.equals(GOOGLE)) {
                TokenData token = getToken(username, socialNetwork);
                if (token.isExpired() && !refreshToken(token)) {
                    return null;
                }
                return  (new Google()).getContacts(token);
            }
        }
        return null;
    }

    private TokenData getToken(String username, String socialName) {
        return userData.get(username).get(socialName);
    }

    private boolean refreshToken(TokenData token){
        return token.refreshToken();
    }

}
