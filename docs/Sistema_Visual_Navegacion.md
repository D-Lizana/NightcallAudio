# Sistema visual y navegación

Estado: Fase 4 completada.

## Principios visuales

- Tema oscuro fijo, sin tema claro ni colores dinámicos.
- Superficie principal casi negra y tarjetas con contraste moderado.
- Azul nocturno como color de acción.
- Esquinas amplias para tarjetas, controles y carátulas.
- Metadatos secundarios con menor énfasis visual.
- Áreas táctiles compatibles con las recomendaciones de Material 3.
- Carátula genérica musical hasta incorporar la carga de imágenes reales.

## Navegación principal

La barra inferior conserva cuatro destinos:

1. Biblioteca.
2. Buscar.
3. Playlists.
4. Favoritos.

El minirreproductor aparece sobre la barra cuando existe una canción seleccionada. Permite abrir el reproductor, pausar o reanudar y avanzar a la siguiente canción.

## Rutas

```text
library ─┐
search ──┼─ barra inferior + minirreproductor
playlists┤
favorites┘

minirreproductor → player → queue
```

`player` y `queue` son destinos secundarios y no muestran la barra inferior.

## Biblioteca

La biblioteca dispone de filtros visuales para:

- Canciones.
- Artistas.
- Álbumes.
- Géneros.
- Carpetas.

Canciones utiliza la lista compartida `TrackList`. Las demás categorías tienen una presentación inicial que se completará con los datos definitivos de MediaStore en la Fase 6.

## Estados comunes

`MessageState` representa estados vacíos y errores con título, explicación y acción opcional. `TrackRow` y `TrackList` centralizan la representación de canciones para mantener consistencia entre biblioteca, búsqueda y cola.

## Accesibilidad inicial

- Los botones interactivos tienen descripciones en español.
- Los iconos decorativos no generan anuncios redundantes.
- El texto utiliza la tipografía escalable de Material 3.
- Los estados no dependen únicamente del color.

La validación completa con TalkBack y tamaños de fuente se realizará en la Fase 16.
