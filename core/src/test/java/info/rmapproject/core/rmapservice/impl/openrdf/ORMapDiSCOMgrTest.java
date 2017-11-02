/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a 
 * collaboration between Data Conservancy, Portico, and IEEE.
 *******************************************************************************/
/**
 * 
 */
package info.rmapproject.core.rmapservice.impl.openrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.rMapIri2OpenRdfIri;
import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static info.rmapproject.core.rmapservice.impl.openrdf.ORMapQueriesLineage.findLineageProgenitor;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapDeletedObjectException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.exception.RMapNotLatestVersionException;
import info.rmapproject.core.exception.RMapTombstonedObjectException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORMapDiSCO;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.testdata.service.TestConstants;
import info.rmapproject.testdata.service.TestFile;

/**
 * @author smorrissey
 * @author khanson
 *
 */
@TestPropertySource(properties = {"rmapcore.adminAgentUri = https://fakermap.rmap-hub.org#Administrator"})
public class ORMapDiSCOMgrTest extends ORMapMgrTest {
	
	@Autowired 
	ORMapDiSCOMgr discomgr;
	
	/**
	 * Test method for {@link info.rmapproject.core.rmapservice.impl.openrdf.ORMapDiSCOMgr#readDiSCO(org.openrdf.model.IRI, boolean, Map, Map, ORMapEventMgr, info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore)}.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testReadDiSCO() throws RMapException, RMapDefectiveArgumentException {

		try {		
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			RMapIri idIRI = disco.getId();
			String description = disco.getDescription().toString();
			
			reqEventDetails.setAgentKeyId(new java.net.URI("rmap:testkey"));
			
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			
			//read DiSCO back
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			RMapDiSCO rDisco = discomgr.readDiSCO(dIri, triplestore);
			RMapIri idIRI2 = rDisco.getId();
			assertEquals(idIRI.toString(),idIRI2.toString());
			String description2 = rDisco.getDescription().toString();
			assertEquals(description,description2);									
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}

	/**
	 * Test disco with no aggregates specified
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateDiscoNoAggregates() throws RMapException, RMapDefectiveArgumentException {	
		try {
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_NO_AGGREGATES);	
			reqEventDetails.setAgentKeyId(new java.net.URI("rmap:testkey"));
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);									
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage().contains("No aggregated resource statements"));
		}	
	}

	/**
	 * Test disco with no aggregates specified
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateDiscoNoBody() throws RMapException, RMapDefectiveArgumentException {		
		try {
			// create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_NO_BODY);	
			RMapIri idIRI = disco.getId();
			String description = disco.getDescription().toString();
			
			reqEventDetails.setAgentKeyId(new java.net.URI("rmap:testkey"));
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			
			//read DiSCO back
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			ORMapDiSCO rDisco = discomgr.readDiSCO(dIri, triplestore);
			RMapIri idIRI2 = rDisco.getId();
			assertEquals(idIRI.toString(),idIRI2.toString());
			String description2 = rDisco.getDescription().toString();
			assertEquals(description,description2);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}



	/**
	 * Test disco with no aggregates or additional statements specified - test makes sure that it fails because there are 
	 * no aggregates, not for any other reason
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateDiscoNoBodyOrAggregates() throws RMapException, RMapDefectiveArgumentException {	
		try {
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_NO_BODY_NO_AGGREGATES);	
			
			reqEventDetails.setAgentKeyId(new java.net.URI("rmap:testkey"));
			
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);
				
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage().contains("No aggregated resource statements"));
		}	
	}
	
	

	/**
	 * Test simplest form of DiSCO with no creator or stmt, just 2 aggregates
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateDiSCOAggregatesOnly() throws RMapException, RMapDefectiveArgumentException {

		try {
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_AGGREGATES_ONLY);	
			
			reqEventDetails.setAgentKeyId(new java.net.URI("rmap:testkey"));
			
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);
				
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage().contains("No aggregated resource statements"));
		}	
	}
	
	

	/**
	 * Test method first creates a DiSCO and then attempts to update it twice.
	 * The second update should be rejected as you can only update the latest version.
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateAndUpdateDiSCO() throws RMapException, RMapDefectiveArgumentException {

		try {
			// create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			RMapIri idIRI = disco.getId();
			
			RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			assertTrue(event!=null);
			
			//read DiSCO back
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			ORMapDiSCO rDisco = discomgr.readDiSCO(dIri,triplestore);
			RMapIri rIdIRI = rDisco.getId();
			assertEquals(idIRI.toString(),rIdIRI.toString());
			//check key is associated with event
			
			boolean correctErrorThrown = false;
			// now update DiSCO	
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri, disco2, reqEventDetails, false, triplestore);
			
			ORMapDiSCO disco3 = getRMapDiSCO(TestFile.DISCOA_XML);
			try{
				discomgr.updateDiSCO(dIri, disco3, reqEventDetails, false, triplestore);
			} catch(RMapNotLatestVersionException ex){
				if (ex.getMessage().contains("latest version")){
					correctErrorThrown=true;
				}
			}
			if (!correctErrorThrown)	{
				fail("A 'not latest version' exception should have been thrown!"); 
			}
			//now update with different agent using latest id
			try{
				IRI dIri2 = ORAdapter.rMapIri2OpenRdfIri(disco2.getId());
				discomgr.updateDiSCO(dIri2, disco3, reqEventDetails2, false, triplestore);
			} catch(Exception ex){
				ex.printStackTrace();
			}
			String description = disco3.getDescription().toString();
			//read back derived disco
			ORMapDiSCO rDisco2 = discomgr.readDiSCO(dIri, triplestore);
			RMapIri rIdIRI2 = rDisco2.getId();
			assertEquals(idIRI.toString(),rIdIRI2.toString());
			String description3 = rDisco2.getDescription().toString();
			assertEquals(description,description3);

			List<java.net.URI> agentVersions = rmapService.getDiSCOAgentVersions(rIdIRI2.getIri());
			assertTrue(agentVersions.size()==2);
						
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	/**
	 * Test method creates a DiSCO with blank nodes in the body and as aggregates, 
	 * it then updates with the same DiSCO to confirm Update works too
	 * @throws RMapDefectiveArgumentException 
	 * @throws RMapException 
	 */
	@Test
	public void testCreateAndUpdateDiSCOWithBNodes() throws RMapException, RMapDefectiveArgumentException {
		try {
			
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_WITH_BNODES);
			RMapIri idIRI = disco.getId();
			String description = disco.getDescription().toString();
			
			RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
						
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			ORMapDiSCO rDisco = discomgr.readDiSCO(dIri, triplestore);
			RMapIri idIRI2 = rDisco.getId();
			assertEquals(idIRI.toString(),idIRI2.toString());
			String description2 = rDisco.getDescription().toString();
			assertEquals(description,description2);
			RMapTriple triple = rDisco.getRelatedStatements().get(19);
			assertTrue(triple.getSubject() instanceof RMapIri);
			rmapService.tombstoneDiSCO(disco.getId().getIri(), reqEventDetails);
			assertEquals(event.getAssociatedAgent().toString(),TestConstants.SYSAGENT_ID);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}	
	}
	
	/* Creating a DiSCO should define a lineage with that disco as progenitor */
    @Test
    public void testCreateDiscoLineage() throws Exception {
        ORMapDiSCO disco = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));

        discomgr.createDiSCO(disco, reqEventDetails, triplestore);

        assertEquals(disco.getId().getIri(), findLineageProgenitor(disco.getId().getIri(), triplestore));
    }

	/* Updates (by the same agent) should all have the same lineage progenitor */
    @Test
    public void testUpdateDiscoLineage() {

        final ORMapDiSCO originalDisco = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));

        final ORMapDiSCO updatedDisco1 = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));
        
        final ORMapDiSCO updatedDisco2 = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));

        discomgr.createDiSCO(originalDisco, reqEventDetails, triplestore);

        discomgr.updateDiSCO(
                rMapIri2OpenRdfIri(originalDisco.getId()), updatedDisco1, reqEventDetails, false, triplestore);
        
        discomgr.updateDiSCO(
                rMapIri2OpenRdfIri(updatedDisco1.getId()), updatedDisco2, reqEventDetails, false, triplestore);

        assertEquals(findLineageProgenitor(originalDisco.getId().getIri(), triplestore),
                findLineageProgenitor(updatedDisco1.getId().getIri(), triplestore));
        
        assertEquals(findLineageProgenitor(updatedDisco1.getId().getIri(), triplestore),
                findLineageProgenitor(updatedDisco2.getId().getIri(), triplestore));

    }
    
    /* Derived discos (updated by different agent) should be a progenitor of a new lineage */
    @Test
    public void testDerivedDiscoLineage() {
        
        final ORMapDiSCO originalDisco = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));
        
        final ORMapDiSCO derivedDisco = new ORMapDiSCO(uri2OpenRdfIri(randomURI()), 
                new RMapIri(randomURI()), 
                Arrays.asList(randomURI()));
  
        discomgr.createDiSCO(originalDisco, reqEventDetails, triplestore);
        
        discomgr.updateDiSCO(
                rMapIri2OpenRdfIri(originalDisco.getId()), derivedDisco, reqEventDetails2, false, triplestore);
        
        assertNotEquals(findLineageProgenitor(originalDisco.getId().getIri(), triplestore),
                findLineageProgenitor(derivedDisco.getId().getIri(), triplestore));
        
        assertEquals(derivedDisco.getId().getIri(), findLineageProgenitor(derivedDisco.getId().getIri(), triplestore));
    }
	
	/**
	 * Verifies that the Agent Key ID was associated with the Event on create.
	 */
	@Test
	public void testCreateDiSCOWithUserKeyIdAssociated() {
		try {			
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_TURTLE);
			RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			assertTrue(event.getAssociatedKey().toString().equals(TestConstants.SYSAGENT_KEY)); 		
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	
	/**
	 * Verifies that a DiSCO ID provided by the DiSCO creator gets moved to ProviderId field
	 * and does not become the DiSCO ID.
	 */
	@Test
	public void testCreateDiSCOWithProviderId() throws Exception {
		String providerId = "ark:/00000/providerid";
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML_WITH_PROVIDERID);
		RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		assertTrue(event!=null);
		assertEquals(disco.getProviderId(),providerId); 		
		assertFalse(disco.getId().equals(providerId));
	}	
	
	/**
	 * Test method creates a DiSCO, updates it, updates again, updates it with a different agent, tombstones it, 
	 * then checks to see we have 3 agent versions with dates returned and that these can be ordered correctly.
	 */
	@Test
	public void testGetDiSCOAgentVersionsWithDates() {
		try {
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			RMapIri idIRI = disco.getId();
			
			@SuppressWarnings("unused")
			RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			
			//read DiSCO back
			IRI dIri = ORAdapter.rMapIri2OpenRdfIri(idIRI);
			ORMapDiSCO rDisco = discomgr.readDiSCO(dIri,triplestore);
			RMapIri rIdIRI = rDisco.getId();
			assertEquals(idIRI.toString(),rIdIRI.toString());
			
			// now update DiSCO	
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri, disco2, reqEventDetails, false, triplestore);
			IRI dIri2 = ORAdapter.rMapIri2OpenRdfIri(disco2.getId());
			
			//update again
			ORMapDiSCO disco3 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri2, disco3, reqEventDetails, false, triplestore);
			
			IRI dIri3 = ORAdapter.rMapIri2OpenRdfIri(disco3.getId());
			//update with different agent using latest id
			ORMapDiSCO disco4 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri3, disco4, reqEventDetails2, false, triplestore);

			rmapService.tombstoneDiSCO(disco.getId().getIri(), reqEventDetails);
			
			NavigableMap<Date, java.net.URI> versions = new TreeMap<Date, java.net.URI>();
			versions.putAll(rmapService.getDiSCOAgentVersionsWithDates(new java.net.URI(dIri3.toString())));
			assertTrue(versions.size()==3); //should include 2 updates and deleted, not derived.
			Entry<Date,java.net.URI> version3 = versions.lowerEntry(new Date());
			assertTrue(version3.getValue().toString().equals(dIri3.toString()));
			Entry<Date,java.net.URI> version2 = versions.lowerEntry(version3.getKey());
			assertTrue(version2.getValue().toString().equals(dIri2.toString()));
			Entry<Date,java.net.URI> version1 = versions.lowerEntry(version2.getKey());
			assertTrue(version1.getValue().toString().equals(dIri.toString()));
			versions.clear();
			
			versions.putAll(rmapService.getDiSCOAgentVersionsWithDates(new java.net.URI(dIri2.toString())));
			assertTrue(versions.size()==3); //should include 2 updates and deleted, not derived.

			version3 = versions.lowerEntry(new Date());
			assertTrue(version3.getValue().toString().equals(dIri3.toString()));
			version2 = versions.lowerEntry(version3.getKey());
			assertTrue(version2.getValue().toString().equals(dIri2.toString()));
			version1 = versions.lowerEntry(version2.getKey());
			assertTrue(version1.getValue().toString().equals(dIri.toString()));
			versions.clear();

			versions.putAll(rmapService.getDiSCOAgentVersionsWithDates(new java.net.URI(dIri.toString())));
			version3 = versions.lowerEntry(new Date());
			assertTrue(version3.getValue().toString().equals(dIri3.toString()));
			version2 = versions.lowerEntry(version3.getKey());
			assertTrue(version2.getValue().toString().equals(dIri2.toString()));
			version1 = versions.lowerEntry(version2.getKey());
			assertTrue(version1.getValue().toString().equals(dIri.toString()));
			assertTrue(versions.size()==3); //should include 2 updates and deleted, not derived.
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}
	

	/**
	 * Test method creates a DiSCO, updates it, updates it with a different agent, 
	 * then checks to see we have 3 versions from the 2 different agents on source disco, 
	 * 2 versions from perspective of derived version
	 */
	@Test
	public void testGetDiSCOAllAgentVersions() {
		try {
			// now create DiSCO	
			ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.createDiSCO(disco, reqEventDetails, triplestore);
			URI dUri = disco.getId().getIri();
			IRI dIri = ORAdapter.uri2OpenRdfIri(dUri);
			
			//read DiSCO back
			ORMapDiSCO rDisco = discomgr.readDiSCO(dIri,triplestore);
			assertEquals(rDisco.getId().toString(),dIri.toString());
			
			// now update DiSCO	
			ORMapDiSCO disco2 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri, disco2, reqEventDetails, false, triplestore);
			URI dUri2 = disco2.getId().getIri();
			IRI dIri2 = ORAdapter.uri2OpenRdfIri(dUri2);
			
			//update again with different agent to do derivation
			ORMapDiSCO disco3 = getRMapDiSCO(TestFile.DISCOA_XML);
			discomgr.updateDiSCO(dIri2, disco3, reqEventDetails2, false, triplestore);
			URI dUri3 = disco3.getId().getIri();
			
			List<URI> versionsForSource = rmapService.getDiSCOAllVersions(dUri2);
			assertTrue(versionsForSource.size()==3);
			assertTrue(versionsForSource.contains(dUri));
			assertTrue(versionsForSource.contains(dUri2));
			assertTrue(versionsForSource.contains(dUri3));
			
			List<URI> versionsForDerived = rmapService.getDiSCOAllVersions(dUri3);
			assertTrue(versionsForDerived.size()==1);
			assertTrue(versionsForDerived.contains(dUri3));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}	
	}

	/**
	 * Test method creates a DiSCO, deletes it, then checks that it is gone
	 */
	@Test
	public void testHardDeleteDiSCO() throws Exception {

		// now create DiSCO	
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);
		
		@SuppressWarnings("unused")
		RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
							
		rmapService.deleteDiSCO(disco.getId().getIri(), reqEventDetails);

		try {
			rmapService.readDiSCO(disco.getId().getIri());
		} catch (RMapDeletedObjectException ex) {
			assertTrue(ex.getMessage().contains("deleted"));			
		}
		
	}
	

	/**
	 * Test method creates a DiSCO, deletes it using and admin, then checks that it is gone
	 */
	@Test
	public void testHardDeleteDiSCOByAdmin() throws Exception {

		// now create DiSCO	
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);

		//create admin agent
		IRI adminIri = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org#Administrator");
		IRI authid = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org/authid");
		Literal name = ORAdapter.getValueFactory().createLiteral("RMap Administrator");
		ORMapAgent adminAgent = new ORMapAgent(adminIri, adminIri, authid, name);
		RequestEventDetails adminReqAgent = new RequestEventDetails(new URI(adminIri.toString()));
		rmapService.createAgent(adminAgent, adminReqAgent);
		
		//create disco using regular agent
		@SuppressWarnings("unused")
		RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		
		//delete disco using admin agent
		RMapEvent delEvent = rmapService.deleteDiSCO(disco.getId().getIri(), adminReqAgent);

		//confirm it's deleted
		try {
			rmapService.readDiSCO(disco.getId().getIri());
		} catch (RMapDeletedObjectException ex) {
			assertTrue(ex.getMessage().contains("deleted"));			
		}
		
		RMapEventDeletion rDelEvent = (RMapEventDeletion) rmapService.readEvent(delEvent.getId().getIri());
		assertTrue(rDelEvent.getDeletedObjectId().equals(disco.getId()));
	}


	/**
	 * Test method creates a DiSCO, tombstones it, then checks you can delete it after tombstone
	 */
	@Test
	public void testHardDeleteDiSCOThatIsTombstoned() throws Exception {

		// now create DiSCO	
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);

		//create admin agent
		IRI adminIri = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org#Administrator");
		IRI authid = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org/authid");
		Literal name = ORAdapter.getValueFactory().createLiteral("RMap Administrator");
		ORMapAgent adminAgent = new ORMapAgent(adminIri, adminIri, authid, name);
		RequestEventDetails adminReqAgent = new RequestEventDetails(new URI(adminIri.toString()));
		rmapService.createAgent(adminAgent, adminReqAgent);
		
		//create disco using regular agent
		@SuppressWarnings("unused")
		RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		
		RMapEvent tsEvent = rmapService.tombstoneDiSCO(disco.getId().getIri(), reqEventDetails);
		assertTrue(tsEvent!=null);
		//confirm it's tombstoned
		try {
			rmapService.readDiSCO(disco.getId().getIri());
		} catch (RMapTombstonedObjectException ex) {
			assertTrue(ex.getMessage().contains("soft deleted"));			
		}
		
		//delete disco using admin agent
		RMapEvent delEvent = rmapService.deleteDiSCO(disco.getId().getIri(), adminReqAgent);
		//confirm it's deleted
		try {
			rmapService.readDiSCO(disco.getId().getIri());
		} catch (RMapDeletedObjectException ex) {
			assertTrue(ex.getMessage().contains("deleted"));			
		}
		
		RMapEventDeletion rDelEvent = (RMapEventDeletion) rmapService.readEvent(delEvent.getId().getIri());
		assertTrue(rDelEvent.getDeletedObjectId().equals(disco.getId()));
	}


	/**
	 * Check that if you attempt to hard delete a DiSCO twice, the second time you get an object deleted message
	 */
	@Test(expected=RMapDeletedObjectException.class)
	public void testHardDeleteDiSCOThatIsDeleted() throws Exception {

		// now create DiSCO	
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);

		//create admin agent
		IRI adminIri = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org#Administrator");
		IRI authid = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org/authid");
		Literal name = ORAdapter.getValueFactory().createLiteral("RMap Administrator");
		ORMapAgent adminAgent = new ORMapAgent(adminIri, adminIri, authid, name);
		RequestEventDetails adminReqAgent = new RequestEventDetails(new URI(adminIri.toString()));
		rmapService.createAgent(adminAgent, adminReqAgent);
		
		//create disco using regular agent
		discomgr.createDiSCO(disco, reqEventDetails, triplestore);
		
		//delete disco using admin agent
		rmapService.deleteDiSCO(disco.getId().getIri(), adminReqAgent);

		//try to delete again, should throw exception
		rmapService.deleteDiSCO(disco.getId().getIri(), adminReqAgent);

	}
	
	
	/**
	 * Test method creates a DiSCO, tombstones it using and admin, then checks that it is gone
	 */
	@Test
	public void testTombstoneDiSCOByAdmin() throws Exception {

		// now create DiSCO	
		ORMapDiSCO disco = getRMapDiSCO(TestFile.DISCOA_XML);

		//create disco using regular agent
		@SuppressWarnings("unused")
		RMapEvent event = discomgr.createDiSCO(disco, reqEventDetails, triplestore);

		//create admin agent
		IRI adminIri = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org#Administrator");
		IRI authid = ORAdapter.getValueFactory().createIRI("https://fakermap.rmap-hub.org/authid");
		Literal name = ORAdapter.getValueFactory().createLiteral("RMap Administrator");
		ORMapAgent adminAgent = new ORMapAgent(adminIri, adminIri, authid, name);
		RequestEventDetails adminReqAgent = new RequestEventDetails(new URI(adminIri.toString()));
		rmapService.createAgent(adminAgent, adminReqAgent);
		
		//delete disco using admin agent
		RMapEvent tsEvent = rmapService.tombstoneDiSCO(disco.getId().getIri(), adminReqAgent);

		//confirm it's deleted
		try {
			rmapService.readDiSCO(disco.getId().getIri());
		} catch (RMapTombstonedObjectException ex) {
			assertTrue(ex.getMessage().contains("deleted"));			
		}

		RMapEventTombstone rTsEvent = (RMapEventTombstone) rmapService.readEvent(tsEvent.getId().getIri());
		assertTrue(rTsEvent.getTombstonedObjectId().equals(disco.getId()));
	}
	

	
	

}
