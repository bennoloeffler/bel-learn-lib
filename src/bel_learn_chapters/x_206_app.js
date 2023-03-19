let counter = 0;
function clickCheckbox(element) {
 let flexContainer = document.getElementById("flexContainerID");
  if (element.checked) {
     // space is reserved
     flexContainer.style.visibility = "visible";
  } else {
    flexContainer.style.visibility = "hidden";
 }
}

function syncBadgeSlider() {
  document.getElementById("badge").innerHTML = counter;
  document.getElementById("inVal").value = counter;
  valBox.innerHTML = counter;
  randomSquare();
}

function changeHtml() {
 document.getElementById("demo").innerHTML = "call from external file!"
 counter++;
 syncBadgeSlider();
}

var ctx = null;
function replaceWithCanvas(){
 document.getElementById("canvasContainerID").innerHTML =
 `<canvas width=2500 height=950 id='canvasID'>`;
 // just use id of element: canvasID
 ctx = canvasID.getContext("2d");
 randomSquare();
 // space is created
 commentCanvas1.hidden = false;
 commentCanvas2.hidden = true;
 //addScrollingToCanvas();
}

function randomSquare() {
 if (ctx) {
  for(let i=0; i<100; i++) {
        x = Math.random() * 2500;
        y = Math.random() * 950;
        ctx.fillStyle = "white";
        ctx.fillRect(x, y, 5, 5);
    }
 }
}

function showVal(){
  counter = inVal.value;
  valBox.innerHTML = inVal.value;
  syncBadgeSlider();
}

function addScrollingToCanvas () {

var canvas = document.getElementById("canvasID");
var context = canvas.getContext('2d');
var dragging = false;
var lastX;
var marginLeft = 0;

for (var i = 0; i < 1000; i++) {
    context.beginPath();
    context.arc(Math.random() * 1500, Math.random() * 350, 20.0, 0, 2 * Math.PI, false);
    context.stroke();
}
    alert ("mouse down");

canvas.addEventListener('mousedown', function(e) {
    var evt = e || event;
    dragging = true;
    lastX = evt.clientX;
    e.preventDefault();
}, false);

canvas.addEventListener('mousemove', function(e) {
    var evt = e || event;
    if (dragging) {
        var delta = evt.clientX - lastX;
        lastX = evt.clientX;
        marginLeft += delta;
        canvas.style.marginLeft = marginLeft + "px";
    }
    e.preventDefault();
}, false);

canvas.addEventListener('mouseup', function() {
    dragging = false;
}, false);
}


// just for testing
//replaceWithCanvas();