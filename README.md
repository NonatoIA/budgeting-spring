# 💰 Budgeting AI: Assistente Financeiro por Voz com Spring AI

Projeto desenvolvido no **Desafio de Projeto de Spring AI** da DIO, a partir do projeto final da trilha [dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai), evoluído com correções e novas funcionalidades.

## 📌 O que o projeto faz

É uma API de orçamento pessoal que entende comandos de voz. A pessoa envia um áudio como *"gastei 35 reais na farmácia"* e a aplicação:

1. **Transcreve** o áudio em texto (Whisper);
2. Envia o texto para o **modelo de linguagem** (GPT-4o mini) via `ChatClient`;
3. O modelo escolhe e executa uma **ferramenta real** da aplicação (*Tool Calling*): registrar um gasto, listar por categoria ou resumir os gastos;
4. Os casos de uso **salvam ou consultam** as transações no banco de dados;
5. A resposta final é convertida em **áudio MP3** e devolvida para a pessoa.

A IA não acessa o banco diretamente: ela só chama os casos de uso da camada de aplicação, que aplicam as regras de negócio do domínio.

```
Áudio ─► Transcrição ─► ChatClient + Tools ─► Casos de uso ─► Banco
                                   │
Áudio MP3 ◄── Texto para voz ◄─────┘ resposta final
```

## ✨ Melhorias que implementei

| Melhoria | O que foi feito |
|---|---|
| 🐛 **Correção de bug nos valores** | O valor é salvo em centavos, mas era devolvido sem dividir por 100: um gasto de R$ 35,00 aparecia como `3500.0`, e a IA podia "falar" o valor errado. Agora as respostas trazem o valor em reais (`35.0`). |
| 📊 **Nova consulta financeira** | Caso de uso `SummarizeTransactionsUseCase`, com total geral e total por categoria, ordenado do maior para o menor gasto. Disponível no endpoint `GET /transactions/summary`. |
| 🛠️ **Nova ferramenta para Tool Calling** | O resumo também foi registrado como ferramenta (`summarize-transactions`). Agora dá para perguntar por voz *"em qual categoria eu gastei mais?"*. |
| ✅ **Validações antes de salvar** | O domínio (`Transaction`) rejeita descrição vazia, valor menor ou igual a zero, valor acima de R$ 100.000,00 (proteção contra erro de conversão da IA) e categoria ausente. Pelo REST, retorna `400`; pela IA, o modelo recebe o erro e explica o problema para a pessoa. |
| 💬 **Endpoint de texto** | `POST /transactions/ai/text` executa o mesmo fluxo de IA sem áudio, o que facilita os testes e reduz custos. |
| 🗂️ **Novas categorias** | Além de `GROCERIES`, `PHARMA` e `AUTO`, foram adicionadas `RESTAURANT`, `ENTERTAINMENT` e `OTHER`. |
| 🧠 **Prompt de sistema melhorado** | Instruções para converter reais em centavos, escolher a categoria certa, não inventar dados, pedir informações que faltam e responder em frases curtas, adequadas para áudio. |
| 🚨 **Erros claros** | `@RestControllerAdvice` no padrão *Problem Details* (RFC 9457): categoria inválida, JSON inválido, áudio grande demais e IA indisponível (`503`, com orientação sobre a chave da OpenAI). |
| ⚙️ **Facilidade para rodar** | A aplicação sobe mesmo sem a chave da OpenAI (os endpoints REST funcionam); perfil `h2` para rodar sem Docker; download automático do JDK 25 pelo Gradle; limite de upload de áudio aumentado de 1 MB para 25 MB. |
| 🧪 **Testes unitários** | Testes do domínio e dos casos de uso com um repositório em memória, sem precisar de banco nem da OpenAI. |
| 🏗️ **Organização** | A lógica de IA saiu do controller para a classe `FinancialAssistant`, deixando o controller responsável apenas pelo HTTP. |

## 🛠️ Tecnologias

- Java 25 e Spring Boot 4
- Spring AI 2.0 (`ChatClient`, Tool Calling, `TranscriptionModel`, `TextToSpeechModel`)
- OpenAI: `gpt-4o-mini` (chat), `whisper-1` (transcrição) e `gpt-4o-mini-tts` (voz)
- Spring Web, Spring Data JPA, MySQL (Docker Compose) e H2
- JUnit 5 e Gradle

## 🏛️ Arquitetura

```
src/main/java/dio/budgeting
├── domain/          → Transaction (com validações), Category, TransactionRepository
├── application/     → Casos de uso, que também são as ferramentas da IA (@Tool)
│   ├── PersistTransactionUseCase
│   ├── ListTransactionsByCategoryUseCase
│   └── SummarizeTransactionsUseCase   ← novo
└── infrastructure/
    ├── ai/          → FinancialAssistant (transcrição, chat e voz)   ← novo
    ├── http/        → TransactionController e ApiExceptionHandler    ← novo
    └── persistence/ → Entidade e repositório JPA
```

