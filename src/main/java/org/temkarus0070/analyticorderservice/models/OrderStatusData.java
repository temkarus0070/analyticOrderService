package org.temkarus0070.analyticorderservice.models;

import java.io.Serializable;
import java.util.Objects;


public class OrderStatusData implements Serializable {
    private OrderStatus status;
    private String clientFIO;

    @Override
    public String toString() {
        return "OrderStatusData{" +
                "status=" + status +
                ", clientFIO='" + clientFIO + '\'' +
                '}';
    }

    public OrderStatusData(OrderStatus status, String clientFIO) {
        if (clientFIO==null)
            clientFIO="";
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
