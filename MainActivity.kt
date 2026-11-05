package at.ernaehrung.begleiter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Meal(val name: String, val kcal: Int, val category: String, val time: String)
private data class Task(val title: String, val done: Boolean)

private enum class AppTheme(val label: String, val bg: Color, val surface: Color, val accent: Color, val text: Color) {
    NOTHING("Nothing", Color(0xFF101010), Color(0xFF202020), Color(0xFFFFFFFF), Color.White),
    FOREST("Wald", Color(0xFF102019), Color(0xFF1D3429), Color(0xFF8BE0A6), Color.White),
    OCEAN("Ozean", Color(0xFF101D30), Color(0xFF1C304A), Color(0xFF77C7FF), Color.White),
    SUNSET("Sunset", Color(0xFF2A1720), Color(0xFF432632), Color(0xFFFFB28A), Color.White),
    LIGHT("Hell", Color(0xFFF3F4F6), Color.White, Color(0xFF30343B), Color(0xFF16181D))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NutritionApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutritionApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nutrition_local", Context.MODE_PRIVATE) }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var themeName by remember { mutableStateOf(prefs.getString("theme", "NOTHING") ?: "NOTHING") }
    val theme = AppTheme.valueOf(themeName)
    var waterGoal by remember { mutableIntStateOf(prefs.getInt("waterGoal", 2000)) }
    var water by remember { mutableIntStateOf(prefs.getInt("water_$today", 0)) }
    var meals by remember { mutableStateOf(loadMeals(prefs, today)) }
    var tasks by remember { mutableStateOf(loadTasks(prefs, today)) }
    var tab by remember { mutableIntStateOf(0) }
    var showMealDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    val totalKcal = meals.sumOf { it.kcal }
    val fg = theme.text

