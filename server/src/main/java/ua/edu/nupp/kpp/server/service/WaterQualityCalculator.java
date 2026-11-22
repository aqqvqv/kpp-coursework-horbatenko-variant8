package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.model.Measurement;
import java.util.List;

public class WaterQualityCalculator {

    /**
     * F1 — величина перевищення нормативу (0..100).
     * Розраховується як максимальне відсоткове перевищення серед вимірювань проби:
     * max(0, min(100, (measuredValue - normValue) / normValue * 100)).
     */
    public static double calculateF1(List<Measurement> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            return 0.0;
        }
        double maxExceedance = 0.0;
        for (Measurement m : measurements) {
            if (m.getNormValue() <= 0) {
                throw new IllegalArgumentException("Нормативне значення має бути > 0");
            }
            if (m.getMeasuredValue() > m.getNormValue()) {
                double exc = ((m.getMeasuredValue() - m.getNormValue()) / m.getNormValue()) * 100.0;
                double normalized = Math.min(100.0, Math.max(0.0, exc));
                if (normalized > maxExceedance) {
                    maxExceedance = normalized;
                }
            }
        }
        return Math.round(maxExceedance * 100.0) / 100.0;
    }

    /**
     * F2 — кількість критичних параметрів (0..100).
     * Кожен параметр із перевищенням норми додає 20 балів (5 і більше параметрів = 100).
     */
    public static double calculateF2(List<Measurement> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            return 0.0;
        }
        long criticalCount = measurements.stream()
                .filter(m -> m.getMeasuredValue() > m.getNormValue())
                .count();
        double score = Math.min(100.0, criticalCount * 20.0);
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * I = 0.40 * F1 + 0.35 * F2 + 0.25 * F3
     */
    public static double calculateIntegralIndex(double f1, double f2, double f3) {
        validateFactor(f1, "F1");
        validateFactor(f2, "F2");
        validateFactor(f3, "F3");
        double index = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(index * 100.0) / 100.0;
    }

    public static void validateFactor(double value, String name) {
        if (value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException("Фактор " + name + " має бути в діапазоні 0..100");
        }
    }
}
