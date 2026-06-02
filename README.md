# PRD — Fullstack: System Zarządzania Kartami Kredytowymi

## 1. Cel projektu

Aplikacja fullstack do zarządzania kartami kredytowymi: wydawanie kart, blokowanie, zmiana limitów i historia operacji. Frontend w Angular, backend REST API w Spring Boot z uwierzytelnianiem JWT.

---

## 2. Stack technologiczny

### Backend
| Warstwa | Technologia |
|---|---|
| Framework | Spring Boot 3.x |
| Bezpieczeństwo | Spring Security + JWT |
| Baza danych | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Build | Gradle |
| Dokumentacja API | Springdoc OpenAPI (Swagger UI) |

### Frontend
| Warstwa | Technologia |
|---|---|
| Framework | Angular 17+ |
| Język | TypeScript |
| Style | Angular Material |
| HTTP | `HttpClient` + Interceptory |
| Routing | Angular Router z guard'ami |
| State | Serwisy + RxJS |

---

## 3. Role użytkowników

| Rola | Uprawnienia |
|---|---|
| `ADMIN` | Pełny dostęp — wydawanie kart, blokowanie, zmiana limitów, historia wszystkich kart |
| `USER` | Podgląd własnych kart i historii operacji |

---

## 4. Model danych

```sql
CREATE TABLE "user" (
    id         UUID PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE "credit_cards" (
    id UUID PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    card_limit NUMERIC(19, 2) NOT NULL,
    used_funds NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_customerId foreign key (customer_id) references "user"(id)
);

CREATE TABLE "card_transactions" (
    id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES "credit_cards"(id)
);

```

---

## 5. Backend — Endpointy API

### Auth
| Metoda | Ścieżka | Opis | Dostęp |
|---|---|---|---|
| POST | `/api/auth/register` | Rejestracja | Publiczny |
| POST | `/api/auth/login` | Logowanie → JWT | Publiczny |

### Karty
| Metoda | Ścieżka | Opis | Dostęp |
|---|---|---|---|
| POST | `/api/cards` | Wydaj nową kartę | ADMIN |
| GET | `/api/cards` | Lista kart | ADMIN: wszystkie, USER: własne |
| GET | `/api/cards/{id}` | Szczegóły karty | Właściciel / ADMIN |
| PATCH | `/api/cards/{id}/block` | Zablokuj | ADMIN |
| PATCH | `/api/cards/{id}/unblock` | Odblokuj | ADMIN |
| PATCH | `/api/cards/{id}/limit` | Zmień limit | ADMIN |
| DELETE | `/api/cards/{id}` | Usuń kartę | ADMIN |

### Historia
| Metoda | Ścieżka | Opis | Dostęp |
|---|---|---|---|
| GET | `/api/cards/{id}/history` | Historia operacji karty | Właściciel / ADMIN |
| GET | `/api/operations` | Wszystkie operacje | ADMIN |

---

## 6. Backend — Spring Security

- JWT w nagłówku `Authorization: Bearer <token>`
- Filtr `JwtAuthenticationFilter` dla każdego żądania
- `UserDetailsService` ładujący użytkownika z PostgreSQL przez JPA
- Endpointy `/api/auth/**` wyłączone z ochrony
- Autoryzacja na poziomie metod (`@PreAuthorize`)
- CORS skonfigurowany dla `http://localhost:4200`

---

## 7. Backend — Spring Data JPA

- Repozytoria: `UserRepository`, `CreditCardRepository`, `CardOperationRepository`
- Metody przez konwencję nazw: `findByUserId`, `findByStatus`, `findByCardId`
- Każda modyfikacja karty zapisuje wpis w `CardOperation`
- Operacje modyfikujące oznaczone `@Transactional`

---

## 8. Frontend — Widoki Angular

| Widok | Ścieżka | Dostęp |
|---|---|---|
| Logowanie | `/login` | Publiczny |
| Rejestracja | `/register` | Publiczny |
| Dashboard | `/dashboard` | Zalogowany |
| Lista kart | `/cards` | Zalogowany |
| Szczegóły karty | `/cards/:id` | Właściciel / ADMIN |
| Panel admina | `/admin` | ADMIN |
| Historia operacji | `/cards/:id/history` | Właściciel / ADMIN |

---

## 9. Frontend — Kluczowe elementy

### AuthInterceptor
Automatyczne dołączanie JWT do każdego żądania HTTP:
```typescript
headers = req.headers.set('Authorization', `Bearer ${token}`);
```

### AuthGuard
Blokowanie tras dla niezalogowanych użytkowników i ról bez dostępu:
```typescript
canActivate(): boolean { return this.authService.isLoggedIn(); }
```

### Serwisy
- `AuthService` — login, logout, przechowywanie tokena w `localStorage`
- `CardService` — CRUD kart przez `HttpClient`
- `OperationService` — pobieranie historii

---

## 10. Walidacja i błędy

| Kod | Sytuacja |
|---|---|
| `400` | Nieprawidłowe dane wejściowe |
| `401` | Brak / nieprawidłowy token |
| `403` | Brak uprawnień do zasobu |
| `404` | Karta / użytkownik nie istnieje |
| `409` | Karta już zablokowana / już aktywna |

Frontend obsługuje błędy globalnie przez `HttpInterceptor` — `401` przekierowuje na `/login`.

---

## 11. Przykładowe request/response

### POST `/api/auth/login`
**Request:**
```json
{ "username": "admin", "password": "haslo123" }
```
**Response `200`:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### POST `/api/cards`
**Request:**
```json
{ "holderName": "Jan Kowalski", "userId": 5, "creditLimit": 5000.00 }
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

---

## 12. Zakres MVP

- [x] Rejestracja i logowanie (JWT)
- [x] Wydawanie kart (ADMIN)
- [x] Blokowanie / odblokowanie
- [x] Zmiana limitu
- [x] Historia operacji
- [x] Panel użytkownika (Angular)
- [x] Panel admina (Angular)
- [ ] *(poza MVP)* Powiadomienia email
- [ ] *(poza MVP)* Płatności / transakcje
- [ ] *(poza MVP)* Eksport historii do PDF/CSV