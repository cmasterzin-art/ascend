"""Render the original Northborn control plates.

Run with Blender 4.3: blender -b -t 4 -P docs/art/tools/forged_controls.py -- /tmp/northborn-plates
Resize to 384 × 96 with Lanczos and export WebP quality 90 to drawable-nodpi.
"""
from math import cos, sin, tau
from pathlib import Path
import sys

import bpy
from mathutils import Vector


def alloy(name, shadow, highlight, metal, roughness, scale=12):
    material = bpy.data.materials.new(name)
    material.use_nodes = True
    nodes = material.node_tree.nodes
    link = material.node_tree.links.new
    coordinates = nodes.new("ShaderNodeTexCoord")
    oxidation = nodes.new("ShaderNodeTexNoise")
    oxidation.inputs["Scale"].default_value = scale
    oxidation.inputs["Detail"].default_value = 4
    link(coordinates.outputs["Object"], oxidation.inputs["Vector"])

    colors = nodes.new("ShaderNodeValToRGB")
    colors.color_ramp.elements[0].position = .22
    colors.color_ramp.elements[0].color = (*shadow, 1)
    colors.color_ramp.elements[1].position = .81
    colors.color_ramp.elements[1].color = (*highlight, 1)
    link(oxidation.outputs["Fac"], colors.inputs[0])

    pits = nodes.new("ShaderNodeTexVoronoi")
    pits.inputs["Scale"].default_value = 55
    link(coordinates.outputs["Object"], pits.inputs["Vector"])
    bump = nodes.new("ShaderNodeBump")
    bump.inputs["Strength"].default_value = .28
    bump.inputs["Distance"].default_value = .045
    link(pits.outputs["Distance"], bump.inputs["Height"])

    shader = nodes.get("Principled BSDF")
    shader.inputs["Metallic"].default_value = metal
    shader.inputs["Roughness"].default_value = roughness
    link(colors.outputs["Color"], shader.inputs["Base Color"])
    link(bump.outputs["Normal"], shader.inputs["Normal"])
    return material


def outline(width, height, cut):
    x, y = width / 2, height / 2
    return [
        (-x + cut, -y), (x - cut, -y), (x, -y + cut), (x, y - cut),
        (x - cut, y), (-x + cut, y), (-x, y - cut), (-x, -y + cut),
    ]


def plate(name, width, height, cut, bottom, top, bevel, material, opening=None):
    outer = outline(width, height, cut)
    points = [(px, py, z) for z in (bottom, top) for px, py in outer]
    if opening:
        inner = outline(*opening)
        points.extend((px, py, z) for z in (bottom, top) for px, py in inner)
        faces = []
    else:
        faces = [tuple(reversed(range(8))), tuple(range(8, 16))]
    for index in range(8):
        adjoining = (index + 1) % 8
        faces.append((index, adjoining, adjoining + 8, index + 8))
        if opening:
            faces.extend([
                (adjoining + 16, index + 16, index + 24, adjoining + 24),
                (index + 8, adjoining + 8, adjoining + 24, index + 24),
                (adjoining, index, index + 16, adjoining + 16),
            ])
    mesh = bpy.data.meshes.new(name)
    mesh.from_pydata(points, [], faces)
    mesh.update()
    piece = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(piece)
    piece.data.materials.append(material)
    edge = piece.modifiers.new("Forged shoulder", "BEVEL")
    edge.width = bevel
    edge.segments = 4
    piece.modifiers.new("Face normals", "WEIGHTED_NORMAL")
    return piece


def light(name, xyz, energy, color, size):
    bpy.ops.object.light_add(type="AREA", location=xyz)
    source = bpy.context.object
    source.name = name
    source.data.energy = energy
    source.data.color = color
    source.data.size = size
    source.rotation_euler = (-Vector(xyz)).to_track_quat("-Z", "Y").to_euler()


