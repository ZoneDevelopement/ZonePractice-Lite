{

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Profile profile = Practice.getProfileManager().getProfiles().get(player);
            Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

            if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.DUEL)) {
                if (match.getStatus().equals(MatchStatus.LIVE)) {
                    boolean isVoid = e.getCause().equals(EntityDamageEvent.DamageCause.VOID);

                    if (isVoid || player.getHealth() - e.getFinalDamage() <= 0) {
                        // Apply damage animation to die.
                        player.damage(0);
                        Duel.killPlayer(match, player, isVoid);
                        // Cancel the damage event
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = Practice.getProfileManager().getProfiles().get(player);
        Match match = Practice.getMatchManager().getLiveMatchByPlayer(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) && match.getType().equals(MatchType.DUEL)) {
            match.removePlayer(player, false);

            // Stop the match at the stating countdown
            if (match.getStartCountdown().isRunning())
                match.getRoundManager().endMatch(null);
                // Stop the match when it's live
            else
                match.getRoundManager().endMatch(Duel.getOppositePlayer(match, player));
        }
    }

}