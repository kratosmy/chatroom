from flask import Flask, g, jsonify, request
from MyDatabase import MyDatabase
import requests
import json

app = Flask(__name__)


@app.before_request
def before_request():
    g.mydb = MyDatabase()
    return


@app.teardown_request
def teardown_request(exception):
    mydb = getattr(g, "mydb", None)
    if mydb is not None:
        mydb.conn.close()
    return


@app.route("/api/a3/get_chatrooms")
def query_chatroom():
    user_id = request.args.get('user_id', None)
    if user_id:
        unique_user_id = int(user_id) % 10000
        query = 'SELECT * FROM chatrooms WHERE id<1000000 OR (id MOD 10000) = %s OR FLOOR(id / 10000) = %s' % (unique_user_id, unique_user_id)
    else:
        query = 'SELECT * FROM chatrooms'
    g.mydb.cursor.execute(query)
    chatrooms = g.mydb.cursor.fetchall()
    return jsonify(status='OK', data=chatrooms)


@app.route('/api/a3/get_messages')
def query_message():
    chatroom_id = request.args.get('chatroom_id', None)
    page = request.args.get('page', None)
    if chatroom_id and page:
        limit = 5
        offset = (int(page) - 1) * limit
        # get page count
        query = 'SELECT COUNT(*) AS "COUNT" FROM messages WHERE chatroom_id = %s' % (chatroom_id)
        #query = f'SELECT COUNT(*) AS "COUNT" FROM messages WHERE chatroom_id = {chatroom_id}'
        #query = 'SELECT * FROM chatrooms'
        g.mydb.cursor.execute(query)
        num = g.mydb.cursor.fetchall()
        print(num)
        page_count = int(int(num[0]['COUNT']) / limit) + 1
        if page_count < int(page):
            return jsonify(status='ERROR')
        # get messages
        query = "SELECT chatroom_id, message, name, user_id, DATE_FORMAT(message_time,'%%Y-%%m-%%d %%H:%%i:%%s') AS message_time FROM messages WHERE chatroom_id=%s ORDER BY id DESC LIMIT %d OFFSET %d" % (chatroom_id, limit, offset)
        g.mydb.cursor.execute(query)
        messages = g.mydb.cursor.fetchall()
        print(messages)
        # structure result
        data = {'current_page': page, 'messages': messages, 'total_pages': page_count}
        return jsonify(status='OK', data=data)
    return jsonify(status='ERROR', message="Something Wrong With Your Parameters")

@app.route("/api/a3/send_message", methods=['POST'])
def send_message():
    chatroom_id = request.form['chatroom_id']
    user_id = request.form['user_id']
    name = request.form['name']
    message = request.form['message']
    print("sending message..........")
    print(chatroom_id)
    print(user_id)
    print(name)
    print(message)
    if chatroom_id and user_id and name and message:
        query = "INSERT INTO messages (chatroom_id, user_id, name, message) VALUES (\"%s\", \"%s\", \"%s\", \"%s\");" % (chatroom_id, user_id, name, message)
        print(query)
        try:
            g.mydb.cursor.execute(query)
            g.mydb.conn.commit()
            get_insert_row = "SELECT * FROM messages WHERE id = %d" % (g.mydb.cursor.lastrowid)
            g.mydb.cursor.execute(get_insert_row)
            inserted_row = g.mydb.cursor.fetchone()
            url = 'http://0.0.0.0:8001/api/a4/broadcast_room'
            print(inserted_row)
            print(inserted_row["message_time"])
            payload = {'chatroom_id': chatroom_id, 'user_id': user_id, 'name': name, 'message': message,
                       'message_time': inserted_row["message_time"]}
            r = requests.post(url, data=payload)
            print(r.content)
        except Exception as e:
            print(e)
            print('db problem')
            g.mydb.conn.rollback()
            return jsonify(status='ERROR', message="Fail To Send Message")
    else:
        return jsonify(status='ERROR', message="Something Wrong With Your Input")
    return jsonify(status='OK')


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)





