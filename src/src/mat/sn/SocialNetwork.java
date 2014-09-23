package mat;

 
import java.io.IOException;
import java.util.List;

public abstract class SocialNetwork {
    //Methods that all the social networks must implement
    abstract List<String> getContacts(String accessToken);
    abstract boolean shareByMail(String urlMatt, String[] contacts, String accessToken);
    abstract TokenData retrieveToken(String authCode);
    abstract boolean refreshToken(TokenData token);
    abstract String[] getApplicationData();
    abstract void setMatCalendar(List<Matt> matts, String accessToken);
    abstract List<Boolean> getSlots(MattData interval, String accessToken) throws IOException;
}
