package net.osaris.exterminate.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import net.osaris.exterminate.InGame;
import net.osaris.exterminate.world.Chunk;
import net.osaris.exterminate.world.World;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class EntityManager {
	public World world;

	public Hashtable<Long, Entity> managedEntities = new Hashtable<Long, Entity>();
	public Set<Entity> entitiesToAdd = Collections
			.synchronizedSet(new HashSet<Entity>());
	public Set<Entity> entitiesToRemove = Collections
			.synchronizedSet(new HashSet<Entity>());

	public EntityManager(World world2) {
		world = world2;
	}

	Vector2 tmpv2a = new Vector2();
	Vector2 tmpv2b = new Vector2();

	public void update(float deltaTime) {

		InGame.player.removeMe = false;
		managedEntities.put(InGame.player.id, InGame.player);

		Chunk newChunk;

		synchronized (entitiesToAdd) {
			for (Entity toAdd : entitiesToAdd) {
				if (toAdd != null)
					managedEntities.put(toAdd.id, toAdd);
			}

			entitiesToAdd.clear();
		}
		for (Entity entity : managedEntities.values()) {
			// System.out.println(entity+" at "+entity.pos);
			if (entity instanceof LivingEntity) {

				LivingEntity livingEntiy = (LivingEntity) entity;

				// on va despawn les ennemis trop loins, et les respawn plus
				// près
				tmpv2a.set(livingEntiy.pos.x, livingEntiy.pos.z);
				tmpv2b.set(InGame.player.pos.x, InGame.player.pos.z);

				if (livingEntiy.mustTPTo != null) {
					// System.out.println(livingEntiy.id);
					livingEntiy.pos.set(livingEntiy.mustTPTo);
					livingEntiy.previousPos.set(livingEntiy.mustTPTo);
					livingEntiy.smoothPosY = livingEntiy.pos.y;
					livingEntiy.reelPosy = livingEntiy.pos.y;
					livingEntiy.mustTPTo = null;
					livingEntiy.v.setZero();
					livingEntiy.vchoc.setZero();
					// System.out.println(livingEntiy.id
					// +" tped at "+livingEntiy.pos+" "+InGame.boy.pos);

				}
			} else if (entity.mustTPTo != null) {
				entity.pos.set(entity.mustTPTo);
				entity.previousPos.set(entity.mustTPTo);
				entity.mustTPTo = null;

			}

			newChunk = world.getChunk(entity.pos.x, entity.pos.z);
			if (newChunk != World.EMPTY && newChunk != entity.onChunk) {

				if (entity.onChunk != null) {

					for (float i = -8; i < 25; i += 16) {
						for (float j = -8; j < 25; j += 16) {
							world.getChunk(entity.onChunk.posX + i,
									entity.onChunk.posZ + j).entities
									.remove(entity);
						}
					}
				}

				for (float i = -8; i < 25; i += 16) {
					for (float j = -8; j < 25; j += 16) {
						world.getChunk(newChunk.posX + i, newChunk.posZ + j).entities
								.add(entity);
					}
				}

				if (entity instanceof LivingEntity && newChunk != World.EMPTY) {
					LivingEntity livingEntiy = (LivingEntity) entity;

					// on recalcule les chunks concerné pour enemis proches.

					if (entity.onChunk != null) {
						for (float i = -24; i < 41; i += 16) {
							for (float j = -24; j < 41; j += 16) {
								world.getChunk(entity.onChunk.posX + i,
										entity.onChunk.posZ + j).ennemisProches
										.remove(livingEntiy);
							}
						}
					}

					for (float i = -24; i < 41; i += 16) {
						for (float j = -24; j < 41; j += 16) {
							world.getChunk(newChunk.posX + i, newChunk.posZ + j).ennemisProches
									.add(livingEntiy);
						}
					}
				}

				entity.onChunk = newChunk;
			}

/*			if (entity.pos.dst2(InGame.player.pos) < 150f * 150f
					|| (entity instanceof LivingEntity
							&& ((LivingEntity) entity).multiPos != null && ((LivingEntity) entity).multiPos
							.dst2(InGame.player.pos) < 150f * 150f)) {*/
				// entity.applyFromBullet();
				try {
					if (entity.onChunk != null)
						entity.update(deltaTime);
				} catch (StackOverflowError e) {
					System.err.println("ouch!");
					e.printStackTrace();
				}
				// System.out.println("in range"+entity.pos);
				// entity.applyToBullet();
	//		} else {
	//		}

			if (entity.removeMe || entity.pos.dst2(InGame.player.pos)>1000*1000) {
				entity.removeMe = true;
				unmanage(entity);
			}

		}

		synchronized (entitiesToRemove) {
			for (Entity toRemove : entitiesToRemove) {
				// System.out.println("removed "+toRemove.id);
				toRemove.onRemove();
				if (toRemove.onChunk != null) {
					for (float i = -8; i < 25; i += 16) {
						for (float j = -8; j < 25; j += 16) {
							world.getChunk(toRemove.onChunk.posX + i,
									toRemove.onChunk.posZ + j).entities
									.remove(toRemove);
						}
					}

					if (toRemove instanceof LivingEntity) {
						LivingEntity livingEntiy = (LivingEntity) toRemove;

						for (float i = -24; i < 41; i += 16) {
							for (float j = -24; j < 41; j += 16) {
								world.getChunk(livingEntiy.onChunk.posX + i,
										livingEntiy.onChunk.posZ + j).ennemisProches
										.remove(livingEntiy);
							}
						}

					}
				}
				managedEntities.remove(toRemove.id);
			}
			entitiesToRemove.clear();
		}
		for (Entity entity : managedEntities.values()) {
			if (entity.hasPhysics && entity.onChunk != null
					&& (entity.pos.dst2(InGame.player.pos) < 150 * 150)) {
				entity.animate = true;

				for (Entity other : entity.onChunk.entities) {
					if (other.hasPhysics
							&& entity != other && entity.ownerId != other.id && entity.id != other.ownerId
							&& entity.pos.dst2(other.pos) < (5*5)) {
						
							axis.set(entity.pos).sub(other.pos);
							float dist = axis.len();
							if (dist == 0) {
								axis.set(0.1f, 0f, 0f);
							} else {
								axis.scl((5f - dist)
										/ (((!entity.canMove) || (!other.canMove)) ? 1f
												: 2f) / dist);
							}
							if (entity.canMove && other.solid)
								entity.moveByCollisions.add(axis);
							if (other.canMove && entity.solid)
								other.moveByCollisions.sub(axis);

							entity.onCollide(other);
							other.onCollide(entity);

					}
				}

			} else {
				entity.animate = false;
			}
		}

	}

	Vector3 axis = new Vector3();

	public void draw(ModelBatch modelBatch, Environment environment,
			boolean pourOmbre) {
		for (Entity entity : managedEntities.values()) {
			if (!entity.removeMe
					&& entity.onChunk != World.EMPTY
					&& entity.onChunk != null
					&& entity != InGame.player
	//Z				&& entity.pos.dst2(InGame.player.pos) < 14400f
					&& (!pourOmbre || entity.pos.dst2(InGame.player.pos) < 60f * 60f))
				entity.draw(modelBatch, environment, pourOmbre);
		}

	}

	Matrix4 tmpMat4 = new Matrix4();
	Vector3 tmpVec1 = new Vector3();
	Vector3 tmpVec2 = new Vector3();
	Vector3 tmpVec3 = new Vector3();

	public void drawOther() {
		for (Entity entity : managedEntities.values()) {
			if (!entity.removeMe && entity.onChunk != World.EMPTY
					&& entity.onChunk != null && entity != InGame.player
					&& entity.pos.dst2(InGame.player.pos) < 14400f)
				entity.drawOther();
		}

	}

	public void manage(Entity entity) {
		entity.world = world;
		entitiesToAdd.add(entity);
	}

	protected void unmanage(Entity entity) {
		entitiesToRemove.add(entity);
	}

	public void unmanage(long entityId) {
		Entity entity = managedEntities.get(entityId);

		if (entity != null && InGame.player != entity)
			entitiesToRemove.add(entity);
	}

	public LivingEntity getLivingEntity(long id) {
		Entity entity = managedEntities.get(id);
		if (entity instanceof LivingEntity) {
			return (LivingEntity) entity;
		} else {
			return null;
		}
	}

}
