from docx import Document
from pathlib import Path

doc = Document(str(Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")))

for idx in [152, 153, 154, 155]:
    p = doc.paragraphs[idx]
    pf = p.paragraph_format
    fi = pf.first_line_indent.cm if pf.first_line_indent else None
    align = pf.alignment
    print(f"{idx} style={p.style.style_id} align={align} first={fi}")
    print(f"   {p.text[:100]}")
