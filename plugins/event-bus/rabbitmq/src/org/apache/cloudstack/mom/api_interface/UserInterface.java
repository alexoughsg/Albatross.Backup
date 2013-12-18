package org.apache.cloudstack.mom.api_interface;

import com.amazonaws.util.json.JSONObject;
import com.amazonaws.util.json.JSONArray;
import java.net.URLEncoder;

public class UserInterface extends BaseInterface {

    //private static final Logger s_logger = Logger.getLogger(UserInterface.class);

    public UserInterface(String url)
    {
        super(url);
    }

    public JSONArray listUsers() throws Exception
    {
        String paramStr = "command=listUsers&listAll=true&page=1&pagesize=20&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return (JSONArray)sendApacheGet(paramStr).get("user");
    }

    public JSONObject createUser(String userName, String password, String email, String firstName, String lastName, String accountName, String domainId, String timezone) throws Exception
    {
        String paramStr = "command=createUser&username=" + userName + "&password=" + password;
        paramStr += "&email=" + email + "&firstname=" + firstName + "&lastname=" + lastName + "&account=" + accountName;
        paramStr += "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (domainId != null)
            paramStr += "&domainid=" + domainId;
        if (timezone != null)
            paramStr += "&timezone=" + timezone;

        return sendApachePost(paramStr);
    }

    public JSONObject deleteUser(String userId) throws Exception
    {
        String paramStr = "command=deleteUser&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject updateUser(String userId, String email, String firstName, String lastName, String password, String timezone, String userAPIKey, String userName, String userSecretKey) throws Exception
    {
        String paramStr = "command=updateUser&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (email != null)  paramStr += "&email=" + email;
        if (firstName != null)  paramStr += "&firstname=" + firstName;
        if (lastName != null)   paramStr += "&lastname=" + lastName;
        if (password != null)    paramStr += "&password=" + password;
        if (timezone != null)    paramStr += "&timezone=" + timezone;
        if (userAPIKey != null)    paramStr += "&userapikey=" + userAPIKey;
        if (userName != null)   paramStr += "&username=" + userName;
        if (userSecretKey != null) paramStr += "&usersecretkey=" + userSecretKey;

        return sendApacheGet(paramStr);
    }

    public JSONObject lockUser(String userId) throws Exception
    {
        String paramStr = "command=lockUser&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject disableUser(String userId) throws Exception
    {
        String paramStr = "command=disableUser&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject enableUser(String userId) throws Exception
    {
        String paramStr = "command=enableUser&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject getUser(String userAPIKey) throws Exception
    {
        String paramStr = "command=getUser&userapikey=" + userAPIKey + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject registerUserKeys(String userId) throws Exception
    {
        String paramStr = "command=registerUserKeys&id=" + userId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }
}
