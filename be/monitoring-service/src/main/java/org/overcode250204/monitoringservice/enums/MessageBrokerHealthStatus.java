package org.overcode250204.monitoringservice.enums;

public enum MessageBrokerHealthStatus {
    HEALTHY,     // Broker đang hoạt động ổn định
    UNHEALTHY,   // Broker lỗi
    DEGRADED,    // Hoạt động nhưng không ổn định
    UNKNOWN      // Không xác định trạng thái (mới khởi động, chưa check kịp)
}