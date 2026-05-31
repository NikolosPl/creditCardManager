# PRD — REST API: System Zarządzania Kartami Kredytowymi

## 1. Cel projektu

Backend REST API umożliwiający zarządzanie kartami kredytowymi: wydawanie kart, blokowanie, zmianę limitów oraz przeglądanie historii operacji. Brak uwierzytelniania — wszystkie endpointy publiczne.

---

## 2. Stack technologiczny

| Warstwa | Technologia |
|---|---|
| Framework | Spring Boot 3.x |
| Baza danych | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Build | Maven / Gradle |
| Dokumentacja API | Springdoc OpenAPI (Swagger UI) |

---

## 3. Model danych

### 3.1 `CreditCard`
```
id             BIGSERIAL PRIMARY KEY
card_number    VARCHAR(19) UNIQUE NOT NULL
holder_name    VARCHAR(100) NOT NULL
credit_limit   NUMERIC(12,2) NOT NULL
current_balance NUMERIC(12,2) DEFAULT 0
status         VARCHAR(10) NOT NULL  -- ACTIVE | BLOCKED | EXPIRED
issued_at      TIMESTAMP NOT NULL
expires_at     TIMESTAMP NOT NULL
```

### 3.2 `CardOperation`
```
id          BIGSERIAL PRIMARY KEY
card_id     BIGINT REFERENCES credit_card(id)
type        VARCHAR(20) NOT NULL  -- ISSUE | BLOCK | UNBLOCK | LIMIT_CHANGE
description TEXT
amount      NUMERIC(12,2)
timestamp   TIMESTAMP NOT NULL DEFAULT NOW()
```

---

## 4. Konfiguracja PostgreSQL (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/credit_cards_db
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 5. Endpointy API

### Karty
| Metoda | Ścieżka | Opis |
|---|---|---|
| POST | `/api/cards` | Wydaj nową kartę |
| GET | `/api/cards` | Lista wszystkich kart |
| GET | `/api/cards/{id}` | Szczegóły karty |
| PATCH | `/api/cards/{id}/block` | Zablokuj kartę |
| PATCH | `/api/cards/{id}/unblock` | Odblokuj kartę |
| PATCH | `/api/cards/{id}/limit` | Zmień limit kredytowy |
| DELETE | `/api/cards/{id}` | Usuń kartę |

### Historia
| Metoda | Ścieżka | Opis |
|---|---|---|
| GET | `/api/cards/{id}/history` | Historia operacji karty |
| GET | `/api/operations` | Wszystkie operacje w systemie |

---

## 6. Kluczowe wymagania — Spring Data JPA

- Repozytoria: `CreditCardRepository`, `CardOperationRepository`
- Metody przez konwencję nazw: `findByStatus`, `findByCardId`
- Każda modyfikacja karty (blokada, zmiana limitu) zapisuje wpis w `CardOperation`
- Operacje modyfikujące oznaczone `@Transactional`

---

## 7. Walidacja i błędy

| Kod | Sytuacja |
|---|---|
| `400` | Nieprawidłowe dane wejściowe |
| `404` | Karta nie istnieje |
| `409` | Karta już zablokowana / już aktywna |

---

## 8. Przykładowe request/response

### POST `/api/cards` — wydanie karty
**Request:**
```json
{
  "holderName": "Jan Kowalski",
  "creditLimit": 5000.00
}
```
**Response `201`:**
```json
{
  "id": 12,
  "cardNumber": "**** **** **** 4821",
  "status": "ACTIVE",
  "creditLimit": 5000.00,
  "issuedAt": "2026-05-31T10:00:00Z"
}
```

### PATCH `/api/cards/12/limit`
**Request:**
```json
{ "newLimit": 8000.00 }
```
**Response `200`:**
```json
{
  "id": 12,
  "creditLimit": 8000.00,
  "updatedAt": "2026-05-31T11:30:00Z"
}
```

---

## 9. Zakres MVP

- [x] Wydawanie kart
- [x] Blokowanie / odblokowanie
- [x] Zmiana limitu
- [x] Historia operacji
- [ ] *(poza MVP)* Uwierzytelnianie / autoryzacja
- [ ] *(poza MVP)* Płatności / transakcje
- [ ] *(poza MVP)* Eksport historii do PDF/CSV