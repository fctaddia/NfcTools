package nfctools.io

import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.io.IOException

class NfcReaderFragment : DialogFragment() {

    private var mTvMessage: TextView? = null
    private var mListener: Listener? = null
    private var message : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_read, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        mTvMessage = view.findViewById<View>(R.id.tv_message) as TextView
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

    fun onNfcDetected(ndef: Ndef) {
        readFromNFC(ndef)
    }

    private fun readFromNFC(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage : NdefMessage = ndef.ndefMessage
            message = String(ndefMessage.records[0].payload)
            Log.d(TAG, "readFromNFC: $message")
            ndef.close()
            mTvMessage!!.text = message
        } catch (e: IOException) {
            e.printStackTrace()
            mTvMessage!!.text = getString(R.string.message_read_error)
        } catch (e: FormatException) {
            e.printStackTrace()
            mTvMessage!!.text = getString(R.string.message_read_error)
        }
    }

    companion object {
        val TAG = NfcReaderFragment::class.java.simpleName
        fun newInstance(): NfcReaderFragment { return NfcReaderFragment() }
    }
}