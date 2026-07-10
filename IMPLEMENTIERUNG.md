# SWT2_Vibe – Restarbeitsplan ab dem SWT2-Stand

Dieses Dokument ist der verbindliche Arbeitsplan für die Weiterentwicklung von `SWT2/` nach `SWT2_Vibe/`.

`SWT2/` ist der aktuelle Ausgangsstand. Die dort bereits vorhandenen Klassen sollen weiterverwendet und produktiv ausgebaut werden. Neue Klassen werden nur angelegt, wenn für Persistenz, REST oder MQTT ein klarer technischer Baustein fehlt. Die beiden Dummy-Klassen dürfen ersetzt und entfernt werden.

Die verbleibende Arbeit ist auf **sieben gleichwertige Lieferpakete** verteilt. Jedes Paket enthält einen klaren fachlichen Bereich, Code, Testfälle und eine Abnahme. Die Pakete sind so geschnitten, dass sie von sieben Personen parallel bearbeitet werden können.

> Status: ✅ bereits im SWT2-Ausgangsstand vorhanden · 🟡 teilweise vorhanden, fertigstellen · ⬜ neu umsetzen

---

## Aufgabenübersicht zur Zuteilung

Jedes Paket ist für eine Person bzw. ein Zweierteam gedacht. Die Pakete sind vergleichbar groß: jedes enthält Implementierung, Test und Abnahme. Paket A und B starten mit bereits vorhandener SWT2-Vorarbeit; ihre restlichen Aufgaben gleichen diesen Vorsprung aus.

| Paket | Zuteilung / Schwerpunkt | Vorarbeit im SWT2-Stand | Ergebnis des Pakets | Abhängigkeit |
|---|---|---|---|---|
| A | Infrastruktur und Projektbasis | Root-POM, Mosquitto-Konfiguration und CI vorhanden | Docker-Compose, funktionierendes Startskript, aktuelle README und Frontend-Check in CI | keine |
| B | Simulator: Speiseplan und Mensen | Gerichtsdomain, Controller, Repositories und Seed vorhanden | geprüfte JPA-Domain, saubere Seed-Daten, verlässliche REST-Endpunkte und Tests | PostgreSQL aus A für Abnahme |
| C | Simulator: MQTT und Eingangsdaten | Topic-Einstellungen vorhanden | Publisher für Speiseplan/Mensen sowie persistierter Empfang von Bestellungen und Stimmen | A, B |
| D | Profile und Präferenzen | SWT2-Prototyp `Profil` vorhanden | persistentes Profil, Login/Registrierung ohne JWT und gespeicherte Präferenzen | A |
| E | App-Backend: Speiseplan- und Mensen-Caches | erste Menü- und Adapterklassen vorhanden | MQTT-Subscriber, Caches und REST-Leseendpunkte für Menü und Mensen | A, C |
| F | App-Backend: Bestellung und Abstimmung | SWT2-Prototypen Warenkorb, Bestellung und Abstimmung vorhanden | persistente Bestellung/Votes, Dummy-Entfernung, REST und MQTT-Publish | C, D |
| G | Frontend und Gesamtabnahme | kein Frontend vorhanden | React/Vite-Frontend mit Profil, Warenkorb, Checkout, Standorten und Voting | D, E, F |

### Empfohlene Reihenfolge

```text
A ──┬── B ── C ── E ──┐
    ├── D ─────── F ──┼── G
    └─────────────────┘
```

Parallel möglich sind zunächst A, B und D. C beginnt nach B, E nach C, F nach D und C. G kann als Frontend-Grundlage starten, wird aber erst nach D, E und F vollständig integriert.

---

## 1. Gemeinsame Zielarchitektur

```text
React-Frontend :5173
        │ REST; Profil-E-Mail als Nutzerbezug
        ▼
mensa_app_backend :8082 ── MQTT ── studentenwerk_simulator :8081
        │                                  │
     mensa_db                          simulator_db
```

| Komponente | Verantwortlichkeit |
|---|---|
| `studentenwerk_simulator` | Gerichte und Mensen verwalten, Speiseplan veröffentlichen, Bestellungen und Stimmen empfangen. |
| `mensa_app_backend` | Profile, Warenkorb, Bestellungen, Präferenzen, Abstimmung und Speiseplan-/Mensa-Caches. |
| `frontend` | Speiseplan, Standortansicht, Profil, Warenkorb, Checkout und Abstimmung. |
| PostgreSQL | Je Datenbank eine persistente Sicht: `simulator_db` bzw. `mensa_db`. |
| Mosquitto | Austausch von Speiseplan, Mensen, Bestellungen und Stimmen. |

