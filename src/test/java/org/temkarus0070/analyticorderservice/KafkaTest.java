package org.temkarus0070.analyticorderservice;


import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.temkarus0070.analyticorderservice.kafkaStream.OrdersStatProcessor;
import org.temkarus0070.analyticorderservice.models.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(topics = "orders",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1)
public class KafkaTest {
    private OrdersStatProcessor ordersStatProcessor;

    @Autowired
    public void setOrdersStatProcessor(OrdersStatProcessor ordersStatProcessor) {
        this.ordersStatProcessor = ordersStatProcessor;
    }

    @Test
    void test() {
        List<GoodDTO> goodDTOS = List.of(new GoodDTO(1, "soap", 5, 2, 10));
        OrderDTO orderDTO = new OrderDTO(1L, "Pupkin", goodDTOS, Status.PURCHASED);
        OrderDTO orderDTO1 = new OrderDTO(2L, "Pupkin", goodDTOS, Status.CANCELLED);
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        ordersStatProcessor.process(streamsBuilder);
        Topology topology = streamsBuilder.build();

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology)) {
            final TestInputTopic<Long, OrderDTO> ordersToAnalyze = topologyTestDriver.createInputTopic("ordersToAnalyze", new LongSerializer(), new JsonSerializer<>());
            ordersToAnalyze.pipeInput(1L, orderDTO, Instant.from(LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0).atZone(ZoneId.systemDefault()).toLocalDateTime()));
            ordersToAnalyze.pipeInput(2L, orderDTO1, Instant.from(LocalDateTime.of(2021, Month.DECEMBER, 1, 12, 0).atZone(ZoneId.systemDefault()).toLocalDateTime()));
            final WindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> timestampedWindowStore = topologyTestDriver.getTimestampedWindowStore("readyStats");
            final WindowStoreIterator<ValueAndTimestamp<OrdersReport>> pupkin = timestampedWindowStore.fetch(new OrderStatusData(OrderStatus.PURCHASED, "Pupkin"), Instant.from(LocalDateTime.of(2020, 1, 1, 0, 0, 0)),
                    Instant.from(LocalDateTime.of(2021, 1, 1, 1, 1, 1)));
            pupkin.forEachRemaining(e -> {
                System.out.println(e);
            });
        }
    }


}
