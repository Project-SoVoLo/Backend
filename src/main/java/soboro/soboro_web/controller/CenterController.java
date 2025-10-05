package soboro.soboro_web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import soboro.soboro_web.dto.CenterQuery;
import soboro.soboro_web.dto.CenterResponse;
import soboro.soboro_web.service.CenterSearchService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/center")
public class CenterController {

    private final CenterSearchService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CenterResponse> getCenters(@Valid @ModelAttribute CenterQuery q) {
        return service.search(q.getLat(), q.getLng(), q.getRadiusM());
    }

}
