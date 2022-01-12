package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.util.HashMap;
import java.util.Map;

public class OrderReportSerde extends JsonSerde<OrdersReport> {


    @Override
    public Serializer<OrdersReport> serializer() {
        final JsonSerializer<OrdersReport> orderStatusDataJsonSerializer = new JsonSerializer<>();
        orderStatusDataJsonSerializer.setAddTypeInfo(false);
        return orderStatusDataJsonSerializer;


    }


    @Override
    public Deserializer<OrdersReport> deserializer() {
        JsonDeserializer<OrdersReport> jsonDeserializer = new JsonDeserializer<>();
        Map<String, Object> config = new HashMap<>();

        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrdersReport.class.getName());
        config.put(JsonDeserializer.TRUSTED_PACKAGES,"org.temkarus0070.analyticorderservice.models");
        jsonDeserializer.configure(config,true);
        return jsonDeserializer;
    }

}
