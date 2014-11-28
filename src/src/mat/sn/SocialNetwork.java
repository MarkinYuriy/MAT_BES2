package mat.sn;

import java.util.List;

import mat.Matt;
import mat.MattData;

public abstract class SocialNetwork{
    //Methods that all the social networks must implement
    abstract List<String> getContacts(String accessToken);
    abstract boolean shareByMail(String userName, String urlMatt, String[] contacts, String accessToken);
    abstract TokenData retrieveToken(String authCode);
    abstract boolean refreshToken(TokenData token);
    abstract String[] getApplicationData();
    abstract void setMatCalendar(List<Matt> matts, String accessToken);
    abstract List<Boolean> getSlots(String userName, MattData interval, String accessToken);
    abstract void setEvent(String eventName, String userName, Matt matt, String accessToken);
}
