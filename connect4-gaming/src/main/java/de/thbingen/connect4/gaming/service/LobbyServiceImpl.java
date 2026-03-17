package de.thbingen.connect4.gaming.service;

import de.thbingen.connect4.common.model.dto.CreateLobbyResponse;
import de.thbingen.connect4.common.model.enums.LobbyActorRole;
import de.thbingen.connect4.common.model.enums.LobbyState;
import de.thbingen.connect4.gaming.evt.LobbyUpdateEvent;
import de.thbingen.connect4.gaming.exception.*;
import de.thbingen.connect4.gaming.model.entity.Lobby;
import de.thbingen.connect4.gaming.model.entity.LobbyActor;
import de.thbingen.connect4.gaming.model.enums.LobbyUpdateType;
import de.thbingen.connect4.gaming.ports.in.BotService;
import de.thbingen.connect4.gaming.ports.in.GameService;
import de.thbingen.connect4.gaming.ports.in.LobbyDTOMapper;
import de.thbingen.connect4.gaming.ports.in.LobbyService;
import de.thbingen.connect4.gaming.ports.out.LobbyRepository;
import de.thbingen.connect4.gaming.ports.out.LobbyWordsRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LobbyServiceImpl implements LobbyService {
    private static final int MAX_LOBBY_NAME_TRIES = 10;
    private static final int MAX_LOBBY_SIZE = 2;

    private final LobbyRepository lobbyRepository;
    private final ApplicationEventPublisher publisher;
    private final LobbyDTOMapper lobbyMapper;
    private final BotService botService;
    private final GameService gameService;

    @Override
    public CreateLobbyResponse createLobby() throws LobbyNameGeneratedException {
        return new CreateLobbyResponse(lobbyRepository.create(new Lobby(generateLobbyName())).getId());
    }

    @Override
    public void joinLobby(String lobbyId, String username, LobbyActorRole role) throws LobbyNotFoundException, LobbyClosedException, LobbyFullException {
        LobbyActor actor = new LobbyActor(username, role);
        Lobby lobby = lobbyRepository.find(lobbyId).orElseThrow(LobbyNotFoundException::new);

        Set<LobbyActor> lobbyActors = lobby.getActors();
        Set<LobbyActor> playingActors = getPlayingActors(lobbyActors);

        if (playingActors.size() >= MAX_LOBBY_SIZE && (role == LobbyActorRole.PLAYER || role == LobbyActorRole.PLAYER_BOT)) {
            throw new LobbyFullException();
        }

        if (lobby.getState() != LobbyState.OPEN && role == LobbyActorRole.PLAYER) {
            throw new LobbyClosedException();
        }

        if (lobbyActors.add(actor)) {
            playingActors = getPlayingActors(lobbyActors);

            if (playingActors.size() >= MAX_LOBBY_SIZE) {
                lobby.setState(LobbyState.CLOSED);
            }

            publisher.publishEvent(new LobbyUpdateEvent(lobbyMapper.toDto(lobby), LobbyUpdateType.JOIN, username));
        }
    }

    @Override
    public void leaveLobby(String lobbyId, String username) throws LobbyNotFoundException {
        Lobby lobby = lobbyRepository.find(lobbyId).orElseThrow(LobbyNotFoundException::new);

        if (lobby.getActors().removeIf(actor -> actor.getUsername().equals(username))) {
            publisher.publishEvent(new LobbyUpdateEvent(lobbyMapper.toDto(lobby), LobbyUpdateType.LEFT, username));
        }
    }

    @Override
    public void removeUserFromAllLobbies(String username) throws LobbyNotFoundException {
        for (Lobby lobby : lobbyRepository.findAll()) {
            leaveLobby(lobby.getId(), username);
        }
    }

    @Override
    public Optional<Lobby> getFromId(String id) {
        return lobbyRepository.find(id);
    }

    @Override
    public Optional<Lobby> getFromIdAuthenticated(String id, String username) {
        return lobbyRepository.findAuthenticated(id, username);
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanUpLobbies() {
        Collection<Lobby> allLobbies = lobbyRepository.findAll();

        for (Lobby lobby : allLobbies) {
            Set<LobbyActor> realActors = lobby.getActors().stream().filter(a -> a.getRole() != LobbyActorRole.PLAYER_BOT).collect(Collectors.toSet());

            if (realActors.isEmpty() && lobby.getCreatedAt().before(new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)))) {
                lobbyRepository.remove(lobby.getId());
                gameService.removeGame(lobby.getId());
            }
        }
    }

    @Override
    public void setState(String lobbyId, String username, LobbyState newState) throws LobbyNotFoundException, LobbyStateChangeException {
        Lobby lobby = lobbyRepository.findAuthenticated(lobbyId, username).orElseThrow(LobbyNotFoundException::new);

        Set<LobbyActor> playingActors = getPlayingActors(lobby.getActors());

        if (newState == LobbyState.OPEN && playingActors.size() >= MAX_LOBBY_SIZE) {
            throw new LobbyStateChangeException("Lobby cant be open if full");
        }

        LobbyState oldState = lobby.getState();
        lobby.setState(newState);

        publisher.publishEvent(new LobbyUpdateEvent(lobbyMapper.toDto(lobby), LobbyUpdateType.STATE_CHANGE, oldState.name()));
    }

    @Override
    public void setRole(String lobbyId, String username, LobbyActorRole newRole) throws LobbyException {
        Lobby lobby = lobbyRepository.findAuthenticated(lobbyId, username).orElseThrow(LobbyNotFoundException::new);

        Set<LobbyActor> playingActors = getPlayingActors(lobby.getActors());

        if (playingActors.size() >= MAX_LOBBY_SIZE && newRole == LobbyActorRole.PLAYER) {
            throw new LobbyException("Playing slots full");
        }

        Optional<LobbyActor> optionalActor = lobby.getActors().stream().filter(a -> a.getUsername().equals(username)).findFirst();

        if (optionalActor.isEmpty()) {
            throw new LobbyException("Actor not part of Lobby");
        }

        LobbyActor actor = optionalActor.get();
        actor.setRole(newRole);

        publisher.publishEvent(new LobbyUpdateEvent(lobbyMapper.toDto(lobby), LobbyUpdateType.ROLE_CHANGE, username));
    }

    @Override
    public Boolean isMember(String lobbyId, String username) {
        Optional<Lobby> lobby = lobbyRepository.findAuthenticated(lobbyId, username);

        return lobby.isPresent();
    }

    @Override
    public void addBot(String lobbyId, String username) throws LobbyNotFoundException, LobbyFullException, LobbyClosedException {
        String botName = botService.requestName().username();
        joinLobby(lobbyId, botName, LobbyActorRole.PLAYER_BOT);
    }

    @Override
    public void removeBot(String lobbyId, String botName) throws LobbyNotFoundException {
        leaveLobby(lobbyId, botName);
    }

    private String generateLobbyName() throws LobbyNameGeneratedException {
        Random rnd = new Random();

        String lobbyName = "";
        int tries = 0;

        do {
            tries++;

            String candidateName = String.format("%s-%s-%s",
                    LobbyWordsRepository.ADJECTIVES.get(rnd.nextInt(LobbyWordsRepository.ADJECTIVES.size())),
                    LobbyWordsRepository.ANIMALS.get(rnd.nextInt(LobbyWordsRepository.ANIMALS.size())),
                    LobbyWordsRepository.ACTIONS.get(rnd.nextInt(LobbyWordsRepository.ACTIONS.size())));

            if (lobbyRepository.find(candidateName).isEmpty()) {
                lobbyName = candidateName;
            }
        } while (!lobbyName.isEmpty() && tries < MAX_LOBBY_NAME_TRIES);

        if (lobbyName.isEmpty()) throw new LobbyNameGeneratedException();

        return lobbyName;
    }

    private @NonNull Set<LobbyActor> getPlayingActors(Set<LobbyActor> lobbyActors) {
        return lobbyActors.stream()
                .filter(p -> p.getRole() == LobbyActorRole.PLAYER || p.getRole() == LobbyActorRole.PLAYER_BOT)
                .collect(Collectors.toSet());
    }
}
