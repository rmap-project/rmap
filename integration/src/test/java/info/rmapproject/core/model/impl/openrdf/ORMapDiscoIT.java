package info.rmapproject.core.model.impl.openrdf;

import org.junit.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ORMapDiscoIT {

    /**
     * Insures the superclass of {@link ORMapDiSCO} can be instantiated.  This is tested because {@link ORMapObject}
     * constructs its own Spring context (ugh).  It needs to be refactored, but for now this test exists to make sure
     * the context can be instantiated.
     */
    @Test
    public void testConstructORMapDisco() {
        new ORMapDiSCO();
    }

}
