# Princípios de Código

Referência para design e implementação.

## DDD — Domain-Driven Design

Código organizado por domínio (bounded contexts), não por camada técnica.

**Estrutura:**
```
com.postech.oficinamecanica
├── customer/
│   ├── domain/        (entidades, VOs, repositórios interface)
│   ├── application/   (use cases)
│   └── infra/         (JPA, repositories impl, mappers)
├── serviceorder/
├── vehicle/
└── ...
```

**Regras:**
- Entidade = raiz do agregado. Contém lógica de negócio.
- Value Object = imutável, sem ID. Encapsula regra.
- Repositório = interface no domain, implementação no infra.
- Use case = orquestra domínio (aplicação).

Ver `docs/context/arquitetura-ddd.md` para layout completo.

## DRY — Don't Repeat Yourself

Não duplicar código. Extrair padrão em método/classe/VO reutilizável.

**Quando NÃO aplicar:**
- Acaso: dois métodos parecidos mas com propósitos diferentes = deixa.
- Prematuramente: espera 3 repetições antes de abstrair.

**Como aplicar:**
- Validação repetida → método privado na entidade ou VO.
- Mapeamento repetido → MapStruct mapper.
- Query repetida → repository method.

## KISS — Keep It Simple, Stupid

Código mais simples que funciona. Nenhuma engenharia especulativa.

**Regras:**
- Sem abstrações pra "futuro" (interfaces com 1 implementação).
- Sem config desnecessária (valores constantes, não XML).
- Sem padrão só porque "é bom design" (Factory com 1 produto = desperdício).
- Métodos pequenos, nomes claros, sem surpresas.

**Quando complexidade é justificada:**
- Negócio complexo (validação de status, regras de precificação).
- Performance crítica.
- Testabilidade (injeção de dependência OK).

## CLEAN Code

Código legível, mantível, sem "surpresas".

**Nomes:**
- Método/variável: ação + objeto (`calculateServicePrice`, `customerEmail`).
- Sem siglas inventadas (`cfg` → `config`, `impl` → `implementation`).
- Booleano começa com `is`, `has`, `should` (`isActive`, `hasEmail`).

**Funções:**
- Uma responsabilidade. Se tem `and`, talvez mereça split.
- 3-10 linhas ideal; 20+ é sinal de alerta.
- Argumentos: ≤3; mais que isso → VO/DTO.

**Comentários:**
- Só quando **por quê** não é óbvio.
- Nada de "get customer" (código já diz isso).
- Marcar hacks/workarounds com `// TODO:` ou `// FIXME:`.

**Tratamento de erro:**
- Falhar cedo (validar entrada).
- Exception meaningful (não `Exception`, use `CustomerNotFoundException`).
- Nunca engolir erro silenciosamente.

## SOLID

**S — Single Responsibility**
Classe tem 1 razão pra mudar. `CustomerService` cuida de customer; pricing vai pra `PricingService`.

**O — Open/Closed**
Aberto pra extensão, fechado pra modificação. Use interface quando há variação.

**L — Liskov Substitution**
Subclasse pode substituir superclasse sem quebrar código.

**I — Interface Segregation**
Cliente não depende de método que não usa. Interface pequena > grande.

**D — Dependency Inversion**
Depende de abstração, não de implementação concreta. Spring `@Autowired` cuida disso.

---

**Resumo:** DDD pra arquitetura (domínio), DRY pra reutilização, KISS pra simplicidade, CLEAN pra legibilidade, SOLID pra manutenção. Todos caminham juntos — não há trade-off aqui.

Ver `docs/context/arquitetura-ddd.md` para estrutura concreta do projeto.
