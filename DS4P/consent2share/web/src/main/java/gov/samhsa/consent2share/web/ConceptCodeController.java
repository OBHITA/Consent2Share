package gov.samhsa.consent2share.web;

import gov.samhsa.consent2share.infrastructure.security.AuthenticatedUser;
import gov.samhsa.consent2share.infrastructure.security.UserContext;
import gov.samhsa.consent2share.service.dto.CodeSystemDto;
import gov.samhsa.consent2share.service.dto.ConceptCodeDto;
import gov.samhsa.consent2share.service.dto.ConceptCodeVSCSDto;
import gov.samhsa.consent2share.service.dto.ValueSetDto;
import gov.samhsa.consent2share.service.valueset.CodeSystemNotFoundException;
import gov.samhsa.consent2share.service.valueset.CodeSystemService;
import gov.samhsa.consent2share.service.valueset.CodeSystemVersionNotFoundException;
import gov.samhsa.consent2share.service.valueset.ConceptCodeNotFoundException;
import gov.samhsa.consent2share.service.valueset.ConceptCodeService;
import gov.samhsa.consent2share.service.valueset.DuplicateConceptCodeException;
import gov.samhsa.consent2share.service.valueset.InvalidCSVException;
import gov.samhsa.consent2share.service.valueset.ValueSetNotFoundException;
import gov.samhsa.consent2share.service.valueset.ValueSetService;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Joiner;

