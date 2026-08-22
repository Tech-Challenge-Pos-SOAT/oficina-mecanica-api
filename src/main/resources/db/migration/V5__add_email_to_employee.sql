-- ========================================================================
-- Migration: Add email column to employee
-- Requerido pela regra de cadastro de funcionarios: email obrigatorio e
-- unico no sistema. Tabela employee (V1) ainda nao tinha essa coluna.
-- ========================================================================
ALTER TABLE employee ADD COLUMN email VARCHAR(255) NOT NULL UNIQUE;
