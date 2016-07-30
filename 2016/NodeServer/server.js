var express = require('express');
var app = express();

app.get('/currentAnimation/:id', function (req, res) {
	var currentAnimation = req.id;
	// switchAnimation(currentAnimation);
  	res.send('Switching animation to: ', currentAnimation);
});

app.listen(3000, function () {
  console.log('Example app listening on port 3000!');
});