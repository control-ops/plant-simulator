package com.control_ops.plant_simulator.sensor;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private long measurementCount = 0;
    private boolean isMeasuring = false;

    private final long samplingPeriod;
    private final Buffer<Measurement> buffer;
    private final MeasurementUnit measurementUnit;
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler;

    /**
     * Initializes a new sensor object.
     * @param samplingPeriod How often the sensor should record a new measurement, in milliseconds
     * @param measurementUnit The measurement unit of data gathered by the sensor
     */
    public Sensor(
            final long samplingPeriod,
            final MeasurementUnit measurementUnit,
            final long bufferCapacity) {
        this.samplingPeriod = samplingPeriod;
        this.measurementUnit = measurementUnit;
        this.buffer = new Buffer<>(bufferCapacity);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    private synchronized void takeMeasurement() {
        final Measurement newMeasurement = new Measurement(
                random.nextDouble(),
                this.measurementUnit,
                ZonedDateTime.now(ZoneId.of("UTC")));
        this.buffer.add(newMeasurement);
        this.measurementCount++;
    }

    public void startMeasuring() {
        if (!isMeasuring) {
            this.scheduler.scheduleAtFixedRate(this::takeMeasurement, 0L, this.samplingPeriod, TimeUnit.MILLISECONDS);
            this.isMeasuring = true;
        }
    }

    public void stopMeasuring() {
        if (isMeasuring) {
            this.scheduler.shutdown();
            this.isMeasuring = false;
        }
    }

    public long getMeasurementCount() {
        return this.measurementCount;
    }

    public List<Measurement> getMeasurements() {
        return this.buffer.exportCopy();
    }
}
