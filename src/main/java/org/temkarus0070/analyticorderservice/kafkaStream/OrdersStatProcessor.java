package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.temkarus0070.analyticorderservice.models.*;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;

@Component
public class OrdersStatProcessor {


    @Autowired
    public void process(final StreamsBuilder builder) {
        KStream<Long, Order> messageStream = builder
                .stream("processOrders1", Consumed.with(Serdes.Long(), new JsonSerde<>(Order.class)).withTimestampExtractor(new MyTimeStampExtractor()));

        Serde<Windowed<OrderStatusData>> windowSerde = Serdes.serdeFrom(new TimeWindowedSerializer<>(new JsonSerializer<>())
                , new TimeWindowedDeserializer<>(new JsonDeserializer<>()));

        Serde<OrdersReport> orderReportSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>());

        final KTable<OrderStatusData, Long> ordersCount = messageStream
                .groupBy((key, val) -> new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()), val.getClientFIO()))
                .count();

        final KTable<OrderStatusData, Long> goodsCountByUsers = messageStream
                .map((key, val) -> new KeyValue<OrderStatusData, Collection<Good>>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name()), val.getClientFIO()), val.getGoods()))
                .groupBy((key, val) -> key)
                .count();

        ordersCount.join(goodsCountByUsers, new ValueJoiner<Long, Long, OrdersReport>() {
            @Override
            public OrdersReport apply(Long aLong, Long aLong2) {
                return new OrdersReport(aLong,0,aLong2);
            }
        },);

        final KStream<OrderStatusData, Long> goodsCountByAllUsers = messageStream
                .map((key, val) -> new KeyValue<OrderStatusData, Collection<Good>>(new OrderStatusData(OrderStatus.valueOf(val.getStatus().name())), val.getGoods()))
                .groupBy((key, val) -> key)
                .count()
                .toStream();

        final KStream<OrderStatusData, Long> goodsCountByAllOrders = messageStream
                .map((key, val) -> new KeyValue<OrderStatusData, Collection<Good>>(new OrderStatusData(OrderStatus.ALL), val.getGoods()))
                .groupBy((key, val) -> key)
                .count()
                .toStream();

        final KStream<OrderStatusData, Long> goodsCountByAllUserOrders = messageStream
                .map((key, val) -> new KeyValue<OrderStatusData, Collection<Good>>(new OrderStatusData(OrderStatus.ALL, val.getClientFIO()), val.getGoods()))
                .groupBy((key, val) -> key)
                .count()
                .toStream();


        ;



        KStream<OrderStatusData, OrdersReport> ordersStats =
                .toStream();
        ordersStats.print(Printed.toSysOut());
        ordersStats.to("ordersStats");




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

    private KStream<OrderStatusData,Long> goodsCount(KStream<Long,Order> messageStream,OrderStatus orderStatus,String clientId){
        return messageStream
                .map((key, val) -> new KeyValue<OrderStatusData, Collection<Good>>(new OrderStatusData(orderStatus, clientId), val.getGoods()))
                .groupBy((key, val) -> key)
                .count()
                .toStream();
    }

    private KStream<OrderStatusData,Long> ordersCount(KStream<Long,Order> messageStream,OrderStatus orderStatus,String clientId){
        return messageStream
                .groupBy((key, val) -> new OrderStatusData(orderStatus, clientId))
                .count()
                .toStream();
    }


    public Serde<OrderStatusData> orderStatusDataSerde() {
        return new JsonSerde<>(OrderStatusData.class);
    }


    public Serde<HashMap<OrderStatusData, OrdersReport>> ordersReportSerde() {
        return new JsonSerde<>(HashMap.class);
    }


    private void doMapMerge(HashMap<OrderStatusData, OrdersReport> map, OrderStatusData orderStatusData, Order order) {
        map.merge(orderStatusData, new OrdersReport(1, order.getGoods().stream().map(Good::getSum).reduce(0.0, Double::sum), order.getGoods().size()), (report1, report2) -> {
            report2.setRowsCount(report1.getRowsCount() + report2.getRowsCount());
            report2.setOrdersCount(report1.getOrdersCount() + report2.getOrdersCount());
            report2.setSum(report1.getSum() + report2.getSum());
            return report2;
        });
    }
}

