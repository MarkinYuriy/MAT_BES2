package mat.sn;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.util.ServiceException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

public class Google extends SocialNetwork {

    private static final String APPLICATION_NAME = "MyAvailableTime";

    //Default HTTP transport to use to make HTTP requests.
    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    //Default JSON factory to use to deserialize JSON.
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

    //Creates a client secrets object from the client_secrets.json file.
    private static GoogleClientSecrets clientSecrets;

    //Collection of scopes to work with
    private static ArrayList<String> scopes = new ArrayList<String>();

    //Prepare needed information
    static {
        try {
            //Read json file to get client secrets data
            Reader reader = new FileReader("/Users/broleg/Dropbox/TelRan/MAT_Project/FesBes2/web/client_secrets.json");
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            throw new Error("No client_secrets.json found", e);
        }
        //prepare needed scopes
        scopes.add("https://www.googleapis.com/auth/plus.login");//scope for google authorization
        scopes.add("https://www.google.com/m8/feeds");//scope for working with gmail (read/write)
        scopes.add("https://www.googleapis.com/auth/calendar");//scope for working with calendars (read/write)
        scopes.add("https://www.googleapis.com/auth/gmail.compose");//scope for working with gmail
    }

    protected static final String CLIENT_ID = clientSecrets.getWeb().getClientId();
    protected static final String CLIENT_SECRET = clientSecrets.getWeb().getClientSecret();

    //creating service that allows working with gmail API
    private static final ContactsService gmailService = new ContactsService(APPLICATION_NAME);

    //current feed's URL request
    private static final String contactsRequestURL = "http://www.google.com/m8/feeds/contacts/default/full";
//****************************************************************************************************************

    @Override
    protected LinkedList<String> getContacts(String accessToken) {
        /*
        Created by Oleg Braginsky 09/09/14
        Method allows to get all email-contacts from user's google account
        */
        LinkedList<String> contacts = new LinkedList<String>();

        try {
            gmailService.setHeader("Authorization", "Bearer " + accessToken);
            gmailService.setUserToken(accessToken);//setting credentials according to token received
            URL feedUrl = new URL(contactsRequestURL);//forming full URL request for current user
            ContactFeed feeds = gmailService.getFeed(feedUrl, ContactFeed.class);//getting contacts full info
            //getting emails from contacts info
            for (int i = 0; i < feeds.getEntries().size(); i++) {
                ContactEntry contact = feeds.getEntries().get(i);
                for (Email email : contact.getEmailAddresses()) {
                    contacts.add(email.getAddress());
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    @Override
    protected TokenData retrieveToken(String authCode) {
        //Upgrade the authorization code into an access and refresh token.
        try {
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(TRANSPORT, JSON_FACTORY, CLIENT_ID,
                    CLIENT_SECRET, authCode, "postmessage").setScopes(scopes).execute();
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            return new TokenData(accessToken, refreshToken);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    //Returns array with client id (read from client_secrets.json) and scopes needed
    protected String[] getApplicationData() {
        String[] data = new String[1 + scopes.size()];
        data[INDEX_ID] = CLIENT_ID;
        int i = INDEX_SCOPES;
        for (String scope: scopes) {
            data[i++] = scope;
        }
        return data;
    }

    @Override
    public boolean shareByMail(String urlMatt, String[] contacts, String accessToken) {
        return false;
    }

    //Get new access token using refresh token
    protected boolean refreshToken(TokenData token) {
        try {
            GoogleTokenResponse tokenResponse =
                    new GoogleRefreshTokenRequest(TRANSPORT, JSON_FACTORY, token.getRefreshToken(),
                            CLIENT_ID, CLIENT_SECRET).setScopes(scopes).execute();
            token.setAccessToken(tokenResponse.getAccessToken());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

