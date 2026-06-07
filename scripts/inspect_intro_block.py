from docx import Document
from pathlib import Path

doc = Document(str(Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")))

for i in range(130, 165):
    p = doc.paragraphs[i]
    t = p.text.strip()
    if not t:
        print(f"{i:4} [empty]")
        continue
    style = p.style.style_id if p.style else "?"
    text = t.encode("ascii", "replace").decode()
    print(f"{i:4} [{style}] {text}")
