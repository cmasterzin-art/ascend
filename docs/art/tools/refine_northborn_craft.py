"""ASCEND 1.7.5.9.3 — NORTHBORN 4.2 Supersampled Nordic Craft Asset Refiner.

Generates museum-grade, 3x-supersampled satin Scandinavian metalwork plates,
active navigation clasps (dark obsidian-bronze and light parchment-bronze),
widget 9-patches, de-fringed/color-graded engraved rails, and refined iron/vellum
surfaces while reading clean source textures idempotently from git base e147cfd.

Run:
    uv run --with pillow --with numpy python3 docs/art/tools/refine_northborn_craft.py
"""
from __future__ import annotations

import io
import math
import subprocess
from pathlib import Path
from typing import Tuple

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[3]
RES = ROOT / "app" / "src" / "main" / "res"
NODPI = RES / "drawable-nodpi"
BASE_COMMIT = "e147cfd"


def load_base_image(rel_path: str) -> Image.Image:
    """Load clean original drawable from git commit e147cfd so re-runs are idempotent."""
    raw = subprocess.check_output(["git", "show", f"{BASE_COMMIT}:{rel_path}"], cwd=ROOT)
    return Image.open(io.BytesIO(raw))


def chamfer_sdf(xx: np.ndarray, yy: np.ndarray, hw: float, hh: float, cut: float) -> np.ndarray:
    """Signed distance to a symmetric chamfered rectangle centered at (0, 0). Negative = inside."""
    ax = np.abs(xx)
    ay = np.abs(yy)
    d_box = np.maximum(ax - hw, ay - hh)
    # 45-degree chamfer plane: (ax - (hw - cut)) + (ay - (hh - cut)) <= cut
    d_chamfer = ((ax + ay) - (hw + hh - cut)) * (1.0 / math.sqrt(2.0))
    return np.maximum(d_box, d_chamfer)


def smoothstep(edge0: float, edge1: float, x: np.ndarray) -> np.ndarray:
    t = np.clip((x - edge0) / max(edge1 - edge0, 1e-6), 0.0, 1.0)
    return t * t * (3.0 - 2.0 * t)


def brushed_noise(w: int, h: int, seed: int, horiz_sigma: float = 18.0, vert_sigma: float = 0.7) -> np.ndarray:
    """Create fine anisotropic satin-brushed horizontal metal grain in [-1, 1]."""
    rng = np.random.default_rng(seed)
    base = rng.normal(0.0, 1.0, (h, w)).astype(np.float32)
    # Stretch horizontally via 1D separable box/Gaussian approximation using PIL blur on scaled grid
    small_w = max(8, int(w / max(horiz_sigma * 0.45, 1.0)))
    small_h = max(8, int(h / max(vert_sigma * 1.0, 1.0)))
    coarse = rng.normal(0.0, 1.0, (small_h, small_w)).astype(np.float32)
    coarse_u8 = np.clip((coarse * 42.0 + 128.0), 0, 255).astype(np.uint8)
    streaks = np.asarray(
        Image.fromarray(coarse_u8, mode="L").resize((w, h), Image.Resampling.BICUBIC),
        dtype=np.float32,
    )
    streaks = (streaks - 128.0) / 42.0

    # Micro satin shimmer (horizontal 1-row variation + subtle fine grain)
    row_bias = rng.normal(0.0, 0.35, (h, 1)).astype(np.float32)
    fine = rng.normal(0.0, 0.22, (h, w)).astype(np.float32)
    combined = 0.62 * streaks + 0.26 * row_bias + 0.12 * fine
    std = float(np.std(combined)) + 1e-6
    return np.clip(combined / (std * 2.2), -1.0, 1.0)


def hex_rgb(value: str) -> np.ndarray:
    value = value.lstrip("#")
    return np.array([int(value[i : i + 2], 16) for i in (0, 2, 4)], dtype=np.float32) / 255.0


