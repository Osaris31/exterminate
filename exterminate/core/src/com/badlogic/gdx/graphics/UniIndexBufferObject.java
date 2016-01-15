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
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs. This class can be
 * seamlessly used with OpenGL ES 1.x and 2.0.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 * 
 * <p>
 * You can also use this to store indices for vertex arrays. Do not call {@link #bind()} or {@link #unbind()} in this case but
 * rather use {@link #getBuffer()} to use the buffer directly with glDrawElements. You must also create the IndexBufferObject with
 * the second constructor and specify isDirect as true as glDrawElements in conjunction with vertex arrays needs direct buffers.
 * </p>
 * 
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * 
 * @author mzechner */
public class UniIndexBufferObject implements IndexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);


	private static final int MAX_INDICIES = 65536*3;

	
	int bufferHandle;
	final boolean isDirect;
	boolean isDirty = true;
	boolean isBound = false;
	final int usage;
	public int capacity;

	/** Creates a new IndexBufferObject.
	 * 
	 * @param isStatic whether the index buffer is static
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public UniIndexBufferObject (boolean isStatic, int maxIndices) {
		isDirect = true;

		capacity = maxIndices * 2;

		
		bufferHandle = createBufferObject();
		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
	}

	/** Creates a new IndexBufferObject to be used with vertex arrays.
	 * 
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public UniIndexBufferObject (int maxIndices) {
		this(true, maxIndices);
	}

	private int createBufferObject () {
		Gdx.gl20.glGenBuffers(1, tmpHandle);
		return tmpHandle.get(0);
		
//		return UniVertexBufferObject.bufferPool.obtain();
	}

	/** @return the number of indices currently stored in this buffer */
	public int getNumIndices () {
		return capacity/(intIndices ? 4 : 2);
	}

	/** @return the maximum number of indices this IndexBufferObject can store. */
	public int getNumMaxIndices () {
		return capacity/(intIndices ? 4 : 2);
	}

	/** <p>
	 * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
	 * copied to this IndexBufferObject.
	 * </p>
	 * 
	 * <p>
	 * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
	 * </p>
	 * 
	 * @param indices the vertex data
	 * @param offset the offset to start copying the data from
	 * @param count the number of shorts to copy */
	public void setIndices (short[] indices, int offset, int count) {

	}

	public void setIndices (ShortBuffer indices, int offset, int count) {
		isDirty = true;
		capacity = count*2;
		indices.position(0);
		indices.limit(count);

		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, capacity, indices, usage);
			isDirty = false;
		}
		else {
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, capacity, indices, usage);
			isDirty = false;
		}
	}

	public boolean intIndices = false;
	public void setIndices (IntBuffer indices, int offset, int count) {
		isDirty = true;
		capacity = count*4;
		indices.position(0);
		indices.limit(count);
		intIndices = true;
		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, capacity, indices, usage);
			isDirty = false;
		}
		else {
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, capacity, indices, usage);
			isDirty = false;
		}
	}

	public void updateIndices (IntBuffer indices, int offset, int count) {
		capacity = Math.max(offset * 4 + count * 4, capacity);
		
		isDirty = true;
		indices.position(0);
		indices.limit(count);
		intIndices = true;
		if (isBound) {
			Gdx.gl20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, offset * 4, capacity, indices);
			isDirty = false;
		}
		else {
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
			Gdx.gl20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, offset * 4, capacity, indices);
			isDirty = false;
		}
	}

	/** <p>
	 * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
	 * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
	 * </p>
	 * 
	 * @return the underlying short buffer. */
	public ShortBuffer getBuffer () {
		isDirty = true;
		System.out.println("UNSUPPORTED");
		return null;
	}

	/** Binds this IndexBufferObject for rendering with glDrawElements. */
	public void bind () {
		if (bufferHandle == 0) throw new GdxRuntimeException("No buffer allocated!");

		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
	/*	if (isDirty) {
			byteBuffer.limit(buffer.limit() * 2);
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}*/
		isBound = true;
	}

	/** Unbinds this IndexBufferObject. */
	public void unbind () {
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
		bufferHandle = createBufferObject();
		isDirty = true;
	}

	/** Disposes this IndexBufferObject and all its associated OpenGL resources. */
	public void dispose () {
		tmpHandle.clear();
		tmpHandle.put(bufferHandle);
		tmpHandle.flip();
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		Gdx.gl20.glDeleteBuffers(1, tmpHandle);
		
	//	UniVertexBufferObject.bufferPool.free(bufferHandle);
		
		bufferHandle = 0;

//		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	@Override
	public void setIndices(ShortBuffer arg0) {
		System.out.println("NOT SUPPORTED");
	}
}
