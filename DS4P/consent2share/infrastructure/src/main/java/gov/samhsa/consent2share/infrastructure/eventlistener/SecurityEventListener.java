package gov.samhsa.consent2share.infrastructure.eventlistener;

import gov.samhsa.acs.audit.AuditService;
import gov.samhsa.consent2share.domain.SecurityEvent;

/**
 * The listener interface for receiving securityEvent events. The class that is
 * interested in processing a securityEvent event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addSecurityEventListener<code> method. When
 * the securityEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SecurityEventEvent
 */
public abstract class SecurityEventListener extends EventListener {

	/** The audit service. */
	protected AuditService auditService;

	/**
	 * Instantiates a new security event listener.
	 *
	 * @param eventService
	 *            the event service
	 * @param auditService
	 *            the audit service
	 */
	public SecurityEventListener(EventService eventService,
			AuditService auditService) {
		super(eventService);
		this.auditService = auditService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.infrastructure.eventlistener.EventListener#handle
	 * (java.lang.Object)
	 */
	@Override
	public void handle(Object event) {
		audit((SecurityEvent) event);
	}

	/**
	 * Audit.
	 *
	 * @param event
	 *            the event
	 */
	public abstract void audit(SecurityEvent event);
}
