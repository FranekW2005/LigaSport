#  LigaSport

Aplikacja mobilna na Androida do tworzenia i zarządzania lokalnymi ligami piłkarskimi ze znajomymi.

##  Opis

LigaSport pozwala tworzyć ligi, dodawać drużyny i zawodników, planować mecze oraz śledzić statystyki. Aplikacja wykorzystuje Firebase do synchronizacji w czasie rzeczywistym – wszystko co robisz, od razu widzą pozostali członkowie ligi.

##  Funkcjonalności

-  **Logowanie i rejestracja** – email + hasło, spersonalizowana nazwa użytkownika
-  **Zarządzanie ligami** – twórz ligi, przeglądaj, usuwaj
-  **System admina** – twórca ligi dostaje uprawnienia administratora
-  **Drużyny i zawodnicy** – dodawaj drużyny globalne, przypisuj do lig, zarządzaj składami
-  **Mecze** – planuj mecze między drużynami, wybieraj z dropdowna
-  **Profil użytkownika** – zmiana nazwy, awatar z inicjałem
-  **Ciemny motyw** – kolorystyka inspirowana boiskiem (zieleń + pomarańcz)

##  Technologie

| Technologia | Zastosowanie |
|-------------|-------------|
| **Kotlin** | Język programowania |
| **Jetpack Compose** | Nowoczesny UI |
| **Firebase Auth** | Logowanie i rejestracja |
| **Firebase Firestore** | Baza danych w chmurze |
| **Material3** | Design system Google |

##  Jak uruchomić

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/TwojaNazwa/LigaSport.git
2. Otwórz projekt w Android Studio.
3. Połącz z Firebase:
    - Utwórz projekt w Firebase Console
    - Dodaj plik google-services.json do folderu app/
    - Włącz Authentication (Email/Password)
    - Utwórz Firestore Database
4. Uruchom na emulatorze lub telefonie

## Struktura Firestore

leagues/

    └── {leagueId}/
      ├── name, adminId
      └── teams/ (podkolekcja)

global_teams/

    └── {teamId}/
      ├── name, ownerId    
      └── players[]

matches/

    └── {matchId}/
      ├── leagueId, homeTeam, awayTeam    
      └── date, time, homeScore, awayScore

users/

    └── {userId}/
      └── userName, email

## W planach

- Kalendarz meczów
- Tabela ligowa (punkty, bilans bramek)
- Statystyki zawodników (bramki, asysty, kartki)
- Integracja z Google Health Connect
- Powiadomienia o meczach
- Zapraszanie do ligi przez email

Projekt stworzony w ramach studiów | 2026
