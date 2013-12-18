package org.apache.cloudstack.mom.api_interface;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

import java.net.URLEncoder;

public class DomainInterface extends BaseInterface {

    //private static final Logger s_logger = Logger.getLogger(DomainInterface.class);

    public DomainInterface(String url)
    {
        super(url);
    }

    public JSONArray listDomains(boolean listAll) throws Exception
    {
        // command=listDomains&response=json&sessionkey=null&_=1362457544896
        // { "listdomainsresponse" : { "count":2 ,"domain" : [  {"id":"45152a26-a2ce-11e2-8da9-28fb734f3313","name":"ROOT","level":0,"haschild":true,"path":"ROOT"}, {"id":"3d12e7d5-a528-4626-a423-d1e17024ff91","name":"Ough","level":1,"parentdomainid":"45152a26-a2ce-11e2-8da9-28fb734f3313","parentdomainname":"ROOT","haschild":false,"path":"ROOT/Ough"} ] } }

        String paramStr = "command=listDomains&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (listAll)    paramStr += "&listall" + listAll;
        return (JSONArray)sendApacheGet(paramStr).get("domain");
    }

    public JSONObject listDomainChildren() throws Exception
    {
        // command=listDomainChildren&response=json&sessionkey=null&_=1362457544896

        String paramStr = "command=listDomainChildren&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        return sendApacheGet(paramStr);
    }

    public JSONObject createDomain(String name, String parentDomainId, String domainId, String networkDomain) throws Exception
    {
        /*
            command=createDomain
            &response=json
            &sessionkey=WyKKl72c8fi1d6y%2Bp%2BQuDGxDnZg%3D
            &parentdomainid=b8683900-a486-11e2-8da9-28fb734f3313
            &name=Eldridge
            &_=1365892060259
         */

        String paramStr = "command=createDomain&name=" + name + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (parentDomainId != null)    paramStr += "&'parentdomainid'=" + parentDomainId;
        if (domainId != null)   paramStr += "&domainid=" + domainId;
        if (networkDomain != null) paramStr += "&networkdomain=" + networkDomain;
        return sendApacheGet(paramStr);
    }

    public JSONObject updateDomain(String domainId, String name, String networkDomain) throws Exception
    {
        String paramStr = "command=updateDomain&id=" + domainId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (name != null)  paramStr += "&name=" + name;
        if (networkDomain != null)    paramStr += "&networkdomain=" + networkDomain;

        return sendApacheGet(paramStr);
    }

    public JSONObject deleteDomain(String domainId, boolean cleanUp) throws Exception
    {
        String paramStr = "command=deleteDomain&id=" + domainId + "&response=json&sessionkey=" + URLEncoder.encode(this.sessionKey, "UTF-8");
        if (cleanUp)    paramStr += "&cleanup=" + cleanUp;
        return sendApacheGet(paramStr);
    }
}
