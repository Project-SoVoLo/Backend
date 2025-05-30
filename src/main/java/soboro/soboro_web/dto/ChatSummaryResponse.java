package soboro.soboro_web.dto;

import java.time.LocalDate;

/**************************
* 마이페이지에 띄울 상담 내역 형식
* *************************/
public record ChatSummaryResponse (
    LocalDate date,
    String summary,
    String feedback,
    String emotionKo,
    String colorCode,
    Integer phqScore,
    String googleEmotion
) {}
