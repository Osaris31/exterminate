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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** <p>
 * A Mesh holds vertices composed of attributes specified by a {@link VertexAttributes} instance. The vertices are held either in
 * VRAM in form of vertex buffer objects or in RAM in form of vertex arrays. The former variant is more performant and is preferred
 * over vertex arrays if hardware supports it.
 * </p>
 * 
 * <p>
 * Meshes are automatically managed. If the OpenGL context is lost all vertex buffer objects get invalidated and must be reloaded
 * when the context is recreated. This only happens on Android when a user switches to another application or receives an incoming
 * call. A managed Mesh will be reloaded automagically so you don't have to do this manually.
 * </p>
 * 
 * <p>
 * A Mesh consists of vertices and optionally indices which specify which vertices define a triangle. Each vertex is composed of
 * attributes such as position, normal, color or texture coordinate. Note that not all of this attributes must be given, except
 * for position which is non-optional. Each attribute has an alias which is used when rendering a Mesh in OpenGL ES 2.0. The alias
 * is used to bind a specific vertex attribute to a shader attribute. The shader source and the alias of the attribute must match
 * exactly for this to work.
 * </p>
 * 
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class UniMesh extends Mesh {

	/** by jw: Creates a new Mesh with the given attributes. Adds extra optimizations for dynamic (frequently modified) meshes.
	 * 
	 * @param staticVertices whether vertices of this mesh are static or not. Allows for internal optimizations.
	 * @param staticIndices whether indices of this mesh are static or not. Allows for internal optimizations.
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttributes}. Each vertex attribute defines one property of a vertex such as position,
	 *           normal or texture coordinate
	 * 
	 * @author Jaroslaw Wisniewski <j.wisniewski@appsisle.com> **/
	public UniMesh (boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttributes attributes) {

		super(new UniVertexBufferObject(staticVertices, maxVertices, attributes), new UniIndexBufferObject(staticIndices, maxIndices), false);

	}
	
	public int getIndiceHandle() {
		return ((UniIndexBufferObject)indices).bufferHandle;
	}
	
	public void setIndices(int handle) {
		((UniIndexBufferObject)indices).bufferHandle = handle;
	}
	

	/** Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
	 * 
	 * @param vertices the vertices.
	 * @param offset the offset into the vertices array
	 * @param count the number of floats to use
	 * @return the mesh for invocation chaining. */
	public Mesh setVertices (FloatBuffer vertices_, int offset, int count) {
		((UniVertexBufferObject)vertices).setVertices(vertices_, offset, count);

		return this;
	}
	
	/** Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
	 * 
	 * @param vertices the vertices.
	 * @param offset the offset into the vertices array
	 * @param count the number of floats to use
	 * @return the mesh for invocation chaining. */
	public Mesh updateVertices (FloatBuffer vertices_, int offset, int count) {
		((UniVertexBufferObject)vertices).updateVertices(vertices_, offset, count);

		return this;
	}
	

	/** Sets the indices of this Mesh.
	 * 
	 * @param indices the indices
	 * @param offset the offset into the indices array
	 * @param count the number of indices to copy
	 * @return the mesh for invocation chaining. */
	public Mesh setIndices (ShortBuffer indices_, int offset, int count) {
		((UniIndexBufferObject)indices).setIndices(indices_, offset, count);

		return this;
	}

	public Mesh setIndices (IntBuffer indices_, int offset, int count) {
		((UniIndexBufferObject)indices).setIndices(indices_, offset, count);

		return this;
	}
	
	

	public Mesh updateIndices (IntBuffer indices_, int offset, int count) {
		((UniIndexBufferObject)indices).updateIndices(indices_, offset, count);

		return this;
	}
	
	
	
	
	
	@Override
	public void render (ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
		if (count == 0 || ((UniIndexBufferObject)indices).getNumIndices()==0) return;

		if (autoBind) bind(shader);

		
		Gdx.gl20.glDrawElements(primitiveType, count, ((UniIndexBufferObject)indices).intIndices ? GL20.GL_UNSIGNED_INT : GL20.GL_UNSIGNED_SHORT, offset);


		if (autoBind) unbind(shader);
	}

	public void setCapacity(int i) {
		((UniIndexBufferObject)indices).capacity = i;
	}


}
