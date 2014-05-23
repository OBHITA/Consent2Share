/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 *   
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *       * Redistributions of source code must retain the above copyright
 *         notice, this list of conditions and the following disclaimer.
 *       * Redistributions in binary form must reproduce the above copyright
 *         notice, this list of conditions and the following disclaimer in the
 *         documentation and/or other materials provided with the distribution.
 *       * Neither the name of the <organization> nor the
 *         names of its contributors may be used to endorse or promote products
 *         derived from this software without specific prior written permission.
 *   
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.acs.xdsb.repository.wsclient.adapter;

import java.util.List;

import org.springframework.util.Assert;

import gov.samhsa.acs.common.tool.SimpleMarshaller;
import gov.samhsa.acs.common.tool.exception.SimpleMarshallerException;
import gov.samhsa.acs.xdsb.common.UniqueOidProviderImpl;
import gov.samhsa.acs.xdsb.common.XdsbDocumentType;
import gov.samhsa.acs.xdsb.common.XdsbMetadataGenerator;
import gov.samhsa.acs.xdsb.common.XdsbMetadataGeneratorImpl;
import gov.samhsa.acs.xdsb.repository.wsclient.XDSRepositorybWebServiceClient;
import gov.samhsa.acs.xdsb.repository.wsclient.exception.XdsbRepositoryAdapterException;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequest;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequest.Document;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequest.DocumentRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponse;
import oasis.names.tc.ebxml_regrep.xsd.lcm._3.SubmitObjectsRequest;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponse;

/**
 * The Class XdsbRepositoryAdapter.
 */
public class XdsbRepositoryAdapter {

	/** The Constant EMPTY_XML_DOCUMENT. */
	public static final String EMPTY_XML_DOCUMENT = "<empty/>";

	// Services
	/** The xdsb repository. */
	private XDSRepositorybWebServiceClient xdsbRepository;

	/** The marshaller. */
	private SimpleMarshaller marshaller;

	/** The response filter. */
	private RetrieveDocumentSetResponseFilter responseFilter;

	/**
	 * Instantiates a new xdsb repository adapter.
	 */
	public XdsbRepositoryAdapter() {
	}

	/**
	 * Instantiates a new xdsb repository adapter.
	 * 
	 * @param xdsbRepository
	 *            the xdsb repository
	 * @param marshaller
	 *            the marshaller
	 * @param responseFilter
	 *            the response filter
	 */
	public XdsbRepositoryAdapter(XDSRepositorybWebServiceClient xdsbRepository,
			SimpleMarshaller marshaller,
			RetrieveDocumentSetResponseFilter responseFilter) {

		this.xdsbRepository = xdsbRepository;
		this.marshaller = marshaller;
		this.responseFilter = responseFilter;
	}

	/**
	 * Provide and register document set request (direct call to the XDS.b
	 * repository service).
	 * 
	 * @param provideAndRegisterDocumentSetRequest
	 *            the provide and register document set request
	 * @return the registry response
	 */
	public RegistryResponse provideAndRegisterDocumentSet(
			ProvideAndRegisterDocumentSetRequest provideAndRegisterDocumentSetRequest) {
		return xdsbRepository
				.provideAndRegisterDocumentSet(provideAndRegisterDocumentSetRequest);
	}

