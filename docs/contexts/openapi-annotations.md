# OpenAPI 3 + Swagger UI (springdoc-openapi)

Stack: `springdoc-openapi-starter-webmvc-ui:2.x` (Spring Boot 4). Pacotes: `io.swagger.v3.oas.annotations.*`.

URLs:
- UI: `http://localhost:8080/swagger-ui.html`
- JSON (OpenAPI 3.0.3): `http://localhost:8080/v3/api-docs`

<anotacoes_obrigatorias>

- `@Tag` em classe (name, description)
- `@Operation` em metodo (summary, description)
- `@ApiResponses` com todos os status reais
- `@Parameter` em path/query (description, example)
- `@Schema` em todo campo DTO (description, example)

</anotacoes_obrigatorias>

<regras_projeto>

1. **Anotacoes APENAS em `interfaces.rest`** (controller/DTO). **NUNCA** em `domain` (entidade/agregado/use case).
2. **Imports corretos**: sempre `io.swagger.v3.oas.annotations.*`. Se ver `io.swagger.annotations`, é Swagger v2 (errado).
3. **Sem `@SecurityRequirement`**: nao ha autenticacao ativa.
4. **Sem `@Deprecated`** sem motivo (spam).
5. `description` explica regra de negocio, nao repete `summary`.
6. Lista status reais hoje, nao antecipe 401/403.
7. `example` em todo `@Schema` de campo (concreto, ajuda IA testar).
8. Path: ingles kebab-case (`/api/service-orders`).
9. Config global (bean `OpenAPI`) em `infrastructure.config`, uma vez so.

</regras_projeto>

<o_que_documentar>

| Onde | Anotacao | Detalhe |
|---|---|---|
| Classe controller | `@Tag(name, description)` | Agrupar operacoes por dominio. Uma vez por classe. |
| Metodo HTTP | `@Operation(summary, description)` | `description` explica regra de negocio (validacoes, restricoes). |
| Cada resposta HTTP | `@ApiResponse(code, description, @Content(schema=...))` | **SEMPRE com `@Content`** apontando para DTO ou `ErrorMessage`. Incluir 2xx + 4xx reais (400 validacao, 404 nao existe, 409 conflito/negocio). |
| Path/query param | `@Parameter(description, example)` | `example` concreto (ajuda IA testar). |
| Campo DTO | `@Schema(description, example, ...)` | Cada campo deve ter `description` + `example`. |

</o_que_documentar>

<template_minimo>

```java
@Tag(name = "Customers", description = "Cadastro e gestao de clientes")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

  @Operation(summary = "Cria cliente", description = "CPF ou CNPJ unico no sistema. Email opcional e unico.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Cliente criado",
      content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
    @ApiResponse(responseCode = "400", description = "Documento invalido ou email duplicado",
      content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "409", description = "Documento ja existe",
      content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  
  @PostMapping
  public ResponseEntity<CustomerResponse> create(@RequestBody @Valid CreateCustomerRequest req) { }

  @Operation(summary = "Consulta cliente")
  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> findById(
    @Parameter(description = "ID do cliente", example = "123")
    @PathVariable Long id
  ) { }
}

public record CreateCustomerRequest(
  @Schema(description = "CPF ou CNPJ (11 ou 14 digitos)", example = "52998224725")
  @NotBlank String document,
  
  @Schema(description = "Nome completo", example = "Maria Souza")
  @NotBlank String name,
  
  @Schema(description = "Telefone com DDD", example = "11987654321")
  @NotBlank String phone,
  
  @Schema(description = "Email (opcional, unico)", example = "maria@email.com")
  String email
) {}

public record CustomerResponse(
  @Schema(description = "ID", example = "123")
  Long id,
  
  @Schema(description = "Nome", example = "Maria Souza")
  String name,
  
  @Schema(description = "Documento", example = "529.982.247-25")
  String document,
  
  @Schema(description = "Status", example = "ACTIVE")
  String status
) {}
```

</template_minimo>

<padrao_errormessage>

Crie classe unica em `interfaces.rest.exception` para documentar todos os erros:

```java
public record ErrorMessage(
  @Schema(description = "Codigo HTTP", example = "404")
  int status,

  @Schema(description = "Mensagem de erro", example = "Veiculo nao encontrado")
  String message,

  @Schema(description = "Timestamp ISO", example = "2025-01-15T10:30:00Z")
  String timestamp
) {}
```

Dai toda `@ApiResponse` com erro usa `@Schema(implementation = ErrorMessage.class)`. Swagger renderiza o mesmo schema.

</padrao_errormessage>

<troubleshooting>

| Sintoma | Causa | Fix |
|---|---|---|
| Endpoint nao aparece | Falta `@RestController` ou `@Tag` | Adicionar anotacoes obrigatorias |
| Campo invisivel no schema | Falta `@Schema` | Adicionar em todo campo DTO |
| "Can't resolve reference" no JSON | Import errado (v2 vs v3) | Verificar: `io.swagger.v3.*` |
| Validacoes nao aparecem (200 OK sempre) | Falta `@ApiResponses` com erros | Adicionar todos os status reais |
| Boot lento | springdoc varre classpath inteiro | Normal no primeiro boot. Depois cached. |

</troubleshooting>

<referencias>

- OpenAPI 3.0.3 spec: https://spec.openapis.org/oas/v3.0.3
- springdoc docs: https://springdoc.org/

</referencias>
