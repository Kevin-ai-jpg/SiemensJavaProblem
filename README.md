# Siemens Java Problem – Train Ticketing App (Spring Boot + SQLite)

Hi! This is my student project for a train ticketing application. It’s a Spring Boot REST API with a small static HTML dashboard and a SQLite database.

I tried to keep the code organized (controller → service → repo → model) and the project easy to run.

---

## Features (from the code)

**User**

- Search direct journeys
- Search journeys with a single transfer
- Book seats between two stops (checks capacity)
- Booking confirmation email (console or SMTP)

**Admin**

- Create/update/delete trains
- Create/update/delete stations
- List trains/stations
- View bookings per train
- Send delay notification email to all passengers on a train

---

## Architecture (packages)

- **controller/**  
  `SearchController`, `BookingController`, `AdminController`
- **service/**  
  `TrainService`, `BookingService`, `AdminService`, `EmailService`
- **repo/**  
  `TrainRepository`, `StationRepository`, `BookingRepository`
- **model/**  
  `Train`, `Station`, `StopTime`, `Booking` + converters
- **sqlite/**  
  `SQLiteDialect` (custom Hibernate dialect for SQLite)

---

## Database

- SQLite file: `trains.db`
- Schema migrations:  
  `src/main/resources/db/migration/`
  - `V1__init.sql` (tables + indexes)
  - `V2__normaliza_booked_at.sql` (fixes booking timestamps)

---

## Email behavior

There are two implementations:

- **ConsoleEmailService** (default when `app.email.enabled=false` or missing)
- **SmtpEmailService** (when `app.email.enabled=true`)

SMTP is configured in `application.properties` using:

- `SMTP_USERNAME`
- `SMTP_PASSWORD`

---

## Tech stack / Libraries (based on code)

- Java + Spring Boot
- Spring Data JPA
- SQLite JDBC
- Spring Mail
- Flyway-style migrations (db/migration)

---

## How to run

1. Open the project in IntelliJ/Eclipse
2. Run `TrainApplication` (Spring Boot main class)
3. App runs at: `http://localhost:8080`
4. Optional dashboard: open `http://localhost:8080/` (static `index.html`)

---

## Seed data (from TrainApplication)

On startup, it creates:

**Stations**

- STA, STB, STC, STD, STE, STF

**Trains**

- **T1**: STA → STB → STC → STD
- **T2**: STC → STE → STF
- **T3**: STA → STE → STF

---

## REST API

### Search

- **Direct**  
  `GET /api/search/direct?from=STA&to=STD`
- **Single transfer**  
  `GET /api/search/transfer?from=STA&to=STF&minTransferMinutes=5`

### Booking

- **Create booking**  
  `POST /api/book`
  ```json
  {
    "trainCode": "T1",
    "fromIndex": 0,
    "toIndex": 2,
    "seats": 2,
    "passengerName": "Student",
    "passengerEmail": "student@example.com"
  }
  ```

### Admin

- **List trains**  
  `GET /api/admin/trains`
- **List stations**  
  `GET /api/admin/stations`
- **Create train**  
  `POST /api/admin/trains`
- **Update train**  
  `PUT /api/admin/trains/{trainCode}`
- **Delete train**  
  `DELETE /api/admin/trains/{trainCode}`
- **Bookings for train**  
  `GET /api/admin/trains/{trainCode}/bookings`
- **Send delay notice**  
  `POST /api/admin/trains/{trainCode}/delay`
- **Create station**  
  `POST /api/admin/stations`
- **Update station**  
  `PUT /api/admin/stations/{code}`
- **Delete station**  
  `DELETE /api/admin/stations/{code}`

---

## Problem 2 (optional) – Fastest Route Recommendation

**Problem I define:** Given stations and train stop times, suggest the _fastest_ route between two stations (even with transfers).

**Idea:** Build a graph where each leg has a “travel time” weight, then run Dijkstra to find the minimum total time.

**Why it’s interesting:** It upgrades the search from “direct or 1 transfer” to “optimal route”.

**Mini Java-style example (simplified):**

```java
// pseudo/mini-code for Dijkstra
Map<String, List<Edge>> graph = new HashMap<>();
// Edge = (toStation, minutes)

int dijkstra(String start, String end) {
  Map<String, Integer> dist = new HashMap<>();
  PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
  dist.put(start, 0);
  pq.add(new Node(start, 0));

  while (!pq.isEmpty()) {
    Node cur = pq.poll();
    if (cur.name.equals(end)) return cur.dist;
    if (cur.dist > dist.getOrDefault(cur.name, Integer.MAX_VALUE)) continue;

    for (Edge e : graph.getOrDefault(cur.name, List.of())) {
      int nd = cur.dist + e.minutes;
      if (nd < dist.getOrDefault(e.to, Integer.MAX_VALUE)) {
        dist.put(e.to, nd);
        pq.add(new Node(e.to, nd));
      }
    }
  }
  return -1; // no route
}
```

**Result:** The API can return the fastest route with total time and the sequence of legs.

---

## TSP idea (optional, advanced) – heuristics for many stations

If the number of stations grows, finding the _best_ route becomes complex.  
A classic way to model this is as a **TSP-like problem**, and use heuristics:

**PSO (Particle Swarm Optimization)**

- Each particle is a candidate route.
- Update velocity using personal best + global best.

**SA (Simulated Annealing)**

- Start with a route, randomly swap, and sometimes accept worse moves.
- Slowly reduce the “temperature”.

**ACO / ACS / AS (Ant Colony methods)**

- Many ants build routes based on pheromones.
- Better routes deposit more pheromone.
- ACS is a faster variant with stronger exploitation.

**Mini pseudocode (PSO)**

```
init swarm with random routes
for iter in 1..N:
  for each particle:
    evaluate cost(route)
    update personalBest and globalBest
    update velocity (swap sequence)
    apply velocity to route
return globalBest
```

**Mini pseudocode (SA)**

```
route = random()
T = T0
while T > Tmin:
  candidate = swapTwo(route)
  if cost(candidate) < cost(route) or rand() < exp(-(Δcost)/T):
    route = candidate
  T = cool(T)
return route
```

**Mini pseudocode (ACO / ACS / AS)**

```
init pheromones
for iter in 1..N:
  for each ant:
    build route using pheromones + heuristic
  evaporate pheromones
  deposit pheromones on best routes
return best route
```

**Why I include this:** It shows how the project could be scaled for “many stations” without brute force.

---

## cURL examples (admin)

> Replace values if you changed station/train codes.

**List trains**

```
curl -s http://localhost:8080/api/admin/trains
```

**List stations**

```
curl -s http://localhost:8080/api/admin/stations
```

**Create train**

```
curl -s -X POST http://localhost:8080/api/admin/trains \
  -H "Content-Type: application/json" \
  -d '{
    "trainCode":"T4",
    "name":"Express 4",
    "capacity":80,
    "stops":[
      {"stationCode":"STA","arrival":"","departure":"06:00"},
      {"stationCode":"STB","arrival":"06:25","departure":"06:30"},
      {"stationCode":"STD","arrival":"07:10","departure":""}
    ]
  }'
```

**Update train**

```
curl -s -X PUT http://localhost:8080/api/admin/trains/T4 \
  -H "Content-Type: application/json" \
  -d '{
    "trainCode":"T4",
    "name":"Express 4 (updated)",
    "capacity":90,
    "stops":[
      {"stationCode":"STA","arrival":"","departure":"06:10"},
      {"stationCode":"STB","arrival":"06:35","departure":"06:40"},
      {"stationCode":"STD","arrival":"07:20","departure":""}
    ]
  }'
```

**Delete train**

```
curl -s -X DELETE http://localhost:8080/api/admin/trains/T4
```

**Bookings for a train**

```
curl -s http://localhost:8080/api/admin/trains/T1/bookings
```

**Send delay notice**

```
curl -s -X POST http://localhost:8080/api/admin/trains/T1/delay \
  -H "Content-Type: application/json" \
  -d '{"delayMinutes":15,"reason":"Technical issue"}'
```

**Create station**

```
curl -s -X POST http://localhost:8080/api/admin/stations \
  -H "Content-Type: application/json" \
  -d '{"code":"STX","name":"Station X"}'
```

**Update station**

```
curl -s -X PUT http://localhost:8080/api/admin/stations/STX \
  -H "Content-Type: application/json" \
  -d '{"code":"STX","name":"Station X (updated)"}'
```

**Delete station**

```
curl -s -X DELETE http://localhost:8080/api/admin/stations/STX
```

---

## Screenshots

> Place your images at the paths below (or rename the links).

![Email header details](docs/images/email-header.png)
![Booking confirmation email](docs/images/booking-confirmed.png)

---

## Notes / Limitations

- Booking uses a JVM lock per train to avoid SQLite locking issues.
- For real multi‑user concurrency, a DB with row locks would be better.
- Email only sends if SMTP is configured.

---
