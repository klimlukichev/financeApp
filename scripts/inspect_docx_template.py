import re
import zipfile
from pathlib import Path

path = Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")
with zipfile.ZipFile(path) as z:
    styles = z.read("word/styles.xml").decode("utf-8")
    doc = z.read("word/document.xml").decode("utf-8")

fonts = sorted(set(re.findall(r'w:ascii="([^"]+)"', styles)))
print("Fonts:", fonts)
for sid in ["Normal", "Heading1", "Heading2", "Heading3"]:
    m = re.search(rf'w:styleId="{sid}".*?</w:style>', styles, re.S)
    if m:
        chunk = m.group(0)[:600]
        print(f"\n--- {sid} ---")
        print(chunk)

text = re.sub(r"<w:tab/>", "\t", doc)
text = re.sub(r"</w:p>", "\n", text)
text = re.sub(r"<[^>]+>", "", text)
print("\n--- First paragraphs ---")
for line in text.splitlines()[:25]:
    line = line.strip()
    if line:
        print(line[:120])
