package info.rmapproject.kafka.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static info.rmapproject.kafka.shared.KafkaPropertyUtils.asMap;
import static info.rmapproject.kafka.shared.KafkaPropertyUtils.resolveProperties;

class FactoryState {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryState.class);

    /**
     * Property keys with the supplied prefix will be used to configure the Kafka producer.  For example, if the
     * prefix is {@code rmapcore.producer.}, then any property key enumerated from {@link #sources} that begins with
     * {@code rmapcore.producer.} will be added to the {@link #getConfigurationProperties() producer configuration
     * properties}.  If the prefix is {@code null} (the default), <em>all</em> property keys from all property sources
     * will be considered a producer configuration property.
     */
    String prefix;

    /**
     * If a {@link #prefix prefix} is specified, this flag determines whether or not the prefix is stripped from the
     * {@link #getConfigurationProperties() configuration property keys}.
     * <p>
     * Given a {@code prefix} of {@code rmapcore.producer.}, any property key enumerated from {@link #sources} that
     * begins with {@code rmapcore.producer.} will be added to the {@link #getConfigurationProperties() producer
     * configuration properties}.  For example, if the property key {@code rmapcore.producer.bootstrap.servers} is
     * present in {@link #sources the property sources}, and {@code strip} is {@code true}, the property key will be
     * added to the producer configuration properties as {@code bootstrap.servers} (the prefix is stripped off).  If
     * {@code strip} is {@code false}, the property key will be added to the producer configuration properties as
     * {@code rmapcore.producer.bootstrap.servers} (the prefix is preserved).
     * </p>
     */
    boolean strip = true;

    /**
     * A mutable listing of {@link EnumerablePropertySource}s and subclasses thereof.  Property sources of any other
     * type are not considered by the {@code JustInTimeConfiguredProducerFactory}.
     */
    MutablePropertySources sources = new MutablePropertySources();

    /**
     * The environment used to resolve {@code null} property values when {@link #getConfigurationProperties()} is called
     * (i.e. "just in time")
     */
    Environment env;


    Map<String, Object> getConfigurationProperties() {
        Map<String, Object> props = asMap(sources, prefix, strip);
        PropertySourcesPropertyResolver propertyResolver = new PropertySourcesPropertyResolver(sources);
        resolveProperties(props, propertyResolver, env);

        return props;
    }

    void setProperties(List<Map<String, Object>> props) {
        AtomicInteger count = new AtomicInteger();
        props.forEach(map -> {
            String name = this.getClass().getSimpleName() + "-source" + count.getAndIncrement();
            MapPropertySource ps = new MapPropertySource(name, map);
            addSource(ps);
        });
    }

    void setSources(PropertySources sources) {
        sources.forEach(source -> this.sources.addLast(source));
    }

    void addSource(PropertySource source) {
        this.sources.addLast(source);
    }

    void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    void setStrip(boolean strip) {
        this.strip = strip;
    }

    void setEnvironment(Environment environment) {
        this.env = environment;
    }

}
