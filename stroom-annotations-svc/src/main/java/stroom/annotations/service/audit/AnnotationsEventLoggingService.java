package stroom.annotations.service.audit;

import event.logging.Device;
import event.logging.Event;
import event.logging.EventLoggingService;
import event.logging.System;
import event.logging.User;
import event.logging.impl.DefaultEventLoggingService;
import event.logging.impl.DefaultEventSerializer;
import event.logging.impl.EventSerializer;
import event.logging.util.DeviceUtil;
import event.logging.util.EventLoggingUtil;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

public class AnnotationsEventLoggingService extends DefaultEventLoggingService implements EventLoggingService {

    private static final Logger LOGGER = Logger.getLogger(AnnotationsEventLoggingService.class.getName());

    private static final String SYSTEM = "Stroom";
    private static final String ENVIRONMENT = "";
    private static final String GENERATOR = "AnnotationsEventLoggingService";

    private volatile boolean obtainedDevice;
    private volatile Device storedDevice;

    private final EventSerializer eventSerializer = new DefaultEventSerializer();

    @Context
    private transient HttpServletRequest request;

    @Resource
    private SecurityContext security;

    private final AuditExecutor auditExecutor;

    @Inject
    public AnnotationsEventLoggingService(final AuditExecutor auditExecutor) {
        this.auditExecutor = auditExecutor;
    }

    @Override
    public void log(Event event) {
        String data = this.eventSerializer.serialize(event);
        String trimmed = data.trim();
        if (trimmed.length() > 0) {
            auditExecutor.accept(trimmed);
        }
    }

    public Event createEvent() {

        // Create event time.
        final Event.EventTime eventTime = new Event.EventTime();
        eventTime.setTimeCreated(new Date());

        // Get device.
        final Device device = getDevice(request);

        // Get client.
        final Device client = getClient(request);

        // Get user.
        final User user = getUser();

        // Create system.
        final System system = new System();
        system.setName(SYSTEM);
        system.setEnvironment(ENVIRONMENT);
        //system.setVersion(BuildInfoUtil.getBuildVersion());

        // Create event source.
        final Event.EventSource eventSource = new Event.EventSource();
        eventSource.setSystem(system);
        eventSource.setGenerator(GENERATOR);
        eventSource.setDevice(device);
        eventSource.setClient(client);
        eventSource.setUser(user);

        // Create the detail, ready for the auditor to fetch and fill in
        final Event.EventDetail eventDetail = new Event.EventDetail();

        // Create event.
        final Event event = super.createEvent();
        event.setEventTime(eventTime);
        event.setEventSource(eventSource);
        event.setEventDetail(eventDetail);

        return event;
    }

    public Event createAction(final String typeId, final String description) {
        final Event event = createEvent();

        final Event.EventDetail eventDetail = EventLoggingUtil.createEventDetail(typeId, description);
        event.setEventDetail(eventDetail);

        return event;
    }

    private Device getDevice(final HttpServletRequest request) {
        // Get stored device info.
        final Device storedDevice = obtainStoredDevice(request);

        // We need to copy the stored device as users may make changes to the
        // returned object that might not be thread safe.
        Device device = null;
        if (storedDevice != null) {
            device = copyDevice(storedDevice, new Device());
        }

        return device;
    }

    private Device getClient(final HttpServletRequest request) {
        if (request != null) {
            try {
                String ip = request.getRemoteAddr();
                ip = DeviceUtil.getValidIP(ip);

                if (ip != null) {
                    InetAddress inetAddress = null;
                    try {
                        inetAddress = InetAddress.getByName(ip);
                    } catch (final UnknownHostException e) {
                        LOGGER.warning("Problem getting client InetAddress:" + e.getLocalizedMessage());
                    }

                    Device client = null;
                    if (inetAddress != null) {
                        client = DeviceUtil.createDeviceFromInetAddress(inetAddress);
                    } else {
                        client = new Device();
                    }

                    client.setIPAddress(ip);
                    return client;
                }
            } catch (final Exception e) {
                LOGGER.warning("Problem getting client IP address and host name: " + e.getLocalizedMessage());
            }
        }

        return null;
    }

    private User getUser() {
        try {
            final String userId = security.getUserPrincipal().getName();
            if (userId != null) {
                final User user = new User();
                user.setId(userId);
                return user;
            }
        } catch (final Exception e) {
            LOGGER.warning("Problem getting current user: " + e.getLocalizedMessage());
        }

        return null;
    }

    private synchronized Device obtainStoredDevice(final HttpServletRequest request) {
        if (!obtainedDevice) {
            // First try and get the local server IP address and host name.
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
            } catch (final UnknownHostException e) {
                LOGGER.warning("Problem getting device from InetAddress: " + e.getLocalizedMessage());
            }

            if (inetAddress != null) {
                storedDevice = DeviceUtil.createDeviceFromInetAddress(inetAddress);
            } else {
                // Make final attempt to set with request if we have one and
                // haven't been able to set IP and host name already.
                if (request != null) {
                    final String ip = DeviceUtil.getValidIP(request.getLocalAddr());
                    if (ip != null) {
                        try {
                            inetAddress = InetAddress.getByName(ip);
                        } catch (final UnknownHostException e) {
                            LOGGER.warning("Problem getting client InetAddress: " + e.getLocalizedMessage());
                        }

                        if (inetAddress != null) {
                            storedDevice = DeviceUtil.createDeviceFromInetAddress(inetAddress);
                        } else {
                            storedDevice = new Device();
                        }

                        storedDevice.setIPAddress(ip);
                    }
                }
            }

            obtainedDevice = true;
        }

        return storedDevice;
    }

    private Device copyDevice(final Device source, final Device dest) {
        dest.setIPAddress(source.getIPAddress());
        dest.setHostName(source.getHostName());
        dest.setMACAddress(source.getMACAddress());
        return dest;
    }
}
