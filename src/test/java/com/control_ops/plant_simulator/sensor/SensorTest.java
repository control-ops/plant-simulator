package com.control_ops.plant_simulator.sensor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class SensorTest {
    @Test
    void testMeasurements() {
        final long samplingPeriod = 10L;
        final long bufferCapacity = 100L;
        final Sensor sensor = new Sensor(samplingPeriod, MeasurementUnit.CELSIUS, bufferCapacity);

        sensor.startMeasuring();
        await().atMost(10, TimeUnit.SECONDS).until(() -> sensor.getMeasurementCount() >= bufferCapacity);
        sensor.stopMeasuring();

        final List<Measurement> measurements = sensor.getMeasurements();
        assertEquals(bufferCapacity, measurements.size());

        for (int i = 1; i < measurements.size(); i++) {
            assertNotEquals(measurements.get(i - 1), measurements.get(i));
        }
    }

    double calculateSamplingPeriodError(
            long expectedSamplingPeriod,
            long numMeasurements,
            final ZonedDateTime firstMeasurementTime,
            final ZonedDateTime lastMeasurementTime) {
        final long totalDuration = Duration.between(firstMeasurementTime, lastMeasurementTime).toMillis();
        final double actualSamplingPeriod = (double)totalDuration / (double)(numMeasurements - 1);
        return Math.abs((actualSamplingPeriod - (double)expectedSamplingPeriod) / (double)expectedSamplingPeriod);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 75, 0.01, 0.2",
            "200, 40, 0.01, 0.10",
            "500, 15, 0.01, 0.05"
    })
    void testSamplingPeriod(
            final long samplingPeriod,
            final long totalMeasurements,
            final double maxAverageError,
            final double maxIndividualError) {
        final Sensor sensor = new Sensor(samplingPeriod, MeasurementUnit.CELSIUS, totalMeasurements);

        sensor.startMeasuring();
        await().atMost(60, TimeUnit.SECONDS).until(() -> sensor.getMeasurementCount() >= totalMeasurements);
        sensor.stopMeasuring();

        final List<Measurement> measurements = sensor.getMeasurements();
        assertEquals(totalMeasurements, measurements.size());

        final Measurement firstMeasurement = measurements.getFirst();
        final Measurement lastMeasurement = measurements.getLast();

        final double averageSamplingPeriodError = this.calculateSamplingPeriodError(
                samplingPeriod,
                measurements.size(),
                firstMeasurement.dateTime(),
                lastMeasurement.dateTime());
        assertTrue(averageSamplingPeriodError < maxAverageError);

        for (int i = 1; i < measurements.size(); i++) {
            final double samplingPeriodError = this.calculateSamplingPeriodError(
                    samplingPeriod,
                    2,
                    measurements.get(i - 1).dateTime(),
                    measurements.get(i).dateTime());
            assertTrue(samplingPeriodError < maxIndividualError);
        }
    }

}
