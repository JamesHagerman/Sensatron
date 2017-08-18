SERVICE_URL = 'rs/lights';

var theParams = {
   'on': true,
   'flashlight': false,
   'mode': 4,
   'blendMode': 1,
   'hue1': 2,
   'hue2': 3,
   'saturation': 6,
   'slider4': 5,
   'slider5': 5,
   'pitchSliders': [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
};
allowUpdate = true;


function updateLights(params) {
  	if (params == null) params = {};
	$.ajax(SERVICE_URL, {data: params, method: 'POST', success: lightParamsCallback});
}

function triggerLights(whichTrigger) {
	$.ajax('rs/trigger', {data: {which: whichTrigger}, method: 'POST'});
}

function toggle() {
	updateLights({toggle: 'true'});
}

function flashlight() {
	updateLights({flashlight: true});
}

function directInput() {
	updateLights({directInput: true});
}

function lightParamsCallback(settingsJSON) {
	var settings = JSON.parse(settingsJSON);
	allowUpdate = false;
	$('#lightsOn').text(settings.on ? 'on' : 'off');
	$('#flashlightOn').text(settings.flashlight ? 'on' : 'off');
	$('#directInputOn').text(settings.directInput ? 'on' : 'off');
	$('#hue1').val(settings.hue1).change();
	$('#hue2').val(settings.hue2).change();
	$('#saturation').val(settings.saturation).change();
	$('#slider4').val(settings.slider4).change();
	$('#slider5').val(settings.slider5).change();
	setModeButtonOn(settings.mode);
	setBlendModeButtonOn(settings.blendMode);
//	if (settings.directInput) {
//		$('#fluidControlsDiv').show();
//	} else {
//		$('#fluidControlsDiv').hide();
//	}
	allowUpdate = true;
}

function setModeButtonOn(mode) {
	$('.mode-button').removeClass("mode-button-on");
	$('#mode-' + mode).addClass("mode-button-on");
}

function setBlendModeButtonOn(mode) {
	$('.blend-mode-button').removeClass("mode-button-on");
	$('#blend-mode-' + mode).addClass("mode-button-on");
}

  function setHue(event) {
    var hue = $(this).val();
    var hex = colorsys.hsv2Hex(hue, 100, 100);
    $(this).parent().find('.ui-slider-handle').css( "background-color", hex );
  	if (allowUpdate) {
  	  	var sliderId = $(this).prop('id');
    	var params = {}
    	params[sliderId] = hue;
    	updateLights(params)
    }
  }

function setSliderValue(event) {
	if (allowUpdate) {
  	  	var sliderId = $(this).prop('id');
    	var params = {}
    	params[sliderId] = $(this).val();
    	updateLights(params)
	}
}

function setMode(event) {
	var mode = $(this).data("mode");
	if (allowUpdate) {
		updateLights({mode: mode});
	}
}

function setBlendMode(event) {
	var mode = $(this).data("blend-mode");
	if (allowUpdate) {
		updateLights({blendMode: mode});
	}
}

function pressTrigger(event) {
	var which = $(this).data("trigger");
	triggerLights(which);
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
  $('.canvas').on('touchmove mousemove', handleMovement);

  $('.hue-slider').change(setHue);
  $('#saturation').change(setSliderValue);
  $('#slider4').change(setSliderValue);
  $('#slider5').change(setSliderValue);
  $('.mode-button').click(setMode);
  $('.blend-mode-button').click(setBlendMode);
  $('.trigger-button').on("vmousedown", pressTrigger);
  $('#beat').on("vmousedown", function() { updateLights({beat: true}); });
  setInterval(updateLights, 500);
});

function handleMovement(event) {

  event.preventDefault()
  // This block runs when the mouse moves over the canvas

  var x = 0;
  var y = 0;
  console.log('event: ', event);

  if (event.type === 'touchmove') {
    x = event.touches[0].pageX;
    y = event.touches[0].pageY;
  } else {
    x = event.offsetX;
    y = event.offsetY;
  }

  var xscale = x/window.innerWidth;
  var yscale = 1-y/window.innerHeight;

  // console.log('mousemove event: ', x, y, hue, val);

  var color = Color.HSVtoRGB(xscale, 1, yscale);
  // console.log('color: ', color, Color.getCSSColor());
  $('.canvas').css('background-color', Color.getCSSColor());

  // $('.canvas').off('mousemove');

  theParams.hue1 = Math.round(xscale*255);
  theParams.hue2 = Math.round(yscale*255);

  $.ajax({
     url: 'rs/lights',
     data: JSON.stringify(theParams),
     error: function(e) {
        console.log('Error on AJAX', e);
     },
    //  dataType: 'jsonp',
     success: function(data) {
        // console.log('data: ', data);
     },
     type: 'PUT'
  });

}


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
