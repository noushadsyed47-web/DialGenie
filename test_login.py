import urllib.request, json, urllib.error
req = urllib.request.Request(
    'http://localhost:8080/api/v1/auth/login', 
    data=json.dumps({'email':'admin@dialgenie.com', 'password':'admin@123'}).encode('utf-8'), 
    headers={'Content-Type':'application/json'}
)
try:
    res=urllib.request.urlopen(req)
    print("Success:", res.read().decode())
except urllib.error.HTTPError as e:
    print(f"Error {e.code}:", e.read().decode())
except Exception as e:
    print("Other Error:", e)
