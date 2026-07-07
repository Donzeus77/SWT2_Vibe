# Aufgabenteilung — SWT2_Vibe Mensa-Projekt

Dieses Dokument teilt das Projekt in **7 Aufgabenpakete** für die 7 Teammitglieder auf. Jedes Paket ist in sich abgeschlossen und kann weitgehend unabhängig bearbeitet werden. Die Abhängigkeiten zwischen den Paketen sind klar definiert.

## Team

| # | Name | Aufgabenpaket |
|---|---|---|
| 1 | Yaren Sari (Team Leader) | **Paket A: Koordination & Infrastruktur** |
| 2 | Joel Kawinski | **Paket B: studentenwerk_simulator — Gericht-Domäne & Seed** |
| 3 | Leo Bernoth | **Paket C: studentenwerk_simulator — MQTT-Publisher & ReceivedOrders** |
| 4 | Valeriia Khatchenko | **Paket D: mensa_app_backend — Authentifizierung & User** |
| 5 | Johann Wenner | **Paket E: mensa_app_backend — Bestellwesen & MQTT-Subscriber** |
| 6 | Viktoriia Dovzhenko | **Paket F: Frontend — Speiseplan, Standorte, Abstimmung** |
| 7 | Noel Koblitz | **Paket G: Frontend — Bestellungen, Profil & Integration** |

---

## Architektur-Überblick (für alle)

```
[Frontend (Vite/React)] --REST/JWT--> [mensa_app_backend:8082] --MQTT--> [studentenwerk_simulator:8081]
  Pakete F + G                          Paket D + E                        Paket B + C
  Port 5173                             eigene DB: mensa_db (5434)         eigene DB: simulator_db (5433)
                                          ↑ MQTT sub (speiseplan/mensen)        ↓ MQTT pub (speiseplan/mensen)
                                          └────────────────────────┘           ↓ MQTT sub (orders/votes)
                                                                          Mosquitto (1883)
                                                                              Paket A
```

**MQTT-Topics:**
- `mensa/speiseplan` — Simulator → App (retained) — JSON-Array aller Gerichte
- `mensa/mensen` — Simulator → App (retained) — JSON-Array aller Mensen
- `mensa/orders` — App → Simulator — JSON-Objekt einer neuen Bestellung
- `mensa/votes` — App → Simulator — JSON-Map `{gerichtId: count}`

**Git-Workflow:**
- `main` — stabiler Stand (immer lauffähig)
- Feature-Branches: `feature/<name>-<thema>` (z.B. `feature/joel-gericht-seed`)
- Jeder commitet nur auf eigenen Branch → PR → Review durch Yaren → Merge

---

# Paket A: Koordination & Infrastruktur
**Verantwortlich:** Yaren Sari (Team Leader)

## Ziel
Sicherstellen, dass das Projekt-Setup für alle 6 anderen läuft: Docker, Datenbanken, MQTT-Broker, Build-Pipeline, Repository-Struktur, finale Doku & Abgabe.

## Aufgaben (detailliert)

### A1: Repository & Build verwalten
- GitHub-Repo `Donzeus77/SWT2_Vibe` als Maintainer verwalten
- `main`-Branch schützen (Branch Protection Rule: PR + Review Pflicht, CI muss grün sein)
- Für jeden Merge-Request Review durchführen, Code-Qualität prüfen
- Konflikte bei Abhängigkeiten zwischen Paketen schlichten

### A2: docker-compose.yml
Erstellen und pflegen von `docker-compose.yml` im Root-Verzeichnis:
```yaml
services:
  postgres-simulator:
    image: postgres:16
    environment:
      POSTGRES_DB: simulator_db
      POSTGRES_USER: mensa
      POSTGRES_PASSWORD: mensa123
    ports: ["5433:5432"]
    volumes: [pgdata-simulator:/var/lib/postgresql/data]

  postgres-app:
    image: postgres:16
    environment:
      POSTGRES_DB: mensa_db
      POSTGRES_USER: mensa
      POSTGRES_PASSWORD: mensa123
    ports: ["5434:5432"]
    volumes: [pgdata-app:/var/lib/postgresql/data]

  mosquitto:
    image: eclipse-mosquitto:2
    ports: ["1883:1883"]
    volumes: [./mosquitto/mosquitto.conf:/mosquitto/config/mosquitto.conf]

volumes:
  pgdata-simulator:
  pgdata-app:
```
- `mosquitto/mosquitto.conf` mit Inhalt `listener 1883` + `allow_anonymous true` erstellen

### A3: Maven Parent POM
- `pom.xml` im Root als `<packaging>pom</packaging>` mit Spring Boot 4.0.6 Parent, Java 21
- Module `studentenwerk_simulator` und `mensa_app_backend` (und später ggf. `frontend`) eintragen
- Sicherstellen, dass `./mvnw verify` beide Module baut

### A4: GitHub-Actions CI
Pflegen von `.github/workflows/pre-merge.yaml`:
- Job `verify`: 2 Postgres-Service-Container (simulator_db:5433, mensa_db:5434) mit Health-Checks, Mosquitto via `docker run`, dann `./mvnw --batch-mode --update-snapshots verify`
- Job `container-check`: für Module mit `container.image.enabled=true` → Docker-Image bauen mit `Dockerfile.java`
- Sicherstellen, dass PRs automatisch getestet werden

### A5: README & Doku
- `README.md` mit Architektur-Diagramm, Quickstart, Endpunkt-Übersicht, Team-Liste
- Abschließend: Präsentationsfolien / Demo-Skript für die Abgabe vorbereiten
- Sicherstellen, dass alle 6 Pakete in der Doku erwähnt werden

### A6: Finale Integration & Abnahmetests
- Nach Merge aller Pakete: End-to-End-Test der kompletten Kette
  - `docker compose up -d` → beide Backends starten → Frontend starten
  - Speiseplan wird im Frontend angezeigt (Pakete B+C → E → F)
  - Registrierung/Login klappt (Paket D → G)
  - Bestellung wird erstellt und taucht im Simulator auf (Paket G → E → C)
  - Voting funktioniert end-to-end (Paket F → E → C)
- Bug-Reports an die jeweiligen Paket-Verantwortlichen

## Abhängigkeiten
- **Blockiert:** nichts (startet sofort)
- **Wird benötigt von:** allen (jeder braucht Docker + Repo)

## Abnahme-Kriterien
- `docker compose up -d` startet 3 Container ohne Fehler
- `./mvnw verify` läuft grün auf `main`
- CI-Pipeline läuft bei jedem PR automatisch
- README ist vollständig und aktuell

---

