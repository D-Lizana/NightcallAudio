# Playlists y favoritos

## Playlists

Las playlists se observan desde Room mediante `StateFlow`, por lo que cualquier
cambio se refleja inmediatamente en las pantallas abiertas. El usuario puede:

- crear una playlist con un nombre no vacío;
- renombrarla;
- eliminarla después de una confirmación explícita;
- añadir canciones desde los menús de biblioteca, búsqueda y detalles;
- retirar canciones sin confirmación y sin modificar el archivo original;
- cambiar el orden con los controles de subir y bajar;
- iniciar la reproducción desde cualquier posición.

Los nombres se normalizan eliminando espacios exteriores, se limitan a 80
caracteres y no pueden repetirse ignorando mayúsculas y minúsculas. Una misma
canción no puede aparecer dos veces dentro de la misma playlist.

## Favoritos

El menú contextual de cada canción permite marcarla o desmarcarla. El reproductor
completo ofrece también un botón de corazón para la pista actual. La pantalla
Favoritos resuelve los identificadores guardados contra la biblioteca local y
permite reproducir la colección desde cualquier canción.

Las referencias a archivos desaparecidos se limpian durante la actualización de
MediaStore. Ninguna operación de playlists o favoritos elimina ni modifica los
archivos musicales.

## Persistencia y errores

Room conserva nombres, relaciones y posiciones. Las operaciones que no pueden
completarse muestran el motivo en español, incluidos nombres vacíos, nombres
repetidos y canciones que ya pertenecen a la playlist elegida.

## Comprobaciones manuales

1. Crear, renombrar y eliminar una playlist, comprobando la confirmación.
2. Intentar reutilizar un nombre y añadir dos veces la misma canción.
3. Añadir canciones desde biblioteca, búsqueda, artista y álbum.
4. Cambiar el orden, cerrar el proceso y comprobar que se conserva.
5. Reproducir desde una posición intermedia de una playlist y de favoritos.
6. Marcar y desmarcar la pista actual desde el reproductor.
7. Retirar una canción y comprobar que el archivo sigue en la biblioteca.
