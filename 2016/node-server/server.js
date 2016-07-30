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

// Share the public directory:
app.use(express.static('public'));

app.get('/currentAnimation/:id', function (req, res) {
	var currentAnimation = req.params.id;
	// switchAnimation(currentAnimation);
	console.log('Switching animation to: ', currentAnimation);
	updateAnimationServer();
  	res.status(200).send('Hello world!');
});


app.get('/xyPad', function (req, res) {
  res.send('xyPad World!');
});


app.get('/color', function (req, res) {
  res.send('color World!');
});


app.get('/accel', function (req, res) {
  res.send('accel World!');
});


app.get('/sound', function (req, res) {
  res.send('sound World!');
});


app.get('/gps', function (req, res) {
  res.send('gps World!');
});


app.get('/camera', function (req, res) {
  res.send('camera World!');
});


app.listen(3000, function () {
  console.log('Example app listening on port 3000!');
});

function updateAnimationServer() {
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

	console.log('Updating the Animation Server...');
}