# Biblioteca mediante MediaStore

Estado: Fase 6 completada.

## Fuente de datos

MediaStore es la fuente de verdad de los archivos musicales. La aplicación no explora directamente el sistema de archivos ni almacena copias de las canciones.

## Metadatos obtenidos

- Identificador y URI de contenido.
- Título.
- Artista e identificador de artista.
- Álbum e identificador de álbum.
- URI de carátula del álbum.
- Duración.
- Número de pista y disco.
- Género, cuando MediaStore lo proporciona.
- Carpeta.
- Año.
- Fecha de incorporación.

Los valores vacíos de título, artista y álbum se sustituyen por textos comprensibles en español.

## Compatibilidad de carpetas

- Desde Android 10 se interpreta `RELATIVE_PATH` como una carpeta.
- En Android 8 y 9 se obtiene la carpeta padre a partir de `DATA`.

Esta diferencia evita presentar el nombre del archivo como si fuese una carpeta en dispositivos antiguos.

## Exclusiones

Solo se consultan elementos marcados por MediaStore como música y con una duración mínima de 30 segundos. Además se excluyen segmentos de carpeta conocidos para:

- Alarmas.
- Notificaciones.
- Tonos de llamada.
- Grabaciones de voz o llamadas.

Se contemplan nombres habituales en español e inglés sin distinguir mayúsculas.

## Actualización

Un `ContentObserver` vigila cambios en la colección de audio. Cuando Android añade, mueve o elimina una canción, la biblioteca vuelve a consultarse y la UI recibe el nuevo estado mediante Flow.

También existe una actualización manual para recuperación de errores. Las consultas se realizan en `Dispatchers.IO` y los resultados idénticos no se vuelven a emitir.

## Organización

La biblioteca permite:

- Orden alfabético de canciones.
- Agrupación por artista, álbum, género y carpeta.
- Apertura de cada agrupación para ver sus canciones.
- Orden de álbum por disco, número de pista y título.
- Búsqueda independiente por título, artista y álbum.

La búsqueda ya no modifica accidentalmente el contenido general de la biblioteca.

## Carátulas

MediaStore proporciona una URI de carátula a partir del álbum. Coil carga y limita las imágenes de forma asíncrona. Cuando la URI no existe o falla se conserva el marcador musical genérico.

Se utiliza Coil 3.4 porque es compatible con Kotlin 2.2. Coil 3.5 requiere Kotlin 2.4 y no se incorporó para evitar una migración de herramientas ajena a esta fase.

## Géneros

Los géneros se relacionan con canciones mediante la colección `MediaStore.Audio.Genres.Members`. Algunos archivos o fabricantes no publican esta información; en esos casos la canción no aparece en una categoría de género, pero continúa disponible en el resto de la biblioteca.

## Pruebas

- Duración mínima exacta.
- Exclusión de carpetas en español e inglés.
- Coincidencias parciales que no deben excluirse.
- Extracción de carpetas moderna y antigua.
- Agrupación y búsqueda.
- Procesamiento de una biblioteca artificial de 750 canciones.
- Compilación de pruebas de interfaz.
- Lint y empaquetado del APK debug.
