package br.com.ascend.aetheris

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*

/**
 * 1.7.3 — Módulo I: O Retrato Vivo.
 *
 * Classificação explícita da representação visual dos equipamentos sobre o retrato:
 * - [DirectBodyLayer]: camada pictórica calibrada diretamente sobre cabeça, tronco, peito, fundo ou moldura
 *   preservando 100% da fisionomia dos 3 retratos originais (`wanderer_light`, `wanderer_medium`, `wanderer_dark`).
 * - [ContextualEmblem]: selo pictórico contextual posicionado em nicho discreto da moldura (armas, companheiros, anéis)
 *   sem jamais obstruir o rosto.
 */
enum class PortraitRepresentation(val label: String) {
    DirectBodyLayer("Camada direta no retrato"),
    ContextualEmblem("Selo contextual no retrato")
}

fun portraitRepresentationFor(item: ShopItem): PortraitRepresentation = when (item.kind) {
    "head", "outfit", "frame", "background" -> PortraitRepresentation.DirectBodyLayer
    "accessory" -> if (item.id == "scribes-ring") PortraitRepresentation.ContextualEmblem else PortraitRepresentation.DirectBodyLayer
    else -> PortraitRepresentation.ContextualEmblem
}

/** Mapa de sobreposição de trajes/armaduras em `living_armor_atlas.webp` (2x2, 512x768/célula). */
private data class ArmorLayerSpec(val cellIndex: Int, val tint: Color? = null, val alpha: Float = 1f)

private fun armorLayerFor(outfitId: String?): ArmorLayerSpec? = when (outfitId) {
    "north-mail" -> ArmorLayerSpec(0)
    "scribe-robes" -> ArmorLayerSpec(1)
    "rune-mantle" -> ArmorLayerSpec(2)
    "vigil-armor" -> ArmorLayerSpec(3)
    "armor" -> ArmorLayerSpec(3, tint = Color(0xFFD8B07A), alpha = 0.96f)
    "cloak" -> ArmorLayerSpec(2, tint = Color(0xFFB39DD6), alpha = 0.96f)
    "dawn-outfit" -> ArmorLayerSpec(1, tint = Color(0xFFA2C9A8), alpha = 0.96f)
    else -> null
}

/** Mapa de elmos e coroas em `living_helms_atlas.webp` (2x2, 512x768/célula, rosto 100% livre). */
private fun helmCellFor(headId: String?): Int? = when (headId) {
    "oath-helm" -> 0
    "mimir-runic-crown" -> 1
    "traveler-helm" -> 2
    "will-helm" -> 3
    else -> null
}

/** Mapa de fundos pictóricos em `hero_background_atlas.webp` (2x2, 480x720/célula). */
fun heroBackgroundCellFor(backgroundId: String?): Int? = when (backgroundId) {
    "scholar-bg" -> 1 // Biblioteca Rúnica
    "night" -> 2      // Torre dos Cumes
    else -> null
}

/** Mapa de molduras em `hero_frames_atlas.webp` (2x2, 480x720/célula). */
private fun frameCellFor(frameId: String?, baseFrame: Int): Int = when (frameId) {
    "nine-realms-frame" -> 3 // Moldura dos Nove Reinos (lendária)
    "rune-frame" -> 2        // Moldura Rúnica Cerimonial (épica)
    "frame" -> 1             // Moldura de Bronze Viking (rara)
    else -> when (baseFrame) {
        1 -> 1
        2 -> 2
        else -> 0            // Moldura de Ferro Nórdico
    }
}

/** Identidade visual distinta (luz de borda + runa Futhark) para todas as 10 especializações. */
data class SpecializationVisualTheme(
    val accent: Color,
    val rune: HistoricalFutharkRune,
    val label: String
)

