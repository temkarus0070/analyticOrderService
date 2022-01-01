package org.temkarus0070.analyticorderservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.temkarus0070.analyticorderservice.models.Status;

import java.io.Serializable;
import java.util.Objects;


public class OrderStatusData implements Serializable {
    private OrderStatus status;
    private String clientFIO;

    public OrderStatusData(OrderStatus status, String clientFIO) {
        this.status = status;
        this.clientFIO = clientFIO;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getClientFIO() {
        return clientFIO;
    }
    public OrderStatusData(OrderStatus status) {
        this.status = status;
        this.clientFIO="";
    }
    public OrderStatusData() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusData that = (OrderStatusData) o;
        return status == that.status && Objects.equals(clientFIO, that.clientFIO);
    }
    @Override
    public int hashCode() {
        int n = Objects.hash(status, clientFIO);
        return Objects.hash(status, clientFIO);
    }


}
