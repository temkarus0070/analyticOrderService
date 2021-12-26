package org.temkarus0070.analyticorderservice.models;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

public @Data
class Order implements Serializable {
    private String clientFIO;
    private long orderNum;
    private Collection<org.temkarus0070.analyticorderservice.models.Good> goods;
    private org.temkarus0070.analyticorderservice.models.Status status;

}
