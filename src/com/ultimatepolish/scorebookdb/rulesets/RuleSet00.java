package com.ultimatepolish.scorebookdb.rulesets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.ultimatepolish.polishscorebook.R;
import com.ultimatepolish.scorebookdb.Throw;
import com.ultimatepolish.scorebookdb.enums.DeadType;
import com.ultimatepolish.scorebookdb.enums.ThrowResult;
import com.ultimatepolish.scorebookdb.enums.ThrowType;

public class RuleSet00 implements RuleSet {
	/**
	 * Standard rules without coercion. TODO: remove fire rules, add in manual
	 * fire controls?
	 */

	public RuleSet00() {
	}

	public void setThrowType(Throw t, int throwType) {
		t.throwType = throwType;
	}

	public void setThrowResult(Throw t, int throwResult) {
		t.throwResult = throwResult;
	}

	public void setDeadType(Throw t, int deadType) {
		t.deadType = deadType;
	}

	public void setIsTipped(Throw t, boolean isTipped) {
		t.isTipped = isTipped;
	}

	public void setOwnGoals(Throw t, boolean[] ownGoals) {
		t.setOwnGoals(ownGoals);
	}

	public void setDefErrors(Throw t, boolean[] defErrors) {
		t.setDefErrors(defErrors);
	}

