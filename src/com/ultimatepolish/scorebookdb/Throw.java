package com.ultimatepolish.scorebookdb;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.ultimatepolish.polishscorebook.R;

public class Throw implements Comparable<Throw>{
	public static final String GAME_ID = "gameId";
	public static final String THROW_INDEX = "throwIdx";

	@DatabaseField(generatedId=true)
	private long id;
	
	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private int throwIdx;

	@DatabaseField(canBeNull=false, uniqueCombo=true)
	private long gameId;

	@DatabaseField(canBeNull=false)
	private long offensivePlayerId;

	@DatabaseField(canBeNull=false)
	private long defensivePlayerId;
	
	@DatabaseField(canBeNull=false)
	private Date timestamp;

	@DatabaseField(canBeNull=false)
	private int throwType;

	@DatabaseField(canBeNull=false)
	private int throwResult;

	@DatabaseField
	private int deadType = 0;
	
	@DatabaseField
	public boolean isTipped = false;
	
	@DatabaseField
	public boolean isGoaltend = false;
		
	@DatabaseField
	public boolean isDrinkHit = false;
	
	@DatabaseField
	public boolean isOnFire = false;

	@DatabaseField
	public boolean isLineFault = false;
	
	@DatabaseField
	public boolean isOffensiveDrinkDropped = false;
	
	@DatabaseField
	public boolean isOffensivePoleKnocked = false;
	
	@DatabaseField
	public boolean isOffensiveBottleKnocked = false;
	
	@DatabaseField
	public boolean isOffensiveBreakError = false;
	
	@DatabaseField
	public boolean isDefensiveDrinkDropped = false;
	
	@DatabaseField
	public boolean isDefensivePoleKnocked = false;
	
	@DatabaseField
	public boolean isDefensiveBottleKnocked = false;
	
	@DatabaseField
	public boolean isDefensiveBreakError = false;
	
	@DatabaseField
	private int initialOffensivePlayerScore = 0;

	@DatabaseField
	private int initialDefensivePlayerScore = 0;
	
	Throw(){}
	
	public Throw(int throwIdx, long gameId, long offensivePlayerId, long defensivePlayerId, Date timestamp,
			int throwType, int throwResult) {
		super();
		this.throwIdx = throwIdx;
		this.gameId = gameId;
		this.offensivePlayerId = offensivePlayerId;
		this.defensivePlayerId = defensivePlayerId;
		this.timestamp = timestamp;
		this.throwType = throwType;
		this.throwResult = throwResult;
	}
	
	public Throw(int throwIdx, long gameId, long offensivePlayerId, long defensivePlayerId, Date timestamp) {
		super();
		this.throwIdx = throwIdx;
		this.gameId = gameId;
		this.offensivePlayerId = offensivePlayerId;
		this.defensivePlayerId = defensivePlayerId;
		this.timestamp = timestamp;
        this.throwType = ThrowType.NOT_THROWN;
	}
	
