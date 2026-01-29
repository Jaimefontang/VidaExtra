package com.entrega3.vidaextra

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz DAO.
 * Define las consultas y acciones que podemos realizar sobre la base de datos SQLite.
 */
@Dao
interface UsuarioDao {

    /**
     * OPERACION READ.
     * Recupera el unico usuario de la aplicacion mediante su identificador fijo.
     * Al retornar un objeto Flow, la aplicacion se queda escuchando cambios:
     * si los datos en el disco cambian, Room emite el nuevo objeto automaticamente.
     */
    @Query("SELECT * FROM usuario_table WHERE id = 1")
    fun getUsuario(): Flow<Usuario?>

    /**
     * OPERACION CREATE / UPDATE.
     * La estrategia REPLACE gestiona los conflictos: si intentamos insertar un usuario
     * con un ID que ya existe, Room sobreescribe los datos antiguos con los nuevos.
     * Se usa 'suspend' para que la operacion se ejecute en un hilo secundario mediante corrutinas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(usuario: Usuario)

    /**
     * OPERACION DELETE.
     * Elimina fisicamente el registro del usuario de la base de datos.
     * Al ejecutarse, el Flow de lectura emitira un valor nulo, provocando
     * que la interfaz de usuario regrese automaticamente al formulario de registro.
     */
    @Delete
    suspend fun borrarUsuario(usuario: Usuario)
}