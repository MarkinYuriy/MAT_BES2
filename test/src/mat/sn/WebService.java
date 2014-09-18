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
    IConnector google;
    private static final String username = "user@server.com";

    @RequestMapping({"/"})
    public String homeMethod(Model model) {
        String[] data = new String[0];
        try {
            data = google.getApplicationData(IConnector.GOOGLE);
        } catch (Exception e) {
            e.getMessage();
        }
        String clientID = data[IConnector.INDEX_ID];
        StringBuffer scopes = new StringBuffer();
        for (int i = IConnector.INDEX_SCOPES; i < data.length; i++) {
            scopes.append(data[i]);
            scopes.append(" ");
        }

        //Add attributes to read from google_signin.jsp
        model.addAttribute("id", clientID);
        model.addAttribute("scopes", scopes);

        return "google_signin";
    }

    @RequestMapping({"/home"})
    public String home(Model model) {
        return "google_signin";
    }

    @RequestMapping({"/login"})
    public String login(HttpServletRequest request, Model model) throws IOException {
        //Receive google authorization code from jsp and send it to the service
        String code = request.getParameter("code");
        if (code!=null) {
            try {
                google.authorize(username, "Googl", code);
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
                return "error_form";
            }
        }
        return "google_signin";
    }

    @RequestMapping({"/contacts"})
    public String getContacts(Model model) {
        String[] socialNames = new String[1];
        socialNames[0] = IConnector.GOOGLE;
        String[] contacts;
        try {
            contacts = google.getContacts(username, socialNames);
        } catch (SecurityException e) {
            model.addAttribute("error", e.getMessage());
            return "error_form";
        }
        StringBuilder buffer = new StringBuilder();
        for (String contact : contacts) {
            buffer.append(contact);
            buffer.append("<br>");
        }
        model.addAttribute("contacts", buffer);
        return "contacts_form";
    }
}