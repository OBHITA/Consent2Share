package gov.samhsa.consent2share.infrastructure.securityevent;

import gov.samhsa.acs.audit.AuditVerb;

public enum SecurityAuditVerb implements AuditVerb{
	FAILED_ATTEMPTS_TO_LOGIN_AS,
	DOWNLOADS_FILE,
	UPLOADS_FILE,
	UPLOADS_MALICIOUS_FILE,
	ATTEMPTS_TO_ACCESS_UNAUTHORIZED_RESOURCE,
	CREATES_USER;

	@Override
	public String getAuditVerb() {
		return this.toString();
	}

}
