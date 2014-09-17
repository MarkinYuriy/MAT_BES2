package mat.sn;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

@Controller
public class WebService {
    //Default HTTP transport to use to make HTTP requests.
    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    //Default JSON factory to use to deserialize JSON.
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

    //Creates a client secrets object from the client_secrets.json file.
    private static GoogleClientSecrets clientSecrets;

    //Collection of scopes to work with
    private static ArrayList<String> scopes = new ArrayList<String>();

    //Prepare needed information
    static {
        try {
            //Read json file to get client secrets data
            Reader reader = new FileReader("/Users/broleg/Dropbox/TelRan/MAT_Project/GoogleClient/client_secrets.json");
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            throw new Error("No client_secrets.json found", e);
        }
        //prepare needed scopes
        scopes.add("https://www.googleapis.com/auth/plus.login");//scope for google authorization
        scopes.add("https://www.google.com/m8/feeds");//scope for working with gmail (read/write)
        scopes.add("https://www.googleapis.com/auth/calendar");//scope for working with calendars (read/write)
    }

    private static final String CLIENT_ID = clientSecrets.getWeb().getClientId();
    private static final String CLIENT_SECRET = clientSecrets.getWeb().getClientSecret();

    @Autowired
    IFesBes2 google;
    private static final String username = "user@server.com";

    @RequestMapping({"/"})
    public String homeMethod(Model model) {
        StringBuffer scopes_buffer = new StringBuffer();
        for (String scope: scopes) {
            scopes_buffer.append(scope);
            scopes_buffer.append(" ");
        }
        //Add attributes to read from google_signin.jsp
        model.addAttribute("id", CLIENT_ID);
        model.addAttribute("secret", CLIENT_SECRET);
        model.addAttribute("scopes", scopes_buffer);

        return "google_signin";
    }

    @RequestMapping({"/login"})
    public String login(HttpServletRequest request, Model model) throws IOException {
        //Receive google authorization code from jsp
        String code = request.getParameter("code");
        if (code!=null) {
            //Upgrade the authorization code into an access and refresh token.
            GoogleTokenResponse tokenResponse =
                    new GoogleAuthorizationCodeTokenRequest(TRANSPORT, JSON_FACTORY, CLIENT_ID,
                            CLIENT_SECRET, code, "postmessage").setScopes(scopes).execute();
            String access_token = tokenResponse.getAccessToken();
            String refresh_token = tokenResponse.getRefreshToken();
            //Store received tokens for later use
            google.setToken(username, IFesBes2.GOOGLE, access_token, refresh_token);
        }
        return "google_signin";
    }

    @RequestMapping({"/contacts"})
    public String getContacts(Model model) {
        String[] socialNames = new String[1];
        socialNames[0] = IFesBes2.GOOGLE;
        String[] contacts = google.getContacts(username, socialNames);
        StringBuilder buffer = new StringBuilder();
        for (String contact : contacts) {
            buffer.append(contact);
            buffer.append("<br>");
        }
        model.addAttribute("contacts", buffer);
        return "contacts_form";
    }
}