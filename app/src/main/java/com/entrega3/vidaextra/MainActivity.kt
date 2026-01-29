package com.entrega3.vidaextra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrega3.vidaextra.ui.theme.VidaExtraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Punto de entrada de la aplicacion que establece el diseño principal
        setContent {
            VidaExtraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaPrincipal()
                }
            }
        }
    }
}

@Composable
fun PantallaPrincipal() {
    // Contexto necesario para inicializar la base de datos en esta pantalla
    val context = LocalContext.current

    // El scope permite lanzar procesos en segundo plano para no bloquear la interfaz
    val scope = rememberCoroutineScope()

    // Llamada al metodo estatico para obtener la base de datos y su interfaz de acceso
    val db = AppDatabase.getDatabase(context)
    val dao = db.usuarioDao()

    //Observa la base de datos constantemente
    // Si los datos cambian en el disco, esta variable actualiza la pantalla sola
    val usuarioState by dao.getUsuario().collectAsState(initial = null)

    // Lógica de enrutamiento basada en la existencia del registro
    if (usuarioState == null) {
        // Si la base de datos devuelve nulo, cargamos el formulario inicial
        FormularioRegistro { nombre, precio, cantidad, edad, objetivoElegido ->
            // REQUISITO DE INSERCION: Se ejecuta fuera del hilo principal
            scope.launch {
                dao.insertarOActualizar(
                    Usuario(
                        nombre = nombre,
                        precioPaquete = precio,
                        cantidadPorPaquete = cantidad,
                        edad = edad,
                        objetivo = objetivoElegido
                    )
                )
            }
        }
    } else {
        // Si el usuario existe, extraemos el objeto para trabajar con sus datos
        val user = usuarioState!!

        // LOGICA DE NEGOCIO: Calculos realizados al vuelo para evitar datos duplicados en BD
        val precioUnitario =
            if (user.cantidadPorPaquete > 0) user.precioPaquete / user.cantidadPorPaquete else 0.0

        // Algoritmo de salud que escala el daño en funcion de la edad del perfil
        val minutosPorCigarro = 11 + (user.edad * 0.5)

        // Determina que interfaz mostrar segun el campo objetivo de la base de datos
        when (user.objetivo) {
            "FUMAR" -> PantallaFumador(user, precioUnitario, minutosPorCigarro, dao, scope)
            "DEJAR" -> PantallaExFumador(user, precioUnitario, minutosPorCigarro, dao, scope)
            else -> PantallaFumador(user, precioUnitario, minutosPorCigarro, dao, scope)
        }
    }
}

