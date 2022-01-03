package org.temkarus0070.analyticorderservice.kafkaStream;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

public class OrderReportSerde extends JsonSerde<OrdersReport> {
    @Override
    public Serializer<OrdersReport> serializer() {
        final JsonSerializer<OrdersReport> orderStatusDataJsonSerializer = new JsonSerializer<>();
        orderStatusDataJsonSerializer.setAddTypeInfo(false);
        return orderStatusDataJsonSerializer;


    }


    @Override
    public Deserializer<OrdersReport> deserializer() {
        final JsonDeserializer<OrdersReport> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.getTypeMapper().fromClass(OrdersReport.class,new RecordHeaders());
        jsonDeserializer.addTrustedPackages("org.temkarus0070.analyticorderservice.models");
        return jsonDeserializer;
    }
}