@Controller
@SessionAttributes("conceptCode")
@RequestMapping("/sysadmin") 
public class ConceptCodeController extends AbstractNodeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConceptCodeController.class);
    
    protected static final String ERROR_MESSAGE_KEY_DELETED_CONCEPTCODE_WAS_NOT_FOUND = "Deleted conceptCode was not found.";
    protected static final String ERROR_MESSAGE_KEY_EDITED_CONCEPTCODE_WAS_NOT_FOUND = "Edited conceptCode was not found.";
    protected static final String ERROR_MESSAGE_VALUSETS_NOT_FOUND = "Value Sets not Found. To create and manage concept codes, you need to add Value Sets.";
    protected static final String ERROR_MESSAGE_CODESYSTEMVERSIONS_NOT_FOUND = "Code System Versions not Found. To create and manage concept codes, you need to add Code System Versions.";
    protected static final String ERROR_MESSAGE_CODESYSTEMS_NOT_FOUND = "Code System Versions not Found. To create and manage concept codes, you need to add Code System Versions.";
    protected static final String ERROR_MESSAGE_KEY_SELECTED_CODESYSTEM_WAS_NOT_FOUND = "Selected code system was not found.";
    protected static final String ERROR_MESSAGE_KEY_DUPLICATE_CODE_FOUND = "ConceptCode Already Exists";
    protected static final String ERROR_MESSAGE_KEY_SELECTED_VALUESET_WAS_NOT_FOUND = "Selected valueSet was not found.";
  
    protected static final String MODEL_ATTIRUTE_CONCEPTCODEDTO = "conceptCodeDto";
    protected static final String MODEL_ATTRIBUTE_CONCEPTCODEDTOS = "conceptCodeDtos";
    
    protected static final String CONCEPTCODE_ADD_FORM_VIEW = "views/sysadmin/conceptCodeAdd";
    protected static final String CONCEPTCODE_EDIT_FORM_VIEW = "views/sysadmin/conceptCodeEdit";
    protected static final String CONCEPTCODE_LIST_VIEW = "views/sysadmin/conceptCodeList";
    
    protected static final String REQUEST_MAPPING_LIST = "/conceptCodeList";
    protected static final String REDIRECT_MAPPING_LIST = "../conceptCodeList";   
    protected static final String REDIRECT_ID_MAPPING_LIST = "../../conceptCodeList";   

    protected static final String MODEL_ATTIRUTE_CONCEPTCODEVSCSDTO = "conceptCodeVSCSDto";
    protected static final String MODEL_ATTIRUTE_CODESYSTEMS = "codeSystems";
    protected static final String MODEL_ATTIRUTE_VALSETNAMES = "valueSets";

    
    @Resource
    private ConceptCodeService conceptCodeService;
    
    @Resource
    private CodeSystemService codeSystemService;
    
    @Resource
    private ValueSetService valueSetService;
    
	/** The user context. */
	@Autowired
	UserContext userContext;
	
    /**
     * Processes create conceptCode requests.
     * @param model
     * @return  The name of the create conceptCode form view.
     */
    @RequestMapping(value = REQUEST_MAPPING_LIST, method = RequestMethod.GET)
    public String showCreateConceptCodeForm(Model model,
    			@RequestParam(value = "panelState", required = false, defaultValue = "") String panelState){
    	
    	ConceptCodeVSCSDto conceptCodeVSCSDto = null;
    	
    	List<CodeSystemDto> codeSystems = null;
    	List<ValueSetDto> valueSets = null;
    	
        LOGGER.debug("Rendering Concept Code list page");
        try {
        	conceptCodeVSCSDto = conceptCodeService.create();
        	codeSystems = codeSystemService.findAll();
        	valueSets = valueSetService.findAll();
        } catch (ValueSetNotFoundException e) {
	           LOGGER.debug("No value sets found in the system");
		} catch (CodeSystemNotFoundException e) {
	           LOGGER.debug("No code systems found in the system");
		} catch (CodeSystemVersionNotFoundException e) {
	           LOGGER.debug("No code system versions found in the system");
		}
        
        if(panelState.equals("resetoptions")){
			model.addAttribute("panelState", "resetoptions");
		}else if(panelState.equals("addnew")){
			model.addAttribute("panelState", "addnew");
		}
        
        model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEVSCSDTO, conceptCodeVSCSDto);
        model.addAttribute(MODEL_ATTIRUTE_CODESYSTEMS, codeSystems);
        model.addAttribute(MODEL_ATTIRUTE_VALSETNAMES, valueSets);
        return CONCEPTCODE_LIST_VIEW;
    }
    
	
    /**
     * Processes the submissions of create conceptCode form.
     * @param created   The information of the created conceptCodes.
     * @param bindingResult
     * @param attributes
     * @return
     */
    @RequestMapping(value = "conceptCodeAdd.html", method = RequestMethod.GET)
    public String conceptCodeAddForm(Model model, HttpServletRequest request) {
        LOGGER.debug("Rendering Concept Code Add Form ");
        ConceptCodeVSCSDto conceptCodeVSCSDto = new ConceptCodeVSCSDto();
        ConceptCodeDto conceptCodeDto = new ConceptCodeDto();
        String returnPage = CONCEPTCODE_ADD_FORM_VIEW;
		try {
			conceptCodeVSCSDto = conceptCodeService.create();
			conceptCodeVSCSDto.setError(false);
			conceptCodeDto = (ConceptCodeDto) model.asMap().get(MODEL_ATTIRUTE_CONCEPTCODEDTO);
	        if(conceptCodeDto != null)
	        	 conceptCodeVSCSDto.setConceptCodeDto(conceptCodeDto);      
	
		} catch (ValueSetNotFoundException e) {
	           LOGGER.debug("No value sets found in the system");
	           conceptCodeDto.setError(true);
	           conceptCodeDto.setErrorMessage(ERROR_MESSAGE_VALUSETS_NOT_FOUND);
	           returnPage = CONCEPTCODE_LIST_VIEW;
		} catch (CodeSystemNotFoundException e) {
	           LOGGER.debug("No code systems found in the system");
	           conceptCodeDto.setError(true);
	           conceptCodeDto.setErrorMessage(ERROR_MESSAGE_CODESYSTEMVERSIONS_NOT_FOUND);
	           returnPage = CONCEPTCODE_LIST_VIEW;
		} catch (CodeSystemVersionNotFoundException e) {
	           LOGGER.debug("No code system versions found in the system");
	           conceptCodeDto.setError(true);
	           conceptCodeDto.setErrorMessage(ERROR_MESSAGE_CODESYSTEMS_NOT_FOUND);
	           returnPage = CONCEPTCODE_LIST_VIEW;
		}
       
        model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEVSCSDTO, conceptCodeVSCSDto);
		model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, conceptCodeDto);

		return returnPage;
    }
    
       
    /*
     * Processes the submissions of create conceptCode form.
     * @param created   The information of the created conceptCodes.
     * @param bindingResult
     * @param attributes
     * @return
     */
    @RequestMapping(value = "/conceptCode/create", method = RequestMethod.POST)
    public String ceateConceptCodeForm(@Valid @ModelAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO) ConceptCodeDto created, RedirectAttributes redirectAttribute,  Model model) {
        LOGGER.debug("Create conceptCode form was submitted with information: " + created);
		
        AuthenticatedUser currentUser = userContext.getCurrentUser();
        String path = "../conceptCodeAdd.html";
		try {
			created.setUserName(currentUser.getUsername());
			created = conceptCodeService.create(created);
			created.setError(false);
			created.setSuccessMessage("Concept Code  with code:" + created.getCode() + " and Name: " + created.getName() + " is Added Successfully");
			path = REDIRECT_MAPPING_LIST; 
		} catch (DataIntegrityViolationException ex) {
			LOGGER.info(ex.getLocalizedMessage());
			Throwable t = ex.getCause();
			String message = null;
			if (t != null) {
				message = "Cause: " + t.getMessage();
			}
			created.setError(true);
			created.setErrorMessage("Concept Code is not Added " + message);
			
		} catch (ValueSetNotFoundException e) {
            LOGGER.debug("No value set found with id: " + created.getValueSetId());
            created.setError(true);
            created.setErrorMessage(ERROR_MESSAGE_KEY_SELECTED_VALUESET_WAS_NOT_FOUND);
        } catch (CodeSystemNotFoundException e) {
            LOGGER.debug("No code systm found with id: " + created.getCodeSystemId());
            created.setError(true);
            created.setErrorMessage(ERROR_MESSAGE_KEY_SELECTED_CODESYSTEM_WAS_NOT_FOUND);
		} catch (DuplicateConceptCodeException e) {
           LOGGER.debug("Duplicate Code : " + created.getCodeSystemId());
           created.setError(true);
           created.setErrorMessage(ERROR_MESSAGE_KEY_DUPLICATE_CODE_FOUND);
		} 
		model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, created);
		redirectAttribute.addFlashAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, created);
		
		String panelState = "?panelState=addnew";

		return createRedirectViewPath(path) + panelState;
    }	    
    
	/**
     * Processes delete conceptCode requests.
     * @param id    The id of the deleted conceptCode.
     * @param attributes
     * @return
     */
    @RequestMapping(value = "/conceptCode/delete/{id}", method = RequestMethod.DELETE)
    public @ResponseBody String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttribute) {
        LOGGER.debug("Deleting conceptCode with id: " + id);
        ConceptCodeDto deleted = new ConceptCodeDto();
        try {
            deleted = conceptCodeService.delete(id);
            deleted.setError(false);
            deleted.setSuccessMessage("ConceptCode with Code: " + deleted.getCode() + " and Name: " + deleted.getName() +" is deleted Successfully.");
        } catch (ConceptCodeNotFoundException e) {
            LOGGER.debug("No conceptCode found with id: " + id);
            deleted.setError(true);
            deleted.setErrorMessage(ERROR_MESSAGE_KEY_DELETED_CONCEPTCODE_WAS_NOT_FOUND);
        }
        redirectAttribute.addFlashAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, deleted);
        
        if(deleted.isError == false){
			return deleted.getSuccessMessage();
		}else{
			throw new AjaxException(HttpStatus.INTERNAL_SERVER_ERROR, deleted.getErrorMessage());
		}
    }



    /**
     * Processes edit conceptCode requests.
     * @param id    The id of the edited conceptCode.
     * @param model
     * @param attributes
     * @return  The name of the edit conceptCode form view.
     */
    @RequestMapping(value = "/conceptCode/edit/{id}", method = RequestMethod.GET)
    public String showEditConceptCodeForm(@PathVariable("id") Long id, Model model, RedirectAttributes attributes) {
        LOGGER.debug("Rendering edit conceptCode form for conceptCode with id: " + id);
        ConceptCodeDto conceptCodeDto = new ConceptCodeDto();
		try {
			conceptCodeDto = conceptCodeService.findById(id);
	        if (conceptCodeDto == null) {
	            LOGGER.debug("No conceptCode found with id: " + id);
	            conceptCodeDto = new ConceptCodeDto();
	            conceptCodeDto.setError(true);
	            conceptCodeDto.setErrorMessage(ERROR_MESSAGE_KEY_EDITED_CONCEPTCODE_WAS_NOT_FOUND);
	            return createRedirectViewPath(REDIRECT_ID_MAPPING_LIST);          
	        }
	    } catch (ValueSetNotFoundException e) {
	           LOGGER.debug(e.getMessage());
	           conceptCodeDto.setError(true);
	           conceptCodeDto.setErrorMessage( e.getMessage());
		} catch (ConceptCodeNotFoundException e) {
	           LOGGER.debug(e.getMessage());
	           conceptCodeDto.setError(true);
	           conceptCodeDto.setErrorMessage( e.getMessage());
		}
        model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, conceptCodeDto);
        
        return CONCEPTCODE_EDIT_FORM_VIEW;
    }

    /**
     * Processes the submissions of edit conceptCode form.
     * @param updated   The information of the edited conceptCode.
     * @param bindingResult
     * @param attributes
     * @return
     */
    @RequestMapping(value = "/conceptCode/edit/{id}", method = RequestMethod.POST)
    public String submitEditConceptCodeForm(@ModelAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO) ConceptCodeDto updated, @PathVariable("id") Long id, RedirectAttributes redirectAttribute, Model model) {
        LOGGER.debug("Edit conceptCode form was submitted with information: " + updated + id);
        String path = REDIRECT_ID_MAPPING_LIST; //"../../conceptCodeEdit.html";
        try {
	        AuthenticatedUser currentUser = userContext.getCurrentUser();
	        updated.setUserName(currentUser.getUsername());
	        updated.setId(id);
            updated = conceptCodeService.update(updated);
            updated.setError(false);
            updated.setSuccessMessage("Concept Code  with Code: " + updated.getCode() + " and Name: " + updated.getName() + " is Edited Successfully");
           // path = REDIRECT_ID_MAPPING_LIST;

        } catch (ConceptCodeNotFoundException e) {
            LOGGER.debug("No conceptCode was found with id: " + updated.getId());
			updated.setError(true);
			updated.setErrorMessage( "Edited Concept Code is not found");
        } catch (ValueSetNotFoundException e) {
            LOGGER.debug(e.getMessage());
 			updated.setError(true);
 			updated.setErrorMessage( e.getMessage());
		}
		model.addAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, updated);
		redirectAttribute.addFlashAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, updated);
       	 return createRedirectViewPath(path);
       
    }

    /**
     * This setter method should only be used by unit tests
     * @param conceptCodeService
     */
    protected void setConceptCodeService(ConceptCodeService conceptCodeService) {
        this.conceptCodeService = conceptCodeService;
    }
    
    /**
     * Search for concept code via AJAX call
     * 
     * @param searchCategory
     * @param searchTerm
     * @return conceptCodes
     */
	@RequestMapping("/conceptCode/ajaxSearchConceptCode")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<ConceptCodeDto> searchConceptCode(
			@RequestParam(value ="searchCategory",required = true) String searchCategory, 
			@RequestParam(value ="searchTerm",required = true) String searchTerm) {
		
		List<ConceptCodeDto> conceptCodes=null;
		
		try{
			if(searchCategory.equals("code"))
				conceptCodes = conceptCodeService.findAllByCode(searchTerm);
			else if(searchCategory.equals("name"))
				conceptCodes = conceptCodeService.findAllByName(searchTerm);
		}catch(IllegalArgumentException e){
			conceptCodes = null;
			throw new AjaxException(HttpStatus.BAD_REQUEST, "Unable to perform search because the request parameters contained invalid data.");
		}catch(Exception e){
			conceptCodes = null;
			LOGGER.warn("An exception was caught in search ConceptCode.");
			throw new AjaxException(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error has occured.");
		}
		
		return conceptCodes;
	}
	
	
	/**
	 * Get all concept codes via AJAX
	 * 
	 * @return conceptCodes
	 */
	@RequestMapping("/conceptCode/ajaxGetAllConceptCodes")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<ConceptCodeDto> getAllConceptCodes() {
		
		List<ConceptCodeDto> conceptCodes = null;
		
		try{
			conceptCodes = conceptCodeService.findAll();
		}catch(Exception e){
			conceptCodes = null;
			LOGGER.warn("An exception was caught in search ConceptCode.");
			throw new AjaxException(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error has occured.");
		}
		
		return conceptCodes;
	}

	@RequestMapping(value = "/conceptCode/batchUpload", method = RequestMethod.POST)
	public String batchUpload(@RequestParam(value = "codeSystemId", required=false) String codeSystemId,
			@RequestParam(value = "codeSystemVersionId", required=false) Long codeSystemVersionId, HttpServletRequest request,
			@RequestParam("batch_file") MultipartFile file, @RequestParam(value="valueSetIds", required=false) List<Long> valueSetIds,
			RedirectAttributes redirectAttribute) throws Exception {
		
		AuthenticatedUser currentUser = userContext.getCurrentUser();
		ConceptCodeDto conceptCodeDto = new ConceptCodeDto();
		conceptCodeDto.setUserName(currentUser.getUsername());
		
		try {
			
			conceptCodeDto = conceptCodeService.conceptCodeBatchUpload(conceptCodeDto, file, codeSystemId, codeSystemVersionId, valueSetIds);
			
			if (conceptCodeDto.getListOfDuplicatesCodes() != null && !conceptCodeDto.getListOfDuplicatesCodes().isEmpty()) {
				if (conceptCodeDto.getConceptCodesInserted() > 0) {
					throw new InvalidCSVException("Added " + conceptCodeDto.getConceptCodesInserted() + " codes with errors. Following rows had duplicates codes: " + Joiner.on(",").join(conceptCodeDto.getListOfDuplicatesCodes()));
				} else {
					throw new InvalidCSVException("Error uploading concept codes. Following rows had duplicate codes: " + Joiner.on(",").join(conceptCodeDto.getListOfDuplicatesCodes()));
				}
			}
			else {
				conceptCodeDto.setSuccessMessage("Added " + conceptCodeDto.getConceptCodesInserted() + " Codes");
			}
			
		} catch (ValueSetNotFoundException ex) {
			LOGGER.debug("Invalid value set category found while doing batch upload: " + ex.getMessage());
			conceptCodeDto.setError(true);
		} catch (InvalidCSVException ex) {
			LOGGER.debug("Invalid csv format found while doing batch upload: " + ex.getMessage());
			conceptCodeDto.setError(true);
			conceptCodeDto.setErrorMessage(ex.getMessage());
		} catch (DataIntegrityViolationException ex) {
			LOGGER.debug("Duplicate value set while doing batch upload: " + ex.getMessage());
			conceptCodeDto.setError(true);
		}
		redirectAttribute.addFlashAttribute(MODEL_ATTIRUTE_CONCEPTCODEDTO, conceptCodeDto);

		return  createRedirectViewPath(REDIRECT_MAPPING_LIST);
	}
}
