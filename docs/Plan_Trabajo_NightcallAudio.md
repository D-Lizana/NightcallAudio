# Plan de trabajo para NightcallAudio

## Reproductor local de música para Android

Versión del documento: 1.0  
Fecha: 24 de agosto de 2026  
Estado: requisitos funcionales iniciales confirmados

## 1. Objetivo

Desarrollar NightcallAudio, una aplicación nativa para Android destinada a reproducir y organizar música almacenada localmente en el dispositivo. La aplicación funcionará desde Android 8.0 Oreo (API 26), tendrá una interfaz exclusivamente oscura y en español, y estará optimizada para bibliotecas de cientos de canciones.

El producto no dependerá de cuentas de usuario, servidores, servicios de streaming ni sincronización en la nube. Los datos creados por el usuario, como playlists, favoritos, cola y posición de reproducción, se almacenarán únicamente en el dispositivo.

## 2. Alcance confirmado

### 2.1 Funcionalidades incluidas

- Escaneo de música local mediante MediaStore.
- Compatibilidad con MP3, FLAC, WAV y AAC cuando el dispositivo disponga del decodificador necesario.
- Biblioteca organizada por canciones, artistas, álbumes, géneros y carpetas.
- Búsqueda local por canción, artista y álbum.
- Reproducción, pausa, canción siguiente y canción anterior.
- Avance y retroceso de 10 segundos.
- Barra de progreso interactiva.
- Reproducción en segundo plano y con la pantalla apagada.
- Cola visible, reordenable y persistente.
- Acciones «Reproducir a continuación» y «Añadir al final».
- Repetición de canción, repetición de cola y reproducción aleatoria sin repeticiones durante el ciclo.
- Playlists creadas y administradas dentro de la aplicación.
- Favoritos.
- Persistencia de cola, canción, posición y modos de reproducción.
- Notificación multimedia y controles en la pantalla de bloqueo.
- Controles procedentes de dispositivos Bluetooth.
- Audio Focus y tratamiento de interrupciones.
- Pausa al desconectar auriculares o una salida Bluetooth.
- Reproducción mediante altavoces y auriculares Bluetooth.
- Widget con anterior, reproducir/pausar y siguiente.
- Tema oscuro fijo y textos en español.
- Carátulas embebidas y marcador genérico cuando no estén disponibles.

### 2.2 Funcionalidades excluidas

- Streaming de música.
- Cuentas, backend o sincronización en la nube.
- Android Auto.
- Aplicación específica para Wear OS. Un reloj podrá usar los controles multimedia generales que proporcione su sistema.
- Importación o exportación de playlists.
- Eliminación, renombrado o edición de archivos musicales.
- Edición de metadatos.
- Ecualizador.
- Temporizador de apagado.
- Control de velocidad.
- Temas claros, colores dinámicos o personalización visual.
- Idiomas distintos del español.

## 3. Reglas funcionales confirmadas

### 3.1 Inclusión de archivos

- Se incluirán archivos musicales obtenidos mediante MediaStore.
- Se excluirán grabaciones, tonos de llamada, alarmas y sonidos de notificación.
- Se excluirán audios con una duración inferior a 30 segundos.
- El filtro combinará duración, clasificación de MediaStore y carpetas conocidas, porque la clasificación puede variar entre fabricantes.
- Un archivo corrupto, desaparecido o no compatible no deberá cerrar la aplicación. Se informará del problema y se continuará de forma segura.

### 3.2 Inicio de reproducción

- Al pulsar una canción se sustituirá la cola por el contenido del contexto visible: lista, álbum, artista, carpeta, playlist, favoritos o resultados de búsqueda.
- La reproducción comenzará en la canción seleccionada.
- Se admitirán canciones repetidas en la cola.

### 3.3 Botón Anterior

- Si han transcurrido más de 3 segundos, el botón reiniciará la canción actual.
- Si han transcurrido 3 segundos o menos, reproducirá la canción anterior.
- Si no existe una canción anterior, se aplicará el modo de repetición activo.

### 3.4 Orden predeterminado

- Canciones: orden alfabético por título.
- Artistas: orden alfabético.
- Álbumes: orden alfabético.
- Canciones de álbum: número de disco y número de pista cuando existan; en caso contrario, título.
- Carpetas: orden alfabético.
- Playlists: orden definido por el usuario.

### 3.5 Persistencia

