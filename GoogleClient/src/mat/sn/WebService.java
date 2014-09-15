package mat.sn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@Controller
public class WebService {
//    static {
//        try {
//            Reader reader = new FileReader("/Users/broleg/Dropbox/TelRan/MAT_Project/GoogleClient/client_secrets.json");
//            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
//        } catch (IOException e) {
//            throw new Error("No client_secrets.json found", e);
//        }
//    }
//    private static final String CLIENT_ID = clientSecrets.getWeb().getClientId();
//    private static final String CLIENT_SECRET = clientSecrets.getWeb().getClientSecret();

    @Autowired
    IFesBes2 google;
    private static final String username = "gobrol@gmail.com";
    private static final String mailServer = "gmail";

    @RequestMapping({"/"})
    public String homeMethod(Model model) {
        return "home";
    }

    @RequestMapping({"/login"})
    public String login(HttpServletRequest request, Model model) throws IOException {
        String token = request.getParameter("token");
        google.addToken(username, mailServer, token);
        return "home";
    }

    @RequestMapping({"/contacts"})
    public String getContacts(Model model) {
        String[] contacts = google.getContacts(username, mailServer);
        StringBuilder buffer = new StringBuilder();
        for (String contact : contacts) {
            buffer.append(contact);
            buffer.append("<br>");
        }
        model.addAttribute("contacts", buffer);
        return "contacts_form";
    }
}
