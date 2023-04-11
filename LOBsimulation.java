
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.io.FileWriter;
import java.io.IOException;

public class LOBSimulation {

	List<Agent> agentList;
	List<Agent> activeAgent;
	AlphaLOB alphaLOB;

	double midPrice;
	double prevMidPrice;
	double vol;
	double prevVol;
	double alpha1; 
	double alpha2;
	double alpha3;
	
	

	public long logNormal(double mu, double sigma, double ran) {
		return (Math.round(Math.exp(Math.sqrt(sigma) * (ran + mu))));
	}

public LOBSimulation(double a) {
		agentList = new LinkedList<Agent>();
		activeAgent = new LinkedList<Agent>();
		alphaLOB = new AlphaLOB(a);
		midPrice = 0.0;
		prevMidPrice = 0.0;
		vol = 0.0;
		prevVol = 0.0;
		alpha1 = 0.75;
		alpha2 = 0.5;
		alpha3 = 0.0;
	}

	/* ------------------------------------------------- */
	
	public long runOrderBook(double volatility) {
		long removed = 0;
		if (!this.alphaLOB.runTimeUpdate()) {
			System.out.println("Failed updating order book!");
			return -1;
		}
		if ((removed = this.alphaLOB.removeObsolete(volatility / 10000.0)) == -1) {
			System.out.println("Failed removing obsolete!");
			return -1;
		}
		return removed;
	}

	/* ---------------------------------------------------- */

	/* ------------------------------------------------------- */
	
	public boolean add2Active1Sided(Agent agent) {
		/* -- add to order book -- */
		if (alphaLOB.add1Sided(new Orders(agent), agent.direction) == 0) {
			System.out.println("Failed adding to order book!");
			return false;
		}
		return true;
	}


	public boolean add2Active2Sided(Agent agentBid, Agent agentAsk) {
		/* -- add to order book -- */
		if (alphaLOB.add2Sided(new Orders(agentBid), new Orders(agentAsk)) == 0) {
			System.out.println("Failed adding to order book!");
			return false;
		}
		return true;
	}
	/* ---------------------------------------------------------------------- */

	public boolean add2InActive(Agent agent) {
		/* -- add to inactive list -- */
		try {
			agentList.add(new Agent(agent));
		} catch (Exception e) {
			System.out.println("Failed adding to agent list!");
			return false;
		}
		return true;
	}

	
	public boolean removeInActive(int index) {
		try {
			agentList.remove(index);
		} catch (Exception e) {
			System.out.println("Failed removing extinct order!");
			return false;
		}
		return true;
	}

	/* -------------------------------------------------- */
	
	public int executeTrade(Agent agent, int agentNum_per, double mid, List<Agent> agents) {

		if (agent.direction == 1 && alphaLOB.getAskCount() <= agentNum_per)
			return 0;
		if (agent.direction == -1 && alphaLOB.getBidCount() <= agentNum_per)
			return 0;
		int execute = 0; 
		
		if (agent.size * 3 > (agent.direction == 1 ? alphaLOB.getAskLiq() : alphaLOB.getBidLiq())) {
			return 0;
		}
		
		List<LocalOrder> excOrder = new LinkedList<LocalOrder>(
				alphaLOB.marketExecution(agent, 3 * agent.size, agent.direction, mid, agents));
		if (excOrder == null || excOrder.size() == 0) {
			System.out.println("No return data from execution system!");
			return 0;
		} else {
			execute += excOrder.size();
		}
		
		for (int l = 0; l < excOrder.size(); ++l) {
			
			if (!alphaLOB.removeTop(excOrder.get(l), agent.direction)) {
				System.out.println("Failed removing top of the book!");
				return 0;
			}
		}
		return execute;
	}

	
	public void firstSet(double mid, double v) {
		midPrice = mid;
		prevMidPrice = mid;
		vol = v;
		prevVol = v;
	}

	/* ------------------------------------------------ */

