#!/usr/bin/env python3
"""HTTP integration checks against a running KasKu instance. Uses disposable test accounts."""
import http.cookiejar
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = (sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080').rstrip('/')
checks = []

class Client:
    def __init__(self):
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()), urllib.request.ProxyHandler({}))
        self.csrf = ''
    def request(self, path, values=None, csrf=True):
        if values is not None:
            values = dict(values)
            if csrf:
                values['csrf'] = self.csrf
            data = urllib.parse.urlencode(values).encode()
        else:
            data = None
        try:
            response = self.opener.open(BASE + path, data, timeout=30)
        except urllib.error.HTTPError as e:
            response = e
        body = response.read().decode('utf-8-sig')
        match = re.search(r'name="csrf" value="([^"]+)"', body)
        if match:
            self.csrf = match.group(1)
        return response.code, body, response.geturl()

def check(name, condition):
    checks.append({'name': name, 'passed': bool(condition)})
    if not condition:
        raise AssertionError(name)

def create(c, key, fields, title):
    status, body, _ = c.request('/app/' + key + '/save', fields)
    check(key + ' CREATE', status == 200 and 'Data baru berhasil ditambahkan.' in body)
    match = re.search(r'/app/' + key + r'/detail\?id=(\d+)', body)
    check(key + ' generated id', match is not None)
    return match.group(1)

def main():
    a, b = Client(), Client()
    check('health', a.request('/health')[0] == 200)
    check('auth protected', a.request('/app/dashboard')[2].endswith('/auth/login'))
    suffix = uuid.uuid4().hex[:12]
    for i, client in enumerate((a, b)):
        client.request('/auth/register')
        status, body, _ = client.request('/auth/register', {'name': 'QA User ' + str(i), 'email': f'qa-{suffix}-{i}@example.test', 'password': 'Test-only-password-2026'})
        check(f'register {i}', status == 200 and 'Halo, QA' in body)
    check('CSRF missing rejected', a.request('/app/notes/save', {'title': 'No CSRF'}, csrf=False)[0] == 403)
    wallet = create(a, 'wallets', {'name': 'QA Wallet', 'type': 'cash', 'opening_balance': '100000', 'description': 'Temporary test'}, 'QA Wallet')
    category = create(a, 'categories', {'name': 'QA Category', 'kind': 'expense', 'color': 'orange'}, 'QA Category')
    fixtures = {
        'transactions': {'title': 'QA Expense', 'wallet_id': wallet, 'category_id': category, 'amount': '25000', 'transaction_date': '2026-11-01', 'note': 'Temporary transaction'},
        'budgets': {'category_id': category, 'budget_month': '2026-11', 'amount_limit': '100000'},
        'goals': {'name': 'QA Goal', 'target_amount': '500000', 'saved_amount': '100000', 'deadline': '2027-01-01', 'icon': 'target'},
        'bills': {'name': 'QA Bill', 'amount': '50000', 'due_date': '2026-12-01', 'status': 'unpaid', 'note': 'QA'},
        'debts': {'person': 'QA Person', 'direction': 'payable', 'amount': '100000', 'paid_amount': '20000', 'due_date': '2026-12-01', 'note': 'QA'},
        'notes': {'title': 'QA Note', 'content': '<script>alert(1)</script> & ordinary text', 'label': 'planning'},
    }
    ids = {'wallets': wallet, 'categories': category}
    for key, fields in fixtures.items():
        ids[key] = create(a, key, fields, '')
    for key, rid in ids.items():
        check(key + ' READ detail', a.request(f'/app/{key}/detail?id={rid}')[0] == 200)
        check(key + ' EDIT form', a.request(f'/app/{key}/edit?id={rid}')[0] == 200)
        check(key + ' NEW form', a.request(f'/app/{key}/new')[0] == 200)
        check(key + ' owner isolation', b.request(f'/app/{key}/detail?id={rid}')[0] == 404)
    fields = dict(fixtures['transactions']); fields['wallet_id'] = '999999'
    check('foreign wallet rejected', a.request('/app/transactions/save', fields)[0] == 422)
    fields = dict(fixtures['transactions']); fields['amount'] = '-5'
    check('negative amount rejected', a.request('/app/transactions/save', fields)[0] == 422)
    check('unique budget rejected', a.request('/app/budgets/save', fixtures['budgets'])[0] == 422)
    body = a.request('/app/notes/detail?id=' + ids['notes'])[1]
    check('HTML is escaped', '&lt;script&gt;alert(1)&lt;/script&gt;' in body and '<script>alert(1)</script>' not in body)
    status, body, _ = a.request('/app/wallets/delete', {'id': wallet})
    check('used wallet deletion blocked', status == 200 and 'Data masih digunakan' in body)
    fixtures['wallets'] = {'name': 'QA Wallet Updated', 'type': 'bank', 'opening_balance': '200000', 'description': 'Updated'}
    fixtures['categories'] = {'name': 'QA Category Updated', 'kind': 'expense', 'color': 'green'}
    for key, fields in fixtures.items():
        changed = dict(fields)
        if key == 'transactions': changed['amount'] = '30000'
        if key == 'budgets': changed['amount_limit'] = '120000'
        if key == 'goals': changed['saved_amount'] = '200000'
        if key == 'bills': changed['status'] = 'paid'
        if key == 'debts': changed['paid_amount'] = '50000'
        if key == 'notes': changed['title'] = 'QA Edited Note'
        changed['id'] = ids[key]
        status, body, _ = a.request('/app/' + key + '/save', changed)
        check(key + ' UPDATE', status == 200 and 'Perubahan berhasil disimpan.' in body)
    check('updated balance', 'Rp 170.000' in a.request('/app/wallets')[1])
    check('search returns record', 'QA Edited Note' in a.request('/app/notes?q=Edited')[1])
    check('search empty state', 'Tidak ada data yang cocok' in a.request('/app/notes?q=nonexistent-xyz')[1])
    check('reports render', a.request('/app/reports?from=2026-11-01&to=2026-11-30')[0] == 200)
    check('CSV export', 'QA Expense' in a.request('/app/export?from=2026-11-01&to=2026-11-30')[1])
    for key in ('transactions', 'budgets', 'goals', 'bills', 'debts', 'notes', 'wallets', 'categories'):
        status, body, _ = a.request('/app/' + key + '/delete', {'id': ids[key]})
        check(key + ' DELETE', status == 200 and 'Data berhasil dihapus.' in body)
        check(key + ' deleted record missing', a.request(f'/app/{key}/detail?id={ids[key]}')[0] == 404)
    check('logout', a.request('/auth/logout', {})[2].endswith('/auth/login'))
    status, body, _ = a.request('/auth/login', {'email': f'qa-{suffix}-0@example.test', 'password': 'Test-only-password-2026'})
    check('login with persisted account', status == 200 and 'Halo, QA' in body)
    check('unknown module 404', a.request('/app/not-a-module')[0] == 404)
    print(json.dumps({'passed': len(checks), 'failed': 0, 'checks': checks}, indent=2))

if __name__ == '__main__':
    main()
