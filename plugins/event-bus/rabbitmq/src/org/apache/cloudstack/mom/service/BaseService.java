package org.apache.cloudstack.mom.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import org.apache.cloudstack.mom.api_interface.BaseInterface;
import org.apache.log4j.Logger;

public class BaseService {

    private static final Logger s_logger = Logger.getLogger(BaseService.class);

    protected String hostName;
    protected String userName;
    protected String password;
    protected String url;

    public BaseService(String hostName, String userName, String password)
    {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.url = "http://" + hostName + ":8080/client/api";
    }

    protected BaseInterface getInterface()
    {
        return null;
    }

    protected String getAttrValue(JSONObject obj, String attrName) throws Exception
    {
        try
        {
            return (String)obj.get(attrName);
        }
        catch(Exception ex)
        {
            s_logger.info("Failed to get value of [" + attrName + "] : " + obj);
            throw new Exception("Failed to find attr value for " + attrName);
        }
    }

    protected JSONArray getJSONArray(String attrName, JSONObject jsonObject) throws Exception
    {
        try
        {
            return (JSONArray)jsonObject.get(attrName);
        }
        catch(Exception ex)
        {
            s_logger.info("Failed to find json array for " + attrName);
            throw new Exception("Failed to find json array for " + attrName);
        }
    }

    protected String getErrorText(JSONObject jsonObject)
    {
        try
        {
            return (String)jsonObject.get("errortext");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    protected JSONObject findJSONObject(JSONArray jsonArray, String[] attrNames, String[] attrValues) throws Exception
    {
        for(int index = 0; index < jsonArray.length(); index++)
        {
            JSONObject obj = (JSONObject)jsonArray.get(index);

            int aIndex = 0;
            for(; aIndex < attrNames.length; aIndex++)
            {
                if(!obj.get(attrNames[aIndex]).equals(attrValues[aIndex]))
                {
                    break;
                }
            }

            if (aIndex == attrNames.length)  return (JSONObject)jsonArray.get(index);
        }

        s_logger.error("Failed to find json for " + attrNames + ", " + attrValues);
        throw new Exception("Failed to find json for " + attrNames + ", " + attrValues);
    }

    protected JSONObject queryAsyncJob(JSONObject retJson) throws Exception
    {
        String jobId = getAttrValue(retJson, "jobid");
        String projectId = null;
        try
        {
            projectId = getAttrValue(retJson, "projectid");
        }
        catch(Exception ex)
        {
        }
        if (jobId == null && projectId == null)
        {
            s_logger.info("Failed to find async job status for " + retJson);
            return null;
        }

        int jobStatus = 0;
        int waitSeconds = 1;
        JSONObject resJson = null;

        while (jobStatus == 0)
        {
            Thread.sleep(waitSeconds * 1000);
            resJson = getInterface().queryAsyncJob(jobId, projectId);
            s_logger.info("res = " + resJson);
            jobStatus = (Integer)resJson.get("jobstatus");
        }

        JSONObject jobResult = (JSONObject)resJson.get("jobresult");
        String errorText = getErrorText(jobResult);
        if (errorText != null)
        {
            s_logger.error("Async job failed : " + errorText);
            throw new Exception("Async job failed : " + errorText);
        }

        return jobResult;
    }
}
