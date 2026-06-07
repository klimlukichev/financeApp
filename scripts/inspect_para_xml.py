import re
import zipfile
from pathlib import Path

TEMPLATE = Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")

with zipfile.ZipFile(TEMPLATE) as z:
    doc_xml = z.read("word/document.xml").decode("utf-8")

paras = doc_xml.split("<w:p ")
for i, p in enumerate(paras[1:103], start=1):
    pass

for idx in [102, 103, 104, 107, 108, 116]:
    # find nth paragraph
    parts = re.findall(r"<w:p[ >].*?</w:p>", doc_xml, re.S)
    if idx < len(parts):
        xml = parts[idx]
        text = re.sub(r"<[^>]+>", "", xml).strip()[:80]
        ppr = re.search(r"<w:pPr>.*?</w:pPr>", xml, re.S)
        print(f"\n=== para {idx}: {text} ===")
        print(ppr.group(0) if ppr else "no pPr")
