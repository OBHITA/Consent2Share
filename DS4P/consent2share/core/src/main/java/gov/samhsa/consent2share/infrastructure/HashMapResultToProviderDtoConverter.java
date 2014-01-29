package gov.samhsa.consent2share.infrastructure;

import gov.samhsa.consent2share.domain.reference.EntityType;
import gov.samhsa.consent2share.service.dto.AbstractProviderDto;

import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class HashMapResultToProviderDtoConverter {
	
	//This code is originally in ProviderController and is moved to this seperate class to reuse code in AdminController.
	public AbstractProviderDto setProviderDto(AbstractProviderDto providerDto, HashMap<String,String> Result){
		providerDto.setNpi(Result.get("npi"));
		providerDto.setEntityType(EntityType.valueOf(Result.get("entityType")));
		providerDto.setFirstLineMailingAddress(Result.get("providerFirstLineBusinessMailingAddress"));
		providerDto.setSecondLineMailingAddress(Result.get("providerSecondLineBusinessMailingAddress"));
		providerDto.setMailingAddressCityName(Result.get("providerBusinessMailingAddressCityName"));
		providerDto.setMailingAddressStateName(Result.get("providerBusinessMailingAddressStateName"));
		providerDto.setMailingAddressPostalCode(Result.get("providerBusinessMailingAddressPostalCode"));
		providerDto.setMailingAddressCountryCode(Result.get("providerBusinessMailingAddressCountryCode"));
		providerDto.setMailingAddressTelephoneNumber(Result.get("providerBusinessMailingAddressTelephoneNumber"));
		providerDto.setMailingAddressFaxNumber(Result.get("providerBusinessMailingAddressFaxNumber"));
		providerDto.setFirstLinePracticeLocationAddress(Result.get("providerFirstLineBusinessPracticeLocationAddress"));
		providerDto.setSecondLinePracticeLocationAddress(Result.get("providerSecondLineBusinessPracticeLocationAddress")); 
		providerDto.setPracticeLocationAddressCityName(Result.get("providerBusinessPracticeLocationAddressCityName"));
		providerDto.setPracticeLocationAddressStateName(Result.get("providerBusinessPracticeLocationAddressStateName"));
		providerDto.setPracticeLocationAddressPostalCode(Result.get("providerBusinessPracticeLocationAddressPostalCode")); 
		providerDto.setPracticeLocationAddressCountryCode(Result.get("providerBusinessPracticeLocationAddressCountryCode")); 
		providerDto.setPracticeLocationAddressTelephoneNumber(Result.get("providerBusinessPracticeLocationAddressTelephoneNumber"));
		providerDto.setPracticeLocationAddressFaxNumber(Result.get("providerBusinessPracticeLocationAddressFaxNumber"));
		providerDto.setEnumerationDate(Result.get("providerEnumerationDate"));
		providerDto.setLastUpdateDate(Result.get("lastUpdateDate"));
		providerDto.setProviderTaxonomyCode(Result.get("healthcareProviderTaxonomyCode_1"));
		providerDto.setProviderTaxonomyDescription(Result.get("healthcareProviderTaxonomy_1"));
		return providerDto;
	}
}
