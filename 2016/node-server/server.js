var express = require('express');
var app = express();

// Share the public directory:
app.use(express.static('public'));

app.get('/currentAnimation/:id', function (req, res) {
	var currentAnimation = req.id;
	// switchAnimation(currentAnimation);
  	res.status(200).send('Switching animation to: ', currentAnimation);
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