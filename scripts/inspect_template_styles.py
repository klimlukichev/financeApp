import re
import zipfile
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH

TEMPLATE = Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")


def main() -> None:
    with zipfile.ZipFile(TEMPLATE) as z:
        styles_xml = z.read("word/styles.xml").decode("utf-8")

    for sid in ["af0", "af1", "af4", "af7", "a7", "a"]:
        m = re.search(rf'<w:style[^>]*w:styleId="{sid}".*?</w:style>', styles_xml, re.S)
        if not m:
            continue
        chunk = m.group(0)
        name_m = re.search(r'w:name w:val="([^"]+)"', chunk)
        print(f"=== {sid}: {name_m.group(1) if name_m else '?'} ===")
        for part in re.findall(r"<w:pPr>.*?</w:pPr>", chunk, re.S):
            print(part[:500])
        for part in re.findall(r"<w:rPr>.*?</w:rPr>", chunk, re.S):
            print(part[:300])
        print()

    doc = Document(str(TEMPLATE))
    style_map = {}
    for s in doc.styles:
        if s.type is not None:
            style_map[s.style_id] = s.name

    print("Style IDs:", {k: v for k, v in style_map.items() if k.startswith("af")})

    # resolve annotation paragraph style id from XML
    with zipfile.ZipFile(TEMPLATE) as z:
        doc_xml = z.read("word/document.xml").decode("utf-8")
    paras = doc_xml.split("</w:p>")
    for idx in [102, 103, 108]:
        if idx < len(paras):
            p = paras[idx]
            sid = re.search(r'w:val="([^"]+)"', re.search(r"w:pStyle", p) and p or "")
            style_ref = re.search(r'<w:pStyle w:val="([^"]+)"/>', p)
            print(f"para {idx} styleId={style_ref.group(1) if style_ref else None}")


if __name__ == "__main__":
    main()
