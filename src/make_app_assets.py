# Copies each theme into app/assets/themes/<id>/ and writes app/assets/themes.js for the dashboard.
# shortcut: only flipper-amber has generators, so every theme reads out/; give each theme its own out/<id>/ when adding a second one.
import glob, json, os, shutil
themes = []
for f in sorted(glob.glob("themes/*.json")):
    t = json.load(open(f))
    dst = f"app/assets/themes/{t['id']}"
    shutil.rmtree(dst, ignore_errors=True)
    shutil.copytree("out/icons", f"{dst}/icons")
    shutil.copy("out/wallpaper.png", f"{dst}/wallpaper.png")
    t["wallpaper"] = os.path.getsize(f"{dst}/wallpaper.png")
    t["icons"] = [[n[:-4], os.path.getsize(f"{dst}/icons/{n}")] for n in sorted(os.listdir(f"{dst}/icons"))]
    assert t["icons"] and t["wallpaper"], f"{t['id']} has no icons or wallpaper"
    themes.append(t)
open("app/assets/themes.js", "w").write("window.THEMES=" + json.dumps(themes, separators=(",", ":")) + ";")
print(len(themes), "theme(s)")
