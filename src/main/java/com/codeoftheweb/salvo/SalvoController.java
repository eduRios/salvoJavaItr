package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        dto.put("ships",gamePlayer.getShips());
        dto.put("salvoes",getSalvoList(gamePlayer.getGame()));

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

}
