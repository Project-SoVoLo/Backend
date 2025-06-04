package soboro.soboro_web.domain.enums;

public enum EmotionTypes {
    POSITIVE("긍정", "#2196F3"),
    NEUTRAL("중립", "9E9E9E"),
    NEGATIVE("부정", "F44336");

    private final String korean;
    private final String colorCode;

    EmotionTypes(String korean, String colorCode) {
        this.korean = korean;
        this.colorCode = colorCode;
    }

    // 마이페이지에 표시할 땐 긍정/중립/부정 한국어로 나타냄
    public String getKorean() {
        return korean;
    }

    public String getColorCode() {
        return colorCode;
    }
}