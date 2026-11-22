package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.Measurement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDao {
    public List<Measurement> getAll() throws SQLException {
        List<Measurement> list = new ArrayList<>();
        String sql = "SELECT id, sample_id, parameter_name, measured_value, norm_value, unit FROM measurement ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public List<Measurement> getBySampleId(long sampleId) throws SQLException {
        List<Measurement> list = new ArrayList<>();
        String sql = "SELECT id, sample_id, parameter_name, measured_value, norm_value, unit FROM measurement WHERE sample_id = ? ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, sampleId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    public Measurement getById(long id) throws SQLException {
        String sql = "SELECT id, sample_id, parameter_name, measured_value, norm_value, unit FROM measurement WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void add(Measurement m) throws SQLException {
        String sql = "INSERT INTO measurement (sample_id, parameter_name, measured_value, norm_value, unit) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, m);
            p.executeUpdate();
        }
    }

    public void update(Measurement m) throws SQLException {
        String sql = "UPDATE measurement SET sample_id = ?, parameter_name = ?, measured_value = ?, norm_value = ?, unit = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, m);
            p.setLong(6, m.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM measurement WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Measurement m) throws SQLException {
        p.setLong(1, m.getSampleId());
        p.setString(2, m.getParameterName());
        p.setDouble(3, m.getMeasuredValue());
        p.setDouble(4, m.getNormValue());
        p.setString(5, m.getUnit());
    }

    private Measurement map(ResultSet r) throws SQLException {
        return new Measurement(
                r.getLong("id"),
                r.getLong("sample_id"),
                r.getString("parameter_name"),
                r.getDouble("measured_value"),
                r.getDouble("norm_value"),
                r.getString("unit")
        );
    }
}
