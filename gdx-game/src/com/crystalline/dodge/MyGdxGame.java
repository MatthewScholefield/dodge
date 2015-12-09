package com.crystalline.dodge;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

import java.util.*;
import org.apache.http.cookie.*;

public class MyGdxGame implements ApplicationListener
{
	private enum GameState
	{
		PLAY,
		GAME_OVER
	}
	private enum ColorScheme
	{
		DEFAULT,
		HIGH_CONTRAST
	}
	private BitmapFont font;
	private OrthographicCamera cam;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private Rectangle player;
	private Rectangle floor;
	private ArrayList<Rectangle> obstacles;
	private Random rand;
	private Vector2 pVel;
	private int score;
	private ColorSet colors;
	private int framesHeld;
	private ColorScheme colorScheme;
	private float speed;

	private GameState state;

	private static final int FLOOR_HEIGHT = 200, FONT_PX = 15;

	@Override
	public void create()
	{
		cam = new OrthographicCamera();
		shapes = new ShapeRenderer();
		batch = new SpriteBatch();
		floor = new Rectangle();
		player = new Rectangle();
		obstacles = new ArrayList<Rectangle>();
		rand = new Random();
		player.y = FLOOR_HEIGHT;
		pVel = new Vector2();
		
		font = new BitmapFont();
		font.setScale(3f);
		//FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
		/*FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 20;
		font = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose();
		//*/

		state = GameState.PLAY;
		score = 0;
		colors = new ColorSet();
		framesHeld = 0;
		colorScheme = ColorScheme.DEFAULT;
		speed = 600f;
	}
	
	private void printCenter(float y, String text)
	{
		float size = font.getBounds(text).width;
		font.draw(batch, text, cam.position.x-size/2f,y);
	}
	
	private void setColorScheme(ColorScheme scheme)
	{
		switch(scheme)
		{
			case DEFAULT:
				colors.bg = new Color(0.7f, 0.2f, 0.0f, 1);
				colors.ground = Color.RED;
				colors.player = new Color(0,0.3f, 0.6f,1);
				colors.shadow = new Color(0.6f,0.15f,0,1);
				colors.obstacles = Color.GRAY;
				break;
			case HIGH_CONTRAST:
				colors.bg = Color.WHITE;
				colors.ground = Color.DARK_GRAY;
				colors.player = Color.BLACK;
				colors.shadow = Color.LIGHT_GRAY;
				colors.obstacles = Color.BLACK;
				break;
		}
		colorScheme = scheme;
	}
	
	@Override
	public void render()
	{
		Gdx.gl.glClearColor(colors.bg.r, colors.bg.g, colors.bg.b, colors.bg.a);
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		cam.update();

		shapes.setProjectionMatrix(cam.combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.setColor(colors.shadow);
		shapes.triangle(player.x+player.width,player.y+player.height,player.x+2000,player.y-2000,player.x,player.y);
		for (Rectangle rect:obstacles)
			shapes.triangle(rect.x+rect.width,rect.y+rect.height,rect.x+2000,rect.y-2000,rect.x,rect.y);
		
		shapes.setColor(colors.ground);
		shapes.rect(floor.x, floor.y, floor.width, floor.height);
		shapes.setColor(colors.player);
		shapes.rect(player.x, player.y, player.width, player.height);
		
		shapes.setColor(colors.obstacles);
		for (Rectangle rect:obstacles)
			shapes.rect(rect.x, rect.y, rect.width, rect.height);

		shapes.end();
		
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		if (Gdx.input.isTouched()&&++framesHeld>120)
		{
			framesHeld=0;
			if (colorScheme==ColorScheme.DEFAULT)
				setColorScheme(ColorScheme.HIGH_CONTRAST);
			else
				setColorScheme(ColorScheme.DEFAULT);
		}
		else if (!Gdx.input.isTouched())
			framesHeld=0;
		switch (state)
		{
			case PLAY:
				{
					if (Gdx.input.isTouched() && player.y == FLOOR_HEIGHT)
						pVel.y = 10*speed/600f;
					if (Gdx.input.justTouched()&&player.y>FLOOR_HEIGHT)
						pVel.y=-10*speed/600f;

					player.y += pVel.y;
					if (player.y > FLOOR_HEIGHT)
						pVel.y -= 20 * Gdx.graphics.getDeltaTime()*Math.pow((speed/600f),2);
					if (player.y < FLOOR_HEIGHT)
					{
						player.y = FLOOR_HEIGHT;
						pVel.y = 0;
					}
					cam.position.x += speed * Gdx.graphics.getDeltaTime();
					++speed;

					floor.set(cam.position.x - cam.viewportWidth / 2, cam.position.y - cam.viewportHeight / 2, cam.viewportWidth, FLOOR_HEIGHT);
					player.set(cam.position.x - cam.viewportWidth / 2 + 100, player.y, 30, 100);

					if (rand.nextInt(50) == 1 && obstacles.size() < 5)
						obstacles.add(new Rectangle(cam.position.x + cam.viewportWidth / 2, floor.y + floor.height, 30, 60));

					for (int i=0; i < obstacles.size();i++)
					{
						if (obstacles.get(i).x + obstacles.get(i).width < cam.position.x - cam.viewportWidth / 2)
						{
							obstacles.remove(i);
							score++;
						}
						else if (player.overlaps(obstacles.get(i)))
						{
							state = GameState.GAME_OVER;
						}
					}
					printCenter(FLOOR_HEIGHT / 2, "Score: "+score);
				}
				break;
			case GAME_OVER:
				{
					if (Gdx.input.justTouched())
					{
						state = GameState.PLAY;
						score = 0;
						cam.position.x = 0;
						obstacles.clear();
						player.y = FLOOR_HEIGHT;
						speed = 600f;
					}
					printCenter(cam.position.y + 50, "Game Over!");
					printCenter(cam.position.y-50, "Score: " + score);
				}
				break;
		}
		batch.end();
	}

	private void configureCamera()
	{
		float size = 1600;
		if (Gdx.graphics.getHeight() < Gdx.graphics.getWidth())
			cam.setToOrtho(false, size, size * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
		else
			cam.setToOrtho(false, size * Gdx.graphics.getWidth() / Gdx.graphics.getHeight(), size);
	}

	@Override
	public void dispose()
	{
		shapes.dispose();
		font.dispose();
	}

	@Override
	public void resize(int width, int height)
	{
		configureCamera();
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}
