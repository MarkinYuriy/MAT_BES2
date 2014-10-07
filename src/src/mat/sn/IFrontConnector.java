package mat.sn;

public interface IFrontConnector {
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


    //May throw unchecked SecurityException or RuntimeException. It is strongly recommended to make try/catch block.
    String[] getContacts(String username, String[] socialNames);
    //May throw unchecked SecurityException. Strongly recommended to make try/catch block.
    boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName);

    String[] getAuthorizedSocialNames(String username);
    //May throw unchecked SecurityException or RuntimeException. It is strongly recommended to make try/catch block.
    boolean authorize(String username, String socialName, String authCode);
    //May throw unchecked RuntimeException. It is strongly recommended to make try/catch block.
    String[] getApplicationData(String socialName);
}