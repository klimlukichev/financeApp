from docx import Document
from pathlib import Path

TEMPLATE = Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")
doc = Document(str(TEMPLATE))

for idx in [102, 103, 105, 108]:
    p = doc.paragraphs[idx]
    print(f"\n=== {idx} ===")
    for i, r in enumerate(p.runs):
        bold = r.bold
        sz = r.font.size.pt if r.font.size else None
        print(f"  run{i}: bold={bold} sz={sz} | {r.text[:60]!r}")
