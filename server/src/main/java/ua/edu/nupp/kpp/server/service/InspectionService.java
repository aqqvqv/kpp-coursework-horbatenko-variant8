package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.InspectionDao;
import ua.edu.nupp.kpp.server.dao.SampleDao;
import ua.edu.nupp.kpp.server.model.Inspection;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class InspectionService {
    private static final Logger LOG = Logger.getLogger(InspectionService.class.getName());
    private final InspectionDao dao = new InspectionDao();
    private final SampleDao sampleDao = new SampleDao();

    public List<Inspection> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Inspection ins) throws SQLException {
        validate(ins);
        if (sampleDao.getById(ins.getSampleId()) == null) {
            throw new IllegalArgumentException("Пробу з ID=" + ins.getSampleId() + " не знайдено");
        }
        dao.add(ins);
        LOG.info("Додано перевірку для проби ID=" + ins.getSampleId());
    }

    public void update(Inspection ins) throws SQLException {
        if (ins.getId() == null || ins.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID перевірки");
        }
        validate(ins);
        if (sampleDao.getById(ins.getSampleId()) == null) {
            throw new IllegalArgumentException("Пробу з ID=" + ins.getSampleId() + " не знайдено");
        }
        dao.update(ins);
        LOG.info("Оновлено перевірку ID=" + ins.getId());
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено перевірку ID=" + id);
    }

    public static void validate(Inspection ins) {
        if (ins == null || ins.getSampleId() == null || ins.getSampleId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID проби");
        }
        if (ins.getInspectorName() == null || ins.getInspectorName().isBlank()
                || ins.getInspectionDate() == null || ins.getInspectionDate().isBlank()
                || ins.getConclusion() == null || ins.getConclusion().isBlank()) {
            throw new IllegalArgumentException("Усі поля перевірки є обов'язковими");
        }
    }
}
