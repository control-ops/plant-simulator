package com.control_ops.plant_simulator.sensor;

public interface SensorListener {
    void onMeasurement(final Measurement measurement);
}
