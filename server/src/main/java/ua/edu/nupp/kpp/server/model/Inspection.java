package ua.edu.nupp.kpp.server.model;

public class Inspection {
    private Long id;
    private Long sampleId;
    private String inspectorName;
    private String inspectionDate;
    private String conclusion;

    public Inspection() {}

    public Inspection(Long id, Long sampleId, String inspectorName, String inspectionDate, String conclusion) {
        this.id = id;
        this.sampleId = sampleId;
        this.inspectorName = inspectorName;
        this.inspectionDate = inspectionDate;
        this.conclusion = conclusion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public String getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
}
