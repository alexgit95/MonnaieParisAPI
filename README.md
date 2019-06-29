# MonnaieParisAPI

Api permettant la consultation de la base des pieces de collection de la Monnaie de Paris

## Services


| Fonctionnalite  | Realise | Url |
| ------------- | ------------- |  ------------- | 
| Permet de recuperer toutes les pieces de la collection | | /pieces |
| Permet de recuperer toutes les pieces en ma possession | | /pieces/my |
| Permet de marquer une piece comme Ã©tant en ma possession | | /pieces/my/{idPiece} |
| Permet de recuperer les pieces que je ne possede pas | | /pieces/missing |
| Permet de recuperer la piece manquante la plus proche d'une position | | /pieces/missing/{lattitude}/{longitude} |
| Permet de recuperer les pieces dont la valeur est superieur à 2 euros (triés par ordre decroissant) | | /pieces/my/top |


## Ressources

https://thierry-leriche-dessirier.developpez.com/tutoriels/java/vertx/creer-lancer-tester-verticle/

https://vertx.io/docs/vertx-mongo-client/java/

## Comment lancer l'application ?

```

git clone https://github.com/alexgit95/MonnaieParisAPI.git

cd MonnaieParisAPI

```

Modifier le fichier props.properties (exemple avec mongodb atlas) :

```

host1=cluster0-shard-00-00-XXX.mongodb.net
host2=cluster0-shard-00-01-XXX.mongodb.net
host3=cluster0-shard-00-02-XXX.mongodb.net
db_name=DBNAME
username=(utilisateur configuré sur votre mongo)
password=(mot de passe configure par l'utilisateur)

```

Puis faire :

```

docker build -t monnaieparis .

```

Ou sur un raspberry

```

docker build -t monnaieparis -f Dockerfile-rasp  .

```

Puis pour finir :

```

docker run -p 8181:8181 monnaieparis

```

















