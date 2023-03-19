package com.mike.springforgraphql.kafka;

import com.mike.springforgraphql.api.response.ProductPriceHistory;
import com.mike.springforgraphql.config.ReactiveReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class KafkaConsumer {
    @Autowired
    private ReactiveReceiver reactiveReceiver;

    @KafkaListener(topics = "data-topic", groupId = "group_id")
    public void consume(String message) {
        reactiveReceiver.addData(new ProductPriceHistory(1L, new Date(), message.length()));

        log.info("message = " + message);
    }
}
