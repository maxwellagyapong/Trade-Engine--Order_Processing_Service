package com.glfx.orderprocessingservice.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoggerEntity {
    private Date dateLogged;
    private String logEntity;
    private String message;
}