# Paket B: studentenwerk_simulator — Gericht-Domäne & Seed
**Verantwortlich:** Joel Kawinski

## Ziel
Die `Gericht`-Hierarchie als JPA-Entities abspeichern, mit echten Mensa-Daten seeden, und per REST bereitstellen. Das ist die "Behördenseite" des Studentenwerks — hier liegen die Stammdaten.

## Aufgaben (detailliert)

### B1: JPA-Entities für die Gericht-Hierarchie
Die bestehenden Klassen in `studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/gericht/` mit JPA-Annotationen versehen:

**`Gericht.java`** (abstrakte Basisklasse):
- `@Entity`, `@Table(name = "gericht")`, `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`, `@DiscriminatorColumn(name = "typ")`
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- Felder: `name`, `beschreibung`, `preisStudent`, `preisGast` (alle `private`, nicht-final wegen JPA)
- `@ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "gericht_allergene")` für `allergene` (Enum `Allergen`, `@Enumerated(EnumType.STRING)`)
- `@ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "gericht_tags")` für `tags` (Enum `GerichtTag`, `@Enumerated(EnumType.STRING)`)
- Geschützter No-Arg-Konstruktor für JPA: `protected Gericht() {}`
- Bestehende Konstruktoren + Getter beibehalten
- **WICHTIG:** `@MappedSuperclass` funktioniert NICHT mit `@ElementCollection` (erzeugt konfliktierende FK-Spalten) → `@Inheritance(SINGLE_TABLE)` verwenden

**`Beilage.java`** und **`Hauptspeise.java`**:
- `@Entity`, `@DiscriminatorValue("BEILAGE")` bzw. `@DiscriminatorValue("HAUPTSPEISE")`
- Geschützten No-Arg-Konstruktor ergänzen: `protected Beilage() {}` / `protected Hauptspeise() {}`
- Bestehende Konstruktoren + `getTyp()` beibehalten

**`Allergen.java`** und **`GerichtTag.java`**: bleiben unverändert (Enums)

### B2: Repositories
Für jede Entity ein Spring-Data-JPA-Repository anlegen:
- `GerichtRepository extends JpaRepository<Gericht, Long>` im Package `gericht`
- `BeilageRepository extends JpaRepository<Beilage, Long>`
- `HauptspeiseRepository extends JpaRepository<Hauptspeise, Long>`

### B3: Factory-Klassen zu Spring-Components
- `gericht/factory/HauptgerichtFactory.java` und `BeilageFactory.java` mit `@Component` annotieren
- `GerichtFactory`-Interface bleibt unverändert
- Die Factories werden später vom Seed-Runner verwendet (Paket B) und können injected werden

### B4: Mensa-Entity + Repository
Neues Package `studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/mensa/`:
- `Mensa.java` als `@Entity` mit Feldern: `id` (`@Id @GeneratedValue IDENTITY`), `name`, `campus`, `adresse`, `oeffnungszeiten`, `auslastung` (String: "low"/"medium"/"high")
- Geschützten No-Arg-Konstruktor + öffentlichen Konstruktor mit allen Feldern
- `MensaRepository extends JpaRepository<Mensa, Long>`

### B5: REST-Controller
Neues Package `studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/controller/`:
- `GerichtController` (`@RestController`, `@RequestMapping("/api/gerichte")`):
  - `GET /api/gerichte` → vereint `hauptspeiseRepository.findAll()` + `beilageRepository.findAll()` zu einer Liste
- `MensaController` (`@RestController`, `@RequestMapping("/api/mensen")`):
  - `GET /api/mensen` → `mensaRepository.findAll()`

### B6: Seed-Runner mit realen Daten
`studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/config/SeedData.java` als `@Configuration` mit einem `@Bean CommandLineRunner`:
- Injizieren: `HauptspeiseRepository`, `BeilageRepository`, `MensaRepository`, `HauptgerichtFactory`, `BeilageFactory`
- **Nur wenn DB leer** (`hauptspeiseRepository.count() == 0 && beilageRepository.count() == 0`): seeden
- 8 Hauptspeisen mit realen Namen, Beschreibungen, Student/Gast-Preisen, Allergenen, Tags:
  - Currywurst mit Pommes, Veganes Chili sin Carne, Pasta Bolognese, Vegane Pasta Pesto, Hähnchenbrust mit Reis, Schnitzel Wiener Art, Vegane Linsensuppe, Falafel-Teller
  - Preise Student: 2.50–4.50 €, Gast: 4.00–7.00 €
  - Allergene: GLUTEN, EI, MILCH, SESAM etc. je nach Gericht
  - Tags: VEGAN, VEGETARISCH, HALAL etc.
- 4 Beilagen: Pommes frites, Kartoffelsalat, Gemischter Salat, Reis
- 8 Dortmunder Mensen: Hauptmensa (TU), Archeteria (TU), Mensa Baroper Stern (TU), Mensa Cantstraße (TU), FH Dortmund Mensa (FH), Mensa Emil-Figge-Straße (FH), Mensa Sonnenstraße (FH), Mensa Westfalenhütte (TU)
  - Mit echten Adressen, Öffnungszeiten (Mo–Fr 11:15–14:15), Auslastung

### B7: application.properties
`studentenwerk_simulator/src/main/resources/application.properties`:
```properties
server.port=8081
spring.application.name=studentenwerk_simulator
spring.datasource.url=jdbc:postgresql://localhost:5433/simulator_db
spring.datasource.username=mensa
spring.datasource.password=mensa123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=studentenwerk_simulator
mqtt.topics.speiseplan=mensa/speiseplan
mqtt.topics.mensen=mensa/mensen
mqtt.topics.orders=mensa/orders
mqtt.topics.votes=mensa/votes
```

### B8: pom.xml-Dependencies
In `studentenwerk_simulator/pom.xml` ergänzen:
- `spring-boot-starter-data-jpa`
- `org.postgresql:postgresql` (scope `runtime`)
- `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`
(bestehende MQTT-Dependencies bleiben)

## Abhängigkeiten
- **Blockiert von:** Paket A (Docker + Repo muss stehen)
- **Wird benötigt von:** Paket C (braucht die Entities + Repos), Paket E (App-Backend braucht die JSON-Struktur der Gerichte)

## Abnahme-Kriterien
- `curl http://localhost:8081/api/gerichte` liefert 12 Gerichte als JSON
- `curl http://localhost:8081/api/mensen` liefert 8 Mensen als JSON
- Nach `docker compose down -v && docker compose up -d` + Neustart des Simulators wird die DB neu geseedet
- Factory-Pattern und bestehende Klassenstruktur sind erhalten

---

