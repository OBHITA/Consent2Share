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
package gov.samhsa.acs.documentsegmentation;

import gov.samhsa.acs.brms.RuleExecutionService;
import gov.samhsa.acs.brms.domain.ClinicalFact;
import gov.samhsa.acs.brms.domain.FactModel;
import gov.samhsa.acs.brms.domain.RuleExecutionContainer;
import gov.samhsa.acs.brms.domain.XacmlResult;
import gov.samhsa.acs.common.exception.DS4PException;
import gov.samhsa.acs.common.tool.SimpleMarshaller;
import gov.samhsa.acs.common.util.EncryptTool;
import gov.samhsa.acs.common.validation.XmlValidation;
import gov.samhsa.acs.common.validation.exception.InvalidXmlDocumentException;
import gov.samhsa.acs.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.acs.documentsegmentation.audit.AuditService;
import gov.samhsa.acs.documentsegmentation.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.acs.documentsegmentation.exception.InvalidSegmentedClinicalDocumentException;
import gov.samhsa.acs.documentsegmentation.tools.AdditionalMetadataGeneratorForSegmentedClinicalDocument;
import gov.samhsa.acs.documentsegmentation.tools.DocumentEditor;
import gov.samhsa.acs.documentsegmentation.tools.DocumentEncrypter;
import gov.samhsa.acs.documentsegmentation.tools.DocumentFactModelExtractor;
import gov.samhsa.acs.documentsegmentation.tools.DocumentMasker;
import gov.samhsa.acs.documentsegmentation.tools.DocumentRedactor;
import gov.samhsa.acs.documentsegmentation.tools.DocumentTagger;
import gov.samhsa.acs.documentsegmentation.tools.EmbeddedClinicalDocumentExtractor;
import gov.samhsa.acs.documentsegmentation.valueset.ValueSetService;
import gov.samhsa.consent2share.schema.documentsegmentation.SegmentDocumentResponse;

import java.security.Key;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * The Class DocumentSegmentationImpl.
 */
public class DocumentSegmentationImpl implements DocumentSegmentation {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The rule execution web service client. */
	private final RuleExecutionService ruleExecutionService;

	/** The audit service. */
	private final AuditService auditService;

	/** The de sede encrypt key. */
	private Key deSedeEncryptKey;

	/** The de sede mask key. */
	private Key deSedeMaskKey;

	/** The document editor. */
	private final DocumentEditor documentEditor;

	/** The marshaller. */
	private final SimpleMarshaller marshaller;

	/** The document encrypter. */
	private final DocumentEncrypter documentEncrypter;

	/** The document redactor. */
	private final DocumentRedactor documentRedactor;

	/** The document masker. */
	private final DocumentMasker documentMasker;

	/** The document tagger. */
	private final DocumentTagger documentTagger;

	/** The document fact model extractor. */
	private final DocumentFactModelExtractor documentFactModelExtractor;

	/** The embedded clinical document extractor. */
	private final EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;

	/** The value set service. */
	private final ValueSetService valueSetService;

	/** The additional metadata generator for segmented clinical document. */
	private final AdditionalMetadataGeneratorForSegmentedClinicalDocument additionalMetadataGeneratorForSegmentedClinicalDocument;

	/** The xml validator. */
	private XmlValidation xmlValidator;

	/** The Constant C32_CDA_XSD_PATH. */
	public static final String C32_CDA_XSD_PATH = "schema/cdar2c32/infrastructure/cda/";

	/** The Constant C32_CDA_XSD_NAME. */
	public static final String C32_CDA_XSD_NAME = "C32_CDA.xsd";

