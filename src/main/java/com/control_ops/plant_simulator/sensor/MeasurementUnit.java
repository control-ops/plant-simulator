package com.control_ops.plant_simulator.sensor;

public enum MeasurementUnit {
    CELSIUS(PhysicalProperty.TEMPERATURE),
    FAHRENHEIT(PhysicalProperty.TEMPERATURE),
    M3_PER_HOUR(PhysicalProperty.VOLUMETRIC_FLOW);

    MeasurementUnit(final PhysicalProperty physicalProperty) {
        this.physicalProperty = physicalProperty;
    }

    private final PhysicalProperty physicalProperty;

    public PhysicalProperty getPhysicalProperty() {
        return this.physicalProperty;
    }
}
