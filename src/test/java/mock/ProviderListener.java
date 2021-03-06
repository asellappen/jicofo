/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mock;

import net.java.sip.communicator.service.protocol.*;
import org.osgi.framework.*;

import static org.jitsi.jicofo.util.ServiceUtilsKt.getService;

/**
 *
 */
public class ProviderListener
    implements ServiceListener
{
    private final BundleContext context;

    private ProtocolProviderService pps;

    public ProviderListener(BundleContext context)
    {
        this.context = context;

        pps = getService(context, ProtocolProviderService.class);

        if (pps == null)
        {
            context.addServiceListener(this);
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event)
    {
        if (event.getType() != ServiceEvent.REGISTERED)
            return;

        Object service = context.getService(event.getServiceReference());
        if (service instanceof ProtocolProviderService)
        {
            context.removeServiceListener(this);

            synchronized (this)
            {
                pps = (ProtocolProviderService) service;

                this.notifyAll();
            }
        }
    }

    public synchronized ProtocolProviderService obtainProvider(long timeout)
    {
        if (pps != null)
            return pps;

        try
        {
            this.wait(timeout);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        if (pps == null)
            throw new RuntimeException("Failed to get protocol provider");

        return pps;
    }
}
