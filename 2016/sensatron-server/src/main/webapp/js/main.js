SERVICE_URL = 'rs/lights'

function updateLights(params) {
  console.log('update lights');
	$.ajax(SERVICE_URL, {data: params, method: 'POST', success: lightParamsCallback});
}

function toggle() {
  console.log('toggle hit!');
	updateLights({toggle: 'true'});
}

function lightParamsCallback(settingsJSON) {
  console.log('lightParamsCallback hit');
	var settings = JSON.parse(settingsJSON);
	$('#lightsOn').text(settings.on ? 'on' : 'off');
}




// Entry point to the whole show:
// Note: This uses jQuery:
$(document).ready(function() {
  // This block runs when the document is ready.

	updateLights({});

  // Setup the color picker:
  Color.setHSV();

  // Add an event listener on the canvas to catch
  // mouse movements:
  $('.canvas').on('mousemove', function(event) {
    // This block runs when the mouse moves over the canvas

    var x = event.offsetX;
    var y = event.offsetY;

    var xscale = x/window.innerWidth;
    var yscale = 1-y/window.innerHeight;

    // console.log('mousemove event: ', x, y, hue, val);

    var color = Color.HSVtoRGB(xscale, 1, yscale);
    // console.log('color: ', color, Color.getCSSColor());
    $('.canvas').css('background-color', Color.getCSSColor());

    // $('.canvas').off('mousemove');

    $.ajax({
       url: 'http://localhost:8080/lights',
       data: {
          derp: 'this is a string'
       },
       error: function() {
          $('#info').html('<p>An error has occurred</p>');
       },
      //  dataType: 'jsonp',
       success: function(data) {
          console.log('data: ', JSON.stringify(data, 0, 2));
       },
       type: 'PUT'
    });

  })
});


// h, s, and v should be decimal values between 0 and 1
// They will be bound if they aren't.
var Color = (function() {
  var h = 0;
  var s = 1;
  var v = 1;
  var r, g, b, i, f, p, q, t;
  var stepSize = 0.01; // Used with getNextColor()

  return {
    setHSV: setHSV,
    boundRanges: boundRanges,
    HSVtoRGB: HSVtoRGB,
    getCSSColor: getCSSColor,
    getRGB: getRGB,
    getNextColor: getNextColor,
    getRandom: getRandom,
    getRandomHue: getRandomHue,
    getRandomHueSat: getRandomHueSat,
    getRandomSat: getRandomSat,
    getRandomVal: getRandomVal
  };

  function setHSV(hue, sat, val) {
    h = boundRanges(hue);
    s = boundRanges(sat);
    v = boundRanges(val);
  }
  function boundRanges(toBound) {
    if (typeof toBound === 'undefined') toBound = 0;
    if (toBound > 1) toBound = 1;
    if (toBound < 0) toBound = 0;
    return toBound;
  }
  function HSVtoRGB(hue, sat, val) {
    setHSV(hue, sat, val);

    i = Math.floor(h * 6);
    f = h * 6 - i;
    p = v * (1 - s);
    q = v * (1 - f * s);
    t = v * (1 - (1 - f) * s);
    switch (i % 6) {
       case 0: r = v, g = t, b = p; break;
       case 1: r = q, g = v, b = p; break;
       case 2: r = p, g = v, b = t; break;
       case 3: r = p, g = q, b = v; break;
       case 4: r = t, g = p, b = v; break;
       case 5: r = v, g = p, b = q; break;
    }
    r = Math.floor(r * 255);
    g = Math.floor(g * 255);
    b = Math.floor(b * 255);

    return [r,g,b];
    //return {r: r, g: g, b: b};
    // Returns "#rrggbb" for use in jquery css setters
    //return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
  }
  function getCSSColor() {
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
  }
  function getRGB(hue, sat, val) {
    return HSVtoRGB(hue, sat, val);
  }
  function getNextColor() {
    h += this.stepSize;
    if (h > 1) {
      h = 0;
    }
    return this.HSVtoRGB(h, s, v);
  }
  function getRandom() { // Not very pretty... but might be okay for data projects
    return this.HSVtoRGB(Math.random(), Math.random(), Math.random());
  }
  function getRandomHue() { // RAINBOWS!!!
    return this.HSVtoRGB(Math.random(), s, v);
  }
  function getRandomHueSat() { // Very Pastel but useable if rainbows hurt
    return this.HSVtoRGB(Math.random(), Math.random(), v);
  }
  function getRandomSat() { // Value controls "whiteness"
    return this.HSVtoRGB(h, Math.random(), v);
  }
  function getRandomVal() { // Value controls "blackness"
    return this.HSVtoRGB(h, s, Math.random());
  }


})();
