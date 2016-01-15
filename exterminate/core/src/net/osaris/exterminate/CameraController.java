package net.osaris.exterminate;

import net.osaris.exterminate.entities.Player;
import net.osaris.exterminate.tools.ScreenshotFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

public class CameraController extends InputAdapter {
	private static final float SPEED = 42;
	
	
	public static final int FPS = 0;
	public static final int TPS = 1;
	public static PerspectiveCamera camera;
	private static Player ship;
	public final static IntIntMap keys = new IntIntMap();
	private int STRAFE_LEFT = Keys.Q;
	private int STRAFE_RIGHT = Keys.D;
	private int FORWARD = Keys.Z;
	private int BACKWARD = Keys.S;
	private int SHIFT = Keys.SHIFT_LEFT;
	private int SPACE = Keys.SPACE;
	public static int CTRL1 = Keys.CONTROL_LEFT;
	private float degreesPerPixel = 0.5f;
	private final static Vector3 tmp = new Vector3();
	public static float FOV = 80;
	public static boolean longTimeNoMove = false;
	public static boolean drawHitBox = false;
	private final Vector3 direction = new Vector3(0, 0, 1f);
	public static float longTime;
	public static float sensiSouris = 0.15f;

	public static int cameraMode = 0;

	public static boolean fly = false;
	public static boolean tab = false;
	public static boolean creafly = false;
	public static boolean toggleRun = false;
	public static boolean show2D = true;

	public static boolean PAUSED = false;

	public CameraController(PerspectiveCamera camera, Player boy) {
		CameraController.camera = camera;
		CameraController.ship = boy;
	}

	public static float directionAngle = 90;

	@Override
	public boolean scrolled(int amount) {
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {

		if (keycode == Keys.X && keys.containsKey(Keys.CONTROL_LEFT)) {
			System.exit(0);
		}

		if (keycode == Keys.M) {
			if(SoundManager.masterSound>0.1f) {
				SoundManager.stopAllSounds();
				SoundManager.pauseAllMusiques();
				SoundManager.masterSound = 0.f;				
				SoundManager.masterMusic = 0.f;				
			}
			else {
				SoundManager.masterSound = 0.3f;				
				SoundManager.masterMusic = 0.3f;				
			}
		}

		if ((keycode == Keys.SPACE) && InGame.dead) {
			InGame.score=5000;
			InGame.maxScore=0;
			InGame.nbKills=0;
			InGame.totalTime=0;
			InGame.speedEnergy = 250;
			InGame.dead=false;
			InGame.rouge = 0;
			InGame.player.timeBeforeRespawn = 0;
			Gdx.input.setCursorCatched(true);
			PAUSED = false;
		}

		if (keycode == Keys.UP) {
			STRAFE_LEFT = Keys.LEFT;
			STRAFE_RIGHT = Keys.RIGHT;
			FORWARD = Keys.UP;
			BACKWARD = Keys.DOWN;
		}

		if (keycode == Keys.C) {
			ScreenshotFactory.saveScreenshot();
		}

		if (keycode == Keys.W) {
			STRAFE_LEFT = Keys.A;
			STRAFE_RIGHT = Keys.D;
			FORWARD = Keys.W;
			BACKWARD = Keys.S;
		}

		if (keycode == Keys.Q) {
			STRAFE_LEFT = Keys.Q;
			STRAFE_RIGHT = Keys.D;
			FORWARD = Keys.Z;
			BACKWARD = Keys.S;
		}

		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {

		if (keycode == Keys.ESCAPE || (keycode == Keys.SPACE && PAUSED)) {

			PAUSED = !PAUSED;
			Gdx.input.setCursorCatched(!PAUSED);

		} else if (keycode == Keys.F5) {
			cameraMode++;
			cameraMode = cameraMode % 3;
			if (cameraMode == 2) {
				cameraMode = 0;
			}
		} else if (keycode == Keys.F6) {
			cameraMode = 2;
		} else if (keycode == Keys.F11) {
			ExterminateGame.fullscree = !ExterminateGame.fullscree;

			ExterminateGame.updateFullscreen();
		}
		if (keycode == Keys.F3) {
			InGame.showInfo = !InGame.showInfo;
		}
		if (keycode == Keys.F4) {
			drawHitBox = !drawHitBox;
		}

		keys.remove(keycode, 0);
		return true;
	}

	/**
	 * Sets how many degrees to rotate per pixel the mouse moved.
	 * 
	 * @param degreesPerPixel
	 */
	public void setDegreesPerPixel(float degreesPerPixel) {
		this.degreesPerPixel = degreesPerPixel;
	}

	public static float angleX = -60;
	public static float angleY = 0;

	int boutonClicked = 0;

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		boolean move = mouseMoved(screenX, screenY);
		return move;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (PAUSED || InGame.dead)
			return false;
		if (!posing
				|| Gdx.input.getDeltaX() > 5
				|| Gdx.input.getDeltaX() < -5
				|| Gdx.input.getDeltaY() > 5
				|| Gdx.input.getDeltaY() < -5
				|| (System.currentTimeMillis() > timeStartPose + 100 && (Gdx.input
						.getDeltaX() > 2
						|| Gdx.input.getDeltaX() < -2
						|| Gdx.input.getDeltaY() > 2 || Gdx.input.getDeltaY() < -2))) {//
			justPosed = false;
		}

		if ((System.currentTimeMillis() > timeStartRemove + 200)) {//
			justremoved = false;
		}

		float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel * sensiSouris;
		float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel * sensiSouris;
		angleX += deltaX;
		angleY += deltaY;

		while (angleX >= 360) {
			angleX -= 360f;
			angleXAd -= 360f;
		}

		while (angleX < 0) {
			angleX += 360f;
			angleXAd += 360f;
		}

		angleY = Math.max(-89, Math.min(89, angleY));

		ship.angleX = angleX;
		ship.angleY = angleY;

		return true;
	}

	Vector3 tmp2 = new Vector3();
	private long timeStartPose;
	private long timeStartRemove;
	public static boolean posing;

	public long timeLastClick = 0;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (PAUSED)
			return false;

		if (System.currentTimeMillis() - timeLastClick < 200) {
		}

		timeLastClick = System.currentTimeMillis();

		boutonClicked = button;

		if (button == Buttons.LEFT) {
			ship.attack();
		} 
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (PAUSED)
			return false;
		boutonClicked = 0;

		if (button == Buttons.LEFT) {
		} else {

		}

		return true;
	}

