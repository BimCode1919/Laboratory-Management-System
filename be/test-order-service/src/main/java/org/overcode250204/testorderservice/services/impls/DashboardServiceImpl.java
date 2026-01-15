package org.overcode250204.testorderservice.services.impls;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersOverTimeDTO;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersTypeCountDTO;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.services.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TestOrdersRepository testOrdersRepository;

    @Override
    public List<OrdersOverTimeDTO> getOrdersOverTime(int days) {
        List<Object[]> rows = testOrdersRepository.countOrdersOverTime(days);
        return rows.stream()
                .map(r -> new OrdersOverTimeDTO((String) r[0],
                        TestOrderType.valueOf((String) r[1]),
                        ((Number) r[2]).longValue()))
                .toList();
    }

    @Override
    public List<OrdersTypeCountDTO> getOrdersCountByType() {
        return testOrdersRepository.countOrdersByType();
    }
}