	/**
	 * Provide and register document set request (indirect call to the XDS.b
	 * repository service with a simplified interface).
	 * 
	 * @param documentXmlString
	 *            the document xml string (Pass
	 *            XdsbRepositoryAdapter.EMPTY_XML_DOCUMENT if deprecating a
	 *            document. Otherwise, pass the actual document to be provided.)
	 * @param homeCommunityId
	 *            the home community id (May pass null if deprecating a
	 *            document.)
	 * @param documentTypeForXdsbMetadata
	 *            the document type for xdsb metadata
	 * @param patientUniqueId
	 *            the patient unique id (Pass this only if deprecating a
	 *            document. Otherwise, pass null.)
	 * @param entryUUID
	 *            the entry uuid (Pass this only if deprecating a document.
	 *            Otherwise, pass null.)
	 * @return the registry response
	 * @throws XdsbRepositoryAdapterException
	 *             the xdsb repository adapter exception
	 */
	public RegistryResponse provideAndRegisterDocumentSet(
			String documentXmlString, String homeCommunityId,
			XdsbDocumentType documentTypeForXdsbMetadata,
			String patientUniqueId, String entryUUID)
			throws XdsbRepositoryAdapterException {
		switch (documentTypeForXdsbMetadata) {
		case DEPRECATE_PRIVACY_CONSENT:
			String messageDeprecate = "patientUniqueId and entryUUID must be injected to deprecate a document.";
			Assert.notNull(patientUniqueId, messageDeprecate);
			Assert.notNull(entryUUID, messageDeprecate);
			break;
		default:
			String messageNotDeprecate = "patientUniqueId and entryUUID can only be injected to deprecate a document.";
			Assert.isNull(patientUniqueId, messageNotDeprecate);
			Assert.isNull(entryUUID, messageNotDeprecate);
			break;
		}

		String submitObjectRequestXml = generateMetadata(documentXmlString,
				homeCommunityId, documentTypeForXdsbMetadata, patientUniqueId,
				entryUUID);
		SubmitObjectsRequest submitObjectRequest;
		try {
			submitObjectRequest = marshaller.unmarshallFromXml(
					SubmitObjectsRequest.class, submitObjectRequestXml);
		} catch (SimpleMarshallerException e) {
			throw new XdsbRepositoryAdapterException(e);
		}

		Document document = null;
		if (!documentXmlString.equals(EMPTY_XML_DOCUMENT)) {
			document = createDocument(documentXmlString);
		}

		ProvideAndRegisterDocumentSetRequest request = createProvideAndRegisterDocumentSetRequest(
				submitObjectRequest, document);

		RegistryResponse response = provideAndRegisterDocumentSet(request);
		return response;
	}

	/**
	 * Retrieve document set request (direct call to XDS.b repository service).
	 * 
	 * @param request
	 *            the request
	 * @return the retrieve document set response
	 */
	public RetrieveDocumentSetResponse retrieveDocumentSet(
			RetrieveDocumentSetRequest request) {
		return xdsbRepository.retrieveDocumentSet(request);
	}

	/**
	 * Retrieve document set.
	 * 
	 * @param request
	 *            the request
	 * @param patientId
	 *            the patient id
	 * @param authorNPI
	 *            the author npi
	 * @return the retrieve document set response
	 * @throws XdsbRepositoryAdapterException
	 *             the xdsb repository adapter exception
	 */
	public RetrieveDocumentSetResponse retrieveDocumentSet(
			RetrieveDocumentSetRequest request, String patientId,
			String authorNPI) throws XdsbRepositoryAdapterException {
		try {
			RetrieveDocumentSetResponse response = xdsbRepository
					.retrieveDocumentSet(request);
			response = responseFilter.filterByPatientAndAuthor(response,
					patientId, authorNPI);
			return response;
		} catch (Throwable t) {
			throw new XdsbRepositoryAdapterException(t);
		}
	}

	/**
	 * Retrieve document set request (indirect call to the XDS.b repository
	 * service with a simplified interface).
	 * 
	 * @param documentUniqueId
	 *            the document unique id
	 * @param repositoryId
	 *            the repository id
	 * @return the retrieve document set response
	 */
	public RetrieveDocumentSetResponse retrieveDocumentSet(
			String documentUniqueId, String repositoryId) {
		RetrieveDocumentSetRequest request = createRetrieveDocumentSetRequest(
				documentUniqueId, repositoryId);

		RetrieveDocumentSetResponse retrieveDocumentSetResponse = retrieveDocumentSet(request);
		return retrieveDocumentSetResponse;
	}

	/**
	 * Retrieve document set.
	 * 
	 * @param docRequest
	 *            the doc request
	 * @return the retrieve document set response
	 */
	public RetrieveDocumentSetResponse retrieveDocumentSet(
			DocumentRequest docRequest) {
		RetrieveDocumentSetRequest request = createRetrieveDocumentSetRequest(docRequest);

		RetrieveDocumentSetResponse retrieveDocumentSetResponse = retrieveDocumentSet(request);
		return retrieveDocumentSetResponse;
	}

