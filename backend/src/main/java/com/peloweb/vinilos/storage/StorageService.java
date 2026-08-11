package com.peloweb.vinilos.storage;

/**
 * Almacenamiento de archivos (fotos de vinilos). Implementacion sobre object storage
 * S3-compatible (MinIO en dev, S3/R2/etc. en prod). El resto del sistema solo conoce
 * esta interfaz, asi que cambiar de proveedor no toca la logica de negocio.
 */
public interface StorageService {

    /** Sube el contenido con la key dada y devuelve la URL publica del objeto. */
    String upload(byte[] content, String key, String contentType);

    /** Borra el objeto de la key dada (idempotente). */
    void delete(String key);

    /** URL publica para una key ya existente. */
    String urlFor(String key);

    /** Deriva la key a partir de una URL publica generada por este storage. */
    String keyFromUrl(String url);
}
