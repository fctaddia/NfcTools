package it.kenble.nfctools.nfc

/**
 * @author Francesco Taddia
 * @see 'https://github.com/fctaddia/NfcTools'
 */
interface Listener {
    
    /**
     * Show NFC Dialog Reader
     */
    fun onDialogDisplayed()
    
    /**
     * Dismiss NFC Dialog reader
     */
    fun onDialogDismissed()
    
}
