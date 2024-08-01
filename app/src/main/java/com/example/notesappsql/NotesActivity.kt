package com.example.notesappsql

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notesappsql.ui.theme.DBHelper

class NotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DBHelper(this) // Initialize DBHelper
      //  enableEdgeToEdge()
        setContent {
            val data = intent.getStringExtra("title")
            val data1 = intent.getStringExtra("subtitle")
            val id = intent.getIntExtra("id", -1)
            val content = intent.getStringExtra("content")
            val fromActivity = intent.getStringExtra("FROM_ACTIVITY")

            data?.let {
                data1?.let { it1 ->
                    content?.let { it2 ->
                        notes(it, it1, it2, fromActivity, db, id)
                    }
                }
            }
        }
    }
}

@Composable
fun notes(heading: String, subheading: String, content: String, fromActivity: String?, db: DBHelper, id: Int) {
    val context = LocalContext.current
    var cont by remember { mutableStateOf(content) }
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
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
//Column {      // this solved the mess caused by rememberVerticalScrollState() when not done within column
        // it takes up whole space.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    modifier = Modifier
                        .width(25.dp)
                        .height(25.dp)
                        .padding(0.dp, 5.dp, 0.dp, 0.dp),
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? Activity)?.finish()
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(modifier = Modifier.width(140.dp))
                if (fromActivity == "MainActivity") {
                    Button(
                        onClick = {
                            val checkInsertedData = db.insertNote(id, heading, subheading, cont)
                            if (checkInsertedData)
                                Toast.makeText(context, "New note added.", Toast.LENGTH_SHORT)
                                    .show()
                            else
                                Toast.makeText(context, "New note not added.", Toast.LENGTH_SHORT)
                                    .show()
                        },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "SAVE",
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp
                        )
                    }
                } else {        // UPDATE EXISTING DATA
                    val contTxt = cont
                    Button(
                        onClick = {
                            val updateInsertedData = db.updateNote(id, contTxt)
                            if (updateInsertedData)
                                Toast.makeText(context, "Note updated.", Toast.LENGTH_SHORT)
                                    .show()
                            else
                                Toast.makeText(context, "Note not updated.", Toast.LENGTH_SHORT)
                                    .show()

                        },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "UPDATE",
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = combinedPadding
     //           .verticalScroll(rememberScrollState())
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            placingTF(heading, content, onContentChange = { cont = it },
                onTextClick = {
                    val res = db.getData()
                    if (res == null) {
                        Toast.makeText(context, "Table was dropped", Toast.LENGTH_SHORT).show()
                        return@placingTF
                    }
                    if (res.count == 0) {
                        Toast.makeText(context, "No Entry exists", Toast.LENGTH_SHORT).show()
                        return@placingTF
                    }

                    val buffer = StringBuilder()
                    while (res.moveToNext()) {
                        buffer.append("Id: " + res.getString(0) + "\n")
                        buffer.append("Title: " + res.getString(1) + "\n")
                        buffer.append("Subtitle: " + res.getString(2) + "\n")
                        buffer.append("Note content: " + res.getString(3) + "\n\n")
                    }
                    AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setTitle("User entries")
                        .setMessage(buffer.toString())
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()

                }

//                        placingTF(heading, content, onContentChange = { cont = it }) {
//                Toast.makeText(context, "tkjslf", Toast.LENGTH_SHORT).show()
//            } In Kotlin, if the last parameter of a function is a lambda, you can pass it
            //            outside the parentheses. This is known as a trailing lambda.
            )
        }
    }
}

@Composable
fun placingTF(heading: String, content: String, onContentChange: (String) -> Unit, onTextClick: () -> Unit) {
    var state by remember { mutableStateOf(content) }

    Column(modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(76.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = heading,
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
            //fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(38.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(200.dp, 400.dp)
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            value = state,
            onValueChange = {
                state = it
                onContentChange(it)
            }
//        maxLines = Int.MAX_VALUE,
//        singleLine = false keyboardOptions = KeyboardOptions.Default.copy(
//                imeAction = ImeAction.Default // Changed from Done to Default for multiline
//                ),        these lines are useless just heightIn works fine without these.


        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(text = "View all data",
            modifier = Modifier.clickable { onTextClick() }
                .align(Alignment.End).padding(end = 10.dp),
            fontSize = 11.sp,
            textDecoration = TextDecoration.Underline,
        )
    }
}


@Composable
fun ThreeDotsMenu(modifier: Modifier = Modifier, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(text = { Text("Share", color = Color.Black) },
                onClick = {
                    onOptionSelected("Share")
                    expanded = false
                })
            DropdownMenuItem(text = { Text("Delete", color = Color.Black) }, onClick = {
                onOptionSelected("Delete")
                expanded = false
            })
        }
    }
}
