#!/bin/sh
# Inside the container: SVG -> PNG -> small palette -> lossless squeeze.
set -e
mkdir -p out/icons
render() { # $1 svg, $2 png, $3 colours
  rsvg-convert "$1" -o "$2"
  pngquant --force --skip-if-larger --quality 60-100 "$3" --output "$2" "$2" || true
  oxipng -q -o max --strip all "$2"
}
render src/wallpaper.svg out/wallpaper.png 16
for f in src/icons/*.svg; do render "$f" "out/icons/$(basename "$f" .svg).png" 8; done
ls -l out/wallpaper.png; du -cb out/icons/*.png | tail -1