def render_forge_plate(
    *,
    name: str,
    face_top: str,
    face_bottom: str,
    rim_top: str,
    rim_mid: str,
    rim_bottom: str,
    fillet_color: str,
    fillet_alpha: float,
    grain_strength: float,
    seed: int,
    light_mode: bool = False,
    pin_style: str = "none",  # "none", "bronze", "ritual"
    pin_color: str = "#E6CA95",
) -> Image.Image:
    """Render a 3x-supersampled satin Nordic forged plate (1152x288 -> 384x96)."""
    scale = 3
    w, h = 384 * scale, 96 * scale
    pad = 4 * scale  # clean transparent anti-aliased edge margin

    y_idx, x_idx = np.mgrid[0:h, 0:w].astype(np.float32)
    cx, cy = (w - 1) * 0.5, (h - 1) * 0.5
    xx = x_idx - cx
    yy = y_idx - cy

    hw = w * 0.5 - pad
    hh = h * 0.5 - pad
    cut = 18.0  # 6px chamfer at 1x

    d_outer = chamfer_sdf(xx, yy, hw, hh, cut)
    rim_w = 8.5  # ~2.8px refined metallic bezel at 1x
    d_inner = chamfer_sdf(xx, yy, hw - rim_w, hh - rim_w, max(cut - rim_w * 0.72, 4.0))

    # Smooth outer alpha (anti-aliased, zero harsh black halo)
    alpha = 1.0 - smoothstep(-1.1, 1.1, d_outer)

    # Normalized vertical & horizontal coordinates in [-1, 1]
    ny = np.clip(yy / max(hh, 1.0), -1.0, 1.0)
    nx = np.clip(xx / max(hw, 1.0), -1.0, 1.0)
    ty = (ny + 1.0) * 0.5  # 0 at top, 1 at bottom

    # Brushed metal grain
    grain = brushed_noise(w, h, seed=seed, horiz_sigma=26.0, vert_sigma=1.0)

    # 1. Face surface (subtle vertical bevel + gentle center overhead skylight + fine brushed grain)
    c_ft = hex_rgb(face_top)
    c_fb = hex_rgb(face_bottom)
    face = c_ft[None, None, :] * (1.0 - ty[:, :, None]) + c_fb[None, None, :] * ty[:, :, None]

    # Subtle radial overhead sheen (calm Scandinavian craft light)
    radial = np.exp(-(nx * nx * 1.15 + (ny + 0.35) ** 2 * 1.8))
    sheen_amt = 0.055 if not light_mode else 0.045
    face = face + radial[:, :, None] * sheen_amt

    # Apply subtle brushed grain
    face = face + grain[:, :, None] * grain_strength

    # Subtle recessed step shadow right inside the rim (gives authentic carved depth)
    recess_shadow = np.exp(-np.maximum(-d_inner, 0.0) / 5.5) * (0.14 if not light_mode else 0.06)
    # Top inner rim catches a crisp highlight on light mode or subtle bright bevel on dark mode
    top_inner_mask = np.exp(-np.maximum(-d_inner, 0.0) / 3.2) * np.clip(-ny, 0.0, 1.0) ** 2.2
    face = face * (1.0 - recess_shadow[:, :, None])
    if light_mode:
        face = face + top_inner_mask[:, :, None] * 0.09
    else:
        face = face + top_inner_mask[:, :, None] * 0.055

    # 2. Refined metallic rim (directional cold-boreal top specular -> mid alloy -> shaded bottom)
    c_rt = hex_rgb(rim_top)
    c_rm = hex_rgb(rim_mid)
    c_rb = hex_rgb(rim_bottom)
    top_w = np.clip(1.0 - ty * 1.85, 0.0, 1.0)
    bot_w = np.clip((ty - 0.45) * 1.82, 0.0, 1.0)
    mid_w = np.clip(1.0 - top_w - bot_w, 0.0, 1.0)
    rim = (
        c_rt[None, None, :] * top_w[:, :, None]
        + c_rm[None, None, :] * mid_w[:, :, None]
        + c_rb[None, None, :] * bot_w[:, :, None]
    )
    # Fine metallic brush along the rim too
    rim = rim + grain[:, :, None] * (grain_strength * 0.75)

    # Crisp 1px top specular glinting edge right along the upper outer chamfer
    rim_outer_edge = np.exp(-((d_outer + 2.2) ** 2) / 3.8)
    rim_spec = rim_outer_edge * np.clip(0.55 - ny * 0.75, 0.0, 1.0)
    rim = rim + rim_spec[:, :, None] * (0.18 if not light_mode else 0.14)

    # Blend rim and face smoothly across d_inner = 0
    is_face = 1.0 - smoothstep(-1.0, 1.0, d_inner)
    rgb = rim * (1.0 - is_face[:, :, None]) + face * is_face[:, :, None]

    # 3. Whisper-thin architectural inner incised fillet line
    fillet_inset = rim_w + 10.5
    d_fillet = chamfer_sdf(xx, yy, hw - fillet_inset, hh - fillet_inset, max(cut - 6.0, 3.0))
    fillet_band = np.exp(-(d_fillet ** 2) / 1.35) * is_face
    c_fil = hex_rgb(fillet_color)
    # Slightly brighter fillet on upper edge, quieter on bottom
    fillet_mod = fillet_alpha * (0.72 + 0.38 * np.clip(-ny, 0.0, 1.0))
    f_blend = (fillet_band * fillet_mod)[:, :, None]
    rgb = rgb * (1.0 - f_blend) + c_fil[None, None, :] * f_blend

    # Extra ceremonial second hairline fillet on ritual
    if pin_style == "ritual":
        fillet2_inset = rim_w + 17.5
        d_f2 = chamfer_sdf(xx, yy, hw - fillet2_inset, hh - fillet2_inset, max(cut - 9.0, 2.0))
        f2_band = np.exp(-(d_f2 ** 2) / 1.15) * is_face
        f2_blend = (f2_band * (fillet_alpha * 0.52))[:, :, None]
        rgb = rgb * (1.0 - f2_blend) + c_fil[None, None, :] * f2_blend

    # 4. Precision micro-finials tucked tightly into outer 3.2% ends (zero text collision!)
    if pin_style in ("bronze", "ritual"):
        c_pin = hex_rgb(pin_color)
        c_pin_hi = np.clip(c_pin * 1.28 + 0.08, 0.0, 1.0)
        c_pin_lo = c_pin * 0.42
        pin_x = hw - rim_w - 22.0  # ~34px from edge at 3x => ~11px at 1x (well inside 18dp padding!)
        pin_r = 10.5 if pin_style == "bronze" else 13.0
        for sign in (-1.0, 1.0):
            px = xx - sign * pin_x
            py = yy
            # Crisp rotated diamond (L1 norm)
            d_dia = (np.abs(px) + np.abs(py)) - pin_r
            # Subtle recessed halo shadow under pin
            halo = np.exp(-np.maximum(d_dia, 0.0) / 4.2) * (d_dia > 0.0) * 0.30
            rgb = rgb * (1.0 - halo[:, :, None])

            # Faceted diamond fill (top-left bright facet, bottom-right shaded facet)
            pin_mask = 1.0 - smoothstep(-1.0, 1.0, d_dia)
            facet = np.clip(0.55 - (px * 0.4 + py * 0.75) / max(pin_r, 1.0), 0.0, 1.0)
            pin_rgb = c_pin_lo[None, None, :] * (1.0 - facet[:, :, None]) + c_pin_hi[None, None, :] * facet[:, :, None]
            # Crisp specular pin center
            center_dot = np.exp(-(px * px + py * py) / 12.0) * 0.30
            pin_rgb = np.clip(pin_rgb + center_dot[:, :, None], 0.0, 1.0)

            pm = pin_mask[:, :, None]
            rgb = rgb * (1.0 - pm) + pin_rgb * pm

            if pin_style == "ritual":
                # Tiny vertical filigree ticks above and below the diamond
                for ty_off in (-pin_r - 12.0, pin_r + 12.0):
                    d_tick = np.maximum(np.abs(px) - 2.0, np.abs(py - ty_off) - 6.0)
                    t_mask = (1.0 - smoothstep(-0.9, 0.9, d_tick)) * 0.75
                    rgb = rgb * (1.0 - t_mask[:, :, None]) + c_pin[None, None, :] * t_mask[:, :, None]

    # Subtle outer drop shadow only at the very bottom 1px (never muddy black outline!)
    if not light_mode:
        outer_rim_edge = smoothstep(-0.6, 0.8, d_outer) * (1.0 - smoothstep(0.8, 3.0, d_outer))
        bot_shadow = outer_rim_edge * np.clip((ny + 0.2) * 0.8, 0.0, 1.0) * 0.35
        alpha = np.clip(alpha + bot_shadow, 0.0, 1.0)
    else:
        # Very soft warm stone shadow underneath light pewter plate (warm sepia-taupe, never pure black!)
        outer_rim_edge = smoothstep(-0.4, 0.8, d_outer) * (1.0 - smoothstep(0.8, 3.2, d_outer))
        bot_shadow = outer_rim_edge * np.clip((ny + 0.1) * 0.8, 0.0, 1.0) * 0.16
        warm_shadow = hex_rgb("#7A6E5C")
        s_mask = (bot_shadow * (1.0 - alpha))[:, :, None]
        rgb = rgb * (1.0 - s_mask) + warm_shadow[None, None, :] * s_mask
        alpha = np.clip(alpha + bot_shadow, 0.0, 1.0)

    rgba = np.zeros((h, w, 4), dtype=np.uint8)
    rgba[:, :, :3] = np.clip(rgb * 255.0 + 0.5, 0, 255).astype(np.uint8)
    rgba[:, :, 3] = np.clip(alpha * 255.0 + 0.5, 0, 255).astype(np.uint8)

    hi = Image.fromarray(rgba, mode="RGBA")
    return hi.resize((384, 96), Image.Resampling.LANCZOS)


