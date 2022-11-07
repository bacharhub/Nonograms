package com.white.black.nonogram;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import com.white.black.nonogram.activities.GameActivity;
import com.white.black.nonogram.view.ColorPack;

import java.util.ArrayList;

public enum Puzzles {
    SMALL,
    NORMAL,
    LARGE,
    COMPLEX,
    COLORFUL;

    private static final int NUM_OF_PUZZLES_PER_CATEGORY = 100;

    public static int getNumOfPuzzlesPerCategory() {
        return NUM_OF_PUZZLES_PER_CATEGORY;
    }

    private volatile static boolean firstLoadingDone;

    public static Boolean isFirstLoadingDone() {
        return firstLoadingDone;
    }

    private synchronized static void setFirstLoadingDone(boolean b) {
        firstLoadingDone = b;
    }

    private volatile static Puzzles current = Puzzles.SMALL;

    private volatile static PuzzleReference lastPuzzle;

    public static Puzzles getCurrent() {
        return current;
    }

    public synchronized static void setCategory(Puzzles category) {
        current = category;
    }

    public static void nextPuzzle() {
        switch (current) {
            case SMALL:
                setCategory(NORMAL);
                break;
            case NORMAL:
                setCategory(LARGE);
                break;
            case LARGE:
                setCategory(COMPLEX);
                break;
            case COMPLEX:
                setCategory(COLORFUL);
                break;
            default:
                setCategory(SMALL);
                break;
        }
    }

    public static void releasePuzzlesOfOtherCategories() {
        for (Puzzles puzzleCategory : Puzzles.values()) {
            if (puzzleCategory != Puzzles.getCurrent()) {
                for (PuzzleReference puzzleReference : puzzleCategory.getPuzzleReferences()) {
                    puzzleReference.release();
                }
            }
        }
    }

    public static boolean hasPlayerSolvedAtLeastOnePuzzle(Context context) {
        return numOfSolvedPuzzles(context) > 0;
    }

