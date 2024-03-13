package com.fastcampus.flow.service;

import com.fastcampus.flow.EmbeddedRedis;
import com.fastcampus.flow.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueServiceTest {
    @Autowired
    private UserQueService  userQueService;
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection redisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        redisConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueService.registerWaitQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(userQueService.registerWaitQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueService.registerWaitQueue("default", 100L))
                .expectError(ApplicationException.class)
                .verify();
    }

    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueService.allowUser("default", 3L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void allowUser() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.registerWaitQueue("default", 101L))
                .then(userQueService.registerWaitQueue("default", 102L))
                .then(userQueService.allowUser("default", 2L)))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.registerWaitQueue("default", 101L))
                .then(userQueService.registerWaitQueue("default", 102L))
                .then(userQueService.allowUser("default", 5L)))
                .expectNext(3L)
                .verifyComplete();

    }

    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.registerWaitQueue("default", 101L))
                .then(userQueService.registerWaitQueue("default", 102L))
                .then(userQueService.allowUser("default", 3L))
                .then(userQueService.registerWaitQueue("default", 200L)))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void isNotAllowed() {
        StepVerifier.create(userQueService.isAllowed("default", 100L))
                .expectNext(false)
                .verifyComplete();
    }
    @Test
    void isNotAllowed2() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.allowUser("default", 3L))
                .then(userQueService.isAllowed("default", 101L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.allowUser("default", 3L))
                .then(userQueService.isAllowed("default", 100L)))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void getRank() {
        StepVerifier.create(userQueService.registerWaitQueue("default", 100L)
                .then(userQueService.getRank("default", 100L)))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueService.registerWaitQueue("default", 101L)
                        .then(userQueService.getRank("default", 101L)))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void emptyRank() {
        StepVerifier.create(userQueService.getRank("default", 100L))
                .expectNext(-1L)
                .verifyComplete();
    }

}