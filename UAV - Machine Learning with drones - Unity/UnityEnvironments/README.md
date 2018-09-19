# Environments for UnityDroneSim  

---

## How to Use

This package is meant to be used to supplement the UnityDroneSim. After cloning the repository, the workflow is as follows for creating a new environment. Note that the UnityDroneSim exists as a submodule (path: submodules/DroneSim). It is symlinked into the Assets/DroneSim.

1. Create a terrain and then a scene to represent the environment. Terrains go in the Assets/Terrain folder. You can also duplicate the Terrain data of another scene's Terrain object and just drag that data into your scene and edit that.

2. Drag and drop the Assets/DroneSim/Prefabs/DroneBase.prefab into your scene, and set the Desired_height field in the inspector for the newly added DroneBase/DroneParent GameObject in the hierarchy (You may have to scroll down a bit). In addition, set the initial_height field in the same game object to be the height you want to drone to START at. You shouldn't need to move the drone at all after importing, but you can change its rotation to match the direction it should start facing by just rotating the whole DroneBase object.

3. Create a Start and End Region (as game objects), and link them to the DroneBase/DroneParent under the "Drone Agent (Script)" component in the Inspector.

4. Save the scene to "Assets/Final Scenes/" when you are done editing it and once you have run it to verify that it works.
