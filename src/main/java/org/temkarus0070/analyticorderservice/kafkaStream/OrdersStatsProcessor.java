package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import org.temkarus0070.analyticorderservice.models.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrdersStatsProcessor {
    @Autowired
    public void process(final StreamsBuilder builder) {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonDeserializer.TRUSTED_PACKAGES, OrderDTO.class.getPackageName());
        JsonSerde<OrderStatusData> orderStatusDataSerde = new OrderStatusDataSerde();
        JsonSerde<OrdersReport> ordersReportSerde = new OrderReportSerde();
        JsonSerde<OrderDTO> orderDTOJsonSerde = new JsonSerde<>();
        orderDTOJsonSerde = orderDTOJsonSerde.copyWithType(OrderDTO.class);
        orderDTOJsonSerde.configure(config, false);
        ordersReportSerde.configure(config, false);
        ordersReportSerde = ordersReportSerde.copyWithType(OrdersReport.class);
        KStream<Long, OrderDTO> messageStream = builder
                .stream("ordersToAnalyze", Consumed.with(Serdes.Long(), orderDTOJsonSerde));

        final KTable<Windowed<OrderStatusData>, OrdersReport> readyOrders = messageStream
                .flatMap((key, val) -> {
                    OrdersReport ordersReport = new OrdersReport(1, val.getGoods().stream().map(GoodDTO::getSum).reduce(0.0, Double::sum), val.getGoods().size());
                    return List.of(new KeyValue<>(new OrderStatusData(OrderStatus.ALL), ordersReport),
                            new KeyValue<>(new OrderStatusData(OrderStatus.ALL, val.getClientFIO()), ordersReport),
                            new KeyValue<>(new OrderStatusData(val.getStatus()), ordersReport),
                            new KeyValue<>(new OrderStatusData(val.getStatus(), val.getClientFIO()), ordersReport));
                })
                .groupBy((orderStatusData, ordersReport) -> orderStatusData, Grouped.with(orderStatusDataSerde, ordersReportSerde))
                .windowedBy(SlidingWindows.withTimeDifferenceAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(5)))
                .reduce((ordersReport, v1) -> {
                    ordersReport.setOrdersCount(v1.getOrdersCount() + ordersReport.getOrdersCount());
                    ordersReport.setRowsCount(v1.getRowsCount() + ordersReport.getRowsCount());
                    ordersReport.setSum(v1.getSum() + ordersReport.getSum());
                    return ordersReport;
                }, Materialized.<OrderStatusData, OrdersReport, WindowStore<Bytes, byte[]>>as("readyStats")
                        .withValueSerde(ordersReportSerde)
                        .withKeySerde(orderStatusDataSerde)
                        .withRetention(Duration.ofMinutes(5L)));

        readyOrders.toStream().to("ordersStats");
    }
}

