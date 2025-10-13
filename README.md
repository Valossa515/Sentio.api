# 🔎 Sentio API — Análise de Sentimentos em Texto (Assíncrona com Kafka)

A **Sentio API** é um serviço robusto para **análise de sentimentos em textos**.
Utiliza o **Google Cloud Natural Language API** para o processamento semântico e o **Apache Kafka** para garantir uma arquitetura **assíncrona, escalável e resiliente**, baseada no padrão **Event-Driven Architecture (EDA)**.

---

## 🚀 Arquitetura e Fluxo

A arquitetura foi migrada do modelo **síncrono** para o **assíncrono** para melhorar o tempo de resposta e a capacidade de lidar com alta carga de requisições.

### 🔄 Fluxo Assíncrono (Fire-and-Forget)

1. **Requisição (HTTP POST)** — O cliente envia um ou mais textos para o endpoint `/analyze`.
2. **Produção (Producer)** — O `AnalyzeTextCommandHandler` recebe a requisição e a publica imediatamente como mensagens no tópico Kafka (`text-analysis-requests`).
3. **Resposta Imediata (Accepted)** — A API retorna uma resposta de aceite (Status **202** ou **200**), sem o resultado final.
4. **Consumo e Processamento** — O `SentimentAnalysisConsumer` lê as mensagens do Kafka, executa a análise de sentimentos (via Google API) e persiste os resultados no PostgreSQL.
5. **Consulta** — O cliente consulta o endpoint `/results` ou `/sentiments` para obter o status ou resultado final.

---

## ⚙️ Configuração e Execução (Docker Compose)

A aplicação utiliza **Docker Compose** para orquestrar os serviços de infraestrutura:
**PostgreSQL**, **Kafka**, **Zookeeper** e **Kafka UI**.

### 🧩 Pré-requisitos

* Docker e Docker Compose instalados
* Java **JDK 25+**
* Credenciais do **Google Cloud Natural Language API** acessíveis ao ambiente (via SDK ou variável de ambiente)
* Docker Desktop ou plugin Docker na IDE

### 🐳 Inicializar a Infraestrutura

```bash
docker compose up -d
```

📘 **Serviços levantados:**

* PostgreSQL → `localhost:5433`
* Kafka → `localhost:29092`
* Zookeeper → `localhost:2181`
* Kafka UI → `http://localhost:8085`

> O Kafka é acessível externamente na porta **29092** e internamente na **9092** (via host `kafka`).

---

### 💻 Executar a Aplicação (Spring Boot)

* **Ative o perfil `local`**
* Configure a VM Option:

  ```bash
  -Dspring.profiles.active=local
  ```
* Isso fará a aplicação conectar-se a:

  * Kafka: `localhost:29092`
  * Postgres: `localhost:5433`

A API estará disponível em:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

---

## 🌐 Endpoints da API

### 1. Enviar Textos para Análise

**Endpoint:**
`POST /api/text-analysis/v1/analyze`

**Descrição:**
Aceita um ou mais textos e envia para a fila de processamento Kafka.

#### 📥 Exemplos de Payload

```json
{
    "texts": [
        "Adorei o atendimento, cinco estrelas!",
        "Demorou muito para carregar, estou insatisfeito.",
        "Achei neutro, não tenho opinião formada."
    ]
}

{
  "text": "Bom produto"
}
```

#### 📤 Exemplo de Resposta (Aceite Imediato)

```json
{
    "results": [
        {
            "id": null,
            "originalText": "3 textos aceitos para processamento assíncrono.",
            "sentimentType": null,
            "confidenceScore": null,
            "analysisTimestamp": null
        }
    ]
}
```

---

### 2. Listar Sentimentos com Paginação e Filtro

**Endpoint:**
`GET /api/sentiments/v1`

**Descrição:**
Retorna os resultados da análise de sentimentos armazenados no banco, com suporte a:

* **Paginação** (`page` e `size`)
* **Filtro opcional** por tipo de sentimento (`sentimentType`)
* **Ordenação** via enum (`orderBy` e `sort`)

#### 📥 Parâmetros

