package org.temkarus0070.analyticorderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
public class OrderAnalyticController {

    private AnalyticsService analyticsService;

    @Autowired
    public void setAnalyticsService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    @ExceptionHandler(InvalidDatesAtRequestException.class)
    public OrdersReport getAnalyticsByOrders(@RequestParam LocalDate periodBegin, @RequestParam LocalDate periodEnd, @RequestParam(required = false) Integer clientId, @RequestParam OrderStatus orderStatus){
        return analyticsService.ordersReport(periodBegin,periodEnd,clientId,orderStatus);
    }


}
