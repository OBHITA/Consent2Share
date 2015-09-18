package gov.samhsa.consent2share.infrastructure.eventlistener;

import gov.samhsa.acs.audit.AuditService;
import gov.samhsa.acs.audit.PredicateKey;
import gov.samhsa.consent2share.domain.SecurityEvent;
import gov.samhsa.consent2share.infrastructure.securityevent.MaliciousFileDetectedEvent;
import gov.samhsa.consent2share.infrastructure.securityevent.SecurityAuditVerb;
import gov.samhsa.consent2share.infrastructure.securityevent.SecurityPredicateKey;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.audit.AuditException;

/**
 * The listener interface for receiving maliciousFileDetectedEvent events. The
 * class that is interested in processing a maliciousFileDetectedEvent event
 * implements this interface, and the object created with that class is
 * registered with a component using the component's
 * <code>addMaliciousFileDetectedEventListener<code> method. When
 * the maliciousFileDetectedEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see MaliciousFileDetectedEventEvent
 */
public class MaliciousFileDetectedEventListener extends SecurityEventListener {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Instantiates a new malicious file detected event listener.
	 *
	 * @param eventService
	 *            the event service
	 * @param auditService
	 *            the audit service
	 */
	public MaliciousFileDetectedEventListener(EventService eventService,
			AuditService auditService) {
		super(eventService, auditService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.infrastructure.eventlistener.EventListener#canHandle
	 * (java.lang.Object)
	 */
	@Override
	public boolean canHandle(Object event) {
		return event instanceof MaliciousFileDetectedEvent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.infrastructure.eventlistener.SecurityEventListener
	 * #handle(java.lang.Object)
	 */
	@Override
	public void handle(Object event) {
		super.handle(event);
		logger.debug("maliciousFileDetectedEventListener");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.infrastructure.eventlistener.SecurityEventListener
	 * #audit(gov.samhsa.consent2share.domain.SecurityEvent)
	 */
	@Override
	public void audit(SecurityEvent event) {
		MaliciousFileDetectedEvent maliciousFileDetectedEvent = (MaliciousFileDetectedEvent) event;
		Map<PredicateKey, String> predicateMap = auditService
				.createPredicateMap();
		predicateMap.put(SecurityPredicateKey.IP_ADDRESS, event.getIpAddress());
		try {
			auditService.audit("MaliciousFileDetectedEventListener",
					maliciousFileDetectedEvent.getUserName(),
					SecurityAuditVerb.UPLOADS_MALICIOUS_FILE,
					maliciousFileDetectedEvent.getFileName(), predicateMap);
		} catch (AuditException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
