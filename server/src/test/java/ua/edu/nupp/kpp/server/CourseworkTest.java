package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import ua.edu.nupp.kpp.server.model.*;
import ua.edu.nupp.kpp.server.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CourseworkTest {

    @Test
    void testF1WithoutExceedanceIsZero() {
        List<Measurement> list = List.of(
                new Measurement(1L, 1L, "Залізо", 0.25, 0.30, "мг/дм3")
        );
        assertEquals(0.0, WaterQualityCalculator.calculateF1(list), 0.001);
    }

    @Test
    void testF1WithExceedanceNormalized() {
        List<Measurement> list = List.of(
                new Measurement(1L, 1L, "Нітрати", 63.0, 45.0, "мг/дм3")
        );
        // (63 - 45) / 45 * 100 = 40.0
        assertEquals(40.0, WaterQualityCalculator.calculateF1(list), 0.001);
    }

    @Test
    void testF1ClampedAt100() {
        List<Measurement> list = List.of(
                new Measurement(1L, 1L, "Амоній", 10.0, 2.0, "мг/дм3")
        );
        assertEquals(100.0, WaterQualityCalculator.calculateF1(list), 0.001);
    }

    @Test
    void testF2CriticalParametersCount() {
        List<Measurement> list = List.of(
                new Measurement(1L, 1L, "Нітрати", 63.0, 45.0, "мг/дм3"),
                new Measurement(2L, 1L, "Кисень", 6.5, 6.0, "мг/дм3"),
                new Measurement(3L, 1L, "Залізо", 0.25, 0.30, "мг/дм3")
        );
        // 2 перевищення * 20 = 40.0
        assertEquals(40.0, WaterQualityCalculator.calculateF2(list), 0.001);
    }

    @Test
    void testIntegralIndexCalculation() {
        // I = 0.40*40 + 0.35*20 + 0.25*80 = 16 + 7 + 20 = 43.0
        assertEquals(43.0, WaterQualityCalculator.calculateIntegralIndex(40.0, 20.0, 80.0), 0.001);
    }

    @Test
    void testIntegralIndexRejectsOutOfRangeFactor() {
        assertThrows(IllegalArgumentException.class, () ->
                WaterQualityCalculator.calculateIntegralIndex(105.0, 20.0, 50.0));
        assertThrows(IllegalArgumentException.class, () ->
                WaterQualityCalculator.calculateIntegralIndex(40.0, -5.0, 50.0));
    }

    @Test
    void testLifecycleAllowedTransitions() {
        assertTrue(SampleService.isAllowedTransition(SampleState.REGISTERED, SampleState.VALIDATED));
        assertTrue(SampleService.isAllowedTransition(SampleState.VALIDATED, SampleState.ANALYZED));
        assertTrue(SampleService.isAllowedTransition(SampleState.ANALYZED, SampleState.ARCHIVED));
    }

    @Test
    void testLifecycleForbiddenTransitions() {
        assertFalse(SampleService.isAllowedTransition(SampleState.REGISTERED, SampleState.ARCHIVED));
        assertFalse(SampleService.isAllowedTransition(SampleState.REGISTERED, SampleState.ANALYZED));
        assertFalse(SampleService.isAllowedTransition(SampleState.ARCHIVED, SampleState.REGISTERED));
    }

    @Test
    void testObservationPointValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                ObservationPointService.validate(new ObservationPoint(null, "", "Пункт", "р. Ворскла", 50.0)));
        assertThrows(IllegalArgumentException.class, () ->
                ObservationPointService.validate(new ObservationPoint(null, "WP-01", "Пункт", "р. Ворскла", 120.0)));
    }

    @Test
    void testMeasurementValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                MeasurementService.validate(new Measurement(null, 1L, "", 10.0, 5.0, "мг/дм3")));
        assertThrows(IllegalArgumentException.class, () ->
                MeasurementService.validate(new Measurement(null, 1L, "Нітрати", 10.0, 0.0, "мг/дм3")));
    }

    @Test
    void testInspectionValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                InspectionService.validate(new Inspection(null, 1L, "", "2026-10-20", "Висновок")));
    }
}