	public int[] getScoreDifferentials(Throw t) {
		int[] diffs = { 0, 0 };
		switch (t.throwResult) {
		case ThrowResult.NA:
			if (t.throwType == ThrowType.TRAP) {
				diffs[0] = -1;
			} else if (isOnFire(t)) {
				if (!t.isTipped) {
					switch (t.throwType) {
					case ThrowType.BOTTLE:
						diffs[0] = 3;
						break;
					case ThrowType.CUP:
					case ThrowType.POLE:
						diffs[0] = 2;
						break;
					}
				}
			}
			break;
		case ThrowResult.DROP:
			if (!t.isLineFault) {
				switch (t.throwType) {
				case ThrowType.STRIKE:
					if (!isDropScoreBlocked(t) && t.deadType == DeadType.ALIVE) {
						diffs[0] = 1;
					}
					break;
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!t.isTipped) {
						diffs[0] = 2;
						if (t.isGoaltend) {
							// if goaltended, an extra point for dropping disc
							diffs[0] += 1;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!t.isTipped) {
						diffs[0] = 3;
						if (t.isGoaltend) {
							// if goaltended, an extra point for dropping disc
							diffs[0] += 1;
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		case ThrowResult.CATCH:
			if (!t.isLineFault) {
				switch (t.throwType) {
				case ThrowType.POLE:
				case ThrowType.CUP:
					if (!t.isTipped) {
						if (t.isGoaltend) {
							// if goaltended, award points for hit
							diffs[0] = 2;
						}
					}
					break;
				case ThrowType.BOTTLE:
					if (!t.isTipped) {
						if (t.isGoaltend) {
							// if goaltended, award points for hit
							diffs[0] = 3;
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		case ThrowResult.STALWART:
			if (isStackHit(t)) {
				diffs[1] = 1;
			}
			break;
		case ThrowResult.BROKEN:
			if (!t.isLineFault) {
				diffs[0] = 20;
			}
			break;
		default:
			break;
		}

		// extra points for other modifiers
		if (t.isDrinkHit) {
			diffs[1] -= 1;
		}
		if (t.isGrabbed) {
			diffs[0] += 1;
		}
		if (t.isOffensiveDrinkDropped) {
			diffs[0] -= 1;
		}
		if (t.isOffensivePoleKnocked) {
			diffs[1] += 2;
		}
		if (t.isOffensiveBottleKnocked) {
			diffs[1] += 3;
		}
		if (t.isOffensiveBreakError) {
			diffs[1] += 20;
		}
		if (t.isDefensiveDrinkDropped) {
			diffs[1] -= 1;
		}
		if (t.isDefensivePoleKnocked) {
			diffs[0] += 2;
		}
		if (t.isDefensiveBottleKnocked) {
			diffs[0] += 3;
		}
		if (t.isDefensiveBreakError) {
			diffs[0] += 20;
		}

		return diffs;
	}

	public int[] getFinalScores(Throw t) {
		int[] diff = getScoreDifferentials(t);
		int[] finalScores = { t.initialOffensivePlayerScore + diff[0],
				t.initialDefensivePlayerScore + diff[1] };
		return finalScores;
	}

	public String getSpecialString(Throw t) {
		String s = "";

		if (t.isLineFault) {
			s += "lf.";
		}

		if (t.isDrinkHit) {
			s += "d.";
		}

		if (t.isGoaltend) {
			s += "gt.";
		}

		if (t.isGrabbed) {
			s += "g.";
		}

		int og = 0;
		// technically drink drops are -1 for player instead of +1 for opponent,
		// but subtracting the value for display purposes would be more
		// confusing
		// this is really displaying the resulting differential due to og
		if (t.isOffensiveDrinkDropped) {
			og += 1;
		}
		if (t.isOffensivePoleKnocked) {
			og += 2;
		}
		if (t.isOffensiveBottleKnocked) {
			og += 3;
		}
		if (t.isOffensiveBreakError) {
			og += 20;
		}
		if (og > 0) {
			s += "og" + String.valueOf(og) + '.';
		}

		int err = 0;
		// same as for og
		if (t.isDefensiveDrinkDropped) {
			err += 1;
		}
		if (t.isDefensivePoleKnocked) {
			err += 2;
		}
		if (t.isDefensiveBottleKnocked) {
			err += 3;
		}
		if (t.isDefensiveBreakError) {
			err += 20;
		}
		if (err > 0) {
			s += "e" + String.valueOf(err) + '.';
		}

		if (s.length() == 0) {
			s = "--";
		} else {
			// pop the last '.' off the end of the string
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public void setThrowDrawable(Throw t, ImageView iv) {
		List<Drawable> boxIconLayers = new ArrayList<Drawable>();

		if (!isValid(t, iv.getContext())) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_badthrow));
		}
		switch (t.throwType) {
		case ThrowType.BOTTLE:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_bottle));
			break;
		case ThrowType.CUP:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_cup));
			break;
		case ThrowType.POLE:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_pole));
			break;
		case ThrowType.STRIKE:
			if (t.throwResult == ThrowResult.CATCH || isOnFire(t)) {
				boxIconLayers.add(iv.getResources().getDrawable(
						R.drawable.bxs_under_strike));
			}
			break;
		case ThrowType.BALL_HIGH:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_high));
			break;
		case ThrowType.BALL_RIGHT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_right));
			break;
		case ThrowType.BALL_LOW:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_low));
			break;
		case ThrowType.BALL_LEFT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_left));
			break;
		case ThrowType.SHORT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_short));
			break;
		case ThrowType.TRAP:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_trap));
			break;
		case ThrowType.TRAP_REDEEMED:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_trap));
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_drop));
			break;
		case ThrowType.NOT_THROWN:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_notthrown));
			break;
		case ThrowType.FIRED_ON:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_under_firedon));
			break;
		default:
			boxIconLayers.add(iv.getResources()
					.getDrawable(R.drawable.bxs_oops));
			break;
		}

		switch (t.throwResult) {
		case ThrowResult.DROP:
			if (t.throwType != ThrowType.BALL_HIGH
					&& t.throwType != ThrowType.BALL_RIGHT
					&& t.throwType != ThrowType.BALL_LOW
					&& t.throwType != ThrowType.BALL_LEFT)
				boxIconLayers.add(iv.getResources().getDrawable(
						R.drawable.bxs_over_drop));
			break;
		case ThrowResult.STALWART:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_stalwart));
			break;
		case ThrowResult.BROKEN:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_break));
			break;
		}

		switch (t.deadType) {
		case DeadType.HIGH:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_high));
			break;
		case DeadType.RIGHT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_right));
			break;
		case DeadType.LOW:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_low));
			break;
		case DeadType.LEFT:
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_dead_left));
			break;
		}

		if (isOnFire(t)) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_fire));
		}
		if (t.isTipped) {
			boxIconLayers.add(iv.getResources().getDrawable(
					R.drawable.bxs_over_tipped));
		}

		iv.setImageDrawable(new LayerDrawable(boxIconLayers
				.toArray(new Drawable[0])));
	}

	public boolean isDropScoreBlocked(Throw t) {
		boolean isBlocked = false;
		int oScore = t.initialOffensivePlayerScore;
		int dScore = t.initialDefensivePlayerScore;

		if (oScore < 10 && dScore < 10) {
			isBlocked = false;
		} else if (oScore >= 10 && dScore < oScore && dScore < 10) {
			isBlocked = true;
		} else if (oScore >= 10 && dScore >= 10 && oScore > dScore) {
			isBlocked = true;
		}

		return isBlocked;
	}

	public boolean isFiredOn(Throw t) {
		if (t.defenseFireCount >= 3) {
			assert t.offenseFireCount < 3 : "should not be possible to have both players with fire counts >=3";
			return true;
		} else {
			return false;
		}
	}

	public boolean isOnFire(Throw t) {
		if (t.offenseFireCount > 3) {
			assert t.defenseFireCount < 3 : "should not be possible to have both players with fire counts >=3";
			return true;
		} else {
			return false;
		}
	}

	public boolean stokesOffensiveFire(Throw t) {
		// you didn't quench yourself, hit the stack, your opponent didn't
		// stalwart
		boolean stokes = (!quenchesOffensiveFire(t) && isStackHit(t) && !(t.throwResult == ThrowResult.STALWART));
		return stokes;
	}

	public boolean quenchesOffensiveFire(Throw t) {
		boolean quenches = isOffensiveError(t)
				|| (t.deadType != DeadType.ALIVE);
		return quenches;
	}

	public boolean quenchesDefensiveFire(Throw t) {
		// offense hit the stack and defense failed to defend, or offense was on
		// fire

		boolean defenseFailed = (t.throwResult == ThrowResult.DROP)
				|| (t.throwResult == ThrowResult.BROKEN)
				|| (isOnFire(t) && !t.isTipped);

		boolean quenches = isStackHit(t) && defenseFailed;

		// defensive error will also quench
		quenches = quenches || isDefensiveError(t);

		return quenches;
	}

	public void setFireCounts(Throw t, Throw previousThrow) {
		int oldOffenseCount = previousThrow.defenseFireCount;
		int oldDefenseCount = previousThrow.offenseFireCount;
		int newOffenseCount = oldOffenseCount;
		int newDefenseCount = oldDefenseCount;

		// previous throw, opponent was or went on fire
		if (oldDefenseCount >= 3) {
			newOffenseCount = oldOffenseCount;
			newDefenseCount = oldDefenseCount;
		}
		// opponent not on fire last throw so we have a chance to change things
		else {
			if (oldOffenseCount == 3) {
				newOffenseCount++;
			} else if (stokesOffensiveFire(t)) {
				newOffenseCount++;
			} else {
				newOffenseCount = 0;
			}
			if (quenchesDefensiveFire(t)) {
				newDefenseCount = 0;
			}
		}

		t.offenseFireCount = newOffenseCount;
		t.defenseFireCount = newDefenseCount;

		Log.i("Throw.setFireCounts()", "o=" + newOffenseCount + ", d="
				+ newDefenseCount);
	}

	public boolean isValid(Throw t, Context context) {
		boolean valid = isValid(t);
		if (!valid) {
			Toast.makeText(context, t.invalidMessage, Toast.LENGTH_LONG).show();
		}
		return valid;
	}

	public boolean isValid(Throw t) {
		boolean valid = true;
		t.invalidMessage = "(gameId=%d, throwIdx=%d)";
		t.invalidMessage = String.format(t.invalidMessage, t.getId(),
				t.throwIdx);
		if (isOnFire(t)) {
			if (t.throwResult != ThrowResult.NA
					&& t.throwResult != ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "OnFire => ThrowResult == NA or Broken. ";
			}
		}
		switch (t.throwType) {
		case ThrowType.BALL_HIGH:
		case ThrowType.BALL_RIGHT:
		case ThrowType.BALL_LOW:
		case ThrowType.BALL_LEFT:
		case ThrowType.STRIKE:
			if (t.deadType != DeadType.ALIVE && t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "drinkHit => live throw";
			} else if (t.isGoaltend || t.isTipped) {
				valid = false;
				t.invalidMessage += "Goaltending || tipped => not SHRLL. ";
			}

			switch (t.throwResult) {
			case ThrowResult.DROP:
			case ThrowResult.CATCH:
				break;
			default:
				if (!isOnFire(t)) {
					valid = false;
					t.invalidMessage += "SHRLL => drop or catch. ";
				}
				break;
			}

			break;
		case ThrowType.POLE:
		case ThrowType.CUP:
		case ThrowType.BOTTLE:
			if (t.isGrabbed) {
				valid = false;
				t.invalidMessage += "grabbing a PCB hit should be marked goaltending. ";
			}
			if (t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "drink hit <=>  not PCB hit. ";
			}
			if (t.isTipped && t.isGoaltend) {
				valid = false;
				t.invalidMessage += "PCB throws cant be tipped and goaltended simultaneously. ";
			}
			if (t.deadType != DeadType.ALIVE && t.isGoaltend) {
				valid = false;
				t.invalidMessage += "Dead <=> not goaltended. ";
			}
			if (t.throwResult == ThrowResult.NA && !isOnFire(t)) {
				valid = false;
				t.invalidMessage += "PCB and not onFire => not NA result. ";
			}
			if (t.isTipped && t.throwResult == ThrowResult.STALWART) {
				valid = false;
				t.invalidMessage += "stalwart <=> not tip. ";
			}
			if (t.isGoaltend && t.throwResult == ThrowResult.STALWART) {
				valid = false;
				t.invalidMessage += "stalwart <=> not goaltend. ";
			}
			if (t.isTipped && t.throwResult == ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "tip <=> not broken. ";
			}
			if (t.isGoaltend && t.throwResult == ThrowResult.BROKEN) {
				valid = false;
				t.invalidMessage += "goaltend <=> not broken. ";
			}

			break;

		case ThrowType.TRAP:
		case ThrowType.TRAP_REDEEMED:
		case ThrowType.SHORT:
			if (t.isGoaltend || t.isTipped || t.isDrinkHit) {
				valid = false;
				t.invalidMessage += "Goaltend or tip or drinkHit => not trap and not short. ";
			} else if (t.throwResult != ThrowResult.NA) {
				valid = false;
				t.invalidMessage += "Trap or short => NA result. ";
			}

			break;
		case ThrowType.FIRED_ON:
			// fired_on is a dummy throw, so modifiers dont count and result
			// must be NA
			// errors could potentially happen while returning the disc, so
			// those are allowed
			if (t.isLineFault || t.isGoaltend || t.isTipped || t.isDrinkHit
					|| t.deadType != DeadType.ALIVE) {
				valid = false;
				t.invalidMessage += "Fired-on cannot be modified. ";
			} else if (t.throwResult != ThrowResult.NA) {
				valid = false;
				t.invalidMessage += "Fired-on => NA result.";
			}

			break;
		}
		// logd("isValid",invalidMessage);
		return valid;
	}

	public boolean isStackHit(Throw t) {
		return (t.throwType == ThrowType.POLE || t.throwType == ThrowType.CUP || t.throwType == ThrowType.BOTTLE);
	}

	public boolean isOffensiveError(Throw t) {
		return (t.isOffensiveBottleKnocked || t.isOffensivePoleKnocked
				|| t.isOffensivePoleKnocked || t.isOffensiveBreakError
				|| t.isOffensiveDrinkDropped || t.isLineFault);
	}

	public boolean isDefensiveError(Throw t) {
		return (t.isDefensiveBottleKnocked || t.isDefensivePoleKnocked
				|| t.isDefensivePoleKnocked || t.isDefensiveBreakError
				|| t.isDefensiveDrinkDropped || t.isDrinkHit);
	}
}