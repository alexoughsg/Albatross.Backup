package org.apache.cloudstack.mom.rabbitmq;

import org.apache.cloudstack.framework.events.EventTopic;

import javax.naming.ConfigurationException;
import java.util.Map;

public class MultiRegionEventBus extends RabbitMQEventBus{

    public MultiRegionEventBus()
    {
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {

        if (!super.configure(name, params)) return false;

        try {
            EventTopic topic = new EventTopic("ActionEvent", "*", "Domain", "*", "*");
            subscribe(topic, new DomainSubscriber(1));
        }
        catch (Exception ex)
        {
            throw new ConfigurationException("Invalid topic and/or subscriber");
        }

        try {
            EventTopic topic = new EventTopic("ActionEvent", "*", "Account", "*", "*");
            subscribe(topic, new AccountSubscriber(2));
        }
        catch (Exception ex)
        {
            throw new ConfigurationException("Invalid topic and/or subscriber");
        }

        try {
            EventTopic topic = new EventTopic("ActionEvent", "*", "User", "*", "*");
            subscribe(topic, new UserSubscriber(3));
        }
        catch (Exception ex)
        {
            throw new ConfigurationException("Invalid topic and/or subscriber");
        }

        return true;
    }
}