- Se guardarán la cola y su orden, la canción seleccionada, la posición, el modo aleatorio y el modo de repetición.
- Al abrir la aplicación se restaurará el estado, pero no se emitirá audio hasta que el usuario pulse reproducir.
- Al retirar la aplicación de recientes, la reproducción continuará si estaba activa.
- El usuario podrá detener la sesión desde la notificación.
- Las referencias a archivos que ya no existan se eliminarán de la cola de forma segura.

### 3.6 Interrupciones

- Ante una interrupción temporal, la reproducción se pausará y se reanudará cuando termine.
- Si el usuario pulsa pausa durante la interrupción, no habrá reanudación automática.
- Ante una pérdida permanente de Audio Focus, la reproducción permanecerá pausada.
- Al desconectar auriculares o Bluetooth, la reproducción se pausará y no se reanudará automáticamente al reconectar.

### 3.7 Playlists y favoritos

- Se podrán crear, renombrar y eliminar playlists.
- Antes de eliminar una playlist se solicitará confirmación.
- Retirar una canción de una playlist, favoritos o cola no requerirá confirmación y nunca eliminará el archivo original.
- Las canciones de una playlist podrán reordenarse.

## 4. Arquitectura objetivo

### 4.1 Tecnologías

- Kotlin.
- Jetpack Compose y Material 3.
- Arquitectura MVVM con flujo unidireccional de estado.
- ViewModel, Kotlin Coroutines y StateFlow.
- Media3 ExoPlayer.
- MediaSessionService y MediaController.
- MediaStore.
- Room.
- Navigation Compose.
- Biblioteca de carga y caché de imágenes compatible con Compose.

### 4.2 Responsabilidades

- UI: representa estados y envía acciones del usuario.
- ViewModels: coordinan casos de uso y estado de cada pantalla.
- Casos de uso: contienen reglas funcionales reutilizables.
- Repositorios: abstraen MediaStore, Room y reproducción.
- MediaSessionService: es la fuente única del estado de reproducción.
- Room: conserva playlists, favoritos, cola y estado de la sesión.
- MediaStore: proporciona la biblioteca musical del dispositivo.

### 4.3 Estructura inicial

```text
com.nightcallaudio
├── data
│   ├── database
│   ├── mediastore
│   ├── mapper
│   └── repository
├── domain
│   ├── model
│   └── usecase
├── playback
│   ├── controller
│   ├── notification
│   └── service
├── ui
│   ├── components
│   ├── favorites
│   ├── library
│   ├── navigation
│   ├── player
│   ├── playlists
│   ├── queue
│   ├── search
│   └── theme
└── MainActivity
```

## 5. Plan de ejecución

## Fase 1. Especificación y criterios de aceptación

1. Consolidar los requisitos de este documento en historias o casos de uso.
2. Definir criterios de aceptación para cada funcionalidad.
3. Documentar estados vacíos, carga, error y permisos.
4. Establecer una lista trazable entre requisitos, implementación y pruebas.
5. Confirmar el diseño de navegación y los wireframes principales.

Entregable: especificación funcional cerrada y matriz de aceptación.

## Fase 2. Preparación técnica

1. Revisar el proyecto Compose existente.
2. Mantener `minSdk = 26` y confirmar compileSdk y targetSdk.
3. Revisar compatibilidad entre Gradle, Android Gradle Plugin, Kotlin y JDK.
4. Incorporar Media3, Room, Navigation, ViewModel, Coroutines y carga de imágenes.
5. Centralizar versiones en `libs.versions.toml`.
6. Configurar KSP para Room si corresponde.
7. Configurar variantes debug y release.
8. Activar optimización y reglas de conservación para release.
9. Verificar compilación y pruebas iniciales.

Entregable: proyecto compilable con dependencias y configuración estable.

## Fase 3. Base arquitectónica

1. Crear paquetes y módulos lógicos.
2. Definir modelos de dominio: Track, Artist, Album, Genre, Folder, Playlist y estado de reproducción.
3. Definir interfaces de repositorios.
4. Definir casos de uso.
5. Crear modelos de estado y eventos de UI.
6. Establecer inyección de dependencias manual o mediante una biblioteca apropiada.
7. Preparar dobles de prueba para repositorios y controlador de reproducción.

Entregable: esqueleto arquitectónico desacoplado y comprobable.

## Fase 4. Sistema visual y navegación

1. Crear una paleta oscura fija.
2. Eliminar el tema claro y desactivar colores dinámicos.
3. Definir tipografía, espaciado, formas e iconografía.
4. Crear el grafo de navegación.
5. Diseñar biblioteca, detalles, reproductor, cola, playlists, favoritos y búsqueda.
6. Diseñar minirreproductor persistente.
7. Crear estados de carga, vacío, error y permiso denegado.
8. Crear marcador de carátula genérico.

