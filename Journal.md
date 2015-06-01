#Journal

Idée du jour : 
On veut envoyer un message de Alice à Bob. 

Pour cela, on va envoyer en broadcast le message de Alice, chiffré par la clé publique de Bob.
=> tous les intermédiaires du réseau recevront le message s'ils croisent Alice.

Les intermédiaires transmettront le message en broadcast à leurs voisins si ces derniers n'ont pas déjà le message. 
Lorsque Bob croise un intermédiaire qui possède le message d'Alice, un signal est envoyé via internet et ce message 
est effacé du portable de tous les porteurs, Alice comprise.

=> Pour gérer la diffusion de plusieurs messages en même temps, on peut considérer une base de données implantée dans chaque téléphone.
Chaque utilisateur synchronisera sa BDD de messages avec son voisin, ajoutant uniquement les messages qu'il ne porte pas encore.

Avantages :
  - On garantit que le message se transmettera le plus rapidement possible, puisque tous les intermédiaires possèdent le message.
  - On se passe de recherche du chemin le plus cours et par la même de statistiques de rencontre des utilisateurs
  
Inconvénients :
  - Peut vite devenir lourd puisque chaque téléphone possède potentiellemet tous les messages en cours de transfert (cependant, on peut considérer que puisque l'application fonctionnera de manière relativement locale - pas dans le monde entier- la quantité de messages restera raisonnable, on pourra quand même implémenter une durée maximale de conservation des messages sur les intermédiaires)
  - Obligation de passer par internet pour envoyer à tous les intermédiaires l'accusé de reception d'un message pour qu'ils l'éffacent de leur base de données
  - Nécéssité (?) de centraliser la génération d'identifiants pour les messages ( non, UID)


suppression : verification d'authenticité du signal de suppression + nécéssité de connaitre tous les porteurs du message
messages supprimés au bout d'un certain temps => permet l'accusé de suppression via le même procédé


Timeout => enclenche le timeout quand on a transmis le message une fois

22/05 : Ajout du signal de suppression des messages reçu dans la simulation

26/05 : Réussite partielle d'envoi de texte via Nfc, ça marche dans un sens mais fait quelque chose de bizarre (alors que Baptiste et moi avons exactement le même smartphone tournant sur le même OS). 
- Envoi Baptiste -> Antoine : fonctionne comme prévu
- Envoi Antoine -> Baptiste : ouvre une fenetre chez baptiste indiquant qu'un nouveau tag a été ajouté.

27/05 : Discussion avec M.Casteigts et M.Klein :
- Il ne parrait pas possible d'effectuer une communication à deux sens avec android beam, on serait obligé de connecter deux fois les téléphones et d'envoyer chacun son tour. (Rechercher s'il n'y a pas moyen de contourner cela) 
- Comment gérer la base d'utilisateur? Si un utilisateur veut envoyer un message à un autre, il doit connaitre son identifiant (quel id choisir : clé publique? IMEI? ...) 
- Est-ce qu'on synchronise aussi la base d'utilisateurs automatiquement? ou alors ajout "manuel" (par selection dans un menu puis rapprochement des téléphones" ?


28/05 : Nous avons réussi à régler notre probleme avec le prototype d'envoi de messages. Nous savons désormais transférer un message d'un smartphone à un autre via android beam.
Premier pas dans la crypto effectué, on peut crypter/décrypter un message en utilisant la procédure RSA. Le message est donc chiffré via une clé publique, couplée à un salage et un byte array. Tout ceci est réalisé grâce à la librairie Encryption par simbiose.

Début de tentative d'implémentation d'une base SQLite sur l'application pour tests. (chercher autres moyens de stocker infos sous android) 
On s'est renseignés sur la génération de clés publiques et privées sous android ainsi que sur la facon de stocker la clé privée de manière sécurisée (cf partie crypto)
Nous n'avons pas touché à la simulation aujourd'hui

29/05 : Implémentation terminée (mais à tester) de la base SQLite (enfin). On s'attaque à la génération de clés publiques et privées avec KeyPairGenerator. On avait dans l'idée d'utiliser la fonctionnalité KeyStore (ou KeyChain, un eu plus bas niveau apparemment) d'Android qui permet de stocker les clés de manière sécurisée sur le téléphone.
A faire aujourd'hui :
- Générer le duo clé publique/clé privée pour chaque téléphone UNE FOIS et les stocker dans KeyStore/KeyChain -OK 
- Tenter d'envoyer un message crypté avec la clé publique du destinataire (manuellement pour l'instant)
- Synchronisation des messages et de la base de contacts lors d'une interaction entre portables.


Problème, AndroidKeyStore n'est disponnible que depuis la version 4.3, donc ca ne marchera pas sur des téléphones tournant sous des versions anterieures.

Changement de méthode de cryptage. Après avoir effectué certaines recherches, nous nous sommes aperçus qu'encryption ne nous proposait pas un service fiable au niveau sécurité (clé de 128 bits avec AES, "facilement" crackable apparemment) et rapide niveau utilisation (compter 8 secondes pour chiffrer le message et 8 autres secondes pour le déchiffrer). Nous avons donc effectué des recherches sur les différents algorithmes utilisés en cryptographie pour chiffrer nos messages, et nous avons décidé d'utiliser RSA. C'est l'algorithme qui est le plus recommandé pour son rapport sécurité/rapidité. Il met plus de temps à chiffrer qu'à déchiffrer, à l'inverse de son rival DSA. Nous avons maintenant un duo clé privée/clé publique composé de deux clés de 2048 bits(paramétrable), les messages sont plus ou moins longs à chiffrer en fonction de leur taille (compter 1 sec pour 2 lignes de sms). En revanche, le déchiffrage est instantané, ce qui nous sera favorable lors de la réception d'un message pour le destinataire, la notification lui permettra de consulter le message directement, sans attendre.

Problème : limité à 245 caractères.
( d'après "beginning cryptography with java" (chap4 p 98) de David Hook, si hLen est la longueur du h en octets et kLen la taille de la clé en octets, la taille max du message que l'on peut chiffrer devient : 
MaxLen = kLen -2hLen -2.
)

01/06
Crypto :

AES nous permettrait d'avoir une taille max de message de 250 millions de To mais marche avec un système symétrique (contrairement à RSA), et a besoin d'un premier échange d'une clé secrète sur le réseau. Nous confirmons donc notre choix de RSA, et limitons alors la taille d'un message à 245 caractères, ce qui devrait être suffisant pour envoyer un message équivalent à un SMS (longueur moyenne d'un SMS =60 caractères, sachant que la première limite de caractères d'un sms était de 160 caractères).

Base de données : 

Tentative d'implémentation d'ajout de contact via nfc (dans une autre activité, on rentre son nom et on envoie nom + clé public au destinataire qui enregistre dans sa base de données).
Problème au niveau de la priorisation des activités, la première activité est systématiquement appelée lors de la réception du CallBack, donc pas traité par l'autre activité.
Idée de solution : on "spécialise" les intent, la première activité recoit tout les callback et les traite en fonction des parametres contenus dans l'intent.

MAJ 01/06 : Lors de la réception d'un message, l'activité ne se relance plus, le message s'affiche correctement dans la vue prévue à cet effet. Lors de l'ajout d'un contact, une activité se relance(non souhaité).


TODO
---------------------
Structurer les données pour la création des bases : deux tables :
- Contacts : uuid, nom, cle_publique
- Message : uuid, nom_source, nom_destinataire, contenu ,  (XML/JSON)

Fusionner le code application TestNFC avec AppALaKon

Recherche sur how to nfc sans android beam pour éviter 2 connexions + 2 tap ==> 1 contact, 1 tap, synchronisation des BDD messages. (LLCP layer)




Etat de la simulation 
---------------------
- Envoi de messages avec intermédiaires fonctionnel
- Lorsqu'un destinataire reçoit un message, il envoie un deleteSignal, tous les intermédiaires qui receveront le deletSignal supprimeront le message et transmetteront le deleteSignal. Si l'envoyeur recoit le deleteSignal, il est notifié de la bonne reception du message qu'il a envoyé et ne transmet pas le deleteSignal. 

Reste à implémenter 
-------------------
- Meilleur moyen de visualiser les messages et signaux qui transitent
- Timeout sur les messages et signaux
- Simulation de marche aléatoire des noeuds et apparition plus ou moins aléatoire de nouveaux messages


Partie Crypto
------------------- 
http://stackoverflow.com/questions/10990821/how-to-securely-store-credentials-password-in-android-application
http://stackoverflow.com/questions/8184492/best-way-to-secure-android-app-sensitive-data
http://developer.android.com/training/articles/security-tips.html
http://developer.android.com/reference/java/security/KeyPairGenerator.html
http://developer.android.com/reference/java/security/KeyPair.html
http://developer.android.com/reference/java/security/KeyStore.html
