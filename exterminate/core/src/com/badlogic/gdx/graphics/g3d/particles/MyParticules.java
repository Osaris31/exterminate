package com.badlogic.gdx.graphics.g3d.particles;

public class MyParticules {
	public static float deltaTimeS=ParticleController.DEFAULT_TIME_STEP, deltaTimeSqrS=ParticleController.DEFAULT_TIME_STEP*ParticleController.DEFAULT_TIME_STEP;
	/**Sets the delta used to step the simulation */
	public static void setTimeStepS (float timeStep) {
		deltaTimeS = Math.max(Math.min(timeStep, 1f/45f), 1f/250f);
		deltaTimeSqrS = deltaTimeS*deltaTimeS;
	}

	public static void setTimeStepNoMinMaxS (float timeStep) {
		deltaTimeS = timeStep;
		deltaTimeSqrS = deltaTimeS*deltaTimeS;
	}

}
