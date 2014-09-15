package mat.sn;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

public class Google extends SocialNetworks {
    private static final String APPLICATION_NAME = "MyAvailableTime";
    //creating service that allows working with gmail API
    private static final ContactsService gmailService = new ContactsService(APPLICATION_NAME);
    //current feed's URL request
    private static final String gmailRequestURL = "http://www.google.com/m8/feeds/contacts/default/full";

    @Override
    public String[] getContacts(String username, String mailServer) {
        /*
        Created by Oleg Braginsky 09.09.2014
        Method allows to get all email-contacts from user's gmail account by token.
        */
        LinkedList<String> contacts = new LinkedList<String>();
        try {
            String token = getToken(username, mailServer);
            gmailService.setHeader("Authorization", "Bearer " + token);
            gmailService.setUserToken(token);//setting credentials according to token received
            URL feedUrl = new URL(gmailRequestURL);//forming full URL request for current user
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
        return contacts.toArray(new String[contacts.size()]);
    }

    @Override
    public boolean shareByMail(String urlMatt, String[] contacts, String userName) {
        return false;
    }

    @Override
    public String[] getContactsGooglePlus(String circleName, String username) {
        return new String[0];
    }

    @Override
    public boolean createCircle(String circleName, String username) {
        return false;
    }

    @Override
    public boolean addContactToCircle(String circleName, String contact, String username) {
        return false;
    }

    @Override
    public boolean removeContactFromCircle(String circleName, String contact, String userName) {
        return false;
    }

    @Override
    public boolean shareByGoogle(String urlMatt, String circleName, String userName) {
        return false;
    }

    @Override
    public boolean removeCircle(String circleName, String username) {
        return false;
    }
}

