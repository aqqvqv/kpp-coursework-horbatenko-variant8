package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.ObservationPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObservationPointDao {
    public List<ObservationPoint> getAll() throws SQLException {
        List<ObservationPoint> list = new ArrayList<>();
        String sql = "SELECT id, code, name, water_body, sensitivity_score FROM observation_point ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new ObservationPoint(
                        r.getLong("id"),
                        r.getString("code"),
                        r.getString("name"),
                        r.getString("water_body"),
                        r.getDouble("sensitivity_score")
                ));
            }
        }
        return list;
    }

    public ObservationPoint getById(long id) throws SQLException {
        String sql = "SELECT id, code, name, water_body, sensitivity_score FROM observation_point WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return new ObservationPoint(
                            r.getLong("id"),
                            r.getString("code"),
                            r.getString("name"),
                            r.getString("water_body"),
                            r.getDouble("sensitivity_score")
                    );
                }
                return null;
            }
        }
    }

    public void add(ObservationPoint pt) throws SQLException {
        String sql = "INSERT INTO observation_point (code, name, water_body, sensitivity_score) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, pt.getCode());
            p.setString(2, pt.getName());
            p.setString(3, pt.getWaterBody());
            p.setDouble(4, pt.getSensitivityScore());
            p.executeUpdate();
        }
    }

    public void update(ObservationPoint pt) throws SQLException {
        String sql = "UPDATE observation_point SET code = ?, name = ?, water_body = ?, sensitivity_score = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, pt.getCode());
            p.setString(2, pt.getName());
            p.setString(3, pt.getWaterBody());
            p.setDouble(4, pt.getSensitivityScore());
            p.setLong(5, pt.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM observation_point WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }
}