Entregable: navegación y componentes visuales básicos sin datos reales.

## Fase 5. Permisos

1. Declarar permisos de lectura compatibles con API 26 y posteriores.
2. Usar el permiso de almacenamiento en versiones antiguas y el permiso específico de audio en versiones modernas.
3. Solicitar permiso de notificaciones cuando corresponda.
4. Implementar explicación previa, denegación y denegación permanente.
5. Proporcionar acceso a Ajustes cuando el permiso no pueda solicitarse otra vez.
6. Detectar permisos revocados durante el uso.

Entregable: flujo completo de permisos probado en varias versiones de Android.

## Fase 6. Biblioteca mediante MediaStore

1. Implementar la consulta de canciones.
2. Recuperar ID, URI, título, artista, álbum, duración, género, pista, disco, fecha, carpeta y carátula.
3. Aplicar exclusión de grabaciones, tonos, alarmas, notificaciones y audios inferiores a 30 segundos.
4. Normalizar metadatos ausentes y valores desconocidos.
5. Evitar duplicados.
6. Crear agrupaciones por artista, álbum, género y carpeta.
7. Aplicar reglas de ordenación.
8. Implementar búsqueda local.
9. Implementar actualización de biblioteca.
10. Detectar archivos eliminados o movidos.
11. Ejecutar consultas fuera del hilo principal.
12. Probar con cientos de archivos.

Entregable: biblioteca real, filtrada, ordenada y consultable.

## Fase 7. Persistencia con Room

1. Diseñar entidades para playlists, relaciones ordenadas, favoritos, cola y estado de reproducción.
2. Implementar DAOs observables mediante Flow.
3. Crear repositorios de playlists y favoritos.
4. Implementar transacciones para operaciones compuestas.
5. Persistir el orden de playlists y cola.
6. Persistir posición y modos.
7. Limpiar referencias inválidas sin eliminar archivos.
8. Preparar migraciones de esquema.
9. Probar DAOs, transacciones y migraciones.

Entregable: almacenamiento local fiable y cubierto por pruebas.

## Fase 8. Núcleo de reproducción

1. Crear MediaSessionService.
2. Inicializar ExoPlayer y MediaSession.
3. Declarar el servicio en el manifiesto.
4. Crear la conexión mediante MediaController.
5. Convertir Track en MediaItem.
6. Implementar reproducción, pausa, siguiente, anterior, saltos y búsqueda temporal.
7. Implementar selección directa y sustitución de cola según el contexto visible.
8. Sincronizar canción, posición, duración, cola y modos.
9. Manejar pistas corruptas, desaparecidas o incompatibles.
10. Liberar recursos correctamente.

Entregable: reproducción local operativa en primer y segundo plano.

## Fase 9. Cola y modos de reproducción

1. Mostrar y observar la cola.
2. Implementar «Reproducir a continuación» y «Añadir al final».
3. Permitir selección directa, elementos repetidos y eliminación por deslizamiento.
4. Implementar reordenación mediante arrastrar y soltar.
5. Implementar sin repetición, repetir cola y repetir canción.
6. Implementar shuffle estable sin repetición hasta completar el ciclo.
7. Mantener la canción actual al activar o desactivar shuffle.
8. Incorporar canciones añadidas mientras shuffle está activo.
9. Aplicar la regla de 3 segundos al botón Anterior.
10. Probar cambios simultáneos de cola y reproducción.

Entregable: gestión completa y predecible de la cola.

## Fase 10. Persistencia de la sesión

1. Guardar la cola y los modos cada vez que cambien.
2. Guardar la posición periódicamente sin provocar escrituras excesivas.
3. Restaurar cola, canción y posición al iniciar.
4. Validar que los archivos restaurados todavía existen.
5. No reproducir automáticamente al abrir la aplicación.
6. Mantener la reproducción al retirar la aplicación de recientes.
7. Permitir detener completamente la sesión desde la notificación.
8. Probar restauración después de cerrar y terminar el proceso.

Entregable: continuidad fiable entre sesiones.

## Fase 11. Audio Focus y salidas de audio