	/**
	 * Retrieve document set.
	 * 
	 * @param docRequest
	 *            the doc request
	 * @return the retrieve document set response
	 */
	public RetrieveDocumentSetResponse retrieveDocumentSet(
			List<DocumentRequest> docRequest) {
		RetrieveDocumentSetRequest request = createRetrieveDocumentSetRequest(docRequest);

		RetrieveDocumentSetResponse retrieveDocumentSetResponse = retrieveDocumentSet(request);
		return retrieveDocumentSetResponse;
	}

	/**
	 * Generate metadata.
	 * 
	 * @param documentXmlString
	 *            the document xml string
	 * @param homeCommunityId
	 *            the home community id
	 * @param documentTypeForXdsbMetadata
	 *            the document type for xdsb metadata
	 * @param patientUniqueId
	 *            the patient unique id
	 * @param entryUUID
	 *            the entry uuid
	 * @return the string
	 */
	String generateMetadata(String documentXmlString, String homeCommunityId,
			XdsbDocumentType documentTypeForXdsbMetadata,
			String patientUniqueId, String entryUUID) {
		XdsbMetadataGenerator xdsbMetadataGenerator = createXdsbMetadataGenerator(documentTypeForXdsbMetadata);
		String metadata = xdsbMetadataGenerator.generateMetadataXml(
				documentXmlString, homeCommunityId, patientUniqueId, entryUUID);
		return metadata;
	}

	/**
	 * Creates the xdsb metadata generator.
	 * 
	 * @param documentTypeForXdsbMetadata
	 *            the document type for xdsb metadata
	 * @return the xdsb metadata generator impl
	 */
	XdsbMetadataGeneratorImpl createXdsbMetadataGenerator(
			XdsbDocumentType documentTypeForXdsbMetadata) {
		return new XdsbMetadataGeneratorImpl(new UniqueOidProviderImpl(),
				documentTypeForXdsbMetadata, this.marshaller);
	}

	/**
	 * Creates the provide and register document set request.
	 * 
	 * @param submitObjectRequest
	 *            the submit object request
	 * @param document
	 *            the document
	 * @return the provide and register document set request
	 */
	ProvideAndRegisterDocumentSetRequest createProvideAndRegisterDocumentSetRequest(
			SubmitObjectsRequest submitObjectRequest, Document document) {
		ProvideAndRegisterDocumentSetRequest request = new ProvideAndRegisterDocumentSetRequest();
		request.setSubmitObjectsRequest(submitObjectRequest);
		if (document != null) {
			request.getDocument().add(document);
		}
		return request;
	}

	/**
	 * Creates the document.
	 * 
	 * @param documentXmlString
	 *            the document xml string
	 * @return the document
	 */
	Document createDocument(String documentXmlString) {
		Document document = new Document();
		document.setId("Document01");
		document.setValue(documentXmlString.getBytes());
		return document;
	}

	/**
	 * Creates the retrieve document set request.
	 * 
	 * @param documentUniqueId
	 *            the document unique id
	 * @param repositoryId
	 *            the repository id
	 * @return the retrieve document set request
	 */
	RetrieveDocumentSetRequest createRetrieveDocumentSetRequest(
			String documentUniqueId, String repositoryId) {
		RetrieveDocumentSetRequest request = new RetrieveDocumentSetRequest();
		DocumentRequest documentRequest = new DocumentRequest();

		documentRequest.setDocumentUniqueId(documentUniqueId);
		documentRequest.setRepositoryUniqueId(repositoryId);
		request.getDocumentRequest().add(documentRequest);
		return request;
	}

	/**
	 * Creates the retrieve document set request.
	 * 
	 * @param docRequest
	 *            the doc request
	 * @return the retrieve document set request
	 */
	RetrieveDocumentSetRequest createRetrieveDocumentSetRequest(
			DocumentRequest docRequest) {
		RetrieveDocumentSetRequest request = new RetrieveDocumentSetRequest();
		request.getDocumentRequest().add(docRequest);
		return request;
	}

	/**
	 * Creates the retrieve document set request.
	 * 
	 * @param docRequest
	 *            the doc request
	 * @return the retrieve document set request
	 */
	RetrieveDocumentSetRequest createRetrieveDocumentSetRequest(
			List<DocumentRequest> docRequest) {
		RetrieveDocumentSetRequest request = new RetrieveDocumentSetRequest();
		request.getDocumentRequest().addAll(docRequest);
		return request;
	}
}
