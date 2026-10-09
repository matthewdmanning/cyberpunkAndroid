#!/usr/bin/env python3
"""
patch_digits.py

Transplants numeral glyphs ('0'-'9') from a donor font (e.g. Oare Sans) into target
Fastup font files to fix missing/placeholder digits (resolves Issue #46).

Handles:
- Cap height scaling to match target font proportions
- Italic angle shearing
- Cubic (OTF/CFF) to quadratic (TTF) contour conversion via Cu2QuPen
- Contour direction reversal (CFF counterclockwise to TrueType clockwise)
- Bounding box and horizontal metrics adjustment
"""

import argparse
import math
import sys
from pathlib import Path
from fontTools.pens.cu2quPen import Cu2QuPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.ttLib import TTFont

DIGITS = "0123456789"


def transplant_digits(target_path: Path, donor: TTFont, output_path: Path) -> None:
    """
    Transplants digits '0'-'9' from donor TTFont into target TTFont file.
    """
    target = TTFont(target_path)

    # Scale based on capital height ratio
    scale = target["OS/2"].sCapHeight / donor["OS/2"].sCapHeight

    # Shear adjustment to match target font's italic slant
    shear = math.tan(math.radians(-target["post"].italicAngle)) - math.tan(
        math.radians(-donor["post"].italicAngle)
    )

    donor_cmap = donor.getBestCmap()
    target_cmap = target.getBestCmap()
    donor_glyphs = donor.getGlyphSet()
    glyf = target["glyf"]
    hmtx = target["hmtx"]

    for digit in DIGITS:
        char_code = ord(digit)
        if char_code not in donor_cmap or char_code not in target_cmap:
            print(f"Warning: Digit '{digit}' ({char_code}) not found in cmaps, skipping.", file=sys.stderr)
            continue

        donor_name = donor_cmap[char_code]
        target_name = target_cmap[char_code]

        glyph_pen = TTGlyphPen(None)
        # CFF contours run counterclockwise; TrueType contours run clockwise
        quadratic_pen = Cu2QuPen(glyph_pen, max_err=1.0, reverse_direction=True)
        transform = (scale, 0, shear * scale, scale, 0, 0)
        donor_glyphs[donor_name].draw(TransformPen(quadratic_pen, transform))

        glyph = glyph_pen.glyph()
        glyph.recalcBounds(glyf)
        glyf[target_name] = glyph
        advance_width = round(donor["hmtx"][donor_name][0] * scale)
        left_side_bearing = getattr(glyph, "xMin", 0)
        hmtx[target_name] = (advance_width, left_side_bearing)

    target.save(output_path)
    print(f"Successfully patched digits into: {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Patch digit glyphs into Fastup fonts.")
    repo_root = Path(__file__).resolve().parent.parent
    default_font_dir = repo_root / "cyberpunkandroid" / "src" / "main" / "res" / "font"

    parser.add_argument(
        "--font-dir",
        type=Path,
        default=default_font_dir,
        help="Path to font resources directory containing font files",
    )
    parser.add_argument(
        "--donor",
        type=Path,
        default=None,
        help="Path to donor font file (default: <font-dir>/oare_sans_black_oblique.otf)",
    )

    args = parser.parse_args()
    font_dir: Path = args.font_dir
    donor_path: Path = args.donor or (font_dir / "oare_sans_black_oblique.otf")

    if not donor_path.exists():
        print(f"Error: Donor font not found at {donor_path}", file=sys.stderr)
        sys.exit(1)

    donor = TTFont(donor_path)
    target_fonts = ["fastup_regular.ttf", "fastup_bold.ttf"]

    for font_name in target_fonts:
        target_path = font_dir / font_name
        if not target_path.exists():
            print(f"Warning: Target font {target_path} not found, skipping.", file=sys.stderr)
            continue
        transplant_digits(target_path, donor, target_path)


if __name__ == "__main__":
    main()