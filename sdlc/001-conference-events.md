# Rattachement des propositions à un événement de conférence

## Context
Aujourd'hui, le CFP ne connaît qu'une notion de proposition de talk : rien ne distingue une soumission destinée à une conférence d'une autre. Dès qu'un deuxième événement doit être organisé (ou qu'un historique de plusieurs éditions doit coexister), il devient impossible de savoir à quelle conférence une proposition appartient, de la présenter aux organisateurs de l'événement concerné, ou de garder une trace propre par édition. Cette fonctionnalité introduit la notion d'événement de conférence comme brique de base, avant tout traitement plus riche (fenêtre de soumission, sélection, programme, etc.).

## Goals
- Permettre de déclarer un événement de conférence dans le CFP.
- Permettre de consulter la liste des événements existants.
- Permettre à un speaker de rattacher sa proposition à un événement au moment de la soumission.

## Non-goals
- Pas de gestion des droits/rôles (organisateur vs speaker) : la création d'événement reste ouverte sans contrôle d'accès à ce stade, une gestion des utilisateurs pourra être introduite plus tard.
- Pas de notion de fenêtre de soumission ouverte/fermée pour un événement : tout événement créé est considéré disponible pour recevoir des propositions.
- Pas de modification ou de suppression d'un événement une fois créé.
- Pas d'association a posteriori d'une proposition existante à un événement par un organisateur (le rattachement se fait uniquement à la soumission).
- Pas d'attributs complémentaires sur l'événement (dates, lieu, description) dans cette itération : un nom suffit.

## Target users & stakeholders
- **Organisateurs de conférence** : créent les événements pour lesquels ils souhaitent recevoir des propositions.
- **Speakers** : consultent la liste des événements pour choisir celui auquel ils souhaitent proposer un talk, et rattachent leur soumission au bon événement.

## Scenarios
- **Créer un événement** : Étant donné qu'un organisateur souhaite ouvrir un nouvel événement, quand il fournit un nom d'événement, alors l'événement est créé et devient disponible pour recevoir des propositions.
- **Lister les événements** : Étant donné qu'un ou plusieurs événements existent, quand un utilisateur consulte la liste des événements, alors il voit tous les événements créés.
- **Soumettre une proposition liée à un événement** : Étant donné qu'un speaker soumet une proposition, quand il sélectionne un événement existant, alors la proposition est enregistrée en étant rattachée à cet événement.
- **Soumettre une proposition sans événement** : Étant donné que le lien à un événement reste optionnel dans cette itération, quand un speaker soumet une proposition sans sélectionner d'événement, alors la proposition est acceptée sans rattachement (comme c'est le cas aujourd'hui).

## Business rules & constraints
- Un nom d'événement est la seule information obligatoire pour créer un événement.
- Une proposition ne peut être rattachée qu'à un événement existant au moment de sa soumission (pas de création d'événement à la volée depuis le formulaire de proposition).
- Une proposition reste valide qu'elle soit ou non rattachée à un événement.

## Success criteria
- Un organisateur peut créer un événement en ne renseignant qu'un nom.
- La liste des événements créés est consultable et à jour immédiatement après création.
- Un speaker peut, au moment de soumettre sa proposition, voir la liste des événements disponibles et en choisir un ; la proposition soumise porte alors la trace de cet événement.
- Les propositions soumises sans événement continuent de fonctionner comme avant, sans erreur ni régression.

## Open questions
- Deux événements peuvent-ils porter exactement le même nom, ou le nom doit-il être unique ?
- Dans quel ordre la liste des événements doit-elle être présentée (ordre de création, alphabétique, autre) ?
- Le rattachement a posteriori (associer une proposition déjà soumise à un événement) sera-t-il nécessaire dans une itération suivante, notamment pour traiter les propositions déjà existantes en base ?