	Vector3 lookatSansY = new Vector3();

	public static float angleYAd = 0;
	public static float angleXAd = 0;
	public static boolean snipe;
	public static boolean justPosed = false;
	public static boolean justremoved = false;
	public static boolean CONSOLE = false;
	public static boolean EVENT = false;
	public static boolean INVENTORY = false;

	Vector3 bpos = new Vector3();
	private float roolAngle = 0f;
	private float hauteurCamera = 3.3f;
	private float walkSince;
	public static Vector3 cameraRealPosition = new Vector3();
	public static float VITESSE_CREAFLY = 10f;
	public static float targetHauteurCamera = 0;

	public static void lerp(Vector3 vec, Vector3 target, float speed,
			float deltaTime) {

		vec.x -= (vec.x - target.x)
				* Math.min(1.0f / 60f, Math.max(1.0f / 120f, deltaTime))
				* speed;
		vec.y -= (vec.y - target.y)
				* Math.min(1.0f / 60f, Math.max(1.0f / 120f, deltaTime))
				* speed;
		vec.z -= (vec.z - target.z)
				* Math.min(1.0f / 60f, Math.max(1.0f / 120f, deltaTime))
				* speed;
	}

	public static float lerp(float vec, float target, float speed,
			float deltaTime) {

		return vec - (vec - target)
				* Math.min(1.0f / 60f, Math.max(1.0f / 120f, deltaTime))
				* speed;
	}

	public static float moveYangle = 0;
	public static float targetLife;

