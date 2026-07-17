import pymysql
conn = pymysql.connect(host='localhost', user='root', password='yongc20', database='huigrowth_dev')
cursor = conn.cursor()
cursor.execute("UPDATE users SET role='ADMIN' WHERE id=2")
conn.commit()
print('Updated role to ADMIN for user id=2')
cursor.execute('SELECT id, username, role FROM users')
rows = cursor.fetchall()
for r in rows:
    print(r)
cursor.close()
conn.close()