	public void printOrderBook() {
		FileWriter file = null;
		try {
			file = new FileWriter("D:/test/LOBSpread.dat", true);
		} catch (IOException e) {
			System.out.println("Failed printing bid/ask!");
		}
		if (alphaLOB.Bid != null || !alphaLOB.Bid.isEmpty()) {
			for (int i = 0; i < alphaLOB.Bid.size(); ++i) {
				if (alphaLOB.Bid.get(i).get(0).getOrder().getSpread() > 0) {
					String s = " ";
					for (int j = 0; j < alphaLOB.Bid.get(i).size(); ++j) {
						s += (alphaLOB.Bid.get(i).get(j).getOrder().getPrice() + ",");
					}
					System.out.println(alphaLOB.Bid.get(i).get(0).getOrder().getSpread() + " " + s);
				} else {
					System.out.println(alphaLOB.Bid.get(i).get(0).getOrder().getSpread() + " "
							+ alphaLOB.Bid.get(i).get(0).getOrder().getPrice());
				}
			}
		}
		try {
			Thread.sleep(1000);
		} catch (Exception e) {

		}
		System.out.println("-------");

		if (alphaLOB.Ask != null || !alphaLOB.Ask.isEmpty()) {
			for (int i = 0; i < alphaLOB.Ask.size(); ++i) {
				if (alphaLOB.Ask.get(i).get(0).getOrder().getSpread() > 0) {
					String s = " ";
					for (int j = 0; j < alphaLOB.Ask.get(i).size(); ++j) {
						s += (alphaLOB.Ask.get(i).get(j).getOrder().getPrice() + ",");
					}
					System.out.println(alphaLOB.Ask.get(i).get(0).getOrder().getSpread() + " " + s);
				} else {
					System.out.println(alphaLOB.Ask.get(i).get(0).getOrder().getSpread() + " "
							+ alphaLOB.Ask.get(i).get(0).getOrder().getPrice());
				}
			}
		}

		try {
			Thread.sleep(1000);
		} catch (Exception e) {

		}

	}

	
	public void runUpdated(double bid, double ask) {
		double L = 5.0;
		
		midPrice = ((bid + ask) / 2.0) / L + (1.0 - 1.0 / L) * prevMidPrice;
		
		vol = Math.pow(100.0 * (midPrice - prevMidPrice), 2.0) / L + (1.0 - 1.0 / L) * prevVol;

		prevMidPrice = midPrice;
		prevVol = vol;
	}
	/* ------------------------------------------------ */

