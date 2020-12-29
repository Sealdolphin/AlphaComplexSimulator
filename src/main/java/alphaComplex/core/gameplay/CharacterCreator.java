package alphaComplex.core.gameplay;

import daiv.networking.command.acpf.request.SkillRequest;
import daiv.networking.command.acpf.response.SkillResponse;

import java.util.ArrayList;
import java.util.List;

public class CharacterCreator implements SkillRequest.SkillPickListener {

    private int round = 0;
    private ParanoiaPlayer player;
    private final List<ParanoiaPlayer> playerList = new ArrayList<>();

    public CharacterCreator(List<ParanoiaPlayer> players) {
        playerList.addAll(players);
    }

    public void setPlayer(ParanoiaPlayer player) {
        this.player = player;
    }

    public void startRound(int round) {
        this.round = round;
        playerList.get(0).sendSkillCommand(round);
    }

    @Override
    public void pickSkill(String skill) {
        if(round <= 5) {
            int index = playerList.indexOf(player);
            //Modify players!
            ParanoiaPlayer victim = index == 0 ? playerList.get(playerList.size() - 1) : playerList.get(index - 1);
            player.setSkill(skill, round);
            victim.setSkill(victim == player ? player.getRandomSkill() : skill, -round);

            if (index >= playerList.size() - 1) {
                startRound(++round);
            } else {
                playerList.get(index + 1).sendSkillCommand(round);
            }
        } //else invalid skill value
    }
}
