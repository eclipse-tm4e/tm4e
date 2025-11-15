# TM Partitioner (IDocumentPartitioner)

## Overview

- Partitioning ID: `tm4e.partitioning`
- Partition type naming: `tm4e:<root-scope>` derived from the active TextMate grammar's base root scope.
  - Examples: `tm4e:text.html`, `tm4e:source.js`, `tm4e:source.css`.
- Base partition type:
  - Before a grammar is known, the synthetic base type `tm4e:base` is used.
  - After activation with a grammar, the base becomes `tm4e:<root-scope>` (for example, HTML base `tm4e:text.html`).
- Installed automatically by `org.eclipse.tm4e.ui` for any document that resolves a TextMate grammar. It is added as a secondary partitioning and does not replace an editor's default partitioner.

## Partition Semantics

- `computePartitioning(offset, length)` returns regions that cover the entire requested range contiguously. Gaps between known embedded regions are filled with the current base type.
- `getPartition(offset)` clamps the offset to the valid range `[0, docLength-1]`. Negative offsets resolve to the first region; EOF resolves to the last region.
- Regions implement `ITMPartitionRegion` (extends `ITypedRegion`) and carry language metadata:
  - `getGrammarScope()` returns the effective grammar scope such as `source.js` or `text.html`.

## How to Consume from Code

Get partitions from the TM4E partitioner via `IDocumentExtension3`:
```java
var ext3 = (IDocumentExtension3) doc;
var partitioner = ext3.getDocumentPartitioner(org.eclipse.tm4e.ui.text.TMPartitions.TM_PARTITIONING /* or "tm4e.partitioning" */);
ITypedRegion[] regions = partitioner.computePartitioning(offset, length);
```

### Example: Content Assist / Feature Code

```java
import org.eclipse.tm4e.ui.text.ITMPartitionRegion;
import org.eclipse.tm4e.ui.text.ITMPartitioner;
import org.eclipse.tm4e.ui.text.TMPartitions;

// ...

IDocument doc = viewer.getDocument();
if (doc instanceof IDocumentExtension3 ext3) {
  if (ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING) instanceof ITMPartitioner tmPartitioner) {
    ITypedRegion r = tmPartitioner.getPartition(caretOffset);
    // Prefer matching by normalized language scope (handles embedded variants)
    switch(r.getGrammarScope()) {
      case "source.js":   // offer JavaScript proposals
      case "source.css":  // offer CSS proposals
    }
  }
}
```

## Matching by Scope (Recommended)

- Partition type strings are base-root only (for example, `tm4e:source.js`, `tm4e:text.html`). If you need to distinguish embedded variants (for example, JavaScript-in-Markdown), use `ITMPartitionRegion.getGrammarScope()` which carries the normalized full scope (such as `source.js`).
- For most feature switches, detect the language via the normalized scope name from `getGrammarScope()` as shown in the example. This is stable for families like `source.css`, `source.js`, or `text.html`.

## Mapping to Content Types (Alternative)

- If your feature keys off `IContentType`, translate the partition at the caret offset:

```java
IContentType[] cts = org.eclipse.tm4e.ui.text.TMPartitions.getContentTypesForOffset(doc, caretOffset);
```

## Generic Editor Contributions

- Register your feature with the Generic Editor as usual (e.g., content assist, hovers). Inside your implementation, use the code above to query TM4E’s partitioning and branch behavior based on the normalized scope (recommended) or the partition type.
- You do not need to install a partitioner yourself; `org.eclipse.tm4e.ui` installs it automatically when a grammar exists.

Example plugin.xml (content assist):

```xml
<extension point="org.eclipse.ui.genericeditor.contentAssistProcessors">
  <contentAssistProcessor
      class="com.example.MyTM4EAwareAssist"
      contentType="org.eclipse.core.runtime.text"
      targetId="org.eclipse.ui.genericeditor.GenericEditor"/>
  <!-- partitionType here refers to the editor's default partitioning;
       to use TM4E partitions, query tm4e.partitioning from your code. -->
  </extension>
```

## Utilities

- Check if a document has a TM4E partitioner installed:

```java
boolean installed = org.eclipse.tm4e.ui.text.TMPartitions.hasPartitioning(doc);
```

- Convenience to fetch the TM4E partition at an offset (null if none):

```java
ITypedRegion part = org.eclipse.tm4e.ui.text.TMPartitions.getPartition(doc, caretOffset);
```
