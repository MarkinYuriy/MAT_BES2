package mat.sn;

public interface IFesBes2 {
    /* *********************************************** */
    //PROTOCOL CONSTANTS
    //social networks names
    public static final String GOOGLE = "Google";
    public static final String APPLE = "Apple";
    public static final String TWITTER = "Twitter";
    public static final String FACEBOOK = "Facebook";
    public static final String WINDOWS = "Windows";
    /* ********************************************** */

    String[] getContacts(String username, String[] socialNames);//assuming Google
    boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName);

    String[] getAuthorizedSocialNames(String username);
    boolean setToken(String username, String socialName, String accessToken, String refreshToken);
}