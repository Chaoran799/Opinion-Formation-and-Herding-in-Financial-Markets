import java.util.Random;
import java.util.UUID;


public class Agent {
	public static Random random = new Random(123456789 / 2);

	public long logNormal(double mu, double sigma) {

		return (Math.round(Math.exp(Math.sqrt(sigma) * (random.nextGaussian() + mu))));
	}

	
	public long logNormalByAgent(double mu, double sigma) {
		// Random random = new Random();
//		double value = 0;
//		if ("C".equals(this.role)) {// 0-1
//			value=getRoundNnm(-2.00000000, -0.70000000);
//			System.out.println("C:value===>"+value);
//			//value = random.nextDouble() * 1;
//		} else if ("N".equals(this.role)) {// 1-2
//			value=getRoundNnm(-0.70000000, 0.70000000);
//			System.out.println("N:value===>"+value);
//			//value = random.nextDouble() * 1 + 1;
//		} else if ("F".equals(this.role)) {// 2-3
//			//value = random.nextDouble() * 1 + 2;
//			value=getRoundNnm(0.70000000, 0.20000000);
//			System.out.println("F:value===>"+value);
//		}
//	
		// return (Math.round(Math.exp(Math.sqrt(sigma) * (random.nextGaussian() +
		// mu))));

//		long value = 0;
//		if ("C".equals(this.role)) {// 0-1
//			value = getRoundNnm1(-50, -17);
//		} else if ("N".equals(this.role)) {// 1-2
//			value = getRoundNnm1(-17,17);
//		} else if ("F".equals(this.role)) {// 2-3
//			value = getRoundNnm1(17, 50);
//		}
//		return value;

		return (Math.round(Math.exp(Math.sqrt(sigma) * (random.nextGaussian() + mu))));
	}

	
	public long getRoundNnm1(long min, long max) {
		return (long) (Math.random() * (max - min) + min);
	}

	
	public Double getRoundNnm(Double min, Double max) {
		return Math.random() * (max - min) + min;
	}

	public String agentId; 
	public double prevMidPrice;
	public double prevVol;
	public boolean activateOrder;
	public boolean cancelOrder;
	public int direction;
	public long targetPrice;
	public long size;
	public long localID;
	public long tMax;
	public long tickTime;
	public double riskAversion;
	public double gamma = 0.02;
	public double L = 5.0;
	public double theta0 = 0.165; 
	public double sigma2 = Math.log(Math.pow(10.0 / 7.0, 2.0) + 1.0);
	public double mu = Math.log(7.0) - sigma2 / 2.0;

	

	public double xjz;
	public long gpNUm;
	public double caifu = 0;

	public double caifuPrev = 0;

	public double bdl = 0;
	public double bdlSum = 0;
	public double bdlAve = 0;

	public String role;
	public boolean isFirst = true;
	public double firstCaifu = 0;
	public double caifuCha = 0;
	public long cishu = 0;
	public boolean isExecute = false;

	public boolean isFirst1 = true;
	public double firstCaifu1 = 0;
	public double caifuCha1 = 0;
	public long cishu1 = 0;
	public boolean isExecute1 = false;
	public double caifuPrev1 = 0;
	public double caifu1 = 0;
	public long gpNUm1;
	public double xjz1;

	public double bdl1;
	public double bdlSum1 = 0;
	public double bdlAve1 = 0;

	public int per;
	
	public double alpha;
	public double bt;

	public Agent() {
	};

	public Agent(String agentId, double xjz, long gpNUm) {
		this.agentId = agentId;
		this.xjz = xjz;
		this.gpNUm = gpNUm;
	}

	public Agent(Agent agent) {
		prevMidPrice = agent.prevMidPrice;
		prevVol = agent.prevVol;
		activateOrder = agent.activateOrder;
		cancelOrder = agent.cancelOrder;

		direction = agent.direction;
		targetPrice = agent.targetPrice;
		size = agent.size;

		localID = agent.localID;
		tMax = agent.tMax;

		tickTime = agent.tickTime;
		riskAversion = agent.riskAversion;
		agentId = agent.getAgentId();
		xjz = agent.getXjz();
		gpNUm = agent.getGpNUm();
		role = agent.getRole();
		isFirst = agent.isFirst();
		alpha = agent.alpha;
		bt = agent.bt;
	}

