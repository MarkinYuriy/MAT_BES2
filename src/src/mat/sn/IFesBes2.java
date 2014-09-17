package mat.sn;

public interface IFesBes2 {
    /* *********************************************** */
    //PROTOCOL CONSTANTS
    //social networks names
    public static final String APPLE = "Apple";
    public static final String FACEBOOK = "Facebook";
    public static final String GOOGLE = "Google";
    public static final String TWITTER = "Twitter";
    public static final String WINDOWS = "Windows";

    //indexes for application data array (for Google)
    public static final int INDEX_ID = 0;
    public static final int INDEX_SCOPES = 1;
    /* ********************************************** */

    String[] getContacts(String username, String[] socialNames);
    boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName);

    String[] getAuthorizedSocialNames(String username);
    boolean authorize(String username, String socialName, String authCode);
    String[] getApplicationData(String socialName);
}