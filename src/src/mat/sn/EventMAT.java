package mat.sn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.api.services.calendar.model.Event;

public class EventMAT{
	int startTime;//minutes from 00.00
	int endTime;//minutes
	
	public EventMAT(Event event) {
		if (event.getStart().getDate() == null) { //check if a whole-day event calendar
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(event.getStart().getDateTime().getValue());
			startTime = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
			cal.setTimeInMillis(event.getEnd().getDateTime().getValue());
			endTime = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
		} else {
			startTime = 0;
			endTime = 1440;
		} 					
System.out.println(event);
System.out.println(startTime);
System.out.println(endTime);
	}
	
	void setSlots(ArrayList<Boolean> slotsList, int startHour, int timeSlot){
		int currentTime = startHour*60;
		for(int i=0;i<slotsList.size();i++){
			if(startTime>=currentTime){
				if(startTime<currentTime+timeSlot) slotsList.set(i, true);
			} else {
				if(endTime > currentTime) slotsList.set(i, true);
			}
			currentTime+=timeSlot;
		}
	}

}
