#!/usr/bin/env python3
"""Render HLD Mermaid .mmd files under assets/images/high_level_design/ to SVG."""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
ASSETS = REPO_ROOT / "assets" / "images" / "high_level_design"
CONFIG = ASSETS / "mermaid-config.json"


def postprocess_svg(svg: Path) -> None:
    """Force opaque white background so diagrams read well on dark UIs."""
    text = svg.read_text(encoding="utf-8")
    text = text.replace('background-color: transparent;', 'background-color: #ffffff;')
    if "<rect" not in text[:800] or 'id="bg"' not in text:
        text = re.sub(
            r"(<svg[^>]*>)",
            r'\1<rect id="bg" width="100%" height="100%" fill="#ffffff"/>',
            text,
            count=1,
        )
    svg.write_text(text, encoding="utf-8")


def render_mmd(mmd: Path) -> bool:
    svg = mmd.with_suffix(".svg")
    cmd = [
        "npx",
        "-y",
        "@mermaid-js/mermaid-cli",
        "-i",
        str(mmd),
        "-o",
        str(svg),
        "-b",
        "white",
        "-c",
        str(CONFIG),
    ]
    try:
        subprocess.run(
            cmd,
            check=True,
            capture_output=True,
            text=True,
            timeout=180,
            cwd=str(mmd.parent),
        )
        if svg.is_file():
            postprocess_svg(svg)
            return True
        return False
    except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired) as e:
        print(f"FAIL {mmd.name}: {e}", file=sys.stderr)
        return False


def main() -> None:
    files = sorted(ASSETS.glob("*.mmd"))
    if not files:
        print("No .mmd files found.", file=sys.stderr)
        sys.exit(1)
    ok = sum(render_mmd(f) for f in files)
    print(f"Rendered {ok}/{len(files)} diagrams.")


if __name__ == "__main__":
    main()
