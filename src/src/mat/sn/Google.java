package mat;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

public class Google extends SocialNetwork {
	private static final String APPLICATION_NAME = "MyAvailableTime";
	private static final String MAT_NAME = "My Available Time";
	private static final String EVENT_NAME = "Available Time";
	private final static long millisInHour = 3600000;
	private long minPoint = Long.MAX_VALUE;
	private long maxPoint = 0;

	// Default HTTP transport to use to make HTTP requests.
	private static final HttpTransport TRANSPORT = new NetHttpTransport();

	// Default JSON factory to use to deserialize JSON.
	private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

	// Creates a client secrets object from the client_secrets.json file.
	private static GoogleClientSecrets clientSecrets;

	// Collection of scopes to work with
	private static ArrayList<String> scopes = new ArrayList<String>();

	// Prepare needed information
	static {
		try {
			// Read json file to get client secrets data
			Reader reader = new FileReader(
					"/Users/broleg/Dropbox/TelRan/MAT_Project/FesBes2/web/client_secrets.json");
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
		} catch (IOException e) {
			throw new Error("No client_secrets.json found", e);
		}
		// prepare needed scopes
		scopes.add("https://www.googleapis.com/auth/plus.login");// scope for
																	// google
																	// authorization
		scopes.add("https://www.google.com/m8/feeds");// scope for working with
														// gmail (read/write)
		scopes.add("https://www.googleapis.com/auth/calendar");// scope for
																// working with
																// calendars
																// (read/write)
		scopes.add("https://www.googleapis.com/auth/gmail.compose");// scope for
																	// working
																	// with
																	// gmail
	}

	protected static final String CLIENT_ID = clientSecrets.getWeb()
			.getClientId();
	protected static final String CLIENT_SECRET = clientSecrets.getWeb()
			.getClientSecret();

	// creating service that allows working with gmail API
	 private static final ContactsService gmailService = new ContactsService(APPLICATION_NAME);
	
	private com.google.api.services.calendar.Calendar calendarService;

	// current feed's URL request
	private static final String contactsRequestURL = "http://www.google.com/m8/feeds/contacts/default/full";

	// ****************************************************************************************************************

