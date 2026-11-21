#!/bin/sh
# Arranque del contenedor de la API.
# Railway (y otros proveedores) montan el volumen /data como root. El contenedor
# inicia como root solo para entregar /data al usuario comercio; despues la API
# se ejecuta sin privilegios.
set -e

if [ "$(id -u)" = "0" ]; then
    chown -R comercio:comercio /data
    exec setpriv --reuid=comercio --regid=comercio --init-groups \
        java -XX:MaxRAMPercentage=75 -jar /home/comercio/app.jar "$@"
fi

exec java -XX:MaxRAMPercentage=75 -jar /home/comercio/app.jar "$@"
