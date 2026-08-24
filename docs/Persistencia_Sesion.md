# Persistencia de la sesión de reproducción

## Alcance de la fase 10

NightcallAudio conserva localmente en Room:

- la cola activa, incluidos los elementos duplicados;
- el orden original necesario para desactivar el modo aleatorio;
- la pista seleccionada y su posición;
- los modos de reproducción aleatoria y repetición.

Los cambios estructurales de la cola y de los modos se escriben con un breve
debounce. Durante la reproducción, la posición se guarda como máximo cada cinco
segundos. Las pausas y los cambios de pista o posición fuerzan una actualización
inmediata para no perder el último estado relevante.

## Restauración

La restauración comienza cuando MediaStore ha entregado una biblioteca no vacía.
Cada identificador persistido se contrasta con esa biblioteca:

- las referencias a archivos desaparecidos se descartan;
- las apariciones repetidas de una misma canción se conservan;
- si desapareció la pista seleccionada, se selecciona la siguiente disponible y
  su posición comienza en cero;
- si no queda ninguna pista válida, se limpia la sesión persistida.

El controlador prepara la cola restaurada en pausa. Abrir la aplicación nunca
inicia la emisión de audio: el usuario debe pulsar reproducir.

## Ciclo de vida

`MediaSessionService` mantiene la reproducción cuando se retira la actividad de
la lista de recientes si existe una reproducción activa. Si no hay una sesión
activa, el servicio puede detenerse. La operación de cierre completo expuesta por
el controlador vacía la cola y persiste ese estado; su presentación como acción
explícita en la notificación se completará en la fase 12.

## Verificación

- Pruebas unitarias de reconstrucción del orden normal y aleatorio.
- Pruebas unitarias de duplicados, archivos desaparecidos y pista seleccionada.
- Prueba Room instrumentada de cola duplicada y estado completo.
- Compilación de las pruebas instrumentadas.
- `testDebugUnitTest`, `lintDebug` y `assembleDebug` ejecutados correctamente.

La comprobación final de terminación real del proceso debe realizarse en un
emulador o dispositivo: cargar una cola, cambiar los modos, avanzar en una pista,
forzar la detención del proceso y abrir de nuevo la aplicación. La cola y la
posición deben recuperarse sin reproducción automática.
