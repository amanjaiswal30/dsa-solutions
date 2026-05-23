#!/usr/bin/env python3
"""Render SVG class diagrams for all 18 LLD topics with Java code."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate_lld_docs import TOPICS
from lld_java_analysis import build_diagram_for_project

REPO_ROOT = Path(__file__).resolve().parent.parent
ASSETS = REPO_ROOT / "assets"


def main() -> None:
    ok = 0
    for topic in TOPICS:
        if not topic.project:
            continue
        md, _ = build_diagram_for_project(
            topic.project, topic.slug, ASSETS, render_svg=True
        )
        if md:
            ok += 1
            print(f"OK {topic.slug}")
        else:
            print(f"FAIL {topic.slug}")
    print(f"Rendered {ok} diagrams.")


if __name__ == "__main__":
    main()
