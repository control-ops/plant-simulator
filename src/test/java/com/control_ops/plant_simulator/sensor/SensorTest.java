package com.control_ops.plant_simulator.sensor;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the Sensor class and SensorListener class that implements the Observer pattern.
 */
class SensorTest {

    final MeasurementList measurementList = new MeasurementList();
    final List<Measurement> measurements = measurementList.getMeasurements();

    long samplingPeriod = 20L;
    final TimeUnit samplingTimeUnit = TimeUnit.MILLISECONDS;

    /**
     * Causes the calling thread to wait until at least one measurement is received.
     */
    private void waitForMeasurements() {
        await().atMost(10*samplingPeriod, samplingTimeUnit).until(() -> !measurements.isEmpty());
    }

    /**
     * Tests that the sensor's startMeasuring() method commences the generation and transmission of measurements.
     */
    @Test
    void testStartMeasuring() {
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);
        assertThrows(
                ConditionTimeoutException.class,
                this::waitForMeasurements);
        assertThat(measurements).isEmpty();

        sensor.startMeasuring();
        this.waitForMeasurements();
        assertThat(measurements).isNotEmpty();
    }

    /**
     * Tests that the sensor's stopMeasuring() method ceases the generation and transmission of measurements.
     */
    @Test
    void testStopMeasuring() {
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);
        sensor.startMeasuring();
        this.waitForMeasurements();
        sensor.stopMeasuring();
        measurements.clear();
        assertThrows(ConditionTimeoutException.class, this::waitForMeasurements);
    }

    /**
     * Tests that the sensor's addListener() method causes the observer to start receiving measurements.
     */
    @Test
    void testAddListener() {
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);
        sensor.startMeasuring();
        await().atMost(100*samplingPeriod, samplingTimeUnit).until(() -> !measurements.isEmpty());
        assertThat(measurements).hasSizeGreaterThan(1);
    }

    /**
     * Tests that the sensor's removeObserver method causes the observer to stop receiving measurements.
     */
    @Test
    void testRemoveListener() {
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);
        sensor.removeListener(measurementList);
        sensor.startMeasuring();
        await().during(10*samplingPeriod, samplingTimeUnit);
        assertThat(measurements).isEmpty();
    }

    /**
     * Tests that the sensor actually takes unique measurements.
     */
    @Test
    void testTakeMeasurement() {
        final long minimumMeasurements = 100L;
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(10, TimeUnit.SECONDS).until(() -> measurements.size() >= minimumMeasurements);
        sensor.stopMeasuring();
        for (int i = 1; i < measurements.size(); i++) {
            assertThat(measurements.get(i - 1)).isNotEqualTo(measurements.get(i));
        }
    }

    /**
     * Tests that the sensor takes measurements in chronological order.
     */
    @Test
    void testMeasurementSequence() {
        final long minimumMeasurements = 100L;
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(10, TimeUnit.SECONDS).until(() -> measurements.size() >= minimumMeasurements);
        sensor.stopMeasuring();
        for (int i = 1; i < measurements.size(); i++) {
            final long elapsedTime = Duration.between(
                    measurements.get(i - 1).dateTime(),
                    measurements.get(i).dateTime()).toMillis();
            assertThat(elapsedTime).isPositive();
        }
    }


    /**
     * Calculates the fractional error between an expected sampling period and an actual sampling period. The actual
     * sampling period is calculating by comparing the total time measurements were being taken to the number of
     * measurements that were taken.
     * @param expectedSamplingPeriod The sampling period that was set on the sensor
     * @param numMeasurements The total number of measurements that were exported by the sensor
     * @param firstMeasurementTime The time at which the sensor took the first measurement
     * @param lastMeasurementTime The time at which the sensor took the last measurement
     * @return The fractional error between the expected and actual sampling periods
     */
    double calculateSamplingPeriodError(
            long expectedSamplingPeriod,
            long numMeasurements,
            final ZonedDateTime firstMeasurementTime,
            final ZonedDateTime lastMeasurementTime) {
        final long totalDuration = Duration.between(firstMeasurementTime, lastMeasurementTime).toMillis();
        final double actualSamplingPeriod = (double)totalDuration / (double)(numMeasurements - 1);
        return Math.abs((actualSamplingPeriod - (double)expectedSamplingPeriod) / (double)expectedSamplingPeriod);
    }


    /**
     * Tests that the actual time interval between measurements matches the one set using the sensor's samplingPeriod
     * field; the fractional error between the two is used to determine whether the test passes.
     * <br><br>
     * The error depends on non-deterministic threading behaviour; the error is therefore calculated over a large number
     * of measurements and compared to a threshold to smooth out the results.
     * @param expectedSamplingPeriod The sampling period to be set on the sensor
     * @param minimumMeasurements The minimum number of measurements required to calculate the fractional error
     * @param maxFractionalError The maximum tolerable fractional error between the expected and actual sampling periods
     */
    @ParameterizedTest
    @CsvSource({
            "50, 150, 0.01",
            "100, 75, 0.01",
            "200, 40, 0.01",
            "500, 15, 0.01"
    })
    void testSamplingPeriod(
            final long expectedSamplingPeriod,
            final long minimumMeasurements,
            final double maxFractionalError) {
        this.samplingPeriod = expectedSamplingPeriod;
        final Sensor sensor = new Sensor(samplingPeriod, samplingTimeUnit, MeasurementUnit.CELSIUS);
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(60, TimeUnit.SECONDS).until(() -> measurements.size() >= minimumMeasurements);
        sensor.stopMeasuring();

        final Measurement firstMeasurement = measurements.getFirst();
        final Measurement lastMeasurement = measurements.getLast();

        final double averageSamplingPeriodError = this.calculateSamplingPeriodError(
                samplingPeriod,
                measurements.size(),
                firstMeasurement.dateTime(),
                lastMeasurement.dateTime());
        assertThat(averageSamplingPeriodError).isLessThan(maxFractionalError);
    }
}
