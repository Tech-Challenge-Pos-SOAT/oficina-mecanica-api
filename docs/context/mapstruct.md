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
// interfaces.rest.serviceorder
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderRestMapper {
    OpenServiceOrderCommand toCommand(OpenServiceOrderRequest request);
    @Mapping(target = "plate", source = "vehicle.plate.value")
    @Mapping(target = "totalPrice", source = "price")
    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}

// infrastructure.persistence.serviceorder
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderPersistenceMapper {
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    ServiceOrderJpaEntity toEntity(ServiceOrder serviceOrder);
    ServiceOrder toDomain(ServiceOrderJpaEntity entity);
}

// Injecao
public ServiceOrderController(OpenServiceOrderUseCase useCase, ServiceOrderRestMapper mapper) { ... }
```
Bean Spring; dois mappers (um por fronteira); Command entre DTO e use case.
</exemplo-bom>

<vo>
VO com construtor validante nao mapeia sozinho. Use `default` no mapper:
```java
@Mapper
public interface CustomerRestMapper {
    CustomerResponse toResponse(Customer customer);
    default String map(Document document) { 
        return document == null ? null : document.formatted(); 
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
