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
public class OrdersStatProcessor {


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
                    return List.of(new KeyValue<>(new OrderStatusData(OrderStatus.ALL), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.ALL, val.getClientFIO()), ordersReport),
                            new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name())), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()), val.getClientFIO()), ordersReport));
                })
                .groupBy((orderStatusData, ordersReport) -> orderStatusData, Grouped.with(orderStatusDataSerde, ordersReportSerde))
                .windowedBy(SlidingWindows.withTimeDifferenceAndGrace(Duration.ofMinutes(1), Duration.ZERO))

                .aggregate(OrdersReport::new, (status, report, report1) -> {
                    report.setRowsCount(report1.getRowsCount() + report.getRowsCount());
                    report.setOrdersCount(report1.getOrdersCount() + report.getOrdersCount());
                    report.setSum(report1.getSum() + report.getSum());
                    return report;

                }, Materialized.<OrderStatusData, OrdersReport, WindowStore<Bytes, byte[]>>as("readyStats").withValueSerde(ordersReportSerde).withKeySerde(orderStatusDataSerde).withRetention(Duration.ofMinutes(5L)));

        readyOrders.toStream().to("ordersStats");

    }


}