### Gemeinsame Regeln

- Java 21 und Spring Boot 4.0.6 verwenden.
- Die beiden Backends haben getrennte Datenbanken. Keine Simulator-JPA-Entity direkt im App-Backend importieren.
- Speiseplan und Mensen werden als MQTT-JSON übertragen; Bestellungen und Votes referenzieren Gerichte über `gerichtId`.
- `Profil` bleibt das Nutzer-Modell. Es gibt **kein JWT** und keine Security-Konfiguration.
- Nutzerbezogene REST-Aufrufe enthalten die E-Mail. Das Backend löst sie zu einem gespeicherten `Profil` auf.
- `target/`, `node_modules/` und `dist/` werden nie committed.

### Gemeinsame MQTT-Topics

| Topic | Richtung | Retained | Payload |
|---|---|---:|---|
| `mensa/speiseplan` | Simulator → App | ja | Liste der Gerichte inklusive ID, Preise, Tags, Allergene und Typ |
| `mensa/mensen` | Simulator → App | ja | Liste der Mensen |
| `mensa/orders` | App → Simulator | nein | Bestellung inklusive Items und Abholzeit |
| `mensa/votes` | App → Simulator | nein | Stimmenstände je Gericht-ID |

---

## 2. Bereits vorhandene SWT2-Arbeit

Diese Arbeit wird nicht neu erfunden. Sie wird in den jeweiligen Paketen überprüft, ergänzt und getestet.

| Bereich | Vorhandener SWT2-Stand |
|---|---|
| Root-POM | ✅ Maven-Aggregator mit den Modulen `studentenwerk_simulator` und `mensa_app_backend` |
| CI und Mosquitto | ✅ Workflow und `mosquitto/mosquitto.conf` existieren |
| Gerichtsdomain | ✅ `Gericht`, `Hauptspeise`, `Beilage`, Tags, Allergene, Factories und Repositories existieren |
| Simulator-REST | ✅ Controller für Gerichte und Mensen existieren |
| Seed-Daten | ✅ `SeedData` für Gerichte und Mensen existiert |
| SWT2-Fachdomain im App-Backend | ✅ `Profil`, `Warenkorb`, `Warenkorb_Item`, `Bestellung`, `Abstimmung` existieren als Prototypen |
| Basis-Menü/Bestellung | ✅ erste `menu`- und `order`-Klassen existieren, sind aber noch kein Zielmodell |

Nicht übernommen werden die Root-Prototypen (`Authentizierung.java`, `Gericht.java`, `MenuGetter.java`, `Profil.java`, `Voting.java`). Die fachlich relevanten Varianten in den Modulen bleiben die Grundlage.

---

# Paket A – Infrastruktur, Startbarkeit und Projektbereinigung

**Ziel:** Jede Person kann das Gesamtsystem mit denselben Ports, Datenbanken und Befehlen starten.

**Größe:** Infrastruktur, CI, Dokumentation und Bereinigung; keine fachlichen Endpunkte.

## A1 – Docker-Compose ergänzen ⬜

Im Root eine `docker-compose.yml` für folgende Services anlegen:

| Service | Port | Konfiguration |
|---|---:|---|
| PostgreSQL Simulator | `5433` | DB `simulator_db`, Nutzer `mensa`, Passwort `mensa123` |
| PostgreSQL App | `5434` | DB `mensa_db`, Nutzer `mensa`, Passwort `mensa123` |
| Mosquitto | `1883` | bindet `mosquitto/mosquitto.conf` ein |

Die Datenbanken brauchen getrennte Docker-Volumes, damit Neustarts ihre Daten behalten.

## A2 – Startskript korrigieren 🟡

`start-services.ps1` verweist noch auf nicht vorhandene Module `app` und `order_service`. Es muss:

1. zuerst `docker compose up -d` ausführen,
2. danach `studentenwerk_simulator` auf Port 8081 starten,
3. danach `mensa_app_backend` auf Port 8082 starten,
4. den separaten Frontend-Start (`cd frontend; npm run dev`) in der Ausgabe erklären.

