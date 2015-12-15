package com.crystalline.dodge;
import com.badlogic.gdx.graphics.*;

public class ColorSet
{
	public Color bg, ground, player, obstacles, shadow, text;
	public ColorSet()
	{
		bg = new Color(0.7f, 0.2f, 0.0f, 1);
		ground = Color.RED;
		player = new Color(0,0.3f, 0.6f,1);
		shadow = new Color(0.6f,0.15f,0,1);
		obstacles = Color.GRAY;
		text = Color.WHITE;
	}
}
