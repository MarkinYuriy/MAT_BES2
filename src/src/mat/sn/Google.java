package mat.sn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mat.Matt;
import mat.MattData;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;

public class Google extends SocialNetwork {

    private static final String APPLICATION_NAME = "MyAvailableTime";

    //Default HTTP transport to use to make HTTP requests.
    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    //Default JSON factory to use to deserialize JSON.
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

    //Collection of scopes to work with
    private static ArrayList<String> scopes = new ArrayList<String>();

    //Prepare needed information
    static {
        //prepare needed scopes
        scopes.add("https://www.googleapis.com/auth/plus.login");//scope for google authorization
        scopes.add("https://www.google.com/m8/feeds");//scope for working with gmail (read/write)
        scopes.add("https://www.googleapis.com/auth/calendar");//scope for working with calendars (read/write)
        scopes.add("https://www.googleapis.com/auth/gmail.compose");//scope for working with gmail
    }

    protected static final String CLIENT_ID = "227063309542-shimecta6c560ur1vuee7fde4c2l0qee.apps.googleusercontent.com";//clientSecrets.getWeb().getClientId();
    protected static final String CLIENT_SECRET = "RzIgbq3Lbyc4ElsG-rzkHikm";//clientSecrets.getWeb().getClientSecret();

    //creating service that allows working with gmail API
    private static final ContactsService gmailService = new ContactsService(APPLICATION_NAME);
    private static final int MAX_COUNT_EMAILS = 200; 

    private com.google.api.services.calendar.Calendar calendarService;
	private static final String MAT_NAME = "My Available Time";
	private final static long millisInHour = 3600000;

    //current feed's URL request
    private static final String contactsRequestURL = "http://www.google.com/m8/feeds/contacts/default/full";
//****************************************************************************************************************
	private GoogleCredential getCredential(String accessToken) {
		GoogleCredential credential = new GoogleCredential.Builder()
		.setTransport(TRANSPORT).setJsonFactory(JSON_FACTORY)
		.setClientSecrets(CLIENT_ID, CLIENT_SECRET).build();
		credential.setAccessToken(accessToken);
		return credential;
	}
	
	@Override
    protected LinkedList<String> getContacts(String accessToken) {
        // Method allows to get all email-contacts from user's google account
        LinkedList<String> contacts = new LinkedList<String>();
        try {
            gmailService.setHeader("Authorization", "Bearer " + accessToken);
            gmailService.setUserToken(accessToken);//setting credentials according to token received
            URL feedUrl = new URL(contactsRequestURL);//forming full URL request for current user
            Query query = new Query(feedUrl);
            query.setMaxResults(MAX_COUNT_EMAILS);            
            ContactFeed feeds = gmailService.getFeed(query, ContactFeed.class);//getting contacts full info
            //getting emails from contacts info
            for (int i = 0; i < feeds.getEntries().size(); i++) {
                ContactEntry contact = feeds.getEntries().get(i);
                for (Email email : contact.getEmailAddresses()) {
                    contacts.add(email.getAddress());
                }
            }
        	return contacts;
        } catch (Exception e) {
            throw new SecurityException(SocialNetworksConnector.AUTH_ERROR);
        }
    }
//****************************************************************************************************************

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
            throw new SecurityException(SocialNetworksConnector.AUTH_ERROR);
        }
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
            throw new SecurityException(SocialNetworksConnector.NO_AUTH);
        }
    }
//****************************************************************************************************************

    @Override
    //Returns array with client id (read from client_secrets.json) and scopes needed
    protected String[] getApplicationData() {
        String[] data = new String[1 + scopes.size()];
        data[SocialNetworksConnector.INDEX_ID] = CLIENT_ID;
        int i = SocialNetworksConnector.INDEX_SCOPES;
        for (String scope: scopes) {
            data[i++] = scope;
        }
        return data;
    }
    
