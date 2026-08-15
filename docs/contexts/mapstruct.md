# MapStruct

## Princípio

Conversão entre camadas: **MapStruct** (nunca a mão, nunca `BeanUtils.copyProperties` ou ModelMapper).
Sem Lombok: escreva construtor/getters explícitos ou use `record` para DTO.

## Configuração

| Configuração | Efeito |
|---|---|
| `componentModel="spring"` | `@Mapper` vira bean injetável. Não use `Mappers.getMapper(...)`. |
| `unmappedTargetPolicy=ReportingPolicy.ERROR` | Campo sem origem quebra o build (evita `null` silencioso em prod). |

## Localização e Responsabilidade

| Mapper | Pacote | Converte |
|---|---|---|
| `*RestMapper` | `interfaces.rest.<context>` | DTO ↔ Command; Domínio → Response |
| `*PersistenceMapper` | `infrastructure.persistence.<context>` | Domínio ↔ Entidade JPA |

**NUNCA**: mapper único DTO → JPA diretamente (pula domínio, quebra arquitetura).

```java
@Mapper
public interface ServiceOrderMapper {
    ServiceOrderMapper INSTANCE = Mappers.getMapper(ServiceOrderMapper.class);
    ServiceOrderJpaEntity toEntity(OpenServiceOrderRequest request); // DTO → JPA pula dominio
    default BigDecimal calculateTotal(ServiceOrder order) { ... } // regra no mapper
}
```

❌ Mappers.getMapper ignora Spring; DTO direto pra JPA; regra de negócio.

## Exemplo Bom
```java
// interfaces.rest.customer
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {
    CreateCustomerCommand toCommand(CreateCustomerRequest request);
    @Mapping(target = "document", source = "document.value")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    CustomerResponse toResponse(Customer domain);
}

// infrastructure.persistence.customer
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "document", source = "document.value")
    CustomerJpaEntity toEntity(Customer domain);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "document", source = "document")
    Customer toDomain(CustomerJpaEntity entity);
}

// Injeção
public CustomerController(CreateCustomerUseCase useCase, CustomerRestMapper mapper) { ... }
```

Bean Spring; dois mappers (um por fronteira); Command entre DTO e use case.

## Value Objects com Construtor Validante

VO com construtor validante não mapeia sozinho. Use `default` no mapper:

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {
    CustomerResponse toResponse(Customer customer);
    
    default String map(Document document) { 
        return document == null ? null : document.value(); 
    }
    
    default Document map(String document) { 
        return document == null ? null : new Document(document); // validação aqui
    }
}
```

## Regras

1. Mapper **só move dado**; lógica fica em domínio.
2. Um mapper por contexto e por fronteira.
3. Não escrever implementação: `target/generated-sources/annotations` é autogenerado. Não commitar.
4. `Unmapped target property`? Mapeie ou `@Mapping(target="x", ignore=true)` com motivo. Não afrouxe política.
