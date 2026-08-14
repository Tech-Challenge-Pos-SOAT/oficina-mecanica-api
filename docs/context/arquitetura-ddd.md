# Arquitetura DDD

Responde: "onde essa classe mora e o que ela pode importar?"

<skill>
Todo identificador: ingles. Nomes de `modelo-de-dados.md`.
A estrutura segue **uma fatia end-to-end**: `Customer` cruza todas as camadas.
</skill>

<mapa-pacotes>
```
com.postech.oficinamecanica        ← raiz em portugues, nao renomear
├── domain                          # entidades, agregados, VOs, regras. SEM Spring.
│   ├── customer
│   ├── vehicle
│   ├── material
│   ├── service
│   ├── employee
│   ├── serviceorder                # agregado central
│   └── shared                       # so compartilhado (EntityStatus)
├── application                     # use cases, portas (interfaces repository)
├── infrastructure                  # JPA, config
└── interfaces.rest                 # controllers, DTOs, mappers
```

`domain/shared` = tipo genuinamente compartilhado (hoje `EntityStatus`). **Nao** e deposito sem dono.
</mapa-pacotes>

<direcao-dependencia>
```
interfaces.rest ---> application ---> domain
                          ^              ^
                          |              |
                    infrastructure ------+
```

**Regras imutaveis:**
- `domain` importa NADA: nem Spring, nem JPA, nem DTO, nem outra camada.
- `application` importa `domain`. Nunca `infrastructure` nem `interfaces`.
- `infrastructure` implementa interfaces de `application`.
- `interfaces.rest` importa `application`. Nunca `infrastructure`.

Teste mental: apagando `infrastructure`, `domain` e `application` ainda compilam?
</direcao-dependencia>

<fatia-customer>
Arquivos por camada para `Customer` (crud mais simples, mesmo assim completo):

**domain/shared**
- `EntityStatus.java` — enum ACTIVE/INACTIVE

**domain/customer**
- `Customer.java` — entidade, regras
- `Document.java` — VO: CPF ou CNPJ
- `InvalidDocumentException.java`
- `DuplicateDocumentException.java`
- `CustomerAlreadyInactiveException.java`

**application/customer**
- `CreateCustomerUseCase.java` — orquestra
- `CreateCustomerCommand.java` — entrada do use case (nao DTO HTTP)
- `CustomerRepository.java` — PORTA: interface, sem Spring Data

**infrastructure/persistence/customer**
- `CustomerJpaEntity.java` — @Entity
- `CustomerJpaRepository.java` — extends JpaRepository
- `CustomerRepositoryImpl.java` — implementa porta
- `CustomerPersistenceMapper.java` — dominio ↔ JPA

**interfaces/rest/customer**
- `CustomerController.java` — POST /api/customers
- `CreateCustomerRequest.java` — DTO entrada (record + Bean Validation)
- `CustomerResponse.java` — DTO saida
- `CustomerRestMapper.java` — DTO ↔ Command; dominio → response

Padrao: copiar pra qualquer outra entidade.
</fatia-customer>
