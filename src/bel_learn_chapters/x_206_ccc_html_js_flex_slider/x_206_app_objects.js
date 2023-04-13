
class Ball {
    constructor(x, y, r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }
}

function Point(x,y) {
    this.x = x;
    this.y = y;
}

function toCanvasCoords(pageX, pageY) {
    var rect = canvas.getBoundingClientRect();
    let x = (pageX - rect.left);
    let y = (pageY - rect.top);
    return new Point(x, y);
}

function CanvasInElement(containerID) {
    this.container = document.getElementById(containerID);
    this.myCanvasID = "canvasID"+"_"+containerID;
    let myHTML = "<canvas width=" + this.width +
                 " height="+this.height +
                 " id=" + this.myCanvasID + ">";
    this.container.innerHTML = myHTML;
    this.canvas = document.getElementById(myCanvasID);
    this.canvas.style.backgroundColor="orange";
    this.ctx = this.canvas.getContext("2d");

    var radius = 20;
    this.drawMouse = function(e) {
            //console.log("mouse:" + e.x + ", " + e.y);
            const p = toCanvasCoords(e.x, e.y);
            if (p.y > 0) {
                //console.log("trans:" + p.x + ", " + p.y);
                ctx.beginPath();
                //this.ctx.fillRect(p.x , p.y, 20, 20);
                radius += (Math.random() - 0.5) * 5;
                if(radius < 0) radius = 1;
                ctx.arc(p.x, p.y, radius, 0, 2 * Math.PI, false);
                ctx.fillStyle = "white";
                ctx.fill();
                //context.lineWidth = 5;
                //context.strokeStyle = '#003300';
                //context.stroke();


                ctx.fillStyle = "rgba(255, 165, 0, 0.01)"; // orange #FFA500, rgb(255, 165, 0)
                ctx.fillRect(0, 0, canvas.width, canvas.height);

            }
    }

    this.setSize = function(e) {
        //console.log ("evt: " "+ e);
        canvas.width = this.container.clientWidth;
        canvas.height = 400;
    }

    window.onresize = this.setSize
    window.onmousemove = this.drawMouse
    this.setSize();
    canvasID_resizedCanvas.style.borderTop = "3px solid white";
    canvasID_resizedCanvas.style.borderBottom = "3px solid white";

}