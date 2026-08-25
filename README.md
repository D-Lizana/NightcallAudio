<div align="center">
  <img src="docs/assets/nightcall_launcher_master.jpg" alt="Logotipo de Nightcall" width="220">

  # Nightcall

  Reproductor de música local para Android, privado, sin cuentas y sin conexión.
</div>

> [!NOTE]
> Nightcall se encuentra actualmente en desarrollo. La compilación `debug` es
> funcional y está destinada a pruebas, pero todavía no constituye una versión
> final publicada.

## Descripción

Nightcall es una aplicación nativa para reproducir y organizar la música guardada
en un dispositivo Android. La biblioteca se obtiene directamente mediante
MediaStore y todos los datos creados por el usuario permanecen en el dispositivo.
No utiliza servidores, servicios de streaming, cuentas ni sincronización en la
nube.

La aplicación está diseñada en español, utiliza un tema oscuro inspirado en su
identidad visual neón y es compatible desde Android 8.0 Oreo (API 26).

## Funciones principales

- Detección automática de cambios en la biblioteca musical del dispositivo.
- Biblioteca organizada por canciones, artistas, álbumes, géneros y carpetas.
- Búsqueda local por título, artista y álbum.
- Reproducción en primer y segundo plano mediante Media3 y ExoPlayer.
- Controles de reproducción, saltos de 10 segundos y barra de progreso.
- Minirreproductor, reproductor completo y cola reordenable.
- Modos de reproducción aleatoria, repetir cola y repetir canción.
- Acciones «Reproducir a continuación» y «Añadir a la cola».
- Persistencia de la cola, pista actual, posición y modos de reproducción.
- Creación y gestión de playlists dentro de la aplicación.
- Colección de canciones favoritas.
- Notificación multimedia, controles de bloqueo y dispositivos Bluetooth.
- Pausa segura ante desconexiones de audio y gestión de interrupciones temporales.
- Widget con pista actual, anterior, reproducir/pausar y siguiente.
- Carátulas de álbum obtenidas desde la biblioteca del dispositivo.
- Tema oscuro con acentos rosa neón, cian petróleo y coral.

## Selección de archivos

Nightcall consulta únicamente los audios que Android clasifica como música y
descarta:

- Audios con una duración inferior a 30 segundos.
- Grabaciones de voz y llamadas.
- Alarmas, notificaciones y tonos de llamada.
- Audios y notas de voz de WhatsApp y WhatsApp Business.

La aplicación no elimina, renombra ni modifica los archivos o sus metadatos.

## Privacidad

- No requiere una cuenta de usuario.
- No envía la biblioteca musical a Internet.
- No incorpora streaming ni sincronización en la nube.
- Playlists, favoritos y sesión de reproducción se almacenan localmente con Room.
- Los permisos de audio se utilizan exclusivamente para leer y reproducir los
  archivos musicales del dispositivo.

## Requisitos

- Android Studio con soporte para el Android Gradle Plugin utilizado por el
  proyecto.
- SDK de Android 37 instalado para compilar.
- Dispositivo o emulador con Android 8.0 Oreo/API 26 o posterior.
- JDK compatible con la versión actual de Android Gradle Plugin; se recomienda
  utilizar el JDK integrado en Android Studio.

La reproducción de MP3, FLAC, WAV y AAC depende de los decodificadores disponibles
en el dispositivo Android.

## Compilación

1. Clona el repositorio:

   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd NightcallAudio
   ```

2. Abre la carpeta en Android Studio y espera a que finalice la sincronización de
   Gradle.

3. Compila una APK de depuración desde Android Studio o mediante terminal:

   ```bash
   ./gradlew assembleDebug
   ```

   En Windows:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

4. La APK se genera en:

   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

También puedes instalarla directamente en un emulador o dispositivo conectado con
el botón **Run** de Android Studio.

## Permisos

Según la versión de Android, Nightcall solicita:

- Lectura del almacenamiento en Android 12L/API 32 y anteriores.
- Acceso a música y audio en Android 13/API 33 y posteriores.
- Notificaciones en las versiones que requieren autorización explícita.
- Servicio en primer plano para mantener una sesión multimedia activa.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Arquitectura MVVM y flujo unidireccional de estado
- Kotlin Coroutines, Flow y StateFlow
- AndroidX Media3, ExoPlayer y MediaSessionService
- MediaStore
- Room y KSP
- Navigation Compose
- Coil

## Arquitectura

El proyecto separa sus responsabilidades en cuatro áreas principales:

```text
Compose → ViewModel → casos de uso → contratos de repositorio
                                      ↑
                         MediaStore / Media3 / Room
```

- **Dominio:** modelos, contratos y reglas funcionales independientes de Android.
- **Datos:** lectura de MediaStore y persistencia local con Room.
- **Reproducción:** ExoPlayer, MediaSessionService y controlador multimedia.
- **Presentación:** pantallas Compose, navegación, estados y ViewModels.

La creación de dependencias se realiza mediante un contenedor manual compartido por
la aplicación. Consulta [la documentación de arquitectura](docs/Arquitectura_NightcallAudio.md)
para conocer la organización interna.

## Pruebas y calidad

Ejecuta las pruebas unitarias:

```bash
./gradlew testDebugUnitTest
```

Ejecuta el análisis estático:

```bash
./gradlew lintDebug
```

Las pruebas cubren, entre otros aspectos, filtros de audio, biblioteca, permisos,
reglas de reproducción, orden de la cola, restauración de sesiones y persistencia
con Room. Las pruebas instrumentadas requieren un emulador o dispositivo conectado.

## Estado y planificación

El alcance, las decisiones funcionales y las fases del desarrollo se encuentran en
el [plan de trabajo](docs/Plan_Trabajo_NightcallAudio.md). La aplicación no incluye
Android Auto, Wear OS específico, ecualizador, temporizador, edición de metadatos,
streaming ni importación o exportación de playlists.

## Licencia

Este repositorio no tiene actualmente una licencia de código abierto. Salvo que se
añada una licencia explícita, se reservan todos los derechos.
