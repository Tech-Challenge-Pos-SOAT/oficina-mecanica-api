# Swagger (springdoc-openapi)

UI: `http://localhost:8080/swagger-ui.html` | JSON: `http://localhost:8080/v3/api-docs`

<anotacoes_obrigatorias>

- `@Tag` em classe (name, description)
- `@Operation` em metodo (summary, description)
- `@ApiResponses` com todos os status reais
- `@Parameter` em path/query (description, example)
- `@Schema` em todo campo DTO (description, example)

</anotacoes_obrigatorias>

Pacote: `io.swagger.v3.oas.annotations.*` (OpenAPI 3, nao Swagger 2). Sem `@SecurityRequirement` (sem JWT ativo).

<regras>

1. `description` explica regra de negocio, nao repete `summary`
2. Lista status reais hoje, nao antecipe 401/403
3. `example` em todo `@Schema` de campo
4. Anotacoes Swagger **nunca** em `domain` — so em DTO de `interfaces.rest`
5. Path: ingles kebab-case (`/api/service-orders`)
6. Config global (bean `OpenAPI`) em `infrastructure.config`, uma vez so

</regras>

<exemplo>

```java
@Tag(name = "Service Orders", description = "Abertura e acompanhamento da ordem de servico")
@RestController
@RequestMapping("/api/service-orders")
public class ServiceOrderController {

    @Operation(summary = "Abre ordem", description = "Registra entrada com status RECEIVED. Veiculo deve estar cadastrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ordem aberta"),
        @ApiResponse(responseCode = "400", description = "Payload invalido"),
        @ApiResponse(responseCode = "404", description = "Veiculo nao encontrado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse open(@RequestBody @Valid OpenServiceOrderRequest request) { ... }

    @Operation(summary = "Consulta ordem por id")
    @GetMapping("/{id}")
    public ServiceOrderResponse byId(
        @Parameter(description = "ID da ordem", example = "42")
        @PathVariable Long id) { ... }
}

@Schema(description = "Dados para abertura de ordem de servico")
public record OpenServiceOrderRequest(
    @Schema(description = "Placa (antigo ou Mercosul)", example = "ABC1D23")
    @NotBlank String plate,
    @Schema(description = "Problema relatado", example = "Barulho no freio")
    @NotBlank @Size(max = 500) String reportedIssue
) {}
```

</exemplo>