## A3 – Root und Dokumentation aufräumen 🟡

- Root-Prototypen nicht in den Zielstand kopieren.
- README mit aktuellen Ports, Docker-Start, Backend-Start, Frontend-Start und MQTT-Topics schreiben.
- Keine Hinweise auf JWT, Bearer-Header, `UserEntity` oder die später entfernten parallelen `order`-/`voting`-Modelle aufnehmen.
- `IMPLEMENTIERUNG.md` nach Änderungen an Schnittstellen mitpflegen.

## A4 – CI vervollständigen 🟡

Der bestehende Workflow testet die Backends. Ergänzen:

```text
cd frontend
npm ci
npm run build
```

Der Workflow muss auch mit gestarteten PostgreSQL- und Mosquitto-Containern funktionieren.

## Abnahme Paket A

```powershell
docker compose up -d
.\mvnw.cmd -pl studentenwerk_simulator spring-boot:run
.\mvnw.cmd -pl mensa_app_backend spring-boot:run
cd frontend
npm run dev
```

Alle vier Ports `5173`, `8081`, `8082`, `1883` und beide Datenbankports sind danach erreichbar.

---

# Paket B – Simulator: Gerichtsdomain, Seed und REST abschließen

**Ziel:** Der Simulator besitzt einen stabilen, persistierten Speiseplan mit echten Gericht-IDs.

**Größe:** Vorhandene JPA-Domain fertigstellen, fachlich prüfen und testen.

## B1 – Gerichtshierarchie prüfen und vervollständigen 🟡

Die vorhandenen Klassen `Gericht`, `Hauptspeise` und `Beilage` zu einer JPA-Hierarchie mit `SINGLE_TABLE`-Vererbung vervollständigen.

Pflichtfelder:

| Feld | Bedeutung |
|---|---|
| `id` | eindeutige Datenbank-ID und spätere `gerichtId` |
| `name`, `beschreibung` | Anzeige im Frontend |
| `preisStudent`, `preisGast` | Preisberechnung |
| `allergene`, `tags` | Filterung und Präferenzen |
| Diskriminator/Typ | Hauptspeise oder Beilage |

`Allergen` und `GerichtTag` bleiben Enums. Factories erzeugen die passende Unterklasse und erhalten keine eigene ID-Logik.

## B2 – Repositories, Mensen und REST prüfen 🟡

- `GerichtRepository`, `HauptspeiseRepository`, `BeilageRepository` prüfen bzw. ergänzen.
- `Mensa` als JPA-Entity mit ID, Name, Campus, Adresse, Öffnungszeiten und Auslastung absichern.
- `MensaRepository` ergänzen.
- `GET /api/gerichte` und `GET /api/mensen` müssen Listen aus der Datenbank liefern.

## B3 – Seed-Daten fachlich bereinigen 🟡

Die vorhandene `SeedData` darf nur beim leeren Datenbestand ausführen. Sie muss mindestens enthalten:

- mehrere Hauptspeisen und Beilagen,
- vegane, vegetarische und halal markierte Gerichte,
- Gerichte mit verschiedenen Allergenen,
- mehrere Dortmunder Mensen.

## B4 – Simulator-Tests ⬜

Mindestens testen:

1. Anwendung startet mit PostgreSQL.
2. Seed erzeugt keine Duplikate beim zweiten Start.
3. Gerichte enthalten ID, Typ, Preise, Tags und Allergene.
4. REST-Endpunkte liefern HTTP 200 und nichtleere Listen.

## Abnahme Paket B

`GET http://localhost:8081/api/gerichte` und `GET http://localhost:8081/api/mensen` liefern nach einem frischen Datenbankstart plausible Daten. Diese Antworten sind die verbindliche Grundlage für alle späteren Pakete.

---

# Paket C – Simulator: MQTT-Publisher, Bestelleingang und Vote-Totals

**Ziel:** Der Simulator verteilt seine Daten aktiv und speichert Nachrichten des App-Backends.

**Größe:** Ein vollständiger MQTT-Fluss in beide Richtungen.

## C1 – MQTT-Grundkonfiguration ⬜

`config/MqttConfig` im Simulator anlegen. Benötigt werden getrennte Channels für:

