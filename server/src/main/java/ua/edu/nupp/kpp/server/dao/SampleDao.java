package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.Sample;
import ua.edu.nupp.kpp.server.model.SampleState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SampleDao {
    public List<Sample> getAll() throws SQLException {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT id, point_id, sample_date, state, f1, f2, f3, integral_score FROM sample ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Sample getById(long id) throws SQLException {
        String sql = "SELECT id, point_id, sample_date, state, f1, f2, f3, integral_score FROM sample WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void add(Sample s) throws SQLException {
        String sql = "INSERT INTO sample (point_id, sample_date, state, f1, f2, f3, integral_score) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            p.executeUpdate();
        }
    }

    public void update(Sample s) throws SQLException {
        String sql = "UPDATE sample SET point_id = ?, sample_date = ?, state = ?, f1 = ?, f2 = ?, f3 = ?, integral_score = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            p.setLong(8, s.getId());
            p.executeUpdate();
        }
    }

    public void updateState(long id, SampleState state) throws SQLException {
        String sql = "UPDATE sample SET state = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM sample WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Sample s) throws SQLException {
        p.setLong(1, s.getPointId());
        p.setString(2, s.getSampleDate());
        p.setString(3, s.getState().name());
        p.setDouble(4, s.getF1());
        p.setDouble(5, s.getF2());
        p.setDouble(6, s.getF3());
        p.setDouble(7, s.getIntegralScore());
    }

    private Sample map(ResultSet r) throws SQLException {
        return new Sample(
                r.getLong("id"),
                r.getLong("point_id"),
                r.getString("sample_date"),
                SampleState.valueOf(r.getString("state")),
                r.getDouble("f1"),
                r.getDouble("f2"),
                r.getDouble("f3"),
                r.getDouble("integral_score")
        );
    }
}
