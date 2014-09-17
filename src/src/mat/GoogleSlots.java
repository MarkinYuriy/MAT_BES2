package mat;

import java.io.*;
import java.util.*;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

public class GoogleSlots implements IBes1Bes2 {

	@Override
	public boolean setIdentity(String snUsername, String matUsername, String snName) {
		return false;
	}
	@Override
		public List<Boolean> getSlots (String username, String[] snName, MattData interval) throws IOException {
			int dayInterval = (interval.getEndHour()-interval.getStartHour())*(60/interval.getTimeSlot());
			ArrayList<Boolean> currentSlots = new ArrayList<Boolean>();
			ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
			String pageToken = null;
			CalendarList calendarList  = null;
			Calendar client = null;
			String idCalendar = null;
			DateTime startResponse = null;
			DateTime endResponse = null;
			Events events = null;
			long startDate2 = interval.getStartDate().getTime() + interval.getStartHour() * 60 * 60 * 1000;
			startResponse = new DateTime(startDate2);
			
			long endDate2 = startDate2 + (interval.getnDays() - 1) * 24 + interval.getEndHour() * 60 * 60 * 1000;
			endResponse = new DateTime(endDate2);

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
							if(flagFull)break;
							long startEvent = (event.getStart().getDateTime()).getValue() / ((interval.getTimeSlot() * 60 * 1000));
							long endEvent = (event.getEnd().getDateTime()).getValue() / ((interval.getTimeSlot() * 60 * 1000));
							
							while (currentSlot >= startEvent && currentSlot <= endEvent && currentSlot < endSlot) {
								currentSlots.set((int)(currentSlot-startSlot), true);
								currentSlot++;
								if(currentSlot>endSlot)flagFull=true;
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
}
