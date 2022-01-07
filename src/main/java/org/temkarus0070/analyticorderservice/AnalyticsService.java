package org.temkarus0070.analyticorderservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AnalyticsService {
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public void setStreamsBuilderFactoryBean(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public OrdersReport ordersReport(LocalDateTime begin, LocalDateTime end, String clientId, OrderStatus orderStatus) {
        streamsBuilderFactoryBean.start();
        if (begin.compareTo(end) > 0) {
            throw new InvalidDatesAtRequestException();
        }
        ZonedDateTime beginZT = ZonedDateTime.of(begin, ZoneId.systemDefault());
        ZonedDateTime endZT = ZonedDateTime.of(end, ZoneId.systemDefault());
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        final OrdersReport ordersReport = new OrdersReport();
        OrderStatusData orderStatusData = new OrderStatusData(orderStatus, clientId);
        final ReadOnlyWindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> readyStats = kafkaStreams.store(StoreQueryParameters.fromNameAndType("readyStats", QueryableStoreTypes.timestampedWindowStore()));
        //  final ReadOnlyKeyValueStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> readyStats = kafkaStreams.store(StoreQueryParameters.fromNameAndType("readyStats", QueryableStoreTypes.timestampedWindowStore().timestampedKeyValueStore()));
        readyStats.all().forEachRemaining(val -> {
            System.out.printf("%s %s %s%n", Instant.ofEpochMilli(val.value.timestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(), val.key, val.value);
        });
        if (orderStatusData.getStatus() == OrderStatus.ALL) {
            if (orderStatusData.getClientFIO().equals("")) {
                final KeyValueIterator<Windowed<OrderStatusData>, ValueAndTimestamp<OrdersReport>> windowedValueAndTimestampKeyValueIterator = readyStats.fetchAll(Instant.from(beginZT), Instant.from(endZT));
                windowedValueAndTimestampKeyValueIterator.forEachRemaining(e -> {
                    final OrdersReport report = e.value.value();
                    ordersReport.setOrdersCount(ordersReport.getOrdersCount() + report.getOrdersCount());
                    ordersReport.setSum(ordersReport.getSum() + report.getSum());
                    ordersReport.setRowsCount(ordersReport.getRowsCount() + report.getRowsCount());
                });
            } else {
                final KeyValueIterator<Windowed<OrderStatusData>, ValueAndTimestamp<OrdersReport>> windowedValueAndTimestampKeyValueIterator = readyStats.fetch(new OrderStatusData(OrderStatus.PURCHASED, clientId),
                        new OrderStatusData(OrderStatus.PENDING, clientId), Instant.from(beginZT), Instant.from(endZT));
                final WindowStoreIterator<ValueAndTimestamp<OrdersReport>> valueAndTimestampWindowStoreIterator = readyStats.fetch(new OrderStatusData(OrderStatus.CANCELLED, clientId), Instant.from(beginZT), Instant.from(endZT));

                windowedValueAndTimestampKeyValueIterator.forEachRemaining(e -> {
                    final OrdersReport report = e.value.value();
                    ordersReport.setOrdersCount(ordersReport.getOrdersCount() + report.getOrdersCount());
                    ordersReport.setSum(ordersReport.getSum() + report.getSum());
                    ordersReport.setRowsCount(ordersReport.getRowsCount() + report.getRowsCount());
                });

                valueAndTimestampWindowStoreIterator.forEachRemaining(e -> {
                    final OrdersReport report = e.value.value();
                    ordersReport.setOrdersCount(ordersReport.getOrdersCount() + report.getOrdersCount());
                    ordersReport.setSum(ordersReport.getSum() + report.getSum());
                    ordersReport.setRowsCount(ordersReport.getRowsCount() + report.getRowsCount());
                });
            }
        }
        WindowStoreIterator<ValueAndTimestamp<OrdersReport>> fetch = readyStats.fetch(orderStatusData, Instant.from(beginZT), Instant.from(endZT));
        fetch.forEachRemaining((val) -> {
            ordersReport.setSum(ordersReport.getSum() + val.value.value().getSum());
            ordersReport.setOrdersCount(ordersReport.getOrdersCount() + val.value.value().getOrdersCount());
            ordersReport.setRowsCount(ordersReport.getRowsCount() + val.value.value().getOrdersCount());
        });
        return ordersReport;

    }
}
