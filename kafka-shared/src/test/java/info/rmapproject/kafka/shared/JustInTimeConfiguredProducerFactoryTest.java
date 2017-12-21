package info.rmapproject.kafka.shared;

import org.apache.kafka.common.serialization.Serializer;
import org.junit.Test;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class JustInTimeConfiguredProducerFactoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionMissingFromEnvironment() throws Exception {
        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put("supplied-foo", "bar");
                put("supplied-null", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                expected, mock(Serializer.class), mock(Serializer.class));
        underTest.setEnvironment(mock(Environment.class));

        Map<String, Object> actual = underTest.getConfigurationProperties();

        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionMissingFromEnvironmentWithPrefix() throws Exception {

        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put("supplied-foo", "bar");
                put("supplied-null", null);
            }
        };

        final String prefix = "foo.bar.";

        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", null);
                put("baz", "biz");
                put("biz", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                supplied, mock(Serializer.class), mock(Serializer.class));
        underTest.setPrefix(prefix);
        underTest.setEnvironment(mock(Environment.class));

        Map<String, Object> actual = underTest.getConfigurationProperties();

        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionFromEnvironment() throws Exception {
        Environment env = mock(Environment.class);
        when(env.getProperty("supplied-null")).thenReturn("resolved-value");
        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put("supplied-foo", "bar");
                put("supplied-null", "resolved-value");
            }
        };

        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            {
                put("supplied-foo", "bar");
                put("supplied-null", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                supplied, mock(Serializer.class), mock(Serializer.class));
        underTest.setEnvironment(env);

        Map<String, Object> actual = underTest.getConfigurationProperties();

        verify(env).getProperty("supplied-null");
        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionFromEnvironmentWithPrefix() throws Exception {
        Environment env = mock(Environment.class);
        when(env.getProperty("supplied-null")).thenReturn("resolved-value");
        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put("supplied-foo", "bar");
                put("supplied-null", "resolved-value");
            }
        };

        final String prefix = "foo.bar.";

        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", null);
                put("baz", "biz");
                put("biz", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                supplied, mock(Serializer.class), mock(Serializer.class));
        underTest.setPrefix(prefix);
        underTest.setEnvironment(env);

        Map<String, Object> actual = underTest.getConfigurationProperties();

        verify(env).getProperty("supplied-null");
        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionFromEnvironmentWithPrefixNotStripped() throws Exception {
        final String prefix = "foo.bar.";
        Environment env = mock(Environment.class);
        when(env.getProperty(prefix + "supplied-null")).thenReturn("resolved-value");
        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", "resolved-value");
            }
        };

        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", null);
                put("baz", "biz");
                put("biz", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                supplied, mock(Serializer.class), mock(Serializer.class));
        underTest.setPrefix(prefix);
        underTest.setEnvironment(env);
        underTest.setStrip(false);

        Map<String, Object> actual = underTest.getConfigurationProperties();

        verify(env).getProperty(prefix + "supplied-null");
        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPropertyResolutionWithPrefixNotStripped() throws Exception {
        final String prefix = "foo.bar.";

        HashMap<String, Object> expected = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", null);
            }
        };

        HashMap<String, Object> supplied = new HashMap<String, Object>() {
            {
                put(prefix + "supplied-foo", "bar");
                put(prefix + "supplied-null", null);
                put("baz", "biz");
                put("biz", null);
            }
        };

        JustInTimeConfiguredProducerFactory underTest = new JustInTimeConfiguredProducerFactory(
                supplied, mock(Serializer.class), mock(Serializer.class));
        underTest.setPrefix(prefix);
        underTest.setEnvironment(mock(Environment.class));
        underTest.setStrip(false);

        Map<String, Object> actual = underTest.getConfigurationProperties();

        assertEquals(expected, actual);
    }
}