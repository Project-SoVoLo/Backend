package soboro.soboro_web.controller;

import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.scheduler.Schedulers;
import soboro.soboro_web.dto.ChatResponse;
import soboro.soboro_web.dto.ChatRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/***********************
* ChatGPT API 사용 컨트롤러
* **********************/

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatgpt")
public class ChatGPTController {
    private final OpenAiChatModel openAiChatModel;

    // ChatGPT 대화 요청
    @PostMapping("/ask")
    public Mono<ChatResponse> ask(@RequestBody Mono<ChatRequest> requestMono) {
        return requestMono
                .map(request -> new Prompt(new UserMessage(request.getMessage())))
                .flatMap(prompt -> Mono.fromCallable(() -> openAiChatModel.call(prompt))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnNext(resp -> System.out.println("🧪 GPT Response: " + resp))
                .map(response -> new ChatResponse(
                        response.getResult().getOutput().getText()
                ));
    }


}
