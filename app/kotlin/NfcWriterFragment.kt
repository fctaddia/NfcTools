package nfctools.io

import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.io.IOException
import java.nio.charset.Charset


class NfcWriterFragment : DialogFragment() {

    private var mTvMessage: TextView? = null
    private var mListener: Listener? = null
    private var mProgress: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_write, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        mTvMessage = view.findViewById<View>(R.id.tv_message) as TextView
        mProgress = view.findViewById<View>(R.id.progress) as ProgressBar
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as MainActivity
        mListener!!.onDialogDisplayed()
    }

    override fun onDetach() {
        super.onDetach()
        mListener!!.onDialogDismissed()
    }

    fun onNfcDetected(ndef: Ndef, messageToWrite: String) {
        writeToNfc(ndef, messageToWrite)
    }

    private fun writeToNfc(ndef: Ndef?, message: String) {
        mTvMessage?.text = getString(R.string.message_write_progress)
        if (ndef != null) {
            try {
                ndef.connect()
                val mimeRecord = NdefRecord.createMime("NfcTools", message.toByteArray(Charset.forName("US-ASCII")))
                ndef.writeNdefMessage(NdefMessage(mimeRecord))
                ndef.close()
                mTvMessage!!.text = getString(R.string.message_write_success)
            } catch (e: IOException) {
                e.printStackTrace()
                mTvMessage!!.text = getString(R.string.message_write_error)
            } catch (e: FormatException) {
                e.printStackTrace()
                mTvMessage!!.text = getString(R.string.message_write_error)
            }
        }
    }

    companion object {
        val TAG = NfcWriterFragment::class.java.simpleName
        fun newInstance(): NfcWriterFragment { return NfcWriterFragment() }
    }

}