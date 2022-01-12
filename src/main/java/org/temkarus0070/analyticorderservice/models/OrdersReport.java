package org.temkarus0070.analyticorderservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersReport implements Serializable {
    private long ordersCount;
    private double sum;
    private long rowsCount;
}
