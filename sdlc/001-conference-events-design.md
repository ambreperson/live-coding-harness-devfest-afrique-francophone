# Rattachement des propositions à un événement de conférence — Design

Spec: [sdlc/001-conference-events.md](001-conference-events.md)

## Technical approach

Cette fonctionnalité crée un **nouveau domaine** `conf.live.cfp.event`, avec exactement le
même gabarit hexagonal que `proposal` (`domain/`, `application/port/{in,out}`,
`application/service`, `adapter/{in/web,out/persistence}`, `config`) : il n'y a aucune
raison d'attacher `Event` au domaine `proposal`, un événement n'est pas un concept de
proposition et devra porter sa propre évolution (statuts, dates, etc.) plus tard.

Le lien entre les deux domaines se fait au niveau de `proposal` :

- `Proposal` (domaine) gagne un champ `eventId` optionnel (`Optional<EventId>` ou équivalent
  nullable en interne, aucune classe partagée entre domaines — `proposal` connaît juste un
  identifiant, pas le modèle `Event`).
- `CreateProposalCommand`, la requête HTTP et la persistance de `proposal` gagnent un champ
  `eventId` optionnel en cohérence.
- La règle métier « une proposition ne peut référencer qu'un événement existant » est
  vérifiée par `CreateProposalService`, qui a besoin d'une information venant du domaine
  `event` sans dépendre de son implémentation. C'est le seul vrai point de couplage entre les
  deux domaines : il est résolu par un nouveau out-port côté `proposal`
  (`EventExistsPort`), dont l'unique implémentation adapte le in-port de query du domaine
  `event` (`FindEventUseCase`). Ainsi `proposal.application` ne dépend que d'une interface
  qu'il définit lui-même ; `event` n'a besoin de rien savoir de `proposal`.

Rien dans `event.domain` ou `event.application` n'importe Spring/JPA, comme pour `proposal`.

## Capability breakdown

| Besoin métier (spec) | Domaine | Capacité technique |
|---|---|---|
| Créer un événement (nom obligatoire) | `event` | `CreateEventUseCase` (in-port) + `CreateEventCommand`, implémenté par `CreateEventService`, s'appuyant sur `SaveEventPort` (out-port) |
| Lister les événements | `event` | `ListEventsUseCase` (in-port), implémenté par `ListEventsService`, s'appuyant sur `ListEventsPort` (out-port) |
| Vérifier qu'un événement référencé existe (utilisé par `proposal`) | `event` puis `proposal` | `FindEventUseCase` (in-port `event`, méthode `findById`), consommé par un nouvel out-port `EventExistsPort` côté `proposal`, dont l'adaptateur délègue à `FindEventUseCase` |
| Soumettre une proposition avec un `eventId` optionnel | `proposal` | Extension de `Proposal` (domaine), `CreateProposalCommand`, `CreateProposalService` (validation via `EventExistsPort`), web + persistence adapters de `proposal` |
| Rejeter une proposition référençant un événement inconnu | `proposal` | Nouvelle exception domaine `UnknownEventException` (ou équivalent), levée par `CreateProposalService`, mappée à 400 par `ProposalExceptionHandler` |
| Exposer la création/liste d'événements en HTTP | `event` | `EventController` (`POST /api/events`, `GET /api/events`), DTOs `CreateEventRequest`/`EventResponse`, `EventExceptionHandler` |
| Persister les événements | `event` | `EventJpaEntity`, `SpringDataEventRepository`, `EventPersistenceAdapter` (implémente `SaveEventPort` + `FindEventPort`) |

## Implementation phases

### Phase 0 — Contrats (bloquant, séquentiel)

**Goal** — Figer, sans les implémenter, les signatures qui permettent à tout le reste de
travailler en parallèle :
- `event.domain.model.Event` (id, nom) et `EventId` (value object UUID, même forme que
  `ProposalId`), `InvalidEventException`.
- `event.application.port.in` : `CreateEventUseCase`/`CreateEventCommand`,
  `ListEventsUseCase`, `FindEventUseCase` (méthode `Optional<Event> findById(EventId)`).
- `event.application.port.out` : `SaveEventPort` (méthode `save`), `ListEventsPort`
  (méthode `findAll`), `FindEventPort` (méthode `findById`).
- `proposal.application.port.out.EventExistsPort` (méthode `boolean existsById(String
  eventId)`), défini côté `proposal`.
- Signature étendue de `Proposal.submit(...)`/`reconstitute(...)`, `CreateProposalCommand`
  et `UnknownEventException` (côté `proposal.domain.exception`).

**Depends on** — rien.
**Can run in parallel with** — rien (tout le reste dépend de ces contrats).
**Verification** — compilation ; pas de test de comportement à ce stade, seuls des
interfaces/records/squelettes.

### Phase 1a — `event` : couche application

