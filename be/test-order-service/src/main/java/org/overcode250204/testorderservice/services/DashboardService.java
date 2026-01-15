package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.dashboard.OrdersOverTimeDTO;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersTypeCountDTO;

import java.util.List;

public interface DashboardService {
    List<OrdersOverTimeDTO> getOrdersOverTime(int days);
    List<OrdersTypeCountDTO> getOrdersCountByType();
}
