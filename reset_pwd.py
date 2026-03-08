import sqlite3
import bcrypt

password = b"admin@123"
hashed = bcrypt.hashpw(password, bcrypt.gensalt()).decode('utf-8')

conn = sqlite3.connect(r"C:\DialGenie\dialgenie.db")
c = conn.cursor()

c.execute("UPDATE users SET password_hash = ? WHERE email = 'admin@dialgenie.com'", (hashed,))
conn.commit()

print(f"Password hash updated to {hashed}")
conn.close()
