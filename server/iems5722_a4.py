from flask import Flask, request, jsonify, request
from flask_socketio import SocketIO, emit, join_room, leave_room, send

app = Flask(__name__)
app.config['SECRET_KEY'] = 'iems5722'
socketio = SocketIO(app, cors_allowed_origins='*')


@app.route('/api/a4')
def helloworld():
    return "assign4"


@app.route("/api/a4/broadcast_room", methods=["POST"])
def broadcast_room():
    print('get post request')
    print(request)
    print(request.form)
    chatroom_id = request.form['chatroom_id']
    user_id = request.form['user_id']
    name = request.form['name']
    message = request.form['message']
    if chatroom_id and user_id and name and message:
        # data = {'chatroom_id': chatroom_id, 'user_id': user_id, 'name': name, 'message': message}
        data = request.form
        response = {'status': 'OK', 'data': data}
        print(response)
        socketio.emit('my response', response, broadcast=True, room=chatroom_id)
        print('successfully broadcast')
        return jsonify(status='OK')
    else:
        # response = {'status':'ERROR', 'message':'Something is wrong with your values'}
        # socketio.emit('my response', response ,  broadcast = True, room=chatroom_id)
        return jsonify(status='ERROR', message="Something wrong with your input")
    # socketio.emit(jsonify(status='OK', data=data), json, broadcast = True)


@socketio.on('message')
def handleMessage(msg):
    print("Message:" + msg)
    emit('my response', msg, broadcast=True)


@socketio.on('my event')
def my_event_handler(data):
    print('my event!')


@socketio.on('join')
def on_join(chatroom_id):
    # username = data['username']
    # chatroom_id = data['chatroom_id']
    join_room(chatroom_id)
    print('join room!')


@socketio.on('leave')
def on_leave(chatroom_id):
    # username = data['username']
    # chatroom_id = data['chatroom_id']
    leave_room(chatroom_id)
    print('leave room!')


@socketio.on('connect')
def test_connect():
    print('connected')
    # emit('my response', {'data': 'Connected'})


@socketio.on('disconnect')
def test_disconnect():
    print('Client disconnected')


if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=8001)

