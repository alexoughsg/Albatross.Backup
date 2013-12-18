package org.apache.cloudstack.mom.service;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.cloud.domain.Domain;
import com.cloud.user.Account;
import com.cloud.user.User;
import org.apache.cloudstack.mom.api_interface.BaseInterface;
import org.apache.cloudstack.mom.api_interface.UserInterface;
import org.apache.log4j.Logger;

public class UserService extends BaseService {

    private static final Logger s_logger = Logger.getLogger(UserService.class);
    private UserInterface apiInterface;

    public UserService(String hostName, String userName, String password)
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
            JSONArray userArray = this.apiInterface.listUsers();
            JSONObject userObj = findJSONObject(userArray, attrNames, attrValues);
            return userObj;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    public JSONArray list()
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);
            JSONArray userArray = this.apiInterface.listUsers();
            s_logger.info("Successfully found user list");
            return userArray;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to find users", ex);
            return new JSONArray();
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public JSONObject findById(String id)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);
            String[] attrNames = {"id"};
            String[] attrValues = {id};
            JSONObject userJson = find(attrNames, attrValues);
            s_logger.info("Successfully found user by id[" + id + "]");
            return userJson;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to find user by id[" + id + "]", ex);
            return null;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public JSONObject findByName(String userName, String accountName, String domainName)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);
            String[] attrNames = {"username", "account"};
            String[] attrValues = {userName, accountName};
            JSONObject userJson = find(attrNames, attrValues);
            s_logger.info("Successfully found user by name[" + userName + ", " + accountName + ", " + domainName + "]");
            return userJson;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to find user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return null;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean create(User user, Account account, Domain domain, String oldUserName)
    {
        JSONObject resJson = create(user.getUsername(), account.getAccountName(), domain.getName(), user.getPassword(), user.getEmail(), user.getFirstname(), user.getLastname(), user.getTimezone());
        return (resJson != null);
    }

    public JSONObject create(String userName, String accountName, String domainName, String password, String email, String firstName, String lastName, String timezone)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            // check if the user already exists
            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson != null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] already exists in host[" + this.hostName + "]");
                return userJson;
            }

            // find domain id
            DomainService domainService = new DomainService(this.hostName, this.userName, this.password);
            JSONObject domainObj = domainService.findByName(domainName);
            if (domainObj == null)
            {
                s_logger.info("cannot find domain[" + domainName + "] in host[" + this.hostName + "]");
                return null;
            }
            String domainId = (String)domainObj.get("id");

            userJson = this.apiInterface.createUser(userName, password, email, firstName, lastName, accountName, domainId, timezone);
            s_logger.info("Successfully created user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return userJson;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to create user with name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return null;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean delete(User user, Account account, Domain domain, String oldUserName)
    {
        return delete(user.getUsername(), account.getAccountName(), domain.getName());
    }

    public boolean delete(String userName, String accountName, String domainName)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            // check if the user already exists
            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson == null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(userJson, "id");
            this.apiInterface.deleteUser(id);
            s_logger.info("Successfully deleted user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to delete user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean enable(User user, Account account, Domain domain, String oldUserName)
    {
        return enable(user.getUsername(), account.getAccountName(), domain.getName());
    }

    public boolean enable(String userName, String accountName, String domainName)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson == null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(userJson, "id");
            this.apiInterface.enableUser(id);
            s_logger.info("Successfully enabled user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to enable user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean disable(User user, Account account, Domain domain, String oldUserName)
    {
        return disable(user.getUsername(), account.getAccountName(), domain.getName());
    }

    public boolean disable(String userName, String accountName, String domainName)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson == null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "]  does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(userJson, "id");
            JSONObject retJson = this.apiInterface.disableUser(id);
            queryAsyncJob(retJson);
            s_logger.info("Successfully disabled user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to disable user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean lock(User user, Account account, Domain domain, String oldUserName)
    {
        return lock(user.getUsername(), account.getAccountName(), domain.getName());
    }

    public boolean lock(String userName, String accountName, String domainName)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson == null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "]  does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(userJson, "id");
            this.apiInterface.disableUser(id);
            s_logger.info("Successfully disabled user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to disable user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }

    public boolean update(User user, Account account, Domain domain, String oldUserName)
    {
        return update(oldUserName, user.getUsername(), account.getAccountName(), domain.getName(), user.getEmail(), user.getFirstname(), user.getLastname(), user.getPassword(), user.getTimezone(), user.getApiKey(), user.getSecretKey());
    }

    public boolean update(String userName, String newName, String accountName, String domainName, String email, String firstName, String lastName, String password, String timezone, String userAPIKey, String userSecretKey)
    {
        this.apiInterface = new UserInterface(this.url);
        try
        {
            this.apiInterface.login(this.userName, this.password);

            String[] attrNames = {"username", "account", "domain"};
            String[] attrValues = {userName, accountName, domainName};
            JSONObject userJson = find(attrNames, attrValues);
            if (userJson == null)
            {
                s_logger.info("user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "]  does not exists in host[" + this.hostName + "]");
                return false;
            }

            String id = getAttrValue(userJson, "id");
            this.apiInterface.updateUser(id, email, firstName, lastName, password, timezone, userAPIKey, newName, userSecretKey);
            s_logger.info("Successfully updated user[" + userName + "] in account[" + accountName + "], domain[" + domainName + "] in host[" + this.hostName + "]");
            return true;
        }
        catch(Exception ex)
        {
            s_logger.error("Failed to update user by name[" + userName + ", " + accountName + ", " + domainName + "]", ex);
            return false;
        }
        finally {
            this.apiInterface.logout();
        }
    }
}
