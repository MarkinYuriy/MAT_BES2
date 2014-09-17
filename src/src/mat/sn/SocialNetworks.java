/*
Created by Oleg Braginsky on 09.09.2014
 */

package mat.sn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class SocialNetworks implements IFesBes2 {
    //contains MAT user's user name (email) as a key and user's social networks tokens
    private static HashMap<String, HashMap<String, TokenData>> userData =
            new HashMap<String, HashMap<String, TokenData>>();

    private static final String PACKAGE = "mat.sn.";
//****************************************************************************************************************

    //Method for class reflection. Returns object of class with name received.
    private SocialNetworks getInstance(String socialNetwork) {
        Class cl = null;
        try {
            return  (SocialNetworks) Class.forName(PACKAGE + socialNetwork).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    //Method for authorize into some social network using class reflection
    public boolean authorize(String username, String socialName, String authCode) {
        if (username == null || socialName == null || authCode == null)
            return false;
        TokenData token = getInstance(socialName).retrieveToken(authCode);
        if (token == null)
            return false;

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
    public String[] getContacts(String username, String[] socialNames) {
        List<String> contacts = new LinkedList<String>();
        for (String socialNetwork : socialNames) {
            TokenData token = getToken(username, socialNetwork);
            contacts.addAll(getInstance(socialNetwork).getContacts(token));
        }
        return contacts.toArray(new String[contacts.size()]);
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

    private TokenData getToken(String username, String socialName) {
        return userData.get(username).get(socialName);
    }

    //Methods that all the social networks must implement
    abstract List<String> getContacts(TokenData token);
    abstract TokenData retrieveToken(String authCode);
    abstract String[] getApplicationData();
}
