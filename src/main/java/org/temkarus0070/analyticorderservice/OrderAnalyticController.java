package org.temkarus0070.analyticorderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
import org.temkarus0070.analyticorderservice.models.OrderStatusData;
import org.temkarus0070.analyticorderservice.models.OrdersReport;

import java.time.LocalDateTime;

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
    public OrdersReport getAnalyticsByOrders(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodBegin, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd, @RequestParam(required = false) String clientId, @RequestParam OrderStatus orderStatus){
        return analyticsService.ordersReport(periodBegin,periodEnd,clientId, orderStatus);
    }


}
