FROM alpine:3.20
RUN apk add --no-cache rsvg-convert pngquant oxipng font-dejavu
WORKDIR /w
