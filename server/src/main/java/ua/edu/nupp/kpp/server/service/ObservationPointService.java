package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.ObservationPointDao;
import ua.edu.nupp.kpp.server.model.ObservationPoint;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ObservationPointService {
    private static final Logger LOG = Logger.getLogger(ObservationPointService.class.getName());
    private final ObservationPointDao dao = new ObservationPointDao();

    public List<ObservationPoint> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(ObservationPoint pt) throws SQLException {
        validate(pt);
        dao.add(pt);
        LOG.info("Додано пункт спостереження: " + pt.getCode());
    }

    public void update(ObservationPoint pt) throws SQLException {
        if (pt.getId() == null || pt.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID пункту спостереження");
        }
        validate(pt);
        dao.update(pt);
        LOG.info("Оновлено пункт спостереження ID: " + pt.getId());
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено пункт спостереження ID: " + id);
    }

    public static void validate(ObservationPoint pt) {
        if (pt == null || blank(pt.getCode()) || blank(pt.getName()) || blank(pt.getWaterBody())) {
            throw new IllegalArgumentException("Усі текстові поля пункту спостереження є обов'язковими");
        }
        WaterQualityCalculator.validateFactor(pt.getSensitivityScore(), "F3 (чутливість водокористування)");
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
