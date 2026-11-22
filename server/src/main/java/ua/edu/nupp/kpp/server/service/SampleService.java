package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.MeasurementDao;
import ua.edu.nupp.kpp.server.dao.ObservationPointDao;
import ua.edu.nupp.kpp.server.dao.SampleDao;
import ua.edu.nupp.kpp.server.model.Measurement;
import ua.edu.nupp.kpp.server.model.ObservationPoint;
import ua.edu.nupp.kpp.server.model.Sample;
import ua.edu.nupp.kpp.server.model.SampleState;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class SampleService {
    private static final Logger LOG = Logger.getLogger(SampleService.class.getName());
    private final SampleDao sampleDao = new SampleDao();
    private final ObservationPointDao pointDao = new ObservationPointDao();
    private final MeasurementDao measurementDao = new MeasurementDao();

    public List<Sample> getAll() throws SQLException {
        return sampleDao.getAll();
    }

    public void add(long pointId, String sampleDate) throws SQLException {
        if (sampleDate == null || sampleDate.isBlank()) {
            throw new IllegalArgumentException("Дата відбору проби є обов'язковою");
        }
        ObservationPoint pt = pointDao.getById(pointId);
        if (pt == null) {
            throw new IllegalArgumentException("Пункт спостереження з ID=" + pointId + " не знайдено");
        }
        double f1 = 0.0;
        double f2 = 0.0;
        double f3 = pt.getSensitivityScore();
        double integral = WaterQualityCalculator.calculateIntegralIndex(f1, f2, f3);

        Sample sample = new Sample(null, pointId, sampleDate.trim(), SampleState.REGISTERED, f1, f2, f3, integral);
        sampleDao.add(sample);
        LOG.info("Зареєстровано нову пробу для пункту ID=" + pointId);
    }

    public void update(long id, long pointId, String sampleDate) throws SQLException {
        Sample existing = sampleDao.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Пробу з ID=" + id + " не знайдено");
        }
        if (sampleDate == null || sampleDate.isBlank()) {
            throw new IllegalArgumentException("Дата відбору проби є обов'язковою");
        }
        ObservationPoint pt = pointDao.getById(pointId);
        if (pt == null) {
            throw new IllegalArgumentException("Пункт спостереження з ID=" + pointId + " не знайдено");
        }
        existing.setPointId(pointId);
        existing.setSampleDate(sampleDate.trim());
        recalculateAndSave(existing, pt.getSensitivityScore());
        LOG.info("Оновлено та перераховано пробу ID=" + id);
    }

    public void recalculateForSample(long sampleId) throws SQLException {
        Sample s = sampleDao.getById(sampleId);
        if (s == null) return;
        ObservationPoint pt = pointDao.getById(s.getPointId());
        double f3 = (pt != null) ? pt.getSensitivityScore() : s.getF3();
        recalculateAndSave(s, f3);
    }

    private void recalculateAndSave(Sample s, double f3) throws SQLException {
        List<Measurement> measurements = measurementDao.getBySampleId(s.getId());
        double f1 = WaterQualityCalculator.calculateF1(measurements);
        double f2 = WaterQualityCalculator.calculateF2(measurements);
        double integral = WaterQualityCalculator.calculateIntegralIndex(f1, f2, f3);
        s.setF1(f1);
        s.setF2(f2);
        s.setF3(f3);
        s.setIntegralScore(integral);
        sampleDao.update(s);
    }

    public void changeState(long id, SampleState target) throws SQLException {
        Sample current = sampleDao.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Пробу з ID=" + id + " не знайдено");
        }
        if (!isAllowedTransition(current.getState(), target)) {
            LOG.warning("Спроба недопустимого переходу стану проби ID=" + id + ": " + current.getState() + " -> " + target);
            throw new IllegalArgumentException("Недопустимий перехід стану: " + current.getState() + " -> " + target);
        }
        sampleDao.updateState(id, target);
        LOG.info("Змінено стан проби ID=" + id + " на " + target);
    }

    public void delete(long id) throws SQLException {
        sampleDao.delete(id);
        LOG.info("Видалено пробу ID=" + id);
    }

    public static boolean isAllowedTransition(SampleState from, SampleState to) {
        if (from == null || to == null) return false;
        return (from == SampleState.REGISTERED && to == SampleState.VALIDATED)
                || (from == SampleState.VALIDATED && to == SampleState.ANALYZED)
                || (from == SampleState.ANALYZED && to == SampleState.ARCHIVED);
    }
}
