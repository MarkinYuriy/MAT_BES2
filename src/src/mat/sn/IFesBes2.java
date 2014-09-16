package mat.sn;

public interface IFesBes2 {
    /* *********************************************** */
    //PROTOCOL CONSTANTS
    //social networks names
    public static final String GOOGLE = "google";
    public static final String APPLE = "apple";
    public static final String TWITTER = "twitter";
    public static final String FACEBOOK = "facebook";
    public static final String WINDOWS = "windows";
    /* ********************************************** */

    String[] getContacts(String username, String[] socialNames);//assuming Google
    boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName);

    String[] getAuthorizedSocialNames(String username);
    boolean setToken(String username, String socialName, String accessToken, String refreshToken);
}