package mat.sn;

public interface IFesBes2 {
    String [] getContacts(String username, String mailServer);//assuming Google
    boolean shareByMail(String urlMatt, String[] contacts, String userName);
    String [] getContactsGooglePlus(String circleName, String username);
    boolean createCircle(String circleName, String username);
    boolean addContactToCircle(String circleName, String contact, String username);
    boolean removeContactFromCIrcle(String circleName, String contact, String userName);
    boolean shareByGoogle(String urlMatt, String circleName, String userName );
    boolean removeCircle(String circleName, String username);
}