	/**
	 * Instantiates a new document processor impl.
	 * 
	 * @param ruleExecutionService
	 *            the rule execution service
	 * @param auditService
	 *            the audit service
	 * @param documentEditor
	 *            the document editor
	 * @param marshaller
	 *            the marshaller
	 * @param documentEncrypter
	 *            the document encrypter
	 * @param documentRedactor
	 *            the document redactor
	 * @param documentMasker
	 *            the document masker
	 * @param documentTagger
	 *            the document tagger
	 * @param documentFactModelExtractor
	 *            the document fact model extractor
	 * @param embeddedClinicalDocumentExtractor
	 *            the embedded clinical document extractor
	 * @param valueSetService
	 *            the value set service
	 * @param additionalMetadataGeneratorForSegmentedClinicalDocument
	 *            the additional metadata generator for segmented clinical
	 *            document
	 */
	public DocumentSegmentationImpl(
			RuleExecutionService ruleExecutionService,
			AuditService auditService,
			DocumentEditor documentEditor,
			SimpleMarshaller marshaller,
			DocumentEncrypter documentEncrypter,
			DocumentRedactor documentRedactor,
			DocumentMasker documentMasker,
			DocumentTagger documentTagger,
			DocumentFactModelExtractor documentFactModelExtractor,
			EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor,
			ValueSetService valueSetService,
			AdditionalMetadataGeneratorForSegmentedClinicalDocument additionalMetadataGeneratorForSegmentedClinicalDocument) {

		this.ruleExecutionService = ruleExecutionService;
		this.auditService = auditService;
		this.documentEditor = documentEditor;
		this.marshaller = marshaller;
		this.documentEncrypter = documentEncrypter;
		this.documentRedactor = documentRedactor;
		this.documentMasker = documentMasker;
		this.documentTagger = documentTagger;
		this.documentFactModelExtractor = documentFactModelExtractor;
		this.embeddedClinicalDocumentExtractor = embeddedClinicalDocumentExtractor;
		this.valueSetService = valueSetService;
		this.additionalMetadataGeneratorForSegmentedClinicalDocument = additionalMetadataGeneratorForSegmentedClinicalDocument;
		this.xmlValidator = createXmlValidator();
	}

