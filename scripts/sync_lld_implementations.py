#!/usr/bin/env python3
"""Deprecated: use generate_lld_docs.py instead.

That script regenerates full interview-format *_lld.md files
(flow → entities → code) from LLD/ Java projects.
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def main() -> None:
    script = ROOT / "generate_lld_docs.py"
    print("Redirecting to generate_lld_docs.py …")
    sys.exit(subprocess.call([sys.executable, str(script)]))


if __name__ == "__main__":
    main()
