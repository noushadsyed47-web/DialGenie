import sqlite3
import os

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
DB_PATH = os.path.join(ROOT, 'dialgenie.db')
SCHEMA_FILE = os.path.join(ROOT, 'backend', 'shared', 'src', 'main', 'resources', 'schema.sql')

def main():
    print('DB path:', DB_PATH)
    if not os.path.exists(SCHEMA_FILE):
        print('schema.sql not found at', SCHEMA_FILE)
        return

    with open(SCHEMA_FILE, 'r', encoding='utf-8') as f:
        sql = f.read()

    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.executescript(sql)
    conn.commit()
    conn.close()
    print('Database created/updated at', DB_PATH)

if __name__ == '__main__':
    main()
