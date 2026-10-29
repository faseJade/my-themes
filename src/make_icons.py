# Writes src/icons/<app>.svg: amber Lucide glyph in an amber rounded frame on near-black.
import os, re
BG, AMBER = "#080604", "#FF8A24"
os.makedirs("src/icons", exist_ok=True)
apps = [l.split("=") for l in open("src/icons.txt").read().splitlines() if l and not l.startswith("#")]
for app, icon in apps:
    body = open(f"vendor/lucide/icons/{icon}.svg").read()
    inner = re.search(r"<svg[^>]*>(.*)</svg>", body, re.S).group(1)
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="192" height="192" viewBox="0 0 32 32">'
           f'<rect x="1" y="1" width="30" height="30" rx="9" fill="{BG}" stroke="{AMBER}" stroke-width="1.4"/>'
           f'<g transform="translate(7 7) scale(.75)" fill="none" stroke="{AMBER}" stroke-width="2.2" '
           f'stroke-linecap="round" stroke-linejoin="round">{inner}</g></svg>')
    open(f"src/icons/{app}.svg", "w").write(svg)
assert len(os.listdir("src/icons")) == len(apps), "one svg per app"
print(len(apps), "icons")
