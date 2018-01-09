package info.rmapproject.indexing.kafka;

import info.rmapproject.core.rdfhandler.RDFHandler;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default", "integration-db", "inmemory-idservice", "integration-triplestore", "http-solr", "prod-kafka"})
@ContextConfiguration({"classpath*:/rmap-indexing-solr.xml", "classpath*:/spring-rmapcore-context.xml", "classpath*:/rmap-kafka-shared-test.xml", "classpath*:/spring-rmapauth-context.xml"})
public abstract class BaseKafkaIT {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseKafkaIT.class);

    @Autowired
    protected RDFHandler rdfHandler;

}
