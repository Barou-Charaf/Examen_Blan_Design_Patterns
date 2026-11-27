# Réseau de neurones — Design Patterns

Projet Maven en Java 17.

Le projet contient le modèle (`Neuron`, `Connection`, `Layer` et ses
trois types), le pattern Strategy (`ActivationFunction`, `Sigmoid`, `Identity`)
et la propagation avant dans `NeuralNetwork`. Le pattern Adapter permet également
d'utiliser une ancienne fonction d'activation.

## Exécuter

Depuis ce dossier :

```bash
mvn test
java -cp target/classes org.example.Main
```

`Main` construit le réseau 3–5–2 de l'énoncé. Les poids sont fixés à 0,5 et
les biais à zéro pour obtenir un exemple reproductible. Les deux sorties sont
identiques car leurs paramètres sont identiques.
La première utilise `Sigmoid`, la seconde `LegacySigmoid` via `ActivationAdapter`.

## Utilisation

Créer le réseau avec une `InputLayer`, puis ajouter les couches cachées et la
`OutputLayer` avec `addLayer(layer, weights)`. Chaque ajout crée toutes les
connexions entre la couche précédente et la nouvelle couche.

La matrice utilise `weights[destination][source]` : une ligne par neurone de
la nouvelle couche, une colonne par neurone de la couche précédente. Les poids
doivent être dans `[0, 1]`, conformément à l'énoncé.

`predict(input)` charge les entrées, puis évalue les couches dans l'ordre.
Chaque neurone calcule `activation(somme(source.output * weight) + bias)`.
La couche d'entrée transmet directement les valeurs reçues. Les couches utilisent
`forward()` sans argument : les valeurs circulent par les connexions.

La fonction d'activation d'un neurone peut être remplacée avec `setActivation(...)`.
Les biais et poids sont modifiables avec `setBias(...)` et `setWeight(...)`.

## Adapter

Le package `legacy` représente une ancienne API : `LegacyActivation` expose
`calculate(double)` et `LegacySigmoid` l'implémente. Cet exemple est fourni pour
l'exercice, aucune bibliothèque ancienne n'étant fournie dans le projet initial.

`ActivationAdapter` implémente l'interface attendue par les neurones,
`ActivationFunction`, et délègue `activate(value)` à `legacy.calculate(value)`.
L'ancienne API ne dépend pas de la nouvelle ; les neurones restent inchangés.

```java
neuron.setActivation(new ActivationAdapter(new LegacySigmoid()));
```

## State et Observer

Le réseau commence en `Construction`. `train(Runnable)` exécute l'opération
fournie en état `Training`, puis passe à `Trained` si elle réussit. `fit` est
un alias de `train`. Une exception remet le réseau en `Construction` et est
propagée : les paramètres éventuellement modifiés ne sont pas restaurés.
`predict` est réservé à `Trained`; `addLayer` est réservé à `Construction`.
Le réentraînement est possible. Ces opérations sont synchrones, pour un usage
sur un seul thread. Les setters de paramètres restent disponibles pour l'apprentissage.

Aucun algorithme d'apprentissage numérique n'est ajouté à cette étape.
L'opération fournie à `Main` affiche explicitement qu'il s'agit d'une démonstration.

`NetworkObserver` reçoit le nom du nouvel état. `Dashboard` affiche les transitions
et conserve leur historique. `addObserver` évite les doublons et `removeObserver`
désabonne un composant. Une erreur d'un observateur est signalée sans interrompre
l'entraînement ni les autres notifications.

## Decorator

`LayerDecorator` enveloppe une couche et partage ses neurones sans les recréer.
`TimingDecorator` mesure chaque appel à `forward()` en nanosecondes, avec accès
à la dernière durée par `getLastDurationNanos()`. Il conserve le résultat et les
exceptions du calcul. La mesure n'est pas un benchmark et varie entre exécutions.

Les décorateurs peuvent être imbriqués et utilisés sur les trois types de couches.
`network.decorateLayer(new TimingDecorator(layer))` remplace une couche existante
par son décorateur, sans reconnecter le réseau. L'opération est refusée pendant
Training. `Main` montre la décoration de la sortie après préparation du réseau.
