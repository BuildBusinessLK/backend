import json
import urllib.request
import urllib.error

BASE = 'http://localhost:8083'
TOKEN = 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0ZXN0dXNlcjEyM0BleGFtcGxlLmNvbSIsInVpZCI6Mywicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODE3NjQ2NTUsImV4cCI6MTc4MjM2OTQ1NX0.7NM8NJsAP4HEWxC6RStVu51lRUxr9W4ZeJQ6P_d6nBpbBtxtW6hsm_mo6piTee1u'

payload = {
    'businessName': 'Test Business',
    'sector': 'COCONUT',
    'websiteSlug': 'test-business',
    'businessDescription': 'A test business',
    'targetMarket': 'Local',
    'monthlyIncome': 100000.00,
    'monthlyProduction': 5000.00,
    'marketingGoals': 'Increase sales',
    'products': [
        {
            'name': 'Product A',
            'description': 'Test product',
            'price': 1000.00,
            'category': 'Test',
            'imageUrl': 'https://example.com/image.png'
        }
    ],
    'socialLinks': [
        {
            'platform': 'Facebook',
            'url': 'https://facebook.com/test'
        }
    ]
}

req = urllib.request.Request(
    f'{BASE}/api/businesses',
    data=json.dumps(payload).encode('utf-8'),
    headers={
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {TOKEN}'
    },
    method='POST'
)
try:
    with urllib.request.urlopen(req) as r:
        print('STATUS', r.status)
        print(r.read().decode())
except urllib.error.HTTPError as e:
    print('HTTP', e.code)
    print(e.read().decode())
except Exception as ex:
    print('ERR', ex)
