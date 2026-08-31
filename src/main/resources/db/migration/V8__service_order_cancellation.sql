-- ========================================================================
-- Migration: cancelamento da OS e ciclo do item de servico
--
-- 1. approved: espelha o stock_debited do material. Marca o servico que o
--    cliente ja autorizou, para o descarte de um reparo adicional recusado
--    saber quais itens tirar do orcamento.
-- 2. O status CANCELLED nao exige alteracao: service_order.status ja e'
--    VARCHAR(30) sem constraint de valores.
-- ========================================================================
ALTER TABLE service_order_service ADD COLUMN approved BOOLEAN NOT NULL DEFAULT FALSE;

-- Ordens que ja estao em execucao, finalizadas ou entregues tiveram seus
-- servicos aprovados pelo cliente antes desta migration.
UPDATE service_order_service sos
   SET approved = TRUE
  FROM service_order so
 WHERE so.id = sos.service_order_id
   AND so.status IN ('IN_EXECUTION', 'FINISHED', 'DELIVERED');
