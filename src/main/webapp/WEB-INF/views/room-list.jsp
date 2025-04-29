<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Chat Rooms</title>
</head>
<body>
    <h1>채팅방 목록</h1>
    <form action="/chat" method="get">
        <input type="text" name="roomId" placeholder="방 이름 입력">
        <button type="submit">입장</button>
    </form>
</body>
</html>
