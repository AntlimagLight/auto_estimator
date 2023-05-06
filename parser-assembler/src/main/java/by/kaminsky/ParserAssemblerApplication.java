package by.kaminsky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParserAssemblerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParserAssemblerApplication.class);
    }


}