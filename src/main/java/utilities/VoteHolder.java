package utilities;

import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class VoteHolder {
    public int votes;
    public int voteCap;
    public double voteWinPercentage;
    public List<Member> voters;

    public VoteHolder() {
        this.votes = 0;
        this.voteCap = 0;
        this.voteWinPercentage = 1;
        this.voters = new ArrayList<>();
    }

    public boolean runVoteCheck() {
        return (double) votes / voteCap >= voteWinPercentage;
    }

    public boolean addVoter(Member voter) {
        if (voters.contains(voter)) {
            return false;
        } else {
            this.voters.add(voter);
            this.votes++;
        }
        return true;
    }

    public void modifyAttributes(int voteCap, double voteWinPercentage) {
        this.voteCap = voteCap;
        if (voteWinPercentage > 1) {
            this.voteWinPercentage = 1;
        } else if (voteWinPercentage < 0) {
            this.voteWinPercentage = 0;
        } else {
            this.voteWinPercentage = voteWinPercentage;
        }
    }

    public void resetCounter() {
        this.votes = 0;
        this.voters.clear();
    }

    public int getVotes() {
        return this.votes;
    }

    public int getVoteCap() {
        return this.voteCap;
    }
}
