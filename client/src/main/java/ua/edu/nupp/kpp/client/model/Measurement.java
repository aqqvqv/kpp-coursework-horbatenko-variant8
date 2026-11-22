package ua.edu.nupp.kpp.client.model;

public class Measurement {
    private Long id;
    private Long sampleId;
    private String parameterName;
    private double measuredValue;
    private double normValue;
    private String unit;

    public Measurement() {}

    public Measurement(Long id, Long sampleId, String parameterName,
                       double measuredValue, double normValue, String unit) {
        this.id = id;
        this.sampleId = sampleId;
        this.parameterName = parameterName;
        this.measuredValue = measuredValue;
        this.normValue = normValue;
        this.unit = unit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public double getMeasuredValue() { return measuredValue; }
    public void setMeasuredValue(double measuredValue) { this.measuredValue = measuredValue; }
    public double getNormValue() { return normValue; }
    public void setNormValue(double normValue) { this.normValue = normValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
