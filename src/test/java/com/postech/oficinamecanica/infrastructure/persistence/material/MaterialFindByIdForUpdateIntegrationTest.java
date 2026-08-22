package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.docker.compose.enabled=false"
})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MaterialFindByIdForUpdateIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MaterialJpaRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate template;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Test
    void shouldBlockSecondReaderUntilFirstCommitsAndSerializeDebits() throws Exception {
        Long materialId = repository.save(aMaterial(10)).getId();

        CountDownLatch rowLocked = new CountDownLatch(1);
        CountDownLatch releaseRow = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> holder = executor.submit(() -> template.executeWithoutResult(status -> {
                repository.findByIdForUpdate(materialId).orElseThrow();
                rowLocked.countDown();
                awaitOrThrow(releaseRow);
                debit(repository, materialId, 3);
            }));

            assertThat(rowLocked.await(10, TimeUnit.SECONDS)).isTrue();

            Future<?> contender = executor.submit(() -> template.executeWithoutResult(status ->
                    debit(repository, materialId, 4)));

            boolean finishedWhileLockHeld = waitForCompletion(contender, 1000);
            assertThat(finishedWhileLockHeld)
                    .as("contender must block on FOR UPDATE while the holder keeps its transaction open")
                    .isFalse();

            releaseRow.countDown();
            holder.get(10, TimeUnit.SECONDS);
            contender.get(10, TimeUnit.SECONDS);

            Integer finalStock = template.execute(status ->
                    repository.findById(materialId).orElseThrow().getStockQuantity());

            assertThat(finalStock).isEqualTo(3);
        } finally {
            releaseRow.countDown();
            executor.shutdownNow();
        }
    }

    private void debit(MaterialJpaRepository jpaRepository, Long id, int amount) {
        var material = jpaRepository.findByIdForUpdate(id).orElseThrow();
        material.setStockQuantity(material.getStockQuantity() - amount);
    }

    private void awaitOrThrow(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Latch was not released in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private boolean waitForCompletion(Future<?> future, long timeoutMillis) {
        try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private MaterialJpaEntity aMaterial(int stockQuantity) {
        MaterialJpaEntity material = new MaterialJpaEntity();
        material.setName("Spark Plug");
        material.setDescription("NGK Premium");
        material.setPrice(new BigDecimal("45.00"));
        material.setStockQuantity(stockQuantity);
        material.setStockMinimum(2);
        material.setStatus(EntityStatus.ACTIVE);
        material.setCreatedAt(Instant.now());
        material.setUpdatedAt(Instant.now());
        return material;
    }
}
