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

package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import net.osaris.exterminate.InGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;

/** <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * </p>
 * 
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object. This class
 * can be used seamlessly with OpenGL ES 1.x and 2.0.
 * </p>
 * 
 * <p>
 * In case OpenGL ES 2.0 is used in the application the data is bound via glVertexAttribPointer() according to the attribute
 * aliases specified via {@link VertexAttributes} in the constructor.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 * 
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * 
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class UniVertexBufferObject implements VertexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

	private static final int MAX_VERTICIES = 65536*3;

	final VertexAttributes attributes;
	int bufferHandle;
	final boolean isStatic;
	final int usage;
	int capacity;
	boolean isDirty = false;
	boolean isBound = false;

	/** Constructs a new interleaved VertexBufferObject.
	 * 
	 * @param isStatic whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttribute}s. */
	public UniVertexBufferObject (boolean isStatic, int numVertices, VertexAttribute... attributes) {
		this(isStatic, numVertices, new VertexAttributes(attributes));
	}

	/** Constructs a new interleaved VertexBufferObject.
	 * 
	 * @param isStatic whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttributes}. */
	public UniVertexBufferObject (boolean isStatic, int numVertices, VertexAttributes attributes) {
		this.isStatic = isStatic;
		this.attributes = attributes;

		capacity = this.attributes.vertexSize * numVertices;

		bufferHandle = createBufferObject();
		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
	}

	private int createBufferObject () {
		Gdx.gl20.glGenBuffers(1, tmpHandle);
		return tmpHandle.get(0);
//		return bufferPool.obtain();
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}

	@Override
	public int getNumVertices () {
		return capacity / attributes.vertexSize;
	}

	@Override
	public int getNumMaxVertices () {
		return capacity / attributes.vertexSize;
	}

	@Override
	public FloatBuffer getBuffer () {
		isDirty = true;
		return null;
	}

	
	
	public void setVertices (FloatBuffer vertices, int offset, int count) {
	//	long t = System.nanoTime();
		isDirty = true;
		capacity = count * 4;
		nbBuffers++;
		memUsage+=capacity;
		vertices.limit(count);
		vertices.position(0);
		
		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, capacity , vertices, usage);
			isDirty = false;
		}
		else {
			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, capacity, vertices, usage);
			isDirty = false;
		}
	/*	
		if(System.nanoTime()-t>5000000) {
			System.out.println(count+" tempsuni1:"+(System.nanoTime()-t)/1000000);
			showMem();

			lastFrame = InGame.frame;

		}*/
	}
	
	
	
	public static void showMem() {
		System.out.println((memUsage/1024/1024)+" Mo in "+nbBuffers+" buffers in frame "+InGame.frame);

	}

	public void updateVertices (FloatBuffer vertices, int offset, int count) {
		isDirty = true;
		vertices.limit(count);
		vertices.position(0);

		if (isBound) {
			Gdx.gl20.glBufferSubData(GL20.GL_ARRAY_BUFFER, offset * 4, capacity, vertices);
			isDirty = false;
		}
		else {
			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
			Gdx.gl20.glBufferSubData(GL20.GL_ARRAY_BUFFER, offset * 4, capacity, vertices);
			isDirty = false;
		}

	}
	
	public static int memUsage = 0;
	public static int nbBuffers = 0;
	public static int lastFrame = 0;
	
	@Override
	public void setVertices (float[] vertices, int offset, int count) {
	}

	@Override
	public void updateVertices (int targetOffset, float[] vertices, int sourceOffset, int count) {
		System.out.println("UNSUPPORTED!!!");
	}

	/** Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
	 * 
	 * @param shader the shader */
	@Override
	public void bind (ShaderProgram shader) {
		bind(shader, null);
	}

	@Override
	public void bind (ShaderProgram shader, int[] locations) {
		final GL20 gl = Gdx.gl20;

		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);

		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
						attribute.offset);
			}
			
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
					attribute.offset);
			}
		}
		isBound = true;
	}

	/** Unbinds this VertexBufferObject.
	 * 
	 * @param shader the shader */
	@Override
	public void unbind (final ShaderProgram shader) {
		unbind(shader, null);
	}

	@Override
	public void unbind (final ShaderProgram shader, final int[] locations) {
		final GL20 gl = Gdx.gl20;
		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				shader.disableVertexAttribute(attributes.get(i).alias);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final int location = locations[i];
				if (location >= 0) shader.disableVertexAttribute(location);
			}
		}
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/** Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
		bufferHandle = createBufferObject();
		isDirty = true;
	}

	/** Disposes of all resources this VertexBufferObject uses. */
	@Override
	public void dispose () {
		tmpHandle.clear();
		tmpHandle.put(bufferHandle);
		tmpHandle.flip();
		GL20 gl = Gdx.gl20;
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		gl.glDeleteBuffers(1, tmpHandle);
		
	//	bufferPool.free(bufferHandle);
		
		bufferHandle = 0;
//		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	public static void initBufferPool() {
/*		bufferPool = new BufferPool();
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for(int i=0;i<3000;i++) {
			tmp.add(bufferPool.obtain());
		}
		
		for(Integer buf : tmp) {
			bufferPool.free(buf);
		}*/
	}
	public static BufferPool bufferPool;
	
	public static class BufferPool extends Pool<Integer> {
	    public BufferPool() {
	    	super(3000);
	    }

	    @Override
	    public void free(Integer pfx) {
	       
	        super.free(pfx);
	    }

	    @Override
	    protected Integer newObject() {
			Gdx.gl20.glGenBuffers(1, tmpHandle);
			return tmpHandle.get(0);

	    }
	    
	    @Override
	    public Integer obtain() {
	    	Integer newEffect = super.obtain();

	        
	        return newEffect;
	    }
	    
	}

}
