package soboro.soboro_web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class HomeRouter {
    @Bean
    public RouterFunction<ServerResponse> homeRoute(){
        return RouterFunctions.route(
                GET("/home"),
                request -> ServerResponse.ok().bodyValue("홈 화면입니다")
        );
    }

}