fun specializationVisualTheme(specId: String?): SpecializationVisualTheme? = when (specId) {
    "berserker" -> SpecializationVisualTheme(Color(0xFFC26A5A), HistoricalFutharkRune.Isa, "Fúria de Isa")
    "sentinela" -> SpecializationVisualTheme(Color(0xFF9AA7B8), HistoricalFutharkRune.Gebo, "Muralha de Gebo")
    "erudito" -> SpecializationVisualTheme(Color(0xFF7FA4D8), HistoricalFutharkRune.Ansuz, "Sopro de Ansuz")
    "arquimago_runico" -> SpecializationVisualTheme(Color(0xFFA98CD8), HistoricalFutharkRune.Kenaz, "Chama de Kenaz")
    "protetor" -> SpecializationVisualTheme(Color(0xFFD2AE6C), HistoricalFutharkRune.Dagaz, "Vigília de Dagaz")
    "druida" -> SpecializationVisualTheme(Color(0xFF7CB692), HistoricalFutharkRune.Berkano, "Abrigo de Berkano")
    "cartografo" -> SpecializationVisualTheme(Color(0xFF8CA6D9), HistoricalFutharkRune.Ehwaz, "Rota de Ehwaz")
    "patrulheiro" -> SpecializationVisualTheme(Color(0xFF8BB8C4), HistoricalFutharkRune.Raido, "Trilha de Raido")
    "mestre_da_forja" -> SpecializationVisualTheme(Color(0xFFD9985A), HistoricalFutharkRune.Fehu, "Ofício de Fehu")
    "tecelao_de_runas" -> SpecializationVisualTheme(Color(0xFFC2A378), HistoricalFutharkRune.Mannaz, "Trama de Mannaz")
    else -> null
}

/**
 * Snapshot resolvido de aparência para o Retrato Vivo, aceitando estado real ou overrides de preview do Estúdio/Forja.
 */
data class ResolvedHeroLook(
    val headId: String? = null,
    val outfitId: String? = null,
    val accessoryId: String? = null,
    val weaponId: String? = null,
    val companionId: String? = null,
    val frameId: String? = null,
    val backgroundId: String? = null
) {
    fun byKind(kind: String): String? = when (kind) {
        "head" -> headId
        "outfit" -> outfitId
        "accessory" -> accessoryId
        "weapon" -> weaponId
        "companion" -> companionId
        "frame" -> frameId
        "background" -> backgroundId
        else -> null
    }

    fun allIds(): List<String> = listOfNotNull(headId, outfitId, accessoryId, weaponId, companionId, frameId, backgroundId)
}

fun resolveHeroLook(
    s: GameState?,
    overrides: Map<String, String?>? = null
): ResolvedHeroLook {
    val equippedByKind = s?.assets.orEmpty()
        .filter { it.equipped }
        .associate { it.kind to it.id }

    fun pick(kind: String): String? =
        if (overrides != null && overrides.containsKey(kind)) overrides[kind]?.takeIf { it.isNotBlank() }
        else equippedByKind[kind]?.takeIf { it.isNotBlank() }

    return ResolvedHeroLook(
        headId = pick("head"),
        outfitId = pick("outfit"),
        accessoryId = pick("accessory"),
        weaponId = pick("weapon"),
        companionId = pick("companion"),
        frameId = pick("frame"),
        backgroundId = pick("background")
    )
}

/**
 * Compositor 2D em camadas do **Retrato Vivo** (ASCEND 1.7.3).
 *
 * Camadas ordenadas:
 * 1. Fundo equipado (`hero_background_atlas.webp`) quando presente (ou cena original do retrato)
 * 2. Retrato base original (`wanderer_light/medium/dark.webp`) com máscara suave quando há fundo equipado
 * 3. Iluminação/aura discreta de classe & especialização (10 especializações distintas)
 * 4. Armadura/traje calibrado sobre o tronco (`living_armor_atlas.webp`)
 * 5. Broche/amuleto de peito ancorado no fecho do manto (`living_artifacts_atlas.webp` / `realm_relics_atlas.webp`)
 * 6. Elmo/coroa calibrado na fronte (`living_helms_atlas.webp`, rosto e olhos 100% livres)
 * 7. Selos contextuais discretos de arma, companheiro, anel e especialização (apenas quando `size >= 96.dp`)
 * 8. Moldura nórdica/viking (`hero_frames_atlas.webp`, ou traço austero no modo minimalista)
 */
