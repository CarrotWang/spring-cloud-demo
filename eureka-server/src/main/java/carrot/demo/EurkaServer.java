package carrot.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurkaServer {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EurkaServer.class)
                            .web(true).run(args);
    }
}
