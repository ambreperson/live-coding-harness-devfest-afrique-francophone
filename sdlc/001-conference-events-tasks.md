# Rattachement des propositions à un événement de conférence — Tasks

Spec: [sdlc/001-conference-events.md](001-conference-events.md) · Design: [sdlc/001-conference-events-design.md](001-conference-events-design.md)

### Phase 0: Contrats (domaine `event` + extension `proposal`)
_Depends on: none. Blocks all other phases (defines the shared contracts)._

- [x] Créer les packages `conf.live.cfp.event.domain.model`, `conf.live.cfp.event.domain.exception`, `conf.live.cfp.event.application.port.in`, `conf.live.cfp.event.application.port.out`, `conf.live.cfp.event.application.service`, `conf.live.cfp.event.adapter.in.web`, `conf.live.cfp.event.adapter.out.persistence`, `conf.live.cfp.event.config`
- [x] Définir `EventId` (record `UUID value`, méthodes statiques `newId()` et `fromString(String)`, `toString()` délégué à `value`) dans `event/domain/model/EventId.java`, sur le modèle de `ProposalId`
- [x] Définir `InvalidEventException` (RuntimeException à message) dans `event/domain/exception/InvalidEventException.java`, sur le modèle de `InvalidProposalException`
- [x] Définir le squelette de `Event` (classe finale, champs `EventId id`, `String name`, sans logique de validation encore) dans `event/domain/model/Event.java`
- [x] Définir `CreateEventCommand` (record `String name`) dans `event/application/port/in/CreateEventCommand.java`
- [x] Définir l'interface `CreateEventUseCase` (méthode `Event createEvent(CreateEventCommand command)`) dans `event/application/port/in/CreateEventUseCase.java`
- [x] Définir l'interface `ListEventsUseCase` (méthode `List<Event> listEvents()`) dans `event/application/port/in/ListEventsUseCase.java`
- [x] Définir l'interface `FindEventUseCase` (méthode `Optional<Event> findById(String eventId)`) dans `event/application/port/in/FindEventUseCase.java`
- [x] Définir l'interface `SaveEventPort` (méthode `Event save(Event event)`) dans `event/application/port/out/SaveEventPort.java`
- [x] Définir l'interface `ListEventsPort` (méthode `List<Event> findAll()`) dans `event/application/port/out/ListEventsPort.java`
- [x] Définir l'interface `FindEventPort` (méthode `Optional<Event> findById(EventId id)`) dans `event/application/port/out/FindEventPort.java`
- [x] Définir `UnknownEventException` (RuntimeException à message) dans `conf/live/cfp/proposal/domain/exception/UnknownEventException.java`
- [x] Définir l'interface `EventExistsPort` (méthode `boolean existsById(String eventId)`) dans `conf/live/cfp/proposal/application/port/out/EventExistsPort.java`
- [x] Étendre la signature de `CreateProposalCommand` en ajoutant le champ `String eventId` (nullable) dans `proposal/application/port/in/CreateProposalCommand.java`
- [x] Vérifier que le projet compile après ces ajouts (`./mvnw -o compile`)

### Phase 1a: `event` — couche application
_Depends on: Phase 0. Can run in parallel with: Phase 1b, 1c, 1d, 1e (toutes ne dépendent que des contrats de Phase 0)._

