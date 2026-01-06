package vn.hoidanit.jobhunter.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class SocketIOConfig {
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setPort(8099);
        configuration.setOrigin("*");

        return new SocketIOServer(configuration);
    }
}
