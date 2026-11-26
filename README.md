# Mini Database Engine

A lightweight database storage engine implemented from scratch in Java, focused on page management, record storage, and disk persistence.

---

## Overview

This project implements the foundational components of a database storage engine. It explores how databases organize records into fixed-size pages, manage available space, handle fragmentation, and persist data to disk.

The implementation focuses on low-level storage operations and binary data management without relying on an existing database engine.

## Features

- **Page Management:** Fixed-size 4 KB pages with structured metadata.
- **Slotted-Page Architecture:** Slot directory for managing record locations within pages.
- **Record Operations:** Insert, retrieve, and delete records using slot identifiers.
- **Space Management:** Free-space tracking and page compaction.
- **Disk Persistence:** Page-level read and write operations using `RandomAccessFile`.
- **Page Reconstruction:** Reconstruct page objects from persisted binary data.

## Architecture

### Page Layout

Each page occupies 4096 bytes and consists of a header, slot directory, free space, and record data.

```text
              4096 Bytes
┌────────────────────────────────┐
│           Page Header           │
├────────────────────────────────┤
│         Slot Directory          │
│                                │
│  Slot 0  │ Offset │ Length     │
│  Slot 1  │ Offset │ Length     │
│  Slot 2  │ Offset │ Length     │
│    ...                         │
├────────────────────────────────┤
│                                │
│           Free Space            │
│                                │
├────────────────────────────────┤
│          Record Data            │
│       (grows backwards)         │
└────────────────────────────────┘
```

The slot directory grows forward from the beginning of the page, while record data grows backward from the end. Each slot stores metadata used to locate its corresponding record.

### Storage Architecture

```text
          Application
               │
               ▼
          DiskManager
               │
        ┌──────┴──────┐
        ▼             ▼
    Read Page     Write Page
        │             │
        └──────┬──────┘
               ▼
              Page
               │
       ┌───────┼────────┐
       ▼       ▼        ▼
     Insert  Retrieve  Delete
```

## Tech Stack

| Technology | Purpose |
|---|---|
| Java | Core implementation |
| ByteBuffer | Binary page metadata manipulation |
| RandomAccessFile | Random-access disk operations |
| Java I/O | File creation and persistent storage |
