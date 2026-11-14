package fr.lumen.tv;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Lit les touches de la télécommande avant le système. Pendant un film, les touches de volume sont
 * gardées pour Lumen (réglage des HomePod) ; le reste du temps, elles repartent au système (volume de la télé).
 */
public class LumenKeyService extends AccessibilityService {
    @Override public void onAccessibilityEvent(AccessibilityEvent e) { }
    @Override public void onInterrupt() { }

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        int code = e.getKeyCode();
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) MainActivity.noter("svc", code);
        String touche = null;
        if (code == KeyEvent.KEYCODE_VOLUME_UP) touche = "AudioVolumeUp";
        else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) touche = "AudioVolumeDown";
        else if (code == KeyEvent.KEYCODE_VOLUME_MUTE) touche = "AudioVolumeMute";
        if (touche != null && MainActivity.capte()) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) MainActivity.envoyer(touche);
            return true; // consommée : le volume de la télé ne bouge pas
        }
        return false;
    }
}
