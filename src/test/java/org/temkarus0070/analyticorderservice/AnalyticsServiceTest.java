package org.temkarus0070.analyticorderservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.LocalDateTime;

@SpringBootTest("spring.autoconfigure.exclude={org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.class,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration}")
@ExtendWith(SpringExtension.class)
public class AnalyticsServiceTest {


    @SpyBean
    private AnalyticsService analyticsService;

    @MockBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @MockBean
    private KafkaStreams kafkaStreams;


    @Test
    void test() {
        Mockito.when(kafkaStreams.store(StoreQueryParameters.fromNameAndType("readyStats", QueryableStoreTypes.timestampedWindowStore())))
                .thenReturn(new TimestampedWindowStore<>() {

                    @Override
                    public String name() {
                        return null;
                    }

                    @Override
                    public void init(ProcessorContext processorContext, StateStore stateStore) {

                    }

                    @Override
                    public void flush() {

                    }

                    @Override
                    public void close() {

                    }

                    @Override
                    public boolean persistent() {
                        return false;
                    }

                    @Override
                    public boolean isOpen() {
                        return false;
                    }

                    @Override
                    public void put(Object o, ValueAndTimestamp<Object> objectValueAndTimestamp) {

                    }

                    @Override
                    public void put(Object o, ValueAndTimestamp<Object> objectValueAndTimestamp, long l) {

                    }

                    @Override
                    public ValueAndTimestamp<Object> fetch(Object o, long l) {
                        return null;
                    }

                    @Override
                    public WindowStoreIterator<ValueAndTimestamp<Object>> fetch(Object o, long l, long l1) {
                        return null;
                    }

                    @Override
                    public KeyValueIterator<Windowed<Object>, ValueAndTimestamp<Object>> fetch(Object o, Object k1, long l, long l1) {
                        return null;
                    }

                    @Override
                    public KeyValueIterator<Windowed<Object>, ValueAndTimestamp<Object>> all() {
                        return null;
                    }

                    @Override
                    public KeyValueIterator<Windowed<Object>, ValueAndTimestamp<Object>> fetchAll(long l, long l1) {
                        return null;
                    }
                });
        Mockito.when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);

        analyticsService.setStreamsBuilderFactoryBean(streamsBuilderFactoryBean);
        final OrdersReport ordersReport = analyticsService.ordersReport(LocalDateTime.of(2020, 1, 1, 1, 1), LocalDateTime.of(
                2020, 2, 2, 2, 2), "", OrderStatus.ALL);
        Assertions.assertNotNull(ordersReport);
    }


}
