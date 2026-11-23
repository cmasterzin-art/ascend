"""Render the original forged navigation clasp used by NORTHBORN.

Run: blender -b -t 2 -P docs/art/tools/engraved_clasp.py -- /tmp/engraved_clasp.png
"""
import sys

import bpy


def forged_material(name, low, high, metal, roughness):
    material = bpy.data.materials.new(name)
    material.use_nodes = True
    nodes = material.node_tree.nodes
    links = material.node_tree.links
    shader = nodes.get("Principled BSDF")
    shader.inputs["Metallic"].default_value = metal
    shader.inputs["Roughness"].default_value = roughness

    noise = nodes.new("ShaderNodeTexNoise")
    noise.inputs["Scale"].default_value = 19.0
    noise.inputs["Detail"].default_value = 3.0
    ramp = nodes.new("ShaderNodeValToRGB")
    ramp.color_ramp.elements[0].color = (*low, 1)
    ramp.color_ramp.elements[1].color = (*high, 1)
    links.new(noise.outputs["Fac"], ramp.inputs[0])
    links.new(ramp.outputs["Color"], shader.inputs["Base Color"])

    grain = nodes.new("ShaderNodeTexNoise")
    grain.inputs["Scale"].default_value = 94.0
    bump = nodes.new("ShaderNodeBump")
    bump.inputs["Strength"].default_value = 0.29
    bump.inputs["Distance"].default_value = 0.038
    links.new(grain.outputs["Fac"], bump.inputs["Height"])
    links.new(bump.outputs["Normal"], shader.inputs["Normal"])
    return material


def plate(name, width, height, chamfer, bottom, top, material):
    x, y = width / 2, height / 2
    outline = [
        (-x + chamfer, -y), (x - chamfer, -y), (x, -y + chamfer),
        (x, y - chamfer), (x - chamfer, y), (-x + chamfer, y),
        (-x, y - chamfer), (-x, -y + chamfer),
    ]
    points = [(px, py, z) for z in (bottom, top) for px, py in outline]
    faces = [tuple(reversed(range(8))), tuple(range(8, 16))]
    for index in range(8):
        adjoining = (index + 1) % 8
        faces.append((index, adjoining, adjoining + 8, index + 8))
    mesh = bpy.data.meshes.new(name)
    mesh.from_pydata(points, [], faces)
    mesh.update()
    obj = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(obj)
    obj.data.materials.append(material)
    bevel = obj.modifiers.new("Hammered bevel", "BEVEL")
    bevel.width = 0.075
    bevel.segments = 3
    obj.modifiers.new("Weighted normals", "WEIGHTED_NORMAL")


def area(name, location, energy, color, size):
    from mathutils import Vector

    bpy.ops.object.light_add(type="AREA", location=location)
    source = bpy.context.object
    source.name = name
    source.data.energy = energy
    source.data.color = color
    source.data.size = size
    source.rotation_euler = (-Vector(location)).to_track_quat("-Z", "Y").to_euler()


bpy.ops.wm.read_factory_settings(use_empty=True)
bronze = forged_material("Patinated copper", (.09, .035, .019), (.34, .18, .073), .72, .38)
iron = forged_material("Oiled deep iron", (.017, .021, .025), (.059, .068, .077), .65, .48)
fillet = forged_material("Cold iron fillet", (.044, .035, .028), (.18, .13, .082), .76, .34)

plate("Copper backing", 4.48, 2.82, .46, .0, .24, bronze)
plate("Recessed dark iron", 4.16, 2.51, .40, .19, .31, iron)
plate("Inner ridge", 3.78, 2.15, .31, .29, .38, fillet)
plate("Surface for the engraved icon", 3.62, 1.98, .29, .32, .41, iron)
for x, y in [(-1.76, -.86), (1.76, -.86), (-1.76, .86), (1.76, .86)]:
    bpy.ops.mesh.primitive_uv_sphere_add(segments=16, ring_count=8, radius=.081, location=(x, y, .34))
    nail = bpy.context.object
    nail.scale.z = .44
    nail.data.materials.append(bronze)

area("Warm lateral light", (-3, -2, 5), 590, (1.0, .71, .49), 3.6)
area("Cold broad fill", (2, 4, 5), 510, (.59, .74, 1.0), 4.0)
area("Overhead light", (0, -1, 7), 160, (1.0, .9, .75), 3.0)
bpy.ops.object.camera_add(location=(0, 0, 8), rotation=(0, 0, 0))
scene = bpy.context.scene
scene.camera = bpy.context.object
scene.camera.data.type = "ORTHO"
scene.camera.data.ortho_scale = 4.9
scene.render.engine = "CYCLES"
scene.cycles.samples = 48
scene.cycles.use_denoising = False
scene.render.film_transparent = True
scene.render.resolution_x = 384
scene.render.resolution_y = 256
scene.render.resolution_percentage = 100
scene.view_settings.view_transform = "AgX"
scene.render.image_settings.color_mode = "RGBA"
scene.render.filepath = sys.argv[sys.argv.index("--") + 1]
bpy.ops.render.render(write_still=True)
