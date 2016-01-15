

package net.osaris.exterminate.heightmap;


public final class WorldInfo
{

    public WorldInfo(int meters_per_world, float sea_level_meters,  float heightmap[][])
    {
       this.sea_level_meters = sea_level_meters;
        this.meters_per_world = meters_per_world;

        this.heightmap = heightmap;
     }

    public final float heightmap[][];
    public final int meters_per_world;
    public final float sea_level_meters;
 }
