/*
Created by Oleg Braginsky on 09.09.2014
 */

package mat.sn;

import java.util.HashMap;

public abstract class SocialNetworks implements IFesBes2 {
    //must contain MAT user's user name (email) as a key and user's social networks information
    private static HashMap<String, HashMap<String, String>> users = new HashMap<String, HashMap<String, String>>();

    @Override
    public void addToken(String userName, String sn, String token) {
        HashMap<String, String> social_networks = new HashMap<String, String>();
        social_networks.put(sn, token);
        users.put(userName, social_networks);
    }

    @Override
    public String getToken(String userName, String sn) {
        return users.get(userName).get(sn);
    }
}
