import org.osbot.rs07.api.Map;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java.awt.*;

@ScriptManifest(name = "Cow Killer", author = "dokato", version = 1.0, info = "", logo = "") 
public class MainCow extends Script {

    private long timeRan;
    private long timeBegan;
	private long timeBotted;
	private long timeOffline;
	private HashSet<NPC> cowSet;
	private String status;

	@Override
    public void onStart(){
		this.timeBegan = System.currentTimeMillis();
		this.timeBotted = 0;
		this.timeOffline = 0;
    }
    
    @Override
    public void onExit(){
    }


    @Override
    public int onLoop() throws InterruptedException{
    	status="loop started";
    	if(getClient().isLoggedIn()){
    		procedures();
    		if(isCowArea()) killCows();
    		else goToCowArea();
    	}
    	status="loop ended";
    	return 0;
    }
    
    private void goToCowArea() throws InterruptedException{
    	getObjects().closest(new Area(3177,3316,3177,3316),"Gate").interact("Open");
    	sleep(random(200,400));
    }
    
    private void killCows() throws InterruptedException{
    	status="is fighting";
    	if(((!myPlayer().isUnderAttack()&&!myPlayer().isAnimating())&&!getCombat().isFighting())&&!myPlayer().isMoving()){
    		status="getting all npcs";
    		List<NPC> npcList = getNpcs().getAll();
    		for(NPC npc : npcList){
    			int id=npc.getId();
    			status="filtering out the cows";
    			if(((id==2806||id==2808)||id==2805)||id==2807){
    				status="filtering out the attackable ones";
    				if(((!npc.isUnderAttack()&&npc.isAttackable())&&getMap().canReach(npc.getPosition()))&&npc.getHealth()!=0){
    					cowSet.add(npc);
    				}
    			}
    		}
    		status="about to attack the cow";
    		if(npcs.closest(cowSet).interact("Attack"))
    			sleep(random(300,600));
    		cowSet.clear();
		}
    }
    
    private boolean isCowArea(){
    	status="entered isCowArea";
    	Position check1 = new Position(3198,3319,0);
    	Position check2 = new Position(3186,3323,0);
    	Position check3 = new Position(3175,3327,0);
    	Position check4 = new Position(3165,3327,0);
    	Map m = getMap();
    	status="retourning isCowArea";
    	return ((m.canReach(check1)||m.canReach(check2))||m.canReach(check3))||m.canReach(check4);
    }
    
    private void procedures(){
    	getCamera().toTop();
    	if(getInventory().isItemSelected()) getInventory().deselectItem();
    	if(getSettings().getRunEnergy()<random(5,10)) getSettings().setRunning(true);
    }

    @Override
    public void onPaint(Graphics2D g1){this.timeRan = (System.currentTimeMillis() - this.timeBegan);
		if (getClient().isLoggedIn()) {
			this.timeBotted = (this.timeRan - this.timeOffline);
		} else {
			this.timeOffline = (this.timeRan - this.timeBotted);
		}
		
		Graphics2D g = g1;
		
		g.setFont(new Font("Arial", 0, 13));
		g.setColor(new Color(255, 255, 255));
		g.drawString("Version: " + getVersion(), 20, 50);
		g.drawString("Runtime: " + ft(this.timeRan), 20, 65);
		g.drawString("Time botted: " + ft(this.timeBotted), 20, 80);
		g.drawString("Status: " + this.status, 20, 95);
		
		g.drawString("Attk: " + getSkills().getStatic(Skill.ATTACK), 20,115 );
		g.drawString("" + getSkills().experienceToLevel(Skill.ATTACK), 20,130 );
		
	    g.drawString("Str: " + getSkills().getStatic(Skill.STRENGTH), 20, 160);
	    g.drawString("" + getSkills().experienceToLevel(Skill.STRENGTH), 20, 175);
	    
	    
	    g.drawString("Def: " + getSkills().getStatic(Skill.DEFENCE), 20, 205);
	    g.drawString("" + getSkills().experienceToLevel(Skill.DEFENCE), 20, 220);
	    
	    g.drawString("Hp: " + getSkills().getStatic(Skill.HITPOINTS), 20, 250);
	    g.drawString("" + getSkills().experienceToLevel(Skill.HITPOINTS), 20, 265);
    }

	private String ft(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(duration));
		if (days == 0L) {
			res = hours + ":" + minutes + ":" + seconds;
		} else {
			res = days + ":" + hours + ":" + minutes + ":" + seconds;
		}
		return res;
	}
	
	static class PriceLookUp {
		private static URLConnection con;
		private static InputStream is;
		private static InputStreamReader isr;
		private static BufferedReader br;

		private static String[] getData(int itemID) {
			try {
				URL url = new URL(
						"https://api.rsbuddy.com/grandExchange?a=guidePrice&i="
								+ itemID);
				con = url.openConnection();
				is = con.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				String line = br.readLine();
				if (line != null) {
					return line.split(",");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
					} else if (isr != null) {
						isr.close();
					} else if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (br != null) {
					br.close();
				} else if (isr != null) {
					isr.close();
				} else if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public static int getPrice(int itemID) {
			String[] data = getData(itemID);
			if ((data != null) && (data.length == 5)) {
				return Integer.parseInt(data[0].split(":")[1]);
			}
			return 0;
		}

		public static int getAverageBuyOffer(int itemID) {
			String[] data = getData(itemID);
			if ((data != null) && (data.length == 5)) {
				return Integer.parseInt(data[1].split(":")[1]);
			}
			return 0;
		}

		public static int getAverageSellOffer(int itemID) {
			String[] data = getData(itemID);
			if ((data != null) && (data.length == 5)) {
				return Integer.parseInt(data[3].split(":")[1]);
			}
			return 0;
		}
	}
}