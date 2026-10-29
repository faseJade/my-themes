# Packs each theme into the app: wallpaper in assets, icons as drawables (one copy, used by both the
# dashboard and the icon pack), appfilter.xml linking icons to apps, and app/assets/themes.js.
# shortcut: only flipper-amber has generators, so every theme reads out/, and the icon pack only serves the first
# theme; give each theme its own out/<id>/ and a "which theme is the icon pack" switch when adding a second one.
import glob, json, os, re, shutil
res = lambda tid, app: re.sub(r"[^a-z0-9]+", "_", f"{tid} {app}".lower()).strip("_")
links = {}  # app name -> [ComponentInfo{...}, ...]
for line in open("src/components.txt").read().splitlines():
    if "=" in line and not line.startswith("#"):
        app, comp = line.split("=", 1)
        links.setdefault(app.strip(), []).append(comp.strip())
draw = "app/res/drawable-nodpi"
shutil.rmtree(draw, ignore_errors=True); os.makedirs(draw); shutil.rmtree("app/assets/themes", ignore_errors=True)
themes, filt = [], []
for f in sorted(glob.glob("themes/*.json")):
    t = json.load(open(f))
    os.makedirs(f"app/assets/themes/{t['id']}")
    shutil.copy("out/wallpaper.png", f"app/assets/themes/{t['id']}/wallpaper.png")
    t["wallpaper"] = os.path.getsize("out/wallpaper.png")
    t["icons"] = []
    for n in sorted(os.listdir("out/icons")):
        app, r = n[:-4], res(t["id"], n[:-4])
        shutil.copy(f"out/icons/{n}", f"{draw}/{r}.png")
        t["icons"].append([app, os.path.getsize(f"out/icons/{n}"), r])
        if not themes:
            filt += [f'  <item component="{c}" drawable="{r}" />' for c in links.get(app, [])]
    t["linked"] = sum(1 for i in t["icons"] if i[0] in links)
    assert t["icons"] and t["wallpaper"], f"{t['id']} has no icons or wallpaper"
    assert len({i[2] for i in t["icons"]}) == len(t["icons"]), "drawable names must be unique"
    themes.append(t)
unknown = set(links) - {i[0] for i in themes[0]["icons"]}
assert not unknown, f"components.txt names apps with no icon: {unknown}"
xml = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n' + "\n".join(filt) + "\n</resources>\n"
os.makedirs("app/res/xml", exist_ok=True)
open("app/res/xml/appfilter.xml", "w").write(xml)
open("app/assets/appfilter.xml", "w").write(xml)  # some icon-pack readers look in assets instead of res
open("app/assets/themes.js", "w").write("window.THEMES=" + json.dumps(themes, separators=(",", ":")) + ";")
print(len(themes), "theme(s),", len(filt), "icon links")
