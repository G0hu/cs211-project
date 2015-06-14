package cs211.tangiblegame;

public interface CollisionListener {

	public void onEdgeCollision(float velocity);
	public void onCylinderCollision(float velocity);
	
}
