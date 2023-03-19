package com.mike.springforgraphql.config;

import com.mike.springforgraphql.api.response.ProductPriceHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

@Component
@Slf4j
public class ReactiveReceiver {
    private ConnectableFlux<ProductPriceHistory> reactiveKafkaReceiver;

    private final Queue<ProductPriceHistory> data = new ConcurrentLinkedDeque<>();

    @PostConstruct
    void init() {
        data.add(new ProductPriceHistory(13L, new Date(), 26));
//        reactiveKafkaReceiver = Flux.fromIterable(data)
//                .doOnNext(it -> logger.info("onNext"))
//                .publish();

        reactiveKafkaReceiver = Flux.fromStream(
                        Stream.generate(() -> {

                            sleep(800);

                            while (data.isEmpty()) {
                                sleep(800);
                            }
                            return data.poll();
                        }))
                .doOnSubscribe(it -> log.info("subscibe; " + it))
                .publish();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Flux<ProductPriceHistory> getReceiver() {
        return reactiveKafkaReceiver.autoConnect();
    }

    public void addData(ProductPriceHistory productPriceHistory) {
        data.add(productPriceHistory);
        reactiveKafkaReceiver.connect();
    }
}
