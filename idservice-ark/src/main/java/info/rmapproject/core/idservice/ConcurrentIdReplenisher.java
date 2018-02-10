package info.rmapproject.core.idservice;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
interface ConcurrentIdReplenisher {

    void replenish(ConcurrentMap<Integer, String> idStore);

}
