package gov.samhsa.consent2share.service.valueset;

import gov.samhsa.consent2share.domain.valueset.CodeSystemRepository;
import gov.samhsa.consent2share.domain.valueset.CodeSystemVersion;
import gov.samhsa.consent2share.domain.valueset.CodeSystemVersionRepository;
import gov.samhsa.consent2share.domain.valueset.ConceptCode;
import gov.samhsa.consent2share.domain.valueset.ConceptCodeRepository;
import gov.samhsa.consent2share.domain.valueset.ConceptCodeValueSet;
import gov.samhsa.consent2share.domain.valueset.ConceptCodeValueSetRepository;
import gov.samhsa.consent2share.domain.valueset.ValueSetRepository;
import gov.samhsa.consent2share.service.dto.ValueSetLookUpDto;
import gov.samhsa.consent2share.service.dto.ValueSetQueryDto;
import gov.samhsa.consent2share.service.dto.ValueSetQueryListDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ValueSetLookupServiceImpl.
 */
public class ValueSetLookupServiceImpl implements ValueSetLookupService {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The concept code repository. */
	private ConceptCodeRepository conceptCodeRepository;

	/** The value set repository. */
	private ValueSetRepository valueSetRepository;

	/** The code system repository. */
	CodeSystemRepository codeSystemRepository;

	/** The code system version repository. */
	CodeSystemVersionRepository codeSystemVersionRepository;

	/** The concept code value set repository. */
	ConceptCodeValueSetRepository conceptCodeValueSetRepository;

	/** The value set mgmt helper. */
	ValueSetMgmtHelper valueSetMgmtHelper;

