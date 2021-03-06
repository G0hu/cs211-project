class Mover {
  PVector location;
  PVector velocity;
  PVector gravityForce;
  float gravityConstant = 0.2;
  float normalForce = 1;
  float mu = 0.05;
  float frictionMagnitude = normalForce * mu;
  PVector friction;
  float sphereDiam = 10;
  float minDist = cylinderBaseSize + sphereDiam;
  
  Mover() {
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
    gravityForce.x = sin(rotZ) * gravityConstant;
    gravityForce.z = -sin(rotX) * gravityConstant;   
    velocity.add(gravityForce);
    velocity.add(friction);
    location.add(velocity);
    checkCylinderCollision(cylinders);
  }
  void display() {
    fill(122, 187, 180);
    translate(location.x, location.y, location.z);
    sphere(sphereDiam);
  }
  void checkEdges() {

    if (location.x > 100) {
      velocity.x = -abs(velocity.x);
      location.x=BOX_WIDTH/2;
    } else if (location.x < -100) {
      velocity.x = abs(velocity.x);
      location.x =-BOX_WIDTH/2;
    }
    if (location.z > 100) {
      velocity.z = -abs(velocity.z);
      location.z= BOX_DEPTH/2;
    } else if (location.z < -100) {
      velocity.z = abs(velocity.z);
      location.z=-BOX_DEPTH/2;
    }
  }
  void checkCylinderCollision(ArrayList<PVector> cylinders) {
    for (int i=0; i< cylinders.size (); i++) {
      PVector normal = location;
      PVector cyl = cylinders.get(i);
      float distance = sqrt(pow((location.x-cyl.x), 2)+pow((location.z-cyl.z), 2));
      if (distance <= minDist) {
        println("Collision with cylinder : "+i);
        //normal.sub(cyl);
        //velocity.sub(2*velocity.dot(normal)*normal);
        PVector normVec = PVector.sub(location, cyl);
        normVec.normalize();
        float cst = PVector.dot(velocity, normVec)*2;
        PVector vec = PVector.mult(normVec, cst);
        velocity = PVector.sub(velocity, vec);
        PVector resPosVec = new PVector(location.x - cyl.x, location.y - cyl.y, 0);
        resPosVec.normalize();
        resPosVec = PVector.mult(resPosVec, 30);
        print("location avant " + location.x + ", " + location.y); 
        location.x = cyl.x + resPosVec.x;
        location.y = cyl.y + resPosVec.y;
        print("location apres " + location.x + ", " + location.y); 
        
        
      }
    }
  }
}
