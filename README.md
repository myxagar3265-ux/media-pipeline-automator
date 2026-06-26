# Enterprise Media Distribution Pipeline (OAuth2 Automated)

A production-grade, high-performance Java desktop application engineered for decentralized multi-user credential acquisition and concurrent batch video distribution via the YouTube Data API v3. 

This system architecture decouples internal processing nodes from rigid external SDK dependencies, utilizing native asynchronous HTTP/2 network transport, granular in-memory multi-threaded execution pools, and a localized micro-server layer for automated web token interception.

---

## 📐 System Architecture & Core Modules

The application is built on three core pillars designed to ensure execution isolation, security compliance, and low resource overhead:

### 1. Decentralized In-Memory Token Rotation (`OAuth2 Flow`)
Unlike monolithic corporate systems relying on persistent global databases, this pipeline operates a localized token rotation workflow:
- **Transient Keys (`access_token`):** Valid for exactly 60 minutes. Injected dynamically into every REST transaction header (`Authorization: Bearer ...`).
- **Persistent Management Keys (`refresh_token`):** Issued once during primary handshake authorization. Safely cached inside a git-ignored file descriptor framework (`channels.txt`). 
- **Automated Exchange:** Before a batch sequence initiates, the core manager dispatches an asynchronous call to Google's Token Exchange Service (`https://oauth2.googleapis.com/token`) to automatically refresh credentials without manual user interaction.

### 2. Micro-Server Callback Handler (`Socket Interception`)
To achieve zero-touch configuration for the end-user, the software implements a temporary embedded HTTP engine using native Java runtime structures (`com.sun.net.httpserver.HttpServer`):
- Spawns an active listener isolated strictly to port `8080` upon profile link initialization.
- Leverages the native OS routing layer to target the system’s default web browser, pointing to Google’s secure authorization endpoint.
- Captures the parameter block (`?code=...`) via a decoupled thread context block, dispatches an automated token exchange payload, writes the newly formed user instance record, and immediately triggers an atomic server shutdown sequence to free network interfaces.

### 3. Throttled Resource Pipeline Engine (`Concurrency & I/O Control`)
Concurrent streaming of massive uncompressed binary media files can induce network buffer chokes and heavy I/O overhead. The application enforces strict runtime boundaries:
- Managed allocation via `java.util.concurrent.ExecutorService` initialized with a fixed pooling limit of exactly **2 parallel worker tasks**.
- Dynamic worker tasks handle chunk re-allocation, thread telemetry piping directly onto responsive Java Swing visual components, and standalone asynchronous POST serialization natively.

---

## 📊 Technical Conventions, Constraints & Design Compromises

Operating within public cloud architecture constraints requires implementing specific engineering boundaries within the code logic:

### ⚙️ Operational Boundaries Specification Table

| Architectural Variable | Engineering Metric | Operational Context / Constraint Description |
| :--- | :--- | :--- |
| **API Daily Quota Budget** | `10,000 units` | Enforced by Google Cloud. Single upload costs `~1,600` units. Limits application lifecycle to **6 video deployments per day per project**. |
| **Concurrent Execution Cap**| `2 Thread Pools` | Strictly throttled to prevent local disk I/O bottlenecks and high remote packet-drop probability during upload. |
| **Security Perimeter** | `Local / Decentralized`| Absolute avoidance of external centralized storage. Secrets reside purely in git-ignored, isolated runtime blocks. |
| **I/O File Sanitization** | `Alphanumeric Explicit`| Native regex pattern wipes special characters from filenames to avoid platform serialization bugs (`a-zA-Z0-9`, Cyrillic, space, `_`). |
| **Chronological Scheduling** | `ISO 8601 Strict` | Timestamps require precise zone offsets (e.g., `+03:00`). Invalid syntax results in immediate Google API rejection. |

### Ingestion Interface Schema (Batch Payload Structuring)
The scheduling parser demands a fully verified JSON structure containing data instructions mapped directly onto target domain nodes. See the explicit data footprint below:

```json
[
  {
    "videoFile": "simulation_render_sequence_01.mp4",
    "title": "Quantum Fluid Dynamics Simulation",
    "tags": ["physics", "engineering", "neon"],
    "description": "Automated batch processing test render sequence. Compiled under Java core execution models.",
    "publishAt": "2026-07-15T16:00:00+03:00"
  }
]