- [ ] RED: `EventTest#should_create_an_event_with_a_name` — assert `Event.create("DevFest Afrique Francophone")` a un `id()` non nul et `name()` égal à `"DevFest Afrique Francophone"`
- [ ] GREEN: implémenter la factory statique `Event.create(String name)` et le constructeur privé dans `Event.java`
- [ ] RED: `EventTest#should_reject_blank_name` (paramétré `@NullAndEmptySource` + `@ValueSource(strings = {"   "})`) — assert `Event.create(blankName)` lève `InvalidEventException` avec un message contenant `"name"`
- [ ] GREEN: ajouter la validation du nom dans le constructeur de `Event.java`
- [ ] RED: `EventTest#should_reconstitute_an_event_with_an_existing_id` — assert `Event.reconstitute(EventId.newId(), "Name")` restitue le même id et nom sans changement de comportement
- [ ] GREEN: implémenter `Event.reconstitute(EventId id, String name)` dans `Event.java`
- [ ] RED: `CreateEventServiceTest#should_create_and_persist_a_valid_event` (JUnit + Mockito, `@Mock SaveEventPort`) — assert que `CreateEventService.createEvent(new CreateEventCommand("Name"))` retourne l'événement créé et que `saveEventPort.save(...)` a été appelé avec un `Event` de même nom
- [ ] GREEN: implémenter `CreateEventService implements CreateEventUseCase` dans `event/application/service/CreateEventService.java`
- [ ] RED: `CreateEventServiceTest#should_reject_an_invalid_command_without_calling_the_port` — assert que `createEvent(new CreateEventCommand(""))` lève `InvalidEventException` et que `verifyNoInteractions(saveEventPort)`
- [ ] GREEN: s'assurer que `CreateEventService` laisse `Event.create` remonter l'exception avant tout appel au port (déjà garanti si l'ordre d'appel est correct ; ajuster si besoin)
- [ ] RED: `ListEventsServiceTest#should_return_all_events` (JUnit + Mockito, `@Mock ListEventsPort`) — assert que `ListEventsService.listEvents()` retourne exactement la liste renvoyée par `listEventsPort.findAll()`
- [ ] GREEN: implémenter `ListEventsService implements ListEventsUseCase` dans `event/application/service/ListEventsService.java`
- [ ] RED: `FindEventServiceTest#should_return_the_event_when_id_exists` (JUnit + Mockito, `@Mock FindEventPort`) — assert que `FindEventService.findById(validUuidString)` retourne `Optional` contenant l'événement renvoyé par `findEventPort.findById(EventId.fromString(validUuidString))`
- [ ] GREEN: implémenter `FindEventService implements FindEventUseCase` dans `event/application/service/FindEventService.java`
- [ ] RED: `FindEventServiceTest#should_return_empty_when_id_is_malformed` — assert que `FindEventService.findById("not-a-uuid")` retourne `Optional.empty()` sans appeler `findEventPort` (`verifyNoInteractions`)
- [ ] GREEN: faire capturer par `FindEventService` l'`IllegalArgumentException` de `EventId.fromString` et retourner `Optional.empty()` dans ce cas

### Phase 1b: `event` — adaptateur de persistance
_Depends on: Phase 0. Can run in parallel with: Phase 1a, 1c, 1d, 1e._

- [ ] Définir `EventJpaEntity` (`@Entity @Table(name = "events")`, colonnes `id` (`@Id`), `name` (`@Column(nullable = false)`), constructeur protégé vide + constructeur public, getters) dans `event/adapter/out/persistence/EventJpaEntity.java`, sur le modèle de `ProposalJpaEntity`
- [ ] Définir `SpringDataEventRepository extends JpaRepository<EventJpaEntity, String>` dans `event/adapter/out/persistence/SpringDataEventRepository.java`
- [ ] RED: `EventPersistenceAdapterTest#should_save_an_event_and_make_it_retrievable` (`@DataJpaTest` + `@Import(EventPersistenceAdapter.class)`, H2) — assert que `adapter.save(event)` persiste l'entité et que `jpaRepository.findById(event.id().toString())` la retrouve avec le bon nom
- [ ] GREEN: implémenter `EventPersistenceAdapter implements SaveEventPort` (méthode `save`, mapping `toEntity`/`toDomain` privés) dans `event/adapter/out/persistence/EventPersistenceAdapter.java`
- [ ] RED: `EventPersistenceAdapterTest#should_find_all_saved_events` — assert que `adapter.findAll()` (après deux `save`) retourne les deux événements
- [ ] GREEN: faire implémenter `ListEventsPort` par `EventPersistenceAdapter` (méthode `findAll`, mapping des entités trouvées via `jpaRepository.findAll()`)
- [ ] RED: `EventPersistenceAdapterTest#should_find_an_event_by_id` — assert que `adapter.findById(event.id())` retourne `Optional` contenant l'événement sauvegardé
- [ ] GREEN: faire implémenter `FindEventPort` par `EventPersistenceAdapter` (méthode `findById(EventId id)`, délègue à `jpaRepository.findById(id.toString())`)
- [ ] RED: `EventPersistenceAdapterTest#should_return_empty_when_event_id_is_unknown` — assert que `adapter.findById(EventId.newId())` (jamais sauvegardé) retourne `Optional.empty()`
- [ ] GREEN: vérifier/ajuster le mapping `findById` pour ce cas (déjà couvert si `Optional` est propagé correctement depuis `jpaRepository.findById`)

### Phase 1c: `event` — adaptateur web
_Depends on: Phase 0. Can run in parallel with: Phase 1a, 1b, 1d, 1e._

