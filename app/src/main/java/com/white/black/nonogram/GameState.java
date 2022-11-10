package com.white.black.nonogram;

public enum GameState {
    MENU,
    PUZZLE_SELECTION,
    GAME,
    NEXT_PUZZLE,
    CONTINUE_PUZZLE,
    LOTTERY,
    LEADERBOARD,
    FACEBOOK,
    PLAYSTORE,
    WIKIPEDIA,
    ICONS8,
    PRIVACY_POLICY,
    PAUSED;

    private static volatile GameState gameState = GameState.MENU;

    public static GameState getGameState() {
        return gameState;
    }

    public synchronized static void setGameState(GameState newState) {
        gameState = newState;
    }
}
