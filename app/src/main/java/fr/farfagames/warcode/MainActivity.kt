package fr.farfagames.warcode

import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.os.Handler

class GameCharacter {
    var varPV: Int = 100
    var varPF: Int = 40
    var varPD: Int = 25
    var varmonnaie: Int = 20
}

class MainActivity : AppCompatActivity() {

    private lateinit var codeValueTextView : TextView
    private lateinit var startScanButton: Button

    private lateinit var lblmonnaie: TextView
    private lateinit var lblPV: TextView
    private lateinit var lblPD: TextView
    private lateinit var lblPF: TextView
    private lateinit var lblScanRestant: TextView
    private lateinit var gameCharacter: GameCharacter

    private val increaseTextView: TextView by lazy { findViewById(R.id.increase_text) }

    companion object {
        val flashedCodes = mutableListOf<String>()
        var nbrScanJournalier : Int = 5
    }

    object SharedData {
        var ValCodeScan : String = ""
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra(ScanCodeActivity.CODE_KEY)
            //    val sharedVariable = intent.getStringExtra("ValCodeScan")

                if (!flashedCodes.contains(SharedData.ValCodeScan)) {
                    flashedCodes.add(SharedData.ValCodeScan)
                    //actualiseGameCharacter(SharedData.ValCodeScan)
                    updateCodeTextView(data)
                }else{
                    val toast = Toast.makeText(this, "Ce code a déjà été scanné aujourd'hui", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
        }

    private fun updateCodeTextView(data: String?) {
    data?.let {
        val gameCharacter = actualiseGameCharacter(data)
        runOnUiThread{
            codeValueTextView.text = it
//on met a jour les valeurs des textview en se basant sur les valeurs du gameCharacter
            lblmonnaie.text = gameCharacter.varmonnaie.toString()
            lblPV.text = gameCharacter.varPV.toString()
            lblPF.text = gameCharacter.varPF.toString()
            lblPD.text = gameCharacter.varPD.toString()

        }
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gameCharacter = GameCharacter()

        codeValueTextView= findViewById(R.id.code_value_tv)
        startScanButton= findViewById(R.id.start_scan_button)

        lblmonnaie= findViewById((R.id.hero_monaie))
        lblPV= findViewById(R.id.hero_PV)
        lblPF= findViewById(R.id.hero_PF)
        lblPD= findViewById(R.id.hero_PD)

        lblScanRestant= findViewById(R.id.nbr_scan_restant)

        initButtonClickListener()

        val buyScanButton:Button = findViewById(R.id.buy_scan_button)

        buyScanButton.setOnClickListener {
            buyAdditionalScan()
        }
// premiere initialisation des textview avec les valeurs de base
        //TODO lire la base de donnée pour afficher les valeur de la précédente connexion
        lblmonnaie.text = gameCharacter.varmonnaie.toString()
        lblPV.text = gameCharacter.varPV.toString()
        lblPF.text = gameCharacter.varPF.toString()
        lblPD.text = gameCharacter.varPD.toString()
    }

    private fun initButtonClickListener() {
        startScanButton.setOnClickListener {
            if (flashedCodes.size == nbrScanJournalier) {
                // Afficher un message indiquant que le nombre maximum de scans a été atteint.
                val message = "Vous avez atteint le nombre maximum de scans journalier. (5)"
                val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                toast.show()
            } else {
                // Lancez l'activité de numérisation uniquement si le nombre de scans est inférieur à 5.
                val intent = Intent(this, ScanCodeActivity::class.java)
                resultLauncher.launch(intent)
            }
        }
    }

    private fun showIncreaseText(value: Int) {
        "+$value".also { increaseTextView.text = it } // Afficher le texte +25 (ou une autre valeur)
        increaseTextView.visibility = View.VISIBLE // Rendre le TextView visible

        // Planifier la disparition du TextView après quelques secondes (par exemple, 3 secondes)
        Handler().postDelayed({
            increaseTextView.visibility = View.GONE // Rendre le TextView invisible après 3 secondes
        }, 3000) // 3000 millisecondes (3 secondes)
    }

    private fun actualiseGameCharacter(barcodeValue: String?): GameCharacter {
        //sauvegarde du player
        // Utiliser le code-barres scanné (barcodeValue) et le seed de la date pour générer le seed
        val seed = generateSeed(barcodeValue)
        val random = Random(seed)

        // Générer les valeurs

        gameCharacter.varPD += generatePointDefence(random)
        gameCharacter.varPF += generatePointForce(random)
        gameCharacter.varPV += generatePointVie(random)
        gameCharacter.varmonnaie += generateMonnaie(random)

        return gameCharacter
    }

    private fun generateSeed(barcodeValue: String?): Long {
        // Créez un "seed" basé sur le code-barres scanné et la date actuelle
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val seedString = "$barcodeValue$currentDate"
        return seedString.hashCode().toLong()
    }

    private fun generatePointDefence(random:Random): Int {
        return random.nextInt(1,10)
    }

    private fun generatePointForce(random:Random): Int {
        return random.nextInt(1,10)
    }
    private fun generatePointVie(random:Random): Int {
        return random.nextInt(1,10)
    }

    private fun generateMonnaie(random:Random) : Int{
        return random.nextInt(10,50)
    }

    private fun buyAdditionalScan(){

        val scanCost = 50 // Coût d'un scan supplémentaire
        if (gameCharacter.varmonnaie >= scanCost) {
            // Le joueur a suffisamment de monnaie pour acheter un scan
            gameCharacter.varmonnaie -= scanCost // Décrémenter la monnaie
            // Autoriser le scan supplémentaire
            nbrScanJournalier += 1
            lblmonnaie.text = gameCharacter.varmonnaie.toString()

            Toast.makeText(this, "Scan acheté. Nouvelle monnaie : ${gameCharacter.varmonnaie}", Toast.LENGTH_SHORT).show()

        } else {
            val message = "Vous n'avez pas assez de monnaie pour acheter un scan supplémentaire. Vous avez actuellement ${gameCharacter.varmonnaie} monnaie.\nRendez-vous demain !"
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Achat de scan supplémentaire")
            builder.setMessage(message)
            builder.setPositiveButton("OK") { _, _ ->
                // Gérer le clic sur le bouton "OK" de la boîte de dialogue
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}