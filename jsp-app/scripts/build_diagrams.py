"""Rebuild CDM and PDM diagrams with Graphviz (dot must be installed)."""
from pathlib import Path
import subprocess
import html

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'database'
OUT.mkdir(exist_ok=True)

def render(name, source):
    p=OUT/(name+'.dot');p.write_text(source)
    for fmt in ('svg','png','pdf'):
        subprocess.run(['dot','-T'+fmt,'-Gdpi=180',str(p),'-o',str(OUT/(name+'.'+fmt))],check=True)

header='''digraph G {
 graph [rankdir=LR, bgcolor="white", pad="0.2", nodesep="0.35", ranksep="0.65", splines=polyline];
 node [shape=plain, fontname="DejaVu Sans", fontsize=10];
 edge [color="#809589", arrowsize=0.65, fontname="DejaVu Sans", fontsize=8, fontcolor="#52695c"];
'''

def entity(key, title, fields):
    rows=''.join('<TR><TD ALIGN="LEFT" CELLPADDING="5">'+html.escape(f)+'</TD></TR>' for f in fields)
    return f'{key} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" COLOR="#d9e2d9"><TR><TD BGCOLOR="#254f3e" CELLPADDING="9"><FONT COLOR="white"><B>{title}</B></FONT></TD></TR>{rows}</TABLE>>];\n'

cdm=header
entities={
 'users':('PENGGUNA',['Nama, email, kata sandi']),
 'wallets':('DOMPET',['Nama, jenis, saldo awal']),
 'categories':('KATEGORI',['Nama, jenis, warna']),
 'transactions':('TRANSAKSI',['Judul, nominal, tanggal']),
 'budgets':('ANGGARAN',['Bulan, batas pengeluaran']),
 'goals':('TARGET TABUNGAN',['Target, dana terkumpul, tenggat']),
 'bills':('TAGIHAN',['Nominal, jatuh tempo, status']),
 'debts':('UTANG DAN PIUTANG',['Pihak, jenis, nominal, terbayar']),
 'notes':('CATATAN',['Judul, isi, label'])
}
for k,(title,fields) in entities.items():cdm+=entity(k,title,fields)
for k in entities:
    if k!='users':cdm+=f'users -> {k} [taillabel="1",headlabel="0..N",label="memiliki",labeldistance=1.4];\n'
cdm+='wallets -> transactions [taillabel="1",headlabel="0..N",label="mencatat"];\ncategories -> transactions [taillabel="1",headlabel="0..N",label="mengelompokkan"];\ncategories -> budgets [taillabel="1",headlabel="0..N",label="dibatasi per bulan"];\n}'
render('CDM_KasKu',cdm)

tables={
 'users':['PK id : BIGINT IDENTITY','name : VARCHAR(100) NOT NULL','UQ email : VARCHAR(160) NOT NULL','password_hash : VARCHAR(255) NOT NULL','is_demo : BOOLEAN NOT NULL DEFAULT FALSE','created_at : TIMESTAMP NOT NULL DEFAULT NOW'],
 'wallets':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','name : VARCHAR(80) NOT NULL','type : VARCHAR(20) NOT NULL','opening_balance : NUMERIC(15,2) NOT NULL','description : VARCHAR(255) NULL','UQ (id, user_id)','UQ (user_id, name)'],
 'categories':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','name : VARCHAR(80) NOT NULL','kind : VARCHAR(10) NOT NULL','color : VARCHAR(20) NOT NULL','UQ (id, user_id)','UQ (user_id, name)'],
 'transactions':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','FK wallet_id : BIGINT NOT NULL','FK category_id : BIGINT NOT NULL','title : VARCHAR(100) NOT NULL','amount : NUMERIC(15,2) NOT NULL','transaction_date : DATE NOT NULL','note : VARCHAR(1000) NULL'],
 'budgets':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','FK category_id : BIGINT NOT NULL','budget_month : VARCHAR(7) NOT NULL','amount_limit : NUMERIC(15,2) NOT NULL','UQ (user_id, category_id, budget_month)'],
 'goals':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','name : VARCHAR(100) NOT NULL','target_amount : NUMERIC(15,2) NOT NULL','saved_amount : NUMERIC(15,2) NOT NULL','deadline : DATE NOT NULL','icon : VARCHAR(20) NOT NULL'],
 'bills':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','name : VARCHAR(100) NOT NULL','amount : NUMERIC(15,2) NOT NULL','due_date : DATE NOT NULL','status : VARCHAR(10) NOT NULL','note : VARCHAR(1000) NULL'],
 'debts':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','person : VARCHAR(100) NOT NULL','direction : VARCHAR(10) NOT NULL','amount : NUMERIC(15,2) NOT NULL','paid_amount : NUMERIC(15,2) NOT NULL','due_date : DATE NOT NULL','note : VARCHAR(1000) NULL'],
 'notes':['PK id : BIGINT IDENTITY','FK user_id : BIGINT NOT NULL','title : VARCHAR(100) NOT NULL','content : VARCHAR(2000) NOT NULL','label : VARCHAR(20) NOT NULL','created_at : TIMESTAMP NOT NULL DEFAULT NOW']
}
core=header
for k in ('users','wallets','categories','transactions','budgets'):core+=entity(k,k,tables[k])
for k in ('wallets','categories','transactions','budgets'):core+=f'users -> {k} [taillabel="1",headlabel="0..N",color="#b2bdb2"];\n'
core+='wallets -> transactions [taillabel="1",headlabel="0..N",label="(wallet_id, user_id)",color="#295543",penwidth=1.5];\ncategories -> transactions [taillabel="1",headlabel="0..N",label="(category_id, user_id)",color="#295543",penwidth=1.5];\ncategories -> budgets [taillabel="1",headlabel="0..N",label="(category_id, user_id)",color="#295543",penwidth=1.5];\n}'
render('PDM_KasKu_Inti',core)

support=header.replace('rankdir=LR','rankdir=TB')
support+=entity('users','users',['PK id : BIGINT IDENTITY','Atribut lainnya pada PDM inti'])
for k in ('goals','bills','debts','notes'):support+=entity(k,k,tables[k])
for k in ('goals','bills','debts','notes'):support+=f'users -> {k} [taillabel="1",headlabel="0..N",label="user_id"];\n'
support+='}'
render('PDM_KasKu_Pendukung',support)

import json
(OUT/'data-dictionary.json').write_text(json.dumps(tables,indent=2))
print('Generated CDM and PDM in',OUT)