def render_clasp(
    *,
    face_top: str,
    face_bottom: str,
    rim_top: str,
    rim_mid: str,
    rim_bottom: str,
    fillet_color: str,
    fillet_alpha: float,
    seed: int,
    light_mode: bool = False,
) -> Image.Image:
    """Render a sleek 3x-supersampled chamfered navigation clasp (256x171 RGBA)."""
    scale = 3
    w, h = 256 * scale, 171 * scale
    pad = 8 * scale

    y_idx, x_idx = np.mgrid[0:h, 0:w].astype(np.float32)
    cx, cy = (w - 1) * 0.5, (h - 1) * 0.5
    xx = x_idx - cx
    yy = y_idx - cy
    hw = w * 0.5 - pad
    hh = h * 0.5 - pad
    cut = 38.0  # refined octagonal chamfer

    d_outer = chamfer_sdf(xx, yy, hw, hh, cut)
    rim_w = 11.0  # ~3.6px refined rim at 1x (much sleeker than thick bulky copper)
    d_inner = chamfer_sdf(xx, yy, hw - rim_w, hh - rim_w, cut - rim_w * 0.72)

    alpha = 1.0 - smoothstep(-1.2, 1.2, d_outer)
    ny = np.clip(yy / max(hh, 1.0), -1.0, 1.0)
    nx = np.clip(xx / max(hw, 1.0), -1.0, 1.0)
    ty = (ny + 1.0) * 0.5

    grain = brushed_noise(w, h, seed=seed, horiz_sigma=18.0, vert_sigma=1.2)

    c_ft, c_fb = hex_rgb(face_top), hex_rgb(face_bottom)
    face = c_ft[None, None, :] * (1.0 - ty[:, :, None]) + c_fb[None, None, :] * ty[:, :, None]
    radial = np.exp(-(nx * nx * 1.1 + (ny + 0.25) ** 2 * 1.4))
    face = face + radial[:, :, None] * (0.07 if not light_mode else 0.05) + grain[:, :, None] * 0.015

    # Inner recessed shadow
    recess = np.exp(-np.maximum(-d_inner, 0.0) / 8.0) * (0.16 if not light_mode else 0.06)
    face = face * (1.0 - recess[:, :, None])

    c_rt, c_rm, c_rb = hex_rgb(rim_top), hex_rgb(rim_mid), hex_rgb(rim_bottom)
    top_w = np.clip(1.0 - ty * 1.85, 0.0, 1.0)
    bot_w = np.clip((ty - 0.45) * 1.82, 0.0, 1.0)
    mid_w = np.clip(1.0 - top_w - bot_w, 0.0, 1.0)
    rim = (
        c_rt[None, None, :] * top_w[:, :, None]
        + c_rm[None, None, :] * mid_w[:, :, None]
        + c_rb[None, None, :] * bot_w[:, :, None]
    ) + grain[:, :, None] * 0.014

    is_face = 1.0 - smoothstep(-1.0, 1.0, d_inner)
    rgb = rim * (1.0 - is_face[:, :, None]) + face * is_face[:, :, None]

    # Delicate inner engraved fillet
    d_fil = chamfer_sdf(xx, yy, hw - rim_w - 14.0, hh - rim_w - 14.0, cut - 14.0)
    f_band = np.exp(-(d_fil ** 2) / 2.2) * is_face
    c_fil = hex_rgb(fillet_color)
    f_blend = (f_band * fillet_alpha)[:, :, None]
    rgb = rgb * (1.0 - f_blend) + c_fil[None, None, :] * f_blend

    # Tiny centered top & bottom architectural micro-dots on the rim
    for sy in (-1.0, 1.0):
        dy = yy - sy * (hh - rim_w * 0.5)
        dot = np.exp(-(xx * xx + dy * dy) / 20.0) * 0.45
        rgb = np.clip(rgb + dot[:, :, None] * c_rt[None, None, :], 0.0, 1.0)

    rgba = np.zeros((h, w, 4), dtype=np.uint8)
    rgba[:, :, :3] = np.clip(rgb * 255.0 + 0.5, 0, 255).astype(np.uint8)
    rgba[:, :, 3] = np.clip(alpha * 255.0 + 0.5, 0, 255).astype(np.uint8)
    return Image.fromarray(rgba, mode="RGBA").resize((256, 171), Image.Resampling.LANCZOS)


