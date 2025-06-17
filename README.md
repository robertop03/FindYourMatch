# FindYourMatch – App Android per partite di calcio

Progetto realizzato per il corso Programmazione di Sistemi Mobile (A.A. 2024/2025)  
Sviluppatori: Roberto Pisu e Marco Giugliani

---

## Descrizione dell’app

FindYourMatch è un'app Android progettata per facilitare l’organizzazione di partite di calcio amatoriali (5vs5, 7vs7, 8vs8, 11vs11).  
L’app consente di:

- Creare partite, scegliendo campo, prezzo previsto, data, orario e formazioni.
- Visualizzare match creati da altri utenti entro un raggio personalizzabile dalla propria posizione.
- Iscriversi o cancellarsi dalle partite.
- Rilasciare recensioni sugli altri giocatori e statistiche a fine partita (es. marcatori, autogol).
- Favorire partite competitive tra partecipanti affidabili grazie a un sistema di valutazione basato sull’esperienza reale.

---

## Funzionalità principali

- Login con credenziali e accesso biometrico (Face ID o impronta digitale)
- Recupero password tramite email di conferma
- Gestione profilo personale (foto, dati, password)
- Gamification con obiettivi a livelli (bronzo, argento, oro)
- Statistiche personali e delle partite (con grafici integrati)
- Ricerca match vicino a una posizione definita (con raggio personalizzabile)
- Notifiche in-app sempre attive e notifiche push tramite Firebase (se abilitate)
- Filtraggio personalizzato dei match
- Supporto tema chiaro, scuro o automatico
- Cambio lingua: Italiano e Inglese

---

## Tecnologie utilizzate

### Backend – Supabase
- Database relazionale PostgreSQL ospitato in cloud
- Sicurezza tramite Row-Level Security e policy personalizzate
- Edge Functions scritte in TypeScript per logica lato server
- Azioni pianificate tramite GitHub Actions

### Notifiche Push – Firebase + Server Node.js
- Supabase non gestisce direttamente i FCM Token, perciò è stato implementato un server Node.js (repo: `fcm-proxy`)
- Il server interagisce con Firebase per l'invio delle notifiche push

### Frontend – Android App (Kotlin)
- Jetpack Compose per l'interfaccia utente
- Gestione della sessione utente e del token biometrico locale

---

## Librerie e strumenti principali

| Funzione                        | Tecnologia                                     |
|--------------------------------|------------------------------------------------|
| Grafici statistici             | MPAndroidChart                                 |
| Gestione immagini profilo      | Coil + Coil Compose                            |
| Autenticazione biometrica      | androidx.biometric                             |
| Chiamate HTTP personalizzate   | okhttp                                         |
| Prototipazione interfaccia     | Figma (link: vedi sotto)                       |

---

## Mockup e repository collegati

- Mockup UI su Figma:  
  https://www.figma.com/design/OOa3RSBNtRic7KOdm7YEAb/FindYourMatch?node-id=0-1&t=E05NOrze3XArL10L-1

- Repository server notifiche push (Node.js + Firebase):  
  https://github.com/robertop03/fcm-proxy
  
