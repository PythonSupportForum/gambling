package com.letsgogambling.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import sun.tools.jconsole.Tab;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Stage stage;
    private Table table;
    private SpriteBatch batch;
    private Texture tilotaler;
    private Texture header;
    private Texture logo;


    private Label ttKonto;



    @Override
    public void create() {

        camera = new OrthographicCamera();
        viewport=new ExtendViewport(800, 480);
        viewport.apply();
        camera.position.set((float) 800 /2, (float) 480 /2, 0);
        camera.update();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        table = new Table();
        table.setFillParent(true);



        tilotaler = new Texture("assets/tilotaler.png");
        header = new Texture("assets/header.png");
        logo = new Texture("assets/Logo.png");
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        ttKonto=new Label("moin hallo moin", skin);
        
        

        
        
        
        batch = new SpriteBatch();
    }


    public void draw(){
        batch.begin();
        //draw content on screen
        batch.draw(header, 0, 0,50,50);
        batch.draw(logo,0,0);
        





        batch.end();
    }
    
    
    
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        stage.act(delta);
        ScreenUtils.clear(Color.WHITE);
        //Viewport, fenstergröße und so
        // Aktualisiere den Viewport, falls sich die Fenstergröße geändert hat
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();


        stage.draw();
;
    }



    public void logic(){


    }







    @Override
    public void dispose() {
        batch.dispose();
        tilotaler.dispose();
        header.dispose();
        logo.dispose();
    }

    @Override
    public void resize (int width, int height) {
        // See below for what true means.
        stage.getViewport().update(width, height, true);












}


