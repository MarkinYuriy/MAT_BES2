package mat.sn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mat.IFrontConnector;
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
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
//import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
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
	private static final String PREFIX_MAT = "MAT: ";
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
            //throw new SecurityException(SocialNetworksConnector.AUTH_ERROR);
            return null;
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
        } catch (Exception e1) {
            //throw new SecurityException(SocialNetworksConnector.AUTH_ERROR);
        	return null;
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
        } catch (Exception e) {
           // throw new SecurityException(SocialNetworksConnector.NO_AUTH);
        	return false;
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
    	try {
    		GoogleCredential credential = getCredential(accessToken);
    		Gmail service = new Gmail.Builder(TRANSPORT, JSON_FACTORY, credential)
    			.setApplicationName(APPLICATION_NAME).build();
    		Message message;
    		MimeMessage email;
    		email = createEmail(contacts, userName, "My Available Time - Calendar", urlMatt, null, null);
    		message =  createMessageWithEmail(email);
    		service.users().messages().send("me", message).execute();
    	} catch (Exception e) {
    		//e.printStackTrace();
    		return false;  
    	} 
    	return true; 
    }
    
    private MimeMessage createEmail(String[] to, String from, String subject, String bodyText, String fileDir, String filename){
    	try {
    		Properties props = new Properties();
    		Session session = Session.getDefaultInstance(props, null);
    		MimeMessage email = new MimeMessage(session);
    		InternetAddress[] tAddress = new InternetAddress[to.length];
    	
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
    		return email;
    	} catch (Exception e) { 
    		//e.printStackTrace();
    		return null;
    	} 
    }
    
    private Message createMessageWithEmail(MimeMessage email)  {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	try {
    		email.writeTo(bytes);
    	} catch (Exception e) { 
    		//e.printStackTrace();
    	}
    	String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
    	Message message = new Message();
    	message.setRaw(encodedEmail); 
    	return message;
	 }

