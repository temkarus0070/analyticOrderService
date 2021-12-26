package org.temkarus0070.analyticorderservice;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrdersReport {
    private long ordersCount;
    private double sum;
    private long rowsCount;


}
