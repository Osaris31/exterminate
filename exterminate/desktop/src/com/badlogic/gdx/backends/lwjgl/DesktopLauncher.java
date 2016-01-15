package com.badlogic.gdx.backends.lwjgl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import net.osaris.exterminate.ExterminateGame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public class DesktopLauncher extends LwjglApplication {
	public DesktopLauncher(ApplicationListener listener,
			LwjglApplicationConfiguration config) {
		super(listener, config);
	}


	public static void main (String[] arg) {
		try {
				System.setOut(new PrintStream(new TeeOutputStream(System.out, new PrintStream("logs.txt"))));
				System.setErr(new PrintStream(new TeeOutputStream(System.err, new PrintStream("Errorlogs.txt"))));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title="EXTERMINATE!";
		config.forceExit = true;
		config.width = 1280;
		config.height = 720;
//		config.addIcon("data/ic_launcher.png", FileType.Internal);
	
		config.samples = 1;
		config.useGL30 = true;
		config.foregroundFPS = 0;
		config.backgroundFPS = 0;
		config.vSyncEnabled = true;

		try {
			new DesktopLauncher(new ExterminateGame(), config);			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		
	}
	
	@Override
	void mainLoop () {
		
		try {
			super.mainLoop();			
		}
		catch(Exception e) {
			e.printStackTrace();
			if (audio != null) audio.dispose();
			Gdx.input.setCursorCatched(false);
			
			System.exit(-1);
		}
		catch(Throwable e) {
			e.printStackTrace();
			if (audio != null) audio.dispose();
			Gdx.input.setCursorCatched(false);
			
			System.exit(-1);
		}
	}
	
	public final static class TeeOutputStream extends OutputStream {

		  private final OutputStream out;
		  private final OutputStream tee;

		  public TeeOutputStream(OutputStream out, OutputStream tee) {
		    if (out == null)
		      throw new NullPointerException();
		    else if (tee == null)
		      throw new NullPointerException();

		    this.out = out;
		    this.tee = tee;
		  }

		  @Override
		  public void write(int b) throws IOException {
		    out.write(b);
		    tee.write(b);
		  }

		  @Override
		  public void write(byte[] b) throws IOException {
		    out.write(b);
		    tee.write(b);
		  }

		  @Override
		  public void write(byte[] b, int off, int len) throws IOException {
		    out.write(b, off, len);
		    tee.write(b, off, len);
		  }

		  @Override
		  public void flush() throws IOException {
		    out.flush();
		    tee.flush();
		  }

		  @Override
		  public void close() throws IOException {
		    out.close();
		    tee.close();
		  }
		}
	
}