//****************************************************************************************************************

    @Override
	Matt getSlots(String userName, Matt matt, String accessToken){
    	MattData mattData = matt.getData();
		ArrayList<Boolean> slotsList = matt.getSlots();
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(TRANSPORT, JSON_FACTORY, credential)
			.setApplicationName(APPLICATION_NAME).build();  
		List<CalendarListEntry> items = null;
		try {
			CalendarList calendarList = calendarService.calendarList().list().execute();
			items = calendarList.getItems();
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
		int countDay = mattData.getnDays();
		int countSlotsInDay = (mattData.getEndHour() - mattData.getStartHour())	* (60 / mattData.getTimeSlot());
		int countSlots =countDay*countSlotsInDay;
		long startDateFirst = mattData.getStartDate().getTime() + mattData.getStartHour() * millisInHour /*+ timeZoneShift*/;
		long endDateFirst = mattData.getStartDate().getTime() + mattData.getEndHour() * millisInHour /*+ timeZoneShift*/;
		for (CalendarListEntry calendarListEntry : items){
			if (mattData.getDownloadCalendars(IFrontConnector.GOOGLE).contains(calendarListEntry.getId())) {
				String idCalendar = calendarListEntry.getId();
				DateTime startDateTime = new DateTime(startDateFirst);
				DateTime endDateTime = new DateTime(endDateFirst);
				Events events = null;
				ArrayList<Boolean> slotsMatt = new ArrayList<Boolean>();
				for(int day=0; day < countDay; day++){
					startDateTime = new DateTime(startDateFirst+24*millisInHour*day);
					endDateTime = new DateTime(endDateFirst+24*millisInHour*day);
					ArrayList<Boolean> slotsDay = new ArrayList<Boolean>();
					for (long i = 0; i < countSlotsInDay; i++)
						slotsDay.add(false);
					if(haveAvailableTimeInTheDay(slotsList, day, countSlotsInDay)){
						try {
							events = calendarService.events().list(idCalendar)
								.setTimeMin(startDateTime).setTimeMax(endDateTime).execute();
						} catch (Exception e) {
							//e.printStackTrace();
							return null;
						}
						List<Event> listEvents = events.getItems();
						for (Event event : listEvents) {
	//System.out.println(event.getReminders());						
							EventMAT eventMAT = new EventMAT(event);
							eventMAT.setSlots(slotsDay, mattData.getStartHour(), mattData.getTimeSlot());
						}
					}
					slotsMatt.addAll(slotsDay);
				}
				for(int i=0; i < countSlots; i++) if(slotsMatt.get(i)) slotsList.set(i, true);
			}
		}
		matt.setSlots(slotsList);
		return matt;
	}
    

    
	private boolean haveAvailableTimeInTheDay(ArrayList<Boolean> slotsList, int day,
			int countSlotsInDay) {
		int firstSlot = day*countSlotsInDay;
		int lastSlot = firstSlot+countSlotsInDay;
		for(int i=firstSlot; i<lastSlot; i++)
			if(!slotsList.get(i)) return true;
		return false;
	}

	@Override
	public void setEvent(String eventName, String userName, Matt matt, String accessToken ) {
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(
				TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build(); 
//		List<CalendarListEntry> items = null;
		try {
//			CalendarList calendarList = calendarService.calendarList().list().execute();	
//			items = calendarList.getItems();
		} catch (Exception e1) {}
		int nDays = matt.getData().getnDays();//number of days
		Date startDate  = matt.getData().getStartDate();
		int startHour = matt.getData().getStartHour();
		int timeSlot = matt.getData().getTimeSlot(); //in minutes
		ArrayList<Boolean> slots =matt.getSlots();
		int slotsByDay = slots.size()/nDays;
		long currentData = startDate.getTime()+startHour*millisInHour;
		int currentSlot = 1;
		for(Boolean slot: slots){
			if(!slot){
				com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
				event.setSummary(eventName);
				EventDateTime eventDTS = new EventDateTime();
				eventDTS.setDateTime(new DateTime(currentData));
				currentData+=timeSlot*millisInHour/60;
				EventDateTime eventDTE = new EventDateTime();
				eventDTE.setDateTime(new DateTime(currentData));
				event.setEnd(eventDTE);
				event.setStart(eventDTS);
				Reminders rem = new Reminders();
				rem.setUseDefault(false);
				List<EventReminder> eventRems = new ArrayList<EventReminder>();
				EventReminder eRemPop = new EventReminder();
				eRemPop.setMethod("popup");
				eRemPop.setMinutes(10);
				eventRems.add(eRemPop);
				EventReminder eRemEml = new EventReminder();
				eRemEml.setMethod("email");
				eRemEml.setMinutes(5);
				eventRems.add(eRemEml);
				rem.setOverrides(eventRems);
				event.setReminders(rem);
//System.out.println(event);
				try {
					calendarService.events().insert(userName, event).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				currentData+=timeSlot*millisInHour/60;
			}
			if((currentSlot++)%slotsByDay==0)
				currentData=startDate.getTime()+startHour*millisInHour+currentSlot/slotsByDay*24*millisInHour;
			
		}
	
	}

	@Override
	List<String> getCalendarNames(String accessToken) {
		List<String> calendarNames = new ArrayList<String>();
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(TRANSPORT, JSON_FACTORY, credential)
			.setApplicationName(APPLICATION_NAME).build();  
		List<CalendarListEntry> items = null;
		try {
			CalendarList calendarList = calendarService.calendarList().list().execute();
			items = calendarList.getItems();
		} catch (Exception e) {
			return null;
		}
		for (CalendarListEntry calendarListEntry : items){
			calendarNames.add(calendarListEntry.getSummary());
		}
		if(calendarNames.size()==0) return null;
		return calendarNames;
	}

	@Override
	void uploadMatt(Matt matt, String accessToken) {
		List<String> calendars = matt.getData().getDownloadCalendars(IFrontConnector.GOOGLE);
		if(calendars != null){
			GoogleCredential credential = getCredential(accessToken);
			calendarService = new com.google.api.services.calendar.Calendar.Builder(
					TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build(); 
//			List<CalendarListEntry> items = null;
			try {
//				CalendarList calendarList = calendarService.calendarList().list().execute();	
//				items = calendarList.getItems();
			} catch (Exception e1) {}
			String name = matt.getData().getName();//name of matt
			int nDays = matt.getData().getnDays();//number of days
			Date startDate  = matt.getData().getStartDate();
			int startHour = matt.getData().getStartHour();
			int timeSlot = matt.getData().getTimeSlot(); //in minutes
			ArrayList<Boolean> slots =matt.getSlots();
			int slotsByDay = slots.size()/nDays;
			long currentData = startDate.getTime()+startHour*millisInHour;
			int currentSlot = 1;
			removUploadingMattFromCalendars(matt, calendarService);
			for(Boolean slot: slots){
				if(!slot){
					com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
					event.setSummary(PREFIX_MAT+name);
					EventDateTime eventDTS = new EventDateTime();
					eventDTS.setDateTime(new DateTime(currentData));
					currentData+=timeSlot*millisInHour/60;
					EventDateTime eventDTE = new EventDateTime();
					eventDTE.setDateTime(new DateTime(currentData));
					event.setEnd(eventDTE);
					event.setStart(eventDTS);
					Reminders rem = new Reminders();
					rem.setUseDefault(false);
					List<EventReminder> eventRems = new ArrayList<EventReminder>();
					EventReminder eRemPop = new EventReminder();
					eRemPop.setMethod("popup");
					eRemPop.setMinutes(10);
					eventRems.add(eRemPop);
					EventReminder eRemEml = new EventReminder();
					eRemEml.setMethod("email");
					eRemEml.setMinutes(5);
					eventRems.add(eRemEml);
					rem.setOverrides(eventRems);
					event.setReminders(rem);
	//System.out.println(event);
					for(int i = 0; i<calendars.size(); i++){
						try {
							calendarService.events().insert(calendars.get(i), event).execute();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					currentData+=timeSlot*millisInHour/60;
				}
				if((currentSlot++)%slotsByDay==0)
					currentData=startDate.getTime()+startHour*millisInHour+currentSlot/slotsByDay*24*millisInHour;
				
			}
		
		}
	}

	private void removUploadingMattFromCalendars(Matt matt, Calendar calendarService2) {
		
	}

	@Override
	boolean sendInvitation(String userName, String inviteLetter, String[] contacts, String accessToken) {
		try {
    		GoogleCredential credential = getCredential(accessToken);
    		Gmail service = new Gmail.Builder(TRANSPORT, JSON_FACTORY, credential)
    			.setApplicationName(APPLICATION_NAME).build();
    		for(int i=0; i<contacts.length; i++){
	    		Message message;
	    		MimeMessage email;
	    		String[] contact = new String[1];
	    		contact[0] = contacts[i];
	    		email = createEmail(contact, userName, "My Available Time - Invitation", "Dear " + contacts[i] + ". " + inviteLetter, null, null);
	    		message =  createMessageWithEmail(email);
	    		service.users().messages().send("me", message).execute();
    		}
    	} catch (Exception e) {
    		//e.printStackTrace();
    		return false;  
    	} 
    	return true; 
	}
}