# Paket C: studentenwerk_simulator — MQTT-Publisher & ReceivedOrders
**Verantwortlich:** Leo Bernoth

## Ziel
Den Simulator per MQTT mit dem App-Backend verbinden: Speiseplan + Mensen publizieren (Pakete B), eingehende Bestellungen und Votes empfangen und abspeichern.

## Aufgaben (detailliert)

### C1: MqttConfig
`studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/config/MqttConfig.java` als `@Configuration`:
- `@Value`-Injection für `mqtt.broker.url`, `mqtt.client.id`, alle 4 Topics aus `application.properties`
- `@Bean MqttPahoClientFactory mqttClientFactory()`:
  - `DefaultMqttPahoClientFactory`, `MqttConnectOptions` mit `setServerURIs`, `setAutomaticReconnect(true)`, `setCleanSession(false)`
- **Outbound Channels** (für Pub):
  - `@Bean MessageChannel speiseplanOutboundChannel()` → `new DirectChannel()`
  - `@Bean MessageChannel mensenOutboundChannel()` → `new DirectChannel()`
  - `@Bean @ServiceActivator(inputChannel = "speiseplanOutboundChannel") MessageHandler speiseplanOutboundHandler()`:
    - `MqttPahoMessageHandler` mit clientId + "-pub-speiseplan"
    - `setAsync(true)`, `setDefaultTopic(speiseplanTopic)`, `setDefaultRetained(true)`
  - Analog `mensenOutboundHandler`
- **Inbound Channels** (für Sub):
  - `@Bean MessageChannel ordersInboundChannel()` → `new DirectChannel()`
  - `@Bean MessageChannel votesInboundChannel()` → `new DirectChannel()`
  - `@Bean MqttPahoMessageDrivenChannelAdapter ordersInboundAdapter()`:
    - `MqttPahoMessageDrivenChannelAdapter(clientId + "-sub-orders", mqttClientFactory(), ordersTopic)`
    - `adapter.setOutputChannel(ordersInboundChannel())`
  - Analog `votesInboundAdapter` für `votesTopic`
- Getter für die Topic-Strings (`getSpeiseplanTopic()` etc.)

### C2: MqttPublisher
`studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/config/MqttPublisher.java` als `@Component`:
- Injizieren: `speiseplanOutboundChannel`, `mensenOutboundChannel`, `HauptspeiseRepository`, `BeilageRepository`, `MensaRepository`, `ObjectMapper`
- `@EventListener(ApplicationReadyEvent.class) public void publishOnStartup()` → ruft `publishSpeiseplan()` + `publishMensen()` auf
- `public void publishSpeiseplan()`:
  - Lädt alle Hauptspeisen + Beilagen aus den Repos
  - Wandelt jedes Gericht in eine `Map<String, Object>` um (id, name, beschreibung, preisStudent, preisGast, allergene, tags, typ)
  - Serialisiert die Liste als JSON via `objectMapper.writeValueAsString`
  - Sendet via `speiseplanOutboundChannel.send(MessageBuilder.withPayload(json).build())`
  - Try/catch mit Fehler-Logging
- `public void publishMensen()`: analog, mit Mensa-Daten
- **WICHTIG:** `@ElementCollection(fetch = FetchType.EAGER)` vorausgesetzt (Paket B), sonst `LazyInitializationException`

### C3: ReceivedOrder als JPA-Entity
Die bestehende `orderreceiver/ReceivedOrder.java` (aktuell ein Record) umschreiben zu einer JPA-Entity:
- `@Entity`, `@Table(name = "received_order")`
- Felder: `id` (`@Id @GeneratedValue IDENTITY`), `studentName`, `status` (Default `"EINGEGANGEN"`), `total` (double), `pickupTime`, `code`, `receivedAt` (`LocalDateTime.now()`)
- `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private List<ReceivedOrderItem> items = new ArrayList<>();`
- `addItem(ReceivedOrderItem item)`-Methode: fügt hinzu und setzt `item.setOrder(this)`
- Geschützten No-Arg-Konstruktor + öffentlichen Konstruktor mit `(studentName, total, pickupTime, code)`

### C4: ReceivedOrderItem (neue Entity)
`orderreceiver/ReceivedOrderItem.java`:
- `@Entity`, `@Table(name = "received_order_item")`
- Felder: `id`, `gerichtId`, `name`, `anzahl` (int), `preis` (double)
- `@ManyToOne @JoinColumn(name = "order_id") private ReceivedOrder order;`
- `setOrder()`-Methode

### C5: ReceivedOrderRequest + ItemRequest (DTOs)
- `ReceivedOrderRequest.java` als Record: `(String studentName, double total, String pickupTime, String code, List<ReceivedOrderItemRequest> items)`
- `ReceivedOrderItemRequest.java` als Record: `(Long gerichtId, String name, int anzahl, double preis)`
- Das ist die Payload, die via MQTT vom App-Backend kommt

### C6: ReceivedOrderRepository
- `orderreceiver/ReceivedOrderRepository.java`: `extends JpaRepository<ReceivedOrder, Long>`

### C7: ReceivedOrderService auf DB umklemmen
Die bestehende `orderreceiver/ReceivedOrderService.java` umschreiben:
- Injiziert `ReceivedOrderRepository` statt eigener In-Memory-Liste
- `receiveOrder(ReceivedOrderRequest request)`:
  - Erzeugt `new ReceivedOrder(studentName, total, pickupTime, code)`
  - Für jedes `ReceivedOrderItemRequest`: `order.addItem(new ReceivedOrderItem(...))`
  - `repository.save(order)` und zurückgeben
- `getAllOrders()`: `repository.findAll()`

### C8: OrderMqttHandler (MQTT-Subscriber)
`orderreceiver/OrderMqttHandler.java` als `@Component`:
- Injiziert `ReceivedOrderService`, `ObjectMapper`
- `@ServiceActivator(inputChannel = "ordersInboundChannel")`
- `handleOrder(Message<?> message)`:
  - Payload als String, via `objectMapper.readValue(payload, ReceivedOrderRequest.class)` deserialisieren
  - `receivedOrderService.receiveOrder(request)` aufrufen
  - Try/catch mit Fehler-Logging (Stderr)

### C9: VoteTotal-Entity + Handler
Neues Package `studentenwerk_simulator/src/main/java/com/example/studentenwerk_simulator/voting/`:
- `VoteTotal.java` als `@Entity`: `id`, `gerichtId` (Long), `count` (int)
- `VoteTotalRepository extends JpaRepository<VoteTotal, Long>` mit `Optional<VoteTotal> findByGerichtId(Long gerichtId)`
- `VoteMqttHandler.java` als `@Component`:
  - `@ServiceActivator(inputChannel = "votesInboundChannel")`
  - Deserialisiert Payload als `Map<String, Integer>` (gerichtId → count)
  - Für jeden Entry: `VoteTotal` aus DB holen oder neu anlegen, `setCount(count)`, `repository.save()`

