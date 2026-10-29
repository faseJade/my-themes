# Writes out/preview.html: phone mock + every icon with its real file size, PNGs embedded.
import base64, os, html
def uri(p): return "data:image/png;base64," + base64.b64encode(open(p, "rb").read()).decode()
apps = [l.split("=")[0] for l in open("src/icons.txt").read().splitlines() if l and not l.startswith("#")]
icons = {a: f"out/icons/{a}.png" for a in apps}
size = {a: os.path.getsize(p) for a, p in icons.items()}
total, wall = sum(size.values()), os.path.getsize("out/wallpaper.png")
row = ["Instagram", "WhatsApp", "YouTube", "Discord"]; dock = ["Phone", "Messages", "Chrome", "Camera"]
def cell(a, label=True):
    lab = f'<span>{html.escape(a)}</span>' if label else ""
    return f'<figure><img src="{uri(icons[a])}" alt="{html.escape(a)} icon" width="96" height="96">{lab}</figure>'
grid = "".join(f'<figure><img src="{uri(icons[a])}" alt="" width="96" height="96"><figcaption>{html.escape(a)}<small>{size[a]/1024:.1f} KB</small></figcaption></figure>' for a in apps)
page = f'''<title>Amber Pocket Icons</title>
<meta name="claude:viewport-fit-sides" content="cover">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Chakra+Petch:wght@500;700&family=IBM+Plex+Sans:wght@400;500&family=IBM+Plex+Mono&display=swap">
<style>
/* Layout: phone mock beside the headline numbers, then the full icon sheet. Single dark look: it previews a dark theme. */
:root {{ --bg:#080604; --panel:#120b06; --amber:#FF8A24; --copper:#8F451B; --text:#FFD2A6; --muted:#A08670;
  --display:"Chakra Petch",ui-monospace,monospace; --body:"IBM Plex Sans",system-ui,sans-serif; --mono:"IBM Plex Mono",ui-monospace,monospace; color-scheme:dark }}
body {{ background:var(--bg); color:var(--text); font:15px/1.5 var(--body); margin:0 }}
main {{ max-width:1080px; margin:0 auto; padding-block:32px 64px; padding-left:max(16px,env(safe-area-inset-left,0px)); padding-right:max(16px,env(safe-area-inset-right,0px)); display:grid; gap:48px }}
.top {{ display:grid; grid-template-columns:minmax(0,1fr) 300px; gap:40px; align-items:center }}
h1 {{ font:700 clamp(32px,6vw,56px)/1.05 var(--display); color:var(--amber); margin:0 0 16px; text-wrap:balance }}
h2 {{ font:500 22px/1.2 var(--display); color:var(--amber); margin:0 0 16px }}
p {{ margin:0 0 12px; max-width:60ch }}
.stats {{ display:flex; flex-wrap:wrap; gap:28px; margin-top:24px; font-variant-numeric:tabular-nums }}
.stats b {{ display:block; font:700 32px/1 var(--display); color:var(--amber) }}
.stats span {{ color:var(--muted); font-size:13px }}
.phone {{ aspect-ratio:1440/3120; max-width:100%; border:2px solid var(--copper); border-radius:36px; background:var(--bg) url({uri("out/wallpaper.png")}) center/cover; position:relative; overflow:hidden }}
.clock {{ position:absolute; top:9%; left:9%; font:700 52px/1 var(--display); color:var(--amber) }}
.clock small {{ display:block; font:400 13px var(--mono); color:var(--muted); margin-top:6px }}
.home {{ position:absolute; left:6%; right:6%; bottom:4%; display:grid; gap:14px }}
.home div {{ display:grid; grid-template-columns:repeat(4,1fr); gap:8px }}
.home figure {{ margin:0; display:grid; justify-items:center; gap:3px; font-size:10px; color:var(--text) }}
.home img {{ width:100%; max-width:52px; height:auto }}
.sheet {{ display:grid; grid-template-columns:repeat(auto-fill,minmax(112px,1fr)); gap:24px 12px }}
.sheet figure {{ margin:0; display:grid; justify-items:center; gap:8px; text-align:center }}
.sheet img {{ width:72px; height:72px }}
figcaption {{ font-size:13px; line-height:1.3 }}
figcaption small {{ display:block; font:12px var(--mono); color:var(--muted) }}
.note {{ color:var(--muted); font-size:13px }}
@media (max-width:700px) {{ .top {{ grid-template-columns:1fr }} .phone {{ width:280px; justify-self:center }} }}
</style>
<main>
<section class="top">
  <div>
    <h1>Amber icons for 49 apps</h1>
    <p>Every app from your screenshots, drawn as amber outlines on near-black to match the wallpaper. Each file is a 192 px PNG cut down to 8 colours, so the whole set weighs less than one normal photo.</p>
    <div class="stats">
      <div><b>{total/1024:.0f} KB</b><span>all 49 icons</span></div>
      <div><b>{total/len(apps)/1024:.1f} KB</b><span>average icon</span></div>
      <div><b>{wall/1024:.0f} KB</b><span>wallpaper</span></div>
    </div>
  </div>
  <div class="phone" role="img" aria-label="Home screen preview with the wallpaper and icons">
    <div class="clock">08:05<small>Thu, 29 Oct</small></div>
    <div class="home"><div>{"".join(cell(a) for a in row)}</div><div>{"".join(cell(a, False) for a in dock)}</div></div>
  </div>
</section>
<section>
  <h2>Full set</h2>
  <div class="sheet">{grid}</div>
  <p class="note">Sizes are the real PNG files that go into Theme Park. Glyphs come from the free Lucide icon set (ISC licence); the frame and colours are ours.</p>
</section>
</main>'''
open("out/preview.html", "w").write(page)
print(round(os.path.getsize("out/preview.html")/1024), "KB page")