	public Agent(long bid, long ask, String agentId, double xjz, long gpNUm, String role, boolean isFirst,int per) {
		activateOrder = false;
		cancelOrder = false;
		tickTime = 0;
		localID = 0;
		riskAversion = 0.5 * random.nextDouble() + 0.25;
		tMax = 100;
		this.agentId = agentId;
		this.xjz = xjz;
		this.gpNUm = gpNUm;
		this.role = role;
		this.isFirst = isFirst;
		this.per = per;
		//this.alpha = alpha;
		bt = MathUtils.getrandom(0, 1);
	}

	
	public boolean updated(double mid, double vol, long bid, long ask, long quoteNumber, double cyzNum) {
		

		if (++tickTime > tMax) {
			cancelOrder = true;
			return true;
		}
		double volatility = (vol == 0.0 ? 1.0 : vol);
		
		double cancelationProb = 1.0 - Math.exp(-gamma * Math.sqrt(volatility)); 
		if (random.nextDouble() > 0.5) { // before 0.7
			/* -- cancel order? -- */
			cancelOrder = true;
			return true;
		}

		double volatilityRisk = 1.0 - cancelationProb;
		
		double theta = theta0 * riskAversion * volatilityRisk * (quoteNumber / cyzNum)
				* (volatility * random.nextGaussian());
		
		double enterProbability = ((1 - this.alpha)  * this.bt) + (this.alpha * Math.random());
		if (enterProbability > 2/3 | enterProbability < 1/3) {
		
			direction = (enterProbability > 2/3 ? 1 : -1);
			long etaB = Math.round(this.logNormalByAgent(this.mu, this.sigma2) - Math.exp(this.mu)); 
			System.out.println("etaB===>" + etaB);

			WriteCSV.writrByStr("C:/test/one/eta_b.csv", etaB + "");

			// System.out.println(
			// "etaB===>" + etaB + ":" + this.mu + ":" + this.sigma2 + ":" +
			// this.logNormal(this.mu, this.sigma2));
			if (direction == 1) {
				targetPrice = bid - etaB; 
				if (targetPrice < 0)
					targetPrice = 1;
			} else if (direction == -1) {
				targetPrice = ask + etaB;
				if (targetPrice < 0)
					targetPrice = 1;
			}

			size = this.logNormal(this.mu, this.sigma2);
			activateOrder = true; 

			return true;
		}
		return false;
	}

	public long setID(long ID) {
		localID = ID;
		return localID;
	}

	public long getGpNUm() {
		return gpNUm;
	}

	public void setGpNUm(long gpNUm) {
		this.gpNUm = gpNUm;
	}

	public double getXjz() {
		return xjz;
	}

	public void setXjz(double xjz) {
		this.xjz = xjz;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public double getCaifu() {
		return caifu;
	}

	public void setCaifu(double caifu) {
		this.caifu = caifu;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public double getBdl() {
		return bdl;
	}

	public void setBdl(double bdl) {
		this.bdl = bdl;
	}

	public double getCaifuPrev() {
		return caifuPrev;
	}

	public void setCaifuPrev(double caifuPrev) {
		this.caifuPrev = caifuPrev;
	}

	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public double getFirstCaifu() {
		return firstCaifu;
	}

	public void setFirstCaifu(double firstCaifu) {
		this.firstCaifu = firstCaifu;
	}

	public long getCishu() {
		return cishu;
	}

	public void setCishu(long cishu) {
		this.cishu = cishu;
	}

	public double getCaifuCha() {
		return caifuCha;
	}

	public void setCaifuCha(double caifuCha) {
		this.caifuCha = caifuCha;
	}

	public double getBdlSum() {
		return bdlSum;
	}

	public void setBdlSum(double bdlSum) {
		this.bdlSum = bdlSum;
	}

	public double getBdlAve() {
		return bdlAve;
	}

	public void setBdlAve(double bdlAve) {
		this.bdlAve = bdlAve;
	}

	public boolean isExecute() {
		return isExecute;
	}

	public void setExecute(boolean isExecute) {
		this.isExecute = isExecute;
	}

	public boolean isFirst1() {
		return isFirst1;
	}

	public void setFirst1(boolean isFirst1) {
		this.isFirst1 = isFirst1;
	}

	public double getFirstCaifu1() {
		return firstCaifu1;
	}

	public void setFirstCaifu1(double firstCaifu1) {
		this.firstCaifu1 = firstCaifu1;
	}

	public double getCaifuCha1() {
		return caifuCha1;
	}

	public void setCaifuCha1(double caifuCha1) {
		this.caifuCha1 = caifuCha1;
	}

	public long getCishu1() {
		return cishu1;
	}

	public void setCishu1(long cishu1) {
		this.cishu1 = cishu1;
	}

	public boolean isExecute1() {
		return isExecute1;
	}

	public void setExecute1(boolean isExecute1) {
		this.isExecute1 = isExecute1;
	}

	public double getCaifuPrev1() {
		return caifuPrev1;
	}

	public void setCaifuPrev1(double caifuPrev1) {
		this.caifuPrev1 = caifuPrev1;
	}

	public double getCaifu1() {
		return caifu1;
	}

	public void setCaifu1(double caifu1) {
		this.caifu1 = caifu1;
	}

	public double getXjz1() {
		return xjz1;
	}

	public void setXjz1(double xjz1) {
		this.xjz1 = xjz1;
	}

	public long getGpNUm1() {
		return gpNUm1;
	}

	public void setGpNUm1(long gpNUm1) {
		this.gpNUm1 = gpNUm1;
	}

	public double getBdl1() {
		return bdl1;
	}

	public void setBdl1(double bdl1) {
		this.bdl1 = bdl1;
	}

	public double getBdlSum1() {
		return bdlSum1;
	}

	public void setBdlSum1(double bdlSum1) {
		this.bdlSum1 = bdlSum1;
	}

	public double getBdlAve1() {
		return bdlAve1;
	}

	public void setBdlAve1(double bdlAve1) {
		this.bdlAve1 = bdlAve1;
	}

	public int getPer() {
		return per;
	}

	public void setPer(int per) {
		this.per = per;
	}
	
	public void setalpha(double alpha) {
		this.alpha = alpha;
	}
	
	public void getalpha(double alpha) {
		this.alpha = alpha;
	}
	
	public void setbt(double bt) {
		this.bt = bt;
	}
	
	public void getbt(double bt) {
		this.bt = bt;
	}

}
