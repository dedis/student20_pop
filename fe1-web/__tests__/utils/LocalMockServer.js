const webSocketsServerPort = 9000;
const WebSocketServer = require('websocket').server;
const http = require('http');

// https://blog.logrocket.com/websockets-tutorial-how-to-go-real-time-with-node-and-react-8e4693fbf843/
// Spinning the http server and the websocket server.

/* ------------------------------------------
 *        --- DO NOT REVIEW IN PR ----
 * this file is temporary (testing purposes)
 * ------------------------------------------
 */

const server = http.createServer();
server.listen(webSocketsServerPort);
console.log(`listening on port ${webSocketsServerPort} :)\n`);

const wsServer = new WebSocketServer({
  httpServer: server,
});

const clients = {};
const laoMessageIds = []; // Note: only handles single lao scenarios

// This code generates unique user id for every user.
const getUniqueID = () => {
  const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  return `${s4() + s4()}-${s4()}`;
};

wsServer.on('request', (request) => {
  const userID = getUniqueID();
  console.log(`\n ==> ${new Date()} Received a new connection from origin ${request.origin}. :)\n`);

  // You can rewrite this part of the code to accept only the requests from allowed origin
  const connection = request.accept(null, request.origin);
  clients[userID] = connection;
  console.log(`connected: ${userID} in ${Object.getOwnPropertyNames(clients)}`);

  connection.on('message', (message) => {
    if (message.type === 'utf8') {
      console.log('Received Message :) :\n', message.utf8Data, '\n-----------------------------\n');

      let answers;
      const JSON_RPC_VERSION = '2.0';
      if (JSON.parse(message.utf8Data).params.message !== undefined) {
        laoMessageIds.push(JSON.parse(message.utf8Data).params.message);
      }

      const generalAnswerPositive = {
        jsonrpc: JSON_RPC_VERSION,
        result: 0,
        id: JSON.parse(message.utf8Data).id,
      };

      const generalAnswerNegative = {
        jsonrpc: JSON_RPC_VERSION,
        error: {
          code: -Math.round(Math.random() * 4 + 1),
          description: 'dummy error from dummy server',
        },
        id: JSON.parse(message.utf8Data).id,
      };

      const generalCatchupAnswerPositive = {
        jsonrpc: JSON_RPC_VERSION,
        result: laoMessageIds,
        id: JSON.parse(message.utf8Data).id,
      };

      answers = [{
        success: 'true',
        error: 'null',
      }, {
        success: 'false',
        error: '[test] I could not accept your request :( (from server LocalMockServer.js)',
      }];

      // const answers = [{type: "answer", msg: JSON.parse(message.utf8Data)}];
      // const answers = [{type: "answer", msg: message.utf8Data}];
      // answers = [JSON.parse(message.utf8Data)];

      if (JSON.parse(message.utf8Data).method === 'catchup') {
        answers = [generalCatchupAnswerPositive];
      } else {
        answers = [generalAnswerPositive, generalAnswerNegative];
        answers = [generalAnswerPositive];
      }

      // const idx = 0;
      // const idx = 1;

      // choose a random index of the array answers
      const idx = (Math.floor(Math.random() * (1000000))) % answers.length;

      // broadcasting message to all connected clients
      Object.keys(clients).forEach((key) => {
        clients[key].sendUTF(JSON.stringify(answers[idx]));
        // console.log('sent Message to: ', clients[key]);
      });
    }
  });
});
