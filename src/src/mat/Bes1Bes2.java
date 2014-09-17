package mat;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class Bes1Bes2 implements IBes1Bes2 {
	private static com.google.api.services.calendar.Calendar client;

	private static final List<String> SCOPE = Arrays
			.asList("https://www.googleapis.com/auth/calendar");
	private static final String APP_NAME = "Calendar API Quickstart";
	private static final String USER = "vasya.beersheva@gmail.com";
	private static final String CLIENT_SECRET_PATH = "D:/TEL-RAN/Workspace_java/MattProject/src/client_secret.json";
	private static GoogleClientSecrets clientSecrets;
	private static Calendar ourCalendar;
	private static GoogleCredential credential;

	public static List<Boolean> getSlots1(String username, String[] snName, MattData interval) throws IOException {
		int dayInterval=(interval.getEndHour()-interval.getStartHour())*(60/interval.getTimeSlot());
		ArrayList<Boolean> currentSlots = new ArrayList<Boolean>();
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		String pageToken = null;
		CalendarList calendarList  = null;
		com.google.api.services.calendar.Calendar service = null;
		String idCalendar = null;
		DateTime startResponse = null;
		DateTime endResponse = null;
		Events events = null;
		System.out.println(interval.getStartDate());
		long startDate2 = interval.getStartDate().getTime() + interval.getStartHour() * 60 * 60 * 1000;
		startResponse = new DateTime(startDate2);
		System.out.println(startDate2);
		System.out.println(startResponse);
		
		long endDate2 = interval.getStartDate().getTime() + (interval.getnDays() - 1) * 24 + (interval.getEndHour()) * 60 * 60 * 1000;
		endResponse = new DateTime(endDate2);
		System.out.println(endDate2);
		System.out.println(endResponse);

		long startSlot = startDate2 / ((interval.getTimeSlot() * 60 * 1000));
		long endSlot = endDate2 / ((interval.getTimeSlot() * 60 * 1000));
		System.out.println(startSlot+" "+endSlot);
		long resultSize= endSlot-startSlot;
		for (long i = 0; i < resultSize; i++)
			currentSlots.add(false);
		
		long currentSlot=startSlot;
		do {
		  try {
			calendarList = client.calendarList().list().execute();
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			List<CalendarListEntry> items = calendarList.getItems();
			Boolean flagFull=false;
			while (currentSlot <= endSlot) {
				for (CalendarListEntry calendarListEntry : items) {
					if(flagFull)break;
					idCalendar = calendarListEntry.getId();
					events = client.events().list(idCalendar).setTimeMin(startResponse).setTimeMax(endResponse).execute();
					List<Event> listEvents = events.getItems();
					
					for (Event event : listEvents) {
						
						 System.out.println(event.getSummary());
				         System.out.println(event.getId());
				         System.out.println(event.getStart().getDateTime());
				         System.out.println(event.getEnd().getDateTime());

						if(flagFull) break;
						long startEvent = (event.getStart().getDateTime()).getValue() / ((interval.getTimeSlot() * 60 * 1000));
						long endEvent = (event.getEnd().getDateTime()).getValue() / ((interval.getTimeSlot() * 60 * 1000));
						
						while (currentSlot >= startEvent && currentSlot < endEvent && currentSlot < endSlot) {
							currentSlots.set((int)(currentSlot-startSlot), true);
							currentSlot++;
							if(currentSlot>endSlot) flagFull=true;
						}
					}
				}
				currentSlot++;
			}
			for(int index=0; index<resultSize; ){
					resultSlots.addAll(currentSlots.subList(index, index+dayInterval));
					index=index+24*(60/interval.timeSlot);
			}
		  pageToken = events.getNextPageToken();
		  	
		
		  } while (pageToken != null);
		
			
		return resultSlots;
		}

	public static void main(String[] args) throws Exception {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		clientSecrets = GoogleClientSecrets.load(jsonFactory, new FileReader(
				CLIENT_SECRET_PATH));

		// Allow user to authorize via url.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, clientSecrets, SCOPE)
				.setAccessType("online").setApprovalPrompt("auto").build();
		String url = flow.newAuthorizationUrl()
				.setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).build();
		System.out
				.println("Please open the following URL in your browser then type"
						+ " the authorization code:\n" + url);

		// Read code entered by user.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		// Generate Credential using retrieved code.
		GoogleTokenResponse response = flow.newTokenRequest(code)
				.setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI)
				.execute();
		 credential = new GoogleCredential()
				.setFromTokenResponse(response);
		// creating Matts
		 
		 GregorianCalendar cld = new GregorianCalendar(2014, 8, 18, 0, 0);
		 System.out.println(cld);
		 Date d=new Date(cld.getTimeInMillis());
		 MattData mData = new MattData("mat1", 1, d, 8, 17, 30,
				"password");
		 System.out.println(d+" "+"from" +" "+8+" "+"till"+" "+17);
		 
	/*	ArrayList<Boolean> slots = new ArrayList<Boolean>();

		Matt mat1 = new Matt();
		mat1.data = mData;
		mat1.slots = slots;
		for (int i = 0; i < 94; i++) {
			slots.add((int) (Math.random() * 2) == 0 ? false : true);
		}

		List<Matt> matts = new ArrayList<Matt>();
		matts.add(mat1);*/

		client = new com.google.api.services.calendar.Calendar.Builder(
				httpTransport, jsonFactory, credential).setApplicationName(
				APP_NAME).build();
		
		/*ourCalendar = client.calendars()
				.insert(new Calendar().setSummary("Mat99")).execute();
		setMatCalendar1(null, null, matts);*/
		
		List<Boolean> Slots = getSlots1(null,null,mData);
		System.out.println(Slots.toString());

	}

	@Override
	public void setMatCalendar(String username, String[] snNames,
			List<Matt> matts) {
	}

	public static void setMatCalendar1(String username, String[] snNames,
			List<Matt> matts) {
		long minPoint = Long.MAX_VALUE;
		long maxPoint = 0;
		long start = 0;
		long end = 0;
		int slotsInHour = 2;
		int size = matts.size();
		ArrayList<MattInfo> listMattInfo = new ArrayList<MattInfo>();
		for (int i = 0; i < size; i++) {
			Matt matt = matts.get(i);

			minPoint = ((start = getStartPoint(matt)) < minPoint) ? start
					: minPoint;
			maxPoint = ((end = getEndPoint(matt)) > maxPoint) ? end : maxPoint;

			// System.out.println(start+" "+end);
			long startSlot = start / ((matt.data.getTimeSlot()) * 60 * 1000);
			long endSlot = end / ((matt.data.getTimeSlot()) * 60 * 1000);
			int slotSize = (int) (endSlot - startSlot);

			ArrayList<Boolean> mattInfoSlots = new ArrayList<Boolean>();

			int intervals = (matt.data.endHour - matt.data.startHour)
					* slotsInHour;
			if (intervals != 0) {
				for (int slot = 0, srcSlot = 0; slot < slotSize; slot++) {
					if (slot % (24 * (60/matt.data.getTimeSlot())) > intervals) {
						mattInfoSlots.add(false);
					} else {
						for (int j = 0; j < (matt.data.timeSlot == 1 ? 1
								: slotsInHour); j++)// it will be better to change
													// matt.data.timeSlot from 0/1 to
													// number of matt.data.slotsInHour
							mattInfoSlots.add(matt.slots.get(srcSlot));
						srcSlot++;
					}
				}
			} else {
				if (matt.data.timeSlot == 1)// it will be better to change
											// matt.data.timeSlot from 0/1 to
											// number of matt.data.slotsInHour
					mattInfoSlots = matt.slots;
				else {
					for (int slot = 0; slot < matt.slots.size(); slot++) {
						for (int j = 0; j < slotsInHour; j++)
							mattInfoSlots.add(matt.slots.get(slot));
					}
				}
			}
			// a need to add proper slots data
			listMattInfo.add(new MattInfo(startSlot, endSlot, mattInfoSlots,
					slotsInHour));
		}
		//System.out.println(maxPoint + " " + minPoint);
		long resultSize = (maxPoint - minPoint)
				/ ((60 / slotsInHour) * 60 * 1000);
		//System.out.println(resultSize);
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		for (long i = 0; i < resultSize; i++)
			resultSlots.add(false);
		MattInfo result = new MattInfo(
				(minPoint / ((60 / slotsInHour) * 60 * 1000)),
				(maxPoint / ((60 / slotsInHour) * 60 * 1000)),
				resultSlots, slotsInHour);

		for (int i = 0; i < resultSize; i++) {
			for (int j = 0; j < size; j++) {
				if ((result.startPoint + i) >= listMattInfo.get(j).startPoint
						&& (result.startPoint + i) <= listMattInfo.get(j).endPoint) {
					if ((listMattInfo.get(j)).slots.get(i)) {
						resultSlots.set(i, false);
						break;
					} else {
						resultSlots.set(i, true);
					}
				}
			}
		}
		long startPoint = result.endPoint;
		for (Boolean slot : result.slots) {
			if (slot) {
				try {
					addEvent(ourCalendar, startPoint);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			startPoint++;
		}

	}

	private static long getStartPoint(Matt matt) {
		return matt.data.startDate.getTime() + matt.data.startHour * 60 * 60
				* 1000;
	}

	private static long getEndPoint(Matt matt) {
		// return matt.data.startDate.getTime() + (matt.data.nDays * 24 +
		// matt.data.endHour) * 60 * 60 * 1000;
		return matt.data.startDate.getTime()
				+ ((matt.data.nDays - 1) * 24 + matt.data.endHour) * 60 * 60
				* 1000;
	}

	private static Calendar updateCalendar(Calendar calendar)
			throws IOException {
		// View.header("Update Calendar");
		Calendar entry = new Calendar();
		entry.setSummary("Updated Calendar for Testing");
		Calendar result = client.calendars().patch(calendar.getId(), entry)
				.execute();
		// View.display(result);
		return result;
	}

	private static void addEvent(Calendar calendar, long startPoint)
			throws IOException {

		Event event = newEvent(startPoint);
		Event result = client.events().insert(calendar.getId(), event)
				.execute();
		// View.display(result);
	}

	private static Event newEvent(long startPoint) {
		Event event = new Event();
		event.setSummary("Reserved time");
		Date startDate = new Date(startPoint * 1800000);
		System.out.println(startDate.toString());
		Date endDate = new Date(startDate.getTime() + 1800000);
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		return event;
	}

	@Override
	public List<Boolean> getSlots(String username, String[] snNames,
			MattData interval){
		// TODO Auto-generated method stub
		return null;
	}
}
