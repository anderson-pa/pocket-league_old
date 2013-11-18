package com.ultimatepolish.scorebookdb.rulesets;

import android.util.Log;

import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.enums.DeadType;
import com.ultimatepolish.scorebookdb.enums.ThrowResult;
import com.ultimatepolish.scorebookdb.enums.ThrowType;

public class RuleSet01 extends RuleSet00 {
	/**
	 * Standard rules with coercion and autofire.
	 */

	public RuleSet01() {
	}

	@Override
	public int getId() {
		return 1;
	}

	@Override
	public String getDescription() {
		return "Standard ruleset with coercion and autofire";
	}

	@Override
	public void setThrowType(Throw t, int throwType) {
		if (t.defenseFireCount >= 3) {
			t.throwType = ThrowType.FIRED_ON;
			setThrowResult(t, ThrowResult.NA);
			setDeadType(t, DeadType.ALIVE);
		} else {
			t.throwType = throwType;

			if (t.offenseFireCount >= 3) {
				setThrowResult(t, ThrowResult.NA);
			} else {
				switch (throwType) {
				case ThrowType.BALL_HIGH:
				case ThrowType.BALL_RIGHT:
				case ThrowType.BALL_LOW:
				case ThrowType.BALL_LEFT:
				case ThrowType.STRIKE:
					if (t.throwResult != ThrowResult.DROP
							&& t.throwResult != ThrowResult.CATCH) {
						setThrowResult(t, ThrowResult.CATCH);
					}
					break;
				case ThrowType.SHORT:
					if (t.deadType == DeadType.ALIVE) {
						setDeadType(t, DeadType.LOW);
					}
					setThrowResult(t, ThrowResult.NA);
					break;
				case ThrowType.TRAP:
					if (t.deadType == DeadType.ALIVE) {
						setDeadType(t, DeadType.HIGH);
					}
					setThrowResult(t, ThrowResult.NA);
					break;
				case ThrowType.TRAP_REDEEMED:
					if (t.deadType == DeadType.ALIVE) {
						setDeadType(t, DeadType.HIGH);
					}
					if (t.throwResult != ThrowResult.BROKEN) {
						setThrowResult(t, ThrowResult.NA);
					}
					break;
				}
			}
		}
	}

	private boolean stokesOffensiveFire(Throw t) {
		// quench caused by own goal or throwing dead
		boolean quenches = isOffensiveError(t)
				|| (t.deadType != DeadType.ALIVE);

		// will stoke if all conditions are met:
		// (a) not quenched, (b) hits the stack, (c) not stalwart
		boolean stokes = !quenches && isStackHit(t)
				&& t.throwResult != ThrowResult.STALWART;

		return stokes;
	}

	private boolean quenchesDefensiveFire(Throw t) {

		boolean fireHit = isOnFire(t) && isStackHit(t);

		boolean defFail = (t.throwResult == ThrowResult.BROKEN)
				|| (t.deadType == DeadType.ALIVE && t.throwResult == ThrowResult.DROP);

		boolean quenches = isStackHit(t) && defFail;

		// defensive error will also quench
		quenches = quenches || isDefensiveError(t);

		return false;
	}

	@Override
	public void setFireCounts(Throw t, Throw previousThrow) {
		int prevOffCount = previousThrow.offenseFireCount;
		int prevDefCount = previousThrow.defenseFireCount;

		if (stokesOffensiveFire(previousThrow)) {
			prevOffCount++;
		} else {
			prevOffCount = 0;
		}
		if (quenchesDefensiveFire(t)) {
			prevDefCount = 0;
		}

		t.offenseFireCount = prevDefCount;
		t.defenseFireCount = prevOffCount;

		Log.i("Throw.setFireCounts()", "o=" + prevDefCount + ", d="
				+ prevOffCount);
	}
}