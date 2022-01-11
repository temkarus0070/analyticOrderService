package org.temkarus0070.analyticorderservice.tests;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.temkarus0070.analyticorderservice.AnalyticsService;
import org.temkarus0070.analyticorderservice.kafkaStream.OrdersStatProcessor;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest("spring.autoconfigure.exclude={org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.class,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration}")
@ExtendWith(SpringExtension.class)
@DirtiesContext
@EmbeddedKafka(
        bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class AnalyticsServiceTest {


    WindowStoreIterator<ValueAndTimestamp<OrdersReport>> windowStoreIterator;

    @InjectMocks
    private AnalyticsService analyticsService;
    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    @Mock
    private OrdersStatProcessor ordersStatProcessor;
    @Mock
    private KafkaStreams kafkaStreams;


    @MockBean
    private ReadOnlyWindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> windowStore;

    @BeforeEach
    void init() {

        windowStoreIterator = new WindowStoreIterator<>() {


            private boolean hasRead = false;

            @Override
            public void close() {

            }

            @Override
            public Long peekNextKey() {
                return 1L;
            }

            @Override
            public boolean hasNext() {
                return !hasRead;
            }

            @Override
            public KeyValue<Long, ValueAndTimestamp<OrdersReport>> next() {
                hasRead = true;
                return KeyValue.pair(1L, ValueAndTimestamp.make(new OrdersReport(1, 2, 2), 10000L));
            }

        };

        Mockito.when(kafkaStreams.store(any()))
                .thenReturn(windowStore);


        Mockito.when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);

    }


    @Test
    void test() {

        Mockito.when(windowStore.fetch(new OrderStatusData(OrderStatus.ALL, ""), Instant.from(LocalDateTime.of(2020, 1, 1, 1, 1).atZone(ZoneId.systemDefault())),
                        Instant.from(LocalDateTime.of(2020, 2, 2, 2, 2).atZone(ZoneId.systemDefault()))))
                .thenReturn(windowStoreIterator);


        final OrdersReport ordersReport = analyticsService.ordersReport(LocalDateTime.of(2020, 1, 1, 1, 1), LocalDateTime.of(
                2020, 2, 2, 2, 2), "", OrderStatus.ALL);

        Assertions.assertNotNull(ordersReport);
        Assertions.assertEquals(ordersReport, new OrdersReport(1, 2, 2));
    }


}