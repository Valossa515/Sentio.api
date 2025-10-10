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
5. **Consulta** — O cliente consulta o endpoint `/results` (a ser implementado) para obter o status ou resultado final.

---

## ⚙️ Configuração e Execução (Docker Compose)

A aplicação utiliza **Docker Compose** para orquestrar os serviços de infraestrutura:  
**PostgreSQL**, **Kafka**, **Zookeeper** e **Kafka UI**.

### 🧩 Pré-requisitos

- Docker e Docker Compose instalados  
- Java **JDK 25+**
- Credenciais do **Google Cloud Natural Language API** acessíveis ao ambiente (via SDK ou variável de ambiente)
- Se já possui o Docker Desktop e rodar o projeto na IntelliJ não precisa se precocupar em executar os comandos do docker basta ter o plugin do docker na IDE.

---

### 🐳 1. Inicializar a Infraestrutura

Certifique-se de que o arquivo `docker-compose.yml` está na raiz do projeto e execute:

```bash
docker compose up -d
```

📘 **Serviços levantados:**  
- PostgreSQL → `localhost:5433`  
- Kafka → `localhost:29092`  
- Zookeeper → `localhost:2181`  
- Kafka UI → `http://localhost:8085`  

> O Kafka é acessível externamente na porta **29092** e internamente na **9092** (via host `kafka`).

---

### 💻 2. Executar a Aplicação (Spring Boot)

Para rodar localmente via IDE:

- **Ative o perfil `local`**  
- Configure a VM Option:  
  ```bash
  -Dspring.profiles.active=local
  ```
- Isso fará a aplicação conectar-se a:
  - Kafka: `localhost:29092`
  - Postgres: `localhost:5433`

A API estará disponível em:  
👉 **http://localhost:8080/swagger-ui/index.html**

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

### 2. Consultar Resultados (🚧 Em Desenvolvimento)

**Endpoint sugerido:**  
`GET /api/text-analysis/v1/results`

**Descrição:**  
Retornará os resultados persistidos no banco (`AnalysisResultRepository`).

---

## 🛠️ Detalhes Técnicos

### 🧵 Tópicos Kafka

| Tópico | Responsabilidade |
|--------|------------------|
| `text-analysis-requests` | Usado pelo Producer (`AnalyzeTextCommandHandler`) e consumido pelo `SentimentAnalysisConsumer`. |

---

### 🧠 Lógica de Classificação de Sentimentos

A classificação é feita com base no **score** retornado pelo Google NLP API:

| Sentimento | Intervalo de Score |
|-------------|--------------------|
| **POSITIVE** | `[0.25, 1.0]` |
| **NEGATIVE** | `[−1.0, −0.10]` |
| **NEUTRAL**  | `(−0.10, 0.25)` |

> 🔧 O limite de negatividade foi ajustado para **-0.10** para representar melhor percepções ligeiramente negativas.

---

## 🧾 Variáveis de Configuração (`application.properties`)

```properties
# Spring
spring.application.name=sentio-api
spring.profiles.active=local

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/sentimento_db
spring.datasource.username=myuser
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update

# Kafka
spring.kafka.bootstrap-servers=localhost:29092
spring.kafka.consumer.group-id=text-analysis-group
spring.kafka.topic.text-analysis-requests=text-analysis-requests
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

---

## 🧰 Tecnologias Principais

- **Java 25**
- **Spring Boot 3.x**
- **Apache Kafka**
- **Google Cloud Natural Language API**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Project Reactor (WebFlux)**

---

## 📈 Benefícios da Arquitetura Assíncrona

- ✅ **Alta escalabilidade** — processamento paralelo via consumidores Kafka  
- ⚡ **Respostas imediatas** — cliente não precisa aguardar análise completa  
- 🔄 **Tolerância a falhas** — mensagens persistem no tópico até serem processadas  
- 🧩 **Extensibilidade** — novos consumidores podem ser adicionados facilmente  

---

## 📋 Próximos Passos

- [ ] Implementar endpoint de consulta de resultados (`/results`)  
- [ ] Adicionar testes unitários e de integração Kafka/Postgres  
- [ ] Publicar container no **Docker Hub**  
- [ ] Configurar CI/CD (GitHub Actions ou Azure DevOps)

---

## 🧑‍💻 Autor

**Felipe Martins**  
Desenvolvedor Fullstack

📧 Contato: [fe.mmo515@gmail.com](mailto:fe.mmo515@gmail.com)

---

> 💡 *Sentio API — Entendendo emoções, uma ou mais mensagens por vez.*
