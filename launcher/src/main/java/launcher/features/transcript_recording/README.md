# Transcript Recording

This directory contains packages for handling transcript recording throughout game sessions, from session management to message recording and transcript saving.

## Package Overview

### 1. session_management

**Entry Point:** `StartSession.java`, `EndSession.java`

**Responsibility:** Manages transcript session lifecycle, including starting sessions with game metadata and ending sessions when end messages are detected.

---

### 2. recording

**Entry Point:** `RecordInboundMessage.java`, `RecordOutboundMessage.java`

**Responsibility:** Records messages exchanged during game sessions, distinguishing between inbound messages (from game) and outbound messages (to game).

---

### 3. transcript_saving

**Entry Point:** `TranscriptSaver.java`

**Responsibility:** Handles saving transcripts to files in multiple formats (JSON and text). Includes format-specific savers and helper utilities for timestamp formatting and message summarization.

---

### 4. Transcript

**Entry Point:** `Transcript.java`

**Responsibility:** Provides shared state for transcript recording, including session status and thread-safe storage for transcript entries.

---