    public static int numOfSolvedPuzzles(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getString(R.string.most_puzzles_solved), 0);
    }

    public static PuzzleReference getTutorialPuzzleReference() {
        return Puzzles.SMALL.puzzleReferences.get(0);
    }

    private static void addPuzzleReference(String puzzleName, int puzzleId, int imageId) {
        addPuzzleReference(Puzzle.PuzzleClass.VIP, puzzleName, puzzleId, imageId);
    }

    private static void addPuzzleReference(Puzzle.PuzzleClass puzzleClass, String puzzleName, int puzzleId, int imageId) {
        addPuzzleReference(puzzleClass, puzzleName, puzzleId, imageId, false, null);
    }

    private static void addPuzzleReference(Puzzle.PuzzleClass puzzleClass, String puzzleName, int puzzleId, int imageId, boolean isTutorial, Puzzle.SolutionSteps solutionSteps) {
        current.puzzleReferences.add(new PuzzleReference(puzzleClass, puzzleName, puzzleId, imageId, isTutorial, solutionSteps));
    }

    private static void loadSmallPuzzles(Context context) {
        current = SMALL;

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.smile), 5, R.drawable.smile_5, true, new Puzzle.SolutionSteps(new Puzzle.SolutionStep[]{
                        new Puzzle.SolutionStep(0, 4, 3, 3),
                        new Puzzle.SolutionStep(4, 4, 2, 3),
                        new Puzzle.SolutionStep(3, 3, 0, 1),
                        new Puzzle.SolutionStep(1, 1, 0, 1)
                })
        ); // 00:05

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.hashtag), 12, R.drawable.hashtag_12); // 00:05
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.ticket), 11, R.drawable.ticket_11); // 00:06
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.screen), 13, R.drawable.screen_13); // 00:08
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.charged_battery), 14, R.drawable.charged_battery_14); // 00:08
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.box), 14, R.drawable.box_14); // 00:13
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.binoculars), 12, R.drawable.binoculars_12); // 00:14
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.flag), 13, R.drawable.flag_13); // 00:19
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.bus), 13, R.drawable.bus_13); // 00:24
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.mail), 12, R.drawable.mail_12); // 00:26
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.hourglass), 14, R.drawable.hourglass_14); // 00:29
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.keyboard), 13, R.drawable.keyboard_13); // 00:32

        addPuzzleReference(context.getString(R.string.camera), 12, R.drawable.camera_12); // 00:32
        addPuzzleReference(context.getString(R.string.lock), 12, R.drawable.lock_12); // 00:34
        addPuzzleReference(context.getString(R.string.human), 18, R.drawable.human_18); // 00:39
        addPuzzleReference(context.getString(R.string.trash_can), 13, R.drawable.trash_can_13); // 00:45
        addPuzzleReference(context.getString(R.string.gameboy), 11, R.drawable.game_boy_11); // 00:45
        addPuzzleReference(context.getString(R.string.calculator), 13, R.drawable.calculator_13); // 00:49
        addPuzzleReference(context.getString(R.string.gun), 12, R.drawable.gun_12); // 00:51
        addPuzzleReference(context.getString(R.string.store), 13, R.drawable.store_13); // 00:53
        addPuzzleReference(context.getString(R.string.kennel), 12, R.drawable.kennel_12); // 00:54
        addPuzzleReference(context.getString(R.string.musical_note), 13, R.drawable.musical_note_13); // 00:55
        addPuzzleReference(context.getString(R.string.glasses), 13, R.drawable.glasses_13); // 01:05
        addPuzzleReference(context.getString(R.string.dumbbell), 13, R.drawable.dumbbell_13); // 01:09
        addPuzzleReference(context.getString(R.string.no_entrance), 12, R.drawable.no_entrance_12); // 01:17
        addPuzzleReference(context.getString(R.string.eye), 13, R.drawable.eye_13); // 01:46
        addPuzzleReference(context.getString(R.string.monster), 10, R.drawable.monster_10); // 01:49
        addPuzzleReference(context.getString(R.string.key), 12, R.drawable.key_12); // 02:05
        addPuzzleReference(context.getString(R.string.wifi), 14, R.drawable.wifi_14); // 02:10
        addPuzzleReference(context.getString(R.string.clock), 12, R.drawable.clock_12); // 02:52
        addPuzzleReference(context.getString(R.string.smile), 12, R.drawable.smile_12); // 02:53
        addPuzzleReference(context.getString(R.string.microphone), 14, R.drawable.microphone_14); // 03:48
        addPuzzleReference(context.getString(R.string.moon), 13, R.drawable.moon_13); // 05:31
    }

    private static void loadNormalPuzzles(Context context) {
        current = NORMAL;

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.dispenser), 16, R.drawable.dispenser_16); // 00:29
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.bell), 16, R.drawable.bell_16); // 00:40
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.chocolate_bar), 16, R.drawable.chocolate_bar_16); // 00:51
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.cheesecake), 16, R.drawable.cheesecake_16); // 01:22
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.gift), 16, R.drawable.gift_16); // 01:38
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.heart_balloon), 16, R.drawable.heart_balloon_16); // 01:45
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.crown), 16, R.drawable.crown_16); // 02:02
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.cake), 16, R.drawable.cake_16); // 02:10
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.cat), 16, R.drawable.cat_16); // 02:13
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.camera), 16, R.drawable.camera_16); // 02:22
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.shop), 16, R.drawable.shop_16); // 02:32
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.octopus), 16, R.drawable.octopus_16); // 02:35

        addPuzzleReference( context.getString(R.string.lock), 18, R.drawable.lock_18); // 02:46
        addPuzzleReference( context.getString(R.string.fox), 16, R.drawable.fox_15); // 02:50
        addPuzzleReference(context.getString(R.string.coat), 16, R.drawable.coat_16); // 02:54
        addPuzzleReference( context.getString(R.string.bottle_of_water), 16, R.drawable.bottle_of_water_16); // 02:56
        addPuzzleReference(context.getString(R.string.golf_ball), 16, R.drawable.golf_ball_16); // 02:57
        addPuzzleReference(context.getString(R.string.mosque), 16, R.drawable.mosque_16); // 03:05
        addPuzzleReference(context.getString(R.string.skull), 16, R.drawable.skull_16); // 03:17
        addPuzzleReference(context.getString(R.string.heart), 16, R.drawable.heart_16); // 03:17
        addPuzzleReference(context.getString(R.string.gatling_gun), 19, R.drawable.gatling_gun_19); // 03:22
        addPuzzleReference(context.getString(R.string.mitten), 16, R.drawable.mitten_16); // 03:25
        addPuzzleReference(context.getString(R.string.hamburger), 13, R.drawable.hamburger_13); // 03:26
        addPuzzleReference(context.getString(R.string.manager), 16, R.drawable.manager_16); // 03:40
        addPuzzleReference(context.getString(R.string.clock), 16, R.drawable.clock_16); // 04:20
        addPuzzleReference(context.getString(R.string.fax), 16, R.drawable.fax_16); // 04:29
        addPuzzleReference(context.getString(R.string.rat), 16, R.drawable.rat_16); // 04:30
        addPuzzleReference(context.getString(R.string.pointing_finger), 16, R.drawable.pointing_finger_16); // 04:43
        addPuzzleReference(context.getString(R.string.torch), 16, R.drawable.torch_16); // 04:56
        addPuzzleReference(context.getString(R.string.fighter_jet), 16, R.drawable.fighter_jet_16); // 05:00
        addPuzzleReference(context.getString(R.string.twitter), 16, R.drawable.twitter_16); // 05:06
        addPuzzleReference(context.getString(R.string.gas), 16, R.drawable.gas_station_16); // 05:09
        addPuzzleReference(context.getString(R.string.santa), 16, R.drawable.santa_16); // 05:13
        addPuzzleReference(context.getString(R.string.scooter), 16, R.drawable.scooter_16); // 05:35
        addPuzzleReference(context.getString(R.string.car), 16, R.drawable.car_16); // 05:53
        addPuzzleReference(context.getString(R.string.bus), 16, R.drawable.bus_16); // 05:57
        addPuzzleReference( context.getString(R.string.doughnut), 16, R.drawable.doughnut_14); // 06:18
        addPuzzleReference(context.getString(R.string.emoji), 15, R.drawable.emoji_15); // 07:02
    }

    private static void loadLargePuzzles(Context context) {
        current = LARGE;

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.android), 24, R.drawable.android_24); // 02:06
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.trash_can), 24, R.drawable.trash_can_24); // 02:39
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.candles), 24, R.drawable.candles_24); // 03:17
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.pig), 24, R.drawable.pig_24); // 04:36
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.corgi), 24, R.drawable.corgi_24); // 05:26
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.bug), 24, R.drawable.bug_24); // 05:49
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.rucksack), 24, R.drawable.rucksack_24); // 06:32
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.coffee_maker), 24, R.drawable.coffee_maker_24); // 06:48
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.butterfly), 24, R.drawable.butterfly_24); // 06:54
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.gorilla), 24, R.drawable.gorilla_24); // 07:46
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.elephant), 24, R.drawable.elephant_24); // 07:54
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.bear), 24, R.drawable.bear_24); // 08:03

        addPuzzleReference(context.getString(R.string.charcoal), 24, R.drawable.charcoal_24); // 08:34
        addPuzzleReference(context.getString(R.string.snail), 24, R.drawable.snail_24); // 09:43
        addPuzzleReference(context.getString(R.string.alcohol_free), 24, R.drawable.alcohol_free_24); // 09:57
        addPuzzleReference(context.getString(R.string.fire_extinguisher), 24, R.drawable.fire_extinguisher_24); // 10:48
        addPuzzleReference(context.getString(R.string.female_model), 24, R.drawable.female_model_24); // 10:51
        addPuzzleReference(context.getString(R.string.office_chair), 24, R.drawable.office_chair_24); // 11:24
        addPuzzleReference(context.getString(R.string.cat), 24, R.drawable.cat_24); // 11:40
        addPuzzleReference(context.getString(R.string.cherry), 24, R.drawable.cherry_24); // 12:55
        addPuzzleReference(context.getString(R.string.coffee_pot), 24, R.drawable.coffee_pot_24); // 13:10
        addPuzzleReference(context.getString(R.string.phone), 24, R.drawable.phone_23); // 13:11
        addPuzzleReference(context.getString(R.string.anonymous), 24, R.drawable.anonymous_24); // 13:15
        addPuzzleReference(context.getString(R.string.octopus), 24, R.drawable.octopus_24); // 13:20
        addPuzzleReference(context.getString(R.string.tire), 24, R.drawable.tire_24); // 13:26
        addPuzzleReference(context.getString(R.string.alligator), 24, R.drawable.alligator_24); // 13:31
        addPuzzleReference(context.getString(R.string.firefighter), 24, R.drawable.firefighter_24); // 14:27
        addPuzzleReference(context.getString(R.string.cards), 24, R.drawable.cards_24); // 14:36
        addPuzzleReference(context.getString(R.string.linux), 24, R.drawable.linux_24); // 14:43
        addPuzzleReference(context.getString(R.string.panda), 24, R.drawable.panda_24); // 14:56
        addPuzzleReference(context.getString(R.string.bowling), 24, R.drawable.bowling_24); // 15:36
        addPuzzleReference(context.getString(R.string.grenade), 24, R.drawable.grenade_24); // 16:52
        addPuzzleReference(context.getString(R.string.ninja), 24, R.drawable.ninja_24); // 27:08
        addPuzzleReference(context.getString(R.string.icq), 24, R.drawable.icq_24); // 28:49
        addPuzzleReference(context.getString(R.string.yin_yang), 24, R.drawable.yin_yang_24); // 33:47
        addPuzzleReference(context.getString(R.string.dice), 24, R.drawable.dice_24); // 36:42
    }

    private static void loadComplexPuzzles(Context context) {
        current = COMPLEX;

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.snapchat), 50, R.drawable.snapchat_50); // 02:08
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.puzzle), 50, R.drawable.puzzle_50); // 02:52
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.credit_card), 50, R.drawable.credit_card_50); // 03:41
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.nail), 50, R.drawable.nail_50); // 04:02
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.mailbox), 50, R.drawable.mailbox_50); // 04:04
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.jacket), 50, R.drawable.jacket_50); // 04:08
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.piano), 50, R.drawable.piano_50); // 04:10
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.stapler), 50, R.drawable.stapler_50); // 05:27
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.kimono), 50, R.drawable.kimono_50); // 06:02
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.crow), 50, R.drawable.crow_50); // 06:13
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.birthday_present), 50, R.drawable.birthday_present_50); // 07:24
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.starfish), 50, R.drawable.starfish_50); // 07:36

        addPuzzleReference(context.getString(R.string.wizard), 50, R.drawable.wizard_50); // 08:01
        addPuzzleReference(context.getString(R.string.treasure_chest), 50, R.drawable.treasure_chest_50); // 09:31
        addPuzzleReference(context.getString(R.string.bandit), 50, R.drawable.bandit_50); // 09:37
        addPuzzleReference(context.getString(R.string.pelican), 50, R.drawable.pelican_50); // 10:34
        addPuzzleReference(context.getString(R.string.ace), 50, R.drawable.ace_50); // 10:42
        addPuzzleReference(context.getString(R.string.fort), 50, R.drawable.fort_50); // 10:57
        addPuzzleReference(context.getString(R.string.work_boot), 50, R.drawable.work_boot_50); // 11:04
        addPuzzleReference(context.getString(R.string.mask), 50, R.drawable.mask_50); // 11:06
        addPuzzleReference(context.getString(R.string.rabbit), 50, R.drawable.rabbit_50); // 11:11
        addPuzzleReference(context.getString(R.string.bulletproof_vest), 50, R.drawable.bulletproof_vest_50); // 11:18
        addPuzzleReference(context.getString(R.string.dinosaur), 50, R.drawable.dinosaur_50); // 11:46
        addPuzzleReference(context.getString(R.string.water_well), 50, R.drawable.water_well_50); // 12:28
        addPuzzleReference(context.getString(R.string.seahorse), 50, R.drawable.seahorse_50); // 12:35
        addPuzzleReference(context.getString(R.string.spectrophotometer), 50, R.drawable.spectrophotometer_50); // 12:52
        addPuzzleReference(context.getString(R.string.popcorn), 50, R.drawable.popcorn_50); // 13:00
        addPuzzleReference(context.getString(R.string.money_sack), 50, R.drawable.money_sack_50); // 13:02
        addPuzzleReference(context.getString(R.string.whale), 50, R.drawable.whale_50); // 13:15
        addPuzzleReference(context.getString(R.string.potato_chips), 50, R.drawable.potato_chips_50); // 14:13
        addPuzzleReference(context.getString(R.string.merry_go_round), 50, R.drawable.merry_go_round_50); // 14:20
        addPuzzleReference(context.getString(R.string.sofa), 50, R.drawable.sofa_50); // 14:28
        addPuzzleReference(context.getString(R.string.sonic_the_hedgehog), 50, R.drawable.sonic_the_hedgehog_50); // 14:33
        addPuzzleReference(context.getString(R.string.astronaut), 50, R.drawable.astronaut_50); // 14:43
        addPuzzleReference(context.getString(R.string.bear), 50, R.drawable.bear_50); // 15:02
        addPuzzleReference(context.getString(R.string.minion), 50, R.drawable.minion_50); // 15:30
        addPuzzleReference(context.getString(R.string.snowflake), 50, R.drawable.snowflake_50); // 16:32
        addPuzzleReference(context.getString(R.string.superman), 50, R.drawable.superman_50); // 16:43
        addPuzzleReference(context.getString(R.string.suit), 50, R.drawable.suit_50); // 16:48
        addPuzzleReference(context.getString(R.string.hippopotamus), 50, R.drawable.hippopotamus_50); // 17:09
        addPuzzleReference(context.getString(R.string.couple), 50, R.drawable.couple_50); // 17:22
        addPuzzleReference(context.getString(R.string.falcon), 50, R.drawable.falcon_50); // 17:41
        addPuzzleReference(context.getString(R.string.ushanka), 50, R.drawable.ushanka_50); // 17:44
        addPuzzleReference(context.getString(R.string.star_of_david), 96, R.drawable.star_of_david_96); // 18:17
        addPuzzleReference(context.getString(R.string.fried_chicken), 50, R.drawable.fried_chicken_50); // 19:14
        addPuzzleReference(context.getString(R.string.snake), 50, R.drawable.snake_50); // 20:48
        addPuzzleReference(context.getString(R.string.roman_soldier), 50, R.drawable.roman_soldier_50); // 22:18
        addPuzzleReference(context.getString(R.string.earthquakes), 50, R.drawable.earthquakes_50); // 25:11
        addPuzzleReference(context.getString(R.string.rose), 50, R.drawable.rose_50); // 30:33
        addPuzzleReference(context.getString(R.string.homer_simpson), 100, R.drawable.homer_simpson_100); // 31:57
        addPuzzleReference(context.getString(R.string.cheburashka), 50, R.drawable.cheburashka_100); // 32:08
        addPuzzleReference(context.getString(R.string.trombone), 50, R.drawable.trombone_50); // 32:10
        addPuzzleReference(context.getString(R.string.farm), 50, R.drawable.farm_50); // 32:39
        addPuzzleReference(context.getString(R.string.amusement_park), 100, R.drawable.amusement_park_100); // 41:03
        addPuzzleReference(context.getString(R.string.tiger), 50, R.drawable.tiger_50); // 43:13
        addPuzzleReference(context.getString(R.string.ruby), 100, R.drawable.ruby_100); // 43:25
        addPuzzleReference(context.getString(R.string.marge_simpson), 100, R.drawable.marge_simpson_100); // 48:11
    }

    private static void loadColorfulPuzzles(Context context) {
        current = COLORFUL;

        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.cactus), 96, R.drawable.cactus_16); // 05:37
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.mushroom), 96, R.drawable.mushroom_16); // 07:06
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.super_mario), 16, R.drawable.super_mario_16); // 10:09
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.pokeball), 96, R.drawable.pokeball_16); // 10:26
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.emoji), 17, R.drawable.emoji_17); // 11:29
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.whale), 20, R.drawable.whale_20); // 21:32
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.sock), 24, R.drawable.sock_96); // 03:00
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.flame), 24, R.drawable.flame_96); // 16:17
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.big_ben), 96, R.drawable.big_ben_96); // 18:06
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.volcano), 48, R.drawable.volcano_96); // 18:23
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.seal), 96, R.drawable.seal_96); // 19:20
        addPuzzleReference(Puzzle.PuzzleClass.FREE, context.getString(R.string.accordion), 96, R.drawable.accordion_96); // 23:27

        addPuzzleReference(context.getString(R.string.engagement_ring), 96, R.drawable.engagement_ring_96); // 25:01
        addPuzzleReference(context.getString(R.string.milkshake), 48, R.drawable.milkshake_96); // 25:19
        addPuzzleReference(context.getString(R.string.dreidel), 96, R.drawable.dreidel_96); // 31:11
        addPuzzleReference(context.getString(R.string.winrar), 96, R.drawable.winrar_96); // 36:54
        addPuzzleReference(context.getString(R.string.steve_jobs), 48, R.drawable.steve_jobs_96); // 42:00
        addPuzzleReference(context.getString(R.string.pizza), 96, R.drawable.pizza_96); // 43:54
        addPuzzleReference(context.getString(R.string.avatar), 96, R.drawable.avatar_96); // 57:21
        addPuzzleReference(context.getString(R.string.barcelona_fc), 96, R.drawable.barcelona_fc_96); // 01:48:56

        addPuzzleReference(context.getString(R.string.maracas), 96, R.drawable.maracas_96);
        addPuzzleReference(context.getString(R.string.pool_table), 96, R.drawable.pool_table_96);
        addPuzzleReference(context.getString(R.string.violin), 96, R.drawable.violin_96);
        addPuzzleReference(context.getString(R.string.children), 96, R.drawable.children_96);
        addPuzzleReference(context.getString(R.string.pikachu_pokemon), 96, R.drawable.pikachu_pokemon_96);
        addPuzzleReference(context.getString(R.string.pie), 96, R.drawable.pie_96);
        addPuzzleReference(context.getString(R.string.statue_of_liberty), 96, R.drawable.statue_of_liberty_96);
        addPuzzleReference(context.getString(R.string.shop), 96, R.drawable.shop_96);
        addPuzzleReference(context.getString(R.string.red_wine), 96, R.drawable.red_wine_96);
        addPuzzleReference(context.getString(R.string.fish), 96, R.drawable.fish_96);
        addPuzzleReference(context.getString(R.string.santa), 48, R.drawable.santa_96);
        addPuzzleReference(context.getString(R.string.gameboy), 96, R.drawable.game_boy_96);
        addPuzzleReference(context.getString(R.string.selfie), 48, R.drawable.selfie_96);
        addPuzzleReference(context.getString(R.string.sock_puppet), 48, R.drawable.sock_puppet_96);
        addPuzzleReference(context.getString(R.string.umbrella), 48, R.drawable.umbrella_96);
        addPuzzleReference(context.getString(R.string.baby_bottle), 48, R.drawable.baby_bottle_96);
        addPuzzleReference(context.getString(R.string.old_man), 48, R.drawable.old_man_96);
        addPuzzleReference(context.getString(R.string.farm), 96, R.drawable.farm_96);
        addPuzzleReference(context.getString(R.string.supplier), 96, R.drawable.supplier_96);
        addPuzzleReference(context.getString(R.string.teddy_bear), 96, R.drawable.teddy_bear_96);
        addPuzzleReference(context.getString(R.string.fly), 48, R.drawable.fly_96);
        addPuzzleReference(context.getString(R.string.save_as), 48, R.drawable.save_as_96);
        addPuzzleReference(context.getString(R.string.policeman), 48, R.drawable.policeman_96);
        addPuzzleReference(context.getString(R.string.salah), 48, R.drawable.salah_96);
        addPuzzleReference(context.getString(R.string.reading_unicorn), 48, R.drawable.reading_unicorn_96);
        addPuzzleReference(context.getString(R.string.chocolate_bar), 24, R.drawable.chocolate_bar_96);
        addPuzzleReference(context.getString(R.string.ice_cream), 24, R.drawable.ice_cream_96);
        addPuzzleReference(context.getString(R.string.colorful_dice), 27, R.drawable.colorful_dice_96);
        addPuzzleReference(context.getString(R.string.ingredients), 24, R.drawable.ingredients_96);
        addPuzzleReference(context.getString(R.string.saturn), 48, R.drawable.saturn_96);
        addPuzzleReference(context.getString(R.string.batman), 48, R.drawable.batman_96);
        addPuzzleReference(context.getString(R.string.jam), 48, R.drawable.jam_96);
        addPuzzleReference(context.getString(R.string.obama), 48, R.drawable.obama_96);
        addPuzzleReference(context.getString(R.string.watermelon), 48, R.drawable.watermelon_96);
        addPuzzleReference(context.getString(R.string.french_fries), 48, R.drawable.french_fries_96);
        addPuzzleReference(context.getString(R.string.like), 48, R.drawable.like_96);
        addPuzzleReference(context.getString(R.string.hulk), 48, R.drawable.hulk_96);
        addPuzzleReference(context.getString(R.string.tudor_rose), 48, R.drawable.rose_96);
        addPuzzleReference(context.getString(R.string.superman), 48, R.drawable.superman_96);
        addPuzzleReference(context.getString(R.string.sewing_machine), 48, R.drawable.sewing_machine_96);
        addPuzzleReference(context.getString(R.string.neymar), 48, R.drawable.neymar_96);
        addPuzzleReference(context.getString(R.string.groot), 48, R.drawable.groot_96);
        addPuzzleReference(context.getString(R.string.joker), 48, R.drawable.joker_96);
        addPuzzleReference(context.getString(R.string.parrot), 48, R.drawable.parrot_96);
        addPuzzleReference(context.getString(R.string.raspberry), 96, R.drawable.razz_berry_96);
        addPuzzleReference(context.getString(R.string.porsche), 96, R.drawable.porsche_96);
        addPuzzleReference(context.getString(R.string.halloween), 96, R.drawable.halloween_96);
        addPuzzleReference(context.getString(R.string.helicopter), 96, R.drawable.helicopter_96);
        addPuzzleReference(context.getString(R.string.bicycle), 96, R.drawable.bicycle_96);
        addPuzzleReference(context.getString(R.string.woody_woodpecker), 96, R.drawable.woody_woodpecker_96);
        addPuzzleReference(context.getString(R.string.steak), 48, R.drawable.steak_96);
        addPuzzleReference(context.getString(R.string.john_cena), 48, R.drawable.john_cena_96);
        addPuzzleReference(context.getString(R.string.tank), 96, R.drawable.tank_96);
        addPuzzleReference(context.getString(R.string.logan), 48, R.drawable.logan_96);
        addPuzzleReference(context.getString(R.string.harley_quinn), 48, R.drawable.harley_quinn_96);
        addPuzzleReference(context.getString(R.string.deadpool), 48, R.drawable.deadpool_96);
        addPuzzleReference(context.getString(R.string.banjo), 48, R.drawable.banjo_96);
        addPuzzleReference(context.getString(R.string.ladybird), 48, R.drawable.ladybird_96);
        addPuzzleReference(context.getString(R.string.harry_potter), 96, R.drawable.harry_potter_96);
        addPuzzleReference(context.getString(R.string.open_book), 48, R.drawable.open_book_96);
        addPuzzleReference(context.getString(R.string.notepad), 48, R.drawable.notepad_96);
    }

    private static void firstLoading(Context context) {
        loadSmallPuzzles(context);
        loadNormalPuzzles(context);
        loadLargePuzzles(context);
        loadComplexPuzzles(context);
        loadColorfulPuzzles(context);

        for (Puzzles puzzleCategory : Puzzles.values()) {
            for (int i = 0; i < 12; i++) {
                puzzleCategory.getPuzzleReferences().get(i).load(context);
            }
        }

        linkPuzzles();
        loadLastPuzzle(context);
    }

    private static void loadLastPuzzle(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPuzzleUniqueId = sharedPreferences.getString("lastPuzzleUniqueId", null);

        if (lastPuzzleUniqueId == null) {
            return;
        }

        for (Puzzles puzzleCategory : Puzzles.values()) {
            for (PuzzleReference puzzleReference : puzzleCategory.getPuzzleReferences()) {
                if (puzzleReference.getUniqueId().equals(lastPuzzleUniqueId)) {
                    lastPuzzle = puzzleReference;
                    return;
                }
            }
        }
    }

    public static void setTutorialPuzzleAsLastPuzzle() {
        lastPuzzle = getTutorialPuzzleReference();
    }

    public static void writeToSharedPreferencesLastPuzzle(Context context, String uniqueId) {
        try {
            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefsEditor.putString("lastPuzzleUniqueId", uniqueId);
            prefsEditor.apply();
        } catch (Exception ignored) {

        }
    }

    public static PuzzleReference getLastPuzzle() {
        return lastPuzzle;
    }

    public static void moveToCategoryByPuzzle(PuzzleReference puzzle) {
        for (Puzzles puzzleCategory : Puzzles.values()) {
            if (puzzleCategory.getPuzzleReferences().contains(puzzle)) {
                Puzzles.setCategory(puzzleCategory);
            }
        }
    }

    public synchronized static void init(Context context) {
        if (firstLoadingDone) {
            return; // do not load puzzles twice
        }

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        Puzzles.SMALL.setColorPack(new ColorPack(ContextCompat.getColor(context, R.color.smallPuzzleGreen1), ContextCompat.getColor(context, R.color.smallPuzzleGreen2), ContextCompat.getColor(context, R.color.smallPuzzleGreen3)));
        Puzzles.NORMAL.setColorPack(new ColorPack(ContextCompat.getColor(context, R.color.normalPuzzleOrange1), ContextCompat.getColor(context, R.color.normalPuzzleOrange2), ContextCompat.getColor(context, R.color.normalPuzzleOrange3)));
        Puzzles.LARGE.setColorPack(new ColorPack(ContextCompat.getColor(context, R.color.largePuzzleRed1), ContextCompat.getColor(context, R.color.largePuzzleRed2), ContextCompat.getColor(context, R.color.largePuzzleRed3)));
        Puzzles.COMPLEX.setColorPack(new ColorPack(ContextCompat.getColor(context, R.color.complexPuzzleLightBlue1), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue2), ContextCompat.getColor(context, R.color.complexPuzzleLightBlue3)));
        Puzzles.COLORFUL.setColorPack(new ColorPack(ContextCompat.getColor(context, R.color.colorfulPuzzlePink1), ContextCompat.getColor(context, R.color.colorfulPuzzlePink2), ContextCompat.getColor(context, R.color.colorfulPuzzlePink3)));

        firstLoading(context);
        setFirstLoadingDone(true);
    }

    private static void linkPuzzles() {
        PuzzleReference lastCreatedPuzzle = SMALL.getPuzzleReferences().get(0);
        for (Puzzles puzzleCategory : Puzzles.values()) {
            for (PuzzleReference puzzle : puzzleCategory.getPuzzleReferences()) {
                lastCreatedPuzzle.setNextPuzzleNode(puzzle);
                lastCreatedPuzzle = puzzle;
            }
        }

        lastCreatedPuzzle.setNextPuzzleNode(SMALL.getPuzzleReferences().get(0));
    }

    private final ArrayList<PuzzleReference> puzzleReferences;
    private volatile ColorPack colorPack;

    Puzzles() {
        this.puzzleReferences = new ArrayList<>(NUM_OF_PUZZLES_PER_CATEGORY);
    }

    public ArrayList<PuzzleReference> getPuzzleReferences() {
        return this.puzzleReferences;
    }

    public ColorPack getColorPack() {
        return colorPack;
    }

    private synchronized void setColorPack(ColorPack colorPack) {
        this.colorPack = colorPack;
    }
}
