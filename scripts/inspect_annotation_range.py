from copy import deepcopy

from docx import Document
from docx.oxml.ns import qn
from pathlib import Path

TEMPLATE = Path(r"c:\Users\Klim\Documents\Диплом образцы\Диплом1.docx")
doc = Document(str(TEMPLATE))

start = 102
for i in range(start, start + 30):
    p = doc.paragraphs[i]
    style = p.style.style_id if p.style else "?"
    has_num = p._p.pPr is not None and p._p.pPr.numPr is not None
    text = p.text[:90].encode("ascii", "replace").decode()
    print(f"{i:3} id={style} num={has_num} | {text}")
