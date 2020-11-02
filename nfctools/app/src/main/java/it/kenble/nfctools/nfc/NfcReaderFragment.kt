package it.kenble.nfctools.nfc

import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import it.kenble.nfctools.R
import it.kenble.nfctools.databinding.FragmentReadBinding
import it.kenble.nfctools.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_read.view.*
import java.io.IOException

class NfcReaderFragment : DialogFragment() {

    private lateinit var readBind : FragmentReadBinding
    private var listener : Listener? = null
    private var message : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        readBind = DataBindingUtil.inflate(inflater,R.layout.fragment_read,container,false) ; return readBind.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as MainActivity
        listener!!.onDialogDisplayed()
    }

    override fun onDetach() { super.onDetach() ; listener!!.onDialogDismissed() }

    fun onNfcDetected(ndef: Ndef) { readFromNFC(ndef) }

    private fun readFromNFC(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage : NdefMessage = ndef.ndefMessage
            message = String(ndefMessage.records[0].payload)
            Log.d(TAG, "readFromNFC: $message")
            ndef.close()
            readBind.root.tv_message!!.text = message
        } catch (e: IOException) {
            e.printStackTrace()
            readBind.root.tv_message!!.text = getString(R.string.message_read_error)
        } catch (e: FormatException) {
            e.printStackTrace()
            readBind.root.tv_message!!.text = getString(R.string.message_read_error)
        }
    }

    companion object {
        val TAG: String = NfcReaderFragment::class.java.simpleName
        fun newInstance(): NfcReaderFragment { return NfcReaderFragment() }
    }
}
