package com.control_ops.plant_simulator.sensor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensorTest {
    @Test
    void testInitialization() {
        final Sensor temperatureSensor = new Sensor(25.0, 10.0, MeasurementUnit.CELSIUS);
        assertEquals(25.0, temperatureSensor.getMeasurement().measuredQuantity());
        assertEquals(MeasurementUnit.CELSIUS, temperatureSensor.getMeasurement().measurementUnit());
        assertEquals(10.0, temperatureSensor.getRefreshRate());

        final Sensor flowMeter = new Sensor(100.0, 5.0, MeasurementUnit.M3_PER_HOUR);
        assertEquals(100.0, flowMeter.getMeasurement().measuredQuantity());
        assertEquals(MeasurementUnit.M3_PER_HOUR, flowMeter.getMeasurement().measurementUnit());
        assertEquals(5.0, flowMeter.getRefreshRate());
    }
}
