package gov.samhsa.consent2share.infrastructure.eventlistener;

import java.util.Map;

import gov.samhsa.acs.audit.AuditService;
import gov.samhsa.acs.audit.PredicateKey;
import gov.samhsa.consent2share.domain.SecurityEvent;
import gov.samhsa.consent2share.infrastructure.securityevent.SecurityAuditVerb;
import gov.samhsa.consent2share.infrastructure.securityevent.UnauthorizedAccessAttemptedEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.qos.logback.audit.AuditException;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnauthorizedAccessAttemptedEventListenerTest {
	
	final static String IP_ADDRESS = "192.168.0.1";
	final static String USER_NAME = "user1";
	
	@Mock
	AuditService auditService;

	@InjectMocks
	UnauthorizedAccessAttemptedEventListener listener = new UnauthorizedAccessAttemptedEventListener();
	
	@Test
	public void testAudit() throws AuditException {
		@SuppressWarnings("unchecked")
		Map<PredicateKey, String> predicateMap = (Map<PredicateKey, String>) mock(Map.class);
		doReturn(predicateMap).when(auditService).createPredicateMap();

		SecurityEvent event = new UnauthorizedAccessAttemptedEvent(IP_ADDRESS,
				USER_NAME);
		listener.audit(event);
		verify(auditService).audit("UnauthorizedAccessAttemptedEventListener",
				USER_NAME, SecurityAuditVerb.ATTEMPTS_TO_ACCESS_UNAUTHORIZED_RESOURCE,
				"Unauthorized Page", predicateMap);
	}
}