- [ ] Définir `CreateEventRequest` (record `@NotBlank String name`) dans `event/adapter/in/web/CreateEventRequest.java`, sur le modèle de `CreateProposalRequest`
- [ ] Définir `EventResponse` (record `String id, String name`, méthode statique `from(Event event)`) dans `event/adapter/in/web/EventResponse.java`
- [ ] RED: `EventControllerTest#should_return_201_with_the_created_event` (`@WebMvcTest(EventController.class)` + `@MockitoBean CreateEventUseCase`) — assert `POST /api/events` avec un nom valide retourne 201, `$.id` et `$.name` corrects
- [ ] GREEN: implémenter `EventController` (`@RestController @RequestMapping("/api/events")`, méthode `POST` déléguant à `CreateEventUseCase`) dans `event/adapter/in/web/EventController.java`
- [ ] RED: `EventControllerTest#should_return_400_when_name_is_blank` — assert `POST /api/events` avec un nom vide retourne 400 (validation `@Valid`/`@NotBlank`)
- [ ] GREEN: ajouter `@Valid @RequestBody` sur la méthode du contrôleur (si pas déjà fait à l'étape précédente)
- [ ] RED: `EventControllerTest#should_return_400_when_use_case_rejects_the_event` — assert que si `createEventUseCase.createEvent(...)` lève `InvalidEventException`, la réponse est 400
- [ ] GREEN: implémenter `EventExceptionHandler` (`@RestControllerAdvice(assignableTypes = EventController.class)`, mappe `InvalidEventException` à 400) dans `event/adapter/in/web/EventExceptionHandler.java`
- [ ] RED: `EventControllerTest#should_return_200_with_the_list_of_events` — assert `GET /api/events` retourne 200 et la liste JSON des événements renvoyés par `listEventsUseCase.listEvents()`
- [ ] GREEN: ajouter la méthode `GET` à `EventController` déléguant à `ListEventsUseCase` et mappant chaque `Event` via `EventResponse.from(...)`

### Phase 1d: `proposal` — extension du domaine et du service applicatif
_Depends on: Phase 0. Can run in parallel with: Phase 1a, 1b, 1c, 1e._

- [ ] RED: `ProposalTest#should_submit_a_proposal_linked_to_an_event` — assert que `Proposal.submit(title, description, speaker, NOW, "11111111-1111-1111-1111-111111111111")` a `eventId()` égal à cette valeur
- [ ] GREEN: ajouter le paramètre `String eventId` (nullable) à `Proposal.submit(...)`, au constructeur privé et à `Proposal.reconstitute(...)`, et l'accesseur `eventId()` dans `Proposal.java`
- [ ] RED: `ProposalTest#should_submit_a_proposal_without_an_event` — assert que `Proposal.submit(title, description, speaker, NOW, null)` a `eventId()` égal à `null` (aucune exception levée)
- [ ] GREEN: vérifier que le constructeur de `Proposal` n'exige pas `eventId` (pas de `requireNonNull` sur ce champ)
- [ ] RED: `CreateProposalServiceTest#should_submit_and_persist_a_proposal_linked_to_an_existing_event` (ajout `@Mock EventExistsPort`) — assert que si `eventExistsPort.existsById("11111111-1111-1111-1111-111111111111")` retourne `true`, `service.createProposal(command avec eventId)` retourne une proposition avec cet `eventId` et que `saveProposalPort.save(...)` est appelé
- [ ] GREEN: injecter `EventExistsPort` dans le constructeur de `CreateProposalService` et propager `command.eventId()` à `Proposal.submit(...)` après vérification
- [ ] RED: `CreateProposalServiceTest#should_reject_a_proposal_referencing_an_unknown_event` — assert que si `eventExistsPort.existsById(...)` retourne `false`, `service.createProposal(...)` lève `UnknownEventException` et que `verifyNoInteractions(saveProposalPort)`
- [ ] GREEN: ajouter la vérification `eventExistsPort.existsById(...)` dans `CreateProposalService.createProposal(...)`, levant `UnknownEventException` si absente, avant tout appel à `saveProposalPort`
- [ ] RED: `CreateProposalServiceTest#should_submit_a_proposal_without_calling_event_exists_port_when_no_event_given` — assert que si `command.eventId()` est `null`, `service.createProposal(...)` réussit sans appeler `eventExistsPort` (`verifyNoInteractions(eventExistsPort)`)
- [ ] GREEN: entourer l'appel à `eventExistsPort.existsById(...)` d'une garde `if (command.eventId() != null)` dans `CreateProposalService`

### Phase 1e: `proposal` — adaptateurs web et persistance
_Depends on: Phase 0. Can run in parallel with: Phase 1a, 1b, 1c, 1d._

- [ ] Ajouter le champ `String eventId` (sans annotation de validation, optionnel) à `CreateProposalRequest` dans `proposal/adapter/in/web/CreateProposalRequest.java`
- [ ] Ajouter le champ `String eventId` à `ProposalResponse` et le renseigner dans `ProposalResponse.from(Proposal proposal)` dans `proposal/adapter/in/web/ProposalResponse.java`
- [ ] Mettre à jour `ProposalController.createProposal(...)` pour passer `request.eventId()` au `CreateProposalCommand` construit dans `proposal/adapter/in/web/ProposalController.java`
- [ ] RED: `ProposalControllerTest#should_return_201_with_the_event_id_when_proposal_is_linked_to_an_event` — assert que si le use case mocké retourne une `Proposal` avec un `eventId`, `$.eventId` apparaît dans la réponse JSON
- [ ] GREEN: vérifier que `ProposalResponse.from(...)` propage bien `eventId` (couvert par la tâche précédente si l'ordre est respecté)
- [ ] RED: `ProposalControllerTest#should_return_400_when_use_case_rejects_an_unknown_event` — assert que si `createProposalUseCase.createProposal(...)` lève `UnknownEventException`, la réponse est 400
- [ ] GREEN: étendre `ProposalExceptionHandler` pour mapper `UnknownEventException` à 400 dans `proposal/adapter/in/web/ProposalExceptionHandler.java`
- [ ] Ajouter la colonne `event_id` (`@Column(name = "event_id", nullable = true)`) à `ProposalJpaEntity`, son constructeur et son getter, dans `proposal/adapter/out/persistence/ProposalJpaEntity.java`
- [ ] RED: `ProposalPersistenceAdapterTest#should_save_a_proposal_linked_to_an_event_and_make_it_retrievable` — assert qu'une `Proposal` avec `eventId` non nul, une fois sauvegardée puis relue via `jpaRepository`, conserve cet `eventId`
- [ ] GREEN: mettre à jour `toEntity`/`toDomain` dans `ProposalPersistenceAdapter.java` pour mapper `eventId`
- [ ] RED: `ProposalPersistenceAdapterTest#should_save_a_proposal_without_an_event` — assert qu'une `Proposal` avec `eventId` nul est sauvegardée et relue sans erreur, `eventId()` restant `null`
- [ ] GREEN: vérifier que le mapping `eventId` gère le cas nul sans lever d'exception (déjà couvert si la tâche précédente est correcte)

### Phase 2: Câblage et bout-en-bout
_Depends on: Phase 1a, 1b, 1c, 1d, 1e (toutes). Can run in parallel with: none — c'est le seul point où toutes les pièces doivent exister ensemble._

- [ ] Créer `EventConfiguration` (`@Configuration`) exposant les beans `CreateEventUseCase` (`CreateEventService` + `SaveEventPort`), `ListEventsUseCase` (`ListEventsService` + `ListEventsPort`) et `FindEventUseCase` (`FindEventService` + `FindEventPort`) dans `event/config/EventConfiguration.java`
- [ ] Créer le package `conf.live.cfp.proposal.adapter.out.event`
- [ ] Implémenter `EventExistsAdapter implements EventExistsPort` (constructeur prenant un `FindEventUseCase`, méthode `existsById(String eventId)` déléguant à `findEventUseCase.findById(eventId).isPresent()`) dans `proposal/adapter/out/event/EventExistsAdapter.java`, avec `@Component`
- [ ] Mettre à jour `ProposalConfiguration.createProposalUseCase(...)` pour injecter `EventExistsPort` en plus de `SaveProposalPort` et `Clock` dans `proposal/config/ProposalConfiguration.java`
- [ ] RED: `EventProposalIntegrationTest#should_create_and_list_events_through_the_http_api` (`@SpringBootTest` + `@AutoConfigureMockMvc`) — assert que `POST /api/events` puis `GET /api/events` retourne l'événement créé
- [ ] GREEN: corriger le câblage jusqu'à ce que ce test passe (aucune nouvelle classe de production attendue si les phases précédentes sont complètes)
- [ ] RED: `EventProposalIntegrationTest#should_submit_a_proposal_linked_to_an_existing_event` — assert que soumettre une proposition avec l'`eventId` d'un événement préalablement créé retourne 201 et que la proposition persistée porte cet `eventId`
- [ ] GREEN: corriger le câblage jusqu'à ce que ce test passe
- [ ] RED: `EventProposalIntegrationTest#should_reject_a_proposal_referencing_an_unknown_event` — assert que soumettre une proposition avec un `eventId` inexistant (UUID valide mais non créé) retourne 400
- [ ] GREEN: corriger le câblage jusqu'à ce que ce test passe
- [ ] RED: `EventProposalIntegrationTest#should_submit_a_proposal_without_an_event_as_before` — assert que soumettre une proposition sans `eventId` retourne 201, comme avant cette fonctionnalité (non-régression)
- [ ] GREEN: corriger le câblage jusqu'à ce que ce test passe
- [ ] Exécuter la suite complète (`./mvnw -o test`) et vérifier que `HexagonalArchitectureTest` ne signale aucune violation pour le nouveau domaine `event`
