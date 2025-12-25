package com.example.strategy.service;

import com.example.RulesApplication;
import com.example.strategy.model.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = RulesApplication.class)
public class StrategyServiceTest {

    @Autowired
    public StrategyService service;

    @Test
    public void testSave() {
        Strategy strategy = service.save(new Strategy(0, "TestStrategy", "{}"));

        assertThat(service.find(strategy.id())).isPresent();
    }
}
