package com.control_ops.plant_simulator.sensor;

import java.time.ZonedDateTime;

public record Measurement (
        double quantity,
        MeasurementUnit unit,
        ZonedDateTime dateTime) {
}
