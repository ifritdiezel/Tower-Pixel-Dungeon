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

package com.towerpixel.towerpixeldungeon.items.quest;

import com.towerpixel.towerpixeldungeon.Assets;
import com.towerpixel.towerpixeldungeon.Dungeon;
import com.towerpixel.towerpixeldungeon.actors.Actor;
import com.towerpixel.towerpixeldungeon.actors.buffs.Buff;
import com.towerpixel.towerpixeldungeon.actors.hero.Hero;
import com.towerpixel.towerpixeldungeon.actors.mobs.Mob;
import com.towerpixel.towerpixeldungeon.actors.mobs.Wraith;
import com.towerpixel.towerpixeldungeon.items.Item;
import com.towerpixel.towerpixeldungeon.messages.Messages;
import com.towerpixel.towerpixeldungeon.sprites.ItemSpriteSheet;
import com.towerpixel.towerpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CorpseDust extends Item {
	
	{
		image = ItemSpriteSheet.DUST;
		
		cursed = true;
		cursedKnown = true;
		
		unique = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		return new ArrayList<>(); //yup, no dropping this one
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (super.doPickUp(hero, pos)){
			GLog.n( Messages.get( this, "chill") );
			Buff.affect(hero, DustGhostSpawner.class);
			return true;
		}
		return false;
	}

	@Override
	protected void onDetach() {
		DustGhostSpawner spawner = Dungeon.hero.buff(DustGhostSpawner.class);
		if (spawner != null){
			spawner.dispel();
		}
	}

	public static class DustGhostSpawner extends Buff {

		int spawnPower = 0;

		{
			//not cleansed by reviving, but does check to ensure the dust is still present
			revivePersists = true;
		}

		@Override
		public boolean act() {
			if (target instanceof Hero && ((Hero) target).belongings.getItem(CorpseDust.class) == null){
				spawnPower = 0;
				spend(TICK);
				return true;
			}

			spawnPower++;
			int wraiths = 1; //we include the wraith we're trying to spawn
			for (Mob mob : Dungeon.level.mobs){
				if (mob instanceof Wraith){
					wraiths++;
				}
			}

			int powerNeeded = Math.min(25, wraiths*wraiths);

			if (powerNeeded <= spawnPower){
				spawnPower -= powerNeeded;
				int pos = 0;
				//FIXME this seems like old bad code (why not more checks at least?) but corpse dust may be balanced around it
				int tries = 20;
				do{
					pos = Random.Int(Dungeon.level.length());
					tries --;
				} while (tries > 0 && (!Dungeon.level.heroFOV[pos] || Dungeon.level.solid[pos] || Actor.findChar( pos ) != null));
				if (tries > 0) {
					Wraith.spawnAt(pos);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}
			}

			spend(TICK);
			return true;
		}

		public void dispel(){
			detach();
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if (mob instanceof Wraith){
					mob.die(null);
				}
			}
		}

		private static String SPAWNPOWER = "spawnpower";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( SPAWNPOWER, spawnPower );
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			spawnPower = bundle.getInt( SPAWNPOWER );
		}
	}

	@Override
	public int value() {
		return 100;
	}
}