	public static void AlphaLOBrun(int a, List<Agent> agents) throws Exception {
		System.out.println("Running alpha LOB!");
		Random random = new Random();
		random.setSeed(123456789);
		LOBSimulation Sim = new LOBSimulation(((double) a * 0.05));

		long bidT = 99999 - 10;
		long askT = 100001 + 10;

		Sim.firstSet((bidT + askT) / 2.0, 10.0);
		Sim.runUpdated(99999, 100000);

		

		int agentNum_per = new Long(Math.round(agents.size() * 0.02)).intValue();
		int cyzNum = agents.size();

		for (int i = 0; i < cyzNum; ++i) {
			System.out.println("first step i==>" + i);
			Agent agent = new Agent(bidT, askT, agents.get(i).getAgentId(), agents.get(i).getXjz(),
					agents.get(i).getGpNUm(), agents.get(i).getRole(), agents.get(i).isFirst(),agents.get(i).getPer());

			agent.tMax = Sim.logNormal(agent.mu, agent.sigma2, random.nextGaussian()); 
			int per = agent.getPer();
			System.out.println("per1===>" + per);
			
			if (agent.updated(Sim.midPrice, Sim.vol, 99999, 100001, cyzNum, Double.valueOf(cyzNum))) {
				if (agent.activateOrder) {
					if ((agent.direction == 1 && agent.targetPrice >= 99999)
							|| (agent.direction == -1 && agent.targetPrice <= 100001)) {
						if (agent.direction == 1) {
							agent.targetPrice = Math.round(99999 - 50 * random.nextDouble());
						} else {
							agent.targetPrice = Math.round(100001 + 50.0 * random.nextDouble());
						}
					}
					if (random.nextDouble() > (double) per * 0.05) { 
						Agent agentBid = null;
						Agent agentAsk = null;
						if (agent.direction == 1) {
							agentBid = new Agent(agent);
							agentBid.targetPrice = Math.round(99999 - 10 * random.nextDouble());
							agent.direction = -1;
							agent.targetPrice = 100001 + (long) Math.ceil(10.0 * random.nextDouble()); 
							agentAsk = new Agent(agent);
						} else if (agent.direction == -1) {
							agentAsk = new Agent(agent);
							agentAsk.targetPrice = Math.round(100001 + 10 * random.nextDouble());
							agent.direction = 1;
							agent.targetPrice = 99999 - (long) Math.ceil(10 * random.nextDouble()); 
							agentBid = new Agent(agent);
						}

						if (!Sim.add2Active2Sided(agentBid, agentAsk)) {
							System.out.println("Failed adding to two-sided active agents!");
						}
						Sim.alphaLOB.penaltyUpdated();
					} else { 
						if (!Sim.add2Active1Sided(agent)) {
							System.out.println("Failed adding to active agents!");
						}
						Sim.alphaLOB.penaltyUpdated();
					}
					
				} else {
					/* -- add to inactive list -- */
					if (!Sim.add2InActive(agent)) {
						System.out.println("Failed adding to inactive agents!");
					}

				}
			}
		}

		
		long orderCountT = Sim.alphaLOB.getAskCount() + Sim.alphaLOB.getBidCount();
		
		System.out.println("order count:" + orderCountT + "inactive:" + Sim.agentList.size());
		
		long bidJ = Sim.alphaLOB.Bid.get(0).get(0).getOrder().getPrice();
		
		long askJ = Sim.alphaLOB.Ask.get(0).get(0).getOrder().getPrice();

		System.out.println("Bid: " + bidJ + " Ask: " + askJ);
		

		for (int i = 0; i < 1000; ++i) {
			
			System.out.println("run simulation i==>" + i);
			
			long bid = bidT;
			
			long ask = askT;
			if (Sim.alphaLOB.Bid != null && Sim.alphaLOB.Bid.size() > 0 && Sim.alphaLOB.Bid.get(0).size() > 0
					&& Sim.alphaLOB.Bid.get(0).get(0).getOrder() != null) {
				bid = Sim.alphaLOB.Bid.get(0).get(0).getOrder().getPrice();
			}
			if (Sim.alphaLOB.Ask != null && Sim.alphaLOB.Ask.size() > 0 && Sim.alphaLOB.Ask.get(0).size() > 0
					&& Sim.alphaLOB.Ask.get(0).get(0).getOrder() != null) {
				ask = Sim.alphaLOB.Ask.get(0).get(0).getOrder().getPrice();
			}
			
			Sim.runUpdated(bid, ask);

			double mid = Sim.midPrice;
			double vol = Sim.vol;//
			System.out.println("Vol: " + Sim.vol);
			System.out.println("Bid: " + bid + " Ask: " + ask);
			
			long orderCount = Sim.alphaLOB.getAskCount() + Sim.alphaLOB.getBidCount();
			System.out.println("Order count " + orderCount);
			
			int nonActRemove = 0;
			int k = 100;
			while (k < Sim.agentList.size()) {

				
				if (Sim.agentList.get(k).updated(mid, vol, bid, ask,
						Math.max(Math.max(Math.round(cyzNum * 0.25), orderCount), Math.round(cyzNum * 0.45)),
						Double.valueOf(cyzNum))) {
					if (Sim.agentList.get(k).cancelOrder) {
						if (!Sim.removeInActive(k)) {
							System.out.println("Failed removing inactive agent!");
						}
						nonActRemove++;
						continue;
					}
				}
				++k;
			}
			
			for (int j = 0; j < agents.size(); j++) {
				agents.get(i).isExecute = false;
			}

			
			int execute = 0;
			k = 0;
			while (k < Sim.agentList.size()) {
				int per = Sim.agentList.get(k).getPer();
				System.out.println("per2===>" + per);
				if (Sim.agentList.get(k).activateOrder) {
					if ((Sim.agentList.get(k).direction == 1
							&& Sim.agentList.get(k).targetPrice >= Sim.alphaLOB.getBestAsk())
							|| (Sim.agentList.get(k).direction == -1
									&& Sim.agentList.get(k).targetPrice <= Sim.alphaLOB.getBestBid())) {
						
						int temp = Sim.executeTrade(Sim.agentList.get(k), agentNum_per, mid, agents);
						if (temp == 0) {
						} else {
							execute += temp;
						}
						Sim.alphaLOB.penaltyUpdated();
					} else { 
						if (random.nextDouble() > (double) per * 0.05) { 
							Agent agentBid = null;
							Agent agentAsk = null;
							if (Sim.agentList.get(k).direction == 1) {
								agentBid = new Agent(Sim.agentList.get(k));

								if (agentBid.targetPrice < Sim.alphaLOB.getBestAsk() - 5)
									agentBid.targetPrice = Sim.alphaLOB.getBestAsk() - 1
											- Math.round(5 * random.nextDouble());
								
								Sim.agentList.get(k).direction = -1;
								agentAsk = new Agent(Sim.agentList.get(k));
								long bidTemp = Sim.alphaLOB.getBestBid();
								
								agentAsk.targetPrice = Math
										.round(Math.max(bidTemp, agentBid.targetPrice) + 1 + 5 * random.nextDouble());
							} else if (Sim.agentList.get(k).direction == -1) {
								agentAsk = new Agent(Sim.agentList.get(k));
								if (agentAsk.targetPrice > Sim.alphaLOB.getBestBid() + 5)
									
									agentAsk.targetPrice = Sim.alphaLOB.getBestBid() + 1
											+ Math.round(5 * random.nextDouble());
								
								Sim.agentList.get(k).direction = 1;
								agentBid = new Agent(Sim.agentList.get(k));
								long askTemp = Sim.alphaLOB.getBestAsk();
								
								agentBid.targetPrice = Math
										.round(Math.min(askTemp, agentAsk.targetPrice) - 1 - 5 * random.nextDouble());
							}
							if (!Sim.add2Active2Sided(agentBid, agentAsk)) {
								System.out.println("Failed adding to two-sided active agents!");
							}
							
							Sim.alphaLOB.penaltyUpdated();
						} else { 
							if (!Sim.add2Active1Sided(Sim.agentList.get(k))) {
								System.out.println("Failed adding to active agents!");
							}
							
							Sim.alphaLOB.penaltyUpdated();
						}
					}
					try {
						Sim.agentList.remove(k);
					} catch (Exception e) {
						System.out.println("Failed removing from agentList!");
					}
					continue;
				} else {
					++k;

				}
			}

			
			for (int j = 0; j < agents.size(); j++) {
				if (!agents.get(i).isExecute) {
					agents.get(i).setCaifuPrev(agents.get(i).getCaifu());
					agents.get(i).setCaifu(agents.get(i).getXjz() + agents.get(i).getGpNUm() * mid);
					if (agents.get(i).getCaifuPrev() != 0) {
						agents.get(i).setBdl((agents.get(i).getCaifu() - agents.get(i).getCaifuPrev())
								/ Math.abs(agents.get(i).getCaifuPrev()));
					} else {
						agents.get(i).setBdl(0);
					}
					
					agents.get(i).setBdlSum(agents.get(i).getBdlSum() + agents.get(i).getBdl());
					
					agents.get(i).setBdlAve(agents.get(i).getBdlSum() / (i + 1));
				}
			}

			long exctRemove = 0;
			if ((exctRemove = Sim.runOrderBook(vol)) == -1) {
				System.out.println("Problem with updating order book!");
			}

			long askC = Sim.alphaLOB.getAskCount();
			long bidC = Sim.alphaLOB.getBidCount();
			long inActSize = Sim.agentList.size();
			
			System.out.println("Removed " + nonActRemove + " inactive and " + exctRemove + " active agents!");
			
			System.out.println("Executed: " + execute + " askC:" + askC + " bidC:" + bidC + " inAct:" + inActSize);


			WriteCSV.writrByStr("C:/test/logomatrix.csv",
					a + "," + Sim.vol + "," + Sim.alphaLOB.getBestBid() + "," + Sim.alphaLOB.getBestAsk() + ","
							+ orderCount + "," + execute + "," + askC + "," + bidC + "," + inActSize);
			

			boolean isInAgentList = false;
			long addSize = agents.size() - (askC + bidC + inActSize);
			long nowAdd = 0;
			for (int j1 = 0; j1 < agents.size(); j1++) {
				if (addSize <= nowAdd) {
					break;
				}
				isInAgentList = false;
				for (int j2 = 0; j2 < Sim.agentList.size(); j2++) {
					if (Sim.agentList.get(j2).agentId.equals(agents.get(j1).getAgentId())) {
						isInAgentList = true;
						break;
					}
				}
				if (!isInAgentList) {
					Sim.agentList.add(new Agent(bid, ask, agents.get(j1).getAgentId(), agents.get(j1).getXjz(),
							agents.get(j1).getGpNUm(), agents.get(j1).getRole(), agents.get(i).isFirst(),agents.get(i).getPer()));
					nowAdd++;
				}
			}
			FileWriter file = null;
			try {
				
				String s = "C:/test/NewPrice" + a + "Alpha.dat";
				file = new FileWriter(s, true);
				file.append(Sim.alphaLOB.getBestBid() + "," + Sim.alphaLOB.getBestAsk() + "," + askC + "," + bidC + ","
						+ execute + "\n");
				file.close();
			} catch (IOException e) {
				System.out.println("Failed printing bid/ask!");
			}
			
			writeCNFByTime(agents);
			writeCNFByTimeAll(agents);
		}
		
		writeCNF(agents);
		writeCNFAll(agents);

	}

