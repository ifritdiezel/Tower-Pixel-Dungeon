/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.towerpixel.towerpixeldungeon.items.bombs;

import com.towerpixel.towerpixeldungeon.Assets;
import com.towerpixel.towerpixeldungeon.Dungeon;
import com.towerpixel.towerpixeldungeon.actors.blobs.Blob;
import com.towerpixel.towerpixeldungeon.actors.blobs.Fire;
import com.towerpixel.towerpixeldungeon.effects.CellEmitter;
import com.towerpixel.towerpixeldungeon.effects.particles.FlameParticle;
import com.towerpixel.towerpixeldungeon.scenes.GameScene;
import com.towerpixel.towerpixeldungeon.sprites.ItemSpriteSheet;
import com.towerpixel.towerpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class Firebomb extends Bomb {
	
	{
		image = ItemSpriteSheet.FIRE_BOMB;
	}
	
	@Override
	public void explode(int cell) {
		super.explode(cell);
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 2 );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				if (Dungeon.level.pit[i])
					GameScene.add(Blob.seed(i, 2, Fire.class));
				else
					GameScene.add(Blob.seed(i, 10, Fire.class));
				CellEmitter.get(i).burst(FlameParticle.FACTORY, 5);
			}
		}
		Sample.INSTANCE.play(Assets.Sounds.BURNING);
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (20 + Dungeon.scalingDepth()*5);
	}
}
