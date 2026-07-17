import pymysql
conn = pymysql.connect(host='localhost', user='root', password='yongc20', database='huigrowth_dev')
cursor = conn.cursor()
cursor.execute('SELECT id, name, birthday FROM babies LIMIT 10')
rows = cursor.fetchall()
for r in rows:
    print(r)
if not rows:
    print('No babies found')
cursor.close()
conn.close()