1. Configurar atributos de audio musical.
2. Gestionar ganancia y pérdida de Audio Focus.
3. Pausar y reanudar después de interrupciones temporales.
4. Evitar la reanudación si hubo una pausa manual.
5. Permanecer pausado después de pérdidas permanentes.
6. Gestionar reducción temporal de volumen cuando proceda.
7. Pausar ante `audio becoming noisy`.
8. Gestionar desconexiones Bluetooth.
9. No reanudar automáticamente tras reconectar.
10. Validar salida por altavoces y auriculares Bluetooth.
11. Probar llamadas, alarmas, navegación y competencia con otras aplicaciones multimedia.

Entregable: integración segura con el sistema de audio.

## Fase 12. Notificación, bloqueo y Bluetooth

1. Crear el canal de notificación multimedia.
2. Configurar la notificación de Media3.
3. Mostrar título, artista, carátula y controles.
4. Añadir una acción para detener la sesión.
5. Sincronizar todos los estados con MediaSession.
6. Validar controles en pantalla de bloqueo.
7. Validar botones enviados por auriculares y altavoces Bluetooth.
8. Cumplir las restricciones de servicio en primer plano de cada versión.
9. Comprobar el comportamiento sin permiso de notificaciones.

Entregable: control externo completo de la sesión.

## Fase 13. Interfaz conectada

1. Conectar la biblioteca con datos reales.
2. Implementar listas de canciones, artistas, álbumes, géneros y carpetas.
3. Crear detalles de artista y álbum.
4. Implementar búsqueda por canción, artista y álbum.
5. Conectar minirreproductor y reproductor completo.
6. Conectar barra de progreso y controles.
7. Mostrar la cola y sus gestos.
8. Crear menús contextuales.
9. Cargar y almacenar en caché las carátulas.
10. Mantener los estados al navegar y recrear la actividad.
11. Adaptar el diseño a distintos tamaños de pantalla.

Entregable: flujo principal de usuario terminado.

## Fase 14. Playlists y favoritos

1. Crear, mostrar y renombrar playlists.
2. Validar nombres vacíos y gestionar nombres repetidos.
3. Solicitar confirmación antes de eliminar una playlist.
4. Añadir y retirar canciones sin modificar archivos originales.
5. Reordenar canciones.
6. Reproducir desde cualquier posición.
7. Marcar y desmarcar favoritos.
8. Crear la colección de favoritos.
9. Sincronizar cambios inmediatamente en todas las pantallas.

Entregable: organización local personalizada.

## Fase 15. Widget

1. Diseñar el widget oscuro.
2. Mostrar canción y artista cuando haya una sesión.
3. Añadir anterior, reproducir/pausar y siguiente.
4. Abrir la aplicación al pulsar el contenido principal.
5. Mostrar un estado inactivo cuando no haya sesión restaurable.
6. Sincronizar cambios sin actualizaciones innecesarias.
7. Probar redimensionado y recreación del widget.

Entregable: widget funcional y sincronizado.

## Fase 16. Errores, accesibilidad y robustez

1. Tratar permisos ausentes, biblioteca vacía, archivos corruptos, formatos incompatibles y referencias obsoletas.
2. Recuperarse de una desconexión del controlador o del servicio.
3. Proporcionar mensajes y acciones comprensibles en español.
4. Añadir descripciones accesibles a controles e imágenes.
5. Garantizar tamaños táctiles, contraste y compatibilidad con TalkBack.
6. Probar tamaños de fuente grandes.
7. Evitar que el color sea el único indicador de estado.
8. Revisar todos los estados de carga y error.

Entregable: aplicación accesible y resistente a fallos habituales.

## Fase 17. Pruebas

### Pruebas unitarias

- Filtros y ordenación de MediaStore.
- Agrupación y búsqueda.
- Regla del botón Anterior.
- Repetición y shuffle.
- Casos de uso de cola, playlists y favoritos.
- Persistencia y restauración.
- Decisiones de Audio Focus.
- Limpieza de referencias inválidas.

### Pruebas de integración

- DAOs y migraciones de Room.
- MediaStore con diferentes metadatos.
- MediaSessionService y MediaController.
- Notificación multimedia.
- Restauración después de terminar el proceso.
- Cambios de cola durante la reproducción.

### Pruebas de interfaz

- Permisos.
- Navegación.
- Biblioteca y búsqueda.
- Reproductor y barra de progreso.
- Cola.
- Playlists y favoritos.
- Estados vacíos y de error.

### Matriz manual de dispositivos y situaciones