//****************************************************************************************************************

    @Override
    public boolean shareByMail(String userName, String urlMatt, String[] contacts, String accessToken) {

    	GoogleCredential credential = getCredential(accessToken);
    	Gmail service = new Gmail.Builder(TRANSPORT, JSON_FACTORY, credential)
    		.setApplicationName(APPLICATION_NAME).build();
    	Message message;
    	MimeMessage email;
    
    	email = createEmail(contacts, userName, "My Available Time - Calendar", urlMatt, null, null);
    	message =  createMessageWithEmail(email);
    	try {
    		service.users().messages().send("me", message).execute();
    	} catch (IOException e) {
    		e.printStackTrace();
    		return false;  
    	} 
    	return true; 
    }
    
    private MimeMessage createEmail(String[] to, String from,
    						String subject, String bodyText, String fileDir, String filename){
    	Properties props = new Properties();
    	Session session = Session.getDefaultInstance(props, null);
    	MimeMessage email = new MimeMessage(session);
    	InternetAddress[] tAddress = new InternetAddress[to.length];
    	try {
    		for (int i = 0; i < to.length; i++){
    			tAddress[i] = new InternetAddress(to[i]);
    		}
    		InternetAddress fAddress = new InternetAddress(from);
    		email.setFrom(fAddress);
    		email.addRecipients(javax.mail.Message.RecipientType.TO, tAddress);
    		email.setSubject(subject);

    		MimeBodyPart mimeBodyPart = new MimeBodyPart();
    		mimeBodyPart.setContent(bodyText, "text/plain");
    		mimeBodyPart.setHeader("Content-Type", "text/plain; charset=\"UTF-8\"");
    		Multipart multipart = new MimeMultipart();
    		multipart.addBodyPart(mimeBodyPart);
    		email.setContent(multipart);
    	} catch (AddressException e) { 
    		e.printStackTrace();
    	} catch (MessagingException e) { 
    		e.printStackTrace();
    	}
    	return email;
    }
    
    private Message createMessageWithEmail(MimeMessage email)  {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	try {
    		email.writeTo(bytes);
    	} catch (IOException e) { 
    		e.printStackTrace();
    	} catch (MessagingException e) { 
    		e.printStackTrace();
    	} 
    	String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
    	Message message = new Message();
    	message.setRaw(encodedEmail); 
    	return message;
	 }

