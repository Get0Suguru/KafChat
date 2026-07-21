# KafChat

A real-time group chat built with Spring Boot, STOMP over WebSockets, MongoDB, and Kafka. Started as a simple WebSocket + STOMP demo (formerly `SocketChat`), then migrated onto Kafka as a producer/consumer backbone so message delivery isn't tied to a single app instance's in-memory broker.

---

## Screenshots

> From `Screenshots/` in this repo

![User connect](Screenshots/user_connect.png)

![Join group](Screenshots/group_join.png)

![Realtime chat](Screenshots/chat_realtime.png)

---

## What it does

- Users "sign in" with a username/password (simple status response: `created` / `found` / `wrong`).
- Join any group by name; if it doesn't exist, it's created.
- Receive group history on join (via REST, pulled straight from MongoDB) and live messages in realtime (via STOMP, routed through Kafka).
- Send a private, ephemeral mention to one specific user inside a group — `/*username* message` — visible only to sender and target, never persisted.
- Single static page UI, neon theme, no build tooling.

---

## Architecture

Messages don't go straight from the STOMP controller to the broker anymore. The controller publishes to Kafka; a consumer on each running instance persists and broadcasts locally. This means two app instances behind a load balancer both see every message, even though each has its own independent, in-memory STOMP broker with no shared state.

```
Client ──STOMP──▶ Controller ──▶ Kafka topic (key = groupName)
                                        │
                        ┌───────────────┴───────────────┐
                        ▼                                ▼
              Consumer @ instance A                Consumer @ instance B
              (own consumer group id)               (own consumer group id)
                        │                                │
              persist + /topic broadcast        persist + /topic broadcast
                        │                                │
              clients on instance A              clients on instance B
```

Each instance runs its **own** Kafka consumer group (a random id generated at startup), not a shared one — a shared group would split partitions across instances (competing consumers, each message delivered once total), which is the wrong model here. A unique group per instance means every instance gets a full copy of every message, which is what lets it broadcast to whichever clients happen to be connected to it.

Targeted mentions (see below) skip Kafka entirely and go straight through Spring's `/user` destination — they're a same-instance-only feature for now, not yet part of the multi-instance story.

---

## Tech stack

- Backend: Spring Boot 3.5.x (Java 23)
  - Web, WebSocket (STOMP), Spring Data MongoDB, Spring Kafka
- DB: MongoDB (Atlas)
- Messaging: Apache Kafka (KRaft mode, no Zookeeper), via Docker Compose
- Build: Maven
- Frontend: Static HTML + STOMP.js (raw WebSocket, no SockJS)

Key deps are in `pom.xml`.

---

## Project layout

- `src/main/java/com/suguru/geto/Kaf/chat/`
  - `KafChatApplication.java` — Spring Boot entry point
  - `config/WebSocketConfig.java` — STOMP endpoint `/stomp`, broker `/topic` and `/queue`, prefixes `/app` and `/user`
  - `config/UsernameHandshakeHandler.java` — assigns a real `Principal` (from a `?username=` query param) to each WebSocket handshake, so `/user` destinations resolve by username instead of raw session id
  - `config/KafkaTopicConfig.java` — declares the `chat-messages` topic (partitions, replicas)
  - `controller/UserController.java` — `POST /api/users` (create/login)
  - `controller/ChatController.java` — STOMP handlers
    - `/app/chat.joinGroup` — ensures the group exists (history is no longer pushed from here — see below)
    - `/app/chat.sendMessage` — publishes broadcast messages to Kafka; routes targeted mentions directly via `/user`
  - `controller/ChatHistoryController.java` — `GET /api/groups/{groupName}/messages`, plain REST read from MongoDB
  - `consumer/ChatMessageConsumer.java` — `@KafkaListener`; persists broadcast messages and forwards them to `/topic/group/{name}`
  - `service/ChatService.java` — user/group lifecycle, persist messages, idempotent on `messageId`
  - `model/` — MongoDB documents: `User`, `ChatGroup`, `ChatMessage`
  - `repository/` — Spring Data MongoDB repos
  - `payload/` — `UserRequest`, `ChatMessageDto` (includes `targetUser` for mentions, `messageId` for idempotency)
- `src/main/resources/`
  - `application.properties` — Mongo URI (via `.env`), Kafka bootstrap servers, consumer group id, topic name
  - `static/index.html` — single-page UI
- `docker-compose.yml` — single-broker Kafka (KRaft mode)
- `Screenshots/` — PNGs used in README

---

## Configuration

`src/main/resources/application.properties` (excerpt):

```properties
# --- MongoDB (Atlas) ---
spring.data.mongodb.uri=${MONGO_URI}

# --- Kafka ---
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.suguru.geto.Kaf.chat.payload
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.consumer.group-id=kafchat-consumer-${random.uuid}
kafka.topic.chat-messages=chat-messages
```

- Set `MONGO_URI` in a `.env` file (loaded via `spring-dotenv`) or your environment directly.
- `auto-offset-reset=latest` is deliberate: MongoDB (via the REST history endpoint) is the source of truth for history, so a fresh consumer group doesn't need to replay the entire topic backlog on every restart.
- `group-id` includes `${random.uuid}` so every instance you start gets its own consumer group — this is what makes multi-instance fan-out work (see Architecture above).