| Parâmetro       | Tipo                                   | Obrigatório | Descrição                                            |
| --------------- | -------------------------------------- | ----------- | ---------------------------------------------------- |
| `sentimentType` | `POSITIVE, NEGATIVE, NEUTRAL, MIXED`   | Não         | Filtra apenas os sentimentos do tipo selecionado     |
| `page`          | `int`                                  | Não         | Número da página (1-based). Default: 1               |
| `size`          | `int`                                  | Não         | Quantidade de registros por página. Default: 10      |
| `sort`          | `ASC, DESC`                            | Não         | Direção de ordenação. Default: DESC                  |
| `orderBy`       | `ANALYSIS_TIMESTAMP, CONFIDENCE_SCORE` | Não         | Campo pelo qual ordenar. Default: ANALYSIS_TIMESTAMP |

#### 📤 Exemplo de Requisição

```
GET /api/sentiments/v1?sentimentType=POSITIVE&orderBy=CONFIDENCE_SCORE&sort=ASC&page=1&size=5
```

#### 📤 Exemplo de Resposta

```json
{
  "data": [
    {
      "originalText": "O produto é ótimo!",
      "sentimentType": "POSITIVE",
      "confidenceScore": 0.8999999761581421,
      "analysisTimestamp": "2025-10-13T11:53:20.965252"
    },
    {
      "originalText": "O produto é de alta qualidade, estou muito satisfeito!",
      "sentimentType": "POSITIVE",
      "confidenceScore": 0.8999999761581421,
      "analysisTimestamp": "2025-10-10T11:56:37.577849"
    }
  ],
  "totalRecords": 4,
  "page": 1,
  "size": 5
}
```

> 🔹 `page` é 1-based e `totalRecords` reflete o total de registros disponíveis para o filtro aplicado.

---

### 3. Métricas

**Endpoint:**
`GET /api/metrics/v1/dashboard`

**Descrição:**
Retornará métricas para exposição em dashboards.

#### 📤 Exemplo de Resposta

```json
{
  "totalProcessed": 0,
  "totalSuccess": 0,
  "totalFailures": 0,
  "totalErrors": 0,
  "totalInvalid": 0,
  "durationStats": {
    "meanSeconds": 0.1,
    "maxSeconds": 0.1,
    "totalSeconds": 0.1,
    "count": 0
  },
  "sentimentDistribution": [
    {
      "sentiment": "string",
      "count": 0.1
    }
  ],
  "timeSeries": [
    {
      "timestamp": "string",
      "processed": 0,
      "success": 0,
      "failures": 0
    }
  ]
}
```

---

## 🧠 Lógica de Classificação de Sentimentos

| Sentimento   | Intervalo de Score |
| ------------ | ------------------ |
| **POSITIVE** | `[0.25, 1.0]`      |
| **NEGATIVE** | `[−1.0, −0.10]`    |
| **NEUTRAL**  | `(−0.10, 0.25)`    |

---

## 🧾 Variáveis de Configuração (`application.properties`)

```properties
spring.application.name=sentio-api
spring.profiles.active=local

spring.datasource.url=jdbc:postgresql://localhost:5433/sentimento_db
spring.datasource.username=myuser
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update

spring.kafka.bootstrap-servers=localhost:29092
spring.kafka.consumer.group-id=text-analysis-group
spring.kafka.topic.text-analysis-requests=text-analysis-requests
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*

management.endpoints.web.exposure.include=health,info,prometheus
endpoint.prometheus.enabled=true
metrics.tags.application=sentiment-analysis-service
```

---

## 🧰 Tecnologias Principais

* **Java 25**
* **Spring Boot 3.x**
* **Apache Kafka**
* **Google Cloud Natural Language API**
* **PostgreSQL**
* **Docker & Docker Compose**
* **Project Reactor (WebFlux)**

---

## 📈 Benefícios da Arquitetura Assíncrona

* ✅ **Alta escalabilidade** — processamento paralelo via consumidores Kafka
* ⚡ **Respostas imediatas** — cliente não precisa aguardar análise completa
* 🔄 **Tolerância a falhas** — mensagens persistem no tópico até serem processadas
* 🧩 **Extensibilidade** — novos consumidores podem ser adicionados facilmente

---

## 🧑‍💻 Autor

**Felipe Martins**
Desenvolvedor Fullstack
