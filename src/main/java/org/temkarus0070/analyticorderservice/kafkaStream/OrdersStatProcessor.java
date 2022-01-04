package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import org.temkarus0070.analyticorderservice.models.*;

import java.time.Duration;
import java.util.List;

import static org.apache.kafka.streams.kstream.Materialized.as;

@Component
public class OrdersStatProcessor {


    @Autowired
    public void process(final StreamsBuilder builder) {
        JsonSerde<OrderStatusData> orderStatusDataSerde=new OrderStatusDataSerde();
        JsonSerde<OrdersReport> ordersReportSerde=new OrderReportSerde();
        JsonSerde<OrderDTO> orderDTOJsonSerde=new JsonSerde<>();
        orderDTOJsonSerde=orderDTOJsonSerde.copyWithType(OrderDTO.class);
        ordersReportSerde=ordersReportSerde.copyWithType(OrdersReport.class);
        KStream<Long, OrderDTO> messageStream = builder
                .stream("ordersToAnalyze",Consumed.with(Serdes.Long(),orderDTOJsonSerde));






/*       final KTable<OrderStatusData, OrdersReport> ordersStats = messageStream
                .flatMap((key,val)-> {
                    OrdersReport ordersReport = new OrdersReport(1, val.getGoods().stream().map(GoodDTO::getSum).reduce(0.0, Double::sum), val.getGoods().size());
                    return List.of(new KeyValue<>(new OrderStatusData(OrderStatus.ALL), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.ALL, val.getClientFIO()), ordersReport),
                            new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name())), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()), val.getClientFIO()), ordersReport));
                })
                .groupBy((orderStatusData, ordersReport) -> orderStatusData
    ,Grouped.with(orderStatusDataSerde,ordersReportSerde))
                .aggregate(OrdersReport::new,(status, report, report1)->{
                    report.setRowsCount(report1.getRowsCount()+report.getRowsCount());
                    report.setOrdersCount(report1.getOrdersCount()+report.getOrdersCount());
                    report.setSum(report1.getSum()+report.getSum());
                    return report;
                },Materialized.with(orderStatusDataSerde,ordersReportSerde));
                ordersStats.toStream().to("ordersStats");*/

        final KTable<Windowed<OrderStatusData>, OrdersReport> readyOrders = messageStream
                .flatMap((key, val) -> {
                    OrdersReport ordersReport = new OrdersReport(1, val.getGoods().stream().map(GoodDTO::getSum).reduce(0.0, Double::sum), val.getGoods().size());
                    return List.of(new KeyValue<>(new OrderStatusData(OrderStatus.ALL), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.ALL, val.getClientFIO()), ordersReport),
                            new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name())), ordersReport), new KeyValue<>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()), val.getClientFIO()), ordersReport));
                })
                .groupBy((orderStatusData, ordersReport) -> orderStatusData, Grouped.with(orderStatusDataSerde, ordersReportSerde))
                .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(30)))
                .aggregate(OrdersReport::new, (status, report, report1) -> {
                    report.setRowsCount(report1.getRowsCount() + report.getRowsCount());
                    report.setOrdersCount(report1.getOrdersCount() + report.getOrdersCount());
                    report.setSum(report1.getSum() + report.getSum());
                    return report;

                }, Materialized.with(orderStatusDataSerde, ordersReportSerde))
                .mapValues((key, val) -> val, as("readyStats"));

        readyOrders.toStream().to("ordersStats");





      /*  TimeWindowedKStream<OrderStatusData,Order> ordersStream=messageStream
                .groupBy((key,val)-> new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()),val.getClientFIO()))
               .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(30)))

                ;


        TimeWindowedKStream<Windowed<OrderStatusData>, OrdersReport> ordersStats = ordersStream.aggregate((Initializer<HashMap<OrderStatusData, OrdersReport>>) HashMap::new, (status, order, map) -> {
                    doMapMerge(map, status, order);

                    OrderStatusData orderStatusData = new OrderStatusData(OrderStatus.ALL, order.getClientFIO());

                    doMapMerge(map, orderStatusData, order);

                    OrderStatusData orderStatusData1 = new OrderStatusData(OrderStatus.ALL, null);
                    doMapMerge(map, orderStatusData1, order);

                    OrderStatusData orderStatusData2 = new OrderStatusData(status.getStatus(), null);
                    doMapMerge(map, orderStatusData2, order);
                    return map;
                }, Materialized.with(keySerde, valueSerde)).mapValues((status, map) -> map.get(status), Materialized.with(windowSerde, orderReportSerde))
                .mapValues((key, val) -> val, Materialized.as("ordersStats"))
                .toStream()
                .groupByKey()
                .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(1), Duration.ofSeconds(1)))
                ;*/


    }


}

