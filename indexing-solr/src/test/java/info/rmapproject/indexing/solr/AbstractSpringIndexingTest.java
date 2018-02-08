package info.rmapproject.indexing.solr;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.core.rdfhandler.RDFHandler;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default", "inmemory-triplestore", "inmemory-idservice", "inmemory-db", "embedded-solr", "mock-kafka"})
@ContextConfiguration({"classpath*:/rmap-indexing-solr.xml", "classpath*:/spring-rmapcore-context.xml", "classpath*:/rmap-kafka-shared-test.xml"})
public abstract class AbstractSpringIndexingTest {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSpringIndexingTest.class);

    @Autowired
    protected RDFHandler rdfHandler;

}
