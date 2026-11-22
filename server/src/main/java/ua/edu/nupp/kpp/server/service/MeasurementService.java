package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.MeasurementDao;
import ua.edu.nupp.kpp.server.dao.SampleDao;
import ua.edu.nupp.kpp.server.model.Measurement;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class MeasurementService {
    private static final Logger LOG = Logger.getLogger(MeasurementService.class.getName());
    private final MeasurementDao dao = new MeasurementDao();
    private final SampleDao sampleDao = new SampleDao();
    private final SampleService sampleService = new SampleService();

    public List<Measurement> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Measurement m) throws SQLException {
        validate(m);
        if (sampleDao.getById(m.getSampleId()) == null) {
            throw new IllegalArgumentException("Пробу з ID=" + m.getSampleId() + " не знайдено");
        }
        dao.add(m);
        sampleService.recalculateForSample(m.getSampleId());
        LOG.info("Додано вимірювання параметра '" + m.getParameterName() + "' для проби ID=" + m.getSampleId());
    }

    public void update(Measurement m) throws SQLException {
        if (m.getId() == null || m.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID вимірювання");
        }
        validate(m);
        if (sampleDao.getById(m.getSampleId()) == null) {
            throw new IllegalArgumentException("Пробу з ID=" + m.getSampleId() + " не знайдено");
        }
        dao.update(m);
        sampleService.recalculateForSample(m.getSampleId());
        LOG.info("Оновлено вимірювання ID=" + m.getId());
    }

    public void delete(long id) throws SQLException {
        Measurement existing = dao.getById(id);
        dao.delete(id);
        if (existing != null) {
            sampleService.recalculateForSample(existing.getSampleId());
        }
        LOG.info("Видалено вимірювання ID=" + id);
    }

    public static void validate(Measurement m) {
        if (m == null || m.getSampleId() == null || m.getSampleId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID проби");
        }
        if (m.getParameterName() == null || m.getParameterName().isBlank()) {
            throw new IllegalArgumentException("Назва показника якості води є обов'язковою");
        }
        if (m.getMeasuredValue() < 0 || m.getNormValue() <= 0) {
            throw new IllegalArgumentException("Виміряне значення має бути >= 0, а норматив > 0");
        }
        if (m.getUnit() == null || m.getUnit().isBlank()) {
            throw new IllegalArgumentException("Одиниця вимірювання є обов'язковою");
        }
    }
}
