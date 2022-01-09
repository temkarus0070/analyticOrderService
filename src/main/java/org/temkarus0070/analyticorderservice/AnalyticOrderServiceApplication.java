package org.temkarus0070.analyticorderservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
@EnableKafka
@OpenAPIDefinition(info = @Info(description = "${project.description}", title = "${project.title}"))
public class AnalyticOrderServiceApplication {

    @Bean
    NewTopic orderStats(){
        return new NewTopic("ordersStats",1,(short)1);
    }

    public static void main(String[] args) {
        SpringApplication.run(AnalyticOrderServiceApplication.class, args);
    }


}
