package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

public class OrderStatusDataSerde extends JsonSerde<OrderStatusData> {
    @Override
    public Serializer<OrderStatusData> serializer() {
        final JsonSerializer<OrderStatusData> orderStatusDataJsonSerializer = new JsonSerializer<>();
        orderStatusDataJsonSerializer.setAddTypeInfo(false);
        return orderStatusDataJsonSerializer;


    }


    @Override
    public Deserializer<OrderStatusData> deserializer() {
         JsonDeserializer<OrderStatusData> jsonDeserializer = new JsonDeserializer<>();

        jsonDeserializer.addTrustedPackages("org.temkarus0070.analyticorderservice.models");
        return jsonDeserializer;
    }

}
