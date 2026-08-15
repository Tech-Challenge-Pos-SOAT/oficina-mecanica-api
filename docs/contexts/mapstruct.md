# MapStruct

<skill>
Conversao entre camadas: **MapStruct** (nunca a mao, nunca `BeanUtils.copyProperties` ou ModelMapper).
Sem Lombok: escreva construtor/getters explicitos ou use `record` para DTO.
</skill>

<config>
| Configuracao | Efeito |
|---|---|
| `defaultComponentModel=spring` | `@Mapper` vira bean injetavel. Nao use `Mappers.getMapper(...)`. |
| `unmappedTargetPolicy=ERROR` | Campo sem origem quebra o build (evita `null` silencioso em prod). |
</config>

<localizacao>
| Mapper | Pacote | Converte |
|---|---|---|
| `*RestMapper` | `interfaces.rest.<context>` | DTO ↔ Command; Dominio → Response |
| `*PersistenceMapper` | `infrastructure.persistence.<context>` | Dominio ↔ Entidade JPA |
**NUNCA**: mapper unico DTO → JPA diretamente (pula dominio, quebra arquitetura).
</localizacao>

<exemplo-ruim>
```java
@Mapper
public interface ServiceOrderMapper {
    ServiceOrderMapper INSTANCE = Mappers.getMapper(ServiceOrderMapper.class);
    ServiceOrderJpaEntity toEntity(OpenServiceOrderRequest request); // DTO → JPA pula dominio
    default BigDecimal calculateTotal(ServiceOrder order) { ... } // regra no mapper
}
```
❌ Mappers.getMapper ignora Spring; DTO direto pra JPA; regra de negocio.
</exemplo-ruim>

<exemplo-bom>
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
</exemplo-bom>

<vo>
VO com construtor validante nao mapeia sozinho. Use `default` no mapper:
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {
    CustomerResponse toResponse(Customer customer);
    
    default String map(Document document) { 
        return document == null ? null : document.value(); 
    }
    
    default Document map(String document) { 
        return document == null ? null : new Document(document); // validacao aqui
    }
}
```
</vo>

<regra>
1. Mapper **so move dado**; logica fica em dominio.
2. Um mapper por contexto e por fronteira.
3. Nao escrever implementacao: `target/generated-sources/annotations` e autogenerad. Nao commitar.
4. `Unmapped target property`? Mapeie ou `@Mapping(target="x", ignore=true)` com motivo. Nao afrouxe politica.
</regra>
