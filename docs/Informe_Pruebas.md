# Informe de pruebas de NightcallAudio

Fecha: 25 de agosto de 2026  
Fase: 17  
Resultado automatizado local: correcto

## Resumen

| Grupo | Casos | Resultado |
| --- | ---: | --- |
| Pruebas unitarias JVM | 29 | 29 superadas, 0 fallos |
| Pruebas instrumentadas declaradas | 10 | Compilación correcta; ejecución pendiente de dispositivo |
| Android Lint debug | Proyecto completo | Sin errores |
| Ensamblado debug | APK | Correcto |

No había un emulador ni dispositivo conectado mediante ADB durante esta fase. Por
ese motivo no se ejecutó `connectedDebugAndroidTest`. Las pruebas instrumentadas
sí fueron compiladas con `compileDebugAndroidTestKotlin`, lo que valida sus
referencias, recursos y compatibilidad con el código de producción.

## Cobertura unitaria

- Inclusión y exclusión de audios por duración y carpeta.
- Extracción de carpetas en Android moderno y antiguo.
- Agrupación y orden de canciones, artistas, álbumes, géneros y carpetas.
- Búsqueda por título, artista y álbum sin distinguir mayúsculas.
- Procesamiento de bibliotecas de cientos de canciones.
- Estados y versiones de permisos de audio y notificaciones.
- Regla de tres segundos del botón Anterior.
- Decisión ante pistas que fallan.
- Shuffle, orden canónico, duplicados y cambios de cola.
- Adición de canciones durante shuffle y comportamiento de cola vacía.
- Restauración con duplicados y archivos desaparecidos.
- Coordinación de limpieza entre playlists y favoritos.
- Configuración musical de Audio Focus y `audio becoming noisy`.

## Cobertura instrumentada

- Room evita duplicados en playlists y conserva el orden.
- Eliminación en cascada de playlists.
- Favoritos sin duplicados.
- Persistencia completa de cola y modos, incluidos duplicados.
- Retirada de canciones y normalización de posiciones.
- Limpieza de referencias ausentes en playlists y favoritos.
- Construcción de la orden Â«DetenerÂ» de la notificación.
- Componentes Compose para estados vacíos, canciones y permisos.

## Matriz manual de dispositivos

| Entorno | Objetivo principal |
| --- | --- |
| Emulador API 26 | Compatibilidad mínima, permiso de almacenamiento y servicio en segundo plano. |
| Emulador API intermedia | Navegación, Room, recreación de actividad y proceso. |
| Emulador API 37 | Permisos de audio/notificaciones y restricciones actuales de servicio. |
| Dispositivo físico | Bluetooth, auriculares, llamadas, alarmas, widget y batería. |

## Escenarios manuales obligatorios

1. Conceder, denegar y denegar permanentemente los permisos.
2. Cargar cientos de MP3, FLAC, WAV y AAC con metadatos completos e incompletos.
3. Confirmar la exclusión de grabaciones, tonos y audios inferiores a 30 segundos.
4. Reproducir un archivo corrupto y otro no compatible.
5. Comprobar segundo plano, pantalla apagada y retirada de recientes.
6. Terminar el proceso y verificar la restauración sin reproducción automática.
7. Modificar la cola durante reproducción, shuffle y repetición.
8. Crear, renombrar, reordenar y eliminar playlists; marcar favoritos.
9. Probar llamadas, alarmas, navegación y otra aplicación multimedia.
10. Probar desconexión de auriculares y Bluetooth sin reanudación automática.
11. Verificar notificación, pantalla de bloqueo, botones Bluetooth y acción Detener.
12. Añadir, redimensionar y usar el widget tras terminar el proceso.
13. Recorrer toda la interfaz con TalkBack y fuentes al 200 %.
14. Repetir los flujos con notificaciones permitidas y denegadas.

## Comandos de verificación

```powershell
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:compileDebugAndroidTestKotlin
./gradlew.bat :app:connectedDebugAndroidTest
./gradlew.bat :app:lintDebug :app:assembleDebug
```

`connectedDebugAndroidTest` debe ejecutarse cuando Android Studio muestre al menos
un emulador o dispositivo autorizado en ADB.
