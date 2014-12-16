package mat.sn;

import java.util.List;

import mat.Matt;

public abstract class SocialNetwork{
    //Methods that all the social networks must implement
    abstract List<String> getContacts(String accessToken);
    abstract boolean shareByMail(String userName, String urlMatt, String[] contacts, String accessToken);
    abstract TokenData retrieveToken(String authCode);
    abstract boolean refreshToken(TokenData token);
    abstract String[] getApplicationData();
    abstract Matt getSlots(String userName, Matt matt, String accessToken);
    abstract void setEvent(String eventName, String userName, Matt matt, String accessToken);
	abstract List<String> getCalendarNames(String accessToken);
	abstract void uploadMatt(Matt matt, String accessToke);
}