def make_nine_patch(image: Image.Image) -> Image.Image:
    """Create Android 9-patch matching ForgedControls.kt (32px end caps, 14px top/bottom lip)."""
    w, h = image.size
    target = Image.new("RGBA", (w + 2, h + 2), (0, 0, 0, 0))
    target.paste(image, (1, 1))
    marks = ImageDraw.Draw(target)
    marks.line((33, 0, w - 32, 0), fill=(0, 0, 0, 255))
    marks.line((0, 15, 0, h - 14), fill=(0, 0, 0, 255))
    marks.line((1, h + 1, w, h + 1), fill=(0, 0, 0, 255))
    marks.line((w + 1, 1, w + 1, h), fill=(0, 0, 0, 255))
    return target


def defringe_white_halo(im: Image.Image, target_edge_rgb: Tuple[float, float, float], tighten: float = 1.0) -> Image.Image:
    """Cleanly remove bright white background cutout halos from transparent carved dividers."""
    arr = np.asarray(im.convert("RGBA"), dtype=np.float32) / 255.0
    rgb = arr[:, :, :3]
    a = arr[:, :, 3]

    # Luminance & saturation
    cmax = np.max(rgb, axis=2)
    cmin = np.min(rgb, axis=2)
    lum = 0.299 * rgb[:, :, 0] + 0.587 * rgb[:, :, 1] + 0.114 * rgb[:, :, 2]
    sat = (cmax - cmin) / np.maximum(cmax, 1e-5)

    # Identify semi-transparent or bright neutral-white halo pixels
    white_halo = np.clip((lum - 0.62) / 0.30, 0.0, 1.0) * np.clip((0.28 - sat) / 0.24, 0.0, 1.0)
    partial = np.clip((0.92 - a) / 0.92, 0.0, 1.0)

    # Pull fringe RGB out of white matting and drop white-fuzz alpha
    halo_strength = np.clip(white_halo * (0.65 + 0.35 * partial) * tighten, 0.0, 1.0)
    t_rgb = np.array(target_edge_rgb, dtype=np.float32)[None, None, :]
    rgb_clean = rgb * (1.0 - halo_strength[:, :, None] * 0.72) + t_rgb * (halo_strength[:, :, None] * 0.72)

    # For partial pixels blended over white, un-premultiply white contamination
    a_safe = np.maximum(a, 0.08)
    unmatted = np.clip((rgb - (1.0 - a[:, :, None]) * 0.95) / a_safe[:, :, None], 0.0, 1.0)
    use_unmat = ((a > 0.05) & (a < 0.92) & (lum > 0.55))[:, :, None]
    rgb_clean = np.where(use_unmat, 0.55 * unmatted + 0.45 * rgb_clean, rgb_clean)

    # Clean stray dust specks & soften white fuzz alpha
    a_clean = a * (1.0 - white_halo * 0.82 * np.clip(0.55 + 0.45 * partial, 0.0, 1.0))
    a_clean = np.where(a_clean < 0.06, 0.0, a_clean)

    out = np.zeros_like(arr)
    out[:, :, :3] = np.clip(rgb_clean, 0.0, 1.0)
    out[:, :, 3] = np.clip(a_clean, 0.0, 1.0)
    return Image.fromarray((out * 255.0 + 0.5).astype(np.uint8), mode="RGBA")


