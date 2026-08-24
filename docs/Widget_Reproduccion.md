# Widget de reproducción

## Presentación

NightcallAudio incluye un widget de pantalla de inicio con tema oscuro. Cuando
existe una sesión muestra el título y el artista de la canción seleccionada, junto
con los controles anterior, reproducir/pausar y siguiente. Al pulsar el contenido
principal se abre la aplicación.

Si no existe una sesión restaurable, el widget presenta Â«NightcallAudioÂ» y Â«Sin
sesión activaÂ» y oculta los controles. Su tamaño puede modificarse horizontal y
verticalmente desde el lanzador.

## Restauración y controles

Cuando Android crea o actualiza el widget, se consulta la sesión guardada y se
contrastan sus identificadores con MediaStore. Esto permite mostrar la última
canción disponible incluso después de recrear el proceso.

Si se pulsa un control con el proceso recreado, el widget carga la biblioteca y
solicita la restauración antes de ejecutar la orden. Esta orden pendiente solo se
crea por una acción explícita del usuario: abrir normalmente la aplicación continúa
restaurando la sesión sin reproducir audio.

## Actualizaciones

No existe una actualización periódica. El widget se redibuja solamente cuando
cambia la pista, el estado reproducir/pausar o la disponibilidad de la sesión.
La posición que avanza cada medio segundo no provoca actualizaciones del widget.

## Comprobaciones manuales

1. Añadir el widget desde el selector del lanzador.
2. Comprobar su estado sin sesión y con una cola activa.
3. Probar anterior, reproducir/pausar y siguiente con la aplicación abierta y cerrada.
4. Terminar el proceso y pulsar reproducir desde el widget.
5. Detener la sesión desde la notificación y comprobar el estado inactivo.
6. Redimensionar el widget hasta sus dimensiones mínimas y máximas.
7. Repetir al menos en API 26 y en la versión objetivo.
