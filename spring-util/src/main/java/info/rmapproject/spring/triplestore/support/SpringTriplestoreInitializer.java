package info.rmapproject.spring.triplestore.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * A Spring-configured {@link TriplestoreInitializer}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SpringTriplestoreInitializer implements TriplestoreInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SpringTriplestoreInitializer.class);

    private TriplestoreManager triplestoreManager;

    private boolean initializeEnabled;

    private boolean destroyEnabled;

    @Override
    public void initializeTriplestore() {
        assertState();
        if (initializeEnabled) {
            URL u = triplestoreManager.createTriplestore();
            LOG.debug("Created triplestore at {}", u);
        } else {
            LOG.debug("Skipping triplestore initialization: [{}] = [{}]", "initializeEnabled", false);
        }
    }

    @Override
    public void destroyTriplestore() {
        assertState();
        if (destroyEnabled) {
            URL u = triplestoreManager.removeTriplestore();
            LOG.debug("Removed triplestore at {}", u);
        } else {
            LOG.debug("Skipping triplestore destruction: [{}] = [{}]", "destroyEnabled", false);
        }
    }

    @Override
    public void clearTriplestore() {
        assertState();
        URL u = triplestoreManager.removeTriplestore();
        LOG.debug("Cleared triplestore at {}", u);
    }

    private void assertState() {
        if (triplestoreManager == null) {
            throw new IllegalStateException("Triplestore manager must be set!");
        }
    }

    public TriplestoreManager getTriplestoreManager() {
        return triplestoreManager;
    }

    public void setTriplestoreManager(TriplestoreManager triplestoreManager) {
        if (triplestoreManager == null) {
            throw new IllegalArgumentException("Triplestore manager must not be null!");
        }
        this.triplestoreManager = triplestoreManager;
    }

    public boolean isInitializeEnabled() {
        return initializeEnabled;
    }

    public void setInitializeEnabled(boolean initializeEnabled) {
        this.initializeEnabled = initializeEnabled;
    }

    public boolean isDestroyEnabled() {
        return destroyEnabled;
    }

    public void setDestroyEnabled(boolean destroyEnabled) {
        this.destroyEnabled = destroyEnabled;
    }

}
