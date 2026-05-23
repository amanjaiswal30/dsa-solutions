#!/usr/bin/env python3
"""Parse LLD Java sources: logical file order, Mermaid class diagrams, SVG export."""

from __future__ import annotations

import re
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path

LLD_ROOT = Path("/Users/aman.jaiswal/Desktop/LLD")
SKIP_JAVA = {"requirements.java"}
JAVA_STD = {
    "String", "Integer", "Long", "Double", "Float", "Boolean", "BigDecimal",
    "LocalDateTime", "Instant", "UUID", "Void", "Object", "List", "Map", "Set",
    "Queue", "Optional", "HashMap", "ArrayList", "EnumMap", "Collections",
}

RE_CLASS = re.compile(
    r"(?:public\s+)?(?:abstract\s+)?(class|interface|enum)\s+(\w+)"
)
RE_EXTENDS = re.compile(r"\bextends\s+(\w+)")
RE_IMPLEMENTS = re.compile(r"\bimplements\s+([\w\s,]+)")
RE_FIELD = re.compile(
    r"(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?"
    r"([\w.<>,\s]+?)\s+(\w+)\s*[;=]"
)
RE_GENERIC = re.compile(r"(?:List|Map|Set|Queue|Optional)<(?:[\w.]+,\s*)?(\w+)>")
RE_LOCAL_CLASS = re.compile(r"\b([A-Z][a-zA-Z0-9]*)\b")


@dataclass
class JavaType:
    name: str
    kind: str  # class | interface | enum
    file: str
    extends: str | None = None
    implements: list[str] = field(default_factory=list)
    fields: list[str] = field(default_factory=list)  # referenced type names
    methods: list[str] = field(default_factory=list)


def parse_java_file(path: Path) -> JavaType | None:
    content = path.read_text(encoding="utf-8")
    m = RE_CLASS.search(content)
    if not m:
        return None
    kind, name = m.group(1), m.group(2)
    extends = None
    em = RE_EXTENDS.search(content)
    if em:
        extends = em.group(1)
    implements: list[str] = []
    im = RE_IMPLEMENTS.search(content)
    if im:
        implements = [x.strip() for x in im.group(1).split(",") if x.strip()]

    refs: set[str] = set()
    if extends and extends not in JAVA_STD:
        refs.add(extends)
    for iface in implements:
        if iface not in JAVA_STD:
            refs.add(iface)

    for fm in RE_FIELD.finditer(content):
        type_str = fm.group(1).strip()
        for gm in RE_GENERIC.finditer(type_str):
            t = gm.group(1)
            if t not in JAVA_STD:
                refs.add(t)
        base = type_str.split("<")[0].strip().split()[-1]
        if base and base[0].isupper() and base not in JAVA_STD:
            refs.add(base)

    # public methods (sample for diagram)
    methods = re.findall(r"public\s+(?:static\s+)?[\w<>,\s]+\s+(\w+)\s*\(", content)
    public_methods = [x for x in methods if x not in {"class", "enum", "interface"}][:6]

    return JavaType(
        name=name,
        kind=kind,
        file=path.name,
        extends=extends,
        implements=implements,
        fields=sorted(refs),
        methods=public_methods,
    )


def load_project(src_dir: Path) -> dict[str, JavaType]:
    types: dict[str, JavaType] = {}
    for path in sorted(src_dir.glob("*.java")):
        if path.name in SKIP_JAVA:
            continue
        parsed = parse_java_file(path)
        if parsed:
            types[parsed.name] = parsed
    return types


def logical_file_order(types: dict[str, JavaType]) -> list[str]:
    """Topological order: dependencies before dependents; Main always last."""
    names = set(types.keys())
    # edges: dep -> dependent (dep must appear first)
    edges: dict[str, set[str]] = {n: set() for n in names}
    in_degree: dict[str, int] = {n: 0 for n in names}

    def add_edge(dep: str, dependent: str) -> None:
        if dep not in names or dependent not in names or dep == dependent:
            return
        if dependent not in edges[dep]:
            edges[dep].add(dependent)
            in_degree[dependent] += 1

    for t in types.values():
        if t.extends:
            add_edge(t.extends, t.name)
        for iface in t.implements:
            add_edge(iface, t.name)
        for ref in t.fields:
            if ref in names:
                add_edge(ref, t.name)

    # Kahn's algorithm with stable tie-break (enum < interface < class, then name)
    def sort_key(n: str) -> tuple:
        k = types[n].kind
        tier = {"enum": 0, "interface": 1, "class": 2}[k]
        return (tier, n)

    queue = sorted([n for n in names if in_degree[n] == 0], key=sort_key)
    order: list[str] = []
    while queue:
        n = queue.pop(0)
        order.append(n)
        for dep in sorted(edges[n]):
            in_degree[dep] -= 1
            if in_degree[dep] == 0:
                queue.append(dep)
                queue.sort(key=sort_key)

    for n in sorted(names - set(order), key=sort_key):
        order.append(n)

    def is_service(n: str) -> bool:
        return "Service" in n or "Factory" in n or n.endswith("Controller")

    enums = [n for n in order if types[n].kind == "enum"]
    ifaces = [n for n in order if types[n].kind == "interface"]
    domain = [
        n for n in order
        if types[n].kind == "class" and n != "Main" and not is_service(n)
    ]
    services = [n for n in order if types[n].kind == "class" and is_service(n)]
    main = ["Main"] if "Main" in names else []

    # Leaf domain types first (fewest internal deps), then dependents
    def dep_count(n: str) -> int:
        return sum(1 for d in types[n].fields if d in names)

    # Roots (Customer, Order, ParkingLot) tend to reference many types — list them first
    domain.sort(key=lambda n: (-dep_count(n), n))
    services.sort()

    reordered = enums + ifaces + domain + services + main
    seen: set[str] = set()
    final: list[str] = []
    for n in reordered:
        if n not in seen:
            final.append(n)
            seen.add(n)
    for n in order:
        if n not in seen:
            final.append(n)
            seen.add(n)

    return [types[n].file for n in final]


