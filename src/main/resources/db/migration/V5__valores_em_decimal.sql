-- Valores em dinheiro passam de DOUBLE para DECIMAL(10,2).
-- DOUBLE é ponto flutuante binário: valores como 0,10 não têm representação exata e as somas
-- acumulam erro de arredondamento. DECIMAL guarda os centavos exatos (BigDecimal no Java).
-- Os valores já gravados (100, 315, 1015...) cabem sem perda: até 99.999.999,99.
-- As constraints CHECK (valor > 0) continuam valendo, porque o nome da coluna não muda.

ALTER TABLE ambientes
    MODIFY valor_hora DECIMAL(10,2) NOT NULL;

ALTER TABLE agendamentos
    MODIFY valor_total DECIMAL(10,2) NOT NULL;
