# Writes src/wallpaper.svg. Flat colours only: no gradients, glow or noise, so the PNG stays tiny.
BG, AMBER, COPPER, GRID, GRID2, CREAM = "#080604", "#FF8A24", "#8F451B", "#1C1009", "#2E180B", "#FFD2A6"
W, H = 1440, 3120

CAT = [  # original pixel cat shown on the device screen
    "..#......#......",
    "..##....##......",
    "..########......",
    ".##########.....",
    ".##.####.##.....",
    ".##########...#.",
    ".###.##.###...#.",
    "..########...#..",
    "..#########.#...",
    "..##########....",
    "..##.##.##.#....",
]

def cat(x0, y0, px):
    return "".join(f'<rect x="{x0+c*px}" y="{y0+r*px}" width="{px}" height="{px}"/>'
                   for r, row in enumerate(CAT) for c, ch in enumerate(row) if ch == "#")

device = f'''
<g transform="translate(720 1720) rotate(-14)" fill="none" stroke="{AMBER}" stroke-width="7" stroke-linejoin="round">
  <rect x="-470" y="-230" width="940" height="460" rx="120" fill="{BG}"/>
  <rect x="-430" y="-190" width="860" height="380" rx="90" stroke="{COPPER}" stroke-width="4"/>
  <rect x="-360" y="-140" width="380" height="250" rx="14" fill="{AMBER}"/>
  <g fill="{BG}" stroke="none">{cat(-310, -110, 16)}</g>
  <rect x="-330" y="130" width="60" height="22" rx="6"/><rect x="-250" y="130" width="60" height="22" rx="6"/>
  <circle cx="230" cy="-10" r="120"/>
  <circle cx="230" cy="-10" r="44" fill="{AMBER}"/>
  <path d="M230 -112 v30 M230 62 v30 M128 -10 h30 M302 -10 h30" stroke-linecap="round"/>
  <circle cx="390" cy="140" r="34"/>
  <path d="M-470 -40 h-40 v80 h40 M470 -60 h28 v40 h-28" stroke="{COPPER}" stroke-width="5"/>
</g>
<g fill="none" stroke="{AMBER}" stroke-width="9" stroke-linecap="round">
  <path d="M1130 1290 a70 70 0 0 1 70 70"/><path d="M1130 1230 a130 130 0 0 1 130 130"/><path d="M1130 1170 a190 190 0 0 1 190 190"/>
</g>'''

callouts = "".join(
    f'<path d="M{x1} {y1} L{x2} {y2} h{dx}" stroke="{COPPER}" stroke-width="3" fill="none"/>'
    f'<circle cx="{x1}" cy="{y1}" r="7" fill="{COPPER}"/>'
    f'<text x="{x2+dx}" y="{y2-16}" text-anchor="{"end" if dx>0 else "start"}">{t}</text>'
    for x1, y1, x2, y2, dx, t in [
        (500, 1610, 420, 1290, -330, "DISPLAY 128x64"),
        (980, 1660, 1060, 2040, 260, "5-WAY PAD"),
        (360, 1950, 300, 2160, -200, "GPIO"),
        (1320, 1372, 1320, 1470, -110, "RF"),
    ])

svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<defs>
  <pattern id="g" width="48" height="48" patternUnits="userSpaceOnUse"><path d="M48 0H0V48" fill="none" stroke="{GRID}" stroke-width="2"/></pattern>
  <pattern id="G" width="240" height="240" patternUnits="userSpaceOnUse"><path d="M240 0H0V240" fill="none" stroke="{GRID2}" stroke-width="2"/></pattern>
</defs>
<rect width="{W}" height="{H}" fill="{BG}"/>
<rect width="{W}" height="{H}" fill="url(#g)"/><rect width="{W}" height="{H}" fill="url(#G)"/>
<g fill="none" stroke="{GRID2}" stroke-width="3">
  <circle cx="720" cy="1720" r="640"/><circle cx="720" cy="1720" r="560" stroke-dasharray="12 18"/>
  <path d="M0 1720 H1440 M720 960 V2480"/>
</g>
<g font-family="DejaVu Sans Mono, monospace" font-size="34" fill="{COPPER}" letter-spacing="3">{callouts}</g>
{device}
<text x="720" y="2330" text-anchor="middle" font-family="DejaVu Sans Mono, monospace" font-weight="bold" font-size="64" letter-spacing="14" fill="{AMBER}">POCKET MULTI-TOOL</text>
</svg>'''
open("src/wallpaper.svg", "w").write(svg)
