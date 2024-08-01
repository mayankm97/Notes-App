package com.example.notesappsql

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notesappsql.ui.theme.DBHelper
import com.example.notesappsql.ui.theme.NotesAppSqlTheme

class ContentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DBHelper(this)
        val noteID = intent.getIntExtra("NOTE_ID", -1)

        if (noteID != -1) {
            val cursor = db.getDataByCustomId(noteID)
            // If the queried custom_id exists, moveToFirst() returns true, pointing to that row.
            // If it doesn't exist, the cursor is empty, and moveToFirst() returns false.
            // Always check the cursor's state before accessing data.

            setContent {
                cursor?.let {
                    if (it.moveToFirst()) {
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val subtitle = it.getString(it.getColumnIndexOrThrow("subtitle"))
                        val content = it.getString(it.getColumnIndexOrThrow("content"))

                        showContent(noteID, title, subtitle, content)
                    } else {
                        showContent(-2,"No Title", "No Subtitle", "No Content Available")
                    }
                } ?: run {
                    showContent(-2,"Error", "Error", "Unable to load content")
                }
            }
        } else {
            setContent {
                showContent(-2,"Error", "Error", "Invalid Note ID")
            }
        }

    }
}

@Composable
fun showContent(id: Int, title: String, subtitle: String, content: String) {
    val context = LocalContext.current
    NotesAppSqlTheme {
        Scaffold(modifier = Modifier
            .fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            //    verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Spacer(modifier = Modifier.height(50.dp))
                Text(text = "Id: " + id.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Title: " + title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(30.dp))

                Text(text = "Subtitle: " + subtitle)
                Spacer(modifier = Modifier.height(40.dp))

                Text(text = "Content:" + content)
                Spacer(modifier = Modifier.height(80.dp))

                Button(onClick = {
                    val intent = Intent(context, NotesActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("title", title)
                    intent.putExtra("subtitle", subtitle)
                    intent.putExtra("content", content)
                    intent.putExtra("FROM_ACTIVITY", "ContentActivity")
                    context.startActivity(intent)
                }) {
                    Text(text = "Edit this note")
                }
            }
        }
    }
}
