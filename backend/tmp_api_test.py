import json
import urllib.request
import urllib.error

BASE = 'http://localhost:8083'

print('Registering')
req = urllib.request.Request(
    f'{BASE}/api/auth/register',
    data=json.dumps({
        'fullName': 'Test User',
        'email': 'testuser123@example.com',
        'password': 'Password123!'
    }).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
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
