package it.kenble.nfctools.nfc

import android.os.Bundle
import android.view.View
import android.nfc.tech.Ndef
import android.nfc.NdefRecord
import android.view.ViewGroup
import android.nfc.NdefMessage
import android.content.Context
import android.widget.ProgressBar
import android.nfc.FormatException
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment

import it.kenble.nfctools.R
import it.kenble.nfctools.ui.MainActivity
import it.kenble.nfctools.databinding.FragmentWriteBinding

import java.io.IOException
import java.nio.charset.Charset

/**
 * @author Francesco Taddia
 * @see 'https://github.com/fctaddia/NfcTools'
 */
class NfcWriterFragment : DialogFragment() {

    //region Variables

    companion object {
        val TAG: String = NfcWriterFragment::class.java.simpleName
        fun newInstance(): NfcWriterFragment { return NfcWriterFragment() }
    }

    private var listener : Listener? = null
    private var progress : ProgressBar? = null
    private lateinit var writeBind : FragmentWriteBinding

    // endregion

    // region Lifecycle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        writeBind = FragmentWriteBinding.inflate(layoutInflater)
        return writeBind.root
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

    fun onNfcDetected(ndef: Ndef, messageToWrite: String) { writeToNfc(ndef, messageToWrite) }

    private fun writeToNfc(ndef: Ndef?, message: String) {
        writeBind.tvMessage.text = getString(R.string.message_write_progress)
        if (ndef != null) {
            try {
                ndef.connect()
                val mimeRecord = NdefRecord.createMime("NfcTools", message.toByteArray(Charset.forName("US-ASCII")))
                ndef.writeNdefMessage(NdefMessage(mimeRecord))
                ndef.close()
                writeBind.tvMessage.text = getString(R.string.message_write_success)
            } catch (e: IOException) {
                e.printStackTrace()
                writeBind.tvMessage.text = getString(R.string.message_write_error)
            } catch (e: FormatException) {
                e.printStackTrace()
                writeBind.tvMessage.text = getString(R.string.message_write_error)
            }
        }
    }

    // endregion

}
