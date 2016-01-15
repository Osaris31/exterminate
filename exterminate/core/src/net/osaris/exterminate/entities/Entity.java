package net.osaris.exterminate.entities;

import net.osaris.exterminate.InGame;
import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.World;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class Entity implements Disposable {
	public Vector3 targetDir = new Vector3();

	
	public Vector3 mustTPTo = null;
	public long id = nextId();
	public long ownerId = 0;
    public boolean canMove = true;
    public boolean solid = true;
	public Mesh mesh;
	public Matrix4 transform;
	public boolean animate = true;
	final static short OBJECT_FLAG = 1<<9;
	public float radius2 = 1;
	public Vector3 moveByCollisions = new Vector3();
	
	public static Quaternion tmpQuaternion = new Quaternion();

	private LivingEntity owner;
	public LivingEntity owner() {
		if(owner!=null && owner.id == ownerId) {
			return owner;
		}
		if(ownerId==0 || world==null) {
			return null;
		}
		else {
			owner = world.entityManager.getLivingEntity(ownerId);
			return owner;
		}
	}
	public void setOwner(LivingEntity own) {
		owner = own;
		ownerId = own.id;
	}
	
	
	public LivingEntity target;

	public Chunk onChunk;
	
	
	 public Entity () {
		 
	 }

	 @Override
	public int hashCode() {
		return (int)id;
	}


    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
    public void dispose () {

        if(mesh!=null) {
        	mesh.dispose();        	
        }
        
    }

	

	public Vector3 pos = new Vector3(); //position du bas
	public Vector3 previousPos = new Vector3();
	public Vector3 v = new Vector3();
	public Vector3 size = new Vector3(1, 1, 1);
	public Vector3 sizeBox = new Vector3(1, 1, 1);
	public Vector3 vchoc = new Vector3();
	public boolean auSol = false;

	
	public float gravity = 50f;
	// les deux sont redondants
	public Vector3 dir = new Vector3(0, 0, 1);
	
	public boolean tombe=false;
	public float vitesse = 0.0f;
	
	public boolean managedDraw = true;
	public boolean managedUpdate = true;
	
	
	static Vector2 tmp = new Vector2();
	static Vector2 tmp2 = new Vector2();
	public World world;
	
	public void blam(Vector3 direction) {
		vchoc.add(direction);
	}
	
	public void update(float deltaTime) {
		

		tombe=false;
		
		float hauteurSol = getHauteurSol();
			
		//gravity
		if(pos.y>hauteurSol-0.1) {
			tombe=true;
			v.y-=gravity*deltaTime;
		}
		
		pos.add(moveByCollisions);
		moveByCollisions.setZero();
		pos.mulAdd(v, deltaTime);
		
		//todo tout ça
		
		if(tombe && pos.y<=hauteurSol) {
			v.y =0;
			pos.y = hauteurSol;
			tombe=false;
		}
		
		v.x=0;
		v.z=0;
		
		transform.idt();
		
		transform.translate(pos);
		transform.translate(InGame.masterTranslate);
		
	}
	

	public float getHauteurSol() {
		return getHauteurSol(pos);
	}

	public float getHauteurSol(Vector3 pos) {
		float hmap = world.getHauteurAt(pos.x, pos.z);

		return hmap;
		
	}

	public void draw(ModelBatch modelBatch, Environment environment, boolean pourOmbre) {
//		  modelBatch.render(model, environment);
	}
	
	public void drawHitboxes(ShaderProgram blockShader) {
	}
	
	public Vector3 tmpv = new Vector3();
	protected Vector3 tmpv2 = new Vector3();
	protected static Matrix4 hbm = new Matrix4();

	public boolean removeMe = false;

	public boolean hasPhysics = true;
		
		public static void lerp(Vector3 vec, Vector3 target, float speed, float deltaTime) {
			
			vec.x-=(vec.x-target.x)*Math.min(1.0f/30f, Math.max(1.0f/120f, deltaTime))*speed;
			vec.y-=(vec.y-target.y)*Math.min(1.0f/30f, Math.max(1.0f/120f, deltaTime))*speed;
			vec.z-=(vec.z-target.z)*Math.min(1.0f/30f, Math.max(1.0f/120f, deltaTime))*speed;
		}
		
		public static float lerp(float vec, float target, float speed, float deltaTime) {
			
			return vec-(vec-target)*Math.min(1.0f/30f, Math.max(1.0f/120f, deltaTime))*speed;
		}

		public void onCollide(Entity other) {

		}


		public static long nextId() {
			currentId--;
			return currentId;
		}
		private static long currentId = -10;

		public void onCreate(long id, World world) {
			if(id>0) {
				this.id = id;				
			}
			this.world = world;
			world.entityManager.manage(this);
		}

	
		public void drawOther() {
			// TODO Auto-generated method stub
			
		}
		public final static Matrix4 IDTM = new Matrix4().idt();


		public void onRemove() {
			// TODO Auto-generated method stub
			
		}
		

}
