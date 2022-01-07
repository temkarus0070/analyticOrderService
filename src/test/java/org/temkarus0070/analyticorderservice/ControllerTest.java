package org.temkarus0070.analyticorderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;


    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void test() throws Exception {

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("periodBegin", "2022-01-07T18:15:45");
        multiValueMap.add("periodEnd", "2022-10-01T09:00:00");
        multiValueMap.add("orderStatus", "ALL");
        Thread.sleep(10000);
        final String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/analytics")
                        .params(multiValueMap)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse().getContentAsString();

        final OrdersReport order1 = objectMapper.readValue(contentAsString, OrdersReport.class);
        Assertions.assertNotNull(order1);
    }
}
