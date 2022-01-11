package org.temkarus0070.analyticorderservice.tests;


import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.TimeWindowedDeserializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
@EmbeddedKafka(
        bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1)
public class KafkaTest {
    private OrdersStatProcessor ordersStatProcessor;

    @Autowired
    public void setOrdersStatProcessor(OrdersStatProcessor ordersStatProcessor) {
        this.ordersStatProcessor = ordersStatProcessor;
    }

    @Test
    void test() {
        List<GoodDTO> goodDTOS = List.of(new GoodDTO(1, "soap", 5, 2, 10), new GoodDTO(2, "coke", 10, 2, 20));
        OrderDTO orderDTO = new OrderDTO(1L, "Pupkin", goodDTOS, Status.PURCHASED);
        goodDTOS = List.of(new GoodDTO(1, "soap", 25, 2, 50), new GoodDTO(2, "coke", 100, 2, 200));
        OrderDTO orderDTO1 = new OrderDTO(2L, "Pupkin", goodDTOS, Status.CANCELLED);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        ordersStatProcessor.process(streamsBuilder);
        Topology topology = streamsBuilder.build();

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology)) {
            final TestInputTopic<Long, OrderDTO> ordersToAnalyze = topologyTestDriver.createInputTopic("ordersToAnalyze", new LongSerializer(), new JsonSerializer<>());
            ordersToAnalyze.pipeInput(1L, orderDTO, LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0).atZone(ZoneId.systemDefault()).toInstant());
            ordersToAnalyze.pipeInput(2L, orderDTO1, LocalDateTime.of(2021, Month.DECEMBER, 1, 12, 0).atZone(ZoneId.systemDefault()).toInstant());
            final TestOutputTopic<Windowed<OrderStatusData>, Object> ordersStats = topologyTestDriver.createOutputTopic("ordersStats", new TimeWindowedDeserializer<OrderStatusData>(), new JsonDeserializer<>());

            final WindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> timestampedWindowStore = topologyTestDriver.getTimestampedWindowStore("readyStats");
            WindowStoreIterator<ValueAndTimestamp<OrdersReport>> windowStoreIterator = timestampedWindowStore.fetch(new OrderStatusData(OrderStatus.PURCHASED, "Pupkin"), Instant.from(LocalDateTime.of(2019, 1, 1, 0, 0).atZone(ZoneId.systemDefault())),
                    Instant.from(LocalDateTime.of(2022, 1, 1, 1, 1, 1).atZone(ZoneId.systemDefault())));
            OrdersReport ordersReport = windowStoreIterator.next().value.value();
            Assertions.assertEquals(ordersReport, new OrdersReport(1, 30, 2));
            windowStoreIterator = timestampedWindowStore.fetch(new OrderStatusData(OrderStatus.ALL,
                            "Pupkin"), Instant.from(LocalDateTime.of(2019, 1, 1, 0, 0).atZone(ZoneId.systemDefault())),
                    Instant.from(LocalDateTime.of(2022, 1, 1, 1, 1, 1).atZone(ZoneId.systemDefault())));
            ordersReport.setSum(0);
            ordersReport.setOrdersCount(0);
            ordersReport.setRowsCount(0);
            windowStoreIterator.forEachRemaining(keyval -> {
                final OrdersReport report = keyval.value.value();
                ordersReport.setRowsCount(ordersReport.getRowsCount() + report.getRowsCount());
                ordersReport.setSum(ordersReport.getSum() + report.getSum());
                ordersReport.setOrdersCount(ordersReport.getOrdersCount() + report.getOrdersCount());
            });
            Assertions.assertEquals(ordersReport, new OrdersReport(2, 280, 4));
        }
    }

    @Test
    void timeTest() {
        List<GoodDTO> goodDTOS = List.of(new GoodDTO(1, "soap", 5, 2, 10), new GoodDTO(2, "coke", 10, 2, 20));
        OrderDTO orderDTO = new OrderDTO(1L, "Pupkin", goodDTOS, Status.PURCHASED);
        goodDTOS = List.of(new GoodDTO(1, "soap", 25, 2, 50), new GoodDTO(2, "coke", 100, 2, 200));
        OrderDTO orderDTO1 = new OrderDTO(2L, "Pupkin", goodDTOS, Status.CANCELLED);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        ordersStatProcessor.process(streamsBuilder);
        Topology topology = streamsBuilder.build();

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology)) {
            final TestInputTopic<Long, OrderDTO> ordersToAnalyze = topologyTestDriver.createInputTopic("ordersToAnalyze", new LongSerializer(), new JsonSerializer<>());
            ordersToAnalyze.pipeInput(1L, orderDTO, LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0).atZone(ZoneId.systemDefault()).toInstant());
            ordersToAnalyze.pipeInput(2L, orderDTO1, LocalDateTime.of(2021, Month.DECEMBER, 1, 12, 0).atZone(ZoneId.systemDefault()).toInstant());
            final TestOutputTopic<Windowed<OrderStatusData>, Object> ordersStats = topologyTestDriver.createOutputTopic("ordersStats", new TimeWindowedDeserializer<OrderStatusData>(), new JsonDeserializer<>());

            final WindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> timestampedWindowStore = topologyTestDriver.getTimestampedWindowStore("readyStats");
            final WindowStoreIterator<ValueAndTimestamp<OrdersReport>> windowStoreIterator = timestampedWindowStore.fetch(new OrderStatusData(OrderStatus.ALL,
                            "Pupkin"), Instant.from(LocalDateTime.of(2021, 1, 1, 0, 0).atZone(ZoneId.systemDefault())),
                    Instant.from(LocalDateTime.of(2022, 1, 1, 1, 1, 1).atZone(ZoneId.systemDefault())));
            final OrdersReport orderReport = new OrdersReport();
            windowStoreIterator.forEachRemaining(keyval -> {
                final OrdersReport report = keyval.value.value();
                orderReport.setRowsCount(orderReport.getRowsCount() + report.getRowsCount());
                orderReport.setSum(orderReport.getSum() + report.getSum());
                orderReport.setOrdersCount(orderReport.getOrdersCount() + report.getOrdersCount());
            });
            Assertions.assertEquals(orderReport, new OrdersReport(1, 250, 2));
        }
    }


}
