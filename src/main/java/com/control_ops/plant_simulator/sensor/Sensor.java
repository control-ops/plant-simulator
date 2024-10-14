package com.control_ops.plant_simulator.sensor;

public class Sensor {
    private double currentMeasurement;
    private double refreshRate;
    private final MeasurementUnit measurementUnit;

    public Sensor(
            final double initialMeasurement,
            final double refreshRate,
            final MeasurementUnit measurementUnit) {
        this.currentMeasurement = initialMeasurement;
        this.refreshRate = refreshRate;
        this.measurementUnit = measurementUnit;
    }

    public Measurement getMeasurement() {
        return new Measurement(this.currentMeasurement, this.measurementUnit);
    }

    public double getRefreshRate() {
        return this.refreshRate;
    }

    public void setRefreshRate(final double refreshRate) {
        this.refreshRate = refreshRate;
    }
}
