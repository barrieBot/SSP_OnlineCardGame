package game.CardGame.dtos;

public class CreateMatchDto {
    private String matchToken;
    private String playerToken;

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getMatchToken() {
        return matchToken;
    }

    public void setMatchToken(String matchToken) {
        this.matchToken = matchToken;
    }
}
