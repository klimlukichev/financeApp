from docx import Document
from pathlib import Path

doc = Document(str(Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")))

keywords = [
    "актуальн",
    "обоснован",
    "постановк",
    "задач",
    "введен",
    "цель",
    "объект",
    "предмет",
]

for i, p in enumerate(doc.paragraphs):
    t = p.text.strip().lower()
    if not t:
        continue
    if any(k in t for k in keywords):
        style = p.style.style_id if p.style else "?"
        print(f"{i:4} [{style:4}] {p.text[:110]}")
