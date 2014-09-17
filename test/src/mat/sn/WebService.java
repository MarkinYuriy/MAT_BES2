package mat.sn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class WebService {

    @Autowired
    IFesBes2 google;
    private static final String username = "user@server.com";

    @RequestMapping({"/"})
    public String homeMethod(Model model) {
        String[] data = google.getApplicationData(IFesBes2.GOOGLE);
        String clientID = data[IFesBes2.INDEX_ID];
        StringBuffer scopes = new StringBuffer();
        for (int i = IFesBes2.INDEX_SCOPES; i < data.length; i++) {
            scopes.append(data[i]);
            scopes.append(" ");
        }

        //Add attributes to read from google_signin.jsp
        model.addAttribute("id", clientID);
//        model.addAttribute("secret", CLIENT_SECRET);
        model.addAttribute("scopes", scopes);

        return "google_signin";
    }

    @RequestMapping({"/login"})
    public String login(HttpServletRequest request, Model model) throws IOException {
        //Receive google authorization code from jsp and send it to the service
        String code = request.getParameter("code");
        if (code!=null) {
            google.authorize(username, IFesBes2.GOOGLE, code);
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