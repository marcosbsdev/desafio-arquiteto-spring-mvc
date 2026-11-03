-- ============================================================================
-- Migration V2: Carga inicial de clientes para parceiros B2B
-- ============================================================================

INSERT INTO tb_clientes (nome, email, telefone, status)
VALUES
    ('TechCorp Brasil', 'contato@techcorp.com.br', '(11) 98765-4321', 'ATIVO'),
    ('Logística Express', 'operacoes@logexpress.com', '(21) 97654-3210', 'ATIVO'),
    ('Alpha Varejo Online', 'comercial@alphavarejo.com', '(31) 96543-2109', 'ATIVO'),
    ('Global Soluções Digitais', 'suporte@globalsolucoes.com', '(41) 95432-1098', 'INATIVO');
