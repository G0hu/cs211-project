Mover mover;
Cylinder cylinder;

void setup() { 
  size(500, 500, P3D); 
  noStroke();
  f= createFont("Arial", 45, true);
  mover = new Mover();
  cylinder = new Cylinder();
}
float rotX = 0;
float rotZ = 0;
float tmpRotX = 0;
float tmpRotZ = 0;
float initX, initY;
float speed = 100;
PFont f;

static final int BOX_WIDTH = 200;
static final int BOX_DEPTH = 200;
static final int BOX_HEIGHT = 10;

boolean addCylinderOK = false;
boolean paused = false;

ArrayList<PVector> cylinders = new ArrayList<PVector>();

void drawBase() {
  pushMatrix();
  textFont(f, 36);
  fill(255);
  background(200);
  lights();
  translate(width/2, height/2, 0);
  popMatrix();
}
void pause() {
  paused = true;
}
void unPause() {
  paused = false;
}

void draw() {
  drawBase();
  pushMatrix();
  translate(width/2, height/2, 0);
  rotateX(rotX);
  rotateZ(rotZ);
  box(BOX_WIDTH, BOX_HEIGHT, BOX_DEPTH);
  for (int i = 0; i < cylinders.size (); i++) {
    PVector vec = cylinders.get(i);
    pushMatrix();
    translate(vec.x, 0, vec.z);
    shape(cylinder.cylinder);
    popMatrix();
  } 
  pushMatrix();
  translate(0, -15, 0);
  if (!paused) {
    mover.update(rotX, rotZ, cylinders);
    mover.checkEdges();
  }
  mover.display();
  popMatrix();
  popMatrix();
  text("Speed : "+1000/speed, 10, 100);
}


void mouseDragged() {
  if (!paused) {
    float nextRotX = rotX+ (initY - mouseY)/speed;
    if (nextRotX>-PI/3 && nextRotX <PI/3) {
      rotX =nextRotX;
    }
    float nextRotZ = rotZ+ (mouseX - initX)/speed;
    if (nextRotZ> -PI/3 && nextRotZ< PI/3) {
      rotZ = nextRotZ;
    }
    initY = mouseY;
    initX = mouseX;
  }
}
void mousePressed() {
  println(mouseX + "  " + mouseY);
  if (addCylinderOK) {
    addCylinder();
  } else {
    initX = mouseX;
    initY = mouseY;
  }
}
void mouseWheel(MouseEvent event) { 
  if (event.getCount()>0 && speed<1000) {
    speed = speed*1.1;
  } else if (event.getCount()<0 && speed>10) {
    speed = speed*0.9;
  }
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode == SHIFT) {
      addCylinderOK = true;
      tmpRotX = rotX;
      tmpRotZ = rotZ;
      rotX = -PI/2;
      rotZ = 0;
      pause();
    }
  }
}

void keyReleased() {
  if (key == CODED) {
    if (keyCode == SHIFT) {
      addCylinderOK = false;
      rotX = tmpRotX;
      rotZ = tmpRotZ;
      unPause();
    }
  }
}

void addCylinder() {
  if (checkBounds(mouseX - 150, mouseY - 150)) {
    cylinders.add(new PVector(-(width/2-mouseX), 0, -(height/2-mouseY)));
    println(cylinders);
  }
}

boolean checkBounds(float x, float y) {
  if ((x > (BOX_WIDTH-10)) || x < 10) {
    return false;
  } else {
    return (y <= (BOX_DEPTH-10)) && (y >= 10);
  }
}

/*void drawCylinders() {
 pushMatrix();
 translate(width/2, height/2, 0);
 rotateX(-PI/2);
 for (int i = 0; i < cylinders.size (); i++) {
 PVector vec = cylinders.get(i);
 pushMatrix();
 translate(vec.x, 0, vec.z);
 shape(cylinder.cylinder);
 popMatrix();
 } 
 popMatrix();
 }*/
