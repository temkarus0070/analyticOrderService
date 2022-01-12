package org.temkarus0070.analyticorderservice.models;

import java.io.Serializable;

public enum OrderStatus implements Serializable {
    ALL,
    PENDING,
    PURCHASED,
    CANCELLED
}