def refine_engraved_wool() -> Image.Image:
    """Transform loud terracotta-orange wool braid into refined Nordic aged bronze-walnut tablet weave."""
    im = load_base_image("app/src/main/res/drawable-nodpi/engraved_wool.webp")
    clean = defringe_white_halo(im, target_edge_rgb=(0.18, 0.14, 0.11), tighten=1.25)

    arr = np.asarray(clean, dtype=np.float32) / 255.0
    r, g, b, a = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2], arr[:, :, 3]

    # Luminance-preserving regrade from loud orange/red into authentic smoked walnut + aged Nordic bronze thread
    lum = 0.299 * r + 0.587 * g + 0.114 * b
    # Gently compress harsh contrast and shift chroma from red-orange to muted antique bronze-ochre
    new_r = np.clip(lum * 1.08 + (r - lum) * 0.40 - 0.02, 0.0, 1.0)
    new_g = np.clip(lum * 0.96 + (g - lum) * 0.35 + 0.015, 0.0, 1.0)
    new_b = np.clip(lum * 0.78 + (b - lum) * 0.25 + 0.025, 0.0, 1.0)

    # Subtle darkening of red-brown warp so bronze chevron weave shines calmly
    warp = np.clip((0.42 - lum) / 0.32, 0.0, 1.0)
    new_r = new_r * (1.0 - warp * 0.28)
    new_g = new_g * (1.0 - warp * 0.20)
    new_b = new_b * (1.0 - warp * 0.12)

    # Trim top/bottom stray fuzzy threads cleanly (3px vertical soft clip) and taper horizontal ends gently
    h, w = a.shape
    y = np.linspace(0.0, 1.0, h)[:, None]
    x = np.linspace(0.0, 1.0, w)[None, :]
    v_mask = smoothstep(0.05, 0.14, y) * smoothstep(0.05, 0.14, 1.0 - y)
    h_mask = smoothstep(0.0, 0.03, x) * smoothstep(0.0, 0.03, 1.0 - x)
    a_out = np.clip(a * v_mask * h_mask * 0.94, 0.0, 1.0)

    out = np.stack([new_r, new_g, new_b, a_out], axis=2)
    return Image.fromarray((np.clip(out, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8), mode="RGBA")


def refine_engraved_oak() -> Image.Image:
    """De-fringe white cutout specks from the carved Viking Urnes oak divider and enrich depth."""
    im = load_base_image("app/src/main/res/drawable-nodpi/engraved_oak.webp")
    clean = defringe_white_halo(im, target_edge_rgb=(0.24, 0.18, 0.12), tighten=1.35)
    arr = np.asarray(clean, dtype=np.float32) / 255.0
    rgb = arr[:, :, :3]
    lum = 0.299 * rgb[:, :, 0] + 0.587 * rgb[:, :, 1] + 0.114 * rgb[:, :, 2]
    # Soften overly harsh contrast slightly into calm museum-lit smoked oak & antique bronze
    rgb = rgb * 0.92 + lum[:, :, None] * 0.06
    arr[:, :, :3] = np.clip(rgb, 0.0, 1.0)
    return Image.fromarray((arr * 255.0 + 0.5).astype(np.uint8), mode="RGBA")


def refine_engraved_spine() -> Image.Image:
    """De-fringe white cutout specks from the engraved antique-brass folio divider."""
    im = load_base_image("app/src/main/res/drawable-nodpi/engraved_spine.webp")
    clean = defringe_white_halo(im, target_edge_rgb=(0.30, 0.25, 0.17), tighten=1.30)
    arr = np.asarray(clean, dtype=np.float32) / 255.0
    # Gentle warm antique-bronze harmonization
    arr[:, :, 0] = np.clip(arr[:, :, 0] * 1.02, 0.0, 1.0)
    arr[:, :, 1] = np.clip(arr[:, :, 1] * 0.98, 0.0, 1.0)
    arr[:, :, 2] = np.clip(arr[:, :, 2] * 0.91, 0.0, 1.0)
    return Image.fromarray((arr * 255.0 + 0.5).astype(np.uint8), mode="RGBA")


def refine_surface_iron() -> Image.Image:
    """Keep the authentic hammered dark volcanic iron center while removing the lumpy orange rim & stretched rivets."""
    im = load_base_image("app/src/main/res/drawable-nodpi/surface_iron.webp").convert("RGB")
    w, h = im.size
    # Crop strictly inside the lumpy bright-copper rim and side studs
    cropped = im.crop((int(w * 0.095), int(h * 0.105), int(w * 0.905), int(h * 0.895)))
    resized = cropped.resize((w, h), Image.Resampling.LANCZOS)

    arr = np.asarray(resized, dtype=np.float32) / 255.0
    # Cool & unify hammered iron toward matte fjord slate-iron (#171B21 - #242A33)
    lum = 0.299 * arr[:, :, 0] + 0.587 * arr[:, :, 1] + 0.114 * arr[:, :, 2]
    # Remove any lingering warm orange cast on the hammered iron face
    r = lum * 0.88 + arr[:, :, 0] * 0.12
    g = lum * 0.94 + arr[:, :, 1] * 0.06
    b = lum * 1.06 + arr[:, :, 2] * 0.06

    # Lift deep blacks slightly and apply subtle top-center skylight gradient + gentle edge vignette
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    nx = (xx / (w - 1)) * 2.0 - 1.0
    ny = (yy / (h - 1)) * 2.0 - 1.0
    skylight = np.exp(-(nx * nx * 0.8 + (ny + 0.4) ** 2 * 1.2)) * 0.045
    vignette = 1.0 - (nx * nx * 0.14 + ny * ny * 0.18)

    base = np.stack([r, g, b], axis=2)
    # Tame contrast for serene readability of overlaid typography
    base = 0.085 + base * 0.52
    base = np.clip((base + skylight[:, :, None]) * vignette[:, :, None], 0.0, 1.0)
    return Image.fromarray((base * 255.0 + 0.5).astype(np.uint8), mode="RGB")


def refine_surface_vellum() -> Image.Image:
    """Keep the stitched leather spine & vellum grain while removing harsh dark jagged outer borders."""
    im = load_base_image("app/src/main/res/drawable-nodpi/surface_vellum.webp").convert("RGB")
    w, h = im.size
    # Crop out the black ragged top/bottom/right borders while preserving the full stitched left binding
    cropped = im.crop((2, 10, w - 12, h - 10))
    resized = cropped.resize((w, h), Image.Resampling.LANCZOS)

    arr = np.asarray(resized, dtype=np.float32) / 255.0
    # Gently lift & even out the parchment area (x > 11%) so text reads luxuriously with zero blotches
    x = np.linspace(0.0, 1.0, w)[None, :]
    page_mask = smoothstep(0.095, 0.14, x)[:, :, None]
    warm_vellum = hex_rgb("#EFE2C8")[None, None, :]
    arr = arr * (1.0 - page_mask * 0.34) + warm_vellum * (page_mask * 0.34)
    return Image.fromarray((np.clip(arr, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8), mode="RGB")


def main() -> None:
    specs = [
        dict(
            name="bronze",
            face_top="#4D3B28",
            face_bottom="#36281A",
            rim_top="#DFC28C",
            rim_mid="#AD8651",
            rim_bottom="#6C512D",
            fillet_color="#E6CA96",
            fillet_alpha=0.30,
            grain_strength=0.017,
            seed=4101,
            light_mode=False,
            pin_style="bronze",
            pin_color="#E4C58D",
        ),
        dict(
            name="ritual",
            face_top="#232831",
            face_bottom="#14181F",
            rim_top="#EBD19D",
            rim_mid="#BC965B",
            rim_bottom="#735830",
            fillet_color="#EBD19D",
            fillet_alpha=0.40,
            grain_strength=0.015,
            seed=4102,
            light_mode=False,
            pin_style="ritual",
            pin_color="#EDD4A2",
        ),
        dict(
            name="iron",
            face_top="#262B34",
            face_bottom="#1B1F26",
            rim_top="#5E6A7C",
            rim_mid="#414A58",
            rim_bottom="#282E38",
            fillet_color="#9AA8BA",
            fillet_alpha=0.16,
            grain_strength=0.014,
            seed=4103,
            light_mode=False,
            pin_style="none",
        ),
        dict(
            name="pewter",
            face_top="#F5F0E6",
            face_bottom="#E7DFD1",
            rim_top="#D3C6B2",
            rim_mid="#B9AA92",
            rim_bottom="#96856D",
            fillet_color="#8E7554",
            fillet_alpha=0.20,
            grain_strength=0.010,
            seed=4104,
            light_mode=True,
            pin_style="none",
        ),
        dict(
            name="patina",
            face_top="#244234",
            face_bottom="#182E24",
            rim_top="#89B89A",
            rim_mid="#558066",
            rim_bottom="#2E4B3A",
            fillet_color="#A1CEB1",
            fillet_alpha=0.25,
            grain_strength=0.014,
            seed=4105,
            light_mode=False,
            pin_style="none",
        ),
        dict(
            name="oxide",
            face_top="#512722",
            face_bottom="#381A17",
            rim_top="#CE8274",
            rim_mid="#974F44",
            rim_bottom="#5E2D26",
            fillet_color="#DF9A8D",
            fillet_alpha=0.24,
            grain_strength=0.014,
            seed=4106,
            light_mode=False,
            pin_style="none",
        ),
    ]

    widget_map = {
        "bronze": ("drawable-xhdpi", "widget_forge_primary"),
        "pewter": ("drawable-xhdpi", "widget_forge_secondary"),
        "iron": ("drawable-night-xhdpi", "widget_forge_secondary"),
    }

    for spec in specs:
        plate = render_forge_plate(**spec)
        variant = spec["name"]
        out_path = NODPI / f"forge_plate_{variant}.webp"
        plate.save(out_path, quality=92, method=6)
        print(f"wrote {out_path.relative_to(ROOT)} ({plate.size})")
        if variant in widget_map:
            folder, wname = widget_map[variant]
            wdir = RES / folder
            wdir.mkdir(parents=True, exist_ok=True)
            wpath = wdir / f"{wname}.9.png"
            make_nine_patch(plate).save(wpath)
            print(f"wrote {wpath.relative_to(ROOT)}")

    clasp_dark = render_clasp(
        face_top="#252B34",
        face_bottom="#171B22",
        rim_top="#DFC28C",
        rim_mid="#AD8651",
        rim_bottom="#674D2B",
        fillet_color="#E6CA96",
        fillet_alpha=0.34,
        seed=4201,
        light_mode=False,
    )
    clasp_dark.save(NODPI / "engraved_clasp.webp", quality=92, method=6)
    print("wrote drawable-nodpi/engraved_clasp.webp")

    clasp_light = render_clasp(
        face_top="#F3EBDD",
        face_bottom="#E5D9C4",
        rim_top="#C4A26D",
        rim_mid="#9D7946",
        rim_bottom="#7B5C30",
        fillet_color="#8D6B39",
        fillet_alpha=0.28,
        seed=4202,
        light_mode=True,
    )
    clasp_light.save(NODPI / "engraved_clasp_light.webp", quality=92, method=6)
    print("wrote drawable-nodpi/engraved_clasp_light.webp")

    refine_engraved_wool().save(NODPI / "engraved_wool.webp", quality=92, method=6)
    print("wrote drawable-nodpi/engraved_wool.webp")

    refine_engraved_oak().save(NODPI / "engraved_oak.webp", quality=92, method=6)
    print("wrote drawable-nodpi/engraved_oak.webp")

    refine_engraved_spine().save(NODPI / "engraved_spine.webp", quality=92, method=6)
    print("wrote drawable-nodpi/engraved_spine.webp")

    refine_surface_iron().save(NODPI / "surface_iron.webp", quality=90, method=6)
    print("wrote drawable-nodpi/surface_iron.webp")

    refine_surface_vellum().save(NODPI / "surface_vellum.webp", quality=90, method=6)
    print("wrote drawable-nodpi/surface_vellum.webp")


if __name__ == "__main__":
    main()
