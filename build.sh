#!/bin/sh
# Inside the container: SVG -> PNG -> 16-colour palette -> lossless squeeze.
set -e
for f in src/*.svg; do
  n=$(basename "$f" .svg)
  rsvg-convert "$f" -o "out/$n.png"
  pngquant --force --skip-if-larger --quality 70-100 16 --output "out/$n.png" "out/$n.png" || true
  oxipng -q -o max --strip all "out/$n.png"
done
ls -l out
