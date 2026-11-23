"""Build the local Compose WebPs and the resizable Android widget backgrounds."""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw


def nine_patch(image):
    width, height = image.size
    target = Image.new("RGBA", (width + 2, height + 2))
    target.paste(image, (1, 1))
    marks = ImageDraw.Draw(target)
    # Same stretch bounds as the Compose slices. Content uses the layout's padding.
    marks.line((49, 0, width - 48, 0), fill="black")
    marks.line((0, 17, 0, height - 16), fill="black")
    marks.line((1, height + 1, width, height + 1), fill="black")
    marks.line((width + 1, 1, width + 1, height), fill="black")
    return target


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("renders", type=Path)
    parser.add_argument("resources", type=Path)
    args = parser.parse_args()
    assets = {
        "bronze": ("drawable-xhdpi", "widget_forge_primary"),
        "pewter": ("drawable-xhdpi", "widget_forge_secondary"),
        "iron": ("drawable-night-xhdpi", "widget_forge_secondary"),
    }
    for variant in ("bronze", "ritual", "iron", "pewter", "patina", "oxide"):
        name = f"forge_plate_{variant}"
        with Image.open(args.renders / f"{name}.png") as render:
            plate = render.resize((384, 96), Image.Resampling.LANCZOS)
        plate.save(args.resources / "drawable-nodpi" / f"{name}.webp", quality=90, method=6)
        if variant in assets:
            folder, widget = assets[variant]
            target = args.resources / folder
            target.mkdir(parents=True, exist_ok=True)
            nine_patch(plate).save(target / f"{widget}.9.png")


if __name__ == "__main__":
    main()