	public void update(float deltaTime) {

		longTime += deltaTime;

		if (longTime > 4 && cameraMode == FPS) {
			longTimeNoMove = true;
		}

		angleYAd = lerp(angleYAd, angleY, 30f, deltaTime);
		angleXAd = lerp(angleXAd, angleX, 30f, deltaTime);

		/*
		 * if(true) { angleXAd = angleX; angleYAd = angleY; }
		 */

		lerp(bpos, ship.pos, 10f, deltaTime);

		if (bpos.dst(ship.pos) > 1.5f) {
			tmp.set(ship.pos).sub(bpos);
			float dst = tmp.len();
			tmp.nor();
			bpos.mulAdd(tmp, dst - 1.5f);
		}
		if (cameraMode == FPS)
			bpos.set(ship.pos);
		moveYangle = lerp(moveYangle, ship.v.y * 0.1f, 15f, deltaTime);
		moveYangle = Math.max(-5, Math.min(5, moveYangle));
		camera.up.set(0, 1f, 0);

		direction.set(0, 0, 1f);
		direction.rotate(Vector3.X,
				Math.max(-89, Math.min(89, -angleYAd + moveYangle)));
		direction.rotate(Vector3.Y, angleXAd);

		lookatSansY.set(direction).crs(Vector3.Y).nor().crs(Vector3.Y).nor();
		lookatSansY.scl(-1);

		if (!ship.dead) {
			// boy.lookat.set(direction);

			ship.droite.set(direction).crs(Vector3.Y).nor();

		}

		boolean run = false;// || toggleRun
		boolean walk = false;// || toggleRun
		boolean cantRun = false;// || toggleRun
		if (fly)
			run = true;
		ship.v.x = 0;
		ship.v.z = 0;

		ship.vitesse = 0.0f;

		if (fly || creafly) {
			ship.vitesse = vitesseFly;
		}

		if (keys.containsKey(SHIFT)) {
			InGame.player.reelPosy+=deltaTime*5f;
		}
		if (keys.containsKey(SPACE)) {
			InGame.player.reelPosy-=deltaTime*5f;
		}
		/*
		 * if(keys.containsKey(CTRL) && !ExterminateGame.release) { vitesse*=10;
		 * }
		 */

		if (keys.containsKey(FORWARD) || true) {

			ship.v.x += lookatSansY.x * ship.vitesse;
			ship.v.z += lookatSansY.z * ship.vitesse;
			ship.backward = false;
			walk = true;
		}
		if (keys.containsKey(BACKWARD)) {

			ship.v.x += -lookatSansY.x * ship.vitesse;
			ship.v.z += -lookatSansY.z * ship.vitesse;
			ship.backward = true;

			walk = true;
		}
		if (keys.containsKey(STRAFE_LEFT)) {

			ship.v.x *= 0.7;
			ship.v.z *= 0.7;
			ship.backward = false;

			ship.v.x += -ship.droite.x * ship.vitesse * (walk ? 0.65f : 1f);
			ship.v.z += -ship.droite.z * ship.vitesse * (walk ? 0.65f : 1f);
			walk = true;
		}
		if (keys.containsKey(STRAFE_RIGHT)) {

			ship.v.x *= 0.7;
			ship.v.z *= 0.7;
			ship.backward = false;

			ship.v.x += ship.droite.x * ship.vitesse * (walk ? 0.65f : 1f);
			ship.v.z += ship.droite.z * ship.vitesse * (walk ? 0.65f : 1f);
			walk = true;
		}

		if (walk) {
			walkSince += deltaTime;
		} else {
			walkSince = 0;
		}


		// camera.position.set(boy.position);
		float angleReel = CameraController.angleX - ship.angleCorps;
		while (angleReel > 180f)
			angleReel -= 360f;
		while (angleReel < -180f)
			angleReel += 360f;

		if (!ship.dead
				&& (ship.attacking || (cameraMode == FPS && Math.abs(angleReel) > 60f))) {

			lerp(ship.dir, lookatSansY, 10f, deltaTime);
			ship.dir.y = 0;
			ship.dir.nor();
		}

		hauteurCamera = lerp(hauteurCamera, targetHauteurCamera, 15f, deltaTime);
		cameraMode =FPS;

		if ((cameraMode == TPS || ship.dead)) {
			tmp.set(direction).crs(Vector3.Y).nor();
			camera.direction.set(direction);
			camera.position.set(bpos);
			camera.position.mulAdd(direction, -2.2f); // -2.75
			// if(!fly)camera.position.mulAdd(tmp, -0.65f);
			camera.position.y += hauteurCamera * 1.121f;// 3.45f

		} else {
			camera.direction.set(direction);

			camera.position.set(bpos);
			camera.position.y += hauteurCamera + Math.cos(timeMove * 16f)
					* 0.045f;
			camera.position.x += Math.cos(timeMove * 7f + 1f) * 0.04f;
			camera.position.z += Math.cos(timeMove * 5f + 2f) * 0.04f;
			if (walk)
				camera.up.rotate(camera.direction,
						(float) (Math.cos(timeMove * 8f + 2f) * 0.1f));

			

		}

		if (!ship.dead && (walk || fly)) {

			lerp(ship.dir, lookatSansY, 5f, deltaTime);
			ship.dir.y = 0;
			ship.dir.nor();

			timeMove += Gdx.graphics.getDeltaTime() * 0.75;
		} else {
			// timeMove+=Gdx.graphics.getDeltaTime()*0.10f;
			ship.vitesse = 0;
		}

		float angle = new Vector2(lookatSansY.x, lookatSansY.z).angle();
		float angle2 = new Vector2(ship.dir.x, ship.dir.z).angle();
		angle = ((angle % 360f + 360f) % 360f);
		angle2 = ((angle2 % 360f + 360f) % 360f);
		float angle3 = (angle - angle2);
		angle3 = ((angle3 % 360f + 360f) % 360f);
		if (angle3 > 180)
			angle3 = 0f - (360f - angle3);
		roolAngle = lerp(roolAngle, (angle3) * ship.vitesse / 70f, 6f,
				deltaTime);

		if (walk && !cantRun) {
			targetFOV = FOV + 3f;
		} else {
			targetFOV = FOV - 4f;
		}

		if (keys.containsKey(Keys.TAB)) {
			tab = true;
		} else {
			tab = false;
		}

		/*
		 * if(keys.containsKey(Keys.TAB)) { targetFOV = 20f; snipe = true; }
		 * else { snipe = false; }
		 */

		camera.fieldOfView += (targetFOV - camera.fieldOfView)
				* Math.min(deltaTime * 10f, 1f);
		if (cameraMode == 2) {
			camera.direction.scl(-1);

			camera.position.mulAdd(direction, 2.6f); // -2.75

			// camera.position.y-=0.5f;//2.85f;

		}

		if (camera.position.y < InGame.world.island.getHauteur(
				camera.position.x, camera.position.z) + 1f) {
			camera.position.y = InGame.world.island.getHauteur(
					camera.position.x, camera.position.z) + 1f;
		}

		if (fly) {
			camera.rotate(camera.direction, roolAngle);
		}

		// roolAngle = lerp(roolAngle, 0, 2f, deltaTime);

		cameraRealPosition.set(camera.position);

		InGame.masterTranslate.setZero();
		InGame.IDTMtranslated.idt();
		InGame.masterTranslate.set(
				camera.position.x - camera.position.x % 1024f, 0,
				camera.position.z - camera.position.z % 1024f);
		InGame.masterTranslate.scl(-1f);
		// System.out.println(InGame.masterTranslate);
		// InGame.masterTranslate.set(2444, 0, -3245);
		camera.position.add(InGame.masterTranslate);
		InGame.IDTMtranslated.translate(InGame.masterTranslate);

		camera.update(true);
		// System.out.println(camera.position);
		if (fly) {
			camera.rotate(camera.direction, -roolAngle);
		}

		// System.out.println("update "+camera.position+" :: "+CameraController.angleXAd);

		try {
			if (posing) {
				majTargetPosing();
			} else {
				majTarget();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(targetBlockCasse + " vs "+targetBlock);

		if ((creafly) && keys.containsKey(SHIFT)) {
			InGame.player.v.y = -vitesseFly;
		} else if ((creafly) && keys.containsKey(SPACE)) {
			InGame.player.v.y = vitesseFly;
		} else if (fly) {
			InGame.player.v.y = (camera.direction.y + 0.1f)
					* InGame.player.vitesse * 1.5f;
		} else if (creafly) {
			InGame.player.v.y = 0;
		}


		vitesseFly = lerp(vitesseFly, InGame.dead ? 0 : SPEED, 0.25f, deltaTime);
		if(!show2D) vitesseFly=0f;
		if (creafly) {
			vitesseFly = VITESSE_CREAFLY;
		}

		if (fly) {
			InGame.forceVent = Math
					.max(InGame.forceVent, 1f + vitesseFly / 15f);
		}

		if (walk) {
			justPosed = false;
		}
		// System.out.println(camera.position);
	}

	static Vector3 origin = new Vector3();
	public static Vector3 targetEntity = new Vector3();
	public static boolean hasTargetEntity;

	public static void majTarget() {

	}

	public static void majTargetPosing() {

	}

	float vitesseFly = 0f;

	float targetFOV;

	float timeMove = 0;
}
