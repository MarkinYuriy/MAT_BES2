package mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class Bes1Bes2 implements IBackConnector {
	
	private final static long millisInHour = 3600000;
	
	private long minPoint = Long.MAX_VALUE;
	private long maxPoint = 0;
		
	@Override
	public List<Boolean> getSlots(String username, String[] snName, MattData interval) throws IOException {
		int dayInterval = (interval.getEndHour() + 1 - interval.getStartHour())
				* (60 / interval.getTimeSlot());
		long millisInSlot = interval.getTimeSlot()*60000;
		ArrayList<Boolean> slots = new ArrayList<Boolean>();
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		
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

		calendarList = TestMatCalendar.client.calendarList().list().execute();

		List<CalendarListEntry> items = calendarList.getItems();

		for (CalendarListEntry calendarListEntry : items) {
			if (!calendarListEntry.getSummary().equals(TestMatCalendar.MAT_NAME)) {
				idCalendar = calendarListEntry.getId();
				try {
					events = TestMatCalendar.client.events().list(idCalendar)
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



	@Override
	public void setMatCalendar(String username, String[] snNames, List<Matt> matts) {
		
		int slotsInHour = 2;
		ArrayList<MattInfo> listMattInfo = new ArrayList<MattInfo>();

		for (Matt matt: matts) {
			MattInfo mattInfo = getMattInfo(matt, slotsInHour);
			listMattInfo.add(mattInfo);
		}		
		MattInfo result = getResultMattInfo(listMattInfo, slotsInHour);
		try {
			createEventsFromMattInfo(result, slotsInHour);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static long getStartPoint(MattData data) {
		return data.startDate.getTime() + data.startHour*millisInHour;
	}

	private static long getEndPoint(MattData data) {
		return data.startDate.getTime()
				+ ((data.nDays - 1) * 24 + data.endHour + 1)*millisInHour;
	}

	private void addEvent(Calendar calendar, long startPoint, int slotsInHour)
			throws IOException {

		Event event = new Event();
		event.setSummary("Available time");
		Date startDate = new Date(startPoint*millisInHour/slotsInHour);
		System.out.println(startDate.toString());
		Date endDate = new Date(startDate.getTime() + millisInHour/2);
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		TestMatCalendar.client.events().insert(calendar.getId(), event)
				.execute();
	}
	
	private MattInfo getMattInfo(Matt matt, int slotsInHour){
		
		int srcSlotInHour = 60/matt.data.timeSlot;
		int slotsInDay = 24 * slotsInHour;
		long start = 0;
		long end = 0;
		minPoint = ((start = getStartPoint(matt.data)) < minPoint) ? start:minPoint;
		maxPoint = ((end = getEndPoint(matt.data)) > maxPoint) ? end:maxPoint;
		long startSlot = start / millisInHour * slotsInHour;
		long endSlot = end / millisInHour * slotsInHour;
		
		int slotSize = (int) (endSlot - startSlot);
		ArrayList<Boolean> mattInfoSlots = new ArrayList<Boolean>();
		for(int i=0; i<slotSize; i++)
			mattInfoSlots.add(false);
		
		int intervals = (matt.data.endHour - matt.data.startHour +1) * slotsInHour;

		int dstSlot = 0;
		int srcSlot = 0;
		int ratio;
		
		if (intervals==slotsInDay && slotsInHour==srcSlotInHour) {
			mattInfoSlots = matt.slots;
		}
		else if (intervals < slotsInDay) {
			while (dstSlot < slotSize) {
				if (dstSlot % slotsInDay < intervals) {
					if(slotsInHour==srcSlotInHour){
						mattInfoSlots.set(dstSlot++, matt.slots.get(srcSlot++));
					}
					else if(slotsInHour>srcSlotInHour){
						ratio = slotsInHour/srcSlotInHour;
						for(int j=0; j<ratio; j++)
							mattInfoSlots.set(dstSlot++, matt.slots.get(srcSlot));
						srcSlot++;
					}
					else if(slotsInHour<srcSlotInHour){
						ratio = srcSlotInHour/slotsInHour;
						Boolean value = false;
						for(int j=slotsInHour; j<ratio; j++)
							value = value || matt.slots.get(srcSlot++);
						mattInfoSlots.set(dstSlot++, value);
					}
				}
			}
		} 
		return new MattInfo(startSlot, endSlot, mattInfoSlots, slotsInHour);
	}

	private MattInfo getResultMattInfo(List<MattInfo> listMattInfo, int slotsInHour){
		long startSlot = minPoint / millisInHour * slotsInHour;
		long endSlot = maxPoint / millisInHour * slotsInHour;
		int slotSize = (int) (endSlot - startSlot);
		
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		for (long i = 0; i < slotSize; i++)
			resultSlots.add(false);

		long currentSlot = startSlot;
		ListIterator<Boolean> litr = resultSlots.listIterator();
		while(litr.hasNext()) {
			litr.next();
			for (MattInfo mattInfo: listMattInfo) {
				if (currentSlot >= mattInfo.startSlot && currentSlot <= mattInfo.endSlot) {
					if (mattInfo.slots.get((int) (currentSlot - mattInfo.startSlot))) {
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
	
	private void createEventsFromMattInfo(MattInfo mattInfo, int slotsInHour) throws IOException{
		CalendarList calendarList = TestMatCalendar.client.calendarList().list().execute();

		List<CalendarListEntry> items = calendarList.getItems();

		for (CalendarListEntry calendarListEntry : items) {
			if (calendarListEntry.getSummary().equals(TestMatCalendar.MAT_NAME)) {
				TestMatCalendar.client.calendars().delete(calendarListEntry.getId()).execute();
				System.out.println(calendarListEntry.getId());
				
			}
		}
		Calendar ourCalendar=TestMatCalendar.client.calendars()
				.insert(new Calendar().setSummary(TestMatCalendar.MAT_NAME)).execute();
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
}
