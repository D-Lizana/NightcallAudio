# Interfaz conectada

## Biblioteca y navegación

La biblioteca observa la fuente real de MediaStore mediante `LibraryViewModel`.
El estado de interfaz contiene canciones, artistas, álbumes, géneros y carpetas,
además de carga, error y resultados de búsqueda.

Las secciones disponibles son:

- Canciones, ordenadas por título.
- Artistas, con número de canciones y pantalla de detalle.
- Álbumes, con artista, número de canciones y pantalla de detalle.
- Géneros y carpetas, con su lista filtrada de canciones.

Al seleccionar una canción, la cola se sustituye por el contexto visible y la
reproducción comienza en la posición seleccionada. El minirreproductor y la barra
de navegación se mantienen también en los detalles de artista y álbum.

## Búsqueda y acciones

La búsqueda local filtra por título, artista y álbum. Las listas de biblioteca,
búsqueda y detalles comparten menús contextuales para Â«Reproducir a
continuaciónÂ» y Â«Añadir al final de la colaÂ».

La cola permite seleccionar, eliminar mediante deslizamiento y reordenar mediante
pulsación prolongada y arrastre.

## Reproductor

El reproductor completo observa el estado real de Media3 y muestra carátula,
metadatos, progreso, duración, errores y modos de shuffle/repetición. La barra de
progreso presenta una previsualización mientras se arrastra y envía una sola orden
de búsqueda temporal al finalizar el gesto.

El contenido es desplazable y limita el tamaño de la carátula, por lo que sigue
siendo utilizable en pantallas compactas, orientación horizontal y tabletas. Coil
carga y almacena en caché las carátulas; si una imagen no existe o falla permanece
visible el marcador musical genérico.

## Estado y recreación

La sección y categoría seleccionadas usan estado guardable. El grafo de Navigation
Compose conserva la pila y las pantallas vuelven a observar los `StateFlow` tras
una recreación de la actividad.

## Comprobaciones manuales

1. Recorrer todas las secciones con una biblioteca real.
2. Abrir artistas y álbumes cuyos nombres contengan espacios o caracteres especiales.
3. Reproducir desde biblioteca, búsqueda y detalles y comprobar la cola resultante.
4. Girar el dispositivo en biblioteca, reproductor, detalle y cola.
5. Probar una pantalla compacta y una tableta.
6. Activar Â«No conservar actividadesÂ» y verificar la restauración de navegación y estado.
