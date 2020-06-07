package nfctools.io

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings


private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() , Listener {

    private lateinit var mEtMessage: EditText
    private lateinit var mBtRead: Button
    private lateinit var mBtWrite: Button
    private var nfcAdapter: NfcAdapter?= null
    private var mNfcReadFragment: NfcReaderFragment? = null
    private var mNfcWriteFragment: NfcWriterFragment? = null
    private var isDialogDisplayed : Boolean = false
    private var isWrite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObj()
        initListeners()
        initNFC()
    }

    private fun initObj() {
        mEtMessage = findViewById(R.id.et_message)
        mBtRead = findViewById(R.id.btn_read)
        mBtWrite = findViewById(R.id.btn_write)
    }
    private fun initListeners() {
        mBtRead.setOnClickListener { if(!isNfcEnabled()){showAlertDialogNdefRead()} else {showReadFragment()}}
        mBtWrite.setOnClickListener { if(!isNfcEnabled()){showAlertDialogNdefWrite()} else {showWriteFragment()}}
    }

    private fun initNFC() {
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
            showAlertDialogNfcNa()
        }
    }

    private fun isNfcEnabled(): Boolean {
        if (nfcAdapter != null) {
            return try { nfcAdapter!!.isEnabled
            } catch (exp: Exception) { try { nfcAdapter!!.isEnabled } catch (exp: Exception) { false } }
        } ; return false
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
                    val messageToWrite = mEtMessage.text.toString()
                    mNfcWriteFragment = supportFragmentManager.findFragmentByTag(NfcWriterFragment.TAG) as NfcWriterFragment
                    mNfcWriteFragment?.onNfcDetected(ndef, messageToWrite)
                } else {
                    mNfcReadFragment = supportFragmentManager.findFragmentByTag(NfcReaderFragment.TAG) as NfcReaderFragment
                    mNfcReadFragment?.onNfcDetected(ndef)
                }
            }
        }
    }

    private fun showAlertDialogNdefWrite() {
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

    private fun showAlertDialogNdefRead() {
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

    private fun showAlertDialogNfcNa() {
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

    private fun showWriteFragment() {
        isWrite = true
        mNfcWriteFragment = supportFragmentManager.findFragmentByTag(NfcWriterFragment.TAG) as? NfcWriterFragment
        if (mNfcWriteFragment == null) { mNfcWriteFragment = NfcWriterFragment.newInstance() }
        mNfcWriteFragment?.show(supportFragmentManager, NfcWriterFragment.TAG)
    }

    private fun showReadFragment() {
        mNfcReadFragment = supportFragmentManager.findFragmentByTag(NfcReaderFragment.TAG) as? NfcReaderFragment
        if (mNfcReadFragment == null) { mNfcReadFragment = NfcReaderFragment.newInstance() }
        mNfcReadFragment?.show(supportFragmentManager, NfcReaderFragment.TAG)
    }


}