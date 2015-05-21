#process 

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
  - Peut vite devenir lourd puisque chaque téléphone possède potentiellemet tous les messages en cours de transfert
  - Obligation de passer par internet pour envoyer à tous les intermédiaires l'accusé de reception d'un message pour qu'ils l'éffacent de leur base de données
  - Nécéssité (?) de centraliser la génération d'identifiants pour les messages 
