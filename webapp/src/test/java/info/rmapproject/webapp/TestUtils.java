package info.rmapproject.webapp;

import static java.net.URI.create;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapAgent;
import info.rmapproject.core.model.impl.rdf4j.ORMapDiSCO;
import info.rmapproject.core.model.impl.rdf4j.OStatementsAdapter;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.core.rdfhandler.impl.rdf4j.RioRDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.indexing.solr.repository.DiscosIndexer;
import info.rmapproject.indexing.solr.repository.IndexDTO;
import info.rmapproject.indexing.solr.repository.IndexDTOMapper;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestDataHandler;
import info.rmapproject.testdata.service.TestFile;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class TestUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

	private static AtomicInteger COUNTER = new AtomicInteger(0);

	/**
     * Instantiate an {@link ORMapAgent} to represent a System agent, and use the {@link RMapService} to create the
     * agent.  Verifies the agent was created using {@link RMapService#isAgentId(URI)}
     *
     * @param rmapService used to create the agent in the underlying triplestore
     * @throws RMapException
     * @throws RMapDefectiveArgumentException
     * @throws URISyntaxException
     */
    public static RMapAgent createSystemAgent(RMapService rmapService) throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
        IRI AGENT_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID);
        IRI ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID_PROVIDER);
        IRI AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_AUTH_ID);
        Literal NAME = ORAdapter.getValueFactory().createLiteral(TestConstants.SYSAGENT_NAME);
        RMapAgent sysagent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);

        RequestEventDetails requestEventDetails = new RequestEventDetails(new URI(TestConstants.SYSAGENT_ID), new URI(TestConstants.SYSAGENT_KEY));

        //create new test agent
        URI agentId = sysagent.getId().getIri();
        if (!rmapService.isAgentId(agentId)) {
            rmapService.createAgent(sysagent, requestEventDetails);
        }

        // Check the agent was created
        assertTrue(rmapService.isAgentId(agentId));

        return sysagent;
    }
    
	/**
	 * Retrieves a test DiSCO object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	public static ORMapDiSCO getRMapDiSCOObj(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapDiSCO disco = OStatementsAdapter.asDisco(stmts, () -> create("http://example.org/disco/" + COUNTER.getAndIncrement()));
		return disco;		
	}

	/**
	 * Retrieves a test Agent object
	 * @param testobj
	 * @return
	 * @throws FileNotFoundException
	 * @throws RMapException
	 * @throws RMapDefectiveArgumentException
	 */
	protected static ORMapAgent getAgentObj(TestFile testobj) throws FileNotFoundException, RMapException, RMapDefectiveArgumentException {
		InputStream stream = TestDataHandler.getTestData(testobj);
		RioRDFHandler handler = new RioRDFHandler();	
		Set<Statement>stmts = handler.convertRDFToStmtList(stream, RDFType.get(testobj.getType()), "");
		ORMapAgent agent = OStatementsAdapter.asAgent(stmts, () -> create("http://example.org/agent/" + COUNTER.getAndIncrement()));
		return agent;		
	}
	
	/**
	 * Creates and indexes a disco based on TestFile reference provided
	 * @param testFile
	 * @return
	 * @throws Exception
	 */
	public static RMapEvent createAndIndexDisco(TestFile testFile, RMapService rmapService, RMapAgent sysagent, DiscosIndexer discosIndexer, IndexDTOMapper mapper) throws Exception {
		ORMapDiSCO disco1 = getRMapDiSCOObj(testFile);
		String discoUri1 = disco1.getId().toString();
        assertNotNull(discoUri1);
		RMapEvent event = rmapService.createDiSCO(disco1, new RequestEventDetails(sysagent.getId().getIri()));
        IndexDTO indexDto = new IndexDTO(event, sysagent, null, disco1);
        discosIndexer.index(mapper.apply(indexDto));
        return event;
	}

	/**
	 * Inactivates disco and indexes event based on discoUri provided
	 * @param discoUri
	 * @return
	 * @throws Exception
	 */
	public RMapEvent inactivateAndIndexDisco(URI discoUri, RMapService rmapService, RMapAgent sysagent, DiscosIndexer discosIndexer, IndexDTOMapper mapper) throws Exception {
		RMapEvent event = rmapService.inactivateDiSCO(discoUri, new RequestEventDetails(sysagent.getId().getIri()));
		RMapDiSCO iDisco = rmapService.readDiSCO(discoUri);
        IndexDTO indexDto = new IndexDTO(event, sysagent, iDisco, iDisco);
        discosIndexer.index(mapper.apply(indexDto));
        return event;
	}

	/**
	 * Deletes disco and indexes event based on discoUri provided
	 * @param discoUri
	 * @return
	 * @throws Exception
	 */
	public RMapEvent tombstoneAndIndexDisco(URI discoUri, RMapService rmapService, RMapAgent sysagent, DiscosIndexer discosIndexer, IndexDTOMapper mapper) throws Exception {
		RMapEvent event = rmapService.tombstoneDiSCO(discoUri, new RequestEventDetails(sysagent.getId().getIri()));
		RMapDiSCO iDisco = rmapService.readDiSCO(discoUri);
        IndexDTO indexDto = new IndexDTO(event, sysagent, iDisco, iDisco);
        discosIndexer.index(mapper.apply(indexDto));
        return event;
	}


	public static InternalResourceViewResolver getViewResolver() {  
	    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
	    viewResolver.setViewClass(JstlView.class);
	    viewResolver.setPrefix("/WEB-INF/jsp/");
	    viewResolver.setSuffix(".jsp");
	    return viewResolver;
	}
	
}
