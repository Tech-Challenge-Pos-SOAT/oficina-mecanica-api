-- Tabela do ShedLock (lock distribuido dos jobs agendados). Com varias replicas
-- da API, so quem pegar o lock executa a varredura de orcamentos vencidos.
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_shedlock PRIMARY KEY (name)
);