//****************************************************************************************************************

    @Override
	List<Boolean> getSlots(MattData interval, String accessToken){
		int dayInterval = (interval.getEndHour() - interval.getStartHour())
				* (60 / interval.getTimeSlot());
		long millisInSlot = interval.getTimeSlot()*60000;
		ArrayList<Boolean> slots = new ArrayList<Boolean>();
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(TRANSPORT, JSON_FACTORY, credential)
			.setApplicationName(APPLICATION_NAME).build();  
		CalendarList calendarList = null;
		String idCalendar = null;
		Events events = null;
		long startDate = getStartPoint(interval);
		long endDate = getEndPoint(interval);
		long startSlot = startDate / millisInSlot;
		long endSlot = endDate / millisInSlot;
		DateTime startDateTime = new DateTime(startDate);
		DateTime endDateTime = new DateTime(endDate);
		System.out.println(startDateTime +" "+endDateTime);
		System.out.println(startSlot + " " + endSlot);
		long resultSize = endSlot - startSlot;
		for (long i = 0; i < resultSize; i++)
			slots.add(false);
		System.out.println(slots.toString());
		try {
			calendarList = calendarService.calendarList().list().execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<CalendarListEntry> items = calendarList.getItems();
		for (CalendarListEntry calendarListEntry : items) {
			if (!calendarListEntry.getSummary().equals(MAT_NAME)) {
				idCalendar = calendarListEntry.getId();
				try {
					events = calendarService.events().list(idCalendar)
							.setTimeMin(startDateTime).setTimeMax(endDateTime).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<Event> listEvents = events.getItems();
				for (Event event : listEvents) {

					System.out.println(event.getSummary());
					System.out.println(event.getId());
					System.out.println(event.getStart().getDateTime());
					System.out.println(event.getEnd().getDateTime());

					long startEvent;
					long endEvent;
					if (event.getStart().getDate() == null) { //check if a whole-day event calendar
						startEvent = (event.getStart().getDateTime()).getValue() / millisInSlot;
						endEvent = (event.getEnd().getDateTime()).getValue() / millisInSlot;
					} else {
						startEvent = (event.getStart().getDate()).getValue() / millisInSlot;
						endEvent = (event.getEnd().getDate()).getValue() / millisInSlot;
					} 					
					if (startEvent < startSlot)	startEvent = startSlot;
					if (endEvent > endSlot) endEvent = endSlot;
					for (; startEvent < endEvent; startEvent++)
						slots.set((int) (startEvent - startSlot), true);
				}
			}
		}
		for (int index = 0; index < resultSize;) {
			resultSlots.addAll(slots.subList(index, index + dayInterval));
			index = index + 24 * (60 / interval.getTimeSlot());
		}
		return resultSlots;
	}
    
//****************************************************************************************************************

	private long getStartPoint(MattData data) {
		return data.getStartDate().getTime() + data.getStartHour() * millisInHour;
	}

	private long getEndPoint(MattData data) {
		return data.getStartDate().getTime()
				+ ((data.getnDays() - 1) * 24 + data.getEndHour()/* + 1*/) * millisInHour;
	}
    
//****************************************************************************************************************

    @Override
	void setMatCalendar(List<Matt> matts, String accessToken) {
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(
				TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build(); 
		Calendar calendar = null;
		try {
			CalendarList calendarList = calendarService.calendarList().list().execute();	
			List<CalendarListEntry> items = calendarList.getItems();
			for (CalendarListEntry calendarListEntry : items) {
				if (calendarListEntry.getSummary().equals(MAT_NAME)) {
					calendarService.calendars().delete(calendarListEntry.getId()).execute();
					System.out.println(calendarListEntry.getId());
				}
			}
			calendar = calendarService.calendars().insert(new Calendar().setSummary(MAT_NAME)).execute();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (Matt matt : matts) {
			String name = matt.getData().getName();//name of matt
			int nDays = matt.getData().getnDays();//number of days
			Date startDate  = matt.getData().getStartDate();
			int startHour = matt.getData().getStartHour();
			int endHour = matt.getData().getEndHour();
			int timeSlot = matt.getData().getTimeSlot(); //in minutes
			ArrayList<Boolean> slots =matt.getSlots();
			int slotsByDay = slots.size()/nDays;
			long currentData = startDate.getTime()+startHour*millisInHour;
			int currentSlot = 1;
			for(Boolean slot: slots)
				if(!slot){
					com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
					event.setSummary(name);
					EventDateTime eventDTS = new EventDateTime();
					eventDTS.setDateTime(new DateTime(new Date(currentData), TimeZone.getTimeZone("UTC")));
					System.out.println("start "+eventDTS);
					currentData+=timeSlot*millisInHour/60;
					EventDateTime eventDTE = new EventDateTime();
					eventDTE.setDateTime(new DateTime(new Date(currentData), TimeZone.getTimeZone("UTC")));
					System.out.println("end "+eventDTE);
					event.setEnd(eventDTE);
					event.setStart(eventDTS);
System.out.println(event);
					if((currentSlot++)%slotsByDay==0)
						currentData=startDate.getTime()+startHour*millisInHour+currentSlot/slotsByDay*24*millisInHour;
					try {
						calendarService.events().insert(calendar.getId(), event).execute();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					currentData+=timeSlot*millisInHour/60;
					if(slotsByDay==currentSlot++)
						currentData=startDate.getTime()+startHour*millisInHour+currentSlot/slotsByDay*24*millisInHour;
				}

		}
    }
}
