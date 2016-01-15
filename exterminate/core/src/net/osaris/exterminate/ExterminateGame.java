package net.osaris.exterminate;


import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.FontBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.profiling.GL30Profiler;


public class ExterminateGame extends Game {
	
	public static final boolean release = true;
	public static final int VERSION = 1;
	public static final int MAX_VIEW_DISTANCE = 10;
	public static boolean useGL3 = true;
	public static int precisionOmbre = 2;
	public static boolean fullscree = false;
	public static boolean windowed = false;
	public static boolean fxaa = false;
	public static boolean vsync = true;
	public static float fsaa = 1.5f;
	public static int rendu = 16;//8
	public static int renduCube = 9;//11
	public static InGame inGame;
	private Texture loading;
	public static OrthographicCamera cam2D;
	public static boolean newvsync = vsync;
	public static SpriteBatch batcher;
	public static FontBatch batcherFont;
	 public static ShapeRenderer  shapeRenderer;
	@Override
	public void create () {
		if(Gdx.gl30==null) {
			useGL3 = false;
		}
		GL30Profiler.enable();
		
		loading = InGame.loadTexture("loading.jpg");
		 shapeRenderer = new  ShapeRenderer();


		 resize();
	}

	boolean init = false;
	private boolean doneOnce = false;
	private boolean finished = false;
	public static  boolean steamConnected = false;
	public static ExterminateGame game;
	public static boolean mustQuit = false;
	
	public void init() {
		if(doneOnce ) return;
		doneOnce = true;
		
		
		Assets.load();
		init = true;
		//ModelInstance.defaultShareKeyframes = true;	
	}

	@Override
	public void render () {
		if(mustQuit) {
			mustQuit = false;
		}
		
		if(!finished) {
			init();

			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		    Gdx.gl.glClearColor(0.4f, 0.41f, 0.45f, 1f);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			//Gdx.gl.glClear(GL20.GL_ALPHA_BITS);
			Gdx.gl.glColorMask(true, true, true, true);
			Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

			batcher.setProjectionMatrix(cam2D.combined);
			     
			batcher.begin();
			batcher.draw(loading, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batcher.setColor(Color.WHITE);
			batcher.end();
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.rect(0f,  Gdx.graphics.getHeight()*0.025f, Gdx.graphics.getWidth()*Assets.assets.getProgress(), Gdx.graphics.getHeight()*0.015f);
			shapeRenderer.end();

			if(init && Assets.loaded && Assets.assets.update(200)) {
				finishLoading();
				
			}
			
		}
		else {
			super.render();
			
		}
		


	}


	
	private void finishLoading() {
		if(finished) return;
		finished = true;
		
		System.out.println("**********************************************************");
		
		System.out.println(Gdx.gl.glGetString(GL20.GL_RENDERER));
		System.out.println(Gdx.gl.glGetString(GL20.GL_VENDOR));
		System.out.println(Gdx.gl.glGetString(GL20.GL_VERSION));
		
		Assets.assets.finishLoading();
		
	//	Bullet.init();
		Assets.init();
	
			
		inGame = new InGame();

			
		game=this;
		setScreen(inGame);	 
		loading.dispose();
	}
	
	@Override
	public void dispose() {

		
		if(InGame.world!=null) InGame.world.save();
		SoundManager.stopAllSounds();
		super.dispose();
		System.out.println("Game exited.");
	}

	public static void resize() {
		cam2D = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam2D.setToOrtho(false);
		cam2D.update();
		
		batcher = new SpriteBatch();
		batcher.setProjectionMatrix(cam2D.combined);
		
		
		batcherFont = new FontBatch(6);
		batcherFont.setProjectionMatrix(cam2D.combined);
		
		InGame.batcherFont = batcherFont;
		InGame.batcher = batcher;
		InGame.batcherFont.setShader(Assets.fontShader);
	}
	
	@Override
	public void resize(int width, int height) {
		resize();
		super.resize(width, height);
	}

	public static void updateFullscreen() {
		if(!ExterminateGame.fullscree) {

			Gdx.graphics.setDisplayMode(1280, 720, false);
		
		}
		else {
			// move the window to the right screen
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice primary = env.getDefaultScreenDevice();

			DisplayMode pmode = primary.getDisplayMode();
		
			Gdx.graphics.setDisplayMode(pmode.getWidth(), pmode.getHeight(), ExterminateGame.fullscree);
			
		}
	}
	
}
