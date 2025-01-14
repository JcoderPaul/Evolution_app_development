package me.oldboy.config;

import me.oldboy.base_imitation.LikeBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "me.oldboy")
public class AppContextConfig {

    @Bean("likeBase")
    public LikeBase likeBase() {
        return new LikeBase();
    }
}
