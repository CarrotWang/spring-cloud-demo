package carrot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin.server.EnableZipkinServer;

@SpringBootApplication
@EnableZipkinServer
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
