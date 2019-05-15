package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
// informacion util: https://mindhubweb.xtolcorp.com/ebooks/item?id=1168

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository repo;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;
/*
    @Autowired
    private PasswordEncoder passwordEncoder;
*/
    @RequestMapping("/games")
// informacion para entender el punto el punto 2.5: https://mindhubweb.xtolcorp.com/ebooks/item?id=1169
    public List<Object> getAllGames() {
        return repo
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer)).collect(Collectors.toList()));
        dto.put("score",getScoreList(game.getScores()));
        return dto;
    }

    public List<Map<String,Object>>getGamePlayerList(Set<GamePlayer> gamePlayers){
        return gamePlayers.stream().map(gamePlayer -> makeGamePlayerDTO(gamePlayer)).collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gp) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gp.getId());
        //dto.put("joinDate",gp.getCreationDate());
        dto.put("player", makePlayerDTO(gp.getPlayer()));
        return dto;
    }

    /*

    Ok, la cosa es así en la clase GamePlayer deben crear un método llamado getScore y en la clase Player deben crear
    un método llamado getScore también, el tema es que el getScore de player recibe un juego entonces ese método va a
    hacer un stream de los gamePlayers de ese player para filtrarlo donde el game del gameplayer sea el mismo game que
    le pasan por parámetro. Luuego en el método getScore de GamePlayer llaman al método getScore del player que tiene
    ese GamePlayer pasandole el juego que tiene ese GamePlayer. Les paso el código

    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", this.player.makePlayerDTO());
        dto.put("gameState", gameState());
        dto.put("salvoTurn", currentTurn());
        dto.put("shotsToMake", shotsToMake());
        if (this.getScore() != null)
            dto.put("score", this.getScore().getScorePoint());
        else
            dto.put("score", this.getScore());
        return dto;
    }
     */

    private Map<String, Object> makePlayerDTO(Player p) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", p.getId());
        dto.put("email", p.getUserName());
        //dto.put("score";makeScoreList(p));
        return dto;
    }

    // tacke 3 punto 2
    @RequestMapping("/game_view/{id}")
    public Map<String,Object> getGamePlayerView(@PathVariable Long id){
       return gameViewDTO(gamePlayerRepository.findById(id).get());
    }

    public Map<String, Object> gameViewDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("creationDate", gamePlayer.getCreationDate());
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships",makeShipList(gamePlayer.getShips()));
        dto.put("salvoes",getSalvoList(gamePlayer.getGame()));

        return dto;
    }

    //ships
    public List<Map<String,Object>> makeShipList(Set<Ship> ships){
        return ships.stream().map(ship -> makeShipDTO(ship)).collect(Collectors.toList());
    }
    public Map<String,Object> makeShipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type",ship.getType());
        dto.put("locations",ship.getLocations());

        return dto;
    }

    //point 4.2

    private List<Map<String,Object>> getSalvoList(Game game) {
        List<Map<String, Object>> myList = new ArrayList<>();
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    public List<Map<String,Object>> makeSalvoList(Set<Salvo> salvos){
        return salvos.stream().map(salvo -> makeSalvoDTO(salvo)).collect(Collectors.toList());
    }

    public Map<String,Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn",salvo.getTurn());
        dto.put("player",salvo.getGamePlayer().getId());
        dto.put("locations",salvo.getLocations());

        return dto;
    }

    //point 5.2


    @RequestMapping("/leaderBoard")
    public List<Object> makeLeaderBoard() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> playerLeaderBoardDTO(player))
                .collect(Collectors.toList());
    }

    private Map<String, Object> playerLeaderBoardDTO(Player p) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", p.getId());
        dto.put("email", p.getUserName());
        dto.put("score",makeScoreList(p));
        return dto;
    }

    public Map<String, Object> makeScoreList(Player player){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("name",player.getUserName());
        dto.put("total",player.getScore(player));
        dto.put("won",player.getWins(player.getScores()));
        dto.put("lost",player.getLoses(player.getScores()));
        dto.put("tied",player.getDraws(player.getScores()));

        return dto;

    }

    public List<Map<String,Object>>getScoreList(List<Score> scores){
        return scores.stream().map(score -> makeScoreDTO(score)).collect(Collectors.toList());
    }

    public Map<String, Object> makeScoreDTO(Score score){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("name",score.getPlayer().getUserName());
        dto.put("score",score.getScore());
        //dto.put("score",player.getWins(player.getScore()));
        dto.put("finishDate",score.getFinishDate());

        return dto;

    }

    //punto 5.3
/*
    @RequestMapping(path = "/player", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
*/
}
