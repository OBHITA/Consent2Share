package gov.samhsa.consent2share.service.valueset;

public class ConceptCodeNotFoundException extends Exception {

	private static final long serialVersionUID = 3234232442718766659L;
	public ConceptCodeNotFoundException(){
		super();
	}
	
	public ConceptCodeNotFoundException(String message)
	{
		super(message);
	}	
	
}
