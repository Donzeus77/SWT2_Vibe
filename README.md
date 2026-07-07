# SWT2_Vibe — Mensa-App Projekt

Ein vollständiges Mensa-Bestellsystem für das Studierendenwerk Dortmund, bestehend aus zwei Spring-Boot-Backends mit MQTT-Kommunikation, jeweils eigener PostgreSQL-Datenbank, und einem schlanken React-Frontend.

## Architektur

```
[Frontend] --REST/JWT--> [mensa_app_backend:8082] --MQTT--> [studentenwerk_simulator:8081]
   Speiseplan, Bestellungen,        eigene DB: mensa_db          eigene DB: simulator_db
   Voting, Profil                   (users, orders, votes)       (gerichte, mensen,
                                       ↑ MQTT (speiseplan/mensen)        received_orders, vote_totals)
                                       └────────────────────────┘
                                     Mosquitto MQTT Broker (1883)
```

## Module

| Modul | Port | DB | Verantwortung |
|---|---|---|---|
| `studentenwerk_simulator` | 8081 | `simulator_db` | Gerichte, Mensen, eingegangene Bestellungen, Voting-Totals (Behörden-Seite) |
| `mensa_app_backend` | 8082 | `mensa_db` | User/Auth, Bestellungen, Votes, Preferences (Nutzer-Seite) |
| `frontend` | 5173 | — | React-SPA (Speiseplan, Standorte, Bestellungen, Abstimmung, Profil) |

## MQTT-Topics

| Topic | Richtung | Retain | Inhalt |
|---|---|---|---|
| `mensa/speiseplan` | simulator → app | ja | Aktuelle Gerichte als JSON-Array |
| `mensa/mensen` | simulator → app | ja | Liste der Mensen als JSON-Array |
| `mensa/orders` | app → simulator | nein | Neue Bestellung als JSON-Objekt |
| `mensa/votes` | app → simulator | nein | Vote-Stände als JSON-Map |

## Voraussetzungen

- Java 21
- Docker (für PostgreSQL + Mosquitto via docker-compose)
- Node 20+ (für das Frontend)
- Maven Wrapper ist enthalten (`mvnw` / `mvnw.cmd`)

## Quickstart

### 1. Infrastruktur starten

```bash
docker compose up -d
```

Startet zwei PostgreSQL-Instanzen und einen Mosquitto-MQTT-Broker.

### 2. Backends starten

```bash
./mvnw -pl studentenwerk_simulator spring-boot:run
./mvnw -pl mensa_app_backend spring-boot:run
```

(Unter Windows: `start-services.ps1` startet alle drei Dienste nacheinander.)

### 3. Frontend starten

```bash
cd frontend
npm install
npm run dev
```

### 4. Endpunkte prüfen

```text
http://localhost:8081/api/gerichte      # Speiseplan aus Simulator-DB
http://localhost:8082/api/menu           # Speiseplan (via MQTT vom Simulator)
http://localhost:8082/actuator/health    # Health-Check
http://localhost:5173/                   # Frontend
```

## Team

Team Leader: Yaren Sari

Members: Joel Kawinski, Leo Bernoth, Valeriia Khatchenko, Johann Wenner, Viktoriia Dovzhenko, Noel Koblitz, Yaren Sari

## Build

```bash
./mvnw verify              # Alle Module bauen und testen
./mvnw -pl frontend verify # Nur Frontend (falls als Modul angebunden)
```
