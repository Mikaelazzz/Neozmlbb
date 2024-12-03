package id.vincent.neozmlbb

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import kotlinx.coroutines.*

class HeroShuffleManager(
    private val mainActivity: MainActivity,
    private val heroData: List<HeroItem>,
    private val funCheckbox: RadioButton,
    private val roleCheckbox: RadioButton,
    private val player3Checkbox: RadioButton,
    private val player5Checkbox: RadioButton,
    private val heroesRecyclerView: RecyclerView,
    private val shuffleButton: Button
) {
    // Data class to represent hero information
    data class HeroItem(
        val hero: String,
        val imageHero: String,
        val role: String
    )

    // ViewHolder for the RecyclerView
    class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerTextView: TextView = itemView.findViewById(R.id.player)
        val heroImageView: ImageView = itemView.findViewById(R.id.imgHero)
        val heroNameTextView: TextView = itemView.findViewById(R.id.namaHero)
        val roleImageView: ImageView = itemView.findViewById(R.id.imgRole)
    }

    // Adapter for RecyclerView
    inner class HeroAdapter(private val heroes: List<HeroItem>) :
        RecyclerView.Adapter<HeroViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            val hero = heroes[position]

            // Set player number
            holder.playerTextView.text = "Player ${position + 1}"

            // Set hero name
            holder.heroNameTextView.text = hero.hero

            // Set hero image
            val heroImageResourceId = mainActivity.resources.getIdentifier(
                hero.imageHero, "drawable", mainActivity.packageName
            )
            if (heroImageResourceId != 0) {
                holder.heroImageView.setImageResource(heroImageResourceId)
            }

            // Set role image based on role
            val roleImageResourceId = when (hero.role.lowercase()) {
                "mage" -> R.drawable.mid
                "gold" -> R.drawable.gold
                "jung" -> R.drawable.jung
                "roam" -> R.drawable.roam
                "exp" -> R.drawable.exp
                else -> R.drawable.vengeance
            }
            holder.roleImageView.setImageResource(roleImageResourceId)
        }

        override fun getItemCount() = heroes.size
    }

    fun setupShuffleLogic() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        // Ensure only one checkbox can be checked at a time
        funCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) roleCheckbox.isChecked = false
        }

        roleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) funCheckbox.isChecked = false
        }

        // Ensure only one player count checkbox can be checked at a time
        player3Checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) player5Checkbox.isChecked = false
        }

        player5Checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) player3Checkbox.isChecked = false
        }

        shuffleButton.setOnClickListener {
            val selectedHeroes: List<HeroShuffleManager.HeroItem> = when {
                !funCheckbox.isChecked && !roleCheckbox.isChecked -> {
                    mainActivity.showToast("Please select a mode")
                    return@setOnClickListener
                }
                !player3Checkbox.isChecked && !player5Checkbox.isChecked -> {
                    mainActivity.showToast("Please select number of players")
                    return@setOnClickListener
                }
                funCheckbox.isChecked -> {
                    shuffleFunMode() // Fungsi ini tetap dipanggil, tetapi hasilnya diabaikan
                    listOf() // Inisialisasi list kosong (karena fungsi tidak mengembalikan nilai)
                }
                roleCheckbox.isChecked -> {
                    shuffleHeroesByRole() // Fungsi ini tetap dipanggil, tetapi hasilnya diabaikan
                    listOf()
                }
                else -> emptyList()
            }

            if (selectedHeroes.isEmpty()) {
                return@setOnClickListener
            }

            // Update RecyclerView dengan tampilan satu per satu
            updateRecyclerViewSequentially(selectedHeroes)

            val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)
            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE
        }

    }

    private fun updateRecyclerViewSequentially(heroes: List<HeroItem>) {
        val availableHeroes = heroes.toMutableList() // Copy of heroes to prevent modifying original list
        val selectedHeroes = mutableListOf<HeroItem>() // To store selected heroes

        // Coroutine untuk menambahkan efek visual "gacha"
        CoroutineScope(Dispatchers.Main).launch {
            // Set RecyclerView untuk efek gacha
            // Jangan menggunakan GridLayoutManager dengan ukuran hero yang dinamis, gunakan LinearLayoutManager jika perlu.
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)

            heroesRecyclerView.adapter = HeroAdapter(listOf()) // Start with an empty adapter

            // Rapidly shuffle dan update RecyclerView untuk efek visual
            for (i in 1..20) { // Show swapping effect for 20 cycles
                delay(500) // Short delay to simulate fast swapping
                val shuffledHeroes = availableHeroes.shuffled() // Shuffle heroes for each cycle
                heroesRecyclerView.adapter = HeroAdapter(shuffledHeroes) // Update dengan shuffled heroes
            }

            // Ambil heroes berdasarkan jumlah pemain yang dipilih
            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            // Loop untuk memilih hero untuk setiap player
            for (i in 1..playerCount) {
                delay(0) // Small delay before picking the next hero
                val heroToPick = availableHeroes.random() // Pilih hero acak dari yang tersedia
                selectedHeroes.add(heroToPick) // Tambahkan hero yang dipilih ke list
                availableHeroes.remove(heroToPick) // Hapus hero yang dipilih untuk mencegah seleksi ulang
            }

            // Tampilkan heroes yang dipilih setelah shuffle
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false) // Pastikan horizontal

            // Update adapter hanya sekali setelah shuffle selesai
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes) // Update adapter untuk menampilkan heroes yang dipilih
        }
    }




    private fun shuffleFunMode() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        CoroutineScope(Dispatchers.Main).launch {
            // Copy hero data untuk diacak
            val availableHeroes = heroData.toMutableList()
            val selectedHeroes = mutableListOf<HeroItem>()

            // Batasi jumlah pemain berdasarkan checkbox
            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            // RecyclerView sementara untuk efek gacha
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(listOf()) // Kosongkan awalnya

            // Lakukan swapping visual untuk efek gacha
            for (cycle in 1..20) {
                delay(50) // Durasi swapping per siklus (bisa diatur)
                val tempHeroes = availableHeroes.shuffled().take(playerCount) // Ambil sejumlah pemain
                val tempAdapterHeroes = tempHeroes.map {
                    // Tampilkan hero acak dengan role acak sementara
                    it.copy(role = listOf("Mage", "Gold", "Jung", "Roam", "Exp").random())
                }
                heroesRecyclerView.adapter = HeroAdapter(tempAdapterHeroes) // Tampilkan acakan sementara
            }

            // Setelah swapping selesai, tentukan hasil akhir
            while (selectedHeroes.size < playerCount) {
                val heroToPick = availableHeroes.random()
                val randomRole = listOf("Mage", "Gold", "Jung", "Roam", "Exp").random()

                // Pastikan hero atau role tidak terduplikasi
                if (selectedHeroes.any { it.hero == heroToPick.hero || it.role == randomRole }) continue

                // Tambahkan hero ke daftar hasil akhir
                selectedHeroes.add(heroToPick.copy(role = randomRole))
                availableHeroes.remove(heroToPick)
            }

            // Tampilkan hasil akhir pada RecyclerView
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes) // Tampilkan hasil akhir

            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE

            // Tampilkan pesan pop-up setelah selesai swapping
            showFunModeMessage()
        }
    }

    // Fungsi untuk menampilkan AlertDialog setelah selesai swapping
    private fun showFunModeMessage() {
        val builder = android.app.AlertDialog.Builder(mainActivity)

        // Set message and button
        builder.setMessage("Gunakan hero ini hanya di mode Clasic yaa..")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
                // Menutup dialog ketika tombol OK ditekan
                dialog.dismiss()
            }

        // Menambahkan custom layout untuk AlertDialog
        val alert = builder.create()

        // Menetapkan background dari drawable
        alert.window?.setBackgroundDrawableResource(R.drawable.back)

        // Menampilkan dialog
        alert.show()
    }




    private fun shuffleHeroesByRole() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        CoroutineScope(Dispatchers.Main).launch {
            val availableHeroes = heroData.toMutableList()
            val selectedHeroes = mutableListOf<HeroItem>()

            // Cek apakah heroData tidak kosong
            if (availableHeroes.isEmpty()) {
                return@launch
            }

            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            // RecyclerView sementara untuk efek gacha
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(listOf()) // Kosongkan awalnya


            // Tampilkan efek shuffle visual selama beberapa iterasi
            for (cycle in 1..20) {
                delay(50) // Memberikan sedikit jeda di antara setiap swap untuk efek yang lebih smooth

                // Shuffle heroes secara acak untuk efek visual
                val tempHeroes = availableHeroes.shuffled().take(playerCount)
                val tempAdapterHeroes = tempHeroes.map { it.copy() }  // Tidak ubah role untuk visual effect

                // Update RecyclerView dengan hero yang di-shuffle
                heroesRecyclerView.adapter = HeroAdapter(tempAdapterHeroes)
            }

            // Pilih hero yang unik berdasarkan role setelah proses shuffle selesai
            while (selectedHeroes.size < playerCount) {
                val heroToPick = availableHeroes.random()
                val randomRole = heroToPick.role // Dapatkan role dari hero yang dipilih

                // Pastikan hero atau role tidak terduplikasi
                if (selectedHeroes.any { it.hero == heroToPick.hero || it.role == randomRole }) {
                    continue // Jika hero atau role sudah ada, lanjutkan ke hero berikutnya
                }

                // Jika tidak ada duplikat, tambahkan hero yang dipilih
                selectedHeroes.add(heroToPick)
                availableHeroes.remove(heroToPick)
            }

            // Update RecyclerView dengan hero yang dipilih secara unik
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes)  // Update adapter dengan heroes yang dipilih

            // Tampilkan hasil akhir pada RecyclerView
            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE
        }
    }


    private fun updateRecyclerView(heroes: List<HeroItem>) {
        heroesRecyclerView.layoutManager = GridLayoutManager(mainActivity, heroes.size)
        heroesRecyclerView.adapter = HeroAdapter(heroes)
    }

    companion object {
        // Helper method to parse JSON data
        fun parseHeroData(jsonString: String): List<HeroItem> {
            val jsonArray = JSONArray(jsonString)
            return (0 until jsonArray.length()).map { index ->
                val heroJson = jsonArray.getJSONObject(index)
                HeroItem(
                    hero = heroJson.getString("hero"),
                    imageHero = heroJson.getString("imageHero"),
                    role = heroJson.getString("role")
                )
            }
        }
    }
}

// Extension function to show toast in MainActivity
fun MainActivity.showToast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

// In MainActivity
class MainActivity : AppCompatActivity() {
    private lateinit var heroShuffleManager: HeroShuffleManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Read JSON from resources
        val jsonString = assets.open("heroes.json").bufferedReader().use { it.readText() }

        heroShuffleManager = HeroShuffleManager(
            mainActivity = this,
            heroData = HeroShuffleManager.parseHeroData(jsonString),
            funCheckbox = findViewById(R.id.funrole),
            roleCheckbox = findViewById(R.id.role),
            player3Checkbox = findViewById(R.id.main3),
            player5Checkbox = findViewById(R.id.main5),
            heroesRecyclerView = findViewById(R.id.heroes),
            shuffleButton = findViewById(R.id.shuffle)
        )

        heroShuffleManager.setupShuffleLogic()
    }
}