### C10: ReceivedOrderController bleibt erhalten
Die bestehende `orderreceiver/ReceivedOrderController.java` (`@RestController`, `/api/received-orders`) bleibt erhalten und nutzt den umgeklemmten Service.

## Abhängigkeiten
- **Blockiert von:** Paket B (Entities + Repos + Seed), Paket A (Docker + Mosquitto)
- **Wird benötigt von:** Paket E (App-Backend muss wissen, welche Payload-Struktur der Simulator erwartet)

## Abnahme-Kriterien
- Beim Simulator-Start erscheint im Log: "Publiziere Speiseplan mit N Gerichten"
- `mosquitto_sub -t 'mensa/#' -v` zeigt die Speiseplan- und Mensen-Nachrichten
- Wenn das App-Backend eine Bestellung via `mensa/orders` published, taucht sie in der Simulator-DB auf (`curl http://localhost:8081/api/received-orders`)
- Vote-Totals werden in der `vote_total`-Tabelle gespeichert

---

# Paket D: mensa_app_backend — Authentifizierung & User
**Verantwortlich:** Valeriia Khatchenko

## Ziel
Benutzerregistrierung, Login, JWT-Token-Ausgabe, Security-Filter und User-Entity. Das ist die Zugangskontrolle für das Frontend.

## Aufgaben (detailliert)

### D1: UserEntity + Repository
Neues Package `mensa_app_backend/src/main/java/com/example/mensa_app_backend/user/`:
- `UserEntity.java` als `@Entity`, `@Table(name = "users")`:
  - `id` (`@Id @GeneratedValue IDENTITY`), `email` (`@Column(unique = true)`), `passwordHash`, `vorname`, `nachname`, `type` (String: "student"/"mitarbeiter"/"gast")
  - Geschützten No-Arg-Konstruktor + öffentlichen Konstruktor mit allen Feldern
  - Statische Factory `fromRegistration(email, passwordHash, vorname, nachname)`: nutzt `Profil.ermittleStatus(email)` aus dem bestehenden `profil/Profil.java` um den Type zu bestimmen
- `UserRepository extends JpaRepository<UserEntity, Long>` mit `Optional<UserEntity> findByEmail(String email)`

### D2: JwtUtil
Neues Package `mensa_app_backend/src/main/java/com/example/mensa_app_backend/security/`:
- `JwtUtil.java` als `@Component`:
  - `@Value("${jwt.secret}")` und `@Value("${jwt.expiration-days:30}")`
  - `SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))` (jjwt 0.12.x)
  - `generateToken(Long userId, String email, String type)`:
    - `Jwts.builder().subject(userId.toString()).claim("email", email).claim("type", type).issuedAt(...).expiration(...).signWith(key).compact()`
  - `validateToken(String token)`: `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` in try/catch → true/false
  - `extractUserId(String token)`: parst das Subject als Long

### D3: JwtAuthFilter
`security/JwtAuthFilter.java` als `@Component`, erbt `OncePerRequestFilter`:
- Injiziert `JwtUtil`
- `doFilterInternal`: liest `Authorization`-Header, extrahiert Bearer-Token
- Wenn Token gültig: `UsernamePasswordAuthenticationToken` mit `userId` als Principal, `ROLE_USER`-Authority, in `SecurityContextHolder` setzen
- Immer `filterChain.doFilter()` am Ende

### D4: SecurityConfig
`mensa_app_backend/src/main/java/com/example/mensa_app_backend/config/SecurityConfig.java` als `@Configuration @EnableWebSecurity`:
- Injiziert `JwtAuthFilter`
- `@Bean SecurityFilterChain filterChain(HttpSecurity http)`:
  - `csrf.disable()` (REST-API, keine CSRF nötig)
  - `sessionManagement.sessionCreationPolicy(STATELESS)`
  - `authorizeHttpRequests`:
    - `permitAll`: `/api/auth/**`, `/api/menu`, `/api/menu/**`, `/api/mensen`, `/api/mensen/**`, `/api/votes` (nur GET, also die öffentlichen Zähler), `/actuator/**`
    - `anyRequest().authenticated()`
  - `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`
- `@Bean BCryptPasswordEncoder passwordEncoder()`

### D5: AuthService
`user/AuthService.java` als `@Service`:
- Injiziert `UserRepository`, `JwtUtil`, `BCryptPasswordEncoder`
- Record `AuthResponse(String token, UserEntity user)`
- `register(email, password, vorname, nachname)`:
  - Check: `userRepository.findByEmail(email)` — wenn vorhanden, Fehler werfen
  - `passwordEncoder.encode(password)` → hash
  - `UserEntity.fromRegistration(...)` → `userRepository.save(user)`
  - Token generieren → `AuthResponse` zurückgeben
- `login(email, password)`:
  - User per Email finden, sonst Fehler
  - `passwordEncoder.matches(password, user.getPasswordHash())` — wenn false, Fehler
  - Token generieren → `AuthResponse` zurückgeben
  - Fehlermeldungen bewusst gleich ("Falsche E-Mail oder Passwort"), um User-Enumeration zu vermeiden

### D6: AuthController
`user/AuthController.java` als `@RestController @RequestMapping("/api/auth")`:
- Injiziert `AuthService`, `UserRepository`, `JwtUtil`
- Records `LoginRequest(String email, String password)`, `RegisterRequest(String email, String password, String vorname, String nachname)`
- `POST /api/auth/login`: ruft `authService.login(...)` auf, gibt `AuthResponse` als JSON zurück. Bei Fehler: 400
- `POST /api/auth/register`: ruft `authService.register(...)` auf, gibt `AuthResponse` zurück. Bei Fehler: 400
- `GET /api/auth/me`: liest `Authorization`-Header, validiert Token via `jwtUtil`, extrahiert userId, findet User via `userRepository.findById(userId)`, gibt User zurück. Bei ungültigem Token: 401 (nicht `ResponseEntity.unauthorized()` — das gibt es in Spring 6 nicht mehr, sondern `ResponseEntity.status(401).build()`)

