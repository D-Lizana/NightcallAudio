# Robustez y accesibilidad

## Recuperación ante errores

- Los errores de MediaStore producen un estado recuperable con una acción para reintentar.
- Las pistas corruptas o incompatibles se omiten y la reproducción continúa con la siguiente cuando existe.
- Las referencias desaparecidas se limpian de cola, playlists y favoritos sin borrar archivos.
- Los errores de reproducción y de operaciones locales se muestran mediante mensajes globales en español.
- Si `MediaController` pierde la conexión con el servicio, se informa al usuario y se crea una nueva conexión automáticamente.
- La ausencia o revocación del permiso de audio vuelve a presentar el flujo de permisos y el acceso a Ajustes cuando corresponde.

## Accesibilidad

Los controles interactivos utilizan los tamaños mínimos proporcionados por
Material 3 y tienen etiquetas en español. Las carátulas informativas describen el
álbum y los iconos puramente decorativos se excluyen de TalkBack.

Las filas de canciones anuncian la acción de reproducción. Los elementos de la
cola incorporan acciones accesibles para subir, bajar y eliminar, de modo que el
arrastre y el deslizamiento no sean la única forma de operarlos.

Shuffle y repetición no dependen solamente del color: presentan insignias visibles
y descripciones que indican tanto el estado actual como la siguiente acción.

Los estados de carga, vacío y error contienen texto, no solo iconos o color. Las
pantallas potencialmente altas son desplazables y los textos secundarios permiten
elipsis, facilitando el uso de fuentes grandes sin ocultar controles esenciales.

## Comprobaciones manuales

1. Recorrer biblioteca, búsqueda, reproductor, cola, playlists y favoritos con TalkBack.
2. Reordenar y eliminar elementos de la cola mediante acciones de accesibilidad.
3. Probar escalas de fuente del 100 %, 150 % y 200 %.
4. Probar alto contraste y una simulación de daltonismo.
5. Revocar el permiso de audio mientras la aplicación está en segundo plano.
6. Eliminar o mover un archivo incluido en cola, playlist y favoritos.
7. Probar una pista corrupta y otra con un formato no admitido por el dispositivo.
8. Forzar la detención del servicio durante el uso y verificar la reconexión.