	/**
	 * Instantiates a new value set lookup service impl.
	 *
	 * @param conceptCodeRepository
	 *            the concept code repository
	 * @param valueSetRepository
	 *            the value set repository
	 * @param codeSystemRepository
	 *            the code system repository
	 * @param codeSystemVersionRepository
	 *            the code system version repository
	 * @param conceptCodeValueSetRepository
	 *            the concept code value set repository
	 * @param valueSetMgmtHelper
	 *            the value set mgmt helper
	 */
	public ValueSetLookupServiceImpl(
			ConceptCodeRepository conceptCodeRepository,
			ValueSetRepository valueSetRepository,
			CodeSystemRepository codeSystemRepository,
			CodeSystemVersionRepository codeSystemVersionRepository,
			ConceptCodeValueSetRepository conceptCodeValueSetRepository,
			ValueSetMgmtHelper valueSetMgmtHelper) {
		super();
		this.conceptCodeRepository = conceptCodeRepository;
		this.valueSetRepository = valueSetRepository;
		this.codeSystemRepository = codeSystemRepository;
		this.codeSystemVersionRepository = codeSystemVersionRepository;
		this.conceptCodeValueSetRepository = conceptCodeValueSetRepository;
		this.valueSetMgmtHelper = valueSetMgmtHelper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.samhsa.consent2share.service.valueset.ValueSetLookupService#
	 * lookupValueSetCategories(java.lang.String, java.lang.String)
	 */
	@Override
	public ValueSetLookUpDto lookupValueSetCategories(String code,
			String codeSystemOid) throws CodeSystemVersionNotFoundException,
			ConceptCodeNotFoundException, ValueSetNotFoundException {

		ValueSetLookUpDto valueSetLookUpDto = new ValueSetLookUpDto();
		valueSetLookUpDto.setCodeSystemOid(codeSystemOid);
		valueSetLookUpDto.setConceptCode(code);
		valueSetLookUpDto.setVsCategoryCodes(ValueSetCategoriesInSet(code,
				codeSystemOid));

		return valueSetLookUpDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.samhsa.consent2share.service.valueset.ValueSetLookupService#
	 * RestfulValueSetCategories(java.lang.String, java.lang.String)
	 */
	@Override
	public ValueSetQueryDto RestfulValueSetCategories(String code,
			String codeSystemOid) throws CodeSystemVersionNotFoundException,
			ConceptCodeNotFoundException, ValueSetNotFoundException {

		ValueSetQueryDto valueSetQueryDto = new ValueSetQueryDto();
		valueSetQueryDto.setCodeSystemOid(codeSystemOid);
		valueSetQueryDto.setConceptCode(code);
		valueSetQueryDto.setVsCategoryCodes(ValueSetCategoriesInSet(code,
				codeSystemOid));

		return valueSetQueryDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.samhsa.consent2share.service.valueset.ValueSetLookupService#
	 * RestfulValueSetCategories
	 * (gov.samhsa.consent2share.service.dto.ValueSetQueryListDto)
	 */
	@Override
	public ValueSetQueryListDto RestfulValueSetCategories(
			ValueSetQueryListDto valueSetQueryListDtos)
			throws CodeSystemVersionNotFoundException,
			ConceptCodeNotFoundException, ValueSetNotFoundException {
		Set<ValueSetQueryDto> valueSetQueryDtos = valueSetQueryListDtos
				.getValueSetQueryDtos();

		for (ValueSetQueryDto valueSetQueryDto : valueSetQueryDtos) {
			valueSetQueryDto.setVsCategoryCodes(ValueSetCategoriesInSet(
					valueSetQueryDto.getConceptCode(),
					valueSetQueryDto.getCodeSystemOid()));
			logger.debug("ValueSetQueryDto : " + valueSetQueryDto);
		}

		return valueSetQueryListDtos;
	}

	/**
	 * Value set categories in set.
	 *
	 * @param code
	 *            the code
	 * @param codeSystemOid
	 *            the code system oid
	 * @return the sets the
	 * @throws CodeSystemVersionNotFoundException
	 *             the code system version not found exception
	 * @throws ConceptCodeNotFoundException
	 *             the concept code not found exception
	 * @throws ValueSetNotFoundException
	 *             the value set not found exception
	 */
	private Set<String> ValueSetCategoriesInSet(String code,
			String codeSystemOid) throws CodeSystemVersionNotFoundException,
			ConceptCodeNotFoundException, ValueSetNotFoundException {

		Set<String> vsCategories = new HashSet<String>();

		// validate the inputs
		if (null == code || code.length() <= 0) {
			throw new ConceptCodeNotFoundException();
		}

		// 1.Get latest version of Code System version for the given code system
		// oid
		List<CodeSystemVersion> codeSystemVersions = codeSystemVersionRepository
				.findAllByCodeSystemCodeSystemOIdOrderByIdDesc(codeSystemOid);
		if (codeSystemVersions == null || codeSystemVersions.size() <= 0) {
			logger.debug("No CodeSystem found with the oid: " + codeSystemOid);
			throw new CodeSystemVersionNotFoundException(
					"No Code System Versions found for the given codesystem oid"
							+ codeSystemOid);
		}
		// Get the latest version
		CodeSystemVersion codeSystemVersion = codeSystemVersions.get(0);

		// 2.Get the concept code for the given code and the latest code system
		// version
		ConceptCode conceptCode = conceptCodeRepository
				.findByCodeAndCodeSystemVersionId(code.trim(),
						codeSystemVersion.getId());
		if (conceptCode == null) {
			throw new ConceptCodeNotFoundException(
					"No Concept Code found for the given Code System  oid: "
							+ codeSystemOid + " And its latest version name: "
							+ codeSystemVersion.getName());
		}

		// 3.Get the value sets associated to the concept code
		List<ConceptCodeValueSet> cValueSets = conceptCodeValueSetRepository
				.findAllByPkConceptCodeId(conceptCode.getId());
		if (cValueSets == null) {
			throw new ValueSetNotFoundException(
					"No Valusets associated to the given codes" + conceptCode);
		}
		for (ConceptCodeValueSet codeValueSet : cValueSets) {
			// get the category code for the associated valuesets
			vsCategories.add(codeValueSet.getValueSet().getValueSetCategory()
					.getCode());
		}

		return vsCategories;
	}
}
