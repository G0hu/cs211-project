package cs211.tangiblegame;

import java.util.ArrayList;

import processing.core.*;

class Mover {
	PApplet p;
	PVector location;
	PVector velocity;
	PVector gravityForce;
	float gravityConstant = 0.2f;
	float normalForce = 1;
	float mu = 0.05f;
	float frictionMagnitude = normalForce * mu;
	PVector friction;
	float sphereDiam = 10;
	float cylinderBaseSize = 20;
	float cylinderHeight = 50;
	int cylinderResolution = 40;

	float minDist = cylinderBaseSize + sphereDiam;
	CollisionListener collisionListener;

	Mover(PApplet parent, CollisionListener collisionListener) {
		p = parent;
		this.collisionListener = collisionListener;
		
		location = new PVector(0, 0, 0);
		velocity = new PVector(0, 0, 0);
		friction = new PVector(0, 0, 0);
		gravityForce = new PVector(0, 0, 0);
	}

	void update(float rotX, float rotZ, ArrayList<PVector> cylinders) {
		friction = velocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);
		gravityForce.x = (float) (Math.sin(rotZ) * gravityConstant);
		gravityForce.z = (float) (-Math.sin(rotX) * gravityConstant);
		velocity.add(gravityForce);
		velocity.add(friction);
		location.add(velocity);
		checkCylinderCollision(cylinders);
	}

	void display() {
		p.fill(122, 187, 180);
		p.translate(location.x, location.y, location.z);
		p.sphere(sphereDiam);
	}

	void checkEdges() {
		boolean collision = false;
		if (location.x > 100) {
			velocity.x = -Math.abs(velocity.x);
			location.x = TangibleGame.BOX_WIDTH / 2;
			collision = true;
		} else if (location.x < -100) {
			velocity.x = Math.abs(velocity.x);
			location.x = -TangibleGame.BOX_WIDTH / 2;
			collision = true;
		}
		
		if (location.z > 100) {
			velocity.z = -Math.abs(velocity.z);
			location.z = TangibleGame.BOX_DEPTH / 2;
			collision = true;
		} else if (location.z < -100) {
			velocity.z = Math.abs(velocity.z);
			location.z = -TangibleGame.BOX_DEPTH / 2;
			collision = true;
		}
		
		if (collision) {
			collisionListener.onEdgeCollision(velocity.mag());
		}
	}

	void checkCylinderCollision(ArrayList<PVector> cylinders) {
		for (int i = 0; i < cylinders.size(); i++) {
			PVector cyl = cylinders.get(i);
			float distance = (float) Math.sqrt(Math
					.pow((location.x - cyl.x), 2)
					+ Math.pow((location.z - cyl.z), 2));
			if (distance <= minDist) {
				// System.out.println("Collision with cylinder : " + i);
				// normal.sub(cyl);
				// velocity.sub(2*velocity.dot(normal)*normal);
				PVector normVec = PVector.sub(location, cyl);
				normVec.normalize();
				float cst = PVector.dot(velocity, normVec) * 2;
				PVector vec = PVector.mult(normVec, cst);
				velocity = PVector.sub(velocity, vec);
				PVector resPosVec = new PVector(location.x - cyl.x, location.y
						- cyl.y, 0);
				resPosVec.normalize();
				resPosVec = PVector.mult(resPosVec, 30);
				//System.out.print("location avant " + location.x + ", " + location.y);
				location.x = cyl.x + resPosVec.x;
				location.y = cyl.y + resPosVec.y;
				// System.out.print("location apres " + location.x + ", " + location.y);
				
				collisionListener.onCylinderCollision(velocity.mag());
			}
		}
	}
}
