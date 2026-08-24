# Cola, shuffle y repetición

Estado: Fase 9 completada.

## Identidad de elementos

Cada incorporación a la cola recibe una identidad interna independiente. Esto permite que una misma canción aparezca varias veces y que cada aparición pueda moverse o eliminarse sin afectar a las demás.

## Orden canónico y activo

`QueueOrderManager` conserva dos órdenes:

- Canónico: orden normal que se restaura al desactivar shuffle.
- Activo: orden que Media3 está reproduciendo en ese momento.

Al activar shuffle, la canción actual se mantiene y el resto se mezcla una sola vez. Cada instancia aparece exactamente una vez antes de completar el ciclo. Al desactivarlo se restaura el orden canónico conservando la canción actual.

## Incorporación de canciones

- «Reproducir a continuación» inserta inmediatamente después de la pista actual.
- «Añadir al final» incorpora al final del orden normal.
- Con shuffle activo, una canción añadida al final entra en una posición aleatoria todavía pendiente.
- Ambas operaciones aceptan canciones repetidas.

## Edición

- Selección directa desde la cola.
- Eliminación mediante deslizamiento hacia la izquierda.
- Reordenación mediante pulsación prolongada y arrastre vertical.
- La edición normal actualiza el orden canónico.

## Repetición

El control del reproductor recorre:

1. Sin repetición.
2. Repetir toda la cola.
3. Repetir la canción actual.

El estado se sincroniza con el modo de repetición de Media3 y se refleja mediante iconos y color.

## Interfaz

El reproductor muestra controles de shuffle y repetición. Las canciones de biblioteca, agrupaciones y búsqueda tienen un menú con «Reproducir a continuación» y «Añadir al final de la cola».

## Pruebas

- La canción actual se conserva al activar shuffle.
- Todas las instancias aparecen una sola vez en el ciclo.
- Al desactivar shuffle se restaura el orden normal.
- Los duplicados se tratan como instancias independientes.
- «Reproducir a continuación» usa la posición correcta.
- La reordenación normal queda conservada.
- Compilación de pruebas instrumentadas, lint y APK debug.
