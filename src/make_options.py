# Writes design/option-{a,b,c}.html: the same 3 app screens in three styles, with real theme assets embedded.
import base64, os
def uri(p): return "data:image/png;base64," + base64.b64encode(open(p, "rb").read()).decode()
WALL = uri("out/wallpaper.png")
strip = ["Phone", "Messages", "Camera", "Gallery", "WhatsApp", "Chrome", "Settings", "Clock"]
ICONS = "".join(f'<img src="{uri(f"out/icons/{a}.png")}" alt="{a}">' for a in strip)
kb = round((os.path.getsize("out/wallpaper.png") + sum(os.path.getsize(f"out/icons/{f}") for f in os.listdir("out/icons"))) / 1024)
SW = [("Background", "#080604"), ("Main", "#FF8A24"), ("Second", "#8F451B"), ("Text", "#FFD2A6")]
swatches = "".join(f'<li><i style="background:{h}"></i><span>{n}</span><code>{h}</code></li>' for n, h in SW)

def screens(o):
    lib = f'''<div class="scr"><header class="bar"><h3>{o["lib_title"]}</h3></header>
      <article class="tcard"><div class="thumb" style="background-image:url({WALL})"></div>
        <div class="meta"><b>Flipper Amber</b><span>Wallpaper and 49 icons, {kb} KB</span><em class="pill">In use</em></div></article>
      <p class="empty">{o["empty"]}</p></div>'''
    detail = f'''<div class="scr"><header class="bar"><span class="back" aria-hidden="true">‹</span><h3>Flipper Amber</h3></header>
      <div class="hero" style="background-image:url({WALL})"></div>
      <div class="strip">{ICONS}</div>
      <div class="acts"><button class="primary">Set wallpaper</button><button>Use icon pack</button></div>
      <section class="sw"><h4>Theme Park colours</h4><ul>{swatches}</ul></section></div>'''
    apply = f'''<div class="scr dim"><div class="hero" style="background-image:url({WALL})"></div>
      <div class="sheet"><h4>Set wallpaper on</h4>
        <label><input type="radio" name="w{o["k"]}">Home screen</label>
        <label><input type="radio" name="w{o["k"]}">Lock screen</label>
        <label><input type="radio" name="w{o["k"]}" checked>Home and lock screens</label>
        <button class="primary">Set wallpaper</button></div>
      <div class="toast">Wallpaper set</div></div>'''
    return "".join(f'<figure class="shot"><div class="phone">{s}</div><figcaption>{c}</figcaption></figure>'
                   for s, c in [(lib, "Your themes"), (detail, "Theme page"), (apply, "Setting the wallpaper")])

BASE = '''
body{background:#080604;color:#FFD2A6;margin:0}
main{max-width:1100px;margin:0 auto;padding-block:32px 56px;padding-left:max(16px,env(safe-area-inset-left,0px));padding-right:max(16px,env(safe-area-inset-right,0px))}
.intro{max-width:62ch;margin-bottom:32px} .intro h1{margin:0 0 12px;color:#FF8A24;text-wrap:balance} .intro p{margin:0 0 10px;line-height:1.55}
.shots{display:flex;flex-wrap:wrap;gap:28px;justify-content:center}
.shot{margin:0;display:grid;gap:10px;justify-items:center;color:#A08670;font-size:13px}
.phone{width:280px;max-width:100%;aspect-ratio:9/19.5;border:2px solid #3a2414;border-radius:34px;overflow:hidden;background:var(--bg);position:relative}
.scr{position:absolute;inset:0;padding:22px 16px;display:flex;flex-direction:column;gap:14px;overflow:hidden}
.bar{display:flex;align-items:center;gap:10px}.bar h3{margin:0}.back{font-size:26px;line-height:1;color:var(--accent)}
.thumb,.hero{background-size:cover;background-position:center 52%}
.strip{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}.strip img{width:100%;height:auto}
.acts{display:grid;gap:8px} button{font:inherit;cursor:pointer}
.sw h4,.sheet h4{margin:0 0 8px}.sw ul{list-style:none;margin:0;padding:0;display:grid;gap:6px}
.sw li{display:grid;grid-template-columns:16px 1fr auto;gap:8px;align-items:center;font-size:12px}.sw i{width:16px;height:16px;border-radius:4px;border:1px solid #3a2414}
.sw code{font-size:11px;color:var(--muted)}
.scr{color:#FFD2A6}.bar h3{color:var(--accent)}
.dim .hero{position:absolute;inset:0;height:auto;border:0;border-radius:0;margin:0;filter:brightness(.45)}
.sheet{position:absolute;left:0;right:0;bottom:0;padding:20px 18px 24px;display:grid;gap:12px;background:var(--panel)}
.sheet label{display:flex;gap:10px;align-items:center;min-height:36px;font-size:14px}.sheet input{accent-color:#FF8A24;width:18px;height:18px}
.toast{position:absolute;top:24px;left:50%;transform:translateX(-50%);padding:8px 14px;font-size:13px;white-space:nowrap}
.empty{font-size:13px;color:var(--muted);line-height:1.45}
button:focus-visible{outline:2px solid #FFD2A6;outline-offset:2px}
'''