@Composable
fun LivingHeroPortrait(
    p: Player,
    s: GameState? = null,
    size: Dp = 100.dp,
    previewOverrides: Map<String, String?>? = null,
    previewSpecializationId: String? = null,
    showBadges: Boolean = size >= 96.dp
) {
    val appearance = LocalAppearance.current
    val minimal = appearance.minimal
    val look = remember(s?.assets, previewOverrides) { resolveHeroLook(s, previewOverrides) }
    val activeSpecId = previewSpecializationId ?: p.specialization.takeIf { id ->
        id.isNotBlank() && specializationById(id)?.heroClass == p.heroClass
    }
    val activeSpec = remember(activeSpecId) { activeSpecId?.let { specializationById(it) } }
    val specTheme = remember(activeSpecId) { specializationVisualTheme(activeSpecId) }

    val portraitRes = when (p.face) {
        1 -> R.drawable.wanderer_dark
        2 -> R.drawable.wanderer_medium
        else -> R.drawable.wanderer_light
    }
    val armorSpec = remember(look.outfitId) { armorLayerFor(look.outfitId) }
    val helmCell = remember(look.headId) { helmCellFor(look.headId) }
    val bgCell = remember(look.backgroundId) { heroBackgroundCellFor(look.backgroundId) }
    val frameCell = remember(look.frameId, p.frame) { frameCellFor(look.frameId, p.frame) }

    val accentBorder = when (look.frameId) {
        "nine-realms-frame" -> Gold
        "rune-frame" -> Color(0xFFB4A1C7)
        "frame" -> OldBronze
        else -> specTheme?.accent ?: listOf(
            Ember,
            Color(0xFF8BA2C7),
            Color(0xFF89967D),
            Color(0xFF92A5B7),
            OldBronze
        )[p.heroClass.coerceIn(0, 4)]
    }

    val signature = remember(look, p.face, p.heroClass, p.frame, p.color, activeSpecId) {
        "${look.allIds().joinToString(",")}|${p.face}|${p.heroClass}|${p.frame}|${p.color}|${activeSpecId.orEmpty()}"
    }
    val flash = remember { Animatable(0f) }
    LaunchedEffect(signature) {
        if (!appearance.reducedMotion) {
            flash.snapTo(0.24f)
            flash.animateTo(0f, tween(480))
        } else {
            flash.snapTo(0f)
        }
    }

    val equippedNames = remember(look) {
        look.allIds().mapNotNull { id -> shopItems.find { it.id == id }?.name }.joinToString(", ").ifBlank { "nenhum" }
    }
    val desc = buildString {
        append("Retrato vivo de ${p.name}, ${classes.getOrElse(p.heroClass) { "" }}")
        if (activeSpec != null) append(", ${activeSpec.name}")
        append(". Equipados: $equippedNames")
    }

    val outerShape = CutCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 6.dp, bottomEnd = 6.dp)

    Box(
        Modifier
            .width(size)
            .height(size * 1.5f)
            .clip(outerShape)
            .background(Color(0xFF0A0D12))
            .semantics { contentDescription = desc }
    ) {
        // Layer 1: Equipped background environment (when present and not minimal)
        if (bgCell != null && !minimal) {
            AtlasCellCropped(
                resId = R.drawable.hero_background_atlas,
                index = bgCell,
                columns = 2,
                rows = 2,
                modifier = Modifier.matchParentSize()
            )
        }

        // Layer 2: Original hero portrait (100% preserved face; soft outer-sky mask only when custom bg is active)
        val portraitModifier = if (bgCell != null && !minimal) {
            Modifier
                .matchParentSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    // Keep hero head, hair & shoulders 100% opaque while dissolving outer upper sky into the equipped background
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black,
                                0.56f to Color.Black,
                                0.84f to Color.Black.copy(alpha = 0.25f),
                                1.0f to Color.Transparent
                            ),
                            center = Offset(this.size.width * 0.51f, this.size.height * 0.44f),
                            radius = this.size.width * 0.68f
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    // Reinforce lower torso region so shoulders stay fully opaque
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.44f to Color.Transparent,
                            0.56f to Color.Black,
                            1.0f to Color.Black
                        ),
                        blendMode = BlendMode.DstOver
                    )
                }
        } else {
            Modifier.matchParentSize()
        }
        Image(
            painter = painterResource(portraitRes),
            contentDescription = null,
            modifier = portraitModifier,
            contentScale = ContentScale.FillBounds
        )

        // Layer 3: Subtle specialization atmosphere rim (never obscuring face)
        if (specTheme != null && !minimal) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.radialGradient(
                        listOf(specTheme.accent.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(this.size.width * 0.5f, this.size.height * 0.22f),
                        radius = this.size.width * 0.72f
                    )
                )
            }
        }

        // Layer 4: Calibrated Torso Armor / Garment Overlay
        if (armorSpec != null) {
            AtlasCellOverlay(
                resId = R.drawable.living_armor_atlas,
                index = armorSpec.cellIndex,
                columns = 2,
                rows = 2,
                modifier = Modifier.matchParentSize(),
                alpha = armorSpec.alpha,
                colorFilter = armorSpec.tint?.let { ColorFilter.tint(it, BlendMode.Modulate) }
            )
        }

        // Layer 5: Direct Chest Brooch / Cloak Clasp Accessory (anchored at x≈0.68, y≈0.70, clear of face)
        val chestAccessory = look.accessoryId?.takeIf { it != "scribes-ring" }
        if (chestAccessory != null) {
            val broochSize = (size * 0.165f).coerceIn(12.dp, 46.dp)
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = size * 0.61f, y = size * 1.5f * 0.64f)
                    .size(broochSize)
            ) {
                AccessoryOverlaySprite(chestAccessory, Modifier.matchParentSize())
            }
        }

        // Layer 6: Calibrated Open-Face Helm / Runic Crown Overlay (eyes & face 100% visible)
        if (helmCell != null) {
            AtlasCellOverlay(
                resId = R.drawable.living_helms_atlas,
                index = helmCell,
                columns = 2,
                rows = 2,
                modifier = Modifier.matchParentSize()
            )
        }

        // Layer 7: Subtle lower shadow gradient
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.78f to Color.Transparent,
                    1.0f to Color(0xFF070A0F).copy(alpha = 0.45f)
                )
            )
        }

        // Layer 8: Pictorial Nordic / Viking Frame (`hero_frames_atlas.webp`) or minimalist engraved border
        if (!minimal) {
            AtlasCellOverlay(
                resId = R.drawable.hero_frames_atlas,
                index = frameCell,
                columns = 2,
                rows = 2,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Canvas(Modifier.matchParentSize()) {
                scale(this.size.width / 100f, this.size.height / 150f, Offset.Zero) {
                    drawRect(accentBorder.copy(alpha = 0.82f), Offset(1.5f, 1.5f), Size(97f, 147f), style = Stroke(1.2f))
                    drawRect(accentBorder.copy(alpha = 0.30f), Offset(3.5f, 3.5f), Size(93f, 143f), style = Stroke(0.5f))
                }
            }
        }

        // Contextual weapon, companion/ring, and specialization medallions cleanly nested inside frame corners
        if (showBadges) {
            val badgeSize = (size * 0.20f).coerceIn(22.dp, 42.dp)
            val edgePadX = (size * 0.115f).coerceIn(10.dp, 24.dp)
            val edgePadY = (size * 0.125f).coerceIn(11.dp, 26.dp)

            // Bottom-left contextual weapon emblem (never covering face)
            if (look.weaponId != null) {
                PortraitCornerMedallion(
                    accent = accentBorder,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = edgePadX, bottom = edgePadY)
                        .size(badgeSize)
                ) {
                    val cell = if (look.weaponId == "craft-blade") 1 else 2
                    AtlasCell(R.drawable.living_artifacts_atlas, cell, 4, 2, Modifier.fillMaxSize().padding(3.dp))
                }
            }

            // Bottom-right contextual companion cameo (or Scribe's Ring when no companion equipped)
            val rightItem = look.companionId ?: look.accessoryId?.takeIf { it == "scribes-ring" }
            if (rightItem != null) {
                PortraitCornerMedallion(
                    accent = if (look.companionId != null) Violet else Gold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = edgePadX, bottom = edgePadY)
                        .size(badgeSize)
                ) {
                    if (rightItem == "scribes-ring") {
                        AtlasCell(R.drawable.living_artifacts_atlas, 6, 4, 2, Modifier.fillMaxSize().padding(3.dp))
                    } else {
                        val compIndex = when (rightItem) {
                            "lyra" -> 5
                            "ignis" -> 6
                            else -> 4
                        }
                        ArtTile(R.drawable.relic_atlas, compIndex, 3, Modifier.fillMaxSize().clip(CircleShape))
                    }
                }
            }

            // Top-right specialization crest seal (when size >= 110dp and active specialization)
            if (activeSpec != null && size >= 110.dp && !minimal) {
                val specSize = (size * 0.18f).coerceIn(22.dp, 36.dp)
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = edgePadY, end = edgePadX)
                        .size(specSize)
                        .clip(CircleShape)
                        .background(Color(0xFF0E1216).copy(alpha = 0.88f))
                        .border(1.dp, (specTheme?.accent ?: Gold).copy(alpha = 0.82f), CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ArchetypeSealArt(activeSpec.atlasIndex, Modifier.fillMaxSize())
                }
            }
        }

        // Subtle equip confirmation gleam (respects reducedMotion)
        if (flash.value > 0.005f) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(accentBorder.copy(alpha = flash.value))
            }
        }
    }
}