### D7: pom.xml-Dependencies
In `mensa_app_backend/pom.xml` ergänzen:
- `spring-boot-starter-data-jpa`
- `org.postgresql:postgresql` (scope `runtime`)
- `spring-boot-starter-security`
- `io.jsonwebtoken:jjwt-api:0.12.6`
- `io.jsonwebtoken:jjwt-impl:0.12.6` (scope `runtime`)
- `io.jsonwebtoken:jjwt-jackson:0.12.6` (scope `runtime`)
- `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`

### D8: application.properties
`mensa_app_backend/src/main/resources/application.properties`:
```properties
server.port=8082
spring.application.name=mensa_app_backend
spring.datasource.url=jdbc:postgresql://localhost:5434/mensa_db
spring.datasource.username=mensa
spring.datasource.password=mensa123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=mensa_app_backend
mqtt.topics.speiseplan=mensa/speiseplan
mqtt.topics.mensen=mensa/mensen
mqtt.topics.orders=mensa/orders
mqtt.topics.votes=mensa/votes
jwt.secret=swt2-vibe-secret-key-min-32-characters-long
jwt.expiration-days=30
```

### D9: Bestehenden Code erhalten
- `profil/Profil.java` bleibt erhalten (enthält `ermittleStatus`-Logik, wird von `UserEntity.fromRegistration` genutzt)
- `profil/Abstimmung.java`, `profil/Hauptspeise.java` bleiben erhalten
- `profil/ProfilTest.java` muss von `src/main/java` nach `src/test/java` verschoben werden (falsche Ablage korrigieren)
- `bezahlsystem/*` bleibt erhalten

## Abhängigkeiten
- **Blockiert von:** Paket A (Docker + Repo)
- **Wird benötigt von:** Paket E (braucht SecurityConfig + JwtUtil), Paket G (Frontend-Login muss gegen `/api/auth` gehen)

## Abnahme-Kriterien
- `POST /api/auth/register` mit `{"email":"test@stud.fh-dortmund.de","password":"passwort","vorname":"Test","nachname":"User"}` → 200 + Token + User (type="student")
- `POST /api/auth/login` mit gleichen Credentials → 200 + Token
- `GET /api/auth/me` mit Bearer-Token → 200 + User-Daten
- `GET /api/auth/me` ohne/ungültigem Token → 401
- `POST /api/orders` ohne Token → 403
- Passwörter sind bcrypt-gehasht in der DB (nicht Klartext)

---

# Paket E: mensa_app_backend — Bestellwesen & MQTT-Subscriber
**Verantwortlich:** Johann Wenner

## Ziel
Bestellungen annehmen (REST), abspeichern (Postgres) und per MQTT an den Simulator weiterleiten. Votes同理. Speiseplan + Mensen vom Simulator via MQTT empfangen und cachen.

## Aufgaben (detailliert)

### E1: MqttConfig (App-Backend-Seite)
`mensa_app_backend/src/main/java/com/example/mensa_app_backend/config/MqttConfig.java` als `@Configuration`:
- Analog zu Paket C, aber gespiegelt:
- **Outbound** (Pub): `ordersOutboundChannel`, `votesOutboundChannel` + Handler (retained=false)
- **Inbound** (Sub): `speiseplanInboundChannel`, `mensenInboundChannel` + `MqttPahoMessageDrivenChannelAdapter`
- `setCleanSession(false)` damit retained-Nachrichten beim Verbinden zugestellt werden

### E2: MenuCache + MensaCache
Neues Package `mensa_app_backend/src/main/java/com/example/mensa_app_backend/menu/`:
- `MenuCache.java`: einfache Klasse mit `volatile List<Map<String, Object>> gerichte` (CopyOnWriteArrayList), `update(list)` und `getAll()`
- `mensa_app_backend/src/main/java/com/example/mensa_app_backend/mensa/MensaCache.java`: analog

### E3: MenuMqttHandler (MQTT-Subscriber)
`menu/MenuMqttHandler.java` als `@Configuration` mit inneren `@Component`-Klassen:
- `@Bean MenuCache menuCache()` und `@Bean MensaCache mensaCache()`
- Innere Klasse `SpeiseplanHandler`:
  - `@ServiceActivator(inputChannel = "speiseplanInboundChannel")`
  - Deserialisiert Payload als `List<Map<String, Object>>` via `objectMapper.readValue(payload, new TypeReference<List<Map<String, Object>>>() {})`
  - **WICHTIG:** Jackson 3.x → `import tools.jackson.core.type.TypeReference;` (nicht `com.fasterxml.jackson.databind.type`)
  - `cache.update(gerichte)` aufrufen
- Innere Klasse `MensaHandler`: analog für `mensenInboundChannel`

### E4: MenuService + MenuController (auf Cache umklemmen)
- Die bestehende `menu/MenuService.java` umschreiben: injiziert `MenuCache`, `getAllItems()` gibt `cache.getAll()`, `getItemById(id)` filtert
- Die bestehende `menu/MenuController.java` umschreiben: gibt `List<Map<String, Object>>` zurück statt `List<MenuItem>` (Cache-Daten sind Maps)
- Optional: `@GetMapping("/debug")` der `cacheSize` + `cacheItems` zurückgibt (für Diagnose)

### E5: MensaController (neu)
`mensa_app_backend/src/main/java/com/example/mensa_app_backend/mensa/MensaController.java`:
- `@RestController @RequestMapping("/api/mensen")`
- Injiziert `MensaCache`
- `GET /api/mensen` → `mensaCache.getAll()`

### E6: Order als JPA-Entity + OrderItem
Die bestehende `order/Order.java` (aktuell Record) umschreiben zu `@Entity @Table(name = "orders")`:
- Felder: `id`, `userId`, `studentName`, `status` (Default "OFFEN"), `total`, `pickupTime`, `code`
- `@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true) List<OrderItem> items`
- `addItem()`, `setStatus()`, `setTotal()` Methoden
- Neues `order/OrderItem.java` als `@Entity @Table(name = "order_item")`: `gerichtId`, `name`, `anzahl`, `preis`, `@ManyToOne Order order`

### E7: OrderRequest + OrderItemRequest
- `order/OrderRequest.java` als Record: `(List<OrderItemRequest> items, String pickupTime)`
- `order/OrderItemRequest.java` als Record: `(Long gerichtId, String name, int anzahl, double preis)`

### E8: OrderRepository
- `order/OrderRepository.java`: `extends JpaRepository<Order, Long>` mit `List<Order> findByUserId(Long userId)`