@Composable
fun PantallaFumador(
    user: Usuario,
    precioUnitario: Double,
    minutosPorCigarro: Double,
    dao: UsuarioDao,
    scope: kotlinx.coroutines.CoroutineScope
) {
    // Calculo de variables financieras y temporales basadas en el contador de la BD
    val dineroPerdido = user.contadorCigarros * precioUnitario
    val vidaPerdidaMin = user.contadorCigarros * minutosPorCigarro

    // Lógica para transformar unidades sueltas en paquetes completos para el usuario
    val paquetesCompletos =
        if (user.cantidadPorPaquete > 0) user.contadorCigarros / user.cantidadPorPaquete else 0
    val cigarrosSueltos =
        if (user.cantidadPorPaquete > 0) user.contadorCigarros % user.cantidadPorPaquete else 0

    // Estimacion de la carga quimica acumulada segun el numero de cigarrillos
    val alquitranMg = user.contadorCigarros * 10.0
    val coMg = user.contadorCigarros * 10.0
    val nicotinaMg = user.contadorCigarros * 1.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "REGISTRO DE CONSUMO",
            color = Color(0xFFD32F2F),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Muestra datos del perfil recuperados directamente de la tabla
        Text("Nombre: ${user.nombre}", fontSize = 14.sp, color = Color.Gray)
        Text("Edad: ${user.edad} años", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        // El numero grande central que representa el estado del contador en la BD
        Text(
            "${user.contadorCigarros}",
            fontSize = 90.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text("Cigarros consumidos", fontSize = 16.sp)

        Text(
            text = "($paquetesCompletos Paquetes y $cigarrosSueltos cigarros)",
            fontSize = 18.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Contenedor para el analisis de perdidas financieras y de salud
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("PÉRDIDAS", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dinero quemado:")
                    Text("${"%.2f".format(dineroPerdido)} €", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vida perdida:")
                    Text(formatearTiempo(vidaPerdidaMin), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEF9A9A))

                // Lógica de utilidad que traduce euros en objetos cotidianos
                Text(
                    text = "HAS GASTADO LO EQUIVALENTE A:\n${obtenerFraseComparativa(dineroPerdido)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Contenedor para la visualizacion de datos toxicologicos acumulados
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("TÓXICOS EN TU CUERPO", color = Color(0xFFFFCC80), fontWeight = FontWeight.Bold)
                Text("Acumulado en pulmones y sangre", fontSize = 12.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(15.dp))

                // Componentes personalizados para cada fila de la tabla de toxicos
                FilaToxico("Alquitrán (Pulmones)", alquitranMg)
                FilaToxico("Monóxido Carbono (Sangre)", coMg)
                FilaToxico("Nicotina (Cerebro)", nicotinaMg)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REQUISITO DE ACTUALIZACION: Botones que modifican el contador en la BD
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    // Se lanza una corrutina para realizar la escritura en disco de forma asincrona
                    scope.launch {
                        // .copy crea una nueva instancia del usuario con el contador incrementado
                        dao.insertarOActualizar(user.copy(contadorCigarros = user.contadorCigarros + 1))
                    }
                },
                modifier = Modifier.weight(1f).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(35.dp)
            ) {
                Text("+1 CIGARRO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    scope.launch {
                        dao.insertarOActualizar(user.copy(contadorCigarros = user.contadorCigarros + user.cantidadPorPaquete))
                    }
                },
                modifier = Modifier.width(170.dp).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800000)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(35.dp)
            ) {
                Text("+1 PAQUETE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // REQUISITO DE BORRADO: Elimina el registro actual para reiniciar el flujo
        TextButton(onClick = { scope.launch { dao.borrarUsuario(user) } }) {
            Text("Reiniciar / Cambiar objetivo", color = Color.Gray)
        }
    }
}

// La logica de PantallaExFumador es identica pero con enfoque positivo (ahorro y salud ganada)
@Composable
fun PantallaExFumador(
    user: Usuario,
    precioUnitario: Double,
    minutosPorCigarro: Double,
    dao: UsuarioDao,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val dineroAhorrado = user.contadorEvitados * precioUnitario
    val vidaGanadaMin = user.contadorEvitados * minutosPorCigarro

    val paquetesEvitados =
        if (user.cantidadPorPaquete > 0) user.contadorEvitados / user.cantidadPorPaquete else 0
    val cigarrosSueltosEvitados =
        if (user.cantidadPorPaquete > 0) user.contadorEvitados % user.cantidadPorPaquete else 0

    val alquitranMg = user.contadorEvitados * 10.0
    val coMg = user.contadorEvitados * 10.0
    val nicotinaMg = user.contadorEvitados * 1.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text("REGISTRO DE PROGRESO", color = Color(0xFF4CAF50), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Nombre: ${user.nombre}", fontSize = 14.sp, color = Color.Gray)
        Text("Edad: ${user.edad} años", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))
        Text(
            "${user.contadorEvitados}",
            fontSize = 90.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text("Tentaciones superadas", fontSize = 16.sp)

        Text(
            text = "($paquetesEvitados Paquetes no comprados)",
            fontSize = 18.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("BENEFICIOS", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dinero ahorrado:")
                    Text("${"%.2f".format(dineroAhorrado)} €", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vida salvada:")
                    Text(formatearTiempo(vidaGanadaMin), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFA5D6A7))

                Text(
                    text = "HAS AHORRADO LO EQUIVALENTE A:\n${obtenerFraseComparativa(dineroAhorrado)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("TÓXICOS EVITADOS", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("Sustancias no ingresadas al organismo", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(15.dp))

                FilaToxicoPositiva("Alquitrán (Evitado)", alquitranMg)
                FilaToxicoPositiva("Monóxido Carbono (Evitado)", coMg)
                FilaToxicoPositiva("Nicotina (Evitada)", nicotinaMg)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { scope.launch { dao.insertarOActualizar(user.copy(contadorEvitados = user.contadorEvitados + 1)) } },
                modifier = Modifier.weight(1f).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(35.dp)
            ) {
                Text("-1 CIGARRO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { scope.launch { dao.insertarOActualizar(user.copy(contadorEvitados = user.contadorEvitados + user.cantidadPorPaquete)) } },
                modifier = Modifier.width(170.dp).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(35.dp)
            ) {
                Text("-1 PAQUETE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        TextButton(onClick = { scope.launch { dao.borrarUsuario(user) } }) {
            Text("Reiniciar / Cambiar objetivo", color = Color.Gray)
        }
    }
}

@Composable
fun FormularioRegistro(onFinalizado: (String, Double, Int, Int, String) -> Unit) {
    // Definicion de estados locales para capturar la entrada de texto del formulario
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("20") }
    var edad by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_app),
            contentDescription = "Logo VidaExtra",
            modifier = Modifier.size(150.dp).padding(bottom = 20.dp)
        )

        Text("Bienvenido a VidaExtra", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("El paso más importante ya lo has dado.", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))
        Text("Introduce tus datos", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(5.dp))

        // Entradas de texto configuradas para tipos especificos de teclado
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it },
            label = { Text("Edad (Años)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio por paquete en euros") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text("¿Cuál es tu objetivo?", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Al finalizar, el formulario devuelve los datos al llamador para la insercion en BD
        Button(
            onClick = {
                val p = precio.toDoubleOrNull() ?: 0.0
                val c = cantidad.toIntOrNull() ?: 20
                val e = edad.toIntOrNull() ?: 18
                if (nombre.isNotEmpty()) onFinalizado(nombre, p, c, e, "FUMAR")
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Controlar lo que fumo", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                val p = precio.toDoubleOrNull() ?: 0.0
                val c = cantidad.toIntOrNull() ?: 20
                val e = edad.toIntOrNull() ?: 18
                if (nombre.isNotEmpty()) onFinalizado(nombre, p, c, e, "DEJAR")
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Dejar de fumar", fontSize = 18.sp)
        }
    }
}

// FUNCIONES DE UTILIDAD: Transforman datos crudos en formatos legibles para el usuario

fun formatearTiempo(minutos: Double): String {
    // Escala la unidad de tiempo segun el valor
    return when {
        minutos < 60 -> "%.0f min".format(minutos)
        minutos < 1440 -> "%.1f h".format(minutos / 60)
        else -> "%.1f días".format(minutos / 1440)
    }
}

fun obtenerFraseComparativa(euros: Double): String {
    // Devuelve un ejemplo de coste de oportunidad para dar significado al ahorro/gasto
    return when {
        euros <= 0 -> " "
        euros < 0.5 -> "Un chicle"
        euros < 1 -> "Una barra de pan"
        euros < 3 -> "Un cafe o botella de agua"
        euros < 4 -> "1kg de plátanos de Canarias"
        euros < 10 -> "Un libro de bolsillo"
        euros < 20 -> "Una botella de Aceite de Oliva Virgen Extra"
        euros < 35 -> "Una suscripción mensual al Gimnasio"
        euros < 50 -> "Una sesión de Fisioterapia"
        euros < 80 -> "Unas buenas zapatillas de Running"
        euros < 120 -> "Una limpieza dental completa"
        euros < 200 -> "Un reloj inteligente"
        euros < 300 -> "Un curso de especialización profesional"
        euros < 500 -> "Un fin de semana de Spa y relax"
        euros < 1000 -> "Aportación a un Fondo de Inversión"
        else -> "La entrada para un coche eléctrico o reforma"
    }
}

// COMPONENTES DE INTERFAZ

@Composable
fun FilaToxico(nombre: String, cantidadMg: Double) {
    // Gestiona el cambio de unidad automatica de miligramos a gramos
    val textoValor = if (cantidadMg >= 1000) "%.2f g".format(cantidadMg / 1000) else "%.0f mg".format(cantidadMg)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(nombre, color = Color.White, fontSize = 14.sp)
        Text(textoValor, color = Color(0xFFFFCC80), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
}

@Composable
fun FilaToxicoPositiva(nombre: String, cantidadMg: Double) {
    val textoValor = if (cantidadMg >= 1000) "%.2f g".format(cantidadMg / 1000) else "%.0f mg".format(cantidadMg)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(nombre, color = Color.DarkGray, fontSize = 14.sp)
        Text(textoValor, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}