package org.apache.cloudstack.mom.rabbitmq;

import com.cloud.domain.dao.DomainDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.component.ComponentContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import org.apache.cloudstack.framework.events.Event;
import org.apache.cloudstack.framework.events.EventSubscriber;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.Map;
import java.util.Iterator;

public class MultiRegionSubscriber  implements EventSubscriber {

    private static final Logger s_logger = Logger.getLogger(MultiRegionSubscriber.class);

    protected int id;
    protected Gson gson;
    protected Map<String,String> descMap = null;

    @Inject
    protected DomainDao domainDao;
    @Inject
    protected AccountDao accountDao;
    @Inject
    protected UserDao userDao;

    protected String[][] regions = {
            {"10.88.90.82", "admin", "password"},
            {"207.19.99.100", "admin", "password"}
    };


    public MultiRegionSubscriber(int id)
    {
        this.id = id;
        this.gson = new GsonBuilder().create();
        this.domainDao = ComponentContext.getComponent(DomainDao.class);
        this.accountDao = ComponentContext.getComponent(AccountDao.class);
        this.userDao = ComponentContext.getComponent(UserDao.class);
    }

    protected boolean isCompleted(String status)
    {
        return (status != null && status.equals("Completed"));
    }

    protected boolean isExecutable()
    {
        String status = this.descMap.get("status");
        if (!isCompleted(status))  return false;

        String entityUUID = this.descMap.get("entityuuid");
        if (entityUUID == null || entityUUID.equals(""))
        {
            s_logger.info("entity uuid is not given");
            return false;
        }

        return true;
    }

        @Override
    public void onEvent(Event event)
    {
        s_logger.info("HANDLER" + id + " Category: " + event.getEventCategory() + " type: " + event.getEventType() +
                " resource type: " + event.getResourceType() + " resource UUID: " + event.getResourceUUID());
        s_logger.info("BODY : " + event.getDescription());

        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        this.descMap = gson.fromJson(event.getDescription(), stringStringMap);
        Iterator i = this.descMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry)i.next();
            s_logger.info("Key: " + e.getKey() + ", Value: " + e.getValue());
        }
    }
}