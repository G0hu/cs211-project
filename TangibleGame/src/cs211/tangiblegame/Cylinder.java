package cs211.tangiblegame;

import processing.core.*;

class Cylinder {

	float cylinderBaseSize = 20;
	float cylinderHeight = 50;
	int cylinderResolution = 40;

	public PShape cylinder = new PShape();
	PShape openCylinder = new PShape();
	PShape topCylinder = new PShape();
	PApplet p;

	Cylinder(PApplet parent) {
		p = parent;
		float angle;
		float[] x = new float[cylinderResolution + 1];
		float[] y = new float[cylinderResolution + 1];

		// get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = (PConstants.TWO_PI / cylinderResolution) * i;
			x[i] = (float) (Math.sin(angle) * cylinderBaseSize);
			y[i] = (float) (Math.cos(angle) * cylinderBaseSize);
		}

		openCylinder = p.createShape();
		openCylinder.beginShape(PConstants.QUAD_STRIP);

		// draw the border of the cylinder
		for (int i = 0; i < x.length; i++) {
			openCylinder.vertex(x[i], 0, y[i]);
			openCylinder.vertex(x[i], -cylinderHeight, y[i]);
		}
		openCylinder.endShape();

		topCylinder = p.createShape();
		topCylinder.beginShape(PConstants.TRIANGLE_FAN);
		topCylinder.vertex(0, -cylinderHeight, 0);

		// draw the top of the cylinder
		for (int i = 0; i < x.length; i++) {
			topCylinder.vertex(x[i], -cylinderHeight, y[i]);
		}
		topCylinder.endShape();

		cylinder = p.createShape(PConstants.GROUP);
		cylinder.addChild(openCylinder);
		cylinder.addChild(topCylinder);
	}
}
