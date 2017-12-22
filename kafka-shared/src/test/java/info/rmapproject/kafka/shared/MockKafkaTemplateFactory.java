package info.rmapproject.kafka.shared;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockKafkaTemplateFactory {

    @SuppressWarnings("unchecked")
    public static <K, V> KafkaTemplate<K, V> mockTemplate()
            throws InterruptedException, ExecutionException, TimeoutException {
        KafkaTemplate<K, V> template = mock(KafkaTemplate.class);
        SettableListenableFuture future = mock(SettableListenableFuture.class);
        when(future.get(30000, TimeUnit.MILLISECONDS)).thenReturn(null);
        when(template.send(any(), any(), any())).thenReturn(future);
        return template;
    }

}
