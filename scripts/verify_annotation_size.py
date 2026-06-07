import zipfile
from pathlib import Path

from docx import Document

FILES = [
    Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx"),
    Path(r"c:\Users\Klim\Desktop\ВКР cursor — полный.docx"),
]


def stats(path: Path) -> None:
    doc = Document(str(path))
    xml = zipfile.ZipFile(path).read("word/document.xml").decode()
    page_breaks = xml.count('w:type="page"')

    ru: list[str] = []
    en: list[str] = []
    mode = None
    for para in doc.paragraphs:
        text = para.text.strip()
        if text == "АННОТАЦИЯ":
            mode = "ru"
            continue
        if text == "THE ANNOTATION":
            mode = "en"
            continue
        if not text:
            continue
        if mode == "ru":
            ru.append(text)
            if text.startswith("Область применения"):
                mode = None
        elif mode == "en":
            en.append(text)
            if text.startswith("Application area"):
                mode = None

    ru_text = " ".join(ru)
    en_text = " ".join(en)
    print(f"\n=== {path.name} ===")
    print(f"page breaks: {page_breaks}")
    print(f"RU: {len(ru_text.split())} words, {len(ru_text)} chars")
    print(f"EN: {len(en_text.split())} words, {len(en_text)} chars")
    print(f"total: {len((ru_text + ' ' + en_text).split())} words")


for file in FILES:
    if file.exists():
        stats(file)
