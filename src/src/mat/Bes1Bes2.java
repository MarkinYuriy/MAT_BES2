package mat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class Bes1Bes2 implements IBes1Bes2 {
	private static com.google.api.services.calendar.Calendar client;

	private static final List<String> SCOPE = Arrays
			.asList("https://www.googleapis.com/auth/calendar");
	private static final String APP_NAME = "Calendar API Quickstart";
	private static final String USER = "natalia.sheshukov@gmail.com";
	private static final String CLIENT_SECRET_PATH = "C:/Users/Natalia/workspace/GmailQuick/src/client_secrets.json";
	private static GoogleClientSecrets clientSecrets;

	private static Calendar ourCalendar;

	@Override
	public List<Boolean> getSlots(String username, String[] snNames,
			MattData interval) {
		// TODO Auto-generated method stub
		return null;
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
		GoogleCredential credential = new GoogleCredential()
				.setFromTokenResponse(response);
		//creating Matts
		MattData mData= new MattData("mat1", 1, new Date(), 0, 23, 1, "password");
		ArrayList<Boolean> slots = new ArrayList<Boolean> ();
		
		Matt mat1 = new Matt();
		mat1.data=mData;
		mat1.slots = slots;
		for(int i=0; i<46; i++){
			slots.add((int)(Math.random()*2)==0?false:true);
		}
		
		List<Matt> matts=new ArrayList<Matt>();
		matts.add(mat1);
		
		client = new com.google.api.services.calendar.Calendar.Builder(
				httpTransport, jsonFactory, credential).setApplicationName(
				APP_NAME).build();
		 ourCalendar = client.calendars().insert(new Calendar().setSummary("Mat5")).execute(); 
		 setMatCalendar1(null, null, matts);
 
	}

	@Override
	public void setMatCalendar(String username, String[] snNames,
			List<Matt> matts) {}
	 
	public static void setMatCalendar1(String username, String[] snNames,
			List<Matt> matts) {
		long minPoint = Long.MAX_VALUE;
		long maxPoint = 0;
		long start = 0;
		long end = 0;
		int size = matts.size();
		MattInfo mattInfo = null;
		ArrayList<MattInfo> listMattInfo = new ArrayList<MattInfo>();
		for (int i = 0; i < size; i++) {
			Matt matt = matts.get(i);
			minPoint = ((start = getStartPoint(matt)) < minPoint) ? start
					: minPoint;
			maxPoint = ((end = getEndPoint(matt)) > maxPoint) ? end : maxPoint;
			listMattInfo.add(new MattInfo((int) start /( 30 * 60 * 1000),
					(int) end / (30 * 60 * 1000), matt.slots));
		}
		System.out.println(maxPoint+" "+minPoint);
		int resultSize = (int) ((maxPoint - minPoint) / (30 * 60 * 1000));
		System.out.println(resultSize);
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>(resultSize);
		Collections.fill(resultSlots, false);
		MattInfo result = new MattInfo((int) minPoint / (30 * 60 * 1000),
				(int) maxPoint /( 30 * 60 * 1000), resultSlots);
		for (int i = 0; i < resultSize; i++) {
			for (int j = 0; j < size; j++) {
				if ((result.startPoint + i) >= listMattInfo.get(j).startPoint
						&& (result.startPoint + i) <= listMattInfo.get(j).endPoint) {
					if ((listMattInfo.get(j)).slots.get(i)) {
						resultSlots.set(i, true);
						break;
					}
				}
			}
		}
		int startPoint = result.endPoint;
		for (Boolean slot : result.slots) {
			if (slot) {
				try {
					addEvent(ourCalendar,  startPoint);
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
		return matt.data.startDate.getTime()
				+ (matt.data.nDays * 24 + matt.data.endHour) * 60 * 60 * 1000;
	}

 
	private static Calendar updateCalendar(Calendar calendar)
			throws IOException {
		View.header("Update Calendar");
		Calendar entry = new Calendar();
		entry.setSummary("Updated Calendar for Testing");
		Calendar result = client.calendars().patch(calendar.getId(), entry)
				.execute();
		View.display(result);
		return result;
	}

	private static void addEvent(Calendar calendar, int startPoint) throws IOException {
		 
		Event event = newEvent( startPoint);
		Event result = client.events().insert(calendar.getId(), event)
				.execute();
		View.display(result);
	}

	private static Event newEvent(int startPoint) {
		Event event = new Event();
		event.setSummary("Reserved time");
		Date startDate = new Date(startPoint * 1800000);
		Date endDate = new Date(startDate.getTime() + 1800000);
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		return event;
	}
}
