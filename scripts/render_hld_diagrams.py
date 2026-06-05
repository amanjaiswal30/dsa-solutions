#!/usr/bin/env python3
"""Render HLD Mermaid .mmd files under assets/images/high_level_design/ to SVG."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
ASSETS = REPO_ROOT / "assets" / "images" / "high_level_design"


def render_mmd(mmd: Path) -> bool:
    svg = mmd.with_suffix(".svg")
    try:
        subprocess.run(
            [
                "npx",
                "-y",
                "@mermaid-js/mermaid-cli",
                "-i",
                str(mmd),
                "-o",
                str(svg),
                "-b",
                "transparent",
            ],
            check=True,
            capture_output=True,
            text=True,
            timeout=180,
            cwd=str(mmd.parent),
        )
        return svg.is_file()
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