@Composable
private fun PortraitCornerMedallion(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Color(0xFF0D1116).copy(alpha = 0.88f))
            .border(1.1.dp, accent.copy(alpha = 0.82f), CircleShape),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun AccessoryOverlaySprite(itemId: String, modifier: Modifier = Modifier) {
    when (itemId) {
        "yggdrasil-brooch" -> AtlasCell(R.drawable.living_artifacts_atlas, 0, 4, 2, modifier)
        "ember-amulet" -> AtlasCell(R.drawable.living_artifacts_atlas, 3, 4, 2, modifier)
        "eternal-memory-medallion" -> AtlasCell(R.drawable.living_artifacts_atlas, 4, 4, 2, modifier)
        "firmament-compass" -> AtlasCell(R.drawable.living_artifacts_atlas, 5, 4, 2, modifier)
        "boreal-vigil-brooch" -> AtlasCell(R.drawable.realm_relics_atlas, 3, 2, 2, modifier.clip(CircleShape))
        "mist-clasp" -> ArtTile(R.drawable.forest_atlas, 8, 3, modifier.clip(CircleShape))
        else -> ArtTile(R.drawable.relic_atlas, 2, 3, modifier.clip(CircleShape))
    }
}

/**
 * Draws a single cell from a transparent RGBA sprite atlas stretched 1:1 over the 2:3 portrait box
 * with optional alpha & colorFilter (used for calibrated armor, helm, and frame layers).
 */