CORS for the STOMP endpoint uses `setAllowedOriginPatterns("*")` — tighten this before deploying anywhere real.

---

## Run locally

1) Requirements
- Java 23
- Maven 3.9+
- Docker (for Kafka)
- A MongoDB Atlas connection string (or local MongoDB)

2) Start Kafka
```bash
docker compose up -d
```

3) Set your Mongo URI
```bash
# .env file, or export directly
MONGO_URI=mongodb+srv://...
```

4) Build & run
```bash
mvn clean package
mvn spring-boot:run
# or
java -jar target/KafChat-0.0.1-SNAPSHOT.jar
```

5) Open the UI
- Navigate to `http://localhost:8080/`.
- To see multi-instance fan-out for yourself: run a second instance with `-Dserver.port=8081`, open a browser tab on each, join the same group in both, send from one — both receive it.

---

## How the realtime flow works

- Client connects to `/stomp` and upgrades to STOMP, passing `?username=` on the socket URL so the server can assign it a real `Principal`.
- **Joining a group** (`/app/chat.joinGroup`):
  - Server ensures the group exists in MongoDB.
  - History is fetched separately, by the client, via `GET /api/groups/{group}/messages` — not pushed over the socket. (This used to be pushed via `/user/queue/group/{group}` immediately after subscribing, but that raced against the subscription actually registering server-side and reliably lost; REST removes the race entirely.)
- **Sending a broadcast message** (`/app/chat.sendMessage`, no mention):
  - Controller publishes the message to the `chat-messages` Kafka topic, keyed by group name (keeps a group's messages ordered on one partition).
  - Every running instance's consumer picks it up independently, persists it to MongoDB (skipping the write if `messageId` was already seen — Kafka is at-least-once, not exactly-once), and broadcasts to `/topic/group/{group}` for its own locally connected clients.
- **Sending a targeted mention** (`/app/chat.sendMessage`, payload includes `targetUser`):
  - Skips Kafka and MongoDB entirely. Delivered directly via `convertAndSendToUser` to both the target's and sender's `/user/queue/group/{group}`.
  - Fully ephemeral — if the target isn't connected (or never subscribed), it's simply gone. No history, no retry, no DB row.
  - Reaches every active session for that username (e.g. two open tabs), because `/user` now resolves against a real `Principal`, not a raw session id.

---

## The `/user` mention feature

Type `/*username* your message` in the message box to send a private aside to one specific person in the group — everyone else sees nothing.

This only works because of `UsernameHandshakeHandler`: without a real `Principal` attached to the WebSocket session, Spring's `/user` destinations can only be targeted by session id (useful for "reply to this one exact connection," useless for "reach whoever `kabadi` is right now"). With a `Principal` in place, `SimpUserRegistry` is keyed by username instead, and `convertAndSendToUser(username, ...)` resolves the way you'd intuitively expect.

---

## REST and STOMP contract

- REST
  - `POST /api/users` → body `{ username, password }` → response: `"created" | "found" | "wrong"`
  - `GET /api/groups/{groupName}/messages` → array of persisted `ChatMessageDto` for that group

- STOMP (client → server)
  - `/app/chat.joinGroup` — payload: string group name
  - `/app/chat.sendMessage` — payload JSON:
    ```json
    { "sender": "alice", "content": "hi", "groupName": "general" }
    ```
    Add `"targetUser": "bob"` to make it a private mention instead of a broadcast.

- STOMP (server → client)
  - `/topic/group/{group}` — broadcast messages (public, persisted)
  - `/user/queue/group/{group}` — targeted mentions (private, ephemeral, per-username via `Principal`)

---

## Notes & caveats

- Passwords are stored in plain text (demo simplicity). For anything real: hash with BCrypt/Argon2, add proper sessions/JWT, CSRF, etc.
- `UsernameHandshakeHandler` is **not** authentication — it's an unverified label taken straight from a query param. Anyone can claim any username on connect. Fine for learning `/user` routing, not fine for production.
- Mentions are same-instance only right now — if the sender and the mentioned user are connected to different app instances, the mention never crosses over (unlike broadcasts, which go through Kafka specifically to solve this). Noted as a deliberate scope cut, not an oversight.
- Kafka consumer is at-least-once, not exactly-once — `ChatMessageDto.messageId` plus a unique (sparse) index on `ChatMessage.messageId` guards against duplicate persistence on redelivery.
- No DB migrations — Mongo's schema-less by nature, but this also means field/index changes made mid-development (like adding `messageId`) require manually dropping/recreating existing collections or indexes.
- Single basic UI page; no build tooling.
- CORS/allowed origins for STOMP are wide open (`*`) — restrict before deploying anywhere public.

---

## Roadmap (nice-to-have)

- Proper auth (hashing, tokens, real sessions) replacing the query-param username trick
- Cross-instance mention delivery (route mentions through Kafka too, same pattern as broadcasts)
- Message pagination and lazy history load
- Typing indicators, presence, read receipts
- Basic moderation (ban/kick)
- A few integration tests

---

## License

MIT for the sample code unless noted otherwise.
