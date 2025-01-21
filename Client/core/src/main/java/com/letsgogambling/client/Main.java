package com.letsgogambling.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private SpriteBatch batch;
    private Texture tilotaler;
    private AssetManager manager;


    @Override
    public void create() {

        camera = new OrthographicCamera();
        viewport=new ExtendViewport(800, 480);
        viewport.apply();
        camera.position.set((float) 800 /2, (float) 480 /2, 0);
        camera.update();

        manager=new AssetManager();
        manager.load("tilotaler.png", Texture.class);
        tilotaler = new Texture("tilotaler.png");
        batch = new SpriteBatch();



    }

    @Override
    public void render() {
        if(manager.update()){
            //was kommt nach dem laden
        }
        float loadingProgress = manager.getProgress();



        draw();
;
    }



    public void logic(){


    }





    public void draw(){
        ScreenUtils.clear(Color.WHITE);

        //Viewport, fenstergröße und so
        // Aktualisiere den Viewport, falls sich die Fenstergröße geändert hat
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();



        batch.begin();
        //draw content on screen
        batch.draw(tilotaler, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }







    @Override
    public void dispose() {
        batch.dispose();
        tilotaler.dispose();
    }}