def inlay(x, material):
    # A twisted ring made from two hammered cords. Used only for ritual rewards.
    for phase in (0, tau / 2):
        curve = bpy.data.curves.new("Twisted casting", "CURVE")
        curve.dimensions = "3D"
        curve.bevel_depth = .018
        curve.bevel_resolution = 2
        wire = curve.splines.new("POLY")
        steps = 96
        wire.points.add(steps - 1)
        wire.use_cyclic_u = True
        for index, point in enumerate(wire.points):
            angle = tau * index / steps
            twist = angle * 8 + phase
            radius = .22 + .026 * cos(twist)
            point.co = (x + radius * cos(angle), radius * sin(angle), .27 + .027 * sin(twist), 1)
        braid = bpy.data.objects.new("Terminal inlay", curve)
        bpy.context.collection.objects.link(braid)
        braid.data.materials.append(material)


bpy.ops.wm.read_factory_settings(use_empty=True)
light("Cold daylight", (-2, 3.6, 5), 610, (.76, .86, 1), 5.5)
light("Hearth reflection", (2.8, -3, 4), 270, (1, .74, .50), 3.4)
light("Soft overhead", (0, 0, 8), 240, (1, .95, .88), 7.5)
bpy.ops.object.camera_add(location=(0, 0, 9), rotation=(0, 0, 0))
scene = bpy.context.scene
scene.camera = bpy.context.object
scene.camera.data.type = "ORTHO"
scene.camera.data.ortho_scale = 8
scene.render.engine = "CYCLES"
scene.cycles.samples = 72
scene.cycles.use_denoising = False
scene.render.film_transparent = True
scene.render.resolution_x = 768
scene.render.resolution_y = 192
scene.render.resolution_percentage = 100
scene.view_settings.view_transform = "AgX"
scene.render.image_settings.color_mode = "RGBA"

directory = Path(sys.argv[sys.argv.index("--") + 1])
directory.mkdir(parents=True, exist_ok=True)
iron = alloy("Blackened iron", (.012, .014, .016), (.10, .12, .14), .60, .53)
bronze = alloy("Worn copper alloy", (.076, .035, .015), (.48, .26, .11), .71, .43)
bright = alloy("Burnished brass inlay", (.17, .078, .036), (.61, .37, .17), .68, .39)
pewter = alloy("Satin pewter", (.36, .32, .26), (.78, .71, .60), .25, .60)
slate = alloy("Oiled dark face", (.006, .008, .009), (.029, .035, .040), .22, .65)
oak = alloy("Smoked oak face", (.013, .006, .003), (.076, .033, .014), .05, .69, 4)
verdigris = alloy("Patinated copper", (.010, .030, .023), (.032, .082, .055), .31, .66)
oxide = alloy("Iron oxide", (.041, .012, .009), (.128, .036, .022), .20, .65)

for variant, rim, face in [
    ("bronze", bronze, oak),
    ("iron", iron, slate),
    ("pewter", bronze, pewter),
    ("ritual", bronze, slate),
    ("patina", iron, verdigris),
    ("oxide", iron, oxide),
]:
    prior = set(bpy.data.objects)
    plate("Seated backing", 7.96, 1.96, .24, 0, .16, .096, iron)
    plate("Raised rim", 7.88, 1.87, .23, .11, .28, .055, rim, opening=(7.54, 1.53, .16))
    plate("Recessed face", 7.63, 1.62, .17, .16, .219, .024, face)
    if variant in ("bronze", "ritual"):
        for sign in (-1, 1):
            terminal = plate("Terminal", .54, 1.08, .17, .23, .30, .068, rim)
            terminal.location.x = sign * 3.53
            if variant == "ritual":
                inlay(sign * 3.53, bright)
            else:
                bpy.ops.mesh.primitive_uv_sphere_add(segments=20, ring_count=12, radius=.076,
                                                    location=(sign * 3.53, 0, .30))
                pin = bpy.context.object
                pin.scale.z = .45
                pin.data.materials.append(bright)

    scene.render.filepath = str(directory / f"forge_plate_{variant}.png")
    bpy.ops.render.render(write_still=True)
    for piece in set(bpy.data.objects) - prior:
        bpy.data.objects.remove(piece, do_unlink=True)
