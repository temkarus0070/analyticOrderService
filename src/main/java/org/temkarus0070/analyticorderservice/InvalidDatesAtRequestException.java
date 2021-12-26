package org.temkarus0070.analyticorderservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "begin period date is  greater than end period date")
public class InvalidDatesAtRequestException extends RuntimeException{

}
