package org.temkarus0070.analyticorderservice.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.temkarus0070.analyticorderservice.AnalyticsService;
import org.temkarus0070.analyticorderservice.OrderAnalyticController;
import org.temkarus0070.analyticorderservice.kafkaStream.OrdersStatProcessor;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest("spring.autoconfigure.exclude={org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.class,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration}")
@ExtendWith(SpringExtension.class)
@DirtiesContext
@EmbeddedKafka(
        bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@AutoConfigureMockMvc
public class ControllerTest {
    WindowStoreIterator<ValueAndTimestamp<OrdersReport>> windowStoreIterator;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @InjectMocks
    private AnalyticsService analyticsService;
    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    @Mock
    private OrdersStatProcessor ordersStatProcessor;
    @Mock
    private KafkaStreams kafkaStreams;
    @MockBean
    private ReadOnlyWindowStore<OrderStatusData, ValueAndTimestamp<OrdersReport>> windowStore;


    private OrderAnalyticController orderAnalyticController;

    @BeforeEach
    void init() {

        windowStoreIterator = new WindowStoreIterator<>() {


            private boolean hasRead = false;

            @Override
            public void close() {

            }

            @Override
            public Long peekNextKey() {
                return 1L;
            }

            @Override
            public boolean hasNext() {
                return !hasRead;
            }

            @Override
            public KeyValue<Long, ValueAndTimestamp<OrdersReport>> next() {
                hasRead = true;
                return KeyValue.pair(1L, ValueAndTimestamp.make(new OrdersReport(1, 2, 2), 10000L));
            }

        };

        Mockito.when(kafkaStreams.store(any()))
                .thenReturn(windowStore);


        Mockito.when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);


        orderAnalyticController = new OrderAnalyticController();
        orderAnalyticController.setAnalyticsService(analyticsService);

        mockMvc = MockMvcBuilders.standaloneSetup(orderAnalyticController).build();

        Mockito.when(windowStore.fetch(new OrderStatusData(OrderStatus.ALL, ""),
                        Instant.from(LocalDateTime.of(2020, 1, 1, 0, 0).atZone(ZoneId.systemDefault())),
                        Instant.from(LocalDateTime.of(2020, 2, 2, 0, 0).atZone(ZoneId.systemDefault()))))
                .thenReturn(windowStoreIterator);

    }


    @Test
    void test() throws Exception {


        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("periodBegin", "2020-01-01T00:00:00");
        multiValueMap.add("periodEnd", "2020-02-02T00:00:00");
        multiValueMap.add("orderStatus", "ALL");
        final String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/analytics")
                        .params(multiValueMap)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse().getContentAsString();

        final OrdersReport order1 = objectMapper.readValue(contentAsString, OrdersReport.class);
        Assertions.assertNotNull(order1);
        Assertions.assertEquals(order1, new OrdersReport(1, 2, 2));

    }


    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }


}