@Composable
fun AtlasCellOverlay(
    resId: Int,
    index: Int,
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null
) {
    val bmp = ImageBitmap.imageResource(resId)
    val safeIndex = index.coerceIn(0, (columns * rows - 1).coerceAtLeast(0))
    val col = safeIndex % columns
    val row = safeIndex / columns
    Canvas(modifier) {
        val cw = bmp.width / columns
        val ch = bmp.height / rows
        drawImage(
            image = bmp,
            srcOffset = IntOffset(col * cw, row * ch),
            srcSize = IntSize(cw, ch),
            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Medium
        )
    }
}

/**
 * Draws a single cell from an atlas using center-crop so wide banners, chambers, and square
 * NPC portraits never stretch or distort their aspect ratio.
 */
@Composable
fun AtlasCellCropped(
    resId: Int,
    index: Int,
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null
) {
    val bmp = ImageBitmap.imageResource(resId)
    val safeIndex = index.coerceIn(0, (columns * rows - 1).coerceAtLeast(0))
    val col = safeIndex % columns
    val row = safeIndex / columns
    Canvas(modifier) {
        val cw = (bmp.width / columns).coerceAtLeast(1)
        val ch = (bmp.height / rows).coerceAtLeast(1)
        val dstW = size.width.coerceAtLeast(1f)
        val dstH = size.height.coerceAtLeast(1f)
        val targetAspect = dstW / dstH
        val cellAspect = cw.toFloat() / ch.toFloat()
        val cropW: Int
        val cropH: Int
        if (cellAspect > targetAspect) {
            cropH = ch
            cropW = (ch * targetAspect).toInt().coerceIn(1, cw)
        } else {
            cropW = cw
            cropH = (cw / targetAspect).toInt().coerceIn(1, ch)
        }
        val srcX = col * cw + (cw - cropW) / 2
        val srcY = row * ch + (ch - cropH) / 2
        drawImage(
            image = bmp,
            srcOffset = IntOffset(srcX, srcY),
            srcSize = IntSize(cropW, cropH),
            dstSize = IntSize(dstW.toInt().coerceAtLeast(1), dstH.toInt().coerceAtLeast(1)),
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Medium
        )
    }
}