- retained Publisher: Speiseplan und Mensen,
- Subscriber: Bestellungen und Stimmen.

Die Werte stammen aus `application.properties`; keine Topic-Namen im Java-Code duplizieren.

## C2 – Speiseplan und Mensen veröffentlichen ⬜

`MqttPublisher` erstellt JSON aus den Repository-Daten und sendet:

- alle Gerichte nach `mensa/speiseplan`,
- alle Mensen nach `mensa/mensen`.

Beide Nachrichten sind retained. Veröffentlichen beim Start und nach jeder fachlichen Datenänderung.

## C3 – Bestellungen empfangen ⬜

Die vorhandenen `orderreceiver`-Prototypen werden zu persistierten Eingängen ausgebaut:

- `ReceivedOrder` als Entity,
- `ReceivedOrderItem` als Entity,
- Request-Records passend zur MQTT-Payload,
- Repository und Service,
- `OrderMqttHandler` für `mensa/orders`.

Eine eingehende Bestellung speichert Profilname, Summe, Abholzeit, Bestellcode und Item-Snapshots.

## C4 – Stimmen empfangen ⬜

`VoteTotal`, Repository und `VoteMqttHandler` anlegen. Die Nachricht von `mensa/votes` wird als Zuordnung `gerichtId → Stimmenzahl` gespeichert bzw. aktualisiert.

## Abnahme Paket C

Nach Start beider Backends enthält das App-Backend einen Speiseplan- und Mensen-Cache. Eine im App-Backend erzeugte Bestellung erscheint im Simulator; eine abgegebene Stimme aktualisiert den Vote-Total des Simulators.

---

# Paket D – App-Backend: Persistentes Profil und Präferenzen

**Ziel:** Der SWT2-Prototyp `Profil` wird das einzige Nutzer-Modell und ersetzt JWT vollständig.

**Größe:** Profilpersistenz, einfache Anmeldung und Präferenzen.

## D1 – Profil zur Entity ausbauen ⬜

`profil/Profil.java` mit JPA versehen und in Tabelle `users` speichern.

- Datenbank-ID statt des statischen Nutzerszählers verwenden.
- E-Mail eindeutig speichern.
- Vor- und Nachname aus `vorname.nachname…@…` ableiten.
- `ermittleStatus(email)` aus SWT2 beibehalten.
- Beziehungen zum Warenkorb und zu Bestellungen vorbereiten.
- `ProfilRepository.findByEmail(...)` anlegen.

## D2 – Einfache Registrierung und Login ⬜

`profil/AuthController` mit zwei Endpunkten umsetzen:

| Endpunkt | Request | Antwort |
|---|---|---|
| `POST /api/auth/register` | `{ "email", "password" }` | Profil ohne Passwort |
| `POST /api/auth/login` | `{ "email", "password" }` | Profil ohne Passwort |

Es gibt kein Token, kein `/me` und keinen `Authorization`-Header. Für dieses Projekt ist das Passwort absichtlich kein Sicherheitsschwerpunkt.

## D3 – Präferenzen persistieren ⬜

`Preference`, Repository und Controller anlegen. Speichern:

- gewünschte Ernährungsformen,
- auszuschließende Allergene,
- Bezug auf das Profil über dessen Datenbank-ID.

API:

```text
GET /api/profil/preferences?email=<email>
PUT /api/profil/preferences?email=<email>
```

## D4 – Alte Authentifizierung entfernen ⬜

Nicht verwenden oder im Zielstand behalten:

- `UserEntity`, `UserRepository`, alter Auth-Service,
- `JwtUtil`, `JwtAuthFilter`, `SecurityConfig`,
- Spring-Security- und JJWT-Dependencies,
- JWT-Eigenschaften aus `application.properties`.

## Abnahme Paket D

Ein neues Profil kann registriert, nach Backend-Neustart erneut eingeloggt und ohne Passwort in der JSON-Antwort dargestellt werden. Präferenzen bleiben nach Neustart erhalten.

---

# Paket E – App-Backend: Speiseplan- und Mensen-Caches

**Ziel:** Das App-Backend nutzt die Simulator-Daten über MQTT, ohne Simulator-Entities direkt zu kopieren.

**Größe:** Subscriber, Cache, REST-Lesezugriff und Cache-Tests.

