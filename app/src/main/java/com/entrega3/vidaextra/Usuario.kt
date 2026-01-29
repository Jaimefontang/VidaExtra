package com.entrega3.vidaextra

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representacion de la tabla de la base de datos en codigo Kotlin.
 * Cada instancia de esta clase equivale a una fila en SQLite.
 */
@Entity(tableName = "usuario_table")
data class Usuario(
    /**
     * Clave primaria obligatoria para identificar el registro.
     * Se establece un valor fijo de 1 para forzar la existencia de un perfil unico.
     */
    @PrimaryKey val id: Int = 1,

    // Datos de configuracion personal introducidos en el registro
    val nombre: String,
    val edad: Int,
    val precioPaquete: Double,
    val cantidadPorPaquete: Int,

    // Contadores de actividad que cambian al interactuar con la interfaz
    val contadorCigarros: Int = 0,
    val contadorEvitados: Int = 0,

    /**
     * Define el modo de visualizacion de la aplicacion.
     * Almacena valores como FUMAR o DEJAR para determinar que dashboard cargar.
     */
    val objetivo: String
)