	public static void writeCNFAll(List<Agent> agents) {
		int num_c = 0; 
		int num_n = 0;
		int num_f = 0;

		double bdl_sum_c = 0; 
		double bdl_sum_n = 0; 
		double bdl_sum_f = 0; 

		double bdl_c = 0;
		double bdl_n = 0;
		double bdl_f = 0;

		double cha_sum_c = 0; 
		double cha_sum_n = 0; 
		double cha_sum_f = 0; 

		double cha_c = 0;
		double cha_n = 0;
		double cha_f = 0;

		for (int j = 0; j < agents.size(); j++) {
			if ("C".equals(agents.get(j).getRole())) {
				num_c++;
				bdl_sum_c += agents.get(j).getBdlAve1();
				cha_sum_c += agents.get(j).getCaifuCha1();

			} else if ("N".equals(agents.get(j).getRole())) {
				num_n++;
				bdl_sum_n += agents.get(j).getBdlAve1();
				cha_sum_n += agents.get(j).getCaifuCha1();
			} else {
				num_f++;
				bdl_sum_f += agents.get(j).getBdlAve1();
				cha_sum_f += agents.get(j).getCaifuCha1();
			}
		}
		if (num_c != 0) {
			bdl_c = bdl_sum_c / num_c;
			cha_c = cha_sum_c / num_c;
		} else {
			bdl_c = 0;
			cha_c = 0;
		}

		if (num_n != 0) {
			bdl_n = bdl_sum_n / num_n;
			cha_n = cha_sum_n / num_c;
		} else {
			bdl_n = 0;
			cha_n = 0;
		}

		if (num_f != 0) {
			bdl_f = bdl_sum_f / num_f;
			cha_f = cha_sum_f / num_c;
		} else {
			bdl_f = 0;
			cha_f = 0;
		}
		WriteCSV.writrByStr("C:/test/all/cnf_bdl.csv", num_c + "," + num_n + "," + num_f + "," + bdl_c + "," + bdl_n
				+ "," + bdl_f + "," + bdl_sum_c + "," + bdl_sum_n + "," + bdl_sum_f);

		WriteCSV.writrByStr("C:/test/all/cnf_cha.csv", num_c + "," + num_n + "," + num_f + "," + cha_c + "," + cha_n
				+ "," + cha_f + "," + cha_sum_c + "," + cha_sum_n + "," + cha_sum_f);
	}

