package mat.sn;

import java.util.List;

public abstract class SocialNetwork extends SocialNetworks{
    //Methods that all the social networks must implement
    abstract List<String> getContacts(String accessToken);
    abstract boolean shareByMail(String urlMatt, String[] contacts, String accessToken);
    abstract TokenData retrieveToken(String authCode);
    abstract boolean refreshToken(TokenData token);
    abstract String[] getApplicationData();
}
