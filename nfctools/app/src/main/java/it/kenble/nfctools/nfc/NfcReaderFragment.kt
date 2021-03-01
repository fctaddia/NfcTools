package it.kenble.nfctools.nfc

import android.util.Log
import android.os.Bundle
import android.view.View
import android.nfc.tech.Ndef
import android.view.ViewGroup
import android.nfc.NdefMessage
import android.content.Context
import android.nfc.FormatException
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment

import it.kenble.nfctools.R
import it.kenble.nfctools.ui.MainActivity
import it.kenble.nfctools.databinding.FragmentReadBinding

import java.io.IOException

/**
 * @author Francesco Taddia
 * @see 'https://github.com/fctaddia/NfcTools'
 */
class NfcReaderFragment : DialogFragment() {

    // region Variables

    companion object {
        val TAG: String = NfcReaderFragment::class.java.simpleName
        fun newInstance(): NfcReaderFragment { return NfcReaderFragment() }
    }

    private var message: String? = null
    private var listener: Listener? = null
    private lateinit var readBind: FragmentReadBinding

    // endregion

    // region Lifecycle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        readBind = FragmentReadBinding.inflate(layoutInflater)
        return readBind.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as MainActivity
        listener!!.onDialogDisplayed()
    }

    override fun onDetach() {
        super.onDetach()
        listener!!.onDialogDismissed()
    }

    // endregion

    // region Nfc

    internal fun onNfcDetected(ndef: Ndef) { readFromNFC(ndef) }

    private fun readFromNFC(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage : NdefMessage = ndef.ndefMessage
            message = String(ndefMessage.records[0].payload)
            Log.d(TAG, "readFromNFC: $message")
            ndef.close()
            readBind.tvMessage.text = message
        } catch (e: IOException) {
            e.printStackTrace()
            readBind.tvMessage.text = getString(R.string.message_read_error)
        } catch (e: FormatException) {
            e.printStackTrace()
            readBind.tvMessage.text = getString(R.string.message_read_error)
        }
    }

    // endregion

}
