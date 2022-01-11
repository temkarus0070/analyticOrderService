package org.temkarus0070.analyticorderservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStoreIterator;
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
        final ReadOnlyWindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> readyStats = kafkaStreams.store(StoreQueryParameters.fromNameAndType("readyStats",
                QueryableStoreTypes.timestampedWindowStore()));
        WindowStoreIterator<ValueAndTimestamp<OrdersReport>> fetch = readyStats.fetch(orderStatusData, Instant.from(beginZT), Instant.from(endZT));
        fetch.forEachRemaining((val) -> {
            ordersReport.setSum(ordersReport.getSum() + val.value.value().getSum());
            ordersReport.setOrdersCount(ordersReport.getOrdersCount() + val.value.value().getOrdersCount());
            ordersReport.setRowsCount(ordersReport.getRowsCount() + val.value.value().getRowsCount());
        });

        return ordersReport;

    }
}
