package org.temkarus0070.analyticorderservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.temkarus0070.analyticorderservice.models.OrderStatus;
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

    @Operation(summary = "Get stats by orders for period from periodBegin to periodEnd, user and orderStatus"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "400"
            , description = "invalid periodBegin or/and periodEnd or/and orderStatus were entered"),
            @ApiResponse(responseCode = "500", description = "problem with connect to kafka or kafka stream is not started yet")})
    @Parameters(value = {@Parameter(name = "periodBegin", description = "first date with time for stats", example = "2022-01-07T18:15:45", required = true),
            @Parameter(name = "periodEnd", description = "last date with time for stats", example = "2023-01-07T18:15:45", required = true),
            @Parameter(name = "clientId", description = "Client fullname", example = "Vasya Pupkin"),
            @Parameter(name = "orderStatus", description = "order status", examples = {@ExampleObject(value = "ALL"), @ExampleObject(value = "PENDING"),
                    @ExampleObject(value = "PURCHASED"), @ExampleObject(value = "CANCELLED")}, required = true),
    })
    @GetMapping(produces = "application/json")
    @ExceptionHandler(InvalidDatesAtRequestException.class)
    public OrdersReport getAnalyticsByOrders(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodBegin,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd,
                                             @RequestParam(required = false) String clientId, @RequestParam OrderStatus orderStatus) {
        return analyticsService.ordersReport(periodBegin, periodEnd, clientId, orderStatus);
    }


}
