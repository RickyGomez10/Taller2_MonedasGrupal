package com.example.coincollector

import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.coincollector.models.Coin
import com.example.coincollector.utilities.NetworkUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var dbHelper = Database(this)


    private var twoPane: Boolean = false
    private lateinit var dataRes: MutableList<Coin>

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true

        }



        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var search: TextView = findViewById(R.id.tv_searching)
        when (item.itemId) {
            R.id.type_sv -> {
                search.text = "Buscando monedas de El Salvador"
                readCoins("El_Salvador")
            }
            R.id.type_hn -> {
                search.text = "Buscando monedas de Honduras"
                readCoins("Honduras")
            }
            R.id.type_nc -> {
                search.text = "Buscando monedas de Nicaragua"
                readCoins("Nicaragua")
            }
            R.id.type_jp -> {
                search.text = "Buscando monedas de Japón"
                readCoins("Japón")
            }
            R.id.type_sp -> {
                search.text = "Buscando monedas de España"
                readCoins("España")
            }
            R.id.type_rs -> {
                search.text = "Buscando monedas de Rusia"
                readCoins("Rusia")

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, dataRes, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: MainActivity,
        private val values: List<Coin>,
        private val twoPane: Boolean
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Coin
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("ID", item.id)
                            putString("Name", item.name)
                            putString("Country", item.country)
                            putString("jsonInfo", item.jsonInfo)
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra("ID", item.id)
                        putExtra("Name", item.name)
                        putExtra("Country", item.country)
                        putExtra("jsonInfo", item.jsonInfo)
                    }
                    v.context.startActivity(intent)
                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            val data = JSONObject(item.jsonInfo)

            holder.imagen?.let {
                Glide.with(holder.imagen.context).load(data.getString("img")).into(
                    it
                )
            }

            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imagen: ImageView = view.imagen
            val contentView: TextView = view.content
        }
    }

    private fun readCoins( id: String) {

        val db = dbHelper.readableDatabase

        val projection = arrayOf(

            DatabaseContract.PersonaEntry.COLUMN_ID,
            DatabaseContract.PersonaEntry.COLUMN_COUNTRY,
            DatabaseContract.PersonaEntry.COLUMN_BASENAME,
            DatabaseContract.PersonaEntry.COLUMN_RESULTADOS

        )

        val sortOrder = "${DatabaseContract.PersonaEntry.COLUMN_BASENAME} DESC"

        val cursor = db.query(
            DatabaseContract.PersonaEntry.TABLE_NAME, // nombre de la tabla
            projection, // columnas que se devolverán
            "country =? ", // Columns where clausule
            arrayOf(id), // values Where clausule
            null, // Do not group rows
            null, // do not filter by row
            sortOrder // sort order
        )

        var lista = mutableListOf<Coin>()


        with(cursor) {

            while (moveToNext()) {
                var persona = Coin(
                    getString(getColumnIndexOrThrow(DatabaseContract.PersonaEntry.COLUMN_ID)),
                    getString(getColumnIndexOrThrow(DatabaseContract.PersonaEntry.COLUMN_COUNTRY)),
                    getString(getColumnIndexOrThrow(DatabaseContract.PersonaEntry.COLUMN_BASENAME)),
                    getString(getColumnIndexOrThrow(DatabaseContract.PersonaEntry.COLUMN_RESULTADOS))

                )

                lista.add(persona)
            }
        }
        if(lista.size == 0 ){
            FetchDataTask().execute(id)
            Log.i("Yayinfo?", "No")


        }else{
            val monedas = JSONObject(lista[0].jsonInfo).getJSONArray("coins");
            dataRes = MutableList(monedas.length()) { i ->
                Coin(monedas.getJSONObject(i).getString("codename"),monedas.getJSONObject(i).getString("name").capitalize(),lista[0].country,monedas.get(i).toString())
            }

            setupRecyclerView(item_list)
            Log.i("Yayinfo?", "Si")
        }

    }

    private inner class FetchDataTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg dataNumbers: String): String? {

            if (dataNumbers.isEmpty()) {
                return null
            }

            val ID = dataNumbers[0]

            val customAPI = NetworkUtils.buildUrl(ID)

            try {
                return NetworkUtils.getResponseFromHttpUrl(customAPI!!)
            } catch (e: IOException) {
                e.printStackTrace()
                return ""
            }

        }

        //base de datos



        override fun onPostExecute(dataInfo: String?) {
            if (dataInfo != null && dataInfo != "") {

                val resultados = JSONObject(dataInfo)
                val monedas = resultados.getJSONArray("coins")

                //Base de datos

                val country = resultados.getString("country")
                val idPais = resultados.getString("id")
                val basename = resultados.getString("basename")
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(DatabaseContract.PersonaEntry.COLUMN_ID, idPais)
                    put(DatabaseContract.PersonaEntry.COLUMN_COUNTRY, country)
                    put(DatabaseContract.PersonaEntry.COLUMN_BASENAME, basename)
                    put(DatabaseContract.PersonaEntry.COLUMN_RESULTADOS, dataInfo)

                }

                val newRowId = db?.insert(DatabaseContract.PersonaEntry.TABLE_NAME, null, values)
                Log.i("Nombre:",country)
                Log.i("id:", idPais)
                if (newRowId == -1L) {
                    Log.i("Se pudo?", "No")
                } else {

                    Log.i("Se pudo?", "Si")
                }




                dataRes = MutableList(monedas.length()) { i ->
                    Coin(monedas.getJSONObject(i).getString("codename"),monedas.getJSONObject(i).getString("name").capitalize(), resultados.getString("basename"),monedas.get(i).toString())
                }


                setupRecyclerView(item_list)

            } else {

                //Algo

            }
        }


    }
}
