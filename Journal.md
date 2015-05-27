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


Etat de la simulation 
---------------------
- Envoi de messages avec intermédiaires fonctionnel
- Lorsqu'un destinataire reçoit un message, il envoie un deleteSignal, tous les intermédiaires qui receveront le deletSignal supprimeront le message et transmetteront le deleteSignal. Si l'envoyeur recoit le deleteSignal, il est notifié de la bonne reception du message qu'il a envoyé et ne transmet pas le deleteSignal. 

Reste à implémenter 
-------------------
- Meilleur moyen de visualiser les messages et signaux qui transitent
- Timeout sur les messages et signaux
- Simulation de marche aléatoire des noeuds et apparition plus ou moins aléatoire de nouveaux messages


libgpg => cryptage