### E9: OrderService (DB + MQTT-Publish)
`order/OrderService.java` umschreiben:
- `@Service`, injiziert `OrderRepository`, `ordersOutboundChannel`, `ObjectMapper`
- **`@Transactional` auf `createOrder` und `getOrdersByUser` und `updateStatus`** (sonst `LazyInitializationException` bei `items`-Serialisierung)
- `createOrder(userId, studentName, request)`:
  - Neues Order-Objekt, Items hinzufügen, Total berechnen, `setTotal()`
  - `repository.save(order)`
  - `publishOrderViaMqtt(order)`: JSON-Map aus Order bauen, via `ordersOutboundChannel.send(MessageBuilder.withPayload(json).build())` verschicken
- `getOrdersByUser(userId)`: `repository.findByUserId(userId)` — **innerhalb der Transaction** werden items geladen
- `updateStatus(orderId, status)`: Order finden, setStatus, save
- `generateCode()`: 5-stelliger Code aus Großbuchstaben+Ziffern (ohne ambiguous chars)

### E10: OrderController
`order/OrderController.java` umschreiben:
- `@RestController @RequestMapping("/api/orders")`
- `GET /api/orders`: `Authentication auth` als Parameter, `userId = (Long) auth.getPrincipal()`, `orderService.getOrdersByUser(userId)`
- `POST /api/orders`: Authentication + `OrderRequest`, `orderService.createOrder(userId, "User "+userId, request)` (studentName könnte später aus UserEntity geholt werden)
- `PATCH /api/orders/{id}` mit `StatusUpdate(String status)`: `orderService.updateStatus(id, status)`

### E11: Vote-Entity + Repository + Service + Controller
Neues Package `mensa_app_backend/src/main/java/com/example/mensa_app_backend/voting/`:
- `Vote.java` als `@Entity @Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "gerichtId"}))`: `userId`, `gerichtId`
- `VoteRepository extends JpaRepository<Vote, Long>`: `findByUserId`, `findByUserIdAndGerichtId`, `findByGerichtId`
- `VoteService.java` als `@Service`, injiziert `VoteRepository`, `votesOutboundChannel`, `ObjectMapper`:
  - `getVoteCounts()`: gruppiert nach `gerichtId`, zählt → `Map<Long, Integer>`
  - `getMyVotes(userId)`: `findByUserId(userId).map(getGerichtId)`
  - `castVote(userId, gerichtId)`: Duplikat-Check, save, dann `publishVotesViaMqtt()` (aktuelle Zähler als Map verschicken)
- `VoteController.java`:
  - `GET /api/votes` (public): `voteService.getVoteCounts()`
  - `GET /api/votes/my` (auth): `voteService.getMyVotes(userId)`
  - `POST /api/votes/{gerichtId}` (auth): `voteService.castVote(userId, gerichtId)`, bei Duplikat 409

### E12: Preference-Entity + Controller
Neues Package `mensa_app_backend/src/main/java/com/example/mensa_app_backend/preference/`:
- `Preference.java` als `@Entity`: `userId`, `@ElementCollection List<String> dietary`, `@ElementCollection List<String> allergens`
- `PreferenceRepository`: `Optional<Preference> findByUserId(Long userId)`
- `PreferenceController.java` als `@RestController @RequestMapping("/api/profil/preferences")`:
  - `GET`: Preferences des Users (oder leere Listen wenn nicht vorhanden)
  - `PUT` mit `PreferenceUpdate(dietary, allergens)`: speichern

## Abhängigkeiten
- **Blockiert von:** Paket D (SecurityConfig, JwtUtil, UserEntity), Paket A (Mosquitto)
- **Wird benötigt von:** Paket F (Frontend braucht `/api/menu`, `/api/mensen`, `/api/votes`), Paket G (Frontend braucht `/api/orders`, `/api/profil/preferences`)

## Abnahme-Kriterien
- Nach Simulator-Start + App-Backend-Start: `curl http://localhost:8082/api/menu` liefert 12 Gerichte (via MQTT vom Simulator)
- `curl http://localhost:8082/api/mensen` liefert 8 Mensen
- `POST /api/orders` mit Token + Items → 200, Order in DB, MQTT-Nachricht an Simulator (`mosquitto_sub -t 'mensa/orders' -v`)
- `GET /api/orders` mit Token → Liste inklusive Items
- `POST /api/votes/{gerichtId}` → 200, Vote in DB, MQTT-Nachricht mit aktualisierten Zählern
- `GET /api/votes` ohne Token → aktuelle Zähler
- `GET/PUT /api/profil/preferences` mit Token → funktioniert

---

# Paket F: Frontend — Speiseplan, Standorte, Abstimmung
**Verantwortlich:** Viktoriia Dovzhenko

## Ziel
Drei der fünf Hauptseiten im Frontend: Speiseplan (mit Tages-/Wochenansicht + Filter), Standorte (Mensen mit Auslastung), Abstimmung (Wunschgericht-Voting).

