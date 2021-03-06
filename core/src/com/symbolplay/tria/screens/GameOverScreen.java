package com.symbolplay.tria.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.symbolplay.tria.GameContainer;
import com.symbolplay.tria.game.background.GameBackground;
import com.symbolplay.tria.net.TriaServiceAccessor;
import com.symbolplay.tria.screens.general.ChangeParamKeys;
import com.symbolplay.tria.screens.general.GameScreenUtils;

public final class GameOverScreen extends ScreenBase {
    
    private static final int NAME_MAX_LENGTH = 20;
    
    private final TriaServiceAccessor triaServiceAccessor;
    
    private final GameBackground background;
    
    private Table content;
    private TextField nameTextField;
    
    private int score;
    
    public GameOverScreen(GameContainer game) {
        super(game);
        
        guiStage.addListener(getStageInputListener(this));
        
        triaServiceAccessor = game.getTriaServiceAccessor();
        
        background = new GameBackground(assetManager);
        
        float offsetFromTop = GameScreenUtils.addTitleLabel("GAME OVER", guiSkin, guiStage);
        content = GameScreenUtils.addScrollTable(offsetFromTop, 140.0f, guiSkin, guiStage);
        createControls();
        
        TextButton continueButton = new TextButton("CONTINUE", guiSkin, "font40");
        continueButton.setBounds(75.0f, 40.0f, 300.0f, 80.0f);
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                enterHighScore();
            }
        });
        guiStage.addActor(continueButton);
        
        score = 0;
    }
    
    @Override
    public void show(ObjectMap<String, Object> changeParams) {
        super.show(changeParams);
        
        score = (Integer) changeParams.get(ChangeParamKeys.SCORE);
        setContent(null);
    }
    
    @Override
    public void renderImpl() {
        batch.setProjectionMatrix(cameraData.getGameAreaMatrix());
        batch.begin();
        background.render(batch);
        batch.end();
    }
    
    private void createControls() {
        nameTextField = new TextField("", guiSkin,  "entername");
    }
    
    private void setContent(String customMessage) {
            
        content.clear();
        content.center().top();
        
        content.row().padTop(40.0f);
        
        String gameOverText = "Score: " + score;
        Label gameOverLabel = new Label(gameOverText, guiSkin, "font40");
        gameOverLabel.setAlignment(Align.center);
        content.add(gameOverLabel);
        
        content.row().height(120.0f).padTop(40.0f);
        
        int scorePlacementIndex = gameData.getAchievementsData().getScorePlacementIndex(score);
        
        if (scorePlacementIndex >= 0) {
            String placementText = String.format("You place %s\non high score list.", getOrdinalString(scorePlacementIndex + 1));
            Label placementLabel = new Label(placementText, guiSkin, "font40");
            placementLabel.setAlignment(Align.center);
            content.add(placementLabel).bottom().fillY();
        } else {
            content.add().fillY();
        }
        
        content.row().height(56.0f).padTop(10.0f).padBottom(10.0f);
        
        if (scorePlacementIndex >= 0) {
            String nameText = gameData.getGamePreferences().getLastEnteredHighScoreName();
            nameTextField.setText(nameText);
            nameTextField.setMaxLength(NAME_MAX_LENGTH);
            content.add(nameTextField).width(360.0f).fillY();
        } else {
            content.add().fillY();
        }
        
        content.row().padTop(10.0f);
    }
    
    private static String getOrdinalString(int num) {
        if (num <= 0) {
            return String.valueOf(num);
        }
        
        switch (num % 100) {
            case 11:
            case 12:
            case 13:
                return num + "th";
        }
        
        switch (num % 10) {
            case 1:
                return num + "st";
            case 2:
                return num + "nd";
            case 3:
                return num + "rd";
            default:
                return num + "th";
        }
    }
    
    private void enterHighScore() {
        String name = nameTextField.getText();
        long time = System.currentTimeMillis();
        
        gameData.getGamePreferences().setLastEnteredHighScoreName(name);
        
        boolean isHighScore = gameData.getAchievementsData().isHighScore(score);
        if (isHighScore) {
            gameData.getAchievementsData().insertHighScore(name, score, time);
            game.changeScreen(GameContainer.HIGH_SCORE_SCREEN_NAME);
        } else {
            game.changeScreen(GameContainer.MAIN_MENU_SCREEN_NAME);
        }
        
        triaServiceAccessor.publishGlobalScore(name, score, game.getAppVersion());
    }
    
    private static InputListener getStageInputListener(final GameOverScreen loseGameScreen) {
        return new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
                    loseGameScreen.game.changeScreen(GameContainer.MAIN_MENU_SCREEN_NAME);
                    return true;
                }
                
                return false;
            }
        };
    }
}
