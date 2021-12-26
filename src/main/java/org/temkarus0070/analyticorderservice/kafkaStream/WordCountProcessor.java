package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.temkarus0070.analyticorderservice.OrderStatus;
import org.temkarus0070.analyticorderservice.OrdersReport;
import org.temkarus0070.analyticorderservice.models.Good;
import org.temkarus0070.analyticorderservice.models.Order;

import java.util.HashMap;

@Component
public class WordCountProcessor {


    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        KStream<Long, Order> messageStream = streamsBuilder
                .stream("orders", Consumed.with(Serdes.Long(),new JsonSerde(Order.class)));


        KStream<OrderStatus,OrdersReport> ordersStream=messageStream
                .groupBy((key,val)-> OrderStatus.valueOf(val.getStatus().name()))

                .aggregate((Initializer<HashMap<OrderStatus, OrdersReport>>) HashMap::new,(status, order, map)->{
                    map.merge(status,new OrdersReport(1,order.getGoods().stream().map(Good::getSum).reduce(0.0, Double::sum),order.getGoods().size()),(report1, report2)->{
                        report2.setRowsCount(report1.getRowsCount()+ report2.getRowsCount());
                        report2.setOrdersCount(report1.getOrdersCount()+ report2.getOrdersCount());
                        report2.setSum(report1.getSum()+ report2.getSum());
                        return report2;
                    });
                    map.merge(OrderStatus.ANY,new OrdersReport(1,order.getGoods().stream().map(Good::getSum).reduce(0.0, Double::sum),order.getGoods().size()),(report1, report2)->{
                        report2.setRowsCount(report1.getRowsCount()+ report2.getRowsCount());
                        report2.setOrdersCount(report1.getOrdersCount()+ report2.getOrdersCount());
                        report2.setSum(report1.getSum()+ report2.getSum());
                        return report2;
                    });
                    return map;
                }).mapValues((status,map)-> map.get(status), Materialized.as("ordersStats"))
                .toStream();
                ordersStream.to("analyticsTopic");

    }
}
