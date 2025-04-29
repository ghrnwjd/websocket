<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chat Room</title>
    <!-- Bootstrap 4.5 CDN -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
    <!-- Font Awesome for icons -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <style>
        .chat-container {
            max-width: 600px;
            margin: 40px auto;
        }
        .card {
            border-radius: 15px;
        }
        .card-header {
            border-top-left-radius: 15px;
            border-top-right-radius: 15px;
            background: #39c0ed;
            color: white;
        }
        .msg_history {
            height: 400px;
            overflow-y: auto;
            background: #f8f8f8;
            padding: 20px;
        }
        .incoming_msg, .outgoing_msg {
            margin-bottom: 20px;
            display: flex;
            align-items: flex-end;
        }
        .incoming_msg .received_msg {
            background: #ebebeb;
            border-radius: 10px;
            padding: 10px 15px;
            color: #646464;
            max-width: 70%;
        }
        .outgoing_msg {
            justify-content: flex-end;
        }
        .outgoing_msg .sent_msg {
            background: #05728f;
            border-radius: 10px;
            padding: 10px 15px;
            color: #fff;
            max-width: 70%;
        }
        .type_msg {
            border-top: 1px solid #c4c4c4;
            padding: 10px;
            background: #fff;
            border-bottom-left-radius: 15px;
            border-bottom-right-radius: 15px;
        }
        .input_msg_write input {
            border: none;
            width: 90%;
            padding: 10px;
            font-size: 16px;
        }
        .msg_send_btn {
            background: #05728f;
            color: #fff;
            border: none;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            font-size: 18px;
            margin-left: 10px;
        }
    </style>
</head>
<body>
<div class="chat-container">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <span><i class="fa fa-comments"></i> 채팅방: ${roomId}</span>
            <span id="status" class="badge badge-success">Online</span>
        </div>
        <div class="msg_history" id="messages"></div>
        <div class="type_msg d-flex align-items-center">
            <div class="input_msg_write flex-grow-1">
                <input type="text" id="messageInput" class="form-control" placeholder="메시지 입력">
            </div>
            <button class="msg_send_btn"><i class="fa fa-paper-plane"></i></button>
        </div>
    </div>
</div>

<script>
    let ws;
    function connect() {
        const roomId = "${roomId}";
        ws = new WebSocket(`ws://localhost:8080/chat/websocket?url=${roomId}`);

        ws.onopen = function() {
            document.getElementById('status').className = 'badge badge-success';
            document.getElementById('status').innerText = 'Online';
        };

        ws.onclose = function() {
            document.getElementById('status').className = 'badge badge-danger';
            document.getElementById('status').innerText = 'Offline';
        };

        ws.onmessage = function(event) {
            const messages = document.getElementById('messages');
            const msg = document.createElement('div');
            // 간단하게 본인 메시지와 타인 메시지 구분 (sessionId 활용 가능)
            if (event.data.startsWith("[${sessionId}]")) {
                msg.className = "outgoing_msg";
                msg.innerText = event.data;
            } else {
                msg.className = "incoming_msg";
                msg.innerText = event.data;
            }
            messages.appendChild(msg);
            messages.scrollTop = messages.scrollHeight;
        };
    }

    function sendMessage() {
        const input = document.getElementById('messageInput');
        const message = input.value.trim();
        if (message !== "") {
            ws.send(message);
            console.log("message: " + message);
            // 바로 내 화면에 추가
            const messages = document.getElementById('messages');
            const msgDiv = document.createElement('div');
            msgDiv.className = "outgoing_msg";
            msgDiv.innerText = message;
            messages.appendChild(msgDiv);
            messages.scrollTop = messages.scrollHeight;

            input.value = '';
        }
    }

    document.getElementById('messageInput').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });
    window.onload = connect;
</script>
</body>
</html>
