package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.DiagnosisQuestion;
import soboro.soboro_web.domain.enums.DiagnosisType;
import soboro.soboro_web.service.DiagnosisQuestionService;

@RestController
@RequestMapping("/api/diagnosis/questions")
@RequiredArgsConstructor
public class DiagnosisQuestionController {

    private final DiagnosisQuestionService service;

    @GetMapping("/{type}")
    public Flux<DiagnosisQuestion> getQuestionsByType(@PathVariable DiagnosisType type) {
        return service.getQuestionsByType(type);
    }
}