	public static void writeCNFByTimeAll(List<Agent> agents) {
		int num_c = 0; 
		int num_n = 0;
		int num_f = 0;

		double bdl_sum_c = 0; 
		double bdl_sum_n = 0; 
		double bdl_sum_f = 0; 

		double bdl_c = 0;
		double bdl_n = 0;
		double bdl_f = 0;

		for (int j = 0; j < agents.size(); j++) {
			if ("C".equals(agents.get(j).getRole())) {
				num_c++;
				bdl_sum_c += agents.get(j).getBdlAve1();
			} else if ("N".equals(agents.get(j).getRole())) {
				num_n++;
				bdl_sum_n += agents.get(j).getBdlAve1();
			} else {
				num_f++;
				bdl_sum_f += agents.get(j).getBdlAve1();
			}
		}
		if (num_c != 0) {
			bdl_c = bdl_sum_c / num_c;
		} else {
			bdl_c = 0;
		}

		if (num_n != 0) {
			bdl_n = bdl_sum_n / num_n;
		} else {
			bdl_n = 0;
		}

		if (num_f != 0) {
			bdl_f = bdl_sum_f / num_f;
		} else {
			bdl_f = 0;
		}
		WriteCSV.writrByStr("C:/test/all/cnf_bdl_time.csv", num_c + "," + num_n + "," + num_f + "," + bdl_c + ","
				+ bdl_n + "," + bdl_f + "," + bdl_sum_c + "," + bdl_sum_n + "," + bdl_sum_f);

	}

	
	public static void writeCNF(List<Agent> agents) {
		int num_c = 0; 
		int num_n = 0;
		int num_f = 0;

		double bdl_sum_c = 0; 
		double bdl_sum_n = 0; 
		double bdl_sum_f = 0; 

		double bdl_c = 0;
		double bdl_n = 0;
		double bdl_f = 0;

		double cha_sum_c = 0; 
		double cha_sum_n = 0; 
		double cha_sum_f = 0; 

		double cha_c = 0;
		double cha_n = 0;
		double cha_f = 0;

		for (int j = 0; j < agents.size(); j++) {
			if ("C".equals(agents.get(j).getRole())) {
				num_c++;
				bdl_sum_c += agents.get(j).getBdlAve();
				cha_sum_c += agents.get(j).getCaifuCha();

			} else if ("N".equals(agents.get(j).getRole())) {
				num_n++;
				bdl_sum_n += agents.get(j).getBdlAve();
				cha_sum_n += agents.get(j).getCaifuCha();
			} else {
				num_f++;
				bdl_sum_f += agents.get(j).getBdlAve();
				cha_sum_f += agents.get(j).getCaifuCha();
			}
		}
		if (num_c != 0) {
			bdl_c = bdl_sum_c / num_c;
			cha_c = cha_sum_c / num_c;
		} else {
			bdl_c = 0;
			cha_c = 0;
		}

		if (num_n != 0) {
			bdl_n = bdl_sum_n / num_n;
			cha_n = cha_sum_n / num_c;
		} else {
			bdl_n = 0;
			cha_n = 0;
		}

		if (num_f != 0) {
			bdl_f = bdl_sum_f / num_f;
			cha_f = cha_sum_f / num_c;
		} else {
			bdl_f = 0;
			cha_f = 0;
		}
		WriteCSV.writrByStr("C:/test/one/cnf_bdl.csv", num_c + "," + num_n + "," + num_f + "," + bdl_c + "," + bdl_n
				+ "," + bdl_f + "," + bdl_sum_c + "," + bdl_sum_n + "," + bdl_sum_f);

		WriteCSV.writrByStr("C:/test/one/cnf_cha.csv", num_c + "," + num_n + "," + num_f + "," + cha_c + "," + cha_n
				+ "," + cha_f + "," + cha_sum_c + "," + cha_sum_n + "," + cha_sum_f);
	}

