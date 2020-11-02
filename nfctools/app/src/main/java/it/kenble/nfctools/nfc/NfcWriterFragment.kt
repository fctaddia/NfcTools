package it.kenble.nfctools.nfc

import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import it.kenble.nfctools.R
import it.kenble.nfctools.databinding.FragmentWriteBinding
import it.kenble.nfctools.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_write.view.*
import java.io.IOException
import java.nio.charset.Charset

class NfcWriterFragment : DialogFragment() {

    private lateinit var writeBind : FragmentWriteBinding
    private var listener : Listener? = null
    private var progress : ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        writeBind = DataBindingUtil.inflate(inflater, R.layout.fragment_write,container,false) ; return writeBind.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as MainActivity
        listener!!.onDialogDisplayed()
    }

    override fun onDetach() { super.onDetach() ; listener!!.onDialogDismissed() }

    fun onNfcDetected(ndef: Ndef, messageToWrite: String) { writeToNfc(ndef, messageToWrite) }

    private fun writeToNfc(ndef: Ndef?, message: String) {
        writeBind.root.tv_message?.text = getString(R.string.message_write_progress)
        if (ndef != null) {
            try {
                ndef.connect()
                val mimeRecord = NdefRecord.createMime("NfcTools", message.toByteArray(Charset.forName("US-ASCII")))
                ndef.writeNdefMessage(NdefMessage(mimeRecord))
                ndef.close()
                writeBind.root.tv_message!!.text = getString(R.string.message_write_success)
            } catch (e: IOException) {
                e.printStackTrace()
                writeBind.root.tv_message!!.text = getString(R.string.message_write_error)
            } catch (e: FormatException) {
                e.printStackTrace()
                writeBind.root.tv_message!!.text = getString(R.string.message_write_error)
            }
        }
    }

    companion object {
        val TAG: String = NfcWriterFragment::class.java.simpleName
        fun newInstance(): NfcWriterFragment { return NfcWriterFragment() }
    }
}