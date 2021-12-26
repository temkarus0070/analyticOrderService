package org.temkarus0070.analyticorderservice;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AnalyticsService {
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public void setStreamsBuilderFactoryBean(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    public OrdersReport ordersReport(LocalDate begin, LocalDate end, Integer clientId, OrderStatus orderStatus) {
            if(begin.compareTo(end)>0){
                throw new InvalidDatesAtRequestException();
            }
        KafkaStreams kafkaStreams=streamsBuilderFactoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<OrderStatus,OrdersReport> store=kafkaStreams.store(StoreQueryParameters.fromNameAndType("ordersStats", QueryableStoreTypes.keyValueStore()));
        return store.get(orderStatus);

    }
}
