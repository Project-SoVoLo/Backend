package soboro.soboro_web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import soboro.soboro_web.handler.UserHandler;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> route(UserHandler userHandler) {
        return RouterFunctions
                .route(RequestPredicates.POST("/users/register"), userHandler::saveUser)
                .andRoute(RequestPredicates.GET("/users/{email}"), userHandler::getUserByEmail);
    }
}
