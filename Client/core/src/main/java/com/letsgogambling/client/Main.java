package com.letsgogambling.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.awt.*;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    FitViewport viewport;

    @Override
    public void create() {
       image = new Texture("tilotaler.png");
       batch = new SpriteBatch();
        viewport = new FitViewport(8, 5);


    }

    @Override
    public void render() {
        draw();
        resize(1280, 720);
    }



    public void logic(){
        //logic for connecting to the server and fetching data

    }





    public void draw(){
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);



        batch.begin();
        //draw content on screen
        batch.draw(image, 140, 210);
        batch.end();
    }













    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

}
