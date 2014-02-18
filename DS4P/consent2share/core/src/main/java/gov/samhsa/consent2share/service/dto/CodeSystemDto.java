package gov.samhsa.consent2share.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;

public class CodeSystemDto extends AbstractNodeDto{

    
    private String displayName;	
    
    private List<ValueSetDto> valueSets = new ArrayList<ValueSetDto>();
    
    @NotEmpty
    private String codeSystemOId;

	
    public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public String getCodeSystemOId() {
		return codeSystemOId;
	}


	public void setCodeSystemOId(String codeSystemOId) {
		this.codeSystemOId = codeSystemOId;
	}


	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


	public List<ValueSetDto> getValueSets() {
		return valueSets;
	}


	public void setValueSets(List<ValueSetDto> valueSets) {
		this.valueSets = valueSets;
	}
	
	
}
