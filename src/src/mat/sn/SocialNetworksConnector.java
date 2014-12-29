package mat.sn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mat.IBackConnector;
import mat.IFrontConnector;
import mat.Matt;

public class SocialNetworksConnector implements IFrontConnector, IBackConnector {
    protected static final String UNSUPPORTED_SN = " is unsupported social network.";
    protected static final String AUTH_ERROR = "Authentication failed. Please, try again later.";
    protected static final String NO_AUTH = "Authentication required.";

    //contains MAT user's user name (email) as a key and user's social networks tokens
    private static HashMap<String, HashMap<String, TokenData>> userData =
            new HashMap<String, HashMap<String, TokenData>>();

    private static final String PACKAGE = "mat.sn.";
//****************************************************************************************************************

    //Method for class reflection. Returns object of class with name received.
    private SocialNetwork getInstance(String socialNetwork) {
        try {
            return  (SocialNetwork) Class.forName(PACKAGE + socialNetwork).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(socialNetwork + UNSUPPORTED_SN);
        }
    }

    private String getToken(String username, String socialName) {
        if (!userData.containsKey(username))
            throw new SecurityException(NO_AUTH);
        TokenData token = userData.get(username).get(socialName);
        if (token.isExpired()) {
            getInstance(socialName).refreshToken(token);
        }
        return token.getAccessToken();
    }

    @Override
    //Method for authorize into some social network using class reflection
    public boolean authorize(String username, String socialName, String authCode) {
        if (username == null || socialName == null || authCode == null)
            return false;
        try {
			TokenData token = getInstance(socialName).retrieveToken(authCode);
			HashMap<String, TokenData> socialNetworks = userData.get(username);
			if (socialNetworks == null) {
			    socialNetworks = new HashMap<String, TokenData>();
			}

			socialNetworks.put(socialName, token);
			userData.put(username, socialNetworks);
			return true;
		} catch (Exception e) {
			return false;
		}
    }
//****************************************************************************************************************

    @Override
    //Method for getting contacts from some social network
    public String[] getContacts(String username, String[] socialNames) {
        List<String> contacts = new LinkedList<String>();
        for (String socialNetwork : socialNames) {
            try {
				String accessToken = getToken(username, socialNetwork);
				contacts.addAll(getInstance(socialNetwork).getContacts(accessToken));
			} catch (Exception e) {	}
        }
        return contacts.toArray(new String[contacts.size()]);
    }
//****************************************************************************************************************

    @Override
    public boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName) {
    	try{
    		return getInstance(socialName).shareByMail(userName, urlMatt, contacts, getToken(userName, socialName));
    	} catch(Exception e){ 
    		return false;
    	}
    }
//****************************************************************************************************************

    @Override
    public String[] getAuthorizedSocialNames(String username) {
    	if(userData.containsKey(username)){
    		Set<String> sNames  = userData.get(username).keySet();
    		String[] arrStr = new String[sNames.size()];
    		int i=0;
    		for(String str: sNames)
    			arrStr[i++]=str;
    		return arrStr;
    	}
        return new String[0];
    }
//****************************************************************************************************************

    @Override
    //Method that provides to front-end server information for some social network login process
    public String[] getApplicationData(String socialName) {
    	try{
    		return getInstance(socialName).getApplicationData();
    	} catch(Exception e){ 
    		return null;
    	}
    }
//****************************************************************************************************************

	@Override
	public mat.Matt getSlots(String username, mat.Matt matt) {
		String[] snNames=matt.getData().getDownloadSN();
		if(snNames != null){	
			for (int i=0; i<snNames.length; i++){
				try{ 
					if(snNames[i]!=null)
						matt = getInstance(snNames[i]).getSlots(username, matt, getToken(username, snNames[i]));
				} catch(Exception e){ 
					//System.out.println(e.toString());
				}
			}
		}
		return matt;
	}
	
//****************************************************************************************************************

	@Override
	public void setEvent(String[] guestsEmail, String userName, Matt matt) {
		Google google = new Google();
		google.setEvent("MAT "+matt.getData().getName(), userName, matt, getToken(userName, SocialNetworksConnector.GOOGLE));
		String token = null;
		for(int i=0; i<guestsEmail.length; i++){
			token = getToken(guestsEmail[i], SocialNetworksConnector.GOOGLE);
			if(token!=null)
				google.setEvent("MAT "+matt.getData().getName()+" from "+userName, userName, matt, token);
		}
	}

	@Override
	public HashMap<String, List<String>> getAvailableCalendars(String userName) {
		String[] authSN = getAuthorizedSocialNames(userName);
		HashMap<String, List<String>> availableCalendars = new HashMap<String, List<String>>();
		for (int i=0; i<authSN.length; i++){
			try{		
				List<String> calendars = getInstance(authSN[i]).getCalendarNames(getToken(userName, authSN[i]));
				if(calendars!=null)
					availableCalendars.put(authSN[i], calendars);
			} catch(Exception e) { }
		}
		return availableCalendars;
	}

	@Override
	public void uploadMatt(String username, mat.Matt matt) {
		String[] snNames = matt.getData().getUploadSN();
		for (int i=0; i<snNames.length; i++){
			try{		
				getInstance(snNames[i]).uploadMatt(matt, getToken(username, snNames[i]));
			} catch(Exception e) { }
		}
	}

	@Override
	public void sendInvitation(String userName, String name, String tableName, String[] contacts) {
		String invitLetter = name + " invites you for " + tableName 
				+ ". Please access www.myavailabletime.com";
		try{
    		getInstance(IFrontConnector.GOOGLE).sendInvitation(userName, invitLetter, contacts, getToken(userName, IFrontConnector.GOOGLE));
    	} catch(Exception e){ 
    		System.out.println(e.toString());
    	}
	}
}
