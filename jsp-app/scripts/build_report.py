"""Build the PDF and editable Markdown project report (reportlab and Pillow)."""
from pathlib import Path
from html import escape
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.lib.styles import ParagraphStyle
from PIL import Image
import json

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'
OUT = DOCS / 'Laporan_KasKu_5025231296.pdf'
FONT = Path('/usr/share/fonts/truetype/dejavu')
pdfmetrics.registerFont(TTFont('Body', str(FONT/'DejaVuSans.ttf')))
pdfmetrics.registerFont(TTFont('Bold', str(FONT/'DejaVuSans-Bold.ttf')))
pdfmetrics.registerFont(TTFont('Mono', str(FONT/'DejaVuSansMono.ttf')))
pdfmetrics.registerFontFamily('Body', normal='Body', bold='Bold', italic='Body', boldItalic='Bold')
INK=colors.HexColor('#22382f'); GREEN=colors.HexColor('#295543'); MUTED=colors.HexColor('#6f7c71'); LINE=colors.HexColor('#dfe5db'); LIGHT=colors.HexColor('#f3f6ef')
BODY=ParagraphStyle('Body',fontName='Body',fontSize=10,leading=15,textColor=INK,spaceAfter=10)
SMALL=ParagraphStyle('Small',parent=BODY,fontSize=8.1,leading=12,textColor=MUTED)
CELL=ParagraphStyle('Cell',parent=BODY,fontSize=8.3,leading=12)
HEAD=ParagraphStyle('Head',parent=CELL,fontName='Bold',textColor=colors.white)
MD=[]

class Report:
    def __init__(self):
        self.c=canvas.Canvas(str(OUT),pagesize=A4)
        self.c.setTitle('KasKu JSP Project Report')
        self.c.setAuthor('Muhammad Elmiro Rayhan; Ahmad Faiz Tajuddin')
        self.n=0
    def page(self,title,subtitle='',wide=False):
        if self.n: self.finish()
        self.n+=1;self.w,self.h=landscape(A4) if wide else A4;self.c.setPageSize((self.w,self.h));self.left=43;self.width=self.w-86;self.y=self.h-45
        self.c.setFont('Bold',8);self.c.setFillColor(GREEN);self.c.drawString(self.left,self.y,'KASKU  /  JSP PROJECT REPORT')
        self.y-=31
        assert pdfmetrics.stringWidth(title, 'Bold', 21) <= self.width, title
        self.c.setFillColor(colors.black);self.c.setFont('Bold',21);self.c.drawString(self.left,self.y,title);self.y-=23
        MD.append('\n## '+title+'\n')
        if subtitle:self.p(subtitle,small=True)
    def p(self,text,small=False):
        p=Paragraph(text, SMALL if small else BODY);_,height=p.wrap(self.width,1000)
        if self.y-height<50:raise RuntimeError(f'Page {self.n} overflows at {text[:50]}')
        p.drawOn(self.c,self.left,self.y-height);self.y-=height+(8 if small else 12)
        MD.append(text.replace('<b>','**').replace('</b>','**').replace('<br/>','\n')+'\n')
    def heading(self,text):
        self.y-=7;self.c.setFont('Bold',11);self.c.setFillColor(GREEN);self.c.drawString(self.left,self.y,text);self.y-=17;MD.append('\n### '+text+'\n')
    def table(self,headers,rows,widths=None):
        data=[[Paragraph(escape(str(v)),HEAD) for v in headers]]+[[Paragraph(escape(str(v)),CELL) for v in row] for row in rows]
        t=Table(data,colWidths=widths or [self.width/len(headers)]*len(headers),hAlign='LEFT')
        t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),GREEN),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,LIGHT]),('LINEBELOW',(0,0),(-1,-1),.4,LINE),('LEFTPADDING',(0,0),(-1,-1),9),('RIGHTPADDING',(0,0),(-1,-1),9),('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),8),('VALIGN',(0,0),(-1,-1),'TOP')]))
        _,height=t.wrap(self.width,1000)
        if self.y-height<50:raise RuntimeError(f'Table overflow page {self.n}')
        t.drawOn(self.c,self.left,self.y-height);self.y-=height+16
        MD.append('| '+' | '.join(headers)+' |\n| '+' | '.join(['---']*len(headers))+' |\n'+'\n'.join('| '+' | '.join(str(v).replace('\n',' ') for v in row)+' |' for row in rows)+'\n')
    def image(self,path,caption,max_height=380):
        path=Path(path);im=Image.open(path);iw,ih=im.size;scale=min(self.width/iw,max_height/ih);w,h=iw*scale,ih*scale;x=self.left+(self.width-w)/2
        if self.y-h<70:raise RuntimeError(f'Image overflow page {self.n}')
        self.c.setStrokeColor(LINE);self.c.setLineWidth(.5);self.c.rect(x-1,self.y-h-1,w+2,h+2,stroke=1,fill=0)
        self.c.drawImage(str(path),x,self.y-h,width=w,height=h);self.y-=h+10;self.p(caption,small=True)
        rel=path.relative_to(DOCS) if path.is_relative_to(DOCS) else Path('..')/path.relative_to(ROOT)
        MD.append(f'![{caption}]({rel.as_posix()})\n')
    def finish(self):
        c=self.c;c.setStrokeColor(LINE);c.setLineWidth(.5);c.line(43,38,self.w-43,38);c.setFont('Body',7);c.setFillColor(MUTED)
        c.drawString(43,25,'Muhammad Elmiro Rayhan (5025231296)  |  Ahmad Faiz Tajuddin (5025231291)');c.drawRightString(self.w-43,25,f'{self.n:02d}');c.showPage()
    def save(self):
        self.finish();self.c.save();(DOCS/'LAPORAN.md').write_text('\n'.join(MD),encoding='utf-8')

from report_english import write_report
r=Report()
write_report(r, ROOT, DOCS)
r.save()
print(json.dumps({'pdf': str(OUT), 'pages': r.n}))
