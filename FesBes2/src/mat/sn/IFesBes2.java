package mat.sn;

public interface IFesBes2 {
    String [] getContacts(String username, String mailServer);
    boolean shareByMail(String urlMatt, String[] contacts, String userName);
    String [] getContactsGooglePlus(String circleName, String username);
    boolean createCircle(String circleName, String username);
    boolean addContactToCircle(String circleName, String contact, String username);
    boolean removeContactFromCircle(String circleName, String contact, String userName);
    boolean shareByGoogle(String urlMatt, String circleName, String userName );
    boolean removeCircle(String circleName, String username);

    void addToken(String userName, String sn, String token);
    String getToken(String userName, String sn);
}