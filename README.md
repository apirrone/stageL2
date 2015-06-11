# stageL2

liste des applications concernant le routage de messages sans réseau internet ou téléphonique :
- Firechat, fonctionnant par peer-to-peer pour envoyer des messages non-chiffrés.
- FluidNexus, projet qui semble avoir été abandonné en 2011, ne marche pas sur Android, du moins sur le téléphone sur lequel nous avons testé. Malgré son dysfonctionnement, le concept de cette application était de pouvoir transmettre des messages via bluetooth dans des zones à couverture réduite donc.
- Android beam NFC, ne marche pas non plus, mais  permet soi-disant d'envoyer des messages via NFC.
- Text SMS, qui permet d'envoyer des messages chiffrés mais passe par le réseau internet.
- Starlogo, application windows/mac/linux qui permet de simuler des réseaux décentralisés avec des tortues. Nous n'avons pour l'instant pas bien compris son fonctionnement. 
(URL : http://www3.nd.edu/~agent/Papers/Research_Kennedy02.pdf && http://education.mit.edu/cgi-bin/StarLogoForm.pl )
- TinCan :Crée un réseau WiFi de nom TinCan_(randomID), l'application détecte les réseaux et s'y connecte, elle échange alors tous les messages qu'ils n'ont pas en commun. Donc messages publics.
- un fichier pdf qui traite de la transmission de messages dans des réseaux "déconnectés" ad-hoc : (URL : http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.130.6204&rep=rep1&type=pdf ) 
- Serval : Un projet open source et libre (GPL) de communication par messages ou audio utilisant un réseau de points WiFi crée par les utilisateurs de l'applicaiton. Elle utilise un system "Store and forward", comme ce que nous voulons faire (Fonctionne temporellement avec graphe non connexe au temps T). L'application utilise un cryptage par courbe elyptique (très sécurisé) . Son objectif principal est de permettre la communication dans les pays pauvres ou en cas de catastrophe.
  Serval utilise un protocole appelé Rhizome, qui fonctionne de la manière suivante : Les "unités de transport" sont des paquets de données associés à un manifest qui contient des meta données (nom du paquet, SHA512 hash pour verifier l'intégrité du message, données sur envoyeur et recepteur, clés publiques/privées du bundle (?) ) . L'identification des envoyeurs et recepteurs se fait avec leurs clés publiques (comme nous).
Lors de la réception du message par le destinataire, le récepteur modifie le contenu du manifest pour indiquer que le message a été reçu, ce qui permet la suppression progressive du message (pas compris comment).
Serval utilise les numéros de téléphones des utilisateurs pour les identifier, nous pouvons ainsi envoyer des messages via serval a tous nos contacts (que nous avons déjà) qui possedent l'application.
