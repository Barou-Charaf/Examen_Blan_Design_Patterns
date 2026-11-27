# Réseau de neurones — Design Patterns

Projet Maven en Java 17.

Cette première étape contient le modèle (`Neuron`, `Connection`, `Layer` et ses
trois types), le pattern Strategy (`ActivationFunction`, `Sigmoid`, `Identity`)
et la propagation avant dans `NeuralNetwork`.

## Exécuter

Depuis ce dossier :

```bash
mvn test
java -cp target/classes org.example.Main
```

`Main` construit le réseau 3–5–2 de l'énoncé. Les poids sont fixés à 0,5 et
les biais à zéro pour obtenir un exemple reproductible. Les deux sorties sont
identiques car leurs paramètres sont identiques.

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

L'entraînement et les autres patterns ne sont pas encore implémentés.
`predict` est utilisable à cette étape pour vérifier les calculs ; la restriction
aux réseaux entraînés sera ajoutée avec le pattern State.
