# MonnaieParisAPI

Api permettant la consultation de la base des pieces de collection de la Monnaie de Paris

## Services


| Fonctionnalite  | Realise | Url |
| ------------- | ------------- |  ------------- | 
| Permet de recuperer toutes les pieces de la collection | | /pieces |
| Permet de recuperer toutes les pieces en ma possession | | /pieces/my |
| Permet de marquer une piece comme étant en ma possession | | /pieces/my/{idPiece} |
| Permet de recuperer les pieces que je ne possede pas | | /pieces/missing |
| Permet de recuperer la piece manquante la plus proche d'une position | | /pieces/missing/{lattitude}/{longitude} |
| Permet de recuperer les pieces dont la valeur est superieur à 2 euros (trié par ordre decroissant) | | /pieces/my/top |



