    public static Dao<Throw, Long> getDao(Context context){
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Throw, Long> d = null;
                try {
                        d = helper.getThrowDao();
                }
                catch (SQLException e){
                        throw new RuntimeException("Couldn't get dao: ", e);
                }
		return d;
	}
	public HashMap<String, Object> getQueryMap(){
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(Throw.THROW_INDEX, getThrowIdx());
        m.put(Throw.GAME_ID, getGameId());
        return m;
}
	public void setInitialScores(Throw previousThrow){
		int[] scores = previousThrow.getFinalScores();
		setInitialDefensivePlayerScore(scores[0]);
		setInitialOffensivePlayerScore(scores[1]);
	}
	public void setInitialScores(){
		setInitialDefensivePlayerScore(0);
		setInitialOffensivePlayerScore(0);
	}
	public int[] getFinalScores(){
		int[] diff = getScoreDifferentials();
		int[] finalScores = {initialOffensivePlayerScore + diff[0], 
				initialDefensivePlayerScore + diff [1]};
		return finalScores;
	}
	private int[] getScoreDifferentials(){
		int[] diffs = {0,0};
		switch (throwResult){
		case ThrowResult.NA:
			if (throwType == ThrowType.TRAP) {
				diffs[0] = -1;
			}
			break;
		case ThrowResult.DROP:
			if (!isLineFault) {
				switch (throwType){
					case ThrowType.STRIKE:
						if (!isDropScoreBlocked() && deadType == 0){
							diffs[0] = 1;
						}
						break;
					case ThrowType.POLE: 
					case ThrowType.CUP:
						if (!isTipped) {
							diffs[0] = 2;
							if (isGoaltend) {
								// if goaltended, an extra point for dropping disc
								diffs[0] += 1;
							}
						}
						break;
					case ThrowType.BOTTLE:
						if (!isTipped) {
							diffs[0] = 3;
							if (isGoaltend) {
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
			if (!isLineFault) {
				switch (throwType){
					case ThrowType.POLE: 
					case ThrowType.CUP:
						if (!isTipped) {
							if (isGoaltend) {
								// if goaltended, award points for hit
								diffs[0] = 2;
							}
						}
						break;
					case ThrowType.BOTTLE:
						if (!isTipped) {
							if (isGoaltend) {
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
			diffs[1] = 1;
			break;
		case ThrowResult.BROKEN:
			if (!isLineFault) {
				diffs[0] = 20;
			}
			break;
		default:
			break;
		}
		
		// extra points for other modifiers
		if (isDrinkHit){
			diffs[1] -= 1;
		}
		if (isOffensiveDrinkDropped){
			diffs[0] -= 1;
		}
		if (isOffensivePoleKnocked){
			diffs[1] += 2;
		}
		if (isOffensiveBottleKnocked){
			diffs[1] += 3;
		}
		if (isOffensiveBreakError){
			diffs[1] += 20;
		}
		if (isDefensiveDrinkDropped){
			diffs[1] -= 1;
		}
		if (isDefensivePoleKnocked){
			diffs[0] += 2;
		}
		if (isDefensiveBottleKnocked){
			diffs[0] += 3;
		}
		if (isDefensiveBreakError){
			diffs[0] += 20;
		}
		
		return diffs;
	}
	private boolean isDropScoreBlocked(){
		boolean isBlocked = false;
		int oScore = initialOffensivePlayerScore;
		int dScore = initialDefensivePlayerScore;
		if (oScore>=10 && dScore<oScore){
			isBlocked = true;
		}
		return isBlocked;
	}
	public String getSpecialString(){
		String s = "";
		if(isDefensiveError){
			s+="e"+String.valueOf(errorScore);
		}
		if (isOwnGoal){
			s+="o"+String.valueOf(ownGoalScore);
		}
		if (isGoaltend){
			s+="g"+String.valueOf(goaltendScore);
		}
//		if (isOnFire){
//			s+="f";
//		}
//		if (isFiredOn){
//			s+="F";
//		}
		if (isBroken){
			s+="*";
		}
//		if (isTrap){
//			s+="^";
//		}
		if (isDead){
			s+="v";
		}
		if (isDefensiveDrinkDropped){
			s+="d";
		}
		if (isDrinkHit){
			s+="d";
		}
		if (s.length()==0){
			s = "--";
		}
		return s;
	}
	public int getThrowDrawableId(){
		int d = R.drawable.bxs_notthrown;
		switch(throwType){
			case ThrowType.BALL_HIGH:
				if (isOnFire) {
					d = R.drawable.bxs_high_fire;
				} else {
					d = R.drawable.bxs_high;
				}
				break;
			case ThrowType.BALL_LEFT:
				if (isOnFire) {
					d = R.drawable.bxs_left_fire;
				} else {
					d = R.drawable.bxs_left;
				}
				break;
			case ThrowType.BALL_RIGHT:
				if (isOnFire) {
					d = R.drawable.bxs_right_fire;
				} else {
					d = R.drawable.bxs_right;
				}
				break;
			case ThrowType.BALL_LOW:
				if (isOnFire) {
					d = R.drawable.bxs_low_fire;
				} else {
					d = R.drawable.bxs_low;
				}
				break;
			case ThrowType.STRIKE:
				switch (throwResult){
				case ThrowResult.DROP:
					d = R.drawable.bxs_strike_drop;
					break;
				case ThrowResult.CATCH:
				case ThrowResult.STALWART:
					d = R.drawable.bxs_strike_catch;
					break;
				}
				break;
			case ThrowType.POLE:
				switch (throwResult){
				case ThrowResult.DROP:
					if (isOnFire) {
						d = R.drawable.bxs_pole_fire;
					} else {
						d = R.drawable.bxs_pole_drop;
					}
					break;
				case ThrowResult.CATCH:
					d = R.drawable.bxs_pole_catch;
					break;
				case ThrowResult.STALWART:
					d = R.drawable.bxs_pole_stalwart;
					break;
				}
				break;
			case ThrowType.CUP:
				switch (throwResult){
				case ThrowResult.DROP:
					if (isOnFire) {
						d = R.drawable.bxs_cup_fire;
					} else {
						d = R.drawable.bxs_cup_drop;
					}
					break;
				case ThrowResult.CATCH:
					d = R.drawable.bxs_cup_catch;
					break;
				case ThrowResult.STALWART:
					d = R.drawable.bxs_cup_stalwart;
					break;
				}
				break;
			case ThrowType.BOTTLE:
				switch (throwResult){
					case ThrowResult.DROP:
						if (isOnFire) {
							d = R.drawable.bxs_bottle_fire;
						} else {
							d = R.drawable.bxs_bottle_drop;
						}
						break;
					case ThrowResult.CATCH:
						d = R.drawable.bxs_bottle_catch;
						break;
					case ThrowResult.STALWART:
						d = R.drawable.bxs_bottle_stalwart;
						break;
				}
				break;
		}
		return d;
	}
	
	public boolean getIsValid() {
		boolean valid = true;
		
		if (isOnFire) {
			if (throwResult != ThrowResult.NA && throwResult != ThrowResult.BROKEN) {
				valid = false;
			}
		}
		switch (throwType) {
		case ThrowType.BALL_HIGH:
		case ThrowType.BALL_RIGHT:
		case ThrowType.BALL_LOW:
		case ThrowType.BALL_LEFT:
		case ThrowType.STRIKE:
			if (deadType != 0 && isDrinkHit){
				// drinkHit must be on a live throw
				valid = false;
			} else if (isGoaltend || isTipped) {
				// goaltending and tipped dont make sense for these throwTypes
				valid = false;
			}
			
			// throwResult much be a drop or catch
			switch (throwResult) {
			case ThrowResult.DROP:
			case ThrowResult.CATCH:
				break;
			default:
				valid = false;
				break;
			}
			
			break;
			
		case ThrowType.POLE:
		case ThrowType.CUP:
		case ThrowType.BOTTLE:
			if (isDrinkHit) {
				// drink hits have to be direct
				valid = false;
			} else if (isTipped && isGoaltend) {
				// cant be tipped if throw was goaltended
				valid = false;
			} else if (deadType != 0 && isGoaltend) {
				// it isnt goaltending if throw is dead
				valid = false;
			} else if (throwResult == ThrowResult.NA) {
				// one of the normal results must apply
				valid = false;
			} else if (isTipped && throwResult == ThrowResult.STALWART) {
				// can't stalwart on a tip
				valid = false;
			} else if (isGoaltend && throwResult == ThrowResult.STALWART) {
				// can't stalwart and goaltend
				valid = false;
			} else if (isTipped && throwResult == ThrowResult.BROKEN) {
				// can't break on a tip
				valid = false;
			} else if (isGoaltend && throwResult == ThrowResult.BROKEN) {
				// can't break and goaltend
				valid = false;
			}
			
			break;
			
		case ThrowType.TRAP:
		case ThrowType.TRAP_REDEEMED:
		case ThrowType.SHORT:
			if (isGoaltend || isTipped || isDrinkHit) {
				// these modifiers dont apply to these throwTypes
				valid = false;
			} else if (throwResult != ThrowResult.NA) {
				// only NA result applies here
				valid = false;
			}
			
			break;
		case ThrowType.FIRED_ON:
			// fired_on is a dummy throw, so modifiers dont count and result must be NA
			// errors could potentially happen while returning the disc, so those are allowed
			if (isLineFault || isGoaltend || isTipped || isDrinkHit || deadType != 0) {
				valid = false;
			} else if (throwResult != ThrowResult.NA) {
				valid = false;
			}
			
			break;
		}
		return valid;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
        this.id = id;
	}

	public int getThrowIdx() {
		return throwIdx;
	}

	public void setThrowIdx(int throwIdx) {
        this.throwIdx = throwIdx;
	}
	
	public long getGameId() {
		return gameId;
	}

	public long getOffensivePlayerId() {
		return offensivePlayerId;
	}

	public long getDefensivePlayerId() {
		return defensivePlayerId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getThrowType() {
		return throwType;
	}

	public void setThrowType(int throwType) {
		this.throwType = throwType;
	}

	public int getThrowResult() {
		return throwResult;
	}

	public void setThrowResult(int throwResult) {
		this.throwResult = throwResult;
	}
	
	public int getDeadType() {
		return deadType;
	}

	public void setDeadType(int deadType) {
		this.deadType = deadType;
	}
	
	public int getInitialOffensivePlayerScore() {
		return initialOffensivePlayerScore;
	}

	public void setInitialOffensivePlayerScore(int initialOffensivePlayerScore) {
		this.initialOffensivePlayerScore = initialOffensivePlayerScore;
	}

	public int getInitialDefensivePlayerScore() {
		return initialDefensivePlayerScore;
	}

	public void setInitialDefensivePlayerScore(int initialDefensivePlayerScore) {
		this.initialDefensivePlayerScore = initialDefensivePlayerScore;
	}

	public int compareTo(Throw another) {
		if (throwIdx<another.throwIdx){
			return -1;
		}
		else if(throwIdx==another.throwIdx){
			return 0;
		}
		else{
			return 1;
		}
	}

	public static boolean isP1Throw(int throwIdx) {
		return throwIdx%2==0;
	}
	public static boolean isP1Throw(Throw t){
		return isP1Throw(t.getThrowIdx());
	}
	public boolean isP1Throw(){
		return isP1Throw(throwIdx);
	}	
}
