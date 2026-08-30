-- ========================================================================
-- Migration: ajustes minimos para o fluxo da Ordem de Servico
--
-- 1. service_order_history.price nasce NOT NULL no V1, mas a OS e aberta sem
--    orcamento (RECEIVED) - o primeiro registro de historico nao tem preco.
-- 2. observation: a linguagem ubiqua ja previa "campo observacao para motivo
--    de encerramento" (recusa do cliente, impossibilidade de execucao); o V1
--    nao criou a coluna.
-- 3. stock_debited: reparo adicional faz a mesma OS voltar para aprovacao;
--    sem essa marca, a segunda aprovacao daria baixa de novo nos materiais
--    que ja sairam do estoque.
-- ========================================================================
ALTER TABLE service_order_history ALTER COLUMN price DROP NOT NULL;
ALTER TABLE service_order_history ADD COLUMN observation VARCHAR(500);

ALTER TABLE service_order_material ADD COLUMN stock_debited BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_so_material_material ON service_order_material(material_id);
CREATE INDEX idx_so_service_service ON service_order_service(service_id);
CREATE INDEX idx_mt_service_order ON material_transaction(service_order_id);
