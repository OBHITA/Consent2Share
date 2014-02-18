package gov.samhsa.consent2share.service.valueset;

import gov.samhsa.consent2share.service.dto.CodeSystemDto;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * Declares methods used to obtain and modify CodeSystem information.
 */
@Transactional(readOnly = true)
public interface CodeSystemService {
	   /**
     * Creates a new person.
     * @param created   The information of the created person.
     * @return  The created person.
     */
    public CodeSystemDto create(CodeSystemDto created);

    /**
     * Deletes a CodeSystem.
     * @param CodeSystemId  The id of the deleted CodeSystem.
     * @return  The deleted CodeSystem.
     * @throws CodeSystemNotFoundException  if no CodeSystem is found with the given id.
     */
    public CodeSystemDto delete(Long codeSystemId) throws CodeSystemNotFoundException;

    /**
     * Finds all CodeSystems.
     * @return  A list of CodeSystems.
     */
    public List<CodeSystemDto> findAll();

    /**
     * Finds CodeSystem by id.
     * @param id    The id of the wanted CodeSystem.
     * @return  The found CodeSystem. If no CodeSystem is found, this method returns null.
     */
    public CodeSystemDto findById(Long id);

    /**
     * Updates the information of a CodeSystem.
     * @param updated   The information of the updated CodeSystem.
     * @return  The updated CodeSystem.
     * @throws CodeSystemNotFoundException  if no CodeSystem is found with given id.
     */
    public CodeSystemDto update(CodeSystemDto updated) throws CodeSystemNotFoundException;

}