	/**
	 * 根据参数者写入 CNF (每次模拟的回报率)
	 * 
	 * @param agents
	 */
	public static void writeCNFByTime(List<Agent> agents) {
		int num_c = 0; 
		int num_n = 0;
		int num_f = 0;

		double bdl_sum_c = 0; 
		double bdl_sum_n = 0; 
		double bdl_sum_f = 0; 

		double bdl_c = 0;
		double bdl_n = 0;
		double bdl_f = 0;

		for (int j = 0; j < agents.size(); j++) {
			if ("C".equals(agents.get(j).getRole())) {
				num_c++;
				bdl_sum_c += agents.get(j).getBdlAve();
			} else if ("N".equals(agents.get(j).getRole())) {
				num_n++;
				bdl_sum_n += agents.get(j).getBdlAve();
			} else {
				num_f++;
				bdl_sum_f += agents.get(j).getBdlAve();
			}
		}
		if (num_c != 0) {
			bdl_c = bdl_sum_c / num_c;
		} else {
			bdl_c = 0;
		}

		if (num_n != 0) {
			bdl_n = bdl_sum_n / num_n;
		} else {
			bdl_n = 0;
		}

		if (num_f != 0) {
			bdl_f = bdl_sum_f / num_f;
		} else {
			bdl_f = 0;
		}
		WriteCSV.writrByStr("C:/test/one/cnf_bdl_time.csv", num_c + "," + num_n + "," + num_f + "," + bdl_c + ","
				+ bdl_n + "," + bdl_f + "," + bdl_sum_c + "," + bdl_sum_n + "," + bdl_sum_f);

	}



