package mat.sn;

import java.util.List;

import mat.Matt;
import mat.MattData;

public interface IBackConnector {
	
	List<Boolean> getSlots(String username, String [] snNames,	MattData interval) ;
	void setMatCalendar(String username,String [] snNames,List<Matt> matts);
	
}
