package info.rmapproject.core.idservice;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contains a {@link Lock} and the {@link Condition}s used to manage access to a {@link ConcurrentMap} of identifiers by
 * {@link ConcurrentCachingIdService} and {@link ConcurrentEzidReplenisher}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class LockHolder {

    /**
     * Mediates access to a map of identifiers between an instance of {@link ConcurrentCachingIdService} and
     * {@link ConcurrentEzidReplenisher}.  The id service and the replenisher <em>must</em> share the same instance of
     * this lock in order for threaded communication to be successful.
     */
    public Lock idStoreLock = new ReentrantLock();

    /**
     * Used by the {@link ConcurrentCachingIdService} to signal the {@link ConcurrentEzidReplenisher} that the identifier map
     * is empty, and needs to be replenished.
     */
    public Condition idStoreEmptyCondition = idStoreLock.newCondition();

    /**
     * Used by the {@link ConcurrentEzidReplenisher} to signal the {@link ConcurrentCachingIdService} that the identifier map
     * has been replenished.
     */
    public Condition idStoreNotEmptyCondition = idStoreLock.newCondition();

    @Override
    public String toString() {
        return "LockHolder{" +
                "idStoreLock=" + idStoreLock +
                ", idStoreEmptyCondition=" + idStoreEmptyCondition +
                ", idStoreNotEmptyCondition=" + idStoreNotEmptyCondition +
                '}';
    }
}