## ▶️ Como executar

**Pré-requisitos:** JDK 17 ou superior para iniciar o Gradle (o JDK 25 é baixado automaticamente) e, opcionalmente, Docker e uma chave da API da OpenAI com créditos.

### 1. Configure a chave da OpenAI (necessária só para os endpoints de IA)

```bash
# Linux / macOS
export OPENAI_API_KEY="sua_chave_aqui"
```

```powershell
# Windows (PowerShell)
$env:OPENAI_API_KEY="sua_chave_aqui"
```

### 2. Suba a aplicação

**Opção A: com Docker (MySQL).** O Spring Boot sobe o banco automaticamente pelo `compose.yml`:

```bash
./gradlew bootRun          # Linux / macOS
gradlew.bat bootRun        # Windows
```

**Opção B: sem Docker (banco H2 em memória).** Os dados são apagados ao reiniciar:

```bash
./gradlew bootRun --args='--spring.profiles.active=h2'      # Linux / macOS
gradlew.bat bootRun --args="--spring.profiles.active=h2"    # Windows
```

A API fica disponível em `http://localhost:8080`.

### 3. Rode os testes

```bash
./gradlew test
```

Os testes unitários e o `contextLoads` rodam sem Docker e sem chave. Os testes de integração com a OpenAI (`*IT`) só executam quando a variável `OPENAI_API_KEY` está definida.

## 🧪 Como testar o fluxo principal

O arquivo [`requests.http`](requests.http) tem todos os exemplos prontos para executar no IntelliJ ou no VS Code (extensão REST Client). Também é possível usar `curl`:

**Comando por voz (fluxo completo):** envia um áudio e salva a resposta falada. Há áudios de exemplo em `src/test/resources/audio`.

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta.mp3
```

**Comando por texto:**

```bash
curl -X POST http://localhost:8080/transactions/ai/text \
  -H "Content-Type: application/json" \
  -d '{"message": "Gastei 42 reais e 90 centavos em uma pizza"}'
```

```json
{
  "message": "Gastei 42 reais e 90 centavos em uma pizza",
  "response": "Registrei o gasto de 42 reais e 90 centavos com pizza na categoria restaurante."
}
```

**Resumo de gastos (nova consulta):**

```bash
curl http://localhost:8080/transactions/summary
```

```json
{
  "transactionCount": 3,
  "total": 278.4,
  "categories": [
    { "category": "AUTO", "transactionCount": 1, "total": 200.0 },
    { "category": "RESTAURANT", "transactionCount": 1, "total": 42.9 },
    { "category": "GROCERIES", "transactionCount": 1, "total": 35.5 }
  ]
}
```

**Validação:**

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "Teste", "category": "GROCERIES", "amount": 0}'
```

```json
{
  "type": "about:blank",
  "title": "Transação inválida",
  "status": 400,
  "detail": "O valor da transação deve ser maior que zero.",
  "instance": "/transactions"
}
```

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/transactions` | Cria uma transação (`amount` em **centavos**) |
| `GET` | `/transactions/{category}` | Lista por categoria (valores em **reais**) |
| `GET` | `/transactions/summary` | Resumo com totais por categoria (**novo**) |
| `POST` | `/transactions/ai` | Áudio (`multipart`, campo `file`) → resposta em áudio MP3 |
| `POST` | `/transactions/ai/text` | Texto (`{"message": "..."}`) → resposta em texto (**novo**) |

> As respostas da IA podem variar a cada execução, porque são geradas por um modelo de linguagem.

## 📚 O que aprendi

- **IA integrada com responsabilidade:** o modelo não acessa o banco diretamente; ele só chama casos de uso, que aplicam as regras do domínio. Assim, a validação vale tanto para o REST quanto para a IA.
- **Tool Calling na prática:** a descrição das ferramentas e dos parâmetros (`@Tool` e `@ToolParam`) é o que guia o modelo. Detalhes como "o valor é em centavos" fazem diferença no resultado.
- **Revisar o que a IA recebe e devolve:** o bug dos centavos mostrou que um erro de conversão vira uma resposta falada errada para a pessoa usuária.
- **Prompt de sistema:** instruções claras (não inventar dados, pedir o que falta, responder de forma curta para áudio) melhoram muito a qualidade das respostas.
- **Erros e testes:** tratar falhas da IA com mensagens claras e testar as regras de negócio sem depender de serviços externos tornam o projeto confiável e barato de testar.