- Android 8.0/API 26, una versión intermedia y la versión objetivo.
- Emulador y dispositivo físico.
- Biblioteca con cientos de canciones.
- MP3, FLAC, WAV y AAC.
- Archivos corruptos y metadatos incompletos.
- Auriculares cableados, Bluetooth y altavoz Bluetooth.
- Llamadas, alarmas y otra aplicación multimedia.
- Pantalla apagada y aplicación retirada de recientes.
- Proceso terminado y sesión restaurada.
- Notificaciones permitidas y denegadas.

Entregable: informe de pruebas y defectos resueltos.

## Fase 18. Rendimiento y calidad

1. Medir carga y actualización de la biblioteca.
2. Detectar trabajo en el hilo principal y posibles ANR.
3. Evitar consultas repetidas a MediaStore.
4. Limitar tamaño y memoria de carátulas.
5. Revisar recomposiciones de Compose.
6. Buscar fugas de memoria en actividad, controlador y servicio.
7. Medir consumo de batería durante reproducción prolongada.
8. Verificar que las escrituras periódicas de posición sean eficientes.
9. Ejecutar análisis estático y corregir advertencias relevantes.

Entregable: aplicación fluida con cientos de canciones y reproducción prolongada estable.

## Fase 19. Preparación de publicación

1. Finalizar nombre, icono y recursos gráficos.
2. Revisar todos los textos en español.
3. Eliminar código y recursos provisionales.
4. Configurar optimización y reglas de release.
5. Definir versionCode y versionName.
6. Crear y proteger la clave de firma.
7. Generar APK/AAB firmado.
8. Ejecutar pruebas de regresión sobre release.
9. Revisar permisos y declaraciones del manifiesto.
10. Preparar política de privacidad que indique el tratamiento exclusivamente local.
11. Documentar compilación, pruebas, arquitectura y publicación.

Entregable: versión candidata firmada y documentación técnica.

## 6. Hitos recomendados

1. Hito técnico: detectar una canción, reproducirla desde MediaSessionService y controlarla desde una notificación.
2. MVP interno: biblioteca, reproductor, cola, persistencia, Audio Focus y tema oscuro.
3. Beta funcional: búsqueda, playlists, favoritos, widget y tratamiento completo de errores.
4. Versión candidata: accesibilidad, pruebas, rendimiento y compilación release.
5. Versión 1.0: criterios de aceptación cumplidos y defectos críticos resueltos.

## 7. Criterios de aceptación globales

- Funciona desde Android 8.0 Oreo/API 26.
- Toda la interfaz visible al usuario está en español y utiliza tema oscuro.
- Detecta y organiza una biblioteca de cientos de canciones sin bloquear la interfaz.
- Excluye grabaciones, tonos, alarmas, notificaciones y audios inferiores a 30 segundos.
- Reproduce los formatos admitidos por el dispositivo sin fallar ante archivos problemáticos.
- Mantiene la reproducción en segundo plano, con pantalla apagada y al retirar la aplicación de recientes.
- Controla la sesión desde aplicación, notificación, pantalla de bloqueo, widget y dispositivos Bluetooth.
- Restaura cola, canción, posición y modos sin comenzar a emitir audio automáticamente.
- Reanuda después de una interrupción temporal salvo pausa manual.
- Pausa al desconectar una salida de audio y no se reanuda al reconectar.
- Permite administrar cola, playlists, favoritos y búsqueda local.
- No modifica ni elimina los archivos musicales.
- No requiere red, cuenta ni servicios externos.
- Supera las pruebas unitarias, de integración, de interfaz y la matriz manual acordada.

## 8. Definición de terminado

Una funcionalidad se considerará terminada cuando:

- Cumpla sus criterios de aceptación.
- Disponga de pruebas proporcionales a su riesgo.
- Gestione carga, vacío y errores aplicables.
- Sea accesible y tenga textos definitivos en español.
- No introduzca fallos en la reproducción en segundo plano.
- Haya sido revisada en una compilación debug y, antes de publicar, en release.
- Su documentación técnica relevante esté actualizada.

## 9. Secuencia crítica

```text
Especificación
→ preparación técnica
→ arquitectura
→ permisos y MediaStore
→ Room
→ Media3 y servicio
→ cola y persistencia
→ Audio Focus y notificación
→ interfaz
→ playlists, favoritos y widget
→ pruebas y rendimiento
→ publicación
```

El desarrollo debe validar pronto el núcleo MediaStore–Media3–MediaSessionService. El primer incremento no será una colección de pantallas completas, sino una prueba vertical capaz de encontrar una canción real, reproducirla en un servicio, mantenerla en segundo plano y controlarla desde la notificación. Esta validación reduce el principal riesgo técnico antes de ampliar la interfaz.
