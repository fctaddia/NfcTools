package it.kenble.nfctools.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import it.kenble.nfctools.R
import it.kenble.nfctools.databinding.ActivityMainBinding
import it.kenble.nfctools.nfc.Listener
import it.kenble.nfctools.nfc.NfcReaderFragment
import it.kenble.nfctools.nfc.NfcWriterFragment

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), Listener {

    private lateinit var mainBind : ActivityMainBinding
    private var nfcAdapter: NfcAdapter?= null
    private var nfcread: NfcReaderFragment? = null
    private var nfcwrite: NfcWriterFragment? = null
    private var isDialogDisplayed : Boolean = false
    private var isWrite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        nfc() ; listeners()
    }

    override fun onResume() {
        super.onResume()
        refreshNfc()
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntentFilter = arrayOf(techDetected, tagDetected, ndefDetected)
        val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, Intent(this@MainActivity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        if (nfcAdapter != null) nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null)
    }

    override fun onPause() {
        super.onPause()
        refreshNfc()
        if (nfcAdapter != null) nfcAdapter!!.disableForegroundDispatch(this)
    }

    override fun onDialogDisplayed() { isDialogDisplayed = true }

    override fun onDialogDismissed() { isDialogDisplayed = false ; isWrite = false }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG)
        Log.d(TAG, "onNewIntent: " + intent.action)
        if (tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show()
            val ndef = Ndef.get(tag as Tag?)
            if (isDialogDisplayed) {
                if (isWrite) {
                    val messageToWrite = mainBind.etMessage.text.toString()
                    nfcwrite = supportFragmentManager.findFragmentByTag(NfcWriterFragment.TAG) as NfcWriterFragment
                    nfcwrite?.onNfcDetected(ndef, messageToWrite)
                } else {
                    nfcread = supportFragmentManager.findFragmentByTag(NfcReaderFragment.TAG) as NfcReaderFragment
                    nfcread?.onNfcDetected(ndef)
                }
            }
        }
    }

    private fun listeners() {
        mainBind.btnRead.setOnClickListener { if(!isNfcEnabled()){alertNfcRead()} else { fragmentRead() } }
        mainBind.btnWrite.setOnClickListener { if(!isNfcEnabled()){alertNfcWrite()} else { fragmentWrite() } }
    }

    private fun nfc() {
        val pm = packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            refreshNfc()
        } else {
            refreshNfc()
            Toast.makeText(this, "Your device does not support NFC", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshNfc(){
        if (nfcAdapter != null && isNfcEnabled()) {
            Log.d("Nfc","Nfc On")
        } else if (nfcAdapter != null && !isNfcEnabled()){
            Log.d("Nfc","Nfc Off")
        } else {
            alertNfc()
        }
    }

    private fun isNfcEnabled(): Boolean {
        if (nfcAdapter != null) {
            return try { nfcAdapter!!.isEnabled
            } catch (exp: Exception) { try { nfcAdapter!!.isEnabled } catch (exp: Exception) { false } }
        } ; return false
    }

    private fun alertNfcWrite() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Per poter scrivere sul TAG è necessario attivare l'NFC")
            .setCancelable(false)
            .setPositiveButton("Attiva") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Vui scrivere sul TAG?")
        alert.show()
    }

    private fun alertNfcRead() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Per poter leggere un TAG è necessario attivare l'NFC")
            .setCancelable(false)
            .setPositiveButton("Attiva") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Vuoi leggere un TAG?")
        alert.show()
    }

    private fun alertNfc() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Sembra che il tuo telefono non abbia l'NFC")
            .setCancelable(false)
            .setNegativeButton("Chiudi") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Non hai l'NFC")
        alert.show()
    }

    private fun fragmentWrite() {
        isWrite = true
        nfcwrite = supportFragmentManager.findFragmentByTag(NfcWriterFragment.TAG) as? NfcWriterFragment
        if (nfcwrite == null) { nfcwrite = NfcWriterFragment.newInstance() }
        nfcwrite?.show(supportFragmentManager, NfcWriterFragment.TAG)
    }

    private fun fragmentRead() {
        nfcread = supportFragmentManager.findFragmentByTag(NfcReaderFragment.TAG) as? NfcReaderFragment
        if (nfcread == null) { nfcread = NfcReaderFragment.newInstance() }
        nfcread?.show(supportFragmentManager, NfcReaderFragment.TAG)
    }
}