	/**
	 * Segment document.
	 * 
	 * @param document
	 *            the document
	 * @param enforcementPolicies
	 *            the enforcement policies
	 * @param packageAsXdm
	 *            the package as xdm
	 * @param encryptDocument
	 *            the encrypt document
	 * @param senderEmailAddress
	 *            the sender email address
	 * @param recipientEmailAddress
	 *            the recipient email address
	 * @param xdsDocumentEntryUniqueId
	 *            the xds document entry unique id
	 * @return the process document response
	 * @throws XmlDocumentReadFailureException
	 *             the xml document read failure exception
	 * @throws InvalidOriginalClinicalDocumentException
	 *             the invalid original clinical document exception
	 * @throws InvalidSegmentedClinicalDocumentException
	 *             the invalid segmented clinical document exception
	 */
	@Override
	public SegmentDocumentResponse segmentDocument(String document,
			String enforcementPolicies, boolean packageAsXdm,
			boolean encryptDocument, String senderEmailAddress,
			String recipientEmailAddress, String xdsDocumentEntryUniqueId)
			throws XmlDocumentReadFailureException,
			InvalidOriginalClinicalDocumentException,
			InvalidSegmentedClinicalDocumentException {

		Assert.notNull(document);
		String err = "";
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("Schema validation is failed for original clinical document.");
			builder.append((xdsDocumentEntryUniqueId != null ? " documentUniqueId:" + xdsDocumentEntryUniqueId:""));
			err =  builder.toString() ;
			Assert.isTrue(xmlValidator.validate(document),err);
		} catch (InvalidXmlDocumentException e) {
			logger.error(err, e);
			logger.error("Invalid Original Clinical Document: ");
			logger.error(document);
			throw new InvalidOriginalClinicalDocumentException(err, e);
		} catch (XmlDocumentReadFailureException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		Assert.notNull(enforcementPolicies);

		RuleExecutionContainer ruleExecutionContainer = null;
		XacmlResult xacmlResult = null;
		ByteArrayDataSource rawData = null;
		enforcementPolicies = enforcementPolicies.replace(
				" xmlns:ns2=\"http://ws.ds4p.ehtac.va.gov/\"", "");
		SegmentDocumentResponse segmentDocumentResponse = new SegmentDocumentResponse();

		try {

			document = documentEditor.setDocumentCreationDate(document);

			// FileHelper.writeStringToFile(document, "Original_C32.xml");

			// extract factModel
			String factModelXml = documentFactModelExtractor.extractFactModel(
					document, enforcementPolicies);
			// get clinical document with generatedEntryId elements
			document = embeddedClinicalDocumentExtractor
					.extractClinicalDocumentFromFactModel(factModelXml);
			FactModel factModel = marshaller.unmarshallFromXml(FactModel.class,
					factModelXml);
			// Get and set value set categories to clinical facts
			for (ClinicalFact fact : factModel.getClinicalFactList()) {
				// Get value set categories
				Set<String> valueSetCategories = valueSetService
						.lookupValueSetCategories(fact.getCode(),
								fact.getCodeSystem());
				// Set retrieved value set categories to the clinical fact
				fact.setValueSetCategories(valueSetCategories);
			}
			// FileHelper.writeStringToFile(factModel, "FactModel.xml");

			// get execution response container
			String executionResponseContainer = ruleExecutionService
					.assertAndExecuteClinicalFacts(factModel)
					.getRuleExecutionResponseContainer();

			// unmarshall from xml to RuleExecutionContainer
			ruleExecutionContainer = marshaller.unmarshallFromXml(
					RuleExecutionContainer.class, executionResponseContainer);

			// FileHelper.writeStringToFile(executionResponseContainer,
			// "ExecutionResponseContainer.xml");

			// unmarshall from xml to XacmlResult
			xacmlResult = marshaller.unmarshallFromXml(XacmlResult.class,
					enforcementPolicies);

			logger.info("Fact model: " + factModelXml);
			logger.info("Rule Execution Container size: "
					+ ruleExecutionContainer.getExecutionResponseList().size());

			// redact document
			document = documentRedactor.redactDocument(document,
					ruleExecutionContainer, xacmlResult, factModel);
			// to get the itemActions from documentRedactor
			executionResponseContainer = marshaller
					.marshall(ruleExecutionContainer);

			// tag document
			document = documentTagger.tagDocument(document,
					executionResponseContainer, xacmlResult.getMessageId());

			// clean up generatedEntryId elements from document
			document = documentRedactor.cleanUpGeneratedEntryIds(document);

			String additionalMetadataForSegmentedClinicalDocument = additionalMetadataGeneratorForSegmentedClinicalDocument
					.generateMetadataXml(xacmlResult.getMessageId(), document,
							executionResponseContainer, senderEmailAddress,
							recipientEmailAddress, xacmlResult
									.getSubjectPurposeOfUse().getPurpose(),
							xdsDocumentEntryUniqueId);
			// FileHelper.writeStringToFile(additionalMetadataForSegmentedClinicalDocument,"additional_metadata.xml");

			segmentDocumentResponse
					.setPostProcessingMetadata(additionalMetadataForSegmentedClinicalDocument);

			// log annotated doc
			auditService.updateAuthorizationEventWithAnnotatedDoc(
					xacmlResult.getMessageId(), document);
			// FileHelper.writeStringToFile(document, "Tagged_C32.xml");

			// mask document
			/*
			 * Get a key to be used for encrypting the symmetric key. Here we
			 * are generating a DESede key.
			 */
			deSedeMaskKey = EncryptTool.generateKeyEncryptionKey();
			document = documentMasker.maskDocument(document, deSedeMaskKey,
					ruleExecutionContainer, xacmlResult);
			segmentDocumentResponse.setMaskedDocument(document);

			byte[] maskingKeyBytes = deSedeMaskKey.getEncoded();
			segmentDocumentResponse.setKekMaskingKey(maskingKeyBytes);

			byte[] encryptionKeyBytes = null;
			// encrypt document
			if (encryptDocument) {
				/*
				 * Get a key to be used for encrypting the symmetric key. Here
				 * we are generating a DESede key.
				 */
				deSedeEncryptKey = EncryptTool.generateKeyEncryptionKey();
				document = documentEncrypter.encryptDocument(deSedeEncryptKey,
						document, ruleExecutionContainer);
				encryptionKeyBytes = deSedeEncryptKey.getEncoded();

				segmentDocumentResponse.setKekEncryptionKey(encryptionKeyBytes);
			}

			rawData = documentEditor.setDocumentPayloadRawData(document,
					packageAsXdm, senderEmailAddress, recipientEmailAddress,
					xacmlResult, executionResponseContainer, maskingKeyBytes,
					encryptionKeyBytes);
			segmentDocumentResponse.setProcessedDocument(new DataHandler(
					rawData));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new DS4PException(e.toString(), e);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new DS4PException(e.toString(), e);
		}

		try {
			StringBuilder builder = new StringBuilder();
			builder.append("Schema validation is failed for segmented clinical document.");
			builder.append((xdsDocumentEntryUniqueId != null ? " documentUniqueId:" + xdsDocumentEntryUniqueId:""));
			err = builder.toString();
			Assert.isTrue(xmlValidator.validate(document),err);
		} catch (InvalidXmlDocumentException e) {
			logger.error(err, e);
			logger.error("Invalid Segmented Clinical Document: ");
			logger.error(document);
			throw new InvalidSegmentedClinicalDocumentException(err, e);
		} catch (XmlDocumentReadFailureException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return segmentDocumentResponse;
	}

	/**
	 * Creates the xml validator.
	 * 
	 * @return the xml validation
	 */
	private XmlValidation createXmlValidator() {
		return new XmlValidation(this.getClass().getClassLoader().getResourceAsStream(C32_CDA_XSD_PATH + C32_CDA_XSD_NAME),C32_CDA_XSD_PATH);
	}
}
