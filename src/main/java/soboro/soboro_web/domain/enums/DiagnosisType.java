package soboro.soboro_web.domain.enums;

public enum DiagnosisType {
    DEPRESSION("우울증"),
    ANXIETY("불안"),
    EARLY_PSYCHOSIS("조기정신증"),
    BIPOLAR("조울증"),
    STRESS("스트레스"),
    INSOMNIA("불면"),
    ALCOHOL("알코올중독"),
    DEVICE_ADDICTION("스마트기기사용장애");

    private final String description;

    DiagnosisType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
