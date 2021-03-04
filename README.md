<img src="docs/nfctools_logo.png" alt="Showcase" height="100px">

## NfcTools
NfcTools simplifies reading and writing on NFC tags

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.31-e60202.svg?style=flat-square)](http://kotlinlang.org)
[![AndroidX](https://img.shields.io/badge/AndroidX-1.3.2-000000.svg?style=flat-square)](https://developer.android.com/jetpack/androidx/)
[![GitHub (pre-)release](https://img.shields.io/github/v/release/fctaddia/nfctools.svg?color=f77200&label=Release&style=flat-square)](./../../releases)
[![License](https://img.shields.io/github/license/fctaddia/NfcTools?color=03DAC5&label=License)](https://opensource.org/licenses/MIT)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2f1acba06d8d4224953814006836d199)](https://www.codacy.com/manual/fctaddia/NfcTools?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fctaddia/NfcTools&amp;utm_campaign=Badge_Grade)

### NFC reading

1)  To read an NFC tag, an interface that is always listening to the NFC sensor is required

Listener.kt:

```kotlin
interface Listener {
    fun onDialogDisplayed()
    fun onDialogDismissed()
}
```
2)  Implemented this interface, create the class for reading nfc. Created the class, extend it with the DialogFragment library:
```kotlin
class NfcReaderFragment : DialogFragment() {
    ...
}
```
3)  Extended the NfcReaderFragment class with DialogFragment you will be asked to override several functions:
```kotlin
override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    ...
}

override fun onAttach(context: Context) {
    super.onAttach(context)
    ...
}
override fun onDetach() {
    super.onDetach()
    ...
}
```
4)  Create the functions for the actual reading of nfc, which will be called each time the TAG NfcAdapter.ACTION_TAG_DISCOVERED is invoked:
```kotlin
companion object {
    val TAG = NfcReaderFragment::class.java.simpleName
    fun newInstance(): NfcReaderFragment { return NfcReaderFragment() }
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
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: FormatException) {
        e.printStackTrace()
    }
}
```
### NFC writing

To create the listener interface and main functions, follow steps 1,2,3. Obviously change the class name to NfcWriterFragment

Create the actual nfc write functions, which will be called each time TAG NfcAdapter.ACTION_TAG_DISCOVERED is called:
```kotlin
companion object {
    val TAG = NfcWriterFragment::class.java.simpleName
    fun newInstance(): NfcWriterFragment { return NfcWriterFragment() }
}

fun onNfcDetected(ndef: Ndef, messageToWrite: String) {
    writeToNfc(ndef, messageToWrite)
}

private fun writeToNfc(ndef: Ndef?, message: String) {
    if (ndef != null) {
        try {
            ndef.connect()
            val mimeRecord = NdefRecord.createMime("NfcTools", message.toByteArray(Charset.forName("US-ASCII")))
            ndef.writeNdefMessage(NdefMessage(mimeRecord))
            ndef.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
    }
}
```
