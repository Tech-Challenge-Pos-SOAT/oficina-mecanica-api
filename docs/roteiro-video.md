# Roteiro do video (ate 15 minutos)

O enunciado pede um video mostrando a arquitetura, a infraestrutura criada, o
pipeline e a aplicacao funcionando. A ordem abaixo cabe em cerca de dez minutos
e conta uma historia continua: do desenho ate a prova de que escala.

## Antes de gravar

Deixe tudo aberto em abas: o repositorio no GitHub, o diagrama de
`docs/arquitetura.md`, o console do Google Cloud (GKE, Cloud SQL, Artifact
Registry), o Swagger da aplicacao, o Insomnia com a colecao importada e dois
terminais (um para `kubectl`, outro para o `k6`). Faca um `terraform apply`
limpo antes, para o ambiente estar quente, e nao mostre senhas nem tokens na
tela (esconda a aba de variaveis e secrets do GitHub).

## Sequencia

Nos primeiros dois minutos, apresente o problema da oficina e o diagrama de
arquitetura, explicando o caminho de uma requisicao do cliente ate o banco e as
decisoes principais (Autopilot, Cloud SQL com proxy, LoadBalancer, HPA).
Nos dois minutos seguintes, mostre o Terraform em `infra/` e o console do
Google Cloud com o cluster, a instancia do Cloud SQL e o repositorio de imagens,
destacando que tudo nasceu de codigo e que o estado esta no bucket.
Depois, mostre o pipeline: um commit na `main`, o CI passando com seus estagios
de qualidade e seguranca, e o CD publicando a imagem e fazendo o rollout.
Na sequencia, mostre o cluster com `kubectl -n oficina get pods,svc,hpa`, as
probes no Deployment e o IP publico respondendo em `/actuator/health`.
Em seguida, percorra o fluxo de negocio pelo Insomnia: abrir a OS ja com servicos
e pecas, listar a fila por prioridade, enviar o orcamento, aprovar como cliente
na rota publica, finalizar e entregar, e mostrar o e-mail chegando.
Por fim, rode o `k6` e deixe `kubectl -n oficina get hpa -w` visivel, mostrando a
CPU subindo e as replicas passando de 2 para mais; encerre mostrando a descida
depois da carga e um resumo do que foi entregue.

## Dicas

Corte as esperas (criacao de cluster, rollout) com edicao, grave em 1080p e fale
sobre o motivo de cada decisao, nao apenas o que esta na tela. Lembre de
compartilhar o repositorio com o usuario `soat-architecture` e anexar o PDF com
os links do repositorio e do video na plataforma.