## E1 – MQTT-Subscriber konfigurieren ⬜

Eine eigene `config/MqttConfig` für das App-Backend anlegen. Sie verbindet sich zum gleichen Broker und subscribt auf:

- `mensa/speiseplan`,
- `mensa/mensen`.

Zusätzlich werden Outbound-Channels für Bestellungen und Stimmen bereitgestellt, die Paket F verwendet.

## E2 – Speiseplan cachebar machen ⬜

- `MenuCache` anlegen; der Cache hält die zuletzt empfangene Gerichtsliste thread-sicher.
- `MenuMqttHandler` parst den JSON-Speiseplan und aktualisiert den Cache.
- `MenuService` nur noch auf den Cache ausrichten.
- `MenuController` auf folgende API festlegen:

```text
GET /api/menu
GET /api/menu/{id}
```

Ein Gericht wird mindestens über seine `id`, seinen Namen und beide Preise erkannt.

## E3 – Mensen cachebar machen ⬜

`MensaCache` und `MensaController` ergänzen. `GET /api/mensen` liefert die letzte MQTT-Nachricht und nicht die alten Adapter-Dummy-Daten.

## E4 – Adapter nur bei tatsächlicher Nutzung behalten 🟡

`adapter/MensaAPI` und `MensaAdapter` prüfen. Falls sie durch MQTT-Caches vollständig ersetzt sind, entfernen; andernfalls klar dokumentieren, welche Funktion sie noch erfüllen.

## Abnahme Paket E

Der App-Backend-Neustart nach einem bereits gestarteten Simulator liefert ohne manuelle Datenanlage:

```text
GET /api/menu     → Speiseplan mit echten IDs
GET /api/mensen   → Mensenliste
```

---

# Paket F – App-Backend: Warenkorb, Bestellung und Abstimmung

**Ziel:** Die SWT2-Fachklassen werden zu persistenter Fachlogik mit REST- und MQTT-Ausgang.

**Größe:** Bestellung und Voting einschließlich Datenmodell, Endpunkte und Publish.

## F1 – Dummy-Gericht durch Item-Snapshot ersetzen ⬜

`bezahlsystem.Gericht` ist ein Dummy und wird entfernt. `Warenkorb_Item` speichert stattdessen:

| Feld | Zweck |
|---|---|
| `gerichtId` | Referenz auf das Simulator-Gericht |
| `name` | Bestell-Snapshot |
| `preis` | Bestell-Snapshot |
| `anzahl` | Warenkorbmenge |

Die Gleichheit eines Artikels richtet sich nach `gerichtId`, nicht nach Preis oder Objektidentität.

## F2 – Warenkorb und Bestellung persistieren ⬜

Die vorhandenen Klassen weiterentwickeln:

- `Warenkorb` gehört zu genau einem `Profil`.
- `Bestellung` enthält Abholzeit, Bestellzeit, Status, Code und eigene Item-Snapshots.
- Beim Checkout werden Warenkorb-Items kopiert; späteres Leeren darf Bestellungen nicht verändern.
- `BestellungRepository` und `BestellungController` anlegen.

API:

```text
GET  /api/orders?email=<email>
POST /api/orders
PATCH /api/orders/{id}
```

Der POST-Body enthält E-Mail, Abholzeit und Items mit ID, Name, Preis und Menge.

## F3 – Abstimmung ohne Dummy-Hauptspeise ⬜

`profil.Hauptspeise` entfernen. `Abstimmung` arbeitet mit realen `gerichtId`-Werten.

- Stimmenstände persistent speichern.
- Für dieselbe E-Mail und dieselbe Gericht-ID nur eine Stimme zulassen.
- `AbstimmungRepository` und `AbstimmungController` anlegen.

```text
GET  /api/votes
GET  /api/votes/my?email=<email>
POST /api/votes/{gerichtId}?email=<email>
```

## F4 – Bestellungen und Stimmen veröffentlichen ⬜

Nach erfolgreichem Speichern:

- Bestellung als JSON an `mensa/orders` publizieren,
- Stimmenstände als JSON an `mensa/votes` publizieren.

Die Nachrichten sind nicht retained. Fehler beim Publish dürfen die bereits gespeicherte Bestellung oder Stimme nicht rückgängig machen; sie müssen aber nachvollziehbar geloggt werden.

