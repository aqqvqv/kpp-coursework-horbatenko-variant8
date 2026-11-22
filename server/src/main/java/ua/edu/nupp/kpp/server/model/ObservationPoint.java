package ua.edu.nupp.kpp.server.model;

public class ObservationPoint {
    private Long id;
    private String code;
    private String name;
    private String waterBody;
    private double sensitivityScore;

    public ObservationPoint() {}

    public ObservationPoint(Long id, String code, String name, String waterBody, double sensitivityScore) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.waterBody = waterBody;
        this.sensitivityScore = sensitivityScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWaterBody() { return waterBody; }
    public void setWaterBody(String waterBody) { this.waterBody = waterBody; }
    public double getSensitivityScore() { return sensitivityScore; }
    public void setSensitivityScore(double sensitivityScore) { this.sensitivityScore = sensitivityScore; }
}