	@Override
	protected LinkedList<String> getContacts(String accessToken) {
		LinkedList<String> contacts = new LinkedList<String>();
		try {
			gmailService.setHeader("Authorization", "Bearer " + accessToken);
			gmailService.setUserToken(accessToken);// setting credentials
													// according to token
													// received
			URL feedUrl = new URL(contactsRequestURL);// forming full URL
														// request for current
														// user
			ContactFeed feeds = gmailService
					.getFeed(feedUrl, ContactFeed.class);// getting contacts
															// full info
			// getting emails from contacts info
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

	@Override
	protected TokenData retrieveToken(String authCode) {
		// Upgrade the authorization code into an access and refresh token.
		try {
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
					TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET,
					authCode, "postmessage").setScopes(scopes).execute();
			String accessToken = tokenResponse.getAccessToken();
			String refreshToken = tokenResponse.getRefreshToken();
			return new TokenData(accessToken, refreshToken);
		} catch (IOException e1) {
			throw new SecurityException(SocialNetworksConnector.AUTH_ERROR);
		}
	}

	@Override
	// Returns array with client id (read from client_secrets.json) and scopes
	// needed
	protected String[] getApplicationData() {
		String[] data = new String[1 + scopes.size()];
		data[IFrontConnector.INDEX_ID] = CLIENT_ID;
		int i = IFrontConnector.INDEX_SCOPES;
		for (String scope : scopes) {
			data[i++] = scope;
		}
		return data;
	}

	@Override
	public boolean shareByMail(String urlMatt, String[] contacts,
			String accessToken) {

		GoogleCredential credential = getCredential(accessToken);

		Gmail service = new Gmail.Builder(TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		Message message;
		MimeMessage email;

		email = createEmail(contacts,
				"natalia.sheshukov@gmail.com", "My Available Time - Calendar",
				urlMatt, null, null);
		message = createMessageWithEmail(email);
		try {
			service.users().messages().send("me", message).execute();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// Get new access token using refresh token
	protected boolean refreshToken(TokenData token) {
		try {
			GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
					TRANSPORT, JSON_FACTORY, token.getRefreshToken(),
					CLIENT_ID, CLIENT_SECRET).setScopes(scopes).execute();
			token.setAccessToken(tokenResponse.getAccessToken());
			return true;
		} catch (IOException e) {
			throw new SecurityException(SocialNetworksConnector.NO_AUTH);
		}
	}

	private MimeMessage createEmail (String[] to, String from,
			String subject, String bodyText, String fileDir, String filename) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);
		InternetAddress[] tAddress = new InternetAddress[to.length];
		try {
			for (int i = 0; i < to.length; i++) {
				tAddress[i] = new InternetAddress(to[i]);
			}
			InternetAddress fAddress = new InternetAddress(from);

			email.setFrom(fAddress);
			email.addRecipients(javax.mail.Message.RecipientType.TO, tAddress);
			email.setSubject(subject);

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(bodyText, "text/plain");
			mimeBodyPart.setHeader("Content-Type",
					"text/plain; charset=\"UTF-8\"");

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

	private Message createMessageWithEmail(MimeMessage email) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try {
			email.writeTo(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes
				.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	@Override
	public void setMatCalendar(List<Matt> matts, String accessToken) {
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(
				TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build(); 
		int slotsInHour = 2;
		ArrayList<MattInfo> listMattInfo = new ArrayList<MattInfo>();

		for (Matt matt : matts) {
			MattInfo mattInfo = getMattInfo(matt, slotsInHour);
			listMattInfo.add(mattInfo);
		}
		MattInfo result = getResultMattInfo(listMattInfo, slotsInHour);
		try {
			createEventsFromMattInfo(result, slotsInHour, accessToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static long getStartPoint(MattData data) {
		return data.startDate.getTime() + data.startHour * millisInHour;
	}

	private static long getEndPoint(MattData data) {
		return data.startDate.getTime()
				+ ((data.nDays - 1) * 24 + data.endHour + 1) * millisInHour;
	}

	private void addEvent(Calendar calendar, long startPoint, int slotsInHour)
			throws IOException {

		Event event = new Event();
		event.setSummary(EVENT_NAME);
		Date startDate = new Date(startPoint * millisInHour / slotsInHour);
		System.out.println(startDate.toString());
		Date endDate = new Date(startDate.getTime() + millisInHour / 2);
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		calendarService.events().insert(calendar.getId(), event)
				.execute();
	}

	private MattInfo getMattInfo(Matt matt, int slotsInHour) {

		int srcSlotInHour = 60 / matt.data.timeSlot;
		int slotsInDay = 24 * slotsInHour;
		long start = 0;
		long end = 0;
		minPoint = ((start = getStartPoint(matt.data)) < minPoint) ? start
				: minPoint;
		maxPoint = ((end = getEndPoint(matt.data)) > maxPoint) ? end : maxPoint;
		long startSlot = start / millisInHour * slotsInHour;
		long endSlot = end / millisInHour * slotsInHour;

		int slotSize = (int) (endSlot - startSlot);
		ArrayList<Boolean> mattInfoSlots = new ArrayList<Boolean>();
		for (int i = 0; i < slotSize; i++)
			mattInfoSlots.add(false);

		int intervals = (matt.data.endHour - matt.data.startHour + 1)
				* slotsInHour;

		int dstSlot = 0;
		int srcSlot = 0;
		int ratio;

		if (intervals == slotsInDay && slotsInHour == srcSlotInHour) {
			mattInfoSlots = matt.slots;
		} else if (intervals < slotsInDay) {
			while (dstSlot < slotSize) {
				if (dstSlot % slotsInDay < intervals) {
					if (slotsInHour == srcSlotInHour) {
						mattInfoSlots.set(dstSlot++, matt.slots.get(srcSlot++));
					} else if (slotsInHour > srcSlotInHour) {
						ratio = slotsInHour / srcSlotInHour;
						for (int j = 0; j < ratio; j++)
							mattInfoSlots.set(dstSlot++,
									matt.slots.get(srcSlot));
						srcSlot++;
					} else if (slotsInHour < srcSlotInHour) {
						ratio = srcSlotInHour / slotsInHour;
						Boolean value = false;
						for (int j = slotsInHour; j < ratio; j++)
							value = value || matt.slots.get(srcSlot++);
						mattInfoSlots.set(dstSlot++, value);
					}
				}
			}
		}
		return new MattInfo(startSlot, endSlot, mattInfoSlots, slotsInHour);
	}

	private MattInfo getResultMattInfo(List<MattInfo> listMattInfo,
			int slotsInHour) {
		long startSlot = minPoint / millisInHour * slotsInHour;
		long endSlot = maxPoint / millisInHour * slotsInHour;
		int slotSize = (int) (endSlot - startSlot);

		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		for (long i = 0; i < slotSize; i++)
			resultSlots.add(false);

		long currentSlot = startSlot;
		ListIterator<Boolean> litr = resultSlots.listIterator();
		while (litr.hasNext()) {
			litr.next();
			for (MattInfo mattInfo : listMattInfo) {
				if (currentSlot >= mattInfo.startSlot
						&& currentSlot <= mattInfo.endSlot) {
					if (mattInfo.slots
							.get((int) (currentSlot - mattInfo.startSlot))) {
						litr.set(false);
						break;
					} else {
						litr.set(true);
					}
				}
			}
			currentSlot++;
		}

		return new MattInfo(startSlot, endSlot, resultSlots, slotsInHour);
	}

	private void createEventsFromMattInfo(MattInfo mattInfo, int slotsInHour, String accessToken)
			throws IOException { 
		
		CalendarList calendarList = calendarService.calendarList().list().execute();	

		List<CalendarListEntry> items = calendarList.getItems();

		for (CalendarListEntry calendarListEntry : items) {
			if (calendarListEntry.getSummary().equals(MAT_NAME)) {
				calendarService.calendars()
						.delete(calendarListEntry.getId()).execute();
				System.out.println(calendarListEntry.getId());

			}
		}
		Calendar ourCalendar = calendarService.calendars()
				.insert(new Calendar().setSummary(MAT_NAME))
				.execute();
		long currentSlot = mattInfo.startSlot;
		for (Boolean slot : mattInfo.slots) {
			if (slot) {
				try {
					addEvent(ourCalendar, currentSlot, slotsInHour);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			currentSlot++;
		}

	}

	@Override
	List<Boolean> getSlots(MattData interval, String accessToken) throws IOException {
		int dayInterval = (interval.getEndHour() + 1 - interval.getStartHour())
				* (60 / interval.getTimeSlot());
		long millisInSlot = interval.getTimeSlot()*60000;
		ArrayList<Boolean> slots = new ArrayList<Boolean>();
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		
		GoogleCredential credential = getCredential(accessToken);
		calendarService = new com.google.api.services.calendar.Calendar.Builder(
				TRANSPORT, JSON_FACTORY, credential).setApplicationName(
				APPLICATION_NAME).build();  
		
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

		calendarList = calendarService.calendarList().list().execute();

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

					long startEvent = (event.getStart().getDateTime())
							.getValue() / millisInSlot;
					long endEvent = (event.getEnd().getDateTime()).getValue()
							/ millisInSlot;

					if (startEvent < startSlot)	startEvent = startSlot;
					if (endEvent > endSlot) endEvent = endSlot;
					for (; startEvent < endEvent; startEvent++) {
						slots.set((int) (startEvent - startSlot), true);
					}
				}
			}
		}
		
		for (int index = 0; index < resultSize;) {
			resultSlots.addAll(slots.subList(index, index + dayInterval));
			index = index + 24 * (60 / interval.timeSlot);
		}
		return resultSlots;
	}

	private GoogleCredential getCredential(String accessToken) {

		GoogleCredential credential = new GoogleCredential.Builder()
		.setTransport(TRANSPORT).setJsonFactory(JSON_FACTORY)
		.setClientSecrets(CLIENT_ID, CLIENT_SECRET).build();
		credential.setAccessToken(accessToken);
		return credential;
	}
}
