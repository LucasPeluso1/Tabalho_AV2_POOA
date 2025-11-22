[![Assista ao vídeo](https://img.youtube.com/vi/kqjvv_kuxok.jpg)](https://www.youtube.com/watch?v=kqjvv_kuxok)

Desenvolvido por: 
- ANTONIO PARIS MORAIS JUNIOR
- AQUILES SANTOS DE ARAUJO
- BRUNO HENRIQUE RAMOS SEIXAS
- GABRIEL ALMEIDA DA CRUZ
- HUGO RODRIGO SILVA SANTOS
- LUCAS NASCIMENTO ANGELO PELUSO
# 💸 FiadoPay Simulator - Gateway de Pagamento

> **Stack:** Java 21, Spring Boot 3.2, H2 Database, Maven.

O **FiadoPay** é um simulador robusto de um Gateway de Pagamento (PSP).
---

🏗️ Arquitetura e Decisões de Engenharia

O sistema foi desenhado para desacoplar a recepção da requisição do seu processamento efetivo, garantindo alta disponibilidade e simulando um ambiente real.

### 1. Processamento Assíncrono e Threads
Em vez de bloquear a thread principal do servidor HTTP enquanto "conversa com o banco" (simulado), adotamos uma abordagem não-bloqueante:
* **Fluxo:** A API retorna `201 Created` com status `PENDING` imediatamente.
* **Bastidores:** Um `ExecutorService` (pool de threads dedicado) assume o trabalho pesado: cálculo de juros complexos, regras de aprovação e comunicação externa.
* **Benefício:** A API permanece responsiva mesmo sob alta carga ou latência bancária simulada.

### 2. Metaprogramação e Reflexão
Utilizamos a API de Reflexão do Java para criar um sistema dinâmico de descoberta de regras de negócio.
* **Annotation Inspector:** Ao iniciar, o serviço `AnnotationInspectorService` varre o Contexto do Spring em busca de métodos anotados, configurando regras sem necessidade de arquivos XML ou configurações estáticas.
* **JAR Safe:** O scanner foi implementado utilizando `ApplicationContext` e `AopUtils`, garantindo que a descoberta de classes funcione tanto na IDE quanto quando a aplicação é empacotada em `.jar`.

### 3. Webhooks Seguros (HMAC)
Para notificar as lojas sobre atualizações de pagamento (`APPROVED`/`DECLINED`), implementamos um sistema de Webhooks passivo-ativo.
* **Segurança:** Todo payload enviado é assinado digitalmente com **HMAC-SHA256**.
* **Validação:** O lojista recebe um header `X-Signature` e deve validá-lo para garantir a integridade e autenticidade da mensagem, evitando ataques de spoofing.

---

## 🧩 Anotações Personalizadas

O projeto introduz anotações que alteram ou marcam o comportamento do sistema em tempo de execução:

| Anotação | Alvo | Descrição | Metadados |
| :--- | :--- | :--- | :--- |
| `@PaymentMethod` | Método | Classifica o tipo de pagamento suportado pelo handler. | `type` (ex: "CARD") |
| `@AntiFraud` | Método | Vincula regras de risco ao processamento do método. | `name`, `threshold` (limite de valor) |
| `@WebhookSink` | Método | Marca métodos que atuam como pontos de saída de eventos. | *Nenhum* |

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
* **Java 21** instalado e configurado.
* Porta **8080** livre.

### Execução
No terminal, na raiz do projeto:

```bash
# Linux / Mac / Git Bash
./mvnw clean spring-boot:run

# Windows (CMD)
mvnw clean spring-boot:run

