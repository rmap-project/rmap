/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.rmapproject.core.idservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Allocates identifiers from a {@link ConcurrentMap cache}.  The cache is shared between this id service and a {@link
 * ConcurrentEzidReplenisher replenisher}.  Access to the cache is mediated by a shared {@link Lock}. When this id
 * service exhausts the cache, it pauses, signals the replenisher to fill the cache, and resumes when the cache has been
 * filled.
 */
public class ConcurrentCachingIdService implements IdService {

    /**
     * The log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentCachingIdService.class);

    /**
     * Length of ID to validate against.
     */
    private int idLength = -1;

    /**
     * String regex to validate an ID against.
     */
    private String idRegex = "";

    /**
     * Contains the locks and conditions used to communicate with {@link ConcurrentEzidReplenisher}.  This instance is
     * shared with a {@code ConcurrentEzidReplenisher}.
     */
    private LockHolder lockHolder;

    /**
     * The cache of identifiers that is shared with {@link ConcurrentEzidReplenisher}.
     */
    private ConcurrentMap<Integer, String> idCache;

    /**
     * Instantiates a new caching identifier service.  If the cache is empty, the first invocation of
     * {@link #createId()} will prompt the replenisher to fill the cache.
     */
    public ConcurrentCachingIdService() {

    }

    /* (non-Javadoc)
     * @see info.rmapproject.core.idservice.IdService#isValidId(java.net.URI)
     */
    @Override
    public boolean isValidId(URI id) throws Exception {
        return isValidId(id.toASCIIString());
    }

    /**
     * Check the string value of an ID is valid by checking it matches a regex and is the right length
     *
     * @param id the id
     * @return boolean
     * @throws Exception the exception
     */
    private boolean isValidId(String id) throws Exception {
        boolean isValid = true;
        if (idRegex != null && idRegex.length() > 0) {
            isValid = id.matches(idRegex);
        }
        if (isValid && idLength > 0) {
            isValid = (id.length() == idLength);
        }
        return isValid;
    }

    /* (non-Javadoc)
     * @see info.rmapproject.core.idservice.IdService#createId()
     */
    public URI createId() throws Exception {
        if (idCache == null) {
            throw new IllegalStateException("Missing an idCache (was setIdCache(ConcurrentMap) invoked?)");
        }
        try {
            return new URI(getEzid(idCache));
        } catch (Exception e) {
            throw new Exception("Failed to create a new ID: " + e.getMessage(), e);
        }
    }

    /**
     * Obtains an EZID from the cache, and removes it.  If the cache is empty, this method pause and signal the
     * replenisher to fill the cache before allocating an identifier.
     *
     * @param ezidCache the cache of identifiers
     * @return an identifier from the cache
     * @throws Exception if the cache is empty or could not be filled
     */
    private String getEzid(ConcurrentMap<Integer, String> ezidCache) throws Exception {
        if (lockHolder == null) {
            throw new IllegalStateException("Missing a LockHolder (was setLockHolder(LockHolder) invoked?)");
        }

        // Wait for ids to become available if the id store is empty
        LOG.debug("Obtaining a read-write reentrant lock over the ID store.");
        lockHolder.idStoreLock.lock();
        String id;
        try {

            while (ezidCache.size() == 0) {
                LOG.debug("ID store is empty.  Signalling replenisher thread to wake up.");
                lockHolder.idStoreEmptyCondition.signal();
                LOG.debug("Waiting for the replenisher thread to populate the ID store.");
                lockHolder.idStoreNotEmptyCondition.await(60000, TimeUnit.MILLISECONDS);
                LOG.debug("Waking up now that the replenisher has populated the ID store!");
            }

            // find the first key for which we have an EZID

            int key = -1;
            Set<Integer> keys = ezidCache.keySet();
            key = keys.stream().filter(ezidCache::containsKey).findAny()
                    .orElseThrow(() -> new RuntimeException("EZID cache is empty, unable to provide one"));

            // remove the id from the map
            LOG.debug("Obtained ID from the idStore.");
            id = ezidCache.remove(key);
        } finally {
            LOG.debug("Freeing the ID store lock.");
            lockHolder.idStoreLock.unlock();
        }

        // return the id

        LOG.debug("Returning the ID.");

        return id;
    }

    /**
     * The expected length in characters of a generated identifier.
     *
     * This class uses the expected length of the identifier to verify that it is viable.  Can be configured using the
     * {@code idservice.idLength} property.
     *
     * @return the expected length of an identifier, or an integer less than 1 if there is no expectation of a
     * consistent length
     */
    public int getIdLength() {
        return idLength;
    }

    /**
     * The expected length in characters of a generated identifier.
     *
     * This class uses the expected length of the identifier to verify that it is viable.  Can be configured using the
     * {@code idservice.idLength} property.
     *
     * @param idLength the expected length of an identifier, or an integer less than 1 if there is no expectation of a
     *                 consistent length
     */
    public void setIdLength(int idLength) {
        this.idLength = idLength;
    }

    /**
     * A regular expression used to match a generated identifier.
     *
     * This class uses the matching regex to verify that the identifier is viable.  Can be configured using the {@code
     * idservice.idRegex} property.
     *
     * @return the regex used to match the identifier, may be empty or {@code null}
     */
    public String getIdRegex() {
        return idRegex;
    }

    /**
     * A regular expression used to match a generated identifier.
     *
     * This class uses the matching regex to verify that the identifier is viable.  Can be configured using the {@code
     * idservice.idRegex} property.
     *
     * @param idRegex the regex used to match the identifier, may be empty or {@code null}
     */
    public void setIdRegex(String idRegex) {
        this.idRegex = idRegex;
    }

    /**
     * The lock and conditions used to communicate with {@link ConcurrentEzidReplenisher}.  This {@code lockHolder}
     * should be shared with a replenisher.
     *
     * @return the lock holder
     */
    public LockHolder getLockHolder() {
        return lockHolder;
    }

    /**
     * The lock and conditions used to communicate with {@link ConcurrentEzidReplenisher}.  This {@code lockHolder}
     * should be shared with a replenisher.
     *
     * @param lockHolder the lock holder, shared with a replenisher
     */
    public void setLockHolder(LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    /**
     * The cache of identifiers used by this {@code IdService} to generate ids from.  This {@code idCache} is shared
     * with a replenisher; the replenisher will be used to fill the cache when {@link LockHolder#idStoreEmptyCondition
     * signalled}.
     *
     * @return the id cache, shared with a replenisher
     */
    public ConcurrentMap<Integer, String> getIdCache() {
        return idCache;
    }

    /**
     * The cache of identifiers used by this {@code IdService} to generate ids from.  This {@code idCache} is shared
     * with a replenisher; the replenisher will be used to fill the cache when {@link LockHolder#idStoreEmptyCondition
     * signalled}.
     *
     * @param idCache the id cache, shared with a replenisher
     */
    public void setIdCache(ConcurrentMap<Integer, String> idCache) {
        this.idCache = idCache;
    }
}
