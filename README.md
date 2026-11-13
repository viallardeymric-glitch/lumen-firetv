# Lumen TV

Petite appli Android TV pour Fire Stick : ouvre Lumen (hébergée sur ton NAS) en plein écran, se lance seule à l'allumage et transmet la télécommande à la page.

## Installer sur le Fire Stick
1. Fire Stick : Paramètres > Ma Fire TV > Options pour les développeurs > autoriser « Installer des applications inconnues » pour Downloader.
2. Dans Downloader, saisis : `https://github.com/viallardeymric-glitch/lumen-firetv/releases/download/derniere/lumen.apk`
3. Installe, puis ouvre Lumen **une fois** (Android ne lance une appli au démarrage qu'après sa première ouverture).
4. Redémarre le Fire Stick : Lumen doit s'ouvrir toute seule.

L'adresse de Lumen se change dans `gradle.properties` (`LUMEN_URL`), puis la compilation GitHub Actions régénère l'APK.

## Alléger le Fire Stick (ADB, depuis le MacBook)
1. Fire Stick : Options pour les développeurs > **Débogage ADB** : activé.
2. Mac : `brew install android-platform-tools`
3. `adb connect ADRESSE_IP_DU_STICK:5555` (Paramètres > Ma Fire TV > À propos > Réseau). Accepte la demande sur la télé.
4. Liste les applications : `adb shell pm list packages > paquets.txt`
5. Pour désactiver une appli (réversible) : `adb shell pm disable-user --user 0 NOM_DU_PAQUET`
   Pour la réactiver : `adb shell pm enable NOM_DU_PAQUET`

Ne désactive pas `com.amazon.tv.launcher` (l'accueil Amazon) ni les paquets système sans savoir à quoi ils servent.
