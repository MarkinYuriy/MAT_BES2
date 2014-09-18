/*
Created by Oleg Braginsky on 09.09.2014
 */

package mat.sn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SocialNetworks implements IConnector {
    //contains MAT user's user name (email) as a key and user's social networks tokens
    private static HashMap<String, HashMap<String, TokenData>> userData =
            new HashMap<String, HashMap<String, TokenData>>();

    private static final String PACKAGE = "mat.sn.";
//****************************************************************************************************************

    //Method for class reflection. Returns object of class with name received.
    private SocialNetwork getInstance(String socialNetwork) {
        Class cl = null;
        try {
            return  (SocialNetwork) Class.forName(PACKAGE + socialNetwork).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getToken(String username, String socialName) {
        TokenData token = userData.get(username).get(socialName);
        if (token.isExpired()) {
            getInstance(socialName).refreshToken(token);
        }
        return token.getAccessToken();
    }

    @Override
    //Method for authorize into some social network using class reflection
    public boolean authorize(String username, String socialName, String authCode) {
        if (username == null || socialName == null || authCode == null)
            return false;
        TokenData token = getInstance(socialName).retrieveToken(authCode);

        HashMap<String, TokenData> socialNetworks = userData.get(username);
        if (socialNetworks == null) {
            socialNetworks = new HashMap<String, TokenData>();
        }

        socialNetworks.put(socialName, token);
        userData.put(username, socialNetworks);
        return true;
    }

    @Override
    //Method for getting contacts from some social network
    public String[] getContacts(String username, String[] socialNames){
        List<String> contacts = new LinkedList<String>();
        for (String socialNetwork : socialNames) {
            String accessToken = getToken(username, socialNetwork);
            contacts.addAll(getInstance(socialNetwork).getContacts(accessToken));
        }
        return contacts.toArray(new String[contacts.size()]);
    }

    @Override
    public boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName) {
        return getInstance(socialName).shareByMail(urlMatt, contacts, getToken(userName, socialName));
    }

    @Override
    public String[] getAuthorizedSocialNames(String username) {
        return new String[0];
    }

    @Override
    //Method that provides to front-end server information for some social network login process
    public String[] getApplicationData(String socialName) {
        return getInstance(socialName).getApplicationData();
    }
}
