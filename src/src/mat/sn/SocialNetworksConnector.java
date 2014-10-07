package mat.sn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mat.MattData;
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
        TokenData token = getInstance(socialName).retrieveToken(authCode);

        HashMap<String, TokenData> socialNetworks = userData.get(username);
        if (socialNetworks == null) {
            socialNetworks = new HashMap<String, TokenData>();
        }

        socialNetworks.put(socialName, token);
        userData.put(username, socialNetworks);
        return true;
    }
//****************************************************************************************************************

    @Override
    //Method for getting contacts from some social network
    public String[] getContacts(String username, String[] socialNames) {
        List<String> contacts = new LinkedList<String>();
        for (String socialNetwork : socialNames) {
            String accessToken = getToken(username, socialNetwork);
            contacts.addAll(getInstance(socialNetwork).getContacts(accessToken));
        }
        return contacts.toArray(new String[contacts.size()]);
    }
//****************************************************************************************************************

    @Override
    public boolean shareByMail(String urlMatt, String[] contacts, String userName, String socialName) {
    	System.out.println("SN urlMatt: "+urlMatt);
    	System.out.println("SN contacts: "+contacts.toString());
    	System.out.println("SN username: "+userName);
    	System.out.println("SN socialName: "+socialName);
    	return getInstance(socialName).shareByMail(userName, urlMatt, contacts, getToken(userName, socialName));
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
        return getInstance(socialName).getApplicationData();
    }
//****************************************************************************************************************

	@Override
	public List<Boolean> getSlots(String username, String[] snNames, MattData interval) {
		ArrayList<ArrayList<Boolean>> slotsLists = new ArrayList<ArrayList<Boolean>>(); 
		for (int i=0; i<snNames.length; i++){
			slotsLists.add((ArrayList<Boolean>) getInstance(snNames[i]).getSlots(interval, getToken(username, snNames[i])));
		}
		return aggregateSlotsLists(slotsLists);
	}
	
	private List<Boolean> aggregateSlotsLists(ArrayList<ArrayList<Boolean>> slotsLists) {
		ArrayList<Boolean> resultSlots = new ArrayList<Boolean>();
		int slotSize = slotsLists.get(0).size();
		for (long i = 0; i < slotSize; i++)
			resultSlots.add(false);
		
		for(int i=0; i<slotSize; i++){
			for(ArrayList<Boolean> currentList : slotsLists){
				if(currentList.get(i)){
					resultSlots.set(i , true);
					break;
				}
			}
		}
		return resultSlots;
	}

//****************************************************************************************************************

	@Override
	public void setMatCalendar(String username, String[] snNames, List<Matt> matts) {
		for (int i=0; i<snNames.length; i++)
			getInstance(snNames[i]).setMatCalendar(matts, getToken(username, snNames[i]));
	}
}
