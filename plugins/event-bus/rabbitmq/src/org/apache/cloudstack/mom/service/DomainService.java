package org.apache.cloudstack.mom.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.Domain;
import org.apache.cloudstack.mom.api_interface.BaseInterface;
import org.apache.cloudstack.mom.api_interface.DomainInterface;
import org.apache.log4j.Logger;

public class DomainService extends BaseService {

    private static final Logger s_logger = Logger.getLogger(DomainService.class);
    private DomainInterface apiInterface;

    public DomainService(String hostName, String userName, String password)
    {
        super(hostName, userName, password);
        this.apiInterface = null;
    }

    @Override
    protected BaseInterface getInterface()
    {
        return this.apiInterface;
    }

    private JSONObject find(String[] attrNames, String[] attrValues)
    {
        try
        {
            JSONArray domainArray = this.apiInterface.listDomains(true);
            JSONObject domainObj = findJSONObject(domainArray, attrNames, attrValues);
            return domainObj;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    public JSONObject findByName(String domainName)
    {
        this.apiInterface = new DomainInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);
            String[] attrNames = {"name"};
            String[] attrValues = {domainName};
            JSONObject domainJson = find(attrNames, attrValues);
            s_logger.info("Successfully found domain by name[" + domainName + "]");
            return domainJson;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to find domain by name[" + domainName + "]", ex);
            return null;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean create(Domain domain, Domain parentDomain, String oldDomainName)
    {
        JSONObject resJson = create(domain.getName(), (parentDomain == null) ? null : parentDomain.getName(), domain.getNetworkDomain());
        return (resJson != null);
    }

    public JSONObject create(String domainName, String parentDomainName, String networkDomain)
    {
        this.apiInterface = new DomainInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            // check if the domain already exists
            String[] attrNames = {"name", "parentdomainname"};
            String[] attrValues = {domainName, parentDomainName};
            JSONObject domainJson = find(attrNames, attrValues);
            if (domainJson != null)
            {
                s_logger.info("domain[" + domainName + "] already exists in host[" + this.hostName + "]");
                return domainJson;
            }

            // find the parent domain id
            String parentDomainId = null;
            if (parentDomainName != null)
            {
                String[] pAttrNames = {"name"};
                String[] pAttrValues = {parentDomainName};
                JSONObject pDomainJson = find(pAttrNames, pAttrValues);
                if (pDomainJson == null)
                {
                    s_logger.info("cannot find parent domain[" + parentDomainName + "] in host[" + this.hostName + "]");
                    return null;
                }
                parentDomainId = (String)pDomainJson.get("id");
            }

            domainJson = this.apiInterface.createDomain(domainName, parentDomainId, null, networkDomain);
            s_logger.info("Successfully created domain[" + domainName + "] in host[" + this.hostName + "]");
            return domainJson;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to create domain with name[" + domainName + "]", ex);
            return null;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean delete(Domain domain, Domain parentDomain, String oldDomainName)
    {
        return delete(domain.getName(), (parentDomain == null) ? null : parentDomain.getName());
    }

    public boolean delete(String domainName, String parentDomainName)
    {
        this.apiInterface = new DomainInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            // check if the domain already exists
            String[] attrNames = {"name", "parentdomainname"};
            String[] attrValues = {domainName, parentDomainName};
            JSONObject domainJson = find(attrNames, attrValues);
            if (domainJson == null)
            {
                s_logger.info("domain[" + domainName + "] does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(domainJson, "id");
            JSONObject retJson = this.apiInterface.deleteDomain(id, false);
            queryAsyncJob(retJson);
            s_logger.info("Successfully deleted domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to delete domain by name[" + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean update(Domain domain, Domain parentDomain, String oldDomainName)
    {
        return update(oldDomainName, domain.getName(), (parentDomain == null) ? null : parentDomain.getName(), domain.getNetworkDomain());
    }

    public boolean update(String domainName, String newName, String parentDomainName, String networkDomain)
    {
        this.apiInterface = new DomainInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            String[] attrNames = {"name", "parentdomainname"};
            String[] attrValues = {domainName, parentDomainName};
            JSONObject domainJson = find(attrNames, attrValues);
            if (domainJson == null)
            {
                s_logger.info("domain[" + domainName + "] does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(domainJson, "id");
            this.apiInterface.updateDomain(id, newName, networkDomain);
            s_logger.info("Successfully updated domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to delete domain by name[" + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }
}
