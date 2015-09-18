package gov.samhsa.consent2share.service.consent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import gov.samhsa.consent2share.common.UniqueValueGeneratorException;
import gov.samhsa.consent2share.domain.consent.Consent;
import gov.samhsa.consent2share.domain.consent.ConsentRepository;
import gov.samhsa.consent2share.service.dto.ConsentDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class PolicyIdServiceImplTest {

	private static final String MRNMOCK = "MRNMOCK";
	private static final String PIDDOMAINIDMOCK = "PIDDOMAINIDMOCK";
	private static final String PIDDOMAINTYPEMOCK = "PIDDOMAINTYPEMOCK";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private ConsentRepository consentRepository;

	@InjectMocks
	private PolicyIdServiceImpl sut = new PolicyIdServiceImpl(PIDDOMAINIDMOCK,
			PIDDOMAINTYPEMOCK, consentRepository);

	@Test
	public void testGeneratePolicyId() {
		// Arrange
		when(consentRepository.findAllByConsentReferenceId(anyString()))
				.thenReturn(new ArrayList<Consent>(1));
		ConsentDto consentDto = new ConsentDto();
		Set<String> organizationalProvidersDisclosureIsMadeTo = new HashSet<String>();
		organizationalProvidersDisclosureIsMadeTo.add("111");
		Set<String> providersPermittedToDisclose = new HashSet<String>();
		providersPermittedToDisclose.add("222");
		consentDto
				.setOrganizationalProvidersDisclosureIsMadeTo(organizationalProvidersDisclosureIsMadeTo);
		consentDto
				.setProvidersPermittedToDisclose(providersPermittedToDisclose);

		// Act
		String policyId = sut.generatePolicyId(consentDto, MRNMOCK);

		// Assert
		assertTrue(StringUtils.hasText(policyId));
		assertTrue(policyId.startsWith(MRNMOCK));
		assertTrue(policyId.contains(PIDDOMAINIDMOCK));
		assertTrue(policyId.contains(PIDDOMAINTYPEMOCK));
	}

	@Test
	public void testGeneratePolicyId_Throws_UniqueValueGeneratorException() {
		// Arrange
		when(consentRepository.findAllByConsentReferenceId(anyString()))
				.thenReturn(Arrays.asList(new Consent()));
		ConsentDto consentDto = new ConsentDto();
		Set<String> organizationalProvidersDisclosureIsMadeTo = new HashSet<String>();
		organizationalProvidersDisclosureIsMadeTo.add("111");
		Set<String> providersPermittedToDisclose = new HashSet<String>();
		providersPermittedToDisclose.add("222");
		consentDto
				.setOrganizationalProvidersDisclosureIsMadeTo(organizationalProvidersDisclosureIsMadeTo);
		consentDto
				.setProvidersPermittedToDisclose(providersPermittedToDisclose);
		thrown.expect(UniqueValueGeneratorException.class);

		// Act
		String policyId = sut.generatePolicyId(consentDto, MRNMOCK);

		// Assert
		assertTrue(StringUtils.hasText(policyId));
		assertTrue(policyId.startsWith(MRNMOCK));
		assertTrue(policyId.contains(PIDDOMAINIDMOCK));
		assertTrue(policyId.contains(PIDDOMAINTYPEMOCK));
	}
}
