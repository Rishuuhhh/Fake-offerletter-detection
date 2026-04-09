# Fake Offer Detection System

A Java Swing desktop application that detects fake internship and job offers using rule-based checks, enhanced NLP phrase analysis, user authentication, persistent storage, and an adaptive learning feedback loop.

## Features

### Offer Analysis
- Classifies offers as `GENUINE`, `SUSPICIOUS`, or `FAKE`
- Overall risk score `0–100` and a separate NLP signal score `0–100`
- Detailed findings explaining every flag raised
- Supports both internship and full-time job offer contexts
- Detects common scam indicators:
  - Free/public email domains
  - Unrealistic compensation for the role
  - Registration or security fee requests
  - Urgency and pressure language
  - Upfront personal-information demands (Aadhaar, OTP, bank details)
  - Domain-spoofing patterns (e.g. `amazon-hr.com`)
  - Salary-promise patterns (e.g. "guaranteed salary of ₹1.5 lakh")

### Enhanced NLP Engine
- 25+ high-risk fraud phrases with diminishing-returns scoring on repeats
- 15+ medium-risk persuasion phrases
- 15+ genuine-signal phrases that reduce suspicion
- Domain-spoofing regex detection (+20 pts, capped at 1 hit)
- Salary-promise regex detection (+10 pts, capped at 2 hits)
- Short-text penalty for descriptions under 20 words

### Adaptive Learning (Feedback Loop)
- After every analysis, click **Mark as Fake** or **Mark as Genuine**
- If the system verdict was wrong, the top 5 distinctive phrases are extracted and added to the NLP vocabulary
- Learned phrases are persisted to SQLite and `data/learned_vocab.csv`
- On the next analysis the updated vocabulary is applied automatically
- Phrases from feedback get a 1.2× weight multiplier once 10+ feedback entries exist

### User Authentication
- Register with a unique username (3–30 chars, alphanumeric/underscore)
- Passwords hashed with SHA-256 + per-user random salt (no plaintext ever stored)
- Login lockout after 5 consecutive failed attempts (60-second cooldown)
- Session displayed in the header; logout clears all form fields

### Persistent Storage
- **Primary**: SQLite database (`data/fakeofferdetector.db`)
- **Fallback**: CSV flat files (`data/credentials.csv`, `data/history.csv`, `data/learned_vocab.csv`)
- Auto-creates schema and seeds 22 known fake offer records on first run
- Analysis history per user, viewable in-app via the **History** button

## Tech Stack

- Java 25+
- Java Swing (`javax.swing`) for the desktop UI
- SQLite via `sqlite-jdbc` (single external JAR, managed by Maven)
- `java.security.MessageDigest` for SHA-256 password hashing (no third-party crypto)
- Maven for dependency management and build

## Project Structure

```
project-root/
├── src/
│   ├── main/
│   │   └── AppLauncher.java         ← entry point
│   ├── ui/
│   │   ├── Theme.java               ← shared colors, fonts, component helpers
│   │   ├── AppFrame.java            ← single JFrame, CardLayout host
│   │   ├── LoginScreen.java
│   │   ├── RegisterScreen.java
│   │   ├── MainScreen.java          ← offer analysis form
│   │   └── HistoryPanel.java
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── Session.java
│   ├── db/
│   │   ├── DatabaseManager.java
│   │   ├── UserStore.java
│   │   ├── HistoryStore.java
│   │   ├── SeedLoader.java
│   │   ├── VocabularyStore.java
│   │   └── FeedbackLogger.java
│   ├── model/
│   │   ├── Offer.java
│   │   ├── JobOffer.java
│   │   ├── InternshipOffer.java
│   │   ├── VerificationResult.java
│   │   ├── User.java
│   │   ├── AnalysisRecord.java
│   │   ├── LearnedVocabEntry.java
│   │   └── FeedbackEvent.java
│   ├── service/
│   │   ├── NlpSignalAnalyzer.java
│   │   ├── VerificationEngine.java
│   │   ├── RuleChecker.java
│   │   ├── FeedbackProcessor.java
│   │   └── PhraseExtractor.java
│   └── utils/
│       ├── InputValidator.java
│       ├── PasswordHasher.java
│       └── FileStore.java
├── lib/
│   └── README.txt                   ← where to place sqlite-jdbc jar (Maven handles this)
├── data/                            ← created at runtime (gitignored)
└── pom.xml
```

## Database Schema

Five tables are auto-created on first run:

| Table | Purpose |
|---|---|
| `users` | Registered user credentials (hashed) |
| `analysis_history` | Per-user offer analysis records |
| `seed_offers` | 22 pre-seeded known fake offer letters |
| `learned_vocabulary` | NLP phrases learned from user feedback |
| `feedback_log` | Audit log of every feedback submission |

## How Scoring Works

`VerificationEngine` combines rule-based and NLP checks:

1. Email domain legitimacy
2. Salary/stipend realism (different threshold for internships)
3. Fee request presence
4. Urgency and personal-info flags
5. Weak company name patterns
6. NLP phrase analysis (weighted, with diminishing returns)
7. Direct suspicious phrase matches

Final score is clamped to `0–100` and classified:

| Range | Verdict |
|---|---|
| 0–29 | `GENUINE` |
| 30–59 | `SUSPICIOUS` |
| 60–100 | `FAKE` |

## Prerequisites

- JDK 25 or newer
- Maven 3.9+

Check your versions:

```bash
java -version
mvn -version
```

## Build & Run

From the project root:

```bash
# Run directly (Maven downloads SQLite dependency automatically)
mvn exec:java

# Or build a fat jar and run it
mvn package
java -jar target/fake-offer-detector-1.0.0-jar-with-dependencies.jar
```

On first launch the app will:
1. Create `data/fakeofferdetector.db` and all five tables
2. Seed 22 known fake offer records
3. Show the **Login** screen

### Running Tests

Tests require jqwik and JUnit 5 jars in `lib/` — see `lib/README.txt` for download links.

```bash
# Compile everything (app + tests)
javac -cp "lib/sqlite-jdbc-3.x.x.jar:lib/jqwik-1.x.x.jar:lib/junit-platform-console-standalone-1.x.x.jar" \
      -d out \
      $(find src -name "*.java")

# Run all tests
java -cp "out:lib/sqlite-jdbc-3.x.x.jar:lib/jqwik-1.x.x.jar:lib/junit-platform-console-standalone-1.x.x.jar" \
     org.junit.platform.console.ConsoleLauncher --select-package=test
```

## Usage

1. **Register** — create an account with a unique username and password (min. 8 chars)
2. **Login** — sign in to access the offer detection form
3. **Analyse** — fill in offer details and click **Analyze Offer**
4. **Feedback** — after seeing the result, click **Mark as Fake** or **Mark as Genuine** to correct the system
5. **History** — click **History** in the header to review past analyses
6. **Logout** — click **Sign Out** to end your session

## Notes and Limitations

- NLP analysis is phrase-signal based, not a trained ML model
- Rule thresholds are heuristic and can be tuned in `NlpSignalAnalyzer.java`
- The feedback loop improves accuracy over time but requires multiple corrections to have a noticeable effect
- This tool assists screening — it does not replace human verification

## Team

JVM Juggernauts (`JAVA-IV-T062`) — TCS-408

- Shanu Khatana
- Disha Jha
- Rakshit Sharma
- Tanuja Kanswal

## License

This project is licensed under the terms in [LICENSE](LICENSE).