def mermaid_class_diagram(types: dict[str, JavaType]) -> str:
    """Compact diagram for GitHub (native Mermaid); no method lists to avoid huge graphs."""
    lines = ["classDiagram"]
    names = set(types.keys())

    for t in types.values():
        if t.kind == "enum":
            lines.append(f"    class {t.name} {{")
            lines.append("        <<enumeration>>")
            lines.append("    }")
        else:
            lines.append(f"    class {t.name}")

    for t in types.values():
        if t.extends and t.extends in names:
            lines.append(f"    {t.extends} <|-- {t.name}")
        for iface in t.implements:
            if iface in names:
                lines.append(f"    {iface} <|.. {t.name}")

    inheritance_pairs: set[tuple[str, str]] = set()
    for t in types.values():
        if t.extends and t.extends in names:
            inheritance_pairs.add((t.extends, t.name))
        for iface in t.implements:
            if iface in names:
                inheritance_pairs.add((iface, t.name))

    added: set[tuple[str, str]] = set()
    for t in types.values():
        for ref in t.fields:
            if ref in names and ref != t.name:
                key = (ref, t.name)
                rev = (t.name, ref)
                if key in inheritance_pairs or rev in inheritance_pairs:
                    continue
                if key not in added:
                    lines.append(f"    {ref} --> {t.name}")
                    added.add(key)

    return "\n".join(lines)


def render_mermaid_to_svg(mermaid: str, out_svg: Path) -> bool:
    out_svg.parent.mkdir(parents=True, exist_ok=True)
    mmd = out_svg.with_suffix(".mmd")
    mmd.write_text(mermaid, encoding="utf-8")
    try:
        subprocess.run(
            [
                "npx",
                "-y",
                "@mermaid-js/mermaid-cli",
                "-i",
                str(mmd),
                "-o",
                str(out_svg),
                "-b",
                "transparent",
            ],
            check=True,
            capture_output=True,
            text=True,
            timeout=180,
            cwd=str(out_svg.parent),
        )
        return out_svg.is_file()
    except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired) as e:
        print(f"  mermaid-cli failed for {out_svg.name}: {e}", file=sys.stderr)
        return False


def read_java_sources_ordered(project_dir: Path) -> list[tuple[str, str]]:
    src = project_dir / "src"
    types = load_project(src)
    order = logical_file_order(types)
    file_map = {
        p.name: p.read_text(encoding="utf-8").rstrip() + "\n"
        for p in src.glob("*.java")
        if p.name not in SKIP_JAVA
    }
    result: list[tuple[str, str]] = []
    for fname in order:
        if fname in file_map:
            result.append((fname, file_map[fname]))
    for fname in sorted(file_map):
        if fname not in {x[0] for x in result}:
            result.append((fname, file_map[fname]))
    return result


def build_diagram_markdown(project: str) -> str | None:
    """Mermaid fenced block — renders on GitHub without heavy SVG embeds."""
    src = LLD_ROOT / project / "src"
    if not src.is_dir():
        return None
    types = load_project(src)
    if not types:
        return None
    mermaid = mermaid_class_diagram(types)
    return f"### Class diagram\n\n```mermaid\n{mermaid}\n```\n"


def build_diagram_for_project(
    project: str, slug: str, assets_root: Path, *, render_svg: bool = False
) -> tuple[str | None, str | None]:
    """Returns (markdown_section, mermaid_source). Prefer Mermaid over SVG for GitHub."""
    _ = slug, assets_root, render_svg
    src = LLD_ROOT / project / "src"
    if not src.is_dir():
        return None, None
    types = load_project(src)
    if not types:
        return None, None
    mermaid = mermaid_class_diagram(types)
    return build_diagram_markdown(project), mermaid
