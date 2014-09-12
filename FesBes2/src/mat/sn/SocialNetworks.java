/*
Created by Oleg Braginsky on 09.09.2014
 */

package mat.sn;

import java.util.HashMap;

public abstract class SocialNetworks implements IFesBes2 {
    //must contain MAT user's user name (email) as a key and social network user information
    //(???user name for gmail URL request forming??? and token)
    private static HashMap<String, String> users = new HashMap<String, String>();

    void addToken(String userName, String token) {
        users.put(userName, token);
    }

    String getToken(String userName) {
        return users.get(userName);
    }
}
