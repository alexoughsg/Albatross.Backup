package org.apache.cloudstack.mom.rabbitmq;

import com.cloud.domain.Domain;
import org.apache.cloudstack.framework.events.Event;
import org.apache.cloudstack.mom.service.DomainService;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

public class DomainSubscriber extends MultiRegionSubscriber {

    private static final Logger s_logger = Logger.getLogger(DomainSubscriber.class);

    public DomainSubscriber(int id)
    {
        super(id);
    }

    @Override
    public void onEvent(Event event)
    {
        super.onEvent(event);

        if (!isExecutable())    return;

        process(event);
    }

    protected void process(Event event)
    {
        String entityUUID = this.descMap.get("entityuuid");
        String oldDomainName = this.descMap.get("oldentityname");
        Domain domain = this.domainDao.findByUuidIncludingRemoved(entityUUID);
        Domain parentDomain = this.domainDao.findByIdIncludingRemoved(domain.getParent());

        String methodName = event.getEventType().split("-")[1].toLowerCase();
        for (int index = 0; index < this.regions.length; index++)
        {
            String hostName = this.regions[index][0];
            String userName = this.regions[index][1];
            String password = this.regions[index][2];

            try
            {
                DomainService domainService = new DomainService(hostName, userName, password);
                Method method = domainService.getClass().getMethod(methodName, Domain.class, Domain.class, String.class);
                method.invoke(domainService, domain, parentDomain, oldDomainName);
            }
            catch(NoSuchMethodException mex)
            {
                s_logger.error(hostName + ": Not valid method[" + methodName + "]");
            }
            catch(Exception ex)
            {
                s_logger.error(hostName + ": Fail to invoke/process method[" + methodName + "]", ex);
            }
        }
    }

}