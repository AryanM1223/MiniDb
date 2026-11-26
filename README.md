# Mini Database Engine

<p align="center">
  

  Exploring how databases store, organize, retrieve, and persist data at the lowest level — one page at a time.
</p>

---

## Overview

**Mini Database Engine** is a hands-on implementation of the storage layer of a database, written in Java.

Instead of relying on an existing database engine, this project explores the fundamentals behind one: fixed-size pages, slotted-page architecture, record management, memory layout, and disk-based persistence.

The goal is simple: understand what happens underneath the abstractions we use every day when working with databases.

## Features

- **Fixed-Size Pages** — 4 KB pages backed by byte arrays.
- **Slotted-Page Architecture** — A slot directory keeps track of records stored inside each page.
- **Record Operations** — Insert, retrieve, and delete records using slot IDs.
- **Space Management** — Track free space and compact pages to reclaim fragmentation.
- **Disk Persistence** — Read and write pages directly to a database file using Java's `RandomAccessFile`.
- **Page Reconstruction** — Rebuild page objects from raw bytes loaded from disk.

## How It Works

The engine uses a page-oriented storage model. Each page is a fixed-size block of 4096 bytes containing metadata, a slot directory, and record data.

### Page Layout

```text
       4096 Bytes
┌──────────────────────────────┐
│         Page Header          │
├──────────────────────────────┤
│        Slot Directory        │
│                              │
│  Slot 0 → Record Metadata    │
│  Slot 1 → Record Metadata    │
│  Slot 2 → Record Metadata    │
│             ...              │
├──────────────────────────────┤
│                              │
│          Free Space          │
│                              │
├──────────────────────────────┤
│         Record Data          │
│         grows backward       │
└──────────────────────────────┘
```

The slot directory grows forward from the beginning of the page, while records are stored from the end backwards. This allows records to be located through stable slot IDs without requiring the slot directory to store the records themselves.

### Storage Architecture

```text
          Application
               │
               ▼
          DiskManager
               │
       ┌───────┴───────┐
       ▼               ▼
   Read Page       Write Page
       │               │
       └───────┬───────┘
               ▼
             Page
               │
       ┌───────┼────────┐
       ▼       ▼        ▼
     Insert  Retrieve  Delete
               │
               ▼
          Page Bytes
          (4096 bytes)
```

## Tech Stack

| Technology | Purpose |
|---|---|
| Java | Core implementation |
| ByteBuffer | Reading and writing binary page metadata |
| RandomAccessFile | Direct file-based storage |
| File I/O | Persistent page management |
