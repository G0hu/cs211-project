float cylinderBaseSize = 10; 
float cylinderHeight = 50; 
int cylinderResolution = 40;


class Cylinder {
  
  public PShape cylinder = new PShape();
  PShape openCylinder = new PShape(); 
  PShape topCylinder = new PShape();

  Cylinder() {
    float angle;
    float[] x = new float[cylinderResolution + 1]; 
    float[] y = new float[cylinderResolution + 1];

    //get the x and y position on a circle for all the sides
    for (int i = 0; i < x.length; i++) {
      angle = (TWO_PI / cylinderResolution) * i; 
      x[i] = sin(angle) * cylinderBaseSize;
      y[i] = cos(angle) * cylinderBaseSize;
    }

    openCylinder = createShape();
    openCylinder.beginShape(QUAD_STRIP);

    //draw the border of the cylinder
    for (int i = 0; i < x.length; i++) { 
      openCylinder.vertex(x[i], 0, y[i]); 
      openCylinder.vertex(x[i], -cylinderHeight, y[i]);
    }
    openCylinder.endShape();

    topCylinder = createShape();
    topCylinder.beginShape(TRIANGLE_FAN);
    topCylinder.vertex(0, -cylinderHeight, 0);

    //draw the top of the cylinder
    for (int i = 0; i< x.length; i++) {
      topCylinder.vertex(x[i], -cylinderHeight, y[i]);
    }
    topCylinder.endShape();

    cylinder = createShape(GROUP);
    cylinder.addChild(openCylinder);
    cylinder.addChild(topCylinder);
  }
}
