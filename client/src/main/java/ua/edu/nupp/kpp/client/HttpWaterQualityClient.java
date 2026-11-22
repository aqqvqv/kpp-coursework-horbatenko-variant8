package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.client.model.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class HttpWaterQualityClient {
    private static final String BASE_URL = "http://localhost:8080/server";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ObservationPoint
    public List<ObservationPoint> getPoints() {
        return Arrays.asList(read(get("/points"), ObservationPoint[].class));
    }

    public void addPoint(ObservationPoint pt) {
        post("/points", "action=add&code=" + enc(pt.getCode()) +
                "&name=" + enc(pt.getName()) +
                "&waterBody=" + enc(pt.getWaterBody()) +
                "&sensitivityScore=" + pt.getSensitivityScore());
    }

    public void updatePoint(ObservationPoint pt) {
        post("/points", "action=update&id=" + pt.getId() +
                "&code=" + enc(pt.getCode()) +
                "&name=" + enc(pt.getName()) +
                "&waterBody=" + enc(pt.getWaterBody()) +
                "&sensitivityScore=" + pt.getSensitivityScore());
    }

    public void deletePoint(long id) {
        post("/points", "action=delete&id=" + id);
    }

    // Sample
    public List<Sample> getSamples() {
        return Arrays.asList(read(get("/samples"), Sample[].class));
    }

    public void addSample(long pointId, String sampleDate) {
        post("/samples", "action=add&pointId=" + pointId + "&sampleDate=" + enc(sampleDate));
    }

    public void updateSample(long id, long pointId, String sampleDate) {
        post("/samples", "action=update&id=" + id + "&pointId=" + pointId + "&sampleDate=" + enc(sampleDate));
    }

    public void changeSampleState(long id, SampleState target) {
        post("/samples", "action=transition&id=" + id + "&state=" + target.name());
    }

    public void deleteSample(long id) {
        post("/samples", "action=delete&id=" + id);
    }

    // Measurement
    public List<Measurement> getMeasurements() {
        return Arrays.asList(read(get("/measurements"), Measurement[].class));
    }

    public void addMeasurement(Measurement m) {
        post("/measurements", "action=add&sampleId=" + m.getSampleId() +
                "&parameterName=" + enc(m.getParameterName()) +
                "&measuredValue=" + m.getMeasuredValue() +
                "&normValue=" + m.getNormValue() +
                "&unit=" + enc(m.getUnit()));
    }

    public void updateMeasurement(Measurement m) {
        post("/measurements", "action=update&id=" + m.getId() +
                "&sampleId=" + m.getSampleId() +
                "&parameterName=" + enc(m.getParameterName()) +
                "&measuredValue=" + m.getMeasuredValue() +
                "&normValue=" + m.getNormValue() +
                "&unit=" + enc(m.getUnit()));
    }

    public void deleteMeasurement(long id) {
        post("/measurements", "action=delete&id=" + id);
    }

    // Inspection
    public List<Inspection> getInspections() {
        return Arrays.asList(read(get("/inspections"), Inspection[].class));
    }

    public void addInspection(Inspection ins) {
        post("/inspections", "action=add&sampleId=" + ins.getSampleId() +
                "&inspectorName=" + enc(ins.getInspectorName()) +
                "&inspectionDate=" + enc(ins.getInspectionDate()) +
                "&conclusion=" + enc(ins.getConclusion()));
    }

    public void updateInspection(Inspection ins) {
        post("/inspections", "action=update&id=" + ins.getId() +
                "&sampleId=" + ins.getSampleId() +
                "&inspectorName=" + enc(ins.getInspectorName()) +
                "&inspectionDate=" + enc(ins.getInspectionDate()) +
                "&conclusion=" + enc(ins.getConclusion()));
    }

    public void deleteInspection(long id) {
        post("/inspections", "action=delete&id=" + id);
    }

    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path)).GET().build();
        return send(req);
    }

    private String post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        return send(req);
    }

    private String send(HttpRequest req) {
        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 400) {
                String msg = resp.body();
                try {
                    JsonNode node = mapper.readTree(msg);
                    if (node.has("error")) msg = node.get("error").asText();
                } catch (Exception ignored) {}
                throw new RuntimeException(msg);
            }
            return resp.body();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Помилка з'єднання з сервером: " + e.getMessage(), e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Помилка розбору JSON відповіді", e);
        }
    }

    private String enc(String val) {
        return URLEncoder.encode(val == null ? "" : val, StandardCharsets.UTF_8);
    }
}
