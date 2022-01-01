package org.temkarus0070.analyticorderservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.temkarus0070.analyticorderservice.models.Order;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AnalyticsService {
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public void setStreamsBuilderFactoryBean(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public OrdersReport ordersReport(LocalDateTime begin, LocalDateTime end, String clientId, OrderStatus orderStatus) {
        streamsBuilderFactoryBean.start();
            if(begin.compareTo(end)>0){
                throw new InvalidDatesAtRequestException();
            }
        ZonedDateTime beginZT=ZonedDateTime.of(begin,ZoneId.systemDefault());
        ZonedDateTime endZT=ZonedDateTime.of(begin,ZoneId.systemDefault());
        KafkaStreams kafkaStreams=streamsBuilderFactoryBean.getKafkaStreams();

        OrderStatusData orderStatusData=new OrderStatusData(orderStatus,clientId);

        final ReadOnlyKeyValueStore<OrderStatusData,OrdersReport> ordersStats = kafkaStreams.store(StoreQueryParameters.fromNameAndType("ordersStats", QueryableStoreTypes.keyValueStore()));
        ordersStats.all().forEachRemaining(System.out::println);
        final OrdersReport report = ordersStats.get(new OrderStatusData(orderStatus, clientId));

/*        ReadOnlyKeyValueStore<OrderStatusData,OrdersReport> ordersStats = kafkaStreams.store(StoreQueryParameters.fromNameAndType("ordersStats",
                QueryableStoreTypes.keyValueStore()));
        long n = ordersStats.approximateNumEntries();
        ordersStats.all().forEachRemaining(
               e-> System.out.println(e)
        );*/
/*        ReadOnlyWindowStore<Object, ValueAndTimestamp<Object>> ordersStats1 = kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                "ordersStats",
                QueryableStoreTypes.windowStore()));





 /*       KeyValueIterator<Object, ValueAndTimestamp<Object>> iterator = ordersStats.all();
        while (iterator.hasNext()) {
            KeyValue<Object, ValueAndTimestamp<Object>> value = iterator.next();

        }*/
     //   ordersStats.fetch(orderStatusData, Instant.from(beginZT),Instant.from(endZT));
        final OrdersReport ordersReport=new OrdersReport();
/*        windowStore.fetch(orderStatusData, Instant.from(beginZT),Instant.from(endZT)).forEachRemaining((val)->{
            ordersReport.setSum(ordersReport.getSum()+val.value.getSum());
            ordersReport.setOrdersCount(ordersReport.getOrdersCount()+val.value.getOrdersCount());
            ordersReport.setRowsCount(ordersReport.getRowsCount()+val.value.getOrdersCount());
        });*/
        return ordersReport;

    }
}
