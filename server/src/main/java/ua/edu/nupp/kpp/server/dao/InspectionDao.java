package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.Inspection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InspectionDao {
    public List<Inspection> getAll() throws SQLException {
        List<Inspection> list = new ArrayList<>();
        String sql = "SELECT id, sample_id, inspector_name, inspection_date, conclusion FROM inspection ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Inspection getById(long id) throws SQLException {
        String sql = "SELECT id, sample_id, inspector_name, inspection_date, conclusion FROM inspection WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void add(Inspection ins) throws SQLException {
        String sql = "INSERT INTO inspection (sample_id, inspector_name, inspection_date, conclusion) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, ins);
            p.executeUpdate();
        }
    }

    public void update(Inspection ins) throws SQLException {
        String sql = "UPDATE inspection SET sample_id = ?, inspector_name = ?, inspection_date = ?, conclusion = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, ins);
            p.setLong(5, ins.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM inspection WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Inspection ins) throws SQLException {
        p.setLong(1, ins.getSampleId());
        p.setString(2, ins.getInspectorName());
        p.setString(3, ins.getInspectionDate());
        p.setString(4, ins.getConclusion());
    }

    private Inspection map(ResultSet r) throws SQLException {
        return new Inspection(
                r.getLong("id"),
                r.getLong("sample_id"),
                r.getString("inspector_name"),
                r.getString("inspection_date"),
                r.getString("conclusion")
        );
    }
}