**Goal** — `CreateEventService` et `ListEventsService` (ou un seul `EventService`
implémentant les deux in-ports), testés avec un `SaveEventPort`/`ListEventsPort` mockés
(Mockito) et un `Clock` fixe si un horodatage de création est retenu.
**Depends on** — Phase 0 uniquement.
**Can run in parallel with** — 1b, 1c, 1d, 1e (tous ne dépendent que des contrats de Phase 0).
**Verification** — tests JUnit + Mockito sur le service, plus tests JUnit + AssertJ sans
Spring sur `Event`/`EventId` (invariants : nom non vide) — cf. `hexagonal-port-adapter-architecture`
et `tdd-red-green-refactor` (red → green sur chaque invariant avant d'écrire le service).

### Phase 1b — `event` : adaptateur de persistance

**Goal** — `EventJpaEntity`, `SpringDataEventRepository`, `EventPersistenceAdapter`
implémentant `SaveEventPort` et `FindEventPort`, avec mapping explicite entité ⇄ domaine.
**Depends on** — Phase 0 uniquement (le contrat des out-ports).
**Can run in parallel with** — 1a, 1c, 1d, 1e.
**Verification** — `@DataJpaTest` + `@Import(EventPersistenceAdapter.class)` sur H2, comme
`ProposalPersistenceAdapterTest`.

### Phase 1c — `event` : adaptateur web

**Goal** — `EventController` (`POST /api/events`, `GET /api/events`),
`CreateEventRequest`/`EventResponse`, `EventExceptionHandler` mappant
`InvalidEventException` à 400.
**Depends on** — Phase 0 uniquement (les in-ports sont mockés en test).
**Can run in parallel with** — 1a, 1b, 1d, 1e.
**Verification** — `@WebMvcTest(EventController.class)` + `MockMvc`, in-ports mockés via
`@MockitoBean`, comme `ProposalControllerTest`.

### Phase 1d — `proposal` : extension du domaine et du service applicatif

**Goal** — `Proposal` accepte un `eventId` optionnel (constructeur + `submit`/
`reconstitute`) ; `CreateProposalCommand` gagne `eventId` ; `CreateProposalService` valide
l'existence de l'événement via `EventExistsPort` (mocké en test) et lève
`UnknownEventException` si l'`eventId` fourni ne correspond à aucun événement.
**Depends on** — Phase 0 uniquement (le contrat `EventExistsPort`, pas son implémentation).
**Can run in parallel with** — 1a, 1b, 1c, 1e — aucune de ces phases ne partage de fichier
avec celle-ci, seulement le contrat figé en Phase 0.
**Verification** — tests domaine (`ProposalTest` étendu) + `CreateProposalServiceTest`
étendu avec un `EventExistsPort` mocké (cas événement présent, absent, et pas d'événement
fourni).

### Phase 1e — `proposal` : adaptateurs web et persistance

**Goal** — `CreateProposalRequest`/`ProposalResponse` gagnent `eventId` (optionnel) ;
`ProposalJpaEntity` gagne une colonne `event_id` nullable ; `ProposalPersistenceAdapter`
mappe ce champ ; `ProposalExceptionHandler` mappe `UnknownEventException` à 400.
**Depends on** — Phase 0 uniquement.
**Can run in parallel with** — 1a, 1b, 1c, 1d.
**Verification** — `ProposalControllerTest` et `ProposalPersistenceAdapterTest` étendus pour
couvrir le champ `eventId` présent/absent.

### Phase 2 — Câblage et bout-en-bout (bloquant, séquentiel)

**Goal** —
- `EventConfiguration` (`@Configuration`) exposant les beans `CreateEventUseCase`,
  `ListEventsUseCase`, `FindEventUseCase`.
- Nouvel adaptateur `EventExistsAdapter implements EventExistsPort` (côté `proposal`,
  package `proposal.adapter.out.persistence` ou `proposal.adapter.out.event` selon lisibilité),
  construit avec le `FindEventUseCase` du domaine `event` et délégant `existsById` à
  `findById(...).isPresent()`.
- `ProposalConfiguration` mise à jour pour injecter le nouveau port dans
  `CreateProposalService`.
**Depends on** — toutes les phases 1a–1e.
**Can run in parallel with** — rien : c'est le seul point où tous les morceaux doivent
exister simultanément pour être assemblés et prouvés ensemble.
**Verification** — nouveau test bout-en-bout `EventProposalIntegrationTest`
(`@SpringBootTest` + `@AutoConfigureMockMvc`) : créer un événement, le retrouver via
`GET /api/events`, soumettre une proposition avec cet `eventId` (201, proposition liée),
soumettre une proposition avec un `eventId` inexistant (400), soumettre une proposition sans
`eventId` (201, comportement inchangé — non-régression explicitement demandée par la spec).

## Risks & open technical questions

- **Nom d'événement dupliqué** : la spec laisse la question ouverte (unicité ou non). Ce
  design ne l'impose pas — `SaveEventPort.save` n'a pas de contrainte d'unicité en base. Si
  la réponse est « unique », cela ajoute une règle de validation dans `CreateEventService`
  (vérifier l'absence via un `FindEventPort` par nom) — changement localisé à Phase 1a/1b, ne
  remet pas en cause le découpage.
- **Ordre de la liste des événements** : non tranché par la spec. Ce design part sur
  `findAll()` renvoyant l'ordre naturel de la base (création) ; à ajuster côté
  `ListEventsPort`/requête JPA si un tri explicite est demandé plus tard, sans impact sur les
  autres phases.
- **Format de l'`eventId` côté `proposal`** : `CreateProposalCommand.eventId` est traité
  comme un `String` optionnel (potentiellement non parseable en UUID). `CreateProposalService`
  doit décider si un `eventId` malformé est une erreur 400 (`UnknownEventException` ou une
  nouvelle `InvalidProposalException`) plutôt qu'une `IllegalArgumentException` non gérée —
  point à trancher en Phase 1d, sans dépendance externe.
- **Couplage `proposal` → `event` via un in-port** : l'implémentation retenue pour
  `EventExistsPort` appelle directement le `FindEventUseCase` du domaine `event` (in-process,
  pas de HTTP interne). C'est un choix pragmatique pour un monolithe modulaire ; si `event`
  devient un jour un service séparé, seul `EventExistsAdapter` change.
