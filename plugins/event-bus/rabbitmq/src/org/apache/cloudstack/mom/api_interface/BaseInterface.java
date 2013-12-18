package org.apache.cloudstack.mom.api_interface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import com.amazonaws.util.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class BaseInterface {

    private Gson gson;
    //private final String USER_AGENT = "Mozilla/5.0";
    private static final Logger s_logger = Logger.getLogger(BaseInterface.class);

    protected String url;
    protected String cookie;
    protected String sessionKey;

    public BaseInterface(String url)
    {
        this.url = url;
        this.cookie = null;
        this.sessionKey = null;
        this.gson = new GsonBuilder().create();
    }

    /*protected Map deserialize(String serialized)
    {
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        Map<String,String> map = this.gson.fromJson(serialized, stringStringMap);
        return map;
    }*/

    public JSONObject toJson(String serialized) throws Exception
    {
        JSONObject jsonObj = new JSONObject(serialized);
        JSONObject retObj = (JSONObject)jsonObj.get(jsonObj.keys().next().toString());

        String errorText = null;
        try
        {
            errorText = (String)retObj.get("errortext");
        }
        catch(Exception ex)
        {
        }

        if(errorText != null)
        {
            s_logger.error("Returned with failure : " + errorText);
            throw new Exception(errorText);
        }

        return retObj;
    }



    /*public String request(String url)
    {
        HttpClient client = new HttpClient();

        //Instantiate a GET HTTP method
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");

        //Define name-value pairs to set into the QueryString
        NameValuePair nvp1 = new NameValuePair("firstName","fname");
        NameValuePair nvp2 = new NameValuePair("lastName","lname");
        NameValuePair nvp3 = new NameValuePair("email","email@email.com");

        method.setQueryString(new NameValuePair[]{nvp1,nvp2,nvp3});

        try{
            int statusCode = client.executeMethod(method);

            System.out.println("Status Code = " + statusCode);
            System.out.println("QueryString>>> " + method.getQueryString());
            System.out.println("Status Text>>>" + HttpStatus.getStatusText(statusCode));

            //Get data as a String
            System.out.println(method.getResponseBodyAsString());

            //OR as a byte array
            byte [] res  = method.getResponseBody();

            //write to file
            FileOutputStream fos= new FileOutputStream("donepage.html");
            fos.write(res);

            //release connection
            method.releaseConnection();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }*/

    /*public JSONObject sendGet() throws Exception {

        //String url = "http://www.google.com/search?q=mkyong";
        String connUrl = this.url;

        URL obj = new URL(connUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        s_logger.info("\nSending 'GET' request to URL : " + connUrl);
        s_logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer result = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        String resultStr = result.toString();

        s_logger.info(resultStr);

        return toJson(resultStr);
    }

    // HTTP POST request
    public JSONObject sendPost(String urlParameters) throws Exception {

        //String url = "https://selfsolve.apple.com/wcResults.do";
        String connUrl = this.url;

        URL obj = new URL(connUrl);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        s_logger.info("\nSending 'POST' request to URL : " + connUrl);
        s_logger.info("Post parameters : " + urlParameters);
        s_logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer result = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        in.close();

        String resultStr = result.toString();

        s_logger.info(resultStr);

        return toJson(resultStr);
    }*/

    public JSONObject sendApacheGet(String paramStr) throws Exception {

        //String url = "http://www.google.com/search?q=developer";
        String connUrl = this.url;

        if (paramStr != null && !paramStr.equals(""))
            connUrl += "?" + paramStr;

        //CookieHandler.setDefault(new CookieManager());

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(connUrl);



        /*CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", this.cookie);
        //cookie.setDomain("your domain");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        //post.setCookieStore(cookieStore);

        org.apache.http.protocol.HttpContext localContext = new org.apache.http.protocol.BasicHttpContext();
        localContext.setAttribute(org.apache.http.client.protocol.ClientContext.COOKIE_STORE, cookieStore);*/



        // add request header
        //request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Cookie", "JSESSIONID=" + this.cookie);

        HttpResponse response = client.execute(request);
        //HttpResponse response = client.execute(request, localContext);
        s_logger.info("\nSending 'GET' request to URL : " + connUrl);
        s_logger.info("Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //String responseStr = response.toString();
        String resultStr = result.toString();

        s_logger.info(resultStr);

        return toJson(resultStr);
    }

    // HTTP POST request
    //public Map<String,String> sendApachePost(List<NameValuePair> urlParameters) throws Exception {
    public JSONObject sendApachePost(String paramStr) throws Exception {

        //String url = "https://selfsolve.apple.com/wcResults.do";
        String connUrl = this.url;
        if (paramStr != null && !paramStr.equals(""))
            connUrl += "?" + paramStr;

        //CookieHandler.setDefault(new CookieManager());

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(connUrl);



        /*CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", this.cookie);
        //cookie.setDomain("your domain");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        //post.setCookieStore(cookieStore);

        org.apache.http.protocol.HttpContext localContext = new org.apache.http.protocol.BasicHttpContext();
        localContext.setAttribute(org.apache.http.client.protocol.ClientContext.COOKIE_STORE, cookieStore);*/






        // add header
        //post.setHeader("User-Agent", USER_AGENT);
        if (this.cookie != null)
        {
            post.setHeader("Cookie", "JSESSIONID=" + this.cookie);
        }

        HttpResponse response = client.execute(post);
        s_logger.info("\nSending 'POST' request to URL : " + connUrl);
        s_logger.info("Post parameters : " + post.getEntity());
        s_logger.info("Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        // if this is the response of 'login' command, store the returned cookie
        if (this.cookie == null)
        {
            try
            {
                this.cookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0].split("=")[1];
            }
            catch(Exception ex)
            {
                s_logger.error("Failed to parse 'Set-Cookie header", ex);
            }
        }

        String resultStr = result.toString();

        s_logger.info(resultStr);

        return toJson(resultStr);
    }

    public JSONObject login(String userName, String password) throws Exception
    {
        String paramStr = "command=login&username=" + userName + "&password=" + password + "&response=json";
        JSONObject responseJson = sendApachePost(paramStr);
        this.sessionKey = (String)responseJson.get("sessionkey");

        // '{ "loginresponse" : { "timeout" : "1800", "sessionkey" : "GNUfHusIyEOsqpgFp/Q9O2zaRFQ=", "username" : "admin", "registered" : "false", "userid" : "813253a8-7c63-11e2-a26f-c9595fd30292", "lastname" : "User", "account" : "admin", "domainid" : "813221a8-7c63-11e2-a26f-c9595fd30292", "firstname" : "Admin", "type" : "1" } }'
        return responseJson;
    }


    public void logout()
    {
        try
        {
            String paramStr = "command=logout&response=json&sessionkey="  + URLEncoder.encode(this.sessionKey, "UTF-8");
            sendApacheGet(paramStr);
            // '{ "logoutresponse" : { "description" : "success" } }'
        }
        catch(Exception ex)
        {
            s_logger.error("Fail to logout", ex);
        }
    }

    public JSONObject queryAsyncJob(String jobId, String projectId)
    {
        try
        {
            // command=queryAsyncJobResult
            //    &jobId=2888ed5d-a42f-49df-9297-a4945e46d3c8
            //    &response=json
            //    &sessionkey=KPqcTgDRRT9rNMJeH%2FUc2OdBhGQ%3D
            //    &projectid=dd19bac8-38a7-43ca-af9c-eca4f9e97e13&_=1365832471891
            String paramStr = "command=queryAsyncJobResult&jobId=" + jobId + "&response=json&sessionkey=" +  URLEncoder.encode(this.sessionKey, "UTF-8");
            if (projectId != null)
            {
                paramStr += "&projectid=" + projectId;
            }
            return sendApacheGet(paramStr);
        }
        catch(Exception ex)
        {
            s_logger.error("Fail to queryAsyncJob", ex);
        }

        return null;
    }
}