OPTS = [
 dict(k="a", file="option-a", title="Theme App Option A", name="Option A: Familiar",
  fonts="Manrope:wght@400;600;800",
  pitch="Looks and works like Samsung's own apps: a big title, roomy rounded cards, filled amber buttons. The terminal flavour stays in the wallpaper and icons, which is what your design brief asked for.",
  why="Easiest to use with one hand, and the least surprising.",
  lib_title="Themes", empty="New themes you add later will show up here.",
  css=''':root{--bg:#0d0905;--panel:#1a120b;--accent:#FF8A24;--muted:#A08670;color-scheme:dark}
  main{font-family:Manrope,system-ui,sans-serif}.intro h1{font:800 40px/1.1 Manrope,sans-serif}
  .scr{font-family:Manrope,system-ui,sans-serif}.bar h3{font-size:28px;font-weight:800;margin-top:28px}
  .tcard{background:var(--panel);border-radius:24px;overflow:hidden}.thumb{height:150px}
  .meta{padding:12px 14px 14px;display:grid;gap:2px;font-size:13px}.meta b{font-size:16px}.meta span{color:var(--muted)}
  .pill{justify-self:start;margin-top:6px;font-style:normal;font-size:11px;font-weight:600;background:#3b2210;color:#FF8A24;padding:3px 10px;border-radius:99px}
  .hero{height:170px;border-radius:22px}
  .acts button{border:0;border-radius:99px;padding:12px;background:var(--panel);color:#FFD2A6;font-weight:600}.acts .primary,.sheet .primary{background:#FF8A24;color:#080604;border:0;border-radius:99px;padding:13px;font-weight:700}
  .sheet{border-radius:26px 26px 0 0}.toast{background:#FFD2A6;color:#080604;border-radius:99px;font-weight:600}'''),
 dict(k="b", file="option-b", title="Theme App Option B", name="Option B: Device screen",
  fonts="Silkscreen&family=IBM+Plex+Mono:wght@400;600",
  pitch="The whole app looks like the gadget's own little screen: pixel headings on solid amber bars, square corners, double outlines and monospaced text. Buttons are still big and labelled in plain words.",
  why="The most character, and closest to the Flipper feel. Text is a little denser to read.",
  lib_title="THEMES", empty="&gt; New themes you add later will show up here.",
  css=''':root{--bg:#080604;--panel:#140c06;--accent:#FF8A24;--muted:#A08670;color-scheme:dark}
  main{font-family:"IBM Plex Mono",ui-monospace,monospace}.intro h1{font:400 34px/1.15 Silkscreen,monospace}
  .scr{font-family:"IBM Plex Mono",ui-monospace,monospace;font-size:13px}
  .bar{background:#FF8A24;color:#080604;margin:0 -16px;padding:26px 16px 8px}.bar h3{font:400 18px Silkscreen,monospace}.bar .back{color:#080604}
  .tcard{border:3px double #FF8A24;padding:6px}.thumb{height:140px;image-rendering:pixelated}
  .meta{padding:8px 4px 2px;display:grid;gap:2px}.meta b{font:400 14px Silkscreen,monospace;color:#FF8A24}.meta span{color:var(--muted);font-size:12px}
  .pill{justify-self:start;margin-top:6px;font-style:normal;font-size:11px;border:1px solid #FF8A24;color:#FF8A24;padding:1px 8px}
  .hero{height:160px;border:3px double #FF8A24}
  .acts button{background:transparent;border:2px solid #8F451B;color:#FFD2A6;padding:11px;text-align:left}.acts button::before{content:"> ";color:#FF8A24}
  .acts .primary,.sheet .primary{background:#FF8A24;color:#080604;border:2px solid #FF8A24;padding:12px;font-weight:600}
  .sw h4,.sheet h4{font:400 13px Silkscreen,monospace;color:#FF8A24}
  .sheet{border-top:3px double #FF8A24}.toast{background:#080604;border:2px solid #FF8A24;color:#FF8A24}'''),
 dict(k="c", file="option-c", title="Theme App Option C", name="Option C: Wallpaper first",
  fonts="Archivo:wdth,wght@62..125,500;62..125,800",
  pitch="Each theme fills the screen like a poster, and the controls sit on a dark panel underneath. You browse themes by looking at them, not by reading a list.",
  why="Best once you have several themes to flick through. With only one theme it can feel a bit empty.",
  lib_title="Themes", empty="Swipe sideways to see more themes once you add them.",
  css=''':root{--bg:#080604;--panel:#110a05;--accent:#FF8A24;--muted:#A08670;color-scheme:dark}
  main{font-family:Archivo,system-ui,sans-serif}.intro h1{font:800 44px/1 Archivo,sans-serif;font-stretch:125%}
  .scr{font-family:Archivo,system-ui,sans-serif;padding:0}.scr>.bar{position:absolute;top:0;left:0;right:0;padding:24px 16px;z-index:2}
  .bar h3{font-size:15px;font-weight:500;letter-spacing:.04em}
  .tcard{position:absolute;inset:0}.thumb{position:absolute;inset:0;background-position:center 56%}
  .meta{position:absolute;left:0;right:0;bottom:0;padding:18px 18px 40px;background:var(--panel);display:grid;gap:4px}
  .meta b{font-size:28px;font-weight:800;font-stretch:125%;color:#FF8A24;line-height:1}.meta span{color:var(--muted);font-size:13px}
  .pill{position:absolute;top:-14px;right:18px;font-style:normal;font-size:12px;font-weight:500;background:#FF8A24;color:#080604;padding:5px 12px;border-radius:99px}
  .scr>.empty{position:absolute;bottom:12px;left:18px;right:18px;font-size:11px;margin:0;z-index:2}
  .scr:not(.dim) .hero{height:46%;margin:0 0 4px}.scr:not(.dim)>.strip,.scr:not(.dim)>.acts,.scr:not(.dim)>.sw{margin:0 16px}
  .acts{grid-template-columns:1fr 1fr}.acts button{border:1.5px solid #8F451B;background:transparent;color:#FFD2A6;border-radius:14px;padding:11px 6px;font-weight:500;font-size:13px}
  .acts .primary,.sheet .primary{background:#FF8A24;color:#080604;border:0;border-radius:14px;padding:13px;font-weight:800}
  .sw{display:none}.sheet{border-radius:22px 22px 0 0}.toast{background:#FF8A24;color:#080604;border-radius:14px;font-weight:500}'''),
]

for o in OPTS:
    page = f'''<title>{o["title"]}</title>
<meta name="claude:viewport-fit-sides" content="cover">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family={o["fonts"]}&display=swap">
<style>{BASE}{o["css"]}</style>
<main><div class="intro"><h1>{o["name"]}</h1><p>{o["pitch"]}</p><p><b>Good for:</b> {o["why"]}</p>
<p>The same three screens appear in all three options, so you can compare like with like. The app stays small in every option, at roughly 200 to 300 KB.</p></div>
<div class="shots">{screens(o)}</div></main>'''
    open(f'design/{o["file"]}.html', "w").write(page)
    print(o["file"], round(len(page)/1024), "KB")
