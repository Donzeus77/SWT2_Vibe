# IMPLEMENTIERUNG.md — Vom Template zum fertigen Projekt

Dieses Dokument beschreibt **detailliert und nachvollziehbar**, welche Änderungen am Original-Template (`SWT2/`) vorgenommen wurden, um den Endstand (`SWT2_vibe_coded/`) zu erreichen. Es ist die Grundlage, damit das Team denselben Stand Schritt für Schritt selbst erarbeiten kann.

Die Änderungen sind in **7 Aufgabenpakete (A–G)** unterteilt. Jedes Paket listet: **modifizierte Dateien**, **neu angelegte Dateien**, **exakte Annotationen/Felder/Methoden**, **Dependencies**, **`application.properties`-Einträge**, **Abhängigkeiten zu anderen Paketen** und **Abnahme-Kriterien**.

> **Konvention:** alle Pfadangaben sind relativ zum jeweiligen Modul-Root, also z. B. `src/main/java/com/example/studentenwerk_simulator/gericht/Gericht.java` meint `studentenwerk_simulator/src/main/java/...`. Beide Backends nutzen **Spring Boot 4.0.6 / Java 21** und damit **Jackson 3.x** (`tools.jackson.*`-Packages, nicht mehr `com.fasterxml.jackson.*`).

---

## Inhaltsverzeichnis