    MaterialTheme(colorScheme = darkColorScheme(
        background = theme.bg, surface = theme.surface, primary = theme.accent,
        onPrimary = if (theme == AppTheme.LIGHT) Color.White else Color.Black,
        onSurface = fg, onBackground = fg
    )) {
        Scaffold(
            containerColor = theme.bg,
            topBar = {
                TopAppBar(
                    title = { Column {
                        Text("ESSENSBEGLEITER", style = MaterialTheme.typography.labelMedium, color = theme.accent)
                        Text(if (tab == 0) "Dein Tag" else if (tab == 1) "Aufgaben" else "Design & Ziele",
                            fontWeight = FontWeight.Bold, color = fg)
                    }},
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.bg)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = theme.surface) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Restaurant, null) }, label = { Text("Heute") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                        icon = { Text("✓") }, label = { Text("Aufgaben") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Text("⚙") }, label = { Text("Design") })
                }
            }
        ) { padding ->
            when (tab) {
                0 -> LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    item {
                        CardBlock(theme) {
                            Text("WASSER", color = theme.accent, style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, null, tint = theme.accent, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("$water ml", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Text("Ziel $waterGoal ml", color = fg.copy(alpha = .7f))
                            }
                            LinearProgressIndicator(progress = { (water.toFloat() / waterGoal).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(), color = theme.accent)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { water = (water + 250).coerceAtMost(10000); prefs.edit().putInt("water_$today", water).apply() }) { Text("+250 ml") }
                                OutlinedButton(onClick = { showWaterDialog = true }) { Text("Andere Menge") }
                                TextButton(onClick = { showGoalDialog = true }) { Text("Ziel") }
                            }
                        }
                    }
                    item {
                        CardBlock(theme) {
                            Text("KALORIEN", color = theme.accent, style = MaterialTheme.typography.labelMedium)
                            Text("$totalKcal kcal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("${meals.size} gespeicherte Mahlzeiten", color = fg.copy(alpha = .7f))
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Mahlzeiten", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Button(onClick = { showMealDialog = true }) { Icon(Icons.Default.Add, null); Text(" Eintragen") }
                        }
                    }
                    if (meals.isEmpty()) item { Text("Noch keine Mahlzeiten eingetragen.", color = fg.copy(alpha = .7f)) }
                    items(meals) { meal ->
                        CardBlock(theme) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(meal.name, fontWeight = FontWeight.SemiBold)
                                    Text("${meal.category} · ${meal.time}", color = fg.copy(alpha = .65f), style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${meal.kcal} kcal", color = theme.accent, fontWeight = FontWeight.Bold)
                                TextButton(onClick = {
                                    meals = meals - meal
                                    saveMeals(prefs, today, meals)
                                }) { Text("✕") }
                            }
                        }
                    }
                }
                1 -> LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text("Kleine Schritte zählen. Hake ab, was du heute geschafft hast.", color = fg.copy(alpha = .75f)) }
                    items(tasks.indices.toList()) { index ->
                        CardBlock(theme) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = tasks[index].done, onCheckedChange = { checked ->
                                    tasks = tasks.toMutableList().also { it[index] = it[index].copy(done = checked) }
                                    saveTasks(prefs, today, tasks)
                                })
                                Text(tasks[index].title, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    item {
                        OutlinedButton(onClick = {
                            tasks = defaultTasks()
                            saveTasks(prefs, today, tasks)
                        }) { Text("Aufgaben zurücksetzen") }
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item { Text("Design auswählen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(AppTheme.entries) { option ->
                        Card(
                            onClick = { themeName = option.name; prefs.edit().putString("theme", option.name).apply() },
                            colors = CardDefaults.cardColors(containerColor = option.surface),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(34.dp).background(option.accent, RoundedCornerShape(10.dp)))
                                Spacer(Modifier.width(14.dp))
                                Text(option.label, color = option.text, modifier = Modifier.weight(1f))
                                if (themeName == option.name) Text("✓", color = option.accent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    item {
                        CardBlock(theme) {
                            Text("Dein Trinkziel", fontWeight = FontWeight.Bold)
                            Text("$waterGoal ml pro Tag")
                            Button(onClick = { showGoalDialog = true }) { Text("Trinkziel ändern") }
                        }
                    }
                    item { Text("Deine Einträge bleiben lokal auf diesem Gerät. Es ist kein Konto und kein Internet nötig.",
                        color = fg.copy(alpha = .7f), style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }

    if (showMealDialog) {
        var name by remember { mutableStateOf("") }
        var kcal by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Mittagessen") }
        AlertDialog(onDismissRequest = { showMealDialog = false },
            title = { Text("Mahlzeit eintragen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Was hast du gegessen?") }, singleLine = true)
                    OutlinedTextField(kcal, { kcal = it.filter(Char::isDigit) }, label = { Text("Kalorien (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(category, {}, readOnly = true, label = { Text("Mahlzeit") },
                            modifier = Modifier.menuAnchor())
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Frühstück", "Mittagessen", "Abendessen", "Snack").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val value = kcal.toIntOrNull()
                if (name.isNotBlank() && value != null) {
                    meals = meals + Meal(name.trim(), value, category,
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                    saveMeals(prefs, today, meals)
                    showMealDialog = false
                }
            }) { Text("Speichern") } },
            dismissButton = { TextButton(onClick = { showMealDialog = false }) { Text("Abbrechen") } })
    }
    if (showWaterDialog) {
        var amount by remember { mutableStateOf("500") }
        AlertDialog(onDismissRequest = { showWaterDialog = false }, title = { Text("Wasser hinzufügen") },
            text = { OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Milliliter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
            confirmButton = { TextButton(onClick = {
                water = (water + (amount.toIntOrNull() ?: 0)).coerceAtMost(10000)
                prefs.edit().putInt("water_$today", water).apply()
                showWaterDialog = false
            }) { Text("Hinzufügen") } },
            dismissButton = { TextButton(onClick = { showWaterDialog = false }) { Text("Abbrechen") } })
    }
    if (showGoalDialog) {
        var goal by remember { mutableStateOf(waterGoal.toString()) }
        AlertDialog(onDismissRequest = { showGoalDialog = false }, title = { Text("Tägliches Trinkziel") },
            text = { OutlinedTextField(goal, { goal = it.filter(Char::isDigit) }, label = { Text("Milliliter pro Tag") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
            confirmButton = { TextButton(onClick = {
                waterGoal = (goal.toIntOrNull() ?: waterGoal).coerceIn(250, 10000)
                prefs.edit().putInt("waterGoal", waterGoal).apply()
                showGoalDialog = false
            }) { Text("Speichern") } },
            dismissButton = { TextButton(onClick = { showGoalDialog = false }) { Text("Abbrechen") } })
    }
}

@Composable
private fun CardBlock(theme: AppTheme, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

private fun defaultTasks() = listOf(
    Task("Nach dem Aufstehen ein Glas Wasser trinken", false),
    Task("Frühstück oder eine erste Mahlzeit einplanen", false),
    Task("Über den Tag verteilt Wasser trinken", false),
    Task("Eine ausgewogene Hauptmahlzeit essen", false),
    Task("Abends kurz den Tag überprüfen", false)
)

private fun loadMeals(prefs: android.content.SharedPreferences, day: String): List<Meal> {
    return try {
        val arr = JSONArray(prefs.getString("meals_$day", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Meal(o.getString("name"), o.getInt("kcal"), o.getString("category"), o.getString("time"))
        }
    } catch (_: Exception) { emptyList() }
}
private fun saveMeals(prefs: android.content.SharedPreferences, day: String, meals: List<Meal>) {
    val arr = JSONArray()
    meals.forEach { arr.put(JSONObject().put("name", it.name).put("kcal", it.kcal)
        .put("category", it.category).put("time", it.time)) }
    prefs.edit().putString("meals_$day", arr.toString()).apply()
}
private fun loadTasks(prefs: android.content.SharedPreferences, day: String): List<Task> {
    return try {
        val arr = JSONArray(prefs.getString("tasks_$day", ""))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i); Task(o.getString("title"), o.getBoolean("done"))
        }
    } catch (_: Exception) { defaultTasks().also { saveTasks(prefs, day, it) } }
}
private fun saveTasks(prefs: android.content.SharedPreferences, day: String, tasks: List<Task>) {
    val arr = JSONArray()
    tasks.forEach { arr.put(JSONObject().put("title", it.title).put("done", it.done)) }
    prefs.edit().putString("tasks_$day", arr.toString()).apply()
}