	public static void changeRole(List<Agent> agents) {

		Random rand = new Random();
		int MAX = 100;
		int MIN = 0;
		int num_c = 100;
		int num_n = 100;
		int num_f = 800;
		
		
		double bdl_sum_c = 0; 
		double bdl_sum_n = 0; 
		double bdl_sum_f = 0; 

		double bdl_c = 0;
		double bdl_n = 0;
		double bdl_f = 0;

		double cha_sum_c = 0; 
		double cha_sum_n = 0; 
		double cha_sum_f = 0;

		
		for (int i = 0; i < agents.size(); i++) {
			double aa = MathUtils.getrandom(0, 1);
			if (aa < num_c/100) {
				agents.get(i).setRole("C");
				num_c--;
				bdl_sum_c += agents.get(i).getBdlAve1();
				cha_sum_c += agents.get(i).getCaifuCha1();
				
			} else if ((num_c/100 < aa) & (aa < (num_c/100) + (num_n/100)) ) {
				agents.get(i).setRole("N");
				num_n--;
				bdl_sum_n += agents.get(i).getBdlAve1();
				cha_sum_n += agents.get(i).getCaifuCha1();
			} else {
				agents.get(i).setRole("F");
			    num_f--;
			    bdl_sum_f += agents.get(i).getBdlAve1();
				cha_sum_f += agents.get(i).getCaifuCha1();
			}
			
			if (num_c != 0) {
				bdl_c = bdl_sum_c / num_c;
				//cha_c = cha_sum_c / num_c;
			} else {
				bdl_c = 0;
				//cha_c = 0;
			}

			if (num_n != 0) {
				bdl_n = bdl_sum_n / num_n;
				//cha_n = cha_sum_n / num_c;
			} else {
				bdl_n = 0;
				//cha_n = 0;
			}

			if (num_f != 0) {
				bdl_f = bdl_sum_f / num_f;
				//cha_f = cha_sum_f / num_c;
			} else {
				bdl_f = 0;
				//cha_f = 0;
			}
			WriteCSV.writrByStr("C:/test/one/cnf_change_bdl.csv", num_c + "," + num_n + "," + num_f + "," + bdl_c + "," + bdl_n
					+ "," + bdl_f + "," + bdl_sum_c + "," + bdl_sum_n + "," + bdl_sum_f);

			if ("C".equals(agents.get(i).getRole())) {// 0-1
				agents.get(i).setalpha(0.75);
			} else if ("N".equals(agents.get(i).getRole())) {// 1-2
				agents.get(i).setalpha(0.5);
			} else if ("F".equals(agents.get(i).getRole())) {// 2-3
				agents.get(i).setalpha(0.0);
			}

		}
		
		Collections.shuffle(agents);
	}

	
	public static void main(String[] args) {
		
		int agentNum = 1000;
		List<Agent> agents = new ArrayList<Agent>();
		for (int i = 0; i < agentNum; ++i) {
			agents.add(new Agent(UUID.randomUUID().toString(), 0, 0));
		}
		
		changeRole(agents);
		int size=50;
		for (int i = 0; i < size; i++) {
			try {
				System.out.println("开始第" + i+"次开始");
				LOBSimulation.AlphaLOBrun(8, agents);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

	}

}