## Setup (vorab)
- Ordner `frontend/` mit Vite + React 18 + Tailwind v4 + shadcn/ui
- `package.json` mit Dependencies: react, react-dom, react-router, lucide-react, date-fns, class-variance-authority, clsx, tailwind-merge, @radix-ui/* (slot, label, checkbox, dialog, separator), tailwindcss v4, @tailwindcss/vite, vite, @vitejs/plugin-react
- `vite.config.ts` mit `@`-Alias auf `./src` und Proxy `/api` → `http://localhost:8082`
- `src/styles/index.css` mit Tailwind-Import + Dortmund-Blau Theme (`--color-stwdo-primary: #003a70`)
- `src/lib/utils.ts` mit `cn()` Helper (twMerge + clsx)

## Aufgaben (detailliert)

### F1: shadcn UI-Primitives
In `src/components/ui/` folgende Primitive-Komponenten erstellen (minimal, nicht die volle shadcn-Library):
- `button.tsx`: mit `cva`-Varianten (default/outline/ghost/destructive) und Größen (default/sm/lg/icon)
- `badge.tsx`: mit Varianten (default/secondary/outline/green/red/blue/yellow/purple)
- `input.tsx`: styled `<input>`
- `label.tsx`: Radix `Label.Root` wrapper
- `card.tsx`: einfache Card-Div
- `checkbox.tsx`: Radix `Checkbox.Root` mit Check-Icon
- `sheet.tsx`: Radix `Dialog` als Bottom-Sheet (SheetContent mit `side="bottom"`, SheetHeader, SheetTitle)

### F2: AuthContext + CartContext
`src/context/AuthContext.tsx`:
- State: `user`, `token` (aus localStorage), `isLoggedIn`
- `login(token, user)`: speichert Token in localStorage, setzt State
- `logout()`: leert localStorage + State
- `useEffect`: bei Token → `api.auth.me()` aufrufen, User setzen
- `useAuth()` Hook

`src/context/CartContext.tsx`:
- State: `items` (Array aus `{item: MenuItem, anzahl: number}`)
- `addItem(item)`, `removeItem(gerichtId)`, `updateQuantity(gerichtId, anzahl)`, `clearCart()`
- Abgeleitet: `totalItems`, `totalPrice` (Summe der `anzahl * preisStudent`)
- `useCart()` Hook

### F3: api.ts (API-Client)
`src/lib/api.ts`:
- `BASE = "/api"` (Vite-Proxy leitet zum Backend)
- `request<T>(path, options)`: fetch mit JSON-Content-Type, Bearer-Token aus localStorage, Fehler-Behandlung
- **WICHTIG:** Bei leerem Response-Body `undefined` zurückgeben (nicht `res.json()` aufrufen — das crasht bei void-Responses wie Voting)
- TypeScript-Interfaces: `MenuItem`, `Mensa`, `Order`, `AuthUser`
- Export `api`-Objekt mit `auth`, `menu`, `mensen`, `orders`, `votes`, `profil` Untergruppen

### F4: Navigation
`src/components/Navigation.tsx`:
- 5 Tabs: Speiseplan (`/`, Home-Icon), Standorte (`/standorte`, MapPin), Bestellen (`/bestellungen`, ShoppingBag mit Cart-Badge), Voting (`/abstimmung`, ThumbsUp), Profil (`/profil`, User)
- **`fixed bottom-0`** mit z-50 (nicht `absolute` — sonst scrollt sie weg)
- Aktiver Tab: `text-[#003a70]`, inaktiv: `text-gray-500`
- Cart-Badge: blauer Kreis mit `totalItems` wenn > 0

### F5: Home.tsx (Speiseplan)
`src/pages/Home.tsx`:
- Header: dunkelblau `bg-[#003a70]`, Titel "Speiseplan", Untertitel "Mensa Dortmund"
- **Toggle Tages-/Wochenansicht** (Calendar/CalendarDays Icons) — nicht nur Icon wechseln, sondern Ansicht ändern!
- **Tagesansicht:**
  - Datum-Navigation (‹ ›): springt zum vorherigen/nächten Werktag (Wochenende überspringen)
  - Anzeige: "Heute" / "Morgen" / Wochentag + Datum (dd. MMMM yyyy, date-fns locale de)
  - Bei Wochenende: Hinweis "Mensa geschlossen"
  - Gerichte gruppiert nach Hauptgerichte / Beilagen, als `MenuCard`-Komponenten
- **Wochenansicht:**
  - Anzeige: "KW N" + Datumrange (Mo–Fr)
  - 5 Tageskarten (Mo–Fr), heutiger Tag hervorgehoben (`border-[#003a70]` + blauer Header)
  - Pro Tag: erste 3 Hauptspeisen + Beilagen-Liste
- Filter-Button (MenuFilters) im Header

### F6: MenuCard.tsx
`src/components/MenuCard.tsx`:
- Zeigt Gericht: Name, Beschreibung, Preis (Student/Gast je nach `user.type`), Tags (grüne Badges), Allergene (farbige Badges, jedes Allergen eigene Farbe)
- "Zum Warenkorb" Button → `addItem(item)`, wird für 1.5s grün "Hinzugefügt"

### F7: MenuFilters.tsx
`src/components/MenuFilters.tsx`:
- Sheet (Bottom-Sheet) mit Trigger-Button (Filter-Icon + gelber Counter bei aktiven Filtern)
- Ernährungsform-Checkboxes: VEGAN, VEGETARISCH, HALAL
- Allergene ausschließen: klickbare Badges (rot wenn aktiv)
- "Alle Filter zurücksetzen"-Button

### F8: Standorte.tsx
`src/pages/Standorte.tsx`:
- Header "Standorte / Mensen & Öffnungszeiten"
- Campus-Tabs: Alle / TU Dortmund / FH Dortmund
- Pro Mensa: Karte mit Name, Auslastung (Pill + Progress-Bar grün/gelb/rot), Öffnungszeiten (Clock-Icon), Adresse (MapPin-Icon)
- Expandierbar: "Route"-Button (Google-Maps-Link) + "Speiseplan"-Button (Link zu `/`)
- `useEffect` lädt `api.mensen.all()`

### F9: Abstimmung.tsx
`src/pages/Abstimmung.tsx`:
- Header "Wunschgericht-Voting / Bestimme den Speiseplan der kommenden Woche"
- Info-Bar: Gesamtzahl Stimmen
- Wenn nicht eingeloggt: amber Hinweis-Box "Zum Abstimmen bitte anmelden"
- **Top 3 Podium** (Trophy-Icon, Medaillen 🥇🥈🥉) — nur anzeigen wenn mindestens 1 Stimme abgegeben
- Liste aller Gerichte aus `api.menu.all()`:
  - Name, Beschreibung, "Stimmen"-Button (wird blau "Abgestimmt" nach Vote)
  - Progress-Bar pro Gericht
  - Ein Vote pro User pro Gericht (`api.votes.cast(gerichtId)`)
- `api.votes.counts()` für aktuelle Zähler, `api.votes.myVotes()` für bereits abgestimmt

## Abhängigkeiten
- **Blockiert von:** Paket A (Repo), Paket E (`/api/menu`, `/api/mensen`, `/api/votes` muss stehen)
- **Wird benötigt von:** nichts (unabhängig von Paket G)

## Abnahme-Kriterien
- `npm run dev` → `http://localhost:5173/`
- Speiseplan zeigt 12 Gerichte (wenn Backends laufen)
- Toggle Tages/Wochenansicht funktioniert
- Filter reduziert die Gerichte korrekt
- Standorte-Seite zeigt 8 Mensen
- Voting: nach Login kann man abstimmen, Zähler aktualisieren, Top 3 erscheint
- Optik: Dortmund-Blau, Inter-Font, 430px Mobile-First, Bottom-Tab-Navigation

---

# Paket G: Frontend — Bestellungen, Profil & Integration
**Verantwortlich:** Noel Koblitz

## Ziel
Die restlichen zwei Seiten: Bestellungen (Warenkorb → Checkout → QR-Code → Historie) und Profil (Login/Register + Preferences). Außerdem App.tsx-Zusammensetzung und finale Frontend-Integration.

## Aufgaben (detailliert)

### G1: App.tsx (Routing + Provider)
`src/App.tsx`:
- `AuthProvider` → `CartProvider` → `BrowserRouter` schachteln
- Mobile-First-Container: `max-w-[430px]`, `shadow-2xl`, `bg-white`, `min-h-screen`, `overflow-hidden`
- Routes: `/` (Home), `/standorte`, `/bestellungen`, `/abstimmung`, `/profil`
- `Navigation`-Komponente nach den Routes (damit sie immer sichtbar ist)
- `pb-20` auf Content, damit die fixed Navigation nichts überdeckt

### G2: Bestellungen.tsx (Warenkorb + Checkout)
`src/pages/Bestellungen.tsx` — die komplexeste Seite:

**Zustände:**
- `step`: `"cart" | "payment" | "pickup" | "processing" | "success"`
- `tab`: `"cart" | "history"`
- `paymentMethod`, `pickupTime`, `orders` (Historie), `lastOrder`

**Wenn nicht eingeloggt:** Hinweis + "Zum Login"-Button

**Warenkorb-Tab (step=cart):**
- Cart-Items: Name, Preis, Mengen-Stepper (− / +), Lösch-Button, Zeilenpreis
- Summary-Card: Zwischensumme, Abholung Gratis, Gesamt (blau)
- "Weiter zur Zahlung"-Button
- Empty-State mit ShoppingBag-Icon + "Zum Speiseplan"

**Payment-Step:**
- 2 Zahlungsmethoden-Karten: Kreditkarte (•••• 4242) + PayPal
- Radio-Style Auswahl (ausgewählt = `border-[#003a70]`)
- Gesamt-Anzeige
- "Weiter zur Abholzeit"-Button + Zurück

**Pickup-Step:**
- 2-Spalten-Grid mit 10-Minuten-Slots: ab jetzt+15min, auf 5min gerundet, bis 14:15
- Ausgewählter Slot hervorgehoben
- "Jetzt X € bezahlen"-Button (disabled wenn kein Slot gewählt)

**Processing-Step:**
- Fullscreen blauer Hintergrund, Spinner, "Zahlung wird verarbeitet..."
- Ruft `api.orders.create({items, pickupTime})` auf
- Bei Erfolg: `clearCart()`, `setLastOrder(order)`, `loadOrders()`, → success

**Success-Step:**
- Grüner CheckCircle, "Bestellung bestätigt"
- **Fake-QR-Code** (7×7 Grid aus dem Code-String deterministisch generiert — nicht echte QR-Encoding-Bibliothek)
- Großer Pickup-Code (z.B. "AB123")
- Abholzeit
- Buttons: "Meine Bestellungen" + "Neue Bestellung"

**Historie-Tab:**
- Liste vergangener Orders
- Pro Order: Code, Abholzeit, Status-Badge (In Bearbeitung/Bereit/Abgeholt), Items, Gesamt
- "Als abgeholt markieren"-Link (`api.orders.updateStatus(id, "ABGEHOLT")`)

### G3: Profil.tsx (Auth + Preferences)
`src/pages/Profil.tsx`:

**Wenn ausgeloggt — 4 Views:**
- **Landing**: User-Icon, "Nicht angemeldet", Anmelden/Registrieren Buttons
- **Login**: Email + Password Inputs (mit Icons), Show/Hide Password, Fehler-Box, "Anmelden"-Button mit Loading-State. Ruft `api.auth.login(email, password)` auf → `login(token, user)` aus Context
- **Register**: Vorname/Nachname (2-Spalten), Email, Password (mit Strength-Meter), "Registrieren"-Button. Ruft `api.auth.register(email, password, vorname, nachname)` auf
- **Verify** (optional): Bestätigungsseite

**Wenn eingeloggt — ProfileView:**
- Header: Initialien-Avatar, Name, Email, Preisgruppen-Badge (Studierendenpreis/Bediensteten/Gast)
- **Ernährungspräferenzen-Card** (Leaf-Icon):
  - Toggle-Badges für VEGAN/VEGETARISCH/HALAL
  - Allergen-Badges (rot wenn ausgeschlossen)
  - Änderungen sofort via `api.profil.updatePreferences(newPrefs)` speichern
- Abmelden-Button (rot outline)

### G4: main.tsx
`src/main.tsx`:
- `createRoot(document.getElementById("root")).render(<StrictMode><App/></StrictMode>)`
- Import `./styles/index.css`

### G5: index.html + Vite-Config
- `index.html` mit `<div id="root">` + Modul-Script
- `vite.config.ts` (siehe Paket F Setup, falls noch nicht vorhanden)

### G6: Frontend-Integration testen
- `npm install` läuft fehlerfrei
- `npm run build` produziert `dist/` ohne Fehler
- `npm run dev` → alle 5 Seiten erreichbar, Navigation funktioniert
- Mit laufenden Backends: Login → Speiseplan → Warenkorb → Checkout → Historie → Voting → Logout — kompletter Flow

### G7: .gitignore
`frontend/.gitignore`:
- `node_modules`, `dist`, `*.local`

## Abhängigkeiten
- **Blockiert von:** Paket D (`/api/auth` muss stehen), Paket E (`/api/orders`, `/api/profil/preferences` muss stehen), Paket F (Navigation, Contexts, api.ts, UI-Primitives)
- **Wird benötigt von:** nichts (Paket G schließt das Frontend ab)

## Abnahme-Kriterien
- Login-Form sendet korrektes JSON `{"email","password","vorname","nachname"}` an `/api/auth/register` (nicht nur Email-String!)
- Registrierung → Login → Warenkorb befüllen → Checkout → QR-Code + Code erscheint
- "Meine Bestellungen" zeigt die neue Order mit Items
- Profil-Seite: Preferences werden gespeichert und beim Neuladen wiederhergestellt
- `npm run build` grün
- Optik entspricht der Frontend-Vorlage (Dortmund-Blau, Mobile-First)

---

# Abhängigkeiten im Überblick

```
Paket A (Yaren)     — Infrastruktur
   ↓
Paket B (Joel)     Paket D (Valeriia)
   ↓                    ↓
Paket C (Leo)      Paket E (Johann)
   ↓                    ↓
              Paket F (Viktoriia) + Paket G (Noel)
                        ↓
                 Paket A (Yaren) — finale Integration
```

**Empfohlene Reihenfolge:**
1. Woche 1: Paket A (Infra) + Paket B (Entities) + Paket D (Auth) parallel
2. Woche 2: Paket C (MQTT-Simulator) + Paket E (MQTT-App) parallel
3. Woche 3: Paket F + G (Frontend) parallel
4. Woche 4: Integration, Tests, Doku (Paket A)

## Git-Branch-Regeln
- Jeder arbeitet auf `feature/<name>-<thema>` (z.B. `feature/joel-gericht-entities`)
- Commits: `feature/<thema>: <kurzbeschreibung>`
- PR → Review durch Yaren → Merge nach `main`
- `main` muss immer lauffähig sein (`./mvnw verify` grün)
