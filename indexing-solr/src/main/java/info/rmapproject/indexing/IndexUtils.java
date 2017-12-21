package info.rmapproject.indexing;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.event.RMapEventUpdateWithReplace;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.IRI;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.openRdfIri2RMapIri;
import static info.rmapproject.core.model.impl.openrdf.ORAdapter.uri2OpenRdfIri;
import static java.net.URI.create;

/**
 * Provides common utility methods.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class IndexUtils {
	
	public static final String HL_PREFIX = "##$";
	public static final String HL_POSTFIX = "$##";
	
    public static boolean notNull(Object o) {
        return o != null;
    }

    public static boolean notEmpty(Collection c) {
        return !c.isEmpty();
    }

    public static boolean notEmpty(String s) {
        return s.trim().length() != 0;
    }

    /**
     * Abstracts the "direction" of an {@link RMapEvent}.
     * <p>
     * Events can be interpreted as having a <em>source</em> and <em>target</em>.  For example, an
     * {@link RMapEventUpdate update event} has the DiSCO <em>to be updated</em> as the source of the event, and the
     * <em>updated</em> DiSCO as the target of the event.
     * </p>
     * <p>
     * The method used to access the source or target of an event depends on the type of event.  A {@code
     * RMapEventUpdate} uses {@link RMapEventUpdate#getInactivatedObjectId()} to refer to the {@code SOURCE}, while a
     * {@link RMapEventTombstone tombstone} event uses {@link RMapEventTombstone#getTombstonedObjectId()} to refer to
     * the {@code SOURCE}.  {@code EventDirection} provides an abstraction, allowing the source or target of an event to
     * be indicated, independent of the concrete event type.
     * </p>
     */
    public enum EventDirection {

        /**
         * Abstract name to refer to the source of an event, without having to reason about the concrete event type.
         */
        SOURCE,

        /**
         * Abstract name to refer to the target of an event, without having to reason about the concrete event type.
         */
        TARGET,

        /**
         * An abstract way to indicate that the directionality of the event is unimportant.
         */
        EITHER
    }

    /**
     * Asserts the supplied string is a valid URI according to {@link URI#create(String)}.  {@code null} URIs are OK,
     * which make the calling code a little easier to write, and cleaner looking.
     *
     * @param uri a string that claims to be a URI
     * @throws IllegalArgumentException if {@code uri} is not a valid {@code URI}
     */
    public static void assertValidUri(String uri) {
        if (uri != null) {
            //noinspection ResultOfMethodCallIgnored
            URI.create(uri);
        }
    }

    /**
     * Asserts that each string in the supplied collection is a valid URI according to {@link URI#create(String)}.
     *
     * @param uris a collection of strings, each of which claims to be a URI
     * @throws IllegalArgumentException if {@code uri} is {@code null} or empty, or is not a valid {@code URI}
     */
    public static void assertValidUri(Collection<String> uris) {
        if (uris == null) {
            throw new IllegalArgumentException("Supplied collection of URIs must not be null.");
        }
        if (uris.isEmpty()) {
            throw new IllegalArgumentException("Supplied collection of URIs must not be empty.");
        }
        uris.forEach(IndexUtils::assertValidUri);
    }

    public static String assertNotNullOrEmpty(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Supplied string must not be null.");
        }

        if (s.trim().length() == 0) {
            throw new IllegalArgumentException("Supplied string must not be empty.");
        }

        return s;
    }

    public static String assertNotNullOrEmpty(String s, String message) {
        if (s == null) {
            throw new IllegalArgumentException(message);
        }

        if (s.trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }

        return s;
    }

    public static String assertNotNullOrEmpty(String s, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (s == null) {
            throw exceptionSupplier.get();
        }

        if (s.trim().length() == 0) {
            throw exceptionSupplier.get();
        }

        return s;
    }


    public static <T> List<T> assertNotNullOrEmpty(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("Supplied List must not be null.");
        }

        if (list.isEmpty()) {
            throw new IllegalArgumentException("Supplied List must not be empty.");
        }

        return list;
    }

    public static <K, V> Map<K, V> assertNotNullOrEmpty(Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Supplied Map must not be null.");
        }

        if (map.isEmpty()) {
            throw new IllegalArgumentException("Supplied Map must not be empty.");
        }

        return map;
    }

    public static <T> Set<T> assertNotNullOrEmpty(Set<T> set) {
        if (set == null) {
            throw new IllegalArgumentException("Supplied Set must not be null.");
        }

        if (set.isEmpty()) {
            throw new IllegalArgumentException("Supplied Set must not be empty.");
        }

        return set;
    }

    public static <T> T assertNotNull(T o) {
        if (o == null) {
            throw new IllegalArgumentException("Supplied object must not be null.");
        }

        return o;
    }

    public static <T> T assertNotNull(T o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }

        return o;
    }

    public static <T> T assertNotNull(T o, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (o == null) {
            throw exceptionSupplier.get();
        }

        return o;
    }

    public static void assertNull(Object o) {
        assertNull(o, iae("Supplied object must not be null"));
    }

    public static void assertNull(Object o, String message) {
        assertNull(o, iae(message));
    }

    public static void assertNull(Object o, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (o != null) {
            throw exceptionSupplier.get();
        }
    }

    public static String dateToString(Date d) {
        return ISODateTimeFormat.dateTime().withZoneUTC().print(new DateTime(d));
    }

    public static boolean irisEqual(RMapIri one, RMapIri two) {
        if (one != null && two != null) {
            return one.getStringValue().equals(two.getStringValue());
        } else if (one == null && two == null) {
            return true;
        }

        return false;
    }

    public static boolean irisEqual(Optional<RMapIri> optional, RMapIri two) {
        return irisEqual(optional.get(), two);
    }

    public static boolean irisEqual(Optional<RMapIri> optionalOne, Optional<RMapIri> optionalTwo) {
        return irisEqual(optionalOne.get(), optionalTwo.get());
    }

    public static IRI asIri(String uri) {
        return uri2OpenRdfIri(create(uri));
    }

    public static RMapIri asRmapIri(String uri) {
        return openRdfIri2RMapIri(asIri(uri));
    }

    /**
     * Retrieves the source or target of the supplied event.
     * <p>
     * Most RMap events have a source and a target.  For example, an update event will have the disco <em>being
     * updated</em> as the source and the <em>updated disco</em> as the target.
     * </p>
     *
     * @param event the event to examine
     * @param direction the direction of the event: source or target
     * @return an {@code Optional} with the IRI of the referenced disco
     * @throws IllegalArgumentException if an unknown {@code RMapEvent} is encountered, or an unsupported
     *                                  {@code EventDirection} is supplied
     * @throws NullPointerException if the source or target IRI is {@code null}
     */
    public static Optional<RMapIri> findEventIri(RMapEvent event, EventDirection direction) {
        Optional<RMapIri> iri = Optional.empty();

        if (direction != EventDirection.SOURCE && direction != EventDirection.TARGET) {
            throw new IllegalArgumentException("Direction must either be SOURCE or TARGET, was " + direction.name());
        }

        if (direction == EventDirection.TARGET) {
            switch (event.getEventType()) {
                case CREATION:
                    // TODO: handle multiple creation ids
                    iri = Optional.of(assertNotNullOrEmpty(((RMapEventCreation) event).getCreatedObjectIds()).get(0));
                    break;

                case DERIVATION:
                    iri = Optional.of(((RMapEventDerivation) event).getDerivedObjectId());
                    break;

                case UPDATE:
                    iri = Optional.of(((RMapEventUpdate) event).getDerivedObjectId());
                    break;

                case DELETION:
                    // no-op: a DELETION event has no target
                    iri = Optional.empty();
                    break;

                case TOMBSTONE:
                    // no-op: a TOMBSTONE event has no target
                    iri = Optional.empty();
                    break;

                case INACTIVATION:
                    // no-op: an INACTIVATION event has no target
                    iri = Optional.empty();
                    break;

                case REPLACE:
                    // TODO: missing the source object of a replacement?
                    iri = Optional.empty();
                    break;

                default:
                    throw new IllegalArgumentException("Unknown RMap event type: " + event);

            }
        }

        if (direction == EventDirection.SOURCE) {
            switch (event.getEventType()) {
                case CREATION:
                    // TODO: handle multiple creation ids
                    iri = Optional.empty();
                    break;

                case DERIVATION:
                    iri = Optional.of(((RMapEventDerivation) event).getSourceObjectId());
                    break;

                case UPDATE:
                    iri = Optional.of(((RMapEventUpdate)event).getInactivatedObjectId());
                    break;

                case DELETION:
                    iri = Optional.of((((RMapEventDeletion)event).getDeletedObjectId()));
                    break;

                case TOMBSTONE:
                    iri = Optional.of(((RMapEventTombstone) event).getTombstonedObjectId());
                    break;

                case INACTIVATION:
                    iri = Optional.of(((RMapEventInactivation) event).getInactivatedObjectId());
                    break;

                case REPLACE:
                    iri = Optional.of(((RMapEventUpdateWithReplace) event).getUpdatedObjectId());
                    break;

                default:
                    throw new IllegalArgumentException("Unknown RMap event type: " + event.getEventType());

            }
        }

        return iri;
    }

    /**
     * Supplies an {@link IllegalArgumentException} with the supplied message.
     *
     * @param message error message
     * @return the supplier of the exception
     */
    public static Supplier<IllegalArgumentException> iae(String message) {
        assertNotNullOrEmpty(message, "Exception message must not be null or empty.");
        return () -> new IllegalArgumentException(message);
    }

    /**
     * Supplies an {@link IllegalArgumentException} with the supplied message and cause.
     *
     * @param message error message
     * @param cause will be chained to the {@code IllegalArgumentException} supplied by the returned {@code Supplier}
     * @return the supplier of the exception
     */
    public static Supplier<IllegalArgumentException> iae(String message, Throwable cause) {
        assertNotNullOrEmpty(message, "Exception message must not be null or empty.");
        assertNotNull(cause, "Exception cause must not be null.");
        return () -> new IllegalArgumentException(message, cause);
    }

    /**
     * Supplies an {@link IllegalStateException} with the supplied message.
     *
     * @param message error message
     * @return the supplier of the exception
     */
    public static Supplier<IllegalStateException> ise(String message) {
        assertNotNullOrEmpty(message, "Exception message must not be null or empty.");
        return () -> new IllegalStateException(message);
    }

    /**
     * Supplies an {@link IllegalStateException} with the supplied message and cause.
     *
     * @param message error message
     * @param cause will be chained to the {@code IllegalArgumentException} supplied by the returned {@code Supplier}
     * @return the supplier of the exception
     */
    public static Supplier<IllegalStateException> ise(String message, Throwable cause) {
        assertNotNullOrEmpty(message, "Exception message must not be null or empty.");
        assertNotNull(cause, "Exception cause must not be null.");
        return () -> new IllegalStateException(message, cause);
    }

    public static int assertPositive(int candidate) {
        return assertPositive(candidate, iae("Argument must be a positive integer."));
    }

    public static int assertPositive(int candidate, Supplier<? extends RuntimeException> toThrow) {
        if (candidate < 1) {
            throw toThrow.get();
        }
        return candidate;
    }

    public static float assertPositive(float candidate) {
        return assertPositive(candidate, iae("Argument must be a positive float."));
    }

    public static float assertPositive(float candidate, Supplier<? extends RuntimeException> toThrow) {
        if (candidate < 1) {
            throw toThrow.get();
        }
        return candidate;
    }

    public static long assertZeroOrPositive(long candidate) {
        return assertZeroOrPositive(candidate, iae("Argument must be greater than -1."));
    }

    public static long assertZeroOrPositive(long candidate, Supplier<? extends RuntimeException> toThrow) {
        if (candidate < 0) {
            throw toThrow.get();
        }
        return candidate;
    }
}
