*I think that its not a good variant for conduct tech interview. But companies continue use this way for finding candidate. In my opinion the best variant is giving part of code where candidate can find bugs, can suggest ways for refactor and suggest better solutions. This variant economy time for candidate and employer.*

Next I present a test project which i got when found job on java developer 

Test project for JAVA EE Developer

This is a web application for authentication.

Client and server are connected by protocol webSocket(http://ru.wikipedia.org/wiki/WebSocket). The service MUST accept and return resources in JSON format.(http://ru.wikipedia.org/wiki/JSON). 

Application need to provide next tasks:
* user authentication
* on request LOGIN_CUSTOMER it needs check that user exists, find user with given password in DB and return message with token and token date expire or message with error
* keep user tokens
* keep history of user tokens
* regenerate token of user if user repeate authentication 

Protocol description 

Response message:
```
{ 
  “type”: “TYPE_OF_MESSAGE” , // string, type of message
  “sequence_id”: “09caaa73-b2b1-187e-2b24-683550a49b23”, // string, id of message connectivity
  “data” : {} // object, contain request data
}
```

Request message:
```
{ 
  “type”: “TYPE_OF_MESSAGE” , // string, type of message
  “sequence_id”: “09caaa73-b2b1-187e-2b24-683550a49b23”, // string, id of message connectivity
  “data” : {} // object, contain request data
}
```

If error message returns, object data contains next fields:
```
"error_description":"Customer not found”, // field with error description
"error_code":"customer.notFound” // field with error code
```

Request for successful authnntication operation:
```
{
  "type":"LOGIN_CUSTOMER”,
  "sequence_id":"a29e4fd0-581d-e06b-c837-4f5f4be7dd18”,
  "data”:{
    "email":"fpi@bk.ru”,
    "password":”123123"
  }
}
```

Return for successful authentication operation: 
```
{
  "type":"CUSTOMER_API_TOKEN”,
  "sequence_id":"cbf187c9-8679-0359-eb3d-c3211ee51a15”,
  "data”:{
    "api_token":"afdd312c-3d2a-45ee-aa61-468aba3397f3”,
    "api_token_expiration_date":"2015-07-15T11:14:30Z”
  }
}
```

Request for failed authnntication operation: 
```
{
  "type":"LOGIN_CUSTOMER",
  "sequence_id":"715c13b3-881a-9c97-b853-10be585a9747”,
  "data”:{
    "email":"123@gmail.com”,
    "password":”newPassword"
  }
}
```

Return for failed authentication operation: 
```
{
  "type":"CUSTOMER_ERROR”,
  "sequence_id":"715c13b3-881a-9c97-b853-10be585a9747”,
  "data”:{
    "error_description":"Customer not found”,
    "error_code":"customer.notFound”
  }
}
```

Available using any framework, maven and gradle

It can be used any application server but jboss better.

Its needed to organize:
* function of message connectivity, there is a field in protocol description;
* error handling;
* using database;
* if needed using config files;
* if needed using WebSocket sessions
* application needs to be ready for highload using by different users and websocket sessions.
