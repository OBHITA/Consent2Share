package gov.samhsa.consent2share.service.valueset;

import gov.samhsa.consent2share.domain.valueset.CodeSystem;
import gov.samhsa.consent2share.domain.valueset.CodeSystemRepository;
import gov.samhsa.consent2share.service.dto.CodeSystemDto;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class CodeSystemServiceImpl.
 */
public class CodeSystemServiceImpl implements CodeSystemService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CodeSystemService.class);

	/** The code system repository. */
	private CodeSystemRepository codeSystemRepository;

	/** The value set mgmt helper. */
	ValueSetMgmtHelper valueSetMgmtHelper;

	/**
	 * Instantiates a new code system service impl.
	 *
	 * @param codeSystemRepository
	 *            the code system repository
	 * @param valueSetMgmtHelper
	 *            the value set mgmt helper
	 */
	public CodeSystemServiceImpl(CodeSystemRepository codeSystemRepository,
			ValueSetMgmtHelper valueSetMgmtHelper) {
		super();
		this.codeSystemRepository = codeSystemRepository;
		this.valueSetMgmtHelper = valueSetMgmtHelper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.service.valueset.CodeSystemService#create(gov
	 * .samhsa.consent2share.service.dto.CodeSystemDto)
	 */
	@Override
	@Transactional
	public CodeSystemDto create(CodeSystemDto created) {
		LOGGER.debug("Creating a new CodeSystem with information: " + created);
		String displayName = (created.getDisplayName() != null) ? created
				.getDisplayName() : "";
		CodeSystem codeSystem = CodeSystem
				.getBuilder(created.getCodeSystemOId(), created.getCode(),
						created.getName(), created.getUserName())
				.displayName(displayName).build();

		codeSystem = codeSystemRepository.save(codeSystem);

		return valueSetMgmtHelper.createCodeSystemDtoFromEntity(codeSystem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.service.valueset.CodeSystemService#delete(java
	 * .lang.Long)
	 */
	@Override
	@Transactional(rollbackFor = CodeSystemNotFoundException.class)
	public CodeSystemDto delete(Long codeSystemId)
			throws CodeSystemNotFoundException {
		LOGGER.debug("Deleting CodeSystem with id: " + codeSystemId);
		CodeSystem deleted = codeSystemRepository.findOne(codeSystemId);
		if (deleted == null) {
			LOGGER.debug("No CodeSystem found with an id: " + codeSystemId);
			throw new CodeSystemNotFoundException();
		}
		codeSystemRepository.delete(deleted);
		return valueSetMgmtHelper.createCodeSystemDtoFromEntity(deleted);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.service.valueset.CodeSystemService#findAll()
	 */
	@Override
	public List<CodeSystemDto> findAll() {
		LOGGER.debug("Finding all codeSystems");
		List<CodeSystem> codeSystems = codeSystemRepository.findAll();
		return valueSetMgmtHelper.convertCodeSystemEntitiesToDtos(codeSystems);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.service.valueset.CodeSystemService#findById(
	 * java.lang.Long)
	 */
	@Override
	public CodeSystemDto findById(Long id) {
		LOGGER.debug("Finding a CodeSystem with id: " + id);
		CodeSystem codeSystem = codeSystemRepository.findOne(id);
		return valueSetMgmtHelper.createCodeSystemDtoFromEntity(codeSystem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.samhsa.consent2share.service.valueset.CodeSystemService#update(gov
	 * .samhsa.consent2share.service.dto.CodeSystemDto)
	 */
	@Override
	@Transactional(rollbackFor = CodeSystemNotFoundException.class)
	public CodeSystemDto update(CodeSystemDto updated)
			throws CodeSystemNotFoundException {
		LOGGER.debug("Updating CodeSystem with information" + updated);

		CodeSystem codeSystem = codeSystemRepository.findOne(updated.getId());
		if (codeSystem == null) {
			LOGGER.debug("No CodeSystem found with an id: " + updated.getId());
			throw new CodeSystemNotFoundException();
		}

		codeSystem.update(updated.getCodeSystemOId(), updated.getCode(),
				updated.getName(), updated.getDisplayName(),
				updated.getUserName());
		return valueSetMgmtHelper.createCodeSystemDtoFromEntity(codeSystem);
	}

	/**
	 * This setter method should be used only by unit tests.
	 *
	 * @param codeSystemRepository
	 *            the new code system repository
	 */
	protected void setCodeSystemRepository(
			CodeSystemRepository codeSystemRepository) {
		this.codeSystemRepository = codeSystemRepository;
	}
}
