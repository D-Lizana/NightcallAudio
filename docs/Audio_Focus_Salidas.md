# Audio Focus y salidas de audio

## Configuración

NightcallAudio declara el contenido como música con uso multimedia mediante
`AudioAttributes`. ExoPlayer recibe estos atributos con la gestión de Audio Focus
activada, por lo que Media3 coordina la sesión con llamadas, alarmas, navegación y
otras aplicaciones multimedia.

La aplicación también activa `handleAudioBecomingNoisy`. Android emite este evento
cuando una ruta privada deja de estar disponible, por ejemplo al desconectar
auriculares con cable o una salida Bluetooth. ExoPlayer pausa la reproducción y
no la reanuda al conectar de nuevo una salida.

## Comportamiento esperado

| Situación | Resultado |
| --- | --- |
| Interrupción temporal | Pausa y reanudación al recuperar Audio Focus. |
| Pausa manual durante la interrupción | Permanece pausado al recuperar Audio Focus. |
| Pérdida permanente | Permanece pausado. |
| Solicitud de ducking | Media3 reduce temporalmente el volumen cuando el sistema lo permite. |
| Desconexión de auriculares o Bluetooth | Pausa inmediata, sin reanudación al reconectar. |
| Altavoz o auriculares Bluetooth conectados | Android enruta el audio sin lógica especial de la aplicación. |

La pausa manual cambia `playWhenReady` a falso. De este modo, una recuperación
posterior de Audio Focus no puede iniciar audio que el usuario haya detenido.

## Validación

Las pruebas unitarias verifican los atributos musicales y que ambas integraciones
de Media3 estén activadas. Las siguientes comprobaciones dependen del sistema de
audio y deben ejecutarse en dispositivo o emulador:

1. Reproducir música y provocar una interrupción temporal; comprobar pausa y reanudación.
2. Repetir la interrupción, pulsar pausa y comprobar que no se reanuda.
3. Iniciar otra aplicación multimedia y comprobar la pérdida permanente.
4. Probar una indicación de navegación o fuente que solicite ducking.
5. Desconectar auriculares con cable y comprobar que no suena por el altavoz.
6. Desconectar y reconectar un altavoz o auriculares Bluetooth y comprobar que sigue pausado.
7. Reproducir expresamente después de conectar Bluetooth y comprobar el enrutamiento.

El emulador permite validar Audio Focus entre aplicaciones, pero la desconexión
y el enrutamiento Bluetooth deben confirmarse preferiblemente en un dispositivo
físico.
