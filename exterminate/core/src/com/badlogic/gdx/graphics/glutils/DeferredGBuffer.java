/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.postprocessing.PostProcessor;

/**
 * 
 * regarde à "OSARIS c'est cette ligne qui change !"
 * Ah et oui j'ai déja essayé l'extends mais c'est mort, ok?
 */
public class DeferredGBuffer implements Disposable {
	/** the frame buffers **/
	private final static Map<Application, Array<DeferredGBuffer>> buffers = new Hashtable<Application, Array<DeferredGBuffer>>();

	/** the color buffer texture **/
	public int colorTexture;
	public int normalTexture;
	public int attribsTexture;
	public int depthTexture;

	public IntBuffer drawBuffs;
	
	/** the default framebuffer handle, a.k.a screen. */
	private static int defaultFramebufferHandle;
	/** true if we have polled for the default handle already. */
	private static boolean defaultFramebufferHandleInitialized = false;

	/** the framebuffer handle **/
	private int framebufferHandle;

	/** the depthbuffer render object handle **/
	public int depthbufferHandle;


	/** width **/
	protected final int width;

	/** height **/
	protected final int height;

	/** depth **/
	protected final boolean hasDepth;

	/** stencil **/
	protected final boolean hasStencil;

	private PostProcessor postProcessor;


//	private int colorbufferHandle;

	

	
	/** Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
	 * 
	 * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
	 *           color-renderable
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException in case the FrameBuffer could not be created */
	public DeferredGBuffer (int width, int height, boolean hasDepth, boolean hasStencil, PostProcessor postProcessor) {
		this.width = width;
		this.height = height;
		this.hasDepth = hasDepth;
		this.hasStencil = hasStencil;
		this.postProcessor = postProcessor;
		build();

		addManagedFrameBuffer(Gdx.app, this);
	}
	
	private void build () {
		GL20 gl = Gdx.gl20;

		// iOS uses a different framebuffer handle! (not necessarily 0)
		if (!defaultFramebufferHandleInitialized) {
			defaultFramebufferHandleInitialized = true;
			if (Gdx.app.getType() == ApplicationType.iOS) {
				IntBuffer intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
				gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, intbuf);
				defaultFramebufferHandle = intbuf.get(0);
			} else {
				defaultFramebufferHandle = 0;
			}
		}