## Abnahme Paket F

Zwei Gerichte mit gleichem Preis bleiben getrennte Warenkorbpositionen. Eine Bestellung bleibt nach Warenkorb-Leerung erhalten. Doppelte Stimme liefert HTTP 409. Beide Aktionen erscheinen als MQTT-Nachrichten am Simulator.

---

# Paket G – Frontend: vollständiger Nutzerablauf

**Ziel:** Das Frontend verbindet alle vorherigen Pakete zu einer nutzbaren Anwendung.

**Größe:** Vite-Setup, Seiten, Kontexte, API-Anbindung und Browser-Abnahme.

## G1 – Frontend-Grundlage ⬜

Unter `frontend/` ein React/Vite-Projekt mit TypeScript erstellen. Benötigt werden:

- API-Helfer in `src/lib/api.ts`,
- Navigation und wiederverwendbare UI-Komponenten,
- Routing für Speiseplan, Standorte, Bestellungen, Abstimmung und Profil,
- globale Styles.

Die Vite-Proxy-Regel leitet `/api` an `http://localhost:8082` weiter.

## G2 – Speiseplan, Mensen und Abstimmung ⬜

- `Home.tsx` lädt `/api/menu`, filtert Tags/Allergene und legt Gerichte in den Warenkorb.
- `Standorte.tsx` lädt `/api/mensen`.
- `Abstimmung.tsx` lädt `/api/votes`, nutzt bei Login `user.email` für eigene Stimmen und sperrt bereits gewählte Gerichte.

## G3 – Profil und Präferenzen ⬜

`AuthContext` speichert nur das zurückgegebene Profil im `localStorage` unter `profil`.

- Registrierung fragt ausschließlich E-Mail und Passwort ab.
- Vor- und Nachname werden im Backend aus der E-Mail erzeugt.
- Login, Logout und Profildarstellung umsetzen.
- Präferenzen über die Paket-D-Endpunkte laden und speichern.

## G4 – Warenkorb und Checkout ⬜

- `CartContext` hält den sichtbaren Warenkorb.
- `Bestellungen.tsx` sendet beim Checkout E-Mail, Items und Abholzeit an Paket F.
- Bestellhistorie anzeigen und Status aktualisieren.
- Abholzeit-Slots zwischen 11:30 und 14:15 anbieten; nach Ladenschluss Slots ab 11:30 am Folgetag anbieten, nie eine leere Auswahl.

## G5 – Browser-Abnahme ⬜

1. Profil registrieren und nach Reload wieder anzeigen.
2. Zwei Gerichte hinzufügen, Menge verändern und bestellen.
3. Abholzeit zu einer Tageszeit nach 14:15 prüfen.
4. Bestellhistorie und Bestellcode prüfen.
5. Abstimmen, Reload durchführen und doppelte Stimme prüfen.
6. `npm run build` erfolgreich ausführen.

---

## 3. Abhängigkeiten und parallele Bearbeitung

```text
Paket A ──────┬──────── Paket B ──────── Paket C ────────┐
              │                                           │
              ├──────── Paket D                            │
              │                                            ▼
              └──────── Paket E ──────── Paket F ─────── Paket G
```

| Paket | Kann sofort starten | Wartet auf |
|---|---|---|
| A | ja | – |
| B | ja | PostgreSQL aus A für vollständige Abnahme |
| C | ja | B und Mosquitto aus A |
| D | ja | PostgreSQL aus A |
| E | ja | C für den echten MQTT-End-to-End-Test |
| F | ja | D; C für MQTT-Abnahme |
| G | UI-Grundlage sofort | E, F und D für vollständige Integration |

---

## 4. Gemeinsame Endabnahme

Erst wenn alle sieben Pakete abgeschlossen sind, gilt der Projektstand als fertig:

```powershell
docker compose up -d
.\mvnw.cmd verify
cd frontend
npm ci
npm run build
```

Danach den vollständigen Ablauf prüfen:

1. Simulator und App-Backend starten.
2. Speiseplan und Mensen im Frontend sehen.
3. Profil registrieren und einloggen.
4. Gericht bestellen und Eingang im Simulator sehen.
5. Gericht bewerten und Vote-Total im Simulator sehen.
6. Backend und Frontend neu starten; Profil, Bestellungen und Präferenzen bleiben erhalten.
