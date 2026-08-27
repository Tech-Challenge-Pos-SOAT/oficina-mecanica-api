-- Funcionário inicial para permitir o primeiro login (bootstrap).
-- Senha em texto puro: "senha123" (mesmo exemplo usado na documentação de autenticação).
-- Hash gerado com bcrypt, custo 10, compatível com o BCryptPasswordEncoder do Spring Security.
INSERT INTO employee (name, email, password, role, status) VALUES
('Carlos Souza', 'carlos.souza@oficina.com', '$2b$10$7HuVLjVQ1TpGfhSOyMNNR.xZ14Nf7dHegJ87fjRu.fdANRjW9hf9C', 'ATTENDANT', 'ACTIVE');