		framebufferHandle = gl.glGenFramebuffer();

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);


		Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);

		colorTexture = gl.glGenTexture();

		gl.glBindTexture(GL20.GL_TEXTURE_2D, colorTexture);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, GL20.GL_GENERATE_MIPMAP, Gdx.gl.GL_FALSE);
		gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_FLOAT, null);
		gl.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D,
				colorTexture, 0);
		
		normalTexture = gl.glGenTexture();
		gl.glBindTexture(GL20.GL_TEXTURE_2D, normalTexture);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, GL20.GL_GENERATE_MIPMAP, Gdx.gl.GL_FALSE);
		gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_FLOAT, null);
		gl.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0+1, GL20.GL_TEXTURE_2D,
				normalTexture, 0);
		
		attribsTexture = gl.glGenTexture();
		gl.glBindTexture(GL20.GL_TEXTURE_2D, attribsTexture);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, GL20.GL_GENERATE_MIPMAP, Gdx.gl.GL_FALSE);
		gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_FLOAT, null);
		gl.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0+2, GL20.GL_TEXTURE_2D,
				attribsTexture, 0);
		
		depthTexture = gl.glGenTexture();
		gl.glBindTexture(GL20.GL_TEXTURE_2D, depthTexture);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MAG_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_MIN_FILTER, Gdx.gl.GL_NEAREST);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_S, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, Gdx.gl.GL_TEXTURE_WRAP_T, Gdx.gl.GL_CLAMP_TO_EDGE);
	    Gdx.gl.glTexParameteri( GL20.GL_TEXTURE_2D, GL20.GL_GENERATE_MIPMAP, Gdx.gl.GL_FALSE);
		gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL30.GL_R32F, width, height, 0, GL11.GL_RED, GL20.GL_FLOAT, null);
		gl.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0+3, GL20.GL_TEXTURE_2D,
				depthTexture, 0);
		
		
		depthbufferHandle = postProcessor.composite.buffer2.depthbufferHandle;/*gl.glGenTexture();
		gl.glBindTexture(GL20.GL_TEXTURE_2D, depthbufferHandle);
	    gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F, width, height, 0, GL20.GL_DEPTH_COMPONENT, GL20.GL_FLOAT,
                   null);*/
		

	/*	Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		depthbufferHandle = gl.glGenTexture();
		gl.glBindTexture(GL20.GL_TEXTURE_2D, depthbufferHandle);
	    gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL30.GL_DEPTH32F_STENCIL8, width, height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV,
                   null);*/


		gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		//gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32F, colorTexture.getWidth(), colorTexture.getHeight());


		drawBuffs = BufferUtils.newIntBuffer(4);
	    drawBuffs.put(0, GL30.GL_COLOR_ATTACHMENT0);
	    drawBuffs.put(1, GL30.GL_COLOR_ATTACHMENT1);
	    drawBuffs.put(2, GL30.GL_COLOR_ATTACHMENT2);
	    drawBuffs.put(3, GL30.GL_COLOR_ATTACHMENT3);

		org.lwjgl.opengl.GL20.glDrawBuffers(drawBuffs);
		
		
		int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		gl.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
		//	colorTexture.dispose();
	

			gl.glDeleteFramebuffer(framebufferHandle);

			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
			if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException("frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	/** Releases all resources associated with the FrameBuffer. */
	public void dispose () {
		GL20 gl = Gdx.gl20;

		IntBuffer handle = BufferUtils.newIntBuffer(1);

		gl.glDeleteTexture(colorTexture);
		gl.glDeleteTexture(normalTexture);
		gl.glDeleteTexture(attribsTexture);
		gl.glDeleteTexture(depthTexture);
		if (hasDepth)
//			gl.glDeleteRenderbuffer(depthbufferHandle);



		gl.glDeleteFramebuffer(framebufferHandle);

		if (buffers.get(Gdx.app) != null) buffers.get(Gdx.app).removeValue(this, true);
	}

	/** Makes the frame buffer current so everything gets drawn to it. */
	public void bind () {
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	public static void unbind () {
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);
	}

	/** Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it. */
	public void begin () {
		bind();
		setFrameBufferViewport();
	}

	/** Sets viewport to the dimensions of framebuffer. Called by {@link #begin()}. */
	protected void setFrameBufferViewport () {
		Gdx.gl20.glViewport(0, 0, width, height);
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	public void end () {
		unbind();
		setDefaultFrameBufferViewport();
	}

	/** Sets viewport to the dimensions of default framebuffer (window). Called by {@link #end()}. */
	protected void setDefaultFrameBufferViewport () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Unbinds the framebuffer and sets viewport sizes, all drawing will be performed to the normal framebuffer from here on.
	 * 
	 * @param x the x-axis position of the viewport in pixels
	 * @param y the y-asis position of the viewport in pixels
	 * @param width the width of the viewport in pixels
	 * @param height the height of the viewport in pixels */
	public void end (int x, int y, int width, int height) {
		unbind();
		Gdx.gl20.glViewport(x, y, width, height);
	}


	/** @return the height of the framebuffer in pixels */
	public int getHeight () {
		return height; 
	}

	/** @return the width of the framebuffer in pixels */
	public int getWidth () {
		return width; 
	}

	private static void addManagedFrameBuffer (Application app, DeferredGBuffer frameBuffer) {
		Array<DeferredGBuffer> managedResources = buffers.get(app);
		if (managedResources == null) managedResources = new Array<DeferredGBuffer>();
		managedResources.add(frameBuffer);
		buffers.put(app, managedResources);
	}

	/** Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild all managed frame buffers. This
	 * assumes that the texture attached to this buffer has already been rebuild! Use with care. */
	public static void invalidateAllFrameBuffers (Application app) {
		if (Gdx.gl20 == null) return;

		Array<DeferredGBuffer> bufferArray = buffers.get(app);
		if (bufferArray == null) return;
		for (int i = 0; i < bufferArray.size; i++) {
			bufferArray.get(i).build();
		}
	}

	public static void clearAllFrameBuffers (Application app) {
		buffers.remove(app);
	}

	public static StringBuilder getManagedStatus (final StringBuilder builder) {
		builder.append("Managed buffers/app: { ");
		for (Application app : buffers.keySet()) {
			builder.append(buffers.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder;
	}

	public static String getManagedStatus () {
		return getManagedStatus(new StringBuilder()).toString();
	}


	public void bindForReading(int i) {
		Gdx.gl20.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferHandle);
	}

	public void bindForWriting(int i) {
		Gdx.gl20.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferHandle);
	}

	public void setReadBuffer(int i) {
		Gdx.gl30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0 + i);
	}
}
