/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.consent2share.infrastructure.domain;

import java.util.Date;

import gov.samhsa.consent2share.domain.AbstractDomainEventHandler;
import gov.samhsa.consent2share.domain.consent.Consent;
import gov.samhsa.consent2share.domain.consent.ConsentRepository;
import gov.samhsa.consent2share.domain.consent.event.ConsentSignedEvent;
import gov.samhsa.consent2share.domain.systemnotification.SystemNotification;
import gov.samhsa.consent2share.domain.systemnotification.SystemNotificationRepository;
import gov.samhsa.consent2share.infrastructure.rabbit.MessageSender;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class ConsentSignedEventHandler.
 */
@Component
public class ConsentSignedEventHandler extends
		AbstractDomainEventHandler<ConsentSignedEvent> {
	
	
	@Value("${notification_consent_signed}")
	private String notificationMessage;
	
	@Autowired
	private MessageSender messageSender;
	
	/** The consent repository. */
	@Autowired
    private ConsentRepository consentRepository;
	
	
	/** The notification repository. */
	@Autowired
    private SystemNotificationRepository systemNotificationRepository;

	/* (non-Javadoc)
	 * @see gov.samhsa.consent2share.domain.AbstractDomainEventHandler#getEventClass()
	 */
	@Override
	protected Class<ConsentSignedEvent> getEventClass() {
		return ConsentSignedEvent.class;
	}

	/* (non-Javadoc)
	 * @see gov.samhsa.consent2share.domain.AbstractDomainEventHandler#handle(gov.samhsa.consent2share.domain.DomainEvent)
	 */
	@Override
	public void handle(ConsentSignedEvent event) {
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("messageType", event.getClass().getName());
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
		messageSender.send(event.getConsentId().toString(), messageProperties);
		
		Consent consent=consentRepository.findOne(event.getConsentId());
		
		SystemNotification notification=new SystemNotification();
		notification.setConsentId(consent.getId());
		notification.setPatientId(consent.getPatient().getId());
		notification.setNotificationMessage(notificationMessage);
		notification.setNotificationType("consent_signed");
		notification.setSendDate(new Date());
		
		systemNotificationRepository.save(notification);
	}
}
