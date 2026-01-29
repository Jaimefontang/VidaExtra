package com.entrega3.vidaextra

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Clase principal de la base de datos que sirve como punto de acceso al almacenamiento.
 * Se define como abstracta porque Room se encarga de implementar el codigo real.
 */
@Database(entities = [Usuario::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Define la conexion con la interfaz de operaciones.
     * Permite que el resto de la aplicacion use las funciones del DAO.
     */
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        /**
         * La anotacion Volatile asegura que el valor de la instancia este siempre actualizado
         * para todos los hilos de ejecucion, evitando errores de lectura y escritura.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Implementacion del Patron Singleton.
         * Su funcion es garantizar que solo exista una instancia de la base de datos abierta.
         * Abrir varias conexiones simultaneas podria corromper el archivo fisico o agotar recursos.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, se devuelve; si no, se crea de forma sincronizada
            return INSTANCE ?: synchronized(this) {
                // Configuracion del constructor de Room
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vida_extra_db" // Nombre del archivo que se guardara en la memoria del movil
                ).build()

                // Asignacion de la instancia creada a la variable global
                INSTANCE = instance
                instance
            }
        }
    }
}