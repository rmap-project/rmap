package info.rmapproject.kafka.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class KafkaPropertyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPropertyUtils.class);

    /**
     * Represents a null property value.  If null values are present in the property maps, NPEs get thrown, for
     * example, when hash tables are merged.  Null property values are replaced by this object so that hash table
     * operations complete without throwing exceptions.  See also {@link #isNullValue(Object)},
     * {@link #mapNullValues(Map)}.
     */
    public static final Object NULL_VALUE = new Object();

    /**
     * Resolves any {@link KafkaPropertyUtils#isNullValue(Object) null} property values in the {@code Map} using the
     * supplied {@code propertyResolver} and {@code Environment}.
     *
     * @param props
     * @param propertyResolver
     * @param env
     */
    public static void resolveProperties(Map<String, Object> props, PropertySourcesPropertyResolver propertyResolver,
                                         Environment env) {
        props.entrySet().stream()
                .filter(entry -> isNullValue(entry.getValue()) || entry.getValue().toString().contains("${"))
                .forEach(entry -> {
                    String resolvedValue = null;
                    if (isNullValue(entry.getValue())) {
                        resolvedValue = env.getProperty(entry.getKey());
                        LOG.debug("Resolved kafka null property key [{}] to [{}]", entry.getKey(), resolvedValue);
                    } else {
                        resolvedValue = entry.getValue().toString();
                    }

                    if (resolvedValue != null && resolvedValue.contains("${")) {
                        String placeholder = resolvedValue;
                        resolvedValue = propertyResolver.resolvePlaceholders(resolvedValue);
                        resolvedValue = env.resolvePlaceholders(resolvedValue);
                        LOG.debug("Resolved kafka property key [{}] with placeholder [{}] to [{}]",
                                entry.getKey(), placeholder, resolvedValue);
                    }

                    props.put(entry.getKey(), resolvedValue);
                });
    }

    /**
     * Returns the property key/value pairs found in {@code sources} and returns them in a {@code Map}.  Only instances
     * of {@code EnumerablePropertySource} are considered.  If {@code sources} contains other types of
     * {@code PropertySource}, they will be ignored, and <em>not</em> included in the returned {@code Map}.
     * <p>
     * If {@code prefix} is provided, only properties that start with the supplied prefix will be included in the
     * returned {@code Map}.  If {@code strip} is {@code true} and {@code prefix} is non-null, the {@code prefix} will
     * be stripped from the property key in the returned {@code Map}.
     * </p>
     *
     * @param sources {@code MutablePropertySources} whose key/value pairs are enumerated and placed into the returned
     *                {@code Map}
     * @param prefix only include properties whose key starts with the supplied prefix, may be {@code null}
     * @param strip if {@code true}, strip the {@code prefix} off of the property key in the returned {@code Map}
     * @return a {@code Map} containing the enumerated property keys and values from {@code sources}
     */
    public static Map<String, Object> asMap(MutablePropertySources sources, String prefix, boolean strip) {
        Map<String, Object> props = new HashMap<>();

        filterEnumerablePropertySources(sources).forEach(source -> {
            if (!(source instanceof EnumerablePropertySource)) {
                return;
            }
            Stream.of(((EnumerablePropertySource)source).getPropertyNames())
                    .filter(propName -> prefix == null || propName.startsWith(prefix))
                    .peek(propName -> LOG.debug(
                            "Found kafka property prefix: [{}], property name: [{}]", prefix, propName))
                    .collect(Collectors.toMap(
                            propName -> (prefix == null || !strip || !propName.startsWith(prefix)) ?
                                    propName : propName.substring(prefix.length()),
                            source::getProperty,
                            (val1, val2) -> {
                                LOG.debug("Merging kafka property value [{}], [{}]: [{}] wins",
                                        val1, val2, val2);
                                return val2;
                            },
                            () -> props
                    ))
                    .forEach((key, value) -> LOG.debug("Kafka property: [{}]=[{}]",
                            key, (isNullValue(value)) ? "null" : value));
        });
        return props;
    }

    /**
     * Filters the supplied {@code MutablePropertySources} for instances of {@code EnumerablePropertySource}, and
     * returns a new {@code MutablePropertySource} containing <em>only</em> {@code EnumerablePropertySource} sources.
     *
     * @param sources property sources that may contain instances of {@code EnumerablePropertySource}
     * @return property sources that contain <em>only</em> {@code EnumerablePropertySource}
     */
    public static MutablePropertySources filterEnumerablePropertySources(MutablePropertySources sources) {
        MutablePropertySources enumerableSources = new MutablePropertySources();

        sources.forEach(source -> {
                    if (source instanceof EnumerablePropertySource) {
                        enumerableSources.addLast(source);
                    }
                });
        return enumerableSources;
    }

    /**
     * Renders a {@code PropertySources} as a {@code Stream<PropertySource>}
     *
     * @param sources PropertySources
     * @return a {@code Stream} of {@code PropertySource}
     */
    public static Stream<PropertySource<?>> asStream(PropertySources sources) {
        if (sources == null) {
            return Stream.empty();
        }
        Iterable<PropertySource<?>> iterable = sources::iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Replaces all {@code null} values with an {@code Object} representing the {@link #NULL_VALUE null value}.
     *
     * @param map a map, potentially containing {@code null} values
     * @return a new map, with {@code null} values replaced with {@link #NULL_VALUE}
     */
    public static Map<String, Object> mapNullValues(Map<String, Object> map) {
        Map<String, Object> copy = new HashMap<>();
        map.forEach((key, value) -> {
            if (value == null) {
                copy.put(key, NULL_VALUE);
            } else {
                copy.put(key, value);
            }
        });

        return copy;
    }

    /**
     * Returns {@code true} if the supplied {@code value} represents {@code null}, or is actually {@code null}.
     * <p>
     * If {@code value} is:
     * <ul>
     *     <li>{@code null}</li>
     *     <li>the object representing a {@link #NULL_VALUE null value}</li>
     *     <li>or a zero-length {@code CharSequence}</li>
     * </ul>
     * this method considers the value to be {@code null} and returns {@code true}
     * </p>
     *
     * @param value a property value, may be {@code null}
     * @return true if the value represents {@code null}
     */
    public static boolean isNullValue(Object value) {
        if (value == null || value == NULL_VALUE) {
            return true;
        }

        if (value instanceof CharSequence) {
            return ((CharSequence)value).length() == 0;
        }

        return false;
    }
}
