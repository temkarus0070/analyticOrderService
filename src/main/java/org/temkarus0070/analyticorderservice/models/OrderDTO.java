package org.temkarus0070.analyticorderservice.models;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class OrderDTO implements Serializable {
    private Long orderNum;
    private String clientFIO;
    private List<GoodDTO> goodDTOS;
    private OrderStatus status;

    public OrderDTO(Long orderNum, String clientFIO, List<GoodDTO> goodDTOS, OrderStatus status) {
        this.orderNum = orderNum;
        this.clientFIO = clientFIO;
        this.goodDTOS = goodDTOS;
        this.status = status;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDTO orderDTO = (OrderDTO) o;
        return Objects.equals(orderNum, orderDTO.orderNum) && Objects.equals(clientFIO, orderDTO.clientFIO) && Objects.equals(goodDTOS, orderDTO.goodDTOS) && status == orderDTO.status;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "orderNum=" + orderNum +
                ", clientFIO='" + clientFIO + '\'' +
                ", goodDTOS=" + goodDTOS +
                ", status=" + status +
                '}';
    }

    public OrderDTO() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNum, clientFIO, goodDTOS, status);
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public String getClientFIO() {
        return clientFIO;
    }

    public void setClientFIO(String clientFIO) {
        this.clientFIO = clientFIO;
    }

    public List<GoodDTO> getGoods() {
        return goodDTOS;
    }

    public void setGoods(List<GoodDTO> goodDTOS) {
        this.goodDTOS = goodDTOS;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