1. [Überblick: Pakete & Zuständigkeiten](#1-überblick-pakete--zuständigkeiten)
2. [Architektur & MQTT-Topics (für alle)](#2-architektur--mqtt-topics-für-alle)
3. [Paket A — Infrastruktur & Koordination](#paket-a--infrastruktur--koordination)
4. [Paket B — Simulator: Gericht-Domäne & Seed](#paket-b--simulator-gericht-domäne--seed)
5. [Paket C — Simulator: MQTT-Publisher & ReceivedOrders](#paket-c--simulator-mqtt-publisher--receivedorders)
6. [Paket D — App-Backend: Authentifizierung & User](#paket-d--app-backend-authentifizierung--user)
7. [Paket E — App-Backend: Bestellwesen & MQTT-Subscriber](#paket-e--app-backend-bestellwesen--mqtt-subscriber)
8. [Paket F — Frontend: Speiseplan, Standorte, Abstimmung](#paket-f--frontend-speiseplan-standorte-abstimmung)
9. [Paket G — Frontend: Bestellungen, Profil & Integration](#paket-g--frontend-bestellungen-profil--integration)
10. [Abhängigkeiten & empfohlene Reihenfolge](#10-abhängigkeiten--empfohlene-reihenfolge)

---

## 1. Überblick: Pakete & Zuständigkeiten

| # | Paket | Modul | Kern-Aufgabe |
|---|---|---|---|
| A | Infrastruktur & Koordination | Root + CI | docker-compose, Maven-Setup, CI, README, finale Integration |
| B | Gericht-Domäne & Seed | `studentenwerk_simulator` | JPA-Entities für Gericht-Hierarchie, Mensa, Seed-Runner, REST |
| C | MQTT-Publisher & ReceivedOrders | `studentenwerk_simulator` | MqttConfig, MqttPublisher, ReceivedOrder als Entity, VoteTotal |
| D | Authentifizierung & User | `mensa_app_backend` | UserEntity, JWT, SecurityConfig, AuthController |
| E | Bestellwesen & MQTT-Subscriber | `mensa_app_backend` | Order/OrderItem als Entities, MQTT-Sub, MenuCache, Vote, Preference |
| F | Frontend Speiseplan/Standorte/Voting | `frontend` | UI-Primitives, Contexts, api.ts, Home/Standorte/Abstimmung |
| G | Frontend Bestellungen/Profil & Integration | `frontend` | Bestellungen (5 Schritte), Profil (Auth+Prefs), App.tsx, Build |

**Datei-Zahlen pro Paket (gegenüber Template):**

| Paket | neu angelegt | modifiziert | identisch gelassen |
|---|---:|---:|---:|
| A | 3 (`docker-compose.yml`, `mosquitto/mosquitto.conf`, neues `README.md`) | 2 (`.github/workflows/pre-merge.yaml`, `start-services.ps1`) | `Dockerfile.java`, `pom.xml` (Root), `.gitignore`, `mvnw*` |
| B | 7 (`GerichtRepository`, `BeilageRepository`, `HauptspeiseRepository`, `Mensa`, `MensaRepository`, `GerichtController`, `MensaController`, `SeedData`) | 5 (`Gericht`, `Beilage`, `Hauptspeise`, `HauptgerichtFactory`, `BeilageFactory`) + `application.properties` + `pom.xml` | `Allergen`, `GerichtTag`, `GerichtFactory`, `Iterator/*`, `core/*` |
| C | 9 (`MqttConfig`, `MqttPublisher`, `ReceivedOrderItem`, `ReceivedOrderItemRequest`, `ReceivedOrderRepository`, `OrderMqttHandler`, `VoteTotal`, `VoteTotalRepository`, `VoteMqttHandler`) | 3 (`ReceivedOrder`, `ReceivedOrderRequest`, `ReceivedOrderService`) | `ReceivedOrderController` |
| D | 7 (`UserEntity`, `UserRepository`, `AuthService`, `AuthController`, `JwtUtil`, `JwtAuthFilter`, `SecurityConfig`) | 2 (`bezahlsystem/Gericht.java`, `bezahlsystem/Warenkorb_Item.java` — Bugfix equals) + `application.properties` + `pom.xml` | `Profil`, `Abstimmung`, `Hauptspeise`, `Bestellung`, `Warenkorb` |
| E | 13 (`MqttConfig`, `MenuCache`, `MenuMqttHandler`, `MensaCache`, `MensaController`, `OrderItem`, `OrderItemRequest`, `OrderRepository`, `Vote`, `VoteRepository`, `VoteService`, `VoteController`, `Preference` + Repo + Controller) | 4 (`Order`, `OrderRequest`, `OrderService`, `OrderController`, `MenuService`, `MenuController`) + `pom.xml` | `MenuItem`, `MensaAPI`, `MensaAdapter`, `bezahlsystem/*` |
| F | 16 (5 UI-Primitive + `Navigation` + `MenuCard` + `MenuFilters` + 2 Contexts + `api.ts` + `utils.ts` + `Home` + `Standorte` + `Abstimmung` + `index.css` + `vite.config.ts` + `package.json` + `index.html`) | — | — |
| G | 5 (`Bestellungen.tsx`, `Profil.tsx`, `App.tsx`, `main.tsx`, `.gitignore`) | — | — |

---

## 2. Architektur & MQTT-Topics (für alle)

```
Frontend (React-Vite, Port 5173)
  Speiseplan, Bestellen, Voting, Profil
     |
     | REST + JWT (JSON)
     v
mensa_app_backend (Port 8082)
  DB: mensa_db (Port 5434)
  Tabellen: users, orders, order_item, votes, user_preferences
     |
     | MQTT (via Mosquitto Broker, Port 1883)
     v
studentenwerk_simulator (Port 8081)
  DB: simulator_db (Port 5433)
  Tabellen: gericht, gericht_allergene, gericht_tags, mensa,
            received_order, received_order_item, vote_total

MQTT-Topics:
  mensa/speiseplan  — Simulator -> App (retained)
  mensa/mensen      — Simulator -> App (retained)
  mensa/orders      — App -> Simulator (nicht retained)
  mensa/votes       — App -> Simulator (nicht retained)
```

| Topic | Richtung | Retain | Payload |
|---|---|---|---|
| `mensa/speiseplan` | simulator → app | **ja** | JSON-Array aller Gerichte, jedes Element: `{id, name, beschreibung, preisStudent, preisGast, allergene[], tags[], typ}` |
| `mensa/mensen` | simulator → app | **ja** | JSON-Array aller Mensen: `{id, name, campus, adresse, oeffnungszeiten, auslastung}` |
| `mensa/orders` | app → simulator | nein | JSON-Objekt einer neuen Bestellung: `{studentName, total, pickupTime, code, items[{gerichtId, name, anzahl, preis}]}` |
| `mensa/votes` | app → simulator | nein | JSON-Map `{gerichtIdAlsString: count}` — **Keys müssen Strings sein** (JSON-Constraint) |

**Wichtig für alle Pakete:**
- `mqtt.broker.url`, `mqtt.client.id`, `mqtt.topics.*` werden in beiden `application.properties` gleich gepflegt, nur `mqtt.client.id` unterscheidet sich (`studentenwerk_simulator` vs. `mensa_app_backend`).
- Beide Backends nutzen **Spring Integration MQTT** (`MqttPahoClientFactory`, `MqttPahoMessageHandler`, `MqttPahoMessageDrivenChannelAdapter`) plus den Paho-Client direkt als Dependency.
- `setCleanSession(false)` ist Pflicht — sonst verpasst das App-Backend die retained-Nachrichten beim Startup.

---

## Paket A — Infrastruktur & Koordination

### Ziel
Setup, das alle 6 anderen Pakete brauchen: Docker-Compose mit 2 Postgres + Mosquitto, Maven-Setup, CI-Pipeline mit Test-Services, README, finale End-to-End-Integration.

### A1 — Root-POM (keine Änderung nötig)
`pom.xml` bleibt unverändert (Spring-Boot-Parent 4.0.6, Java 21, `<packaging>pom</packaging>`, Module `studentenwerk_simulator` + `mensa_app_backend`). **Wichtig:** Das Frontend ist **kein** Maven-Modul — es steht nur lose im Repo und wird via `npm` gebaut. Der Hinweis `./mvnw -pl frontend verify` in der README funktioniert nur, falls das Frontend später als Modul angebunden wird ( aktuell nicht nötig).

### A2 — `docker-compose.yml` (neu)
Pfad: `docker-compose.yml` im Repo-Root.
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
**Port-Mapping bewusst 5433/5434 → 5432**, damit zwei Postgres-Instanzen parallel laufen können, ohne sich in die Quere zu kommen.

### A3 — `mosquitto/mosquitto.conf` (neu)
Pfad: `mosquitto/mosquitto.conf` mit genau:
```
listener 1883
allow_anonymous true
```
Ohne `allow_anonymous true` lehnt Mosquitto 2.x alle Verbindungen ab (Default hat sich gegenüber 1.x geändert).

### A4 — `.github/workflows/pre-merge.yaml` (modifiziert)
Im `verify`-Job **vor** dem Maven-Build zwei Postgres-Service-Container + Mosquitto-Container einfügen. Konkret ergänzen:

```yaml
jobs:
  verify:
    runs-on: ubuntu-latest
    services:
      postgres-simulator:
        image: postgres:16
        env: { POSTGRES_DB: simulator_db, POSTGRES_USER: mensa, POSTGRES_PASSWORD: mensa123 }
        ports: ["5433:5432"]
        options: >-
          --health-cmd "pg_isready -U mensa -d simulator_db"
          --health-interval 5s --health-timeout 5s --health-retries 10
      postgres-app:
        image: postgres:16
        env: { POSTGRES_DB: mensa_db, POSTGRES_USER: mensa, POSTGRES_PASSWORD: mensa123 }
        ports: ["5434:5432"]
        options: >-
          --health-cmd "pg_isready -U mensa -d mensa_db"
          --health-interval 5s --health-timeout 5s --health-retries 10
    steps:
      - uses: actions/checkout@v4
      - name: Start Mosquitto
        run: |
          docker run -d --name mosquitto -p 1883:1883 \
            -v "$PWD/mosquitto/mosquitto.conf:/mosquitto/config/mosquitto.conf" \
            eclipse-mosquitto:2
          for i in $(seq 1 20); do
            if docker exec mosquitto mosquitto_sub -t '$SYS/broker/version' -C 1 -W 2 > /dev/null 2>&1; then
              echo "Mosquitto is ready"; break
            fi
            sleep 1
          done
      - uses: actions/setup-java@v4.5.0
        with: { java-version: '21', distribution: 'temurin', cache: maven }
      - run: ./mvnw --batch-mode --update-snapshots verify
```
Der `discover-deployable-modules`- und `container-check`-Job bleiben unverändert.

### A5 — `start-services.ps1` (modifiziert)
Original verwies auf nicht-existentierte Module `app` und `order_service` (Template-Reste). Neue Version:
- Startet zuerst `docker compose up -d` in neuem Fenster ("Starte Infrastruktur…")
- `Start-Sleep -Seconds 5` warten, bis DB + Broker oben sind
- Startet `mvnw.cmd -pl studentenwerk_simulator spring-boot:run` (Port 8081)
- Startet `mvnw.cmd -pl mensa_app_backend spring-boot:run` (Port 8082)
- Frontend bewusst **nicht** automatisch starten (kann während Entwicklung stören)

### A6 — `README.md` (komplett neu)
Template-README war englisch, referenzierte `app`/`order_service`. Neue README auf Deutsch:
- Architektur-Diagramm (siehe Section 2 oben)
- Modul-Tabelle (3 Module: Simulator 8081/simulator_db, App 8082/mensa_db, Frontend 5173/—)
- MQTT-Topic-Tabelle
- Voraussetzungen: Java 21, Docker, Node 20+, Maven Wrapper
- Quickstart: `docker compose up -d` → beide Backends → Frontend (`npm install && npm run dev`)
- Endpunkt-Übersicht (`/api/gerichte`, `/api/menu`, `/actuator/health`, `:5173`)
- Team-Liste
- Build (`./mvnw verify`)

### A7 — `Dockerfile.java` (unverändert lassen!)
Trotz nicht passendem Default `MODULE=app` und `EXPOSE 8080` **nicht anfassen** — das ist Teil des Templates, den der `container-check`-Job erwartet. Wird nur bei explizitem `--build-arg MODULE=...` gebaut.

### A8 — Finale Integration & Abnahmetests
Nach Merge aller Pakete End-to-End durchpielen:
1. `docker compose up -d` → 3 Container grün
2. `./mvnw -pl studentenwerk_simulator spring-boot:run` → Log zeigt "Publiziere Speiseplan mit N Gerichten"
3. `./mvnw -pl mensa_app_backend spring-boot:run` → Log zeigt MQTT-Subscribe
4. `curl http://localhost:8082/api/menu` → 12 Gerichte
5. Frontend: Register → Login → Speiseplan → Warenkorb → Checkout → QR-Code → Historie → Voting → Logout

### Abnahme-Kriterien Paket A
- [ ] `docker compose up -d` startet 3 Container ohne Fehler
- [ ] `mosquitto_sub -t 'mensa/#' -v` verbindet sich erfolgreich
- [ ] `./mvnw verify` läuft grün **in der CI** (mit Postgres+Mosquitto-Services)
- [ ] `README.md` vollständig, deutsch, keine Template-Reste mehr
- [ ] `start-services.ps1` startet Infra + beide Backends mit korrekten Modulnamen

---

## Paket B — Simulator: Gericht-Domäne & Seed

### Ziel
Aus den Plain-Java-Klassen des Templates (`Gericht`, `Beilage`, `Hauptspeise` mit `final`-Feldern) echte JPA-Entities machen, mit `@Inheritance(SINGLE_TABLE)` statt `@MappedSuperclass`, mit echten Dortmunder Mensa-Daten seeden, per REST bereitstellen.

### B1 — `gericht/Gericht.java` (modifiziert)
Original: abstrakte Klasse, `private final`-Felder, kein JPA. **Änderungen:**

```java
@Entity
@Table(name = "gericht")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "typ")
public abstract class Gericht {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                     // NEU

    private String name;                  // war final → jetzt mutable
    private String beschreibung;          // war final → mutable
    private double preisStudent;          // war final → mutable
    private double preisGast;             // war final → mutable

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gericht_allergene")
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    private Set<Allergen> allergene;      // war final → mutable

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gericht_tags")
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Set<GerichtTag> tags;         // war final → mutable

    protected Gericht() {}                // NEU — No-Arg für JPA Pflicht

    // bestehender 6-Arg-Konstruktor bleibt erhalten
    // bestehende Getter bleiben erhalten
    // NEU: public Long getId() { return id; }
    // toString() bleibt unverändert
}
```

### B2 — `gericht/Beilage.java` (modifiziert)
```java
@Entity
@DiscriminatorValue("BEILAGE")
public class Beilage extends Gericht {
    protected Beilage() {}                // NEU
    // bestehender Konstruktor + getTyp() ("Beilage") bleiben
}
```

### B3 — `gericht/Hauptspeise.java` (modifiziert)
```java
@Entity
@DiscriminatorValue("HAUPTSPEISE")
public class Hauptspeise extends Gericht {
    protected Hauptspeise() {}            // NEU
    // bestehender Konstruktor + getTyp() ("Hauptspeise") bleiben
}
```

### B4 — `gericht/Allergen.java` + `gericht/GerichtTag.java` (unverändert)
Beide Enums bleiben 1:1 wie im Template. Keine JPA-Annotationen nötig — die `@Enumerated(EnumType.STRING)` sitzt auf den `@ElementCollection`-Feldern in `Gericht`.

### B5 — `gericht/factory/HauptgerichtFactory.java` + `BeilageFactory.java` (modifiziert)
Je nur **zwei Zeilen ergänzt**:
```java
import org.springframework.stereotype.Component;
@Component
public class HauptgerichtFactory implements GerichtFactory { /* Body unverändert */ }
// analog BeilageFactory
```
`GerichtFactory` (Interface) bleibt unverändert. Dadurch werden die Factories per `@Autowired` in `SeedData` injizierbar.

### B6 — Repositories (neu, je 5 Zeilen)
```java
// gericht/GerichtRepository.java
public interface GerichtRepository extends JpaRepository<Gericht, Long> {}

// gericht/BeilageRepository.java
public interface BeilageRepository extends JpaRepository<Beilage, Long> {}

// gericht/HauptspeiseRepository.java
public interface HauptspeiseRepository extends JpaRepository<Hauptspeise, Long> {}
```
`GerichtRepository` wird aktuell nicht direkt genutzt, aber der Vollständigkeit halber anlegen.

### B7 — `mensa/Mensa.java` (neu)
```java
@Entity
@Table(name = "mensa")
public class Mensa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String campus;            // "TU Dortmund" oder "FH Dortmund"
    private String adresse;
    private String oeffnungszeiten;  // z.B. "Mo–Fr 11:15–14:15"
    private String auslastung;        // "low" | "medium" | "high"

    protected Mensa() {}
    public Mensa(String name, String campus, String adresse, String oeffnungszeiten, String auslastung) { ... }
    // Getter für alle Felder (keine Setter nötig — Mensa ist praktisch immutable nach Seed)
}
```

### B8 — `mensa/MensaRepository.java` (neu)
```java
public interface MensaRepository extends JpaRepository<Mensa, Long> {}
```

### B9 — `controller/GerichtController.java` (neu)
```java
@RestController
@RequestMapping("/api/gerichte")
public class GerichtController {
    private final HauptspeiseRepository hauptspeiseRepository;
    private final BeilageRepository beilageRepository;
    // Konstruktor-Injection

    @GetMapping
    public List<Gericht> getAllGerichte() {
        List<Gericht> all = new ArrayList<>();
        all.addAll(hauptspeiseRepository.findAll());
        all.addAll(beilageRepository.findAll());
        return all;
    }
}
```
Wichtig: ein einzelner `gerichtRepository.findAll()` würde **auch** gehen (wegen SINGLE_TABLE), aber die Aufsplittung zeigt klar, dass es Subklassen sind. Frontend gruppier später nach `typ`-Feld.

### B10 — `controller/MensaController.java` (neu)
```java
@RestController
@RequestMapping("/api/mensen")
public class MensaController {
    private final MensaRepository mensaRepository;
    @GetMapping
    public List<Mensa> getAllMensen() { return mensaRepository.findAll(); }
}
```

### B11 — `config/SeedData.java` (neu, ~139 Zeilen)
```java
@Configuration
public class SeedData {
    @Bean
    CommandLineRunner seedDatabase(HauptspeiseRepository hRepo, BeilageRepository bRepo,
                                    MensaRepository mRepo, HauptgerichtFactory hFactory,
                                    BeilageFactory bFactory) {
        return args -> {
            if (hRepo.count() > 0 || bRepo.count() > 0) return;   // nur seeden wenn leer

            // 8 Hauptspeisen via Factory:
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Currywurst mit Pommes", "Bratwurst mit Currysauce", 3.50, 5.50,
                Set.of(), Set.of()));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Veganes Chili sin Carne", "Mit Reis und Brot", 3.20, 5.00,
                Set.of(), Set.of(VEGAN, VEGETARISCH)));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Pasta Bolognese", "Italienischer Klassiker", 3.80, 6.00,
                Set.of(GLUTEN, EI, MILCH), Set.of()));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Vegane Pasta Pesto", "Mit Basilikum-Pesto", 3.50, 5.50,
                Set.of(GLUTEN), Set.of(VEGAN)));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Hähnchenbrust mit Reis", "Mit Gemüse", 4.20, 6.50, Set.of(), Set.of()));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Schnitzel Wiener Art", "Paniertes Schweineschnitzel", 4.50, 7.00,
                Set.of(GLUTEN, EI), Set.of()));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Vegane Linsensuppe", "Mit Brot", 2.50, 4.00,
                Set.of(), Set.of(VEGAN, VEGETARISCH)));
            hRepo.save((Hauptspeise) hFactory.createGericht(
                "Falafel-Teller", "Mit Hummus und Salat", 4.00, 6.20,
                Set.of(GLUTEN, SESAM), Set.of(VEGAN, VEGETARISCH, HALAL)));

            // 4 Beilagen via Factory:
            bRepo.save((Beilage) bFactory.createGericht(
                "Pommes frites", "Knusprig", 1.50, 2.50, Set.of(), Set.of(VEGAN, VEGETARISCH)));
            bRepo.save((Beilage) bFactory.createGericht(
                "Kartoffelsalat", "Mit Essig-Öl-Dressing", 1.80, 2.80,
                Set.of(EI, MILCH), Set.of(VEGETARISCH)));
            bRepo.save((Beilage) bFactory.createGericht(
                "Gemischter Salat", "Frisch vom Buffet", 2.00, 3.20,
                Set.of(MILCH), Set.of(VEGAN, VEGETARISCH)));
            bRepo.save((Beilage) bFactory.createGericht(
                "Reis", "Basmati", 1.20, 2.00, Set.of(), Set.of(VEGAN, VEGETARISCH)));

            // 8 Dortmunder Mensen:
            mRepo.save(new Mensa("Hauptmensa (TU)", "TU Dortmund",
                "Sonnenstraße 165, 44137 Dortmund", "Mo–Fr 11:15–14:15", "medium"));
            mRepo.save(new Mensa("Archeteria (TU)", "TU Dortmund",
                "August-Schmidt-Straße 1, 44227 Dortmund", "Mo–Fr 11:15–14:15", "low"));
            mRepo.save(new Mensa("Mensa Baroper Stern (TU)", "TU Dortmund",
                "Baroper Straße 287, 44227 Dortmund", "Mo–Fr 11:15–14:15", "low"));
            mRepo.save(new Mensa("Mensa Cantstraße (TU)", "TU Dortmund",
                "Cantstraße 4, 44227 Dortmund", "Mo–Fr 11:15–14:15", "medium"));
            mRepo.save(new Mensa("FH Dortmund Mensa", "FH Dortmund",
                "Sonnenstraße 96, 44139 Dortmund", "Mo–Fr 11:15–14:15", "high"));
            mRepo.save(new Mensa("Mensa Emil-Figge-Straße (FH)", "FH Dortmund",
                "Emil-Figge-Straße 42, 44227 Dortmund", "Mo–Fr 11:15–14:15", "medium"));
            mRepo.save(new Mensa("Mensa Sonnenstraße (FH)", "FH Dortmund",
                "Sonnenstraße 100, 44139 Dortmund", "Mo–Fr 11:15–14:15", "low"));
            mRepo.save(new Mensa("Mensa Westfalenhütte (TU)", "TU Dortmund",
                "Westfalenhütte 1, 44135 Dortmund", "Mo–Fr 11:15–14:15", "low"));
        };
    }
}
```

### B12 — `application.properties` (modifiziert)
```properties
server.port=8081
spring.application.name=studentenwerk_simulator

# PostgreSQL (simulator_db)
spring.datasource.url=jdbc:postgresql://localhost:5433/simulator_db
spring.datasource.username=mensa
spring.datasource.password=mensa123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# MQTT
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=studentenwerk_simulator
mqtt.topics.speiseplan=mensa/speiseplan
mqtt.topics.mensen=mensa/mensen
mqtt.topics.orders=mensa/orders
mqtt.topics.votes=mensa/votes
```
`ddl-auto=update` reicht für die Entwicklung (Hibernate legt Tabellen an, ändert aber nicht). In Produktion würde man `validate` + Flyway nehmen.

### B13 — `studentenwerk_simulator/pom.xml` (modifiziert)
Drei Dependencies ergänzen:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.5</version>
</dependency>
```
Bestehende (`spring-boot-starter-web`, `-actuator`, `-integration`, `spring-integration-mqtt`, `-test`) bleiben.

### Abnahme-Kriterien Paket B
- [ ] `curl http://localhost:8081/api/gerichte` liefert 12 Gerichte als JSON-Array (8 Hauptspeisen + 4 Beilagen)
- [ ] `curl http://localhost:8081/api/mensen` liefert 8 Mensen
- [ ] Nach `docker compose down -v && docker compose up -d` + Neustart: DB wird neu geseedet
- [ ] In der `gericht`-Tabelle gibt es eine `typ`-Spalte mit Werten `HAUPTSPEISE` bzw. `BEILAGE`
- [ ] `gericht_allergene` und `gericht_tags` sind eigene Tabellen ohne konfligierende FK-Spalten

### Abhängigkeiten
- **Blockiert von:** Paket A (Docker + Repo)
- **Wird benötigt von:** Paket C (braucht Entities + Repos + Seed), Paket E (JSON-Struktur der Gerichte muss zur Map-Serialisierung passen)

---

## Paket C — Simulator: MQTT-Publisher & ReceivedOrders

### Ziel
Den Simulator per MQTT an den Broker anhängen: Speiseplan + Mensen publishen (retained), eingehende Bestellungen und Votes empfangen und in der DB ablegen.

### C1 — `config/MqttConfig.java` (neu, ~110 Zeilen)
```java
@Configuration
public class MqttConfig {
    @Value("${mqtt.broker.url}") private String brokerUrl;
    @Value("${mqtt.client.id}")   private String clientId;
    @Value("${mqtt.topics.speiseplan}") private String speiseplanTopic;
    @Value("${mqtt.topics.mensen}")     private String mensenTopic;
    @Value("${mqtt.topics.orders}")     private String ordersTopic;
    @Value("${mqtt.topics.votes}")      private String votesTopic;

    @Bean public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();
        MqttConnectOptions o = new MqttConnectOptions();
        o.setServerURIs(new String[]{brokerUrl});
        o.setAutomaticReconnect(true);
        o.setCleanSession(false);                    // WICHTIG: sonst verpasst der Sub retained msgs
        f.setConnectionOptions(o);
        return f;
    }

    // ---- Outbound (Publisher) ----
    @Bean public MessageChannel speiseplanOutboundChannel() { return new DirectChannel(); }
    @Bean public MessageChannel mensenOutboundChannel()     { return new DirectChannel(); }

    @Bean @ServiceActivator(inputChannel = "speiseplanOutboundChannel")
    public MessageHandler speiseplanOutboundHandler() {
        MqttPahoMessageHandler h = new MqttPahoMessageHandler(clientId + "-pub-speiseplan", mqttClientFactory());
        h.setAsync(true);
        h.setDefaultTopic(speiseplanTopic);
        h.setDefaultRetained(true);                  // retained — App-Backend bekommt Speiseplan beim Subscribe
        return h;
    }
    @Bean @ServiceActivator(inputChannel = "mensenOutboundChannel")
    public MessageHandler mensenOutboundHandler() {
        MqttPahoMessageHandler h = new MqttPahoMessageHandler(clientId + "-pub-mensen", mqttClientFactory());
        h.setAsync(true); h.setDefaultTopic(mensenTopic); h.setDefaultRetained(true);
        return h;
    }

    // ---- Inbound (Subscriber) ----
    @Bean public MessageChannel ordersInboundChannel() { return new DirectChannel(); }
    @Bean public MessageChannel votesInboundChannel()  { return new DirectChannel(); }

    @Bean public MqttPahoMessageDrivenChannelAdapter ordersInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter a =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-sub-orders", mqttClientFactory(), ordersTopic);
        a.setOutputChannel(ordersInboundChannel());
        return a;
    }
    @Bean public MqttPahoMessageDrivenChannelAdapter votesInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter a =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-sub-votes", mqttClientFactory(), votesTopic);
        a.setOutputChannel(votesInboundChannel());
        return a;
    }

    // Getter für die Topics (für MqttPublisher)
    public String getSpeiseplanTopic() { return speiseplanTopic; }
    public String getMensenTopic()     { return mensenTopic; }
    public String getOrdersTopic()     { return ordersTopic; }
    public String getVotesTopic()      { return votesTopic; }
}
```

### C2 — `config/MqttPublisher.java` (neu, ~102 Zeilen)
```java
@Component
public class MqttPublisher {
    private final MessageChannel speiseplanOutboundChannel;
    private final MessageChannel mensenOutboundChannel;
    private final HauptspeiseRepository hauptspeiseRepository;
    private final BeilageRepository beilageRepository;
    private final MensaRepository mensaRepository;
    private final ObjectMapper objectMapper;        // tools.jackson.databind.ObjectMapper!

    @EventListener(ApplicationReadyEvent.class)
    public void publishOnStartup() {
        publishSpeiseplan();
        publishMensen();
    }

    public void publishSpeiseplan() {
        try {
            List<Map<String,Object>> list = new ArrayList<>();
            for (Gericht g : hauptspeiseRepository.findAll()) list.add(gerichtToMap(g));
            for (Gericht g : beilageRepository.findAll())     list.add(gerichtToMap(g));
            String json = objectMapper.writeValueAsString(list);
            speiseplanOutboundChannel.send(MessageBuilder.withPayload(json).build());
            System.out.println("Publiziere Speiseplan mit " + list.size() + " Gerichten");
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren des Speiseplans: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishMensen() {
        try {
            List<Map<String,Object>> list = mensaRepository.findAll().stream()
                .map(m -> Map.of("id", m.getId(), "name", m.getName(), "campus", m.getCampus(),
                     "adresse", m.getAdresse(), "oeffnungszeiten", m.getOeffnungszeiten(),
                     "auslastung", m.getAuslastung()))
                .toList();
            String json = objectMapper.writeValueAsString(list);
            mensenOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren der Menschen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String,Object> gerichtToMap(Gericht g) {
        return Map.of(
            "id", g.getId(), "name", g.getName(), "beschreibung", g.getBeschreibung(),
            "preisStudent", g.getPreisStudent(), "preisGast", g.getPreisGast(),
            "allergene", g.getAllergene(), "tags", g.getTags(), "typ", g.getTyp()
        );
    }
}
```
**Wichtig:** `ObjectMapper` ist in Spring Boot 4 / Jackson 3 aus `tools.jackson.databind.ObjectMapper` zu importieren — **nicht** aus `com.fasterxml.jackson.databind`. `allergene`/`tags` sind `Set<Enum>` und werden dank `@Enumerated(EnumType.STRING)` automatisch als String-Listen serialisiert.

### C3 — `orderreceiver/ReceivedOrder.java` (modifiziert — war 3-Zeilen-Record, jetzt 56-Zeilen-Entity)
Original: `public record ReceivedOrder(Long id, Long menuItemId, String studentName, String status) {}`

Neu:
```java
@Entity
@Table(name = "received_order")
public class ReceivedOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String studentName;
    private String status;       // Default "EINGEGANGEN" (im Konstruktor gesetzt)
    private double total;
    private String pickupTime;
    private String code;
    private LocalDateTime receivedAt;   // im Konstruktor auf LocalDateTime.now() gesetzt

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivedOrderItem> items = new ArrayList<>();

    protected ReceivedOrder() {}
    public ReceivedOrder(String studentName, double total, String pickupTime, String code) {
        this.studentName = studentName; this.total = total;
        this.pickupTime = pickupTime;   this.code = code;
        this.status = "EINGEGANGEN";    this.receivedAt = LocalDateTime.now();
    }

    public void addItem(ReceivedOrderItem item) {
        items.add(item);
        item.setOrder(this);            // bidirektionale Invariante pflegen!
    }
    // Getter für alle Felder, getItems(), getId()
}
```

### C4 — `orderreceiver/ReceivedOrderItem.java` (neu)
```java
@Entity
@Table(name = "received_order_item")
public class ReceivedOrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long gerichtId;
    private String name;
    private int anzahl;
    private double preis;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private ReceivedOrder order;

    protected ReceivedOrderItem() {}
    public ReceivedOrderItem(Long gerichtId, String name, int anzahl, double preis) { ... }
    public void setOrder(ReceivedOrder order) { this.order = order; }
    // Getter
}
```

### C5 — `orderreceiver/ReceivedOrderRequest.java` (modifiziert — Record-Signatur geändert)
Original: `record ReceivedOrderRequest(Long menuItemId, String studentName) {}`
Neu:
```java
public record ReceivedOrderRequest(
    String studentName,
    double total,
    String pickupTime,
    String code,
    List<ReceivedOrderItemRequest> items
) {}
```

### C6 — `orderreceiver/ReceivedOrderItemRequest.java` (neu)
```java
public record ReceivedOrderItemRequest(Long gerichtId, String name, int anzahl, double preis) {}
```

### C7 — `orderreceiver/ReceivedOrderRepository.java` (neu)
```java
public interface ReceivedOrderRepository extends JpaRepository<ReceivedOrder, Long> {}
```

### C8 — `orderreceiver/ReceivedOrderService.java` (modifiziert — In-Memory → DB)
Original: `ArrayList<ReceivedOrder>` + `AtomicLong nextId`. Neu:
```java
@Service
public class ReceivedOrderService {
    private final ReceivedOrderRepository repository;
    // Konstruktor-Injection

    public ReceivedOrder receiveOrder(ReceivedOrderRequest request) {
        ReceivedOrder order = new ReceivedOrder(
            request.studentName(), request.total(), request.pickupTime(), request.code());
        if (request.items() != null) {
            for (ReceivedOrderItemRequest i : request.items()) {
                order.addItem(new ReceivedOrderItem(i.gerichtId(), i.name(), i.anzahl(), i.preis()));
            }
        }
        return repository.save(order);
    }

    public List<ReceivedOrder> getAllOrders() { return repository.findAll(); }
}
```

### C9 — `orderreceiver/ReceivedOrderController.java` (unverändert lassen)
Der Controller (`@RestController @RequestMapping("/api/received-orders")`, `@GetMapping` + `@PostMapping`) bleibt 1:1 — er nutzt weiterhin den Service, der jetzt DB-backed ist. Strategie: **Service-Signatur stabil halten, Internals austauschen.**

### C10 — `orderreceiver/OrderMqttHandler.java` (neu)
```java
@Component
public class OrderMqttHandler {
    private final ReceivedOrderService receivedOrderService;
    private final ObjectMapper objectMapper;        // tools.jackson.*

    @ServiceActivator(inputChannel = "ordersInboundChannel")
    public void handleOrder(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            ReceivedOrderRequest request = objectMapper.readValue(payload, ReceivedOrderRequest.class);
            receivedOrderService.receiveOrder(request);
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten der Bestellung via MQTT: " + e.getMessage());
        }
    }
}
```

### C11 — `voting/VoteTotal.java` (neu)
```java
@Entity
@Table(name = "vote_total")
public class VoteTotal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long gerichtId;
    private int count;

    protected VoteTotal() {}
    public VoteTotal(Long gerichtId, int count) { this.gerichtId = gerichtId; this.count = count; }
    public void setCount(int count) { this.count = count; }
    // Getter
}
```

### C12 — `voting/VoteTotalRepository.java` (neu)
```java
public interface VoteTotalRepository extends JpaRepository<VoteTotal, Long> {
    Optional<VoteTotal> findByGerichtId(Long gerichtId);
}
```

### C13 — `voting/VoteMqttHandler.java` (neu)
```java
@Component
public class VoteMqttHandler {
    private final VoteTotalRepository repository;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    @ServiceActivator(inputChannel = "votesInboundChannel")
    public void handleVotes(Message<?> message) {
        try {
            String payload = message.getPayload().toString();
            Map<String,Integer> voteMap = objectMapper.readValue(payload, Map.class);
            for (Map.Entry<String,Integer> e : voteMap.entrySet()) {
                Long gerichtId = Long.parseLong(e.getKey());       // JSON-Keys sind Strings!
                int count = e.getValue();
                VoteTotal vt = repository.findByGerichtId(gerichtId)
                    .orElseGet(() -> new VoteTotal(gerichtId, 0));
                vt.setCount(count);
                repository.save(vt);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Verarbeiten der Votes via MQTT: " + e.getMessage());
        }
    }
}
```
**Wichtig:** `objectMapper.readValue(payload, Map.class)` mit raw `Map` + `@SuppressWarnings("unchecked")` — bewusst **kein** `TypeReference<Map<String,Integer>>`, weil JSON-Objekt-Keys immer Strings sind und Jackson das automatisch so deserialisiert. Cast auf `Integer`-Value klappt, weil Jackson Zahlen als Integer/Long erkennt (solange klein genug).

### Abnahme-Kriterien Paket C
- [ ] Beim Simulator-Start: Log "Publiziere Speiseplan mit 12 Gerichten"
- [ ] `mosquitto_sub -t 'mensa/speiseplan' -v` zeigt das JSON-Array einmalig (retained)
- [ ] `mosquitto_sub -t 'mensa/mensen' -v` zeigt das JSON-Array einmalig (retained)
- [ ] Wenn App-Backend eine Nachricht auf `mensa/orders` published: neue Zeile in `received_order` + `received_order_item` Tabelle
- [ ] `curl http://localhost:8081/api/received-orders` zeigt die eingegangene Bestellung inkl. Items
- [ ] Vote-Nachrichten auf `mensa/votes` landen in der `vote_total`-Tabelle

### Abhängigkeiten
- **Blockiert von:** Paket B (Entities + Repos + Seed), Paket A (Mosquitto via Docker)
- **Wird benötigt von:** Paket E (App-Backend muss dieselbe Payload-Struktur für `mensa/orders` und `mensa/votes` senden)

---

## Paket D — App-Backend: Authentifizierung & User

### Ziel
User-Registrierung, Login, JWT-Ausgabe, Security-Filter, User-Entity. Das bestehende `Profil.ermittleStatus(email)` (Matrikel-Enumeration) wird wiederverwendet.

### D1 — `user/UserEntity.java` (neu)
```java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String passwordHash;
    private String vorname;
    private String nachname;
    private String type;        // "student" | "mitarbeiter" | "gast"

    protected UserEntity() {}
    public UserEntity(String email, String passwordHash, String vorname, String nachname, String type) { ... }

    // Brücke zu bestehendem Profil:
    public static UserEntity fromRegistration(String email, String passwordHash, String vorname, String nachname) {
        String type = Profil.ermittleStatus(email);   // bestehende Methode aus profil/Profil.java
        return new UserEntity(email, passwordHash, vorname, nachname, type);
    }
    // Getter für alle Felder
}
```

### D2 — `user/UserRepository.java` (neu)
```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
```

### D3 — `security/JwtUtil.java` (neu, jjwt 0.12.x)
```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expiration-days:30}") private long expirationDays;

    private SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    public String generateToken(Long userId, String email, String type) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", type)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(expirationDays, ChronoUnit.DAYS)))
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) { return false; }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject());
    }
}
```
**Wichtig:** `jwt.secret` muss **mindestens 32 Zeichen** lang sein (HMAC-SHA256 Anforderung). In `application.properties`: `jwt.secret=swt2-vibe-secret-key-min-32-characters-long`. jjwt 0.12.x API: `Jwts.builder().subject(...).signWith(key).compact()` und `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` — **nicht** mehr `SignatureAlgorithm.HS256` und `Jwts.parser().setSigningKey(...)` wie bei jjwt 0.11.x.

### D4 — `security/JwtAuthFilter.java` (neu)
```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);    // IMMER aufrufen — auch bei fehlendem Token (sonst hängt Request)
    }
}
```
**Wichtig:** Der `principal` ist danach eine `Long` (die userId) — das brauchen Paket E und Paket G später: `(Long) auth.getPrincipal()`.

### D5 — `config/SecurityConfig.java` (neu)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                                  // REST-API, keine CSRF-Cookies
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/menu", "/api/menu/**",
                    "/api/mensen", "/api/mensen/**",
                    "/api/votes",                          // NUR bare /api/votes (öffentliche Zähler)
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```
**Wichtig:** `/api/votes/my` und `/api/votes/{gerichtId}` sind **nicht** in `permitAll` → sie erfordern Auth. Nur das bare `/api/votes` (öffentliche Zähler) ist offen.

### D6 — `user/AuthService.java` (neu)
```java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public record AuthResponse(String token, UserEntity user) {}

    public AuthResponse register(String email, String password, String vorname, String nachname) {
        if (userRepository.findByEmail(email).isPresent())
            throw new IllegalArgumentException("E-Mail bereits registriert");
        String hash = passwordEncoder.encode(password);
        UserEntity user = UserEntity.fromRegistration(email, hash, vorname, nachname);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getType());
        return new AuthResponse(token, user);
    }

    public AuthResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Falsche E-Mail oder Passwort"));
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Falsche E-Mail oder Passwort"); // gleiche Msg → keine User-Enumeration
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getType());
        return new AuthResponse(token, user);
    }
}
```

### D7 — `user/AuthController.java` (neu)
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String vorname, String nachname) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try { return ResponseEntity.ok(authService.login(req.email(), req.password())); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().build(); }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try { return ResponseEntity.ok(authService.register(req.email(), req.password(), req.vorname(), req.nachname())); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().build(); }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).build();
        Long userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).build());
    }
}
```
**Wichtig:** `/api/auth/me` parst das Token **selbst** neu — es verlässt sich **nicht** auf `SecurityContextHolder.getContext().getAuthentication()`. Das ist robuster (funktioniert auch, falls der Filter noch nicht gelaufen ist) und einfacher zu testen. **Kein** `ResponseEntity.unauthorized()` verwenden — das gibt es in Spring 6 / Boot 4 nicht mehr, nur `ResponseEntity.status(401).build()`.

### D8 — `bezahlsystem/Gericht.java` + `bezahlsystem/Warenkorb_Item.java` (Bugfix: equals/hashCode)
Original hatten beide Klassen **kein** `equals`/`hashCode`. `Warenkorb.getIndexOf(...)` funktionierte deshalb nicht (verglich per Identität). Neu:

`Gericht.java` ergänzt:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Gericht g)) return false;
    return Double.compare(g.preis, this.preis) == 0;
}
@Override
public int hashCode() { return Double.hashCode(preis); }
```

`Warenkorb_Item.java` ergänzt (statt dem `// TODO: equals überschreiben`):
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Warenkorb_Item that)) return false;
    return gericht != null && gericht.equals(that.gericht);
}
@Override
public int hashCode() { return gericht != null ? gericht.hashCode() : 0; }
```
Die anderen `bezahlsystem/*`-Klassen (`Bestellung`, `Warenkorb`) bleiben unverändert.

### D9 — `profil/ProfilTest.java` (verschieben)
Datei **inhaltlich unverändert**, aber von `src/main/java/.../profil/ProfilTest.java` nach `src/test/java/.../profil/ProfilTest.java` verschieben. Maven beschwert sich sonst nicht, aber es ist die Konvention.

`profil/Profil.java`, `profil/Abstimmung.java`, `profil/Hauptspeise.java`, `bezahlsystem/Bestellung.java`, `bezahlsystem/Warenkorb.java` bleiben **alle unverändert**. Wichtig: `Profil.ermittleStatus(email)` wird von `UserEntity.fromRegistration` weiterverwendet — also nicht anfassen!

### D10 — `application.properties` (modifiziert)
```properties
server.port=8082
spring.application.name=mensa_app_backend

# PostgreSQL (mensa_db)
spring.datasource.url=jdbc:postgresql://localhost:5434/mensa_db
spring.datasource.username=mensa
spring.datasource.password=mensa123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# MQTT
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=mensa_app_backend
mqtt.topics.speiseplan=mensa/speiseplan
mqtt.topics.mensen=mensa/mensen
mqtt.topics.orders=mensa/orders
mqtt.topics.votes=mensa/votes

# JWT
jwt.secret=swt2-vibe-secret-key-min-32-characters-long
jwt.expiration-days=30
```
**Wichtig:** Port `5434` (nicht 5433 wie beim Simulator) und DB-Name `mensa_db` (nicht `simulator_db`).

### D11 — `mensa_app_backend/pom.xml` (modifiziert)
Sieben Dependencies ergänzen:
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
<dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.6</version></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
<dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
<dependency><groupId>org.eclipse.paho</groupId><artifactId>org.eclipse.paho.client.mqttv3</artifactId><version>1.2.5</version></dependency>
```

### Abnahme-Kriterien Paket D
- [ ] `POST /api/auth/register` mit `{"email":"test@stud.fh-dortmund.de","password":"passwort","vorname":"Test","nachname":"User"}` → 200 + `{token, user}`, `user.type === "student"`
- [ ] `POST /api/auth/login` mit denselben Credentials → 200 + Token
- [ ] `GET /api/auth/me` mit `Authorization: Bearer <token>` → 200 + User
- [ ] `GET /api/auth/me` ohne/ungültigem Token → 401
- [ ] `POST /api/orders` ohne Token → 403 (Auth nötig)
- [ ] In der `users`-Tabelle: `password_hash` ist bcrypt (`$2a$...`), nicht Klartext
- [ ] `Warenkorb.getIndexOf(...)` funktioniert (Paket-übergreifender Test: Warenkorb_Item mit gleichem Gericht findet sich)

### Abhängigkeiten
- **Blockiert von:** Paket A (Docker + Repo)
- **Wird benötigt von:** Paket E (braucht `SecurityConfig`, `JwtUtil`, `UserEntity`), Paket G (Frontend-Login geht gegen `/api/auth`)

---

## Paket E — App-Backend: Bestellwesen & MQTT-Subscriber

### Ziel
Bestellungen annehmen (REST), in Postgres abspeichern, per MQTT an Simulator weiterleiten. Votes ebenso. Speiseplan + Mensen vom Simulator via MQTT empfangen und cachen. Preferences verwalten.

### E1 — `config/MqttConfig.java` (neu — gespiegelt zu Paket C)
Wie Paket C, aber **Outbound/Inbound vertauscht**:
```java
@Configuration
public class MqttConfig {
    // @Value für brokerUrl, clientId, 4 topics — wie Paket C

    @Bean public MqttPahoClientFactory mqttClientFactory() {
        // setAutomaticReconnect(true), setCleanSession(false) — WICHTIG: retained msgs sonst weg
    }

    // Outbound (Pub) — retained = false!
    @Bean public MessageChannel ordersOutboundChannel() { return new DirectChannel(); }
    @Bean public MessageChannel votesOutboundChannel()  { return new DirectChannel(); }
    @Bean @ServiceActivator(inputChannel = "ordersOutboundChannel")
    public MessageHandler ordersOutboundHandler() {
        MqttPahoMessageHandler h = new MqttPahoMessageHandler(clientId + "-pub-orders", mqttClientFactory());
        h.setAsync(true); h.setDefaultTopic(ordersTopic); h.setDefaultRetained(false);
        return h;
    }
    @Bean @ServiceActivator(inputChannel = "votesOutboundChannel")
    public MessageHandler votesOutboundHandler() { /* analog, retained=false */ }

    // Inbound (Sub) — lauscht auf speiseplan + mensen
    @Bean public MessageChannel speiseplanInboundChannel() { return new DirectChannel(); }
    @Bean public MessageChannel mensenInboundChannel()     { return new DirectChannel(); }
    @Bean public MqttPahoMessageDrivenChannelAdapter speiseplanInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter a =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-sub-speiseplan", mqttClientFactory(), speiseplanTopic);
        a.setOutputChannel(speiseplanInboundChannel());
        return a;
    }
    @Bean public MqttPahoMessageDrivenChannelAdapter mensenInboundAdapter() { /* analog */ }
}
```
**Wichtig:** `setDefaultRetained(false)` für orders/votes — eine Bestellung soll beim Subscribe **nicht** erneut zugestellt werden, im Gegensatz zum Speiseplan.

### E2 — `menu/MenuCache.java` (neu)
```java
public class MenuCache {
    private volatile List<Map<String,Object>> gerichte = new CopyOnWriteArrayList<>();
    public void update(List<Map<String,Object>> gerichte) {
        this.gerichte = new CopyOnWriteArrayList<>(gerichte);
    }
    public List<Map<String,Object>> getAll() { return gerichte; }
}
```
`volatile` + `CopyOnWriteArrayList` = thread-safe, weil der MQTT-Handler in einem anderen Thread läuft als die REST-Requests.

### E3 — `mensa/MensaCache.java` (neu)
Analog zu `MenuCache`, Feld `mensen`.

### E4 — `menu/MenuMqttHandler.java` (neu — `@Configuration` mit inneren `@Component`-Klassen)
```java
@Configuration
public class MenuMqttHandler {
    @Bean public MenuCache menuCache() { return new MenuCache(); }
    @Bean public MensaCache mensaCache() { return new MensaCache(); }   // MensaCache-Bean liegt hier, nicht im mensa-Package

    @Component
    public static class SpeiseplanHandler {
        private final MenuCache cache;
        private final ObjectMapper objectMapper;
        @ServiceActivator(inputChannel = "speiseplanInboundChannel")
        public void handleSpeiseplan(Message<?> message) {
            try {
                String payload = message.getPayload().toString();
                List<Map<String,Object>> gerichte = objectMapper.readValue(
                    payload, new TypeReference<List<Map<String,Object>>>() {});   // tools.jackson.core.type.TypeReference!
                cache.update(gerichte);
                System.out.println("Speiseplan empfangen: " + gerichte.size() + " Gerichte");
            } catch (Exception e) {
                System.err.println("Fehler beim Empfang des Speiseplans: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Component
    public static class MensaHandler { /* analog für mensenInboundChannel + MensaCache */ }
}
```

### E5 — `menu/MenuService.java` (modifiziert — auf Cache umgeklemmt)
Original: hard-codierte `List<MenuItem>` mit 4 Test-Einträgen. Neu:
```java
@Service
public class MenuService {
    private final MenuCache menuCache;
    // Konstruktor-Injection

    public List<Map<String,Object>> getAllItems() { return menuCache.getAll(); }

    public Optional<Map<String,Object>> getItemById(Long id) {
        return menuCache.getAll().stream()
            .filter(item -> id.equals(item.get("id")))
            .findFirst();
    }
}
```
**Wichtig:** Return-Typ ändert sich von `List<MenuItem>` zu `List<Map<String,Object>>`. Das Frontend kriegt also rohe Maps (mit `id`, `name`, `preisStudent`, `allergene` etc.) — kein typisiertes DTO. Das ist bewusst so, weil die Struktur 1:1 durchgereicht wird, was der Simulator published.

### E6 — `menu/MenuController.java` (modifiziert)
```java
@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public List<Map<String,Object>> getAllMenuItems() {
        List<Map<String,Object>> items = menuService.getAllItems();
        System.out.println("=== /api/menu aufgerufen, Cache hat " + items.size() + " Gerichte ===");
        return items;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getById(@PathVariable Long id) {
        return menuService.getItemById(id)
            .<ResponseEntity<Map<String,Object>>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/debug")                                       // Diagnose-Endpoint
    public Map<String,Object> debug() {
        List<Map<String,Object>> items = menuService.getAllItems();
        return Map.of("cacheSize", items.size(), "cacheItems", items);
    }
}
```
Der `/debug`-Endpoint hilft bei der Diagnose, um zu prüfen, ob der Cache befüllt ist.

### E7 — `menu/MenuItem.java` (unverändert lassen)
`record MenuItem(Long id, String name, String description, double price)` bleibt bestehen. Wird von `adapter/MensaAPI` und `adapter/MensaAdapter` noch importiert, aber **nicht** mehr von `MenuService`/`MenuController`. Nicht aufräumen — Risk of breaking the adapter stubs.

### E8 — `mensa/MensaController.java` (neu)
```java
@RestController
@RequestMapping("/api/mensen")
public class MensaController {
    private final MensaCache mensaCache;
    @GetMapping
    public List<Map<String,Object>> getAllMensen() { return mensaCache.getAll(); }
}
```

### E9 — `order/Order.java` (modifiziert — war Record, jetzt Entity)
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;            // NEU — verknüpft mit authenticated User
    private String studentName;
    private String status;          // Default "OFFEN" (im Konstruktor)
    private double total;
    private String pickupTime;
    private String code;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}
    public Order(Long userId, String studentName, double total, String pickupTime, String code) {
        this.userId = userId; this.studentName = studentName; this.total = total;
        this.pickupTime = pickupTime; this.code = code; this.status = "OFFEN";
    }

    public void setStatus(String status) { this.status = status; }
    public void setTotal(double total) { this.total = total; }
    public void addItem(OrderItem item) { items.add(item); item.setOrder(this); }
    // Getter für alle Felder + getItems()
}
```

### E10 — `order/OrderItem.java` (neu)
```java
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long gerichtId;
    private String name;
    private int anzahl;
    private double preis;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    protected OrderItem() {}
    public OrderItem(Long gerichtId, String name, int anzahl, double preis) { ... }
    public void setOrder(Order order) { this.order = order; }
    // Getter
}
```

### E11 — `order/OrderRequest.java` + `order/OrderItemRequest.java` (Records)
```java
public record OrderRequest(List<OrderItemRequest> items, String pickupTime) {}
public record OrderItemRequest(Long gerichtId, String name, int anzahl, double preis) {}
```

### E12 — `order/OrderRepository.java` (neu)
```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
```

### E13 — `order/OrderService.java` (modifiziert — DB + MQTT-Publish)
```java
@Service
public class OrderService {
    private final OrderRepository repository;
    private final MessageChannel ordersOutboundChannel;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(Long userId, String studentName, OrderRequest request) {
        Order order = new Order(userId, studentName, 0, request.pickupTime(), generateCode());
        double total = 0;
        for (OrderItemRequest i : request.items()) {
            order.addItem(new OrderItem(i.gerichtId(), i.name(), i.anzahl(), i.preis()));
            total += i.preis() * i.anzahl();
        }
        order.setTotal(total);
        order = repository.save(order);
        publishOrderViaMqtt(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {
        return repository.findByUserId(userId);   // items werden dank @Transactional geladen
    }

    @Transactional
    public Order updateStatus(Long orderId, String status) {
        Order o = repository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order nicht gefunden"));
        o.setStatus(status);
        return repository.save(o);
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";  // I, O, 0, 1 entfernt (ambiguous)
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 5; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void publishOrderViaMqtt(Order order) {
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("studentName", order.getStudentName());
            payload.put("total", order.getTotal());
            payload.put("pickupTime", order.getPickupTime());
            payload.put("code", order.getCode());
            payload.put("items", order.getItems().stream().map(i -> Map.of(
                "gerichtId", i.getGerichtId(), "name", i.getName(),
                "anzahl", i.getAnzahl(), "preis", i.getPreis()
            )).toList());
            String json = objectMapper.writeValueAsString(payload);
            ordersOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) {
            System.err.println("Fehler beim Publizieren der Bestellung via MQTT: " + e.getMessage());
            // kein rethrow — DB-Write bleibt bestehen, MQTT-Fehler loggen wir nur
        }
    }
}
```

### E14 — `order/OrderController.java` (modifiziert)
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public record StatusUpdate(String status) {}

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();              // vom JwtAuthFilter gesetzt
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(Authentication auth, @RequestBody OrderRequest request) {
        Long userId = (Long) auth.getPrincipal();
        // studentName als Platzhalter — könnte später aus UserEntity geholt werden
        Order order = orderService.createOrder(userId, "User " + userId, request);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdate update) {
        try { return ResponseEntity.ok(orderService.updateStatus(id, update.status())); }
        catch (IllegalArgumentException e) { return ResponseEntity.notFound().build(); }
    }
}
```
**Wichtig:** `(Long) auth.getPrincipal()` — der `JwtAuthFilter` (Paket D) setzt `userId` als Principal. Wenn `principal` plötzlich ein `String` wäre, wäre das ein Bug im Filter.

### E15 — `voting/Vote.java` + `VoteRepository.java` + `VoteService.java` + `VoteController.java` (neu)
```java
@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId","gerichtId"}))
public class Vote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long gerichtId;
    protected Vote() {}
    public Vote(Long userId, Long gerichtId) { ... }
    // Getter
}

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserId(Long userId);
    Optional<Vote> findByUserIdAndGerichtId(Long userId, Long gerichtId);
    List<Vote> findByGerichtId(Long gerichtId);
}

@Service
public class VoteService {
    private final VoteRepository repository;
    private final MessageChannel votesOutboundChannel;
    private final ObjectMapper objectMapper;

    public Map<Long,Integer> getVoteCounts() {
        return repository.findAll().stream()
            .collect(Collectors.groupingBy(Vote::getGerichtId, Collectors.summingInt(v -> 1)));
    }

    public List<Long> getMyVotes(Long userId) {
        return repository.findByUserId(userId).stream().map(Vote::getGerichtId).toList();
    }

    public void castVote(Long userId, Long gerichtId) {
        if (repository.findByUserIdAndGerichtId(userId, gerichtId).isPresent())
            throw new IllegalStateException("Bereits abgestimmt");
        repository.save(new Vote(userId, gerichtId));
        publishVotesViaMqtt();
    }

    private void publishVotesViaMqtt() {
        try {
            Map<Long,Integer> counts = getVoteCounts();
            Map<String,Integer> payload = new HashMap<>();
            for (Map.Entry<Long,Integer> e : counts.entrySet())
                payload.put(e.getKey().toString(), e.getValue());    // JSON-Keys müssen Strings sein!
            String json = objectMapper.writeValueAsString(payload);
            votesOutboundChannel.send(MessageBuilder.withPayload(json).build());
        } catch (Exception e) { System.err.println("Fehler beim Publish der Votes: " + e.getMessage()); }
    }
}

@RestController
@RequestMapping("/api/votes")
public class VoteController {
    private final VoteService voteService;

    @GetMapping                                            // PUBLIC (permitAll in SecurityConfig)
    public Map<Long,Integer> getVoteCounts() { return voteService.getVoteCounts(); }

    @GetMapping("/my")                                     // AUTH nötig
    public ResponseEntity<List<Long>> getMyVotes(Authentication auth) {
        return ResponseEntity.ok(voteService.getMyVotes((Long) auth.getPrincipal()));
    }

    @PostMapping("/{gerichtId}")                           // AUTH nötig, 409 bei Duplikat
    public ResponseEntity<?> castVote(Authentication auth, @PathVariable Long gerichtId) {
        try { voteService.castVote((Long) auth.getPrincipal(), gerichtId); return ResponseEntity.ok().build(); }
        catch (IllegalStateException e) { return ResponseEntity.status(409).build(); }
    }
}
```

### E16 — `preference/Preference.java` + `PreferenceRepository.java` + `PreferenceController.java` (neu)
```java
@Entity
@Table(name = "user_preferences")
public class Preference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> dietary = new ArrayList<>();        // ["VEGAN", "VEGETARISCH", "HALAL"]

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> allergens = new ArrayList<>();      // ["GLUTEN", "MILCH", ...]

    protected Preference() {}
    public Preference(Long userId) { this.userId = userId; }
    public void setDietary(List<String> d) { this.dietary = d; }
    public void setAllergens(List<String> a) { this.allergens = a; }
    // Getter
}

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    Optional<Preference> findByUserId(Long userId);
}

@RestController
@RequestMapping("/api/profil/preferences")
public class PreferenceController {
    private final PreferenceRepository repository;           // kein separater Service — direkt ans Repo

    public record PreferenceUpdate(List<String> dietary, List<String> allergens) {}

    @GetMapping
    public ResponseEntity<?> getPreferences(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Preference pref = repository.findByUserId(userId).orElse(null);
        if (pref == null) return ResponseEntity.ok(Map.of("dietary", List.of(), "allergens", List.of()));
        return ResponseEntity.ok(Map.of("dietary", pref.getDietary(), "allergens", pref.getAllergens()));
    }

    @PutMapping
    public ResponseEntity<?> updatePreferences(Authentication auth, @RequestBody PreferenceUpdate update) {
        Long userId = (Long) auth.getPrincipal();
        Preference pref = repository.findByUserId(userId).orElseGet(() -> new Preference(userId));
        pref.setDietary(update.dietary() != null ? update.dietary() : List.of());
        pref.setAllergens(update.allergens() != null ? update.allergens() : List.of());
        repository.save(pref);
        return ResponseEntity.ok().build();
    }
}
```
**Wichtig:** `dietary`/`allergens` sind `List<String>`, **nicht** `List<Enum>` — das Frontend schickt beliebige Strings, das Backend speichert sie roh ab. Kein `@Enumerated` nötig.

### E17 — `mensa_app_backend/pom.xml` (modifiziert)
Siehe Paket D11 — dieselben Dependencies (Paket D hat sie schon ergänzt; Paket E profitiert nur davon). Falls Paket D noch nicht gemergt ist: Paket E muss zumindest `spring-boot-starter-data-jpa`, `org.postgresql:postgresql`, `org.eclipse.paho.client.mqttv3` sicherstellen.

### Abnahme-Kriterien Paket E
- [ ] Nach Simulator- + App-Backend-Start: `curl http://localhost:8082/api/menu` liefert 12 Gerichte
- [ ] `curl http://localhost:8082/api/menu/debug` → `{"cacheSize":12,"cacheItems":[...]}`
- [ ] `curl http://localhost:8082/api/mensen` liefert 8 Mensen
- [ ] `POST /api/orders` mit Token + Items → 200, Order in DB, MQTT-Nachricht auf `mensa/orders` (via `mosquitto_sub -t 'mensa/orders' -v` sichtbar), neue Zeile in Simulator-DB `received_order`
- [ ] `GET /api/orders` mit Token → Liste inkl. Items (kein Lazy-Fehler!)
- [ ] `POST /api/votes/{gerichtId}` → 200, Vote in DB, MQTT-Nachricht mit aktualisierten Zählern, `vote_total`-Tabelle im Simulator aktualisiert
- [ ] Zweiter `POST /api/votes/{gerichtId}` mit gleichem User → 409
- [ ] `GET /api/votes` ohne Token → aktuelle Zähler (public)
- [ ] `GET /api/votes/my` ohne Token → 403
- [ ] `PUT /api/profil/preferences` mit Token → 200, bei erneutem `GET` kommen die Werte zurück

### Abhängigkeiten
- **Blockiert von:** Paket D (`SecurityConfig`, `JwtUtil`, `UserEntity` — `(Long) auth.getPrincipal()`), Paket A (Mosquitto)
- **Wird benötigt von:** Paket F (`/api/menu`, `/api/mensen`, `/api/votes`), Paket G (`/api/orders`, `/api/profil/preferences`)

---

## Paket F — Frontend: Speiseplan, Standorte, Abstimmung

### Ziel
Frontend-Gerüst + drei der fünf Hauptseiten + UI-Primitives + Contexts + api.ts. Mobile-First (max 430px), Dortmund-Blau, fixed Bottom-Navigation.

### F1 — `frontend/` Setup (neu)
**`package.json`** — Dependencies:
- Runtime: `react@18.3.1`, `react-dom@18.3.1`, `react-router@7.13.0` (v7 — Achtung, **kein** `react-router-dom`-Package mehr!), `lucide-react@0.487.0`, `date-fns@3.6.0`, `class-variance-authority@0.7.1`, `clsx@2.1.1`, `tailwind-merge@3.2.0`, `@radix-ui/react-slot@1.1.2`, `@radix-ui/react-label@2.1.2`, `@radix-ui/react-checkbox@1.1.4`, `@radix-ui/react-dialog@1.1.6`, `@radix-ui/react-separator@1.1.2` (unbenutzt, kann weg)
- Dev: `@tailwindcss/vite@4.1.12`, `@vitejs/plugin-react@4.7.0`, `tailwindcss@4.1.12`, `tw-animate-css@1.3.8`, `vite@6.3.5`
- Scripts: `dev`, `build`, `preview` (alle via `vite`)

**`vite.config.ts`:**
```ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "path";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: { alias: { "@": path.resolve(__dirname, "./src") } },
  server: { proxy: { "/api": "http://localhost:8082" } }     // /api → App-Backend
});
```

**`index.html`:** `<html lang="de">`, `<title>Mensa App</title>`, `<div id="root">`, `<script type="module" src="/src/main.tsx">`.

**`src/styles/index.css`:**
```css
@import "tailwindcss";
@import "tw-animate-css";

@theme {
  --color-stwdo-primary: #003a70;
  --color-stwdo-dark: #002a52;
  --color-stwdo-light: #0052a3;
  --color-stwdo-secondary: #1976d2;
  --color-stwdo-accent: #0277bd;
  --radius: 0.625rem;
}

@layer base {
  body { font-family: Inter, sans-serif; font-weight: 300; }
  h1, h2, h3, button { font-family: "Inter", sans-serif; font-weight: 700; }
}
```

**`src/lib/utils.ts`:**
```ts
import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
export function cn(...inputs: ClassValue[]) { return twMerge(clsx(inputs)); }
```

### F2 — UI-Primitives (`src/components/ui/`)
Sieben shadcn-style Primitive (minimal, nicht die volle shadcn-Library):

| Datei | Zweck | Wichtig |
|---|---|---|
| `button.tsx` | `cva` mit Varianten `default/outline/ghost/destructive` + Größen `default/sm/lg/icon`, `asChild` via Radix `Slot`, `forwardRef` | default = `bg-[#003a70]` |
| `badge.tsx` | `cva` mit Varianten `default/secondary/outline/green/red/blue/yellow/purple` | plain `<div>` |
| `input.tsx` | styled `<input>` mit `focus:ring-[#003a70]`, `forwardRef` | — |
| `label.tsx` | wrappt `@radix-ui/react-label` `Label.Root` | — |
| `card.tsx` | `<div>` mit `rounded-2xl border border-gray-100 bg-white shadow-sm` | — |
| `checkbox.tsx` | wrappt `@radix-ui/react-checkbox` `Checkbox.Root` mit Check-Icon | checked = `bg-[#003a70]` |
| `sheet.tsx` | wrappt `@radix-ui/react-dialog` als Bottom-Sheet | `SheetContent` mit `side="bottom"`, overlay `bg-black/50 z-50` |

### F3 — `src/lib/api.ts` (neu, ~102 Zeilen)
```ts
const BASE = "/api";
const getToken = () => localStorage.getItem("authToken");

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string,string> = { "Content-Type": "application/json", ...options.headers as any };
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;
  const res = await fetch(`${BASE}${path}`, { ...options, headers });
  if (!res.ok) {
    let msg = "Serverfehler";
    try { const err = await res.json(); msg = err.error || msg; } catch {}
    throw new Error(msg);
  }
  const text = await res.text();
  if (!text) return undefined as T;          // WICHTIG: void-Responses (z. B. Voting) → undefined
  return JSON.parse(text) as T;
}

export interface MenuItem { id: number; name: string; beschreibung: string;
  preisStudent: number; preisGast: number; allergene: string[]; tags: string[]; typ: string; }
export interface Mensa { id: number; name: string; campus: string; adresse: string;
  oeffnungszeiten: string; auslastung: string; }
export interface Order { id: number; code: string; pickupTime: string; status: string;
  total: number; items: { gerichtId: number; name: string; anzahl: number; preis: number }[]; }
export interface AuthUser { id: number; email: string; vorname: string; nachname: string; type: string; }

export const api = {
  auth: {
    login: (email: string, password: string) => request<{token:string; user:AuthUser}>("/auth/login",
      { method: "POST", body: JSON.stringify({ email, password }) }),
    register: (email: string, password: string, vorname: string, nachname: string) =>
      request<{token:string; user:AuthUser}>("/auth/register",
        { method: "POST", body: JSON.stringify({ email, password, vorname, nachname }) }),
    me: () => request<AuthUser>("/auth/me"),
  },
  menu: {
    all: () => request<MenuItem[]>("/menu"),
    byId: (id: number) => request<MenuItem>(`/menu/${id}`),
  },
  mensen: { all: () => request<Mensa[]>("/mensen") },
  orders: {
    all: () => request<Order[]>("/orders"),
    create: (data: { items: {gerichtId:number; name:string; anzahl:number; preis:number}[]; pickupTime: string }) =>
      request<Order>("/orders", { method: "POST", body: JSON.stringify(data) }),
    updateStatus: (id: number, status: string) =>
      request<void>(`/orders/${id}`, { method: "PATCH", body: JSON.stringify({ status }) }),
  },
  votes: {
    counts: () => request<Record<string,number>>("/votes"),
    myVotes: () => request<number[]>("/votes/my"),
    cast: (gerichtId: number) => request<void>(`/votes/${gerichtId}`, { method: "POST" }),
  },
  profil: {
    preferences: () => request<{ dietary: string[]; allergens: string[] }>("/profil/preferences"),
    updatePreferences: (data: { dietary: string[]; allergens: string[] }) =>
      request<void>("/profil/preferences", { method: "PUT", body: JSON.stringify(data) }),
  },
};
```

### F4 — `src/context/AuthContext.tsx` (neu, ~49 Zeilen)
```tsx
interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (token: string, user: AuthUser) => void;
  logout: () => void;
  isLoggedIn: boolean;
}
const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export function AuthProvider({ children }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem("authToken"));

  useEffect(() => {
    if (!token) { setUser(null); return; }
    api.auth.me().then(setUser).catch(() => { localStorage.removeItem("authToken"); setToken(null); setUser(null); });
  }, [token]);

  const login = (newToken: string, newUser: AuthUser) => {
    localStorage.setItem("authToken", newToken); setToken(newToken); setUser(newUser);
  };
  const logout = () => { localStorage.removeItem("authToken"); setToken(null); setUser(null); };

  return <AuthContext.Provider value={{ user, token, login, logout, isLoggedIn: !!user }}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
```

### F5 — `src/context/CartContext.tsx` (neu, ~59 Zeilen)
```tsx
interface CartItem { item: MenuItem; anzahl: number; }
interface CartContextType {
  items: CartItem[];
  addItem: (item: MenuItem) => void;
  removeItem: (gerichtId: number) => void;
  updateQuantity: (gerichtId: number, anzahl: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

export function CartProvider({ children }) {
  const [items, setItems] = useState<CartItem[]>([]);

  const addItem = (item: MenuItem) => setItems(prev => {
    const existing = prev.find(i => i.item.id === item.id);
    if (existing) return prev.map(i => i.item.id === item.id ? { ...i, anzahl: i.anzahl + 1 } : i);
    return [...prev, { item, anzahl: 1 }];
  });
  const removeItem = (gerichtId: number) => setItems(prev => prev.filter(i => i.item.id !== gerichtId));
  const updateQuantity = (gerichtId: number, anzahl: number) =>
    setItems(prev => anzahl <= 0
      ? prev.filter(i => i.item.id !== gerichtId)
      : prev.map(i => i.item.id === gerichtId ? { ...i, anzahl } : i));
  const clearCart = () => setItems([]);

  const totalItems = items.reduce((s, i) => s + i.anzahl, 0);
  const totalPrice = items.reduce((s, i) => s + i.anzahl * i.item.preisStudent, 0);   // Studierendenpreis!

  return <CartContext.Provider value={{ items, addItem, removeItem, updateQuantity, clearCart, totalItems, totalPrice }}>{children}</CartContext.Provider>;
}
export const useCart = () => useContext(CartContext);
```
**Wichtig:** `totalPrice` nutzt `preisStudent`. Wenn ein Gast eingeloggt ist, müsste man `preisGast` nutzen — aktuell wird das im `MenuCard` pro Item beim Add-to-Catch abgefangen, aber im Cart total ist es immer Studierendenpreis. (Kleinere Inkonsistenz, kann in einer späteren Iteration gefixt werden.)

### F6 — `src/components/Navigation.tsx` (neu, ~44 Zeilen)
```tsx
export function Navigation() {
  const { totalItems } = useCart();
  const location = useLocation();
  const tabs = [
    { to: "/",         label: "Speiseplan",  icon: Home },
    { to: "/standorte", label: "Standorte",   icon: MapPin },
    { to: "/bestellungen", label: "Bestellen", icon: ShoppingBag },
    { to: "/abstimmung",  label: "Voting",    icon: ThumbsUp },
    { to: "/profil",      label: "Profil",    icon: User },
  ];
  return (
    <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[430px] h-16 bg-white border-t z-50">
      {tabs.map(t => (
        <Link key={t.to} to={t.to}
          className={cn("flex flex-col items-center justify-center flex-1 h-full",
            location.pathname === t.to ? "text-[#003a70] font-medium" : "text-gray-500")}>
          <t.icon size={20} />
          {t.to === "/bestellungen" && totalItems > 0 && (
            <span className="absolute top-1 right-1/4 bg-[#003a70] text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">{totalItems}</span>
          )}
        </Link>
      ))}
    </nav>
  );
}
```

### F7 — `src/components/MenuCard.tsx` (neu, ~61 Zeilen)
- Props: `{ item: MenuItem }`
- Preis-Auswahl: `user?.type === "gast" ? item.preisGast : item.preisStudent`
- `added`-State mit 1.5s Timeout → Button wird grün ("Hinzugefügt") mit Check-Icon
- Tag-Badges: grün mit Leaf-Icon
- Allergen-Badges: jedes Allergen eigene Farbe (`allergenColors`-Map)
- Button default `bg-[#003a70]` "Zum Warenkorb"

### F8 — `src/components/MenuFilters.tsx` (neu, ~97 Zeilen)
- Exportiert `Filters { dietary: string[]; allergens: string[] }`
- Sheet (Bottom-Sheet via `Sheet`-Primitive) mit Trigger-Button (Filter-Icon + gelber Counter bei aktiven Filtern)
- `dietaryOptions = ["VEGAN", "VEGETARISCH", "HALAL"]` als Checkboxes
- `allergenOptions = ["GLUTEN","MILCH","EI","FISCH","SOJA","NUESSE","ERDNUSS","SELLERIE","SENF","SESAM"]` als klickbare Badges (rot "Ohne X" wenn aktiv)
- "Alle Filter zurücksetzen"-Button

### F9 — `src/pages/Home.tsx` (neu, ~169 Zeilen)
- State: `menu, loading, error, filters, view ("day"|"week"), refDate`
- `useEffect` lädt `api.menu.all()`
- `filtered = menu.filter(...)` nach dietary (some tag match) + allergens (no allergen match)
- Split in `hauptspeisen` (typ==="Hauptspeise") + `beilagen` (typ==="Beilage")
- **Toggle Tages-/Wochenansicht** (Calendar/CalendarDays Icons) — nicht nur Icon, sondern Ansicht ändert sich!
- Tagesansicht: Datum-Navigation (‹ ›) springt zum vorherigen/nächten Werktag (Wochenende überspringen), Anzeige "Heute"/"Morgen"/Wochentag+Datum (`date-fns` mit `de`-Locale), bei Wochenende "Mensa geschlossen", Gerichte gruppiert nach Hauptgerichte/Beilagen als `MenuCard`s
- Wochenansicht: "KW N" + Datumrange Mo–Fr, 5 Tageskarten, heutiger Tag mit `border-[#003a70]` + blauem Header hervorgehoben, pro Tag erste 3 Hauptspeisen + Beilagen-Liste
- Header `bg-[#003a70]`, `MenuFilters` im Header

### F10 — `src/pages/Standorte.tsx` (neu, ~112 Zeilen)
- State: `mensen, loading, campus ("Alle"|"TU Dortmund"|"FH Dortmund"), expanded`
- `useEffect` lädt `api.mensen.all()`
- `auslastungConfig` mappt `low`/`medium`/`high` → Label + Farbe + Progress-Bar-Breite (25%/60%/90%)
- Campus-Tabs filtern die Liste
- Pro Mensa: Karte mit Name, Auslastung-Pill (grün/gelb/rot) + Progress-Bar, Öffnungszeiten (Clock-Icon), Adresse (MapPin-Icon)
- Expandierbar: "Route"-Button (Google-Maps-Link `https://maps.google.com/?q=<adresse>`) + "Speiseplan"-Link zu `/`

### F11 — `src/pages/Abstimmung.tsx` (neu, ~119 Zeilen)
- State: `menu, counts, myVotes, loading`
- `useAuth().isLoggedIn`
- `loadData`: `Promise.all([api.menu.all(), api.votes.counts()])` + (wenn eingeloggt) `api.votes.myVotes()`
- `handleVote`: guard auf `isLoggedIn` und `!myVotes.includes(gerichtId)`, dann `api.votes.cast(gerichtId)`, lokaler State-Update + reload
- **Top 3 Podium** (Trophy-Icon, 🥇🥈🥉 Medaillen) — nur anzeigen wenn ≥ 1 Stimme abgegeben
- Liste aller Gerichte: Name, Beschreibung, "Stimmen"-Button (wird blau "Abgestimmt" nach Vote), Progress-Bar pro Gericht (`pct = count/maxCount * 100`), Stimmenzahl
- Wenn nicht eingeloggt: amber Hinweis-Box "Zum Abstimmen bitte anmelden"

### Abnahme-Kriterien Paket F
- [ ] `npm install` läuft fehlerfrei
- [ ] `npm run dev` → `http://localhost:5173/` lädt ohne Console-Fehler
- [ ] Speiseplan zeigt 12 Gerichte (wenn Backends laufen)
- [ ] Toggle Tages/Wochenansicht wechselt korrekt
- [ ] Wochenansicht: heutiger Tag ist blau hervorgehoben
- [ ] Datum-Navigation überspringt Wochenende
- [ ] Filter reduziert die Gerichte nach Tags und schließt Allergene aus
- [ ] Standorte-Seite zeigt 8 Mensen, Campus-Tabs filtern
- [ ] Voting: nach Login kann man abstimmen, Zähler aktualisieren, Top 3 erscheint, Duplikat-Vote wird blockiert
- [ ] Navigation bleibt beim Scrollen sichtbar (`fixed bottom-0`)
- [ ] Optik: Dortmund-Blau `#003a70`, Inter-Font, max 430px

### Abhängigkeiten
- **Blockiert von:** Paket A (Repo), Paket E (`/api/menu`, `/api/mensen`, `/api/votes` muss stehen)
- **Wird benötigt von:** Paket G (App.tsx importiert Home/Standorte/Abstimmung/Navigation)

---

## Paket G — Frontend: Bestellungen, Profil & Integration

### Ziel
Restliche zwei Seiten: Bestellungen (Warenkorb → Checkout → QR-Code → Historie) und Profil (Login/Register + Preferences). Außerdem `App.tsx`-Zusammensetzung und finale Integration.

### G1 — `src/App.tsx` (neu, ~34 Zeilen)
```tsx
export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <div className="flex justify-center bg-gray-200 min-h-screen">
            <div className="bg-white shadow-2xl w-full max-w-[430px] min-h-screen overflow-hidden relative">
              <div className="pb-20">
                <Routes>
                  <Route path="/"            element={<Home />} />
                  <Route path="/standorte"   element={<Standorte />} />
                  <Route path="/bestellungen" element={<Bestellungen />} />
                  <Route path="/abstimmung"  element={<Abstimmung />} />
                  <Route path="/profil"      element={<Profil />} />
                </Routes>
              </div>
              <Navigation />
            </div>
          </div>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}
```
**Wichtig:** `pb-20` auf dem Content-Wrapper, damit die fixed Navigation (h-16 = 64px + Padding) nichts überdeckt. Reihenfolge der Provider: `AuthProvider` außen (Cart braucht User nicht, aber sauberer), `BrowserRouter` innen.

### G2 — `src/main.tsx` (neu, ~10 Zeilen)
```tsx
import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./styles/index.css";

createRoot(document.getElementById("root")!).render(
  <React.StrictMode><App /></React.StrictMode>
);
```

### G3 — `src/pages/Bestellungen.tsx` (neu, ~254 Zeilen — komplexeste Seite)
**States:** `step: "cart"|"payment"|"pickup"|"processing"|"success"`, `tab: "cart"|"history"`, `paymentMethod: "card"|"paypal"`, `pickupTime: string | null`, `orders: Order[]`, `lastOrder: Order | null`.

**Wenn nicht eingeloggt:** ShoppingBag-Icon + "Zum Login"-Button (Link zu `/profil`).

**Warenkorb-Tab (step=cart):**
- Cart-Items aus `useCart().items`: Name, Preis, Mengen-Stepper (− / +), Lösch-Button, Zeilenpreis
- Summary-Card: Zwischensumme, Abholung Gratis, Gesamt (blau)
- "Weiter zur Zahlung"-Button → `setStep("payment")`
- Empty-State mit ShoppingBag-Icon + "Zum Speiseplan"-Link

**Payment-Step:**
- 2 Zahlungsmethoden-Karten: Kreditkarte (•••• 4242) + PayPal
- Radio-Style Auswahl (ausgewählt = `border-[#003a70]`)
- Gesamt-Anzeige
- "Weiter zur Abholzeit"-Button + Zurück-Button

**Pickup-Step:**
- 2-Spalten-Grid mit 10-Minuten-Slots: ab `now + 15min`, auf 5min gerundet, bis 14:15
- `generatePickupSlots()` erzeugt ~8 Slots je nach Tageszeit
- Ausgewählter Slot hervorgehoben (`bg-[#003a70] text-white`)
- "Jetzt X € bezahlen"-Button (disabled wenn kein Slot gewählt)

**Processing-Step:**
- Fullscreen blauer Hintergrund (`bg-[#003a70]`), Spinner, "Zahlung wird verarbeitet..."
- Ruft `api.orders.create({ items: cartItems.map(...), pickupTime })` auf
- Bei Erfolg: `clearCart()`, `setLastOrder(order)`, `loadOrders()`, → `setStep("success")`
- Bei Fehler: zurück zu `"cart"` + `alert("Fehler bei der Bestellung")`

**Success-Step:**
- Grüner CheckCircle, "Bestellung bestätigt"
- **Fake-QR-Code** (`FakeQR`-Komponente): 7×7 Grid, deterministisch aus dem Code-String generiert (LCG `hash = hash * 1103515245 + 12345`), **keine** echte QR-Encoding-Bibliothek
- Großer Pickup-Code (z. B. "AB123")
- Abholzeit
- Buttons: "Meine Bestellungen" (→ `tab="history"`) + "Neue Bestellung" (→ `step="cart"`)

**Historie-Tab:**
- `loadOrders` ruft `api.orders.all()` auf (wenn eingeloggt)
- Liste vergangener Orders
- Pro Order: Code, Abholzeit, Status-Badge, Items, Gesamt
- Status-Badge-Map: `OFFEN`→"In Bearbeitung" (blau), `BEZAHLT`→"Bereit" (grün), `ABGEHOLT`→"Abgeholt" (grau)
- "Als abgeholt markieren"-Link → `api.orders.updateStatus(id, "ABGEHOLT")` + `loadOrders()`

### G4 — `src/pages/Profil.tsx` (neu, ~173 Zeilen)
**State:** `view: "landing"|"login"|"register"|"verify"`, `email, password, showPw, vorname, nachname, error, loading, prefs`.

**Wenn ausgeloggt — 4 Views:**
- **Landing**: User-Icon, "Nicht angemeldet", Anmelden/Registrieren Buttons (setzen `view`)
- **Login**: Email + Password Inputs (mit Icons), Show/Hide Password, Fehler-Box, "Anmelden"-Button mit Loading-State. Ruft `api.auth.login(email, password)` → `login(token, user)` aus Context. Bei Fehler: `setError("Falsche E-Mail oder Passwort")`.
- **Register**: Vorname/Nachname (2-Spalten), Email, Password (mit Strength-Meter), "Registrieren"-Button. Ruft `api.auth.register(email, password, vorname, nachname)` auf.
- **Verify**: Bestätigungsseite (optional, kann leer bleiben)

**Wenn eingeloggt — ProfileView:**
- Header: Initialien-Avatar (z. B. "JD" aus vorname+nachname), Name, Email, Preisgruppen-Badge (`student`→"Studierendenpreis", `mitarbeiter`→"Bediensteten", `gast`→"Gast")
- **Ernährungspräferenzen-Card** (Leaf-Icon):
  - Toggle-Badges für VEGAN/VEGETARISCH/HALAL (klickbar, aktiv = `bg-[#003a70]`)
  - Allergen-Badges (rot wenn ausgeschlossen, sonst outline) — dieselben 10 wie in `MenuFilters`
  - `togglePref(kind, value)` updated lokale `prefs` + ruft sofort `api.profil.updatePreferences(prefs)` auf
- Abmelden-Button (rot outline) → `logout()` aus Context

**Wichtig:** `api.auth.register(email, password, vorname, nachname)` mit **vier einzelnen Args**, wie in `api.ts` definiert — nicht als Objekt übergeben. Sonst landet nur der Email-String im Body und das Backend wirft einen JSON-parse-Fehler.

### G5 — `frontend/.gitignore` (neu, 3 Zeilen)
```
node_modules
dist
*.local
```

### G6 — Finale Frontend-Integration testen
- `npm install` läuft fehlerfrei (keine Lockfile-Konflikte)
- `npm run build` produziert `dist/` ohne TypeScript-Fehler
- `npm run dev` → alle 5 Seiten erreichbar, Navigation funktioniert, keine Console-Fehler
- Mit laufenden Backends: kompletter Flow durchpielen
  1. Registrierung (FH-Email → `type="student"`)
  2. Login
  3. Speiseplan → Filter setzen → Gericht in Warenkorb
  4. Bestellungen → Warenkorb → Zahlung → Abholzeit → Checkout → QR-Code + Code erscheint
  5. "Meine Bestellungen" zeigt die neue Order mit Items
  6. "Als abgeholt markieren" → Status-Badge ändert sich
  7. Voting → Stimme abgeben → Zähler erhöht sich → Top 3 aktualisiert
  8. Profil → Preferences setzen → Seite neu laden → Preferences bleiben
  9. Abmelden → wieder Anmelden

### Abnahme-Kriterien Paket G
- [ ] Login-Form sendet `{"email","password","vorname","nachname"}` an `/api/auth/register` (vollständiges JSON, nicht nur Email!)
- [ ] Registrierung → Login → Warenkorb befüllen → Checkout → QR-Code + Code erscheint
- [ ] "Meine Bestellungen" zeigt die neue Order mit Items (kein Lazy-Fehler sichtbar)
- [ ] "Als abgeholt markieren" ändert den Status-Badge auf "Abgeholt"
- [ ] Profil-Seite: Preferences werden gespeichert und beim Neuladen wiederhergestellt
- [ ] `npm run build` grün (keine TS-Fehler)
- [ ] Optik entspricht Frontend-Vorlage: Dortmund-Blau, Mobile-First max 430px, fixed Bottom-Navigation

### Abhängigkeiten
- **Blockiert von:** Paket D (`/api/auth` muss stehen), Paket E (`/api/orders`, `/api/profil/preferences`), Paket F (Navigation, Contexts, api.ts, UI-Primitives, Home/Standorte/Abstimmung für App.tsx)
- **Wird benötigt von:** nichts (Paket G schließt das Frontend ab)

---

## 10. Abhängigkeiten & empfohlene Reihenfolge

```
Paket A — Infrastruktur
   |
   v
Paket B              Paket D
   |                    |
   v                    v
Paket C              Paket E
   |                    |
   v                    v
        Paket F + Paket G
              |
              v
     Paket A — finale Integration
```

**Empfohlene Reihenfolge (4 Wochen):**

1. **Woche 1:** Paket A (Infra) + Paket B (Entities) + Paket D (Auth) parallel
   - A macht docker-compose, CI, README-Skeleton
   - B macht JPA-Entities + Seed (unabhängig vom App-Backend)
   - D macht UserEntity + JWT + SecurityConfig (unabhängig vom Simulator)
2. **Woche 2:** Paket C (MQTT-Simulator) + Paket E (MQTT-App) parallel
   - C braucht B (Entities) + A (Mosquitto)
   - E braucht D (Security) + A (Mosquitto)
3. **Woche 3:** Paket F + Paket G (Frontend) parallel
   - F braucht E (`/api/menu`, `/api/mensen`, `/api/votes`)
   - G braucht D (`/api/auth`), E (`/api/orders`, `/api/profil/preferences`), F (UI-Primitives, Contexts, Navigation)
   - F und G können am selben Branch arbeiten oder G branched ab, sobald F's Contexts + api.ts stehen
4. **Woche 4:** Integration, Tests, Doku (Paket A)
   - End-to-End-Tests (siehe A8)
   - README finalisieren
   - Demo-Skript

**Git-Branch-Regeln:**
- Jeder arbeitet auf `feature/<thema>` (z. B. `feature/gericht-entities`)
- Commits: `feature/<thema>: <kurzbeschreibung>` (Prefix `feature/` ist Konvention in der Commit-Message)
- PR → Review → Merge nach `main`
- `main` muss immer lauffähig sein (`./mvnw verify` grün in CI)

---

## Quick-Reference: Datei-Zustände je Paket

### Paket A
| Datei | Aktion |
|---|---|
| `docker-compose.yml` | NEU |
| `mosquitto/mosquitto.conf` | NEU |
| `README.md` | NEU (kompletter Rewrite) |
| `.github/workflows/pre-merge.yaml` | MODIFIZIERT (Services + Mosquitto-Step) |
| `start-services.ps1` | MODIFIZIERT (korrekte Modulnamen) |
| `pom.xml` (Root) | UNVERÄNDERT |
| `Dockerfile.java` | UNVERÄNDERT (Template-Reste tolerieren) |

### Paket B
| Datei | Aktion |
|---|---|
| `gericht/Gericht.java` | MODIFIZIERT (JPA-Annotations + No-Arg + id) |
| `gericht/Beilage.java` | MODIFIZIERT (`@Entity` + `@DiscriminatorValue` + No-Arg) |
| `gericht/Hauptspeise.java` | MODIFIZIERT (`@Entity` + `@DiscriminatorValue` + No-Arg) |
| `gericht/factory/HauptgerichtFactory.java` | MODIFIZIERT (`@Component`) |
| `gericht/factory/BeilageFactory.java` | MODIFIZIERT (`@Component`) |
| `gericht/GerichtRepository.java` | NEU |
| `gericht/BeilageRepository.java` | NEU |
| `gericht/HauptspeiseRepository.java` | NEU |
| `mensa/Mensa.java` | NEU |
| `mensa/MensaRepository.java` | NEU |
| `controller/GerichtController.java` | NEU |
| `controller/MensaController.java` | NEU |
| `config/SeedData.java` | NEU |
| `application.properties` | MODIFIZIERT (DB + MQTT) |
| `pom.xml` | MODIFIZIERT (+jpa +postgres +paho) |
| `gericht/Allergen.java`, `gericht/GerichtTag.java`, `gericht/factory/GerichtFactory.java`, `Iterator/*`, `core/*` | UNVERÄNDERT |

### Paket C
| Datei | Aktion |
|---|---|
| `config/MqttConfig.java` | NEU |
| `config/MqttPublisher.java` | NEU |
| `orderreceiver/ReceivedOrder.java` | MODIFIZIERT (Record → Entity) |
| `orderreceiver/ReceivedOrderRequest.java` | MODIFIZIERT (Record-Felder geändert) |
| `orderreceiver/ReceivedOrderService.java` | MODIFIZIERT (In-Memory → DB) |
| `orderreceiver/ReceivedOrderItem.java` | NEU |
| `orderreceiver/ReceivedOrderItemRequest.java` | NEU |
| `orderreceiver/ReceivedOrderRepository.java` | NEU |
| `orderreceiver/OrderMqttHandler.java` | NEU |
| `voting/VoteTotal.java` | NEU |
| `voting/VoteTotalRepository.java` | NEU |
| `voting/VoteMqttHandler.java` | NEU |
| `orderreceiver/ReceivedOrderController.java` | UNVERÄNDERT |

### Paket D
| Datei | Aktion |
|---|---|
| `user/UserEntity.java` | NEU |
| `user/UserRepository.java` | NEU |
| `user/AuthService.java` | NEU |
| `user/AuthController.java` | NEU |
| `security/JwtUtil.java` | NEU |
| `security/JwtAuthFilter.java` | NEU |
| `config/SecurityConfig.java` | NEU |
| `bezahlsystem/Gericht.java` | MODIFIZIERT (equals/hashCode) |
| `bezahlsystem/Warenkorb_Item.java` | MODIFIZIERT (equals/hashCode) |
| `profil/ProfilTest.java` | VERSCHOBEN (src/main → src/test) |
| `application.properties` | MODIFIZIERT (DB + MQTT + JWT) |
| `pom.xml` | MODIFIZIERT (+jpa +postgres +security +jjwt +paho) |
| `profil/Profil.java`, `profil/Abstimmung.java`, `profil/Hauptspeise.java`, `bezahlsystem/Bestellung.java`, `bezahlsystem/Warenkorb.java`, `adapter/*` | UNVERÄNDERT |

### Paket E
| Datei | Aktion |
|---|---|
| `config/MqttConfig.java` | NEU (gespiegelt zu Paket C) |
| `menu/MenuCache.java` | NEU |
| `menu/MenuMqttHandler.java` | NEU |
| `menu/MenuService.java` | MODIFIZIERT (auf Cache umgeklemmt, Return-Typ `Map`) |
| `menu/MenuController.java` | MODIFIZIERT (Return-Typ `Map`, `/debug` ergänzt) |
| `mensa/MensaCache.java` | NEU |
| `mensa/MensaController.java` | NEU |
| `order/Order.java` | MODIFIZIERT (Record → Entity) |
| `order/OrderRequest.java` | MODIFIZIERT (Record-Felder geändert) |
| `order/OrderService.java` | MODIFIZIERT (In-Memory → DB + MQTT-Publish + `@Transactional`) |
| `order/OrderController.java` | MODIFIZIERT (Authentication + PATCH) |
| `order/OrderItem.java` | NEU |
| `order/OrderItemRequest.java` | NEU |
| `order/OrderRepository.java` | NEU |
| `voting/Vote.java`, `VoteRepository.java`, `VoteService.java`, `VoteController.java` | NEU |
| `preference/Preference.java`, `PreferenceRepository.java`, `PreferenceController.java` | NEU |
| `menu/MenuItem.java`, `adapter/MensaAPI.java`, `adapter/MensaAdapter.java`, `bezahlsystem/*` | UNVERÄNDERT |

### Paket F
| Datei | Aktion |
|---|---|
| `frontend/package.json`, `frontend/vite.config.ts`, `frontend/index.html` | NEU |
| `frontend/src/styles/index.css`, `frontend/src/lib/utils.ts`, `frontend/src/lib/api.ts` | NEU |
| `frontend/src/context/AuthContext.tsx`, `frontend/src/context/CartContext.tsx` | NEU |
| `frontend/src/components/ui/{button,badge,input,label,card,checkbox,sheet}.tsx` | NEU (7 Primitive) |
| `frontend/src/components/Navigation.tsx`, `MenuCard.tsx`, `MenuFilters.tsx` | NEU |
| `frontend/src/pages/Home.tsx`, `Standorte.tsx`, `Abstimmung.tsx` | NEU |

### Paket G
| Datei | Aktion |
|---|---|
| `frontend/src/App.tsx` | NEU (Provider + Routes + Shell + Navigation) |
| `frontend/src/main.tsx` | NEU |
| `frontend/src/pages/Bestellungen.tsx` | NEU (5-Step-Checkout + Historie + Fake-QR) |
| `frontend/src/pages/Profil.tsx` | NEU (Login/Register/ProfileView + Preferences) |
| `frontend/.gitignore` | NEU |

---

**Ende des Dokuments.** Dieses `IMPLEMENTIERUNG.md` ist der retrospektive, auf dem tatsächlichen Diff basierende Umsetzungs-Bericht. Er ermöglicht es, den Endstand vom Original-Template ausgehend Schritt für Schritt nachzubauen.
