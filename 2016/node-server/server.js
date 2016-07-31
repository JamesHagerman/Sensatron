//
// Sensatron NodeJS Static file host + RESTful API Server
// Date: July 29th, 2016
//
// This server hosts all of the content in the public/ directory
// as well as a number of RESTful API endpoints.
//
// Once you've started this server, you should be able to see the
// demo website at the following address in your browser:
//
// http://localhost:3000/
//
// The endpoints will also be available at that URL. For example:
//
// http://localhost:currentAnimation/10


var express = require('express');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io')(server);

// Share the public directory:
app.use(express.static(__dirname + '/public'));

app.get('/currentAnimation/:id', function (req, res) {
	var currentAnimation = req.params.id;
	// switchAnimation(currentAnimation);
	console.log('Switching animation to: ', currentAnimation);
	updateAnimationServer();
  	res.status(200).send('Hello world!');
});


app.get('/xyPad', function (req, res) {
  res.status(200).send('xyPad World!');
});


app.get('/color', function (req, res) {
  res.status(200).send('color World!');
});


app.get('/accel', function (req, res) {
  res.status(200).send('accel World!');
});


app.get('/sound', function (req, res) {
  res.status(200).send('sound World!');
});


app.get('/gps', function (req, res) {
  res.status(200).send('gps World!');
});


app.get('/camera', function (req, res) {
  res.status(200).send('camera World!');
});


server.listen(3000, function () {
  console.log('Example app listening on port 3000!');
});

// End of RESTful and Static Asset Server
//========================================

//============================
// Start of Socket.io event handlers:
io.on('connection', function (socket) {
	// Connect to the light server if we need to:
  // connectToRawColorServer();
	console.log('Socket.io connection made...');

	socket.on('xy', function(xyData) {
		console.log('got xy data from client: ', xyData.x, xyData.y)
		updateAnimationServer(xyData);
	})
	socket.on('color', function(color) {
		console.log('got color data from client: ', color)
		updateAnimationServer(color);
	})
});
// End of socket.io event handlers:
//==================================


//============================
// Start of Raw Color Client:

var net = require('net');
var rawColorClient = new net.Socket();
var rawColorClientConnected = false;
var bufferSize = 600*3;
var buf = new Buffer(bufferSize, 'binary');
// const buf1 = Buffer.alloc(10); // DOES NOT WORK ON 4.4.7

function connectToRawColorServer() {
	if (!rawColorClientConnected) {
		console.log('Connecting to server...');
		rawColorClient.connect(3001, '127.0.0.1', function() {
			console.log('Connected!');
			rawColorClientConnected = true;
			// rawColorClient.write('Hello, server! Love, Client.');
		});

		rawColorClient.on('data', function(data) {
			console.log('Received: ' + data);
		});

		rawColorClient.on('close', function() {
			console.log('Connection closed');
			killClient();
		});

		rawColorClient.on('error', function() {
			console.log('Socket error...');
			killClient();
		});
	}
}

function killClient() {
	// rawColorClient.removeListener('data');
	// rawColorClient.removeListener('close');
	// rawColorClient.removeListener('error');
	rawColorClient.destroy(); // kill client after server's response
	rawColorClientConnected = false;
}

function updateAnimationServer(newData) {
	// When ever an endpoint is hit, this method should
	// send any updated parameters over to the
	// Animation Server written in Processing.
	//
	// That Animation Server will either change which
	// animation is currently playing, or will change
	// how the animations are actually running.
	//
	// This will either be a TCP or UDP stream unless
	// Jacob knows of a better way to communicate
	// from a server directly to Processing.

	// console.log('Updating the Animation Server...', newData);

	// TODO: At some point, this will NOT be the raw color server!
	if (rawColorClientConnected) {
		// console.log('Writing data to server...', newData);

		var encodedString = '';

		for (var i = 0; i < bufferSize; i += 3) {
			// var color = i%256;
			// encodedString = String.fromCharCode(color);
			var red = String.fromCharCode(newData.r);
			var green = String.fromCharCode(newData.g);
			var blue = String.fromCharCode(newData.b);

			// console.log('rgb: ', red, green, blue);

			encodedString += red;
			encodedString += green;
			encodedString += blue;
		}

		// Debug output:
		// for (var i = 0; i<=255; i++) {
		//   var char = String.fromCharCode(i); // takes a byte value, spits out a char
		// 	encodedString += char;
		//   var byte = char.charCodeAt(0);     // takes a char, spits out a byte value
		//   // console.log('see: ' + i +' \''+ char +'\'' +' \''+ byte +'\'');
		// }

		len = buf.write(encodedString, 0, bufferSize, 'binary');
		// console.log('Octets written : '+  len);
		rawColorClient.write(buf);
	} else {
		connectToRawColorServer();
	}
}
