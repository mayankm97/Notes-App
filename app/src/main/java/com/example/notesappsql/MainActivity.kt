package com.example.notesappsql

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesappsql.ui.theme.DBHelper
import com.example.notesappsql.ui.theme.NotesAppSqlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

fun loadNotesFromDatabase(db: DBHelper): List<Items> {
    val notes = mutableListOf<Items>()
    val cursor = db.getData()
    if (cursor != null) {
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle"))
            notes.add(Items(id, noteTitle = title, noteSubtitle = subtitle))
        }
        cursor.close()
    }
    return notes
}

//@Preview(showSystemUi = true)
@Composable
fun MyApp() {
    NotesAppSqlTheme {
        // state variables


        var showDialog by remember { mutableStateOf(false) }

        var title by remember { mutableStateOf("") }
        var subtitle by remember { mutableStateOf("") }
        var itemList by remember { mutableStateOf(listOf<Items>()) }
        val context = LocalContext.current
        val db= DBHelper(LocalContext.current)
        
        LaunchedEffect(Unit) {
            val notes = loadNotesFromDatabase(db)
            itemList = notes
        }

        if (itemList.isEmpty()) {
            empty()
        }

        val nextId = remember(itemList) {
            itemList.maxOfOrNull { it.id }?.plus(1) ?: 1
        }
        var deletedIDs by remember { mutableStateOf(mutableListOf<Int>())}

        Scaffold(containerColor = Color.Unspecified,
            floatingActionButton = {
                FloatingActionButton(onClick = {
//                    str1 = "Note title"
//                    str2 = "Note subtitle"
                    //   itemList = itemList + Items(str1, str2)
                    // itemList.add(...) mutates the existing list without notifying Compose of
                    // the change, so the UI does not update
                    title = ""
                    subtitle = ""
                    showDialog = true
                }) {
                    Text(text = "+")
                }
            },

            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            val customPaddingTop = 18.dp
            val customPaddingBottom = 16.dp
            val customPaddingStart = 8.dp
            val customPaddingEnd = 8.dp

            val combinedPadding = Modifier.padding(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + customPaddingStart,
                top = paddingValues.calculateTopPadding() + customPaddingTop,
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + customPaddingEnd,
                bottom = paddingValues.calculateBottomPadding() + customPaddingBottom
            )

            LazyColumn(
                modifier = combinedPadding//.verticalScroll(rememberScrollState()),
            ) {
                items(itemList) { item ->
                    boxh(item,
                        db,
                        noteTitle = item.noteTitle,
                        noteSubtitle = item.noteSubtitle,
                        onBoxClick = {
                            val intent = Intent(context, ContentActivity::class.java)
                            intent.putExtra("NOTE_ID", item.id)
                            context.startActivity(intent)
                        },
                        onDelete = {
                            itemList = itemList.filter {
                                it.id != item.id
                            }
                            deletedIDs.add(item.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
            /*Column(
                modifier = combinedPadding.verticalScroll(rememberScrollState()),
            ) {
                itemList.forEach { item ->
                    boxh(item,
                        db,
                        id = item.id,
                        noteTitle = item.noteTitle,
                        noteSubtitle = item.noteSubtitle,
                        onBoxClick = {
                            val intent = Intent(context, ContentActivity::class.java)
                            intent.putExtra("NOTE_ID", item.id)
                            context.startActivity(intent)
                        },
                        onDelete = {
                            itemList = itemList.filter {
                                it.id != item.id
                            }
                            deletedIDs.add(item.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            } */
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Enter details") },
                    text = {
                        Column {
                            TextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text(text = "Enter title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column {
                                TextField(
                                    value = subtitle,
                                    onValueChange = { subtitle = it },
                                    placeholder = { Text(text = "Enter subtitle") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (title.isNotEmpty()) {
                                val newId = if (deletedIDs.isNotEmpty()) {
                                    deletedIDs.removeAt(0)
                                } else nextId
                                itemList =
                                    itemList + Items(id = newId, noteTitle = title, noteSubtitle = subtitle)
                                showDialog = false
                                val intent = Intent(context, NotesActivity::class.java)
                                intent.putExtra("id", newId)
                                intent.putExtra("title", title)
                                intent.putExtra("subtitle", subtitle)
                                intent.putExtra("FROM_ACTIVITY", "MainActivity")
                                intent.putExtra("content", "")
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Enter title and subtitle", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(text = "OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false }) {
                            Text(text = "Cancel")
                        }
                    },
                )
            }
        }
    }
}

data class Items(val id: Int, val noteTitle: String, val noteSubtitle: String)


@Composable
fun boxh(
    item: Items,
    db: DBHelper,
    noteTitle: String,
    noteSubtitle: String,
    onBoxClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(modifier = Modifier.height(100.dp)
        .padding(4.dp, 2.dp)
        //.background(Color.Red, shape = RectangleShape)
        .clickable(onClick = onBoxClick),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ThreeDotsMenu(modifier = Modifier.align(Alignment.TopEnd)) {
                when (it) {
                    "Share" -> {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Title: $noteTitle\nSubtitle: $noteSubtitle\nContent: ${item.id}")
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share note")
                        )
                    }

                    "Delete" -> {
                        val checkDeletedData = db.deleteNote(item.id)
                        if (checkDeletedData) {
                            onDelete()
                            Toast.makeText(context, "$noteTitle note deleted.", Toast.LENGTH_SHORT)
                                .show()
                        } else
                            Toast.makeText(context, "Note can't be deleted.", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
            Column(modifier = Modifier.padding(18.dp, 15.dp)) {
//                Text(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(28.dp),
//                    text = id.toString(),
//                    fontSize = 22.sp,
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold
//                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    text = noteTitle,
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 7.dp)
                        .height(17.dp),
                    text = noteSubtitle,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
    //HorizontalDivider(thickness = 2.dp, color = Color.White)
}

@Preview (showSystemUi = true)
@Composable
fun empty() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Image(modifier = Modifier.align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.notes),
                contentDescription = "empty box"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No items to display.\nClick on '+' button to add notes..",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,

            )
        }
    }
}