# Fake Offer Detector

A Java desktop app that checks if a job or internship offer is real or a scam. You paste in the offer details, it gives you a verdict — GENUINE, SUSPICIOUS, or FAKE — and tells you exactly why.

Built for TCS-408 by JVM Juggernauts (JAVA-IV-T062).  
Team: Shanu Khatana, Disha Jha, Rakshit Sharma, Tanuja Kanswal

---

## How to run

You just need Java installed. No Maven, no extra setup.

```bash
./run.sh
```

That compiles everything and launches the app. If you want to do it manually:

```bash
javac -d out $(find src -name "*.java")
java -cp out main.AppLauncher
```

The `data/` folder gets created automatically the first time you run it.

---
##  Tech Stack
* **Language:** Java 8+
* **Framework:** Java Swing (GUI)
* **Storage:** Local text-based datasets
  
## What it does

When you submit an offer, the app checks two things:

**Basic rule checks:**
- Is the email from a free provider like Gmail or Yahoo? That's a red flag.
- Is the salary way too high to be real?
- Does the offer ask for a registration or security fee?
- Does it ask for Aadhaar, OTP, or bank details upfront?
- Did you check the "urgent language" box?

**NLP phrase scanning:**
- Scans the description for known scam phrases like "pay now", "limited seats", "refer and earn", "passive income", "send via UPI"
- Also checks for fake domain patterns like `amazon-hr.com` or `tcs-hiring.net`
- Genuine phrases like "background verification" or "probation period" actually lower the risk score

Both scores get combined into a final number from 0 to 100:
- 0–29 = GENUINE
- 30–59 = SUSPICIOUS
- 60–100 = FAKE

---

## The feedback loop

After you get a result, you can click "Mark as Fake" or "Mark as Genuine" if the app got it wrong. It pulls key phrases from the description and adds them to the keyword files automatically. Next time you analyze something similar, it'll be more accurate.

---

## Keyword files

All the detection logic is driven by files in the `data/` folder. You can edit them in any text editor — no recompiling needed.

`data/fake_keywords.csv` — scam phrases, one per line with a type prefix:
```
high,pay registration fee
medium,work from home no experience needed
rush,act now limited seats
payment,send via upi
```

`data/genuine_keywords.csv` — phrases that suggest a real offer:
```
background verification
offer letter on company letterhead
probation period
```

`data/trusted_domains.csv` — company email domains you trust (one per line):
```
infosys.com
wipro.com
tcs.com
```

`data/blocked_domains.csv` — domains that are always suspicious:
```
gmail.com
yahoo.com
```

---

## Accounts and security

- Passwords are hashed with SHA-256 + a random salt, never stored in plain text
- After 5 wrong login attempts, the account locks for 60 seconds
- Each user only sees their own analysis history

---

## Project layout

```
src/
  main/AppLauncher.java       starts the app, wires everything together
  auth/                       login, registration, session, lockout
  model/                      data classes (Offer, User, AnalysisRecord, etc.)
  service/                    scoring engine, NLP analyzer, feedback processor
  store/                      reads and writes all the CSV files
  ui/                         all the screens (Login, Register, Main, History)
  utils/                      CSV helper, password hasher, input validator

data/                         created on first run
  credentials.csv             user accounts
  history.csv                 past analyses per user
  fake_keywords.csv           scam phrases
  genuine_keywords.csv        legitimate phrases
  trusted_domains.csv         known company domains
  blocked_domains.csv         free/scam domains
  feedback_log.csv            log of user corrections
```

---

## Test it with the sample PDFs

There are two sample offer letters in the repo:

- `Real-Offer-Letter-NovaTech.pdf` — a real-looking offer with proper CTC, no fees, official domain
- `FAKE-Offer-Letter-GlobalTech.pdf` — a scam with MLM structure, a ₹2,499 fee, and WhatsApp-only contact

Just copy the text from either one, paste it into the description field, and hit Analyze.

---

## Known limitations

- The NLP is phrase matching, not actual machine learning — it won't catch every scam
- If a scam uses unusual wording it hasn't seen before, it might miss it
- The thresholds (30/60) are tuned by hand and might not be perfect for every case
