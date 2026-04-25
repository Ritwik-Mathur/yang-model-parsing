# YANG Model Parser — Full Stack Project

> **Assignment 2: YANG Model Parsing**
> Spring Boot (Java 17) backend + React (Vite) frontend

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Prerequisites](#4-prerequisites)
5. [Step-by-Step Setup](#5-step-by-step-setup)
   - 5.1 [Run the Backend](#51-run-the-backend-spring-boot)
   - 5.2 [Run the Frontend](#52-run-the-frontend-react--vite)
6. [How to Use the App](#6-how-to-use-the-app)
7. [REST API Reference](#7-rest-api-reference)
8. [How the Parser Works](#8-how-the-parser-works)
9. [YANG Node Types & Constraints](#9-yang-node-types--constraints)
10. [Sample YANG Model (Built-in)](#10-sample-yang-model-built-in)
11. [Testing with curl](#11-testing-with-curl)
12. [Common Issues & Fixes](#12-common-issues--fixes)
13. [Expected Output](#13-expected-output)

---

## 1. Project Overview

This project fulfills the YANG Model Parsing assignment:

| Requirement | Status |
|---|---|
| Setup a new Java project | ✅ Spring Boot 3.2 + Maven |
| Load a YANG model file | ✅ Built-in sample + file upload + paste |
| Parse nodes, data types, constraints | ✅ Full RFC 7950 parser |
| Display hierarchical structure | ✅ Text hierarchy + interactive React tree |
| Testing | ✅ REST endpoints + visual UI |

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 17, Maven |
| REST API | Spring MVC (`@RestController`) |
| JSON | Jackson (auto-configured) |
| Frontend | React 18, Vite 5 |
| Styling | Pure CSS-in-JS (no extra libraries) |
| Communication | REST API with Vite proxy |

---

## 3. Project Structure

```
yang-parser/
│
├── backend/                              ← Spring Boot Maven project
│   ├── pom.xml
│   └── src/main/java/com/yangparser/
│       ├── YangParserApplication.java    ← @SpringBootApplication entry
│       ├── model/
│       │   ├── YangNode.java             ← Node: name, type, dataType, constraints, children
│       │   └── YangModule.java           ← Module: metadata + nodes + stats
│       ├── parser/
│       │   └── YangModelParser.java      ← Core brace-balanced tree parser
│       ├── service/
│       │   └── YangParserService.java    ← Business logic + built-in YANG sample
│       ├── controller/
│       │   └── YangParserController.java ← REST endpoints
│       └── src/main/resources/
│           └── application.properties
│
├── frontend/                             ← React + Vite project
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js                    ← Proxy: /api → localhost:8080
│   └── src/
│       ├── main.jsx                      ← React root mount
│       └── App.jsx                       ← Full UI (editor + tree + tabs)
│
└── README.md                             ← This file
```

---

## 4. Prerequisites

Make sure you have the following installed before starting:

### Java 17+
```bash
java -version
# Should show: openjdk 17.x.x or higher
```
Download: https://adoptium.net/

### Maven 3.8+
```bash
mvn -version
# Should show: Apache Maven 3.8.x or higher
```
Download: https://maven.apache.org/download.cgi

### Node.js 18+
```bash
node -v
# Should show: v18.x.x or higher

npm -v
# Should show: 9.x.x or higher
```
Download: https://nodejs.org/

---

## 5. Step-by-Step Setup

### 5.1 Run the Backend (Spring Boot)

Open a terminal and navigate to the `backend/` folder:

```bash
cd yang-parser/backend
```

**Build the project** (downloads all Maven dependencies):
```bash
mvn clean install -DskipTests
```

**Start the Spring Boot server:**
```bash
mvn spring-boot:run
```

You should see output like:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Started YangParserApplication in 2.345 seconds
```

**Verify it's running** — open in browser or curl:
```
http://localhost:8080/api/yang/health
```
Expected response:
```json
{"status":"UP","service":"YANG Model Parser"}
```

> **Backend runs on port 8080** by default.
> To change: edit `src/main/resources/application.properties` → `server.port=8081`

---

### 5.2 Run the Frontend (React + Vite)

Open a **second terminal** and navigate to the `frontend/` folder:

```bash
cd yang-parser/frontend
```

**Install Node dependencies:**
```bash
npm install
```

**Start the development server:**
```bash
npm run dev
```

You should see:
```
  VITE v5.x.x  ready in 300 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

**Open in browser:**
```
http://localhost:5173
```

> The Vite config has a proxy set up: any request to `/api/...` is automatically forwarded to `http://localhost:8080`. This avoids CORS issues.

---

## 6. How to Use the App

Once both servers are running and you open `http://localhost:5173`:

### Option A — Load Built-in Sample
1. Click **"⬇ Load Sample"** in the left panel
2. The built-in `network-device` YANG model is loaded into the editor
3. Click **"▶ Parse YANG Model"**
4. The right panel shows the parsed tree + stats

### Option B — Paste Your Own YANG
1. Paste any valid YANG model text into the left editor
2. Click **"▶ Parse YANG Model"**

### Option C — Upload a .yang File
1. Click **"📁 Upload .yang"**
2. Select any `.yang` file from your computer
3. The file is parsed automatically

### Exploring Results
- **Tree View tab**: Interactive expandable nodes with color-coding by type
  - Click any node to expand/collapse
  - Data types, constraints, keys shown inline
- **Hierarchy Text tab**: Plain-text tree (like a terminal output)
- **Stats bar**: Total Nodes · Containers · Leaves · Lists

---

## 7. REST API Reference

Base URL: `http://localhost:8080`

| Method | Endpoint | Description | Request |
|---|---|---|---|
| GET | `/api/yang/health` | Health check | — |
| GET | `/api/yang/sample` | Get built-in YANG text | — |
| POST | `/api/yang/parse` | Parse YANG from text | `{"content": "..."}` |
| POST | `/api/yang/upload` | Parse uploaded .yang file | multipart, field: `file` |

### Response Format (`/api/yang/parse`)

```json
{
  "moduleName": "network-device",
  "namespace": "urn:example:network-device",
  "prefix": "nd",
  "yangVersion": "1.1",
  "revision": "2024-01-15",
  "organization": "Example Corp",
  "description": "A sample YANG model for a network device",
  "totalNodes": 18,
  "containerCount": 5,
  "leafCount": 10,
  "listCount": 3,
  "hierarchyText": "module: network-device\n└── [CONTAINER] system\n    ├── ...",
  "nodes": [
    {
      "name": "system",
      "type": "container",
      "dataType": null,
      "description": "System configuration",
      "mandatory": null,
      "defaultValue": null,
      "range": null,
      "pattern": null,
      "key": null,
      "depth": 0,
      "children": [
        {
          "name": "hostname",
          "type": "leaf",
          "dataType": "string",
          "mandatory": "true",
          "pattern": "[a-zA-Z][a-zA-Z0-9\\-]*",
          "children": []
        }
      ]
    }
  ]
}
```

---

## 8. How the Parser Works

The parser (`YangModelParser.java`) processes YANG files in these stages:

```
Raw .yang text
      │
      ▼
1. Preprocess       — strip // comments and /* block comments */
      │
      ▼
2. Extract Metadata — regex patterns for: namespace, prefix, organization,
                      contact, description, yang-version, revision date
      │
      ▼
3. Parse Node Tree  — brace-balanced lexer:
                      reads keyword → reads name → extracts {...} body
                      recursively for all child nodes
      │
      ▼
4. Populate Nodes   — for each node body, extract:
                      type, mandatory, default, key, description,
                      min-elements, max-elements, range, pattern
      │
      ▼
5. Build Output     — hierarchyText (formatted string) + JSON nodes tree
                    — count stats: total, containers, leaves, lists
      │
      ▼
YangModule (returned as JSON)
```

**Node keywords recognized:** `container`, `list`, `leaf`, `leaf-list`, `choice`, `case`, `grouping`, `rpc`, `input`, `output`, `notification`, `anyxml`, `anydata`

**Skipped (metadata, not nodes):** `namespace`, `prefix`, `typedef`, `import`, `include`, `extension`, `feature`, `identity`, `deviation`

---

## 9. YANG Node Types & Constraints

### Node Types

| Icon | Type | Description |
|---|---|---|
| ⬡ | `container` | Groups related nodes together |
| ⊞ | `list` | Ordered collection with a key |
| ◈ | `leaf` | Single typed data value |
| ◉ | `leaf-list` | Multiple values of the same type |
| ◇ | `choice` | Mutually exclusive alternatives |
| ◆ | `case` | One branch of a choice |
| ◎ | `grouping` | Reusable group of nodes |

### Constraints Extracted

| Constraint | YANG Keyword | Example |
|---|---|---|
| Data type | `type` | `type string`, `type uint32` |
| Mandatory | `mandatory` | `mandatory true;` |
| Numeric range | `range` | `range "0..32";` |
| String pattern | `pattern` | `pattern "[a-z]+";` |
| Default value | `default` | `default "UTC";` |
| List key | `key` | `key "name";` |
| Min elements | `min-elements` | `min-elements 1;` |
| Max elements | `max-elements` | `max-elements 100;` |
| Description | `description` | `description "...";` |

---

## 10. Sample YANG Model (Built-in)

The built-in sample (`YangParserService.getSampleYang()`) models a network device:

```yang
module network-device {
  yang-version 1.1;
  namespace "urn:example:network-device";
  prefix "nd";

  container system {
    leaf hostname { type string; mandatory true; }
    leaf domain-name { type string; }
    container clock {
      leaf timezone { type string; default "UTC"; }
    }
  }

  container interfaces {
    list interface {
      key "name";
      leaf name { type string; mandatory true; }
      leaf enabled { type boolean; default "true"; }
      leaf mtu { type uint16 { range "68..65535"; } }
      container ipv4 {
        list address {
          key "ip";
          leaf ip { type string; mandatory true; }
          leaf prefix-length { type uint8 { range "0..32"; } mandatory true; }
        }
      }
    }
  }

  container routing {
    list static-route {
      key "destination";
      leaf destination { type string; mandatory true; }
      leaf next-hop { type string; mandatory true; }
      leaf metric { type uint32 { range "1..65535"; } default "1"; }
    }
  }
}
```

---

## 11. Testing with curl

```bash
# 1. Health check
curl http://localhost:8080/api/yang/health

# 2. Get the sample YANG source text
curl http://localhost:8080/api/yang/sample

# 3. Parse YANG content directly
curl -X POST http://localhost:8080/api/yang/parse \
  -H "Content-Type: application/json" \
  -d '{
    "content": "module test { yang-version 1.1; namespace \"urn:test\"; prefix t; leaf hostname { type string; mandatory true; } }"
  }'

# 4. Upload a .yang file
curl -X POST http://localhost:8080/api/yang/upload \
  -F "file=@/path/to/your/model.yang"
```

---

## 12. Common Issues & Fixes

### ❌ CORS error in browser console
**Cause:** Calling the backend directly without the Vite proxy.
**Fix:** Make sure `vite.config.js` has the proxy set up (already included). The `API_BASE` in `App.jsx` must be `/api/yang` (not `http://localhost:8080/api/yang`).

---

### ❌ Port 8080 already in use
**Fix:** Edit `backend/src/main/resources/application.properties`:
```properties
server.port=8081
```
Then also update `vite.config.js` target:
```js
target: 'http://localhost:8081',
```

---

### ❌ `mvn` command not found
**Fix:** Make sure Maven is installed and on your PATH. On macOS with Homebrew: `brew install maven`. On Ubuntu: `sudo apt install maven`.

---

### ❌ `npm` command not found
**Fix:** Install Node.js from https://nodejs.org/ (LTS version recommended).

---

### ❌ `moduleName` shows as `unknown-module`
**Cause:** The YANG content doesn't start with a `module <name> { ... }` block.
**Fix:** Ensure the root of the YANG file is `module your-module-name { ... }`.

---

### ❌ Nodes list is empty
**Cause:** The file uses custom/vendor-specific extensions.
**Fix:** The parser handles standard RFC 7950 keywords. Custom extensions (`augment`, vendor-specific) are skipped but don't cause errors.

---

### ❌ File upload gives error "Only .yang files are supported"
**Fix:** Rename your file to have a `.yang` extension before uploading.

---

## 13. Expected Output

After parsing the built-in sample, you will see:

```
module: network-device
├── [CONTAINER] system
│   ├── ℹ System configuration
│   ├── [LEAF] hostname : string *mandatory*
│   │       ℹ System hostname
│   │       pattern: [a-zA-Z][a-zA-Z0-9\-]*
│   ├── [LEAF] domain-name : string
│   └── [CONTAINER] clock
│       └── [LEAF] timezone : string
│               default: UTC
├── [CONTAINER] interfaces
│   └── [LIST] interface
│           key: name
│       ├── [LEAF] name : string *mandatory*
│       ├── [LEAF] enabled : boolean
│       ├── [LEAF] mtu : uint16
│       │       range: 68..65535
│       ├── [CONTAINER] ipv4
│       │   └── [LIST] address
│       │           key: ip
│       │       ├── [LEAF] ip : string *mandatory*
│       │       └── [LEAF] prefix-length : uint8
│       │               range: 0..32
│       └── [CONTAINER] ipv6
│           └── [LEAF] enabled : boolean
└── [CONTAINER] routing
    └── [LIST] static-route
            key: destination
        ├── [LEAF] destination : string *mandatory*
        ├── [LEAF] next-hop : string *mandatory*
        └── [LEAF] metric : uint32
                range: 1..65535
                default: 1
```

**Stats:** 18 total nodes · 5 containers · 10 leaves · 3 lists

---

## Architecture Diagram

```
Browser (React @ :5173)
         │
         │  /api/yang/*  (Vite proxy)
         ▼
Spring Boot (@:8080)
         │
  YangParserController
         │
  YangParserService
         │
  YangModelParser
    ├── preprocessContent()    strips comments
    ├── extractMetadata()      namespace, prefix, revision…
    ├── parseNodes()           brace-balanced lexer → tree
    ├── populateNodeFromBody() constraints per node
    └── countNodes()           stats
         │
  YangModule (JSON)
    ├── moduleName, namespace, prefix, revision
    ├── nodes[]  (recursive YangNode tree)
    ├── hierarchyText  (formatted string)
    └── totalNodes, containerCount, leafCount, listCount
```

---

*Built with Spring Boot 3.2 · React 18 · Vite 5 · RFC 7950 YANG 1.1*
