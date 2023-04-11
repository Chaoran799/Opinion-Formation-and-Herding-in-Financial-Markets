
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AlphaLOB {
	double alpha;

	public static Random random;

	List<List<LocalOrder>> Ask;
	List<List<LocalOrder>> Bid;

	List<Long> Spreads;
	List<Long> SpreadsID;

	public boolean bidEmpty;
	public boolean askEmpty;

	public long askLiq;
	public long bidLiq;

	public long orderID;

	public long askQuoteNumber;

	public long bidQuoteNumber;

	public long worstSpread;

	public AlphaLOB(double a) {
		alpha = a;

		Ask = null;
		Bid = null;
		Spreads = new LinkedList<Long>();
		SpreadsID = new LinkedList<Long>();

		bidEmpty = true;
		askEmpty = true;

		askLiq = 0;
		bidLiq = 0;

		orderID = 0;

		askQuoteNumber = 0;
		bidQuoteNumber = 0;

		random = new Random();
		random.setSeed(987654321);

		worstSpread = -1; // i.e. none
	}

	
	public void addToSpreadList(Long spread, Long ID) {
		if (Spreads == null || Spreads.isEmpty() || Spreads.size() == 0) {
			Spreads.add(new Long(spread));
			SpreadsID.add(new Long(ID));
		} else {
			

			if (Spreads.get(0).longValue() > spread.longValue()) { 
				Spreads.add(0, new Long(spread));
				SpreadsID.add(0, new Long(ID));
			} else if (Spreads.get(Spreads.size() - 1).longValue() < spread.longValue()) { 
				Spreads.add(new Long(spread));
				SpreadsID.add(new Long(ID));
			} else {
				for (int i = 1; i < Spreads.size() - 1; ++i) {
					if (Spreads.get(i).longValue() < spread.longValue()
							&& spread.longValue() < Spreads.get(i + 1).longValue()) {
						Spreads.add(i + 1, new Long(spread));
						SpreadsID.add(i + 1, new Long(ID));
						break;
					} else if (Spreads.get(i).longValue() == spread.longValue()) {
						Spreads.add(i, new Long(spread));
						SpreadsID.add(i, new Long(ID));
						break;
					} else if (Spreads.get(i + 1).longValue() == spread.longValue()) {
						Spreads.add(i + 1, new Long(spread));
						SpreadsID.add(i + 1, new Long(ID));
						break;
					}
				}
			}
		}
	}


	public void removeSpreadList(long spread, long orderID) {
		if (Spreads == null || Spreads.isEmpty()) {

		} else {
			for (int i = 0; i < Spreads.size(); ++i) {
				if (Spreads.get(i).longValue() == spread && SpreadsID.get(i).longValue() == orderID) {
					Spreads.remove(i);
					SpreadsID.remove(i);
					return;
				}
				if (Spreads.get(i).longValue() > spread)
					return;
			}
		}
	}

	public void findOpposite(long ID, int direction) {
		if (direction == 1) {
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					if (Bid.get(i).get(j).getOrder().getID() == ID) {
						Bid.get(i).get(j).getOrder().spread = -1;
						return;
					}
				}
			}
		} else if (direction == -1) {
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					if (Ask.get(i).get(j).getOrder().getID() == ID) {
						Ask.get(i).get(j).getOrder().spread = -1;
						return;
					}
				}
			}
		}
	}

	public long worstSpread() {
		if (Spreads == null || Spreads.isEmpty() || Spreads.size() == 0) {
			return 0;
		} else {
			return (new Long(Spreads.get(Spreads.size() - 1).longValue()).longValue());
		}
	}

	public long getOrderID() {
		return orderID;
	}

	public long bestAskSize() {
		if (Ask == null || Ask.isEmpty()) {
			return 0;
		} else {
			return Ask.get(0).get(0).getOrder().getSize();
		}
	}

	public long getBestAsk() {
		if (Ask == null || Ask.isEmpty()) {
			return 0;
		} else {
			return Ask.get(0).get(0).getOrder().getPrice();
		}
	}

	public long bestBidSize() {
		if (Bid == null || Bid.isEmpty()) {
			return 0;
		} else {
			return Bid.get(0).get(0).getOrder().getSize();
		}
	}

	public long getBestBid() {
		if (Bid == null || Bid.isEmpty()) {
			return 0;
		} else {
			return Bid.get(0).get(0).getOrder().getPrice();
		}
	}

	// ´¦·£¸üÐÂ
	public void penaltyUpdated() {
		long wSpread = this.worstSpread();
		long bestAsk = this.getBestPriceAsk();
		if(Ask!=null) {
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					if (Ask.get(i).get(j).getOrder().getSpread() >= 0) {
						Ask.get(i).get(j).getOrder().penalty = alpha * (Ask.get(i).get(j).getOrder().getPrice() - bestAsk)
								+ (1.0 - alpha) * Ask.get(i).get(j).getOrder().getSpread();
					} else {
						System.out.println(Ask.get(i).get(j).getOrder().getPrice());
						Ask.get(i).get(j).getOrder().penalty = alpha * (Ask.get(i).get(j).getOrder().getPrice() - bestAsk)
								+ (1.0 - alpha) * wSpread;
					}
				}
			}
		}
		

		long bestBid = this.getBestPriceBid();
		if(Bid!=null) {
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					if (Bid.get(i).get(j).getOrder().getSpread() >= 0) {
						Bid.get(i).get(j).getOrder().penalty = alpha * (bestBid - Bid.get(i).get(j).getOrder().getPrice())
								+ (1.0 - alpha) * Bid.get(i).get(j).getOrder().getSpread();
					} else {
						Bid.get(i).get(j).getOrder().penalty = alpha * (bestBid - Bid.get(i).get(j).getOrder().getPrice())
								+ (1.0 - alpha) * wSpread;
					}
				}
			}
		}
		
	}

	
	public long add2Sided(Orders bid, Orders ask) {
		orderID++;
		long spread = ask.getPrice() - bid.getPrice();
		this.addToSpreadList(new Long(spread), new Long(orderID));

		ask.spread = spread;
		bid.spread = spread;

		long bidPenalty = -1;
		if (Bid == null || Bid.isEmpty()) {
			bidPenalty = 0;
		} else {
			bidPenalty = this.getBestPriceBid() - bid.getPrice();
		}
		bid.penalty = alpha * spread + (1.0 - alpha) * bidPenalty;

		long askPenalty = -1;
		if (Ask == null || Ask.isEmpty()) {
			askPenalty = 0;
		} else {
			askPenalty = ask.getPrice() - this.getBestPriceAsk();
		}
		ask.penalty = alpha * spread + (1.0 - alpha) * askPenalty;

		if (Bid == null || Bid.isEmpty()) { 
			Bid = new LinkedList<List<LocalOrder>>();
			Bid.add(new LinkedList<LocalOrder>());
			Bid.get(0).add(new LocalOrder(orderID, bid));
		} else {
			if (bid.getPenalty() < Bid.get(0).get(0).getOrder().getPenalty()) { 
				Bid.add(0, new LinkedList<LocalOrder>());
				Bid.get(0).add(new LocalOrder(orderID, bid));
			} else if (bid.getPenalty() > Bid.get(Bid.size() - 1).get(0).getOrder().getPenalty()) { 
				Bid.add(new LinkedList<LocalOrder>());
				Bid.get(Bid.size() - 1).add(new LocalOrder(orderID, bid));
			} else { 
				for (int i = 1; i < Bid.size() - 1; ++i) {
					if (Bid.get(i).get(0).getOrder().getPenalty() < bid.getPenalty()
							&& bid.getPenalty() < Bid.get(i + 1).get(0).getOrder().getPenalty()) {
						Bid.add(i + 1, new LinkedList<LocalOrder>());
						Bid.get(i + 1).add(new LocalOrder(orderID, bid));
						break;
					} else if (Bid.get(i).get(0).getOrder().getPenalty() == bid.getPenalty()) {
						Bid.get(i).add(new LocalOrder(orderID, bid));
						break;
					} else if (Bid.get(i + 1).get(0).getOrder().getPenalty() == bid.getPenalty()) {
						Bid.get(i + 1).add(new LocalOrder(orderID, bid));
						break;
					}
				}
			}
		}

		if (Ask == null || Ask.isEmpty()) { // just add
			Ask = new LinkedList<List<LocalOrder>>();
			Ask.add(new LinkedList<LocalOrder>());
			Ask.get(0).add(new LocalOrder(orderID, ask));
		} else {
			if (ask.getPenalty() < Ask.get(0).get(0).getOrder().getPenalty()) { // new best
				Ask.add(0, new LinkedList<LocalOrder>());
				Ask.get(0).add(new LocalOrder(orderID, ask));
			} else if (ask.getPenalty() > Ask.get(Ask.size() - 1).get(0).getOrder().getPenalty()) { // new worst
				Ask.add(new LinkedList<LocalOrder>());
				Ask.get(Ask.size() - 1).add(new LocalOrder(orderID, ask));
			} else { 
				for (int i = 1; i < Ask.size() - 1; ++i) {
					if (Ask.get(i).get(0).getOrder().getPenalty() < ask.getPenalty()
							&& ask.getPenalty() < Ask.get(i + 1).get(0).getOrder().getPenalty()) {
						Ask.add(i + 1, new LinkedList<LocalOrder>());
						Ask.get(i + 1).add(new LocalOrder(orderID, ask));
						break;
					} else if (Ask.get(i).get(0).getOrder().getPenalty() == ask.getPenalty()) {
						Ask.get(i).add(new LocalOrder(orderID, ask));
						break;
					} else if (Ask.get(i + 1).get(0).getOrder().getPenalty() == ask.getPenalty()) {
						Ask.get(i + 1).add(new LocalOrder(orderID, ask));
						break;
					}
				}
			}
		}
		return orderID;
	}

	public long add1Sided(Orders order, int direction) {
		long pricePenalty = -1;
		order.spread = -1;

		if (direction == 1) {
			if (Bid == null || Bid.isEmpty()) {

				pricePenalty = 0;
			} else {
				pricePenalty = this.getBestPriceBid() - order.getPrice();
			}
		} else if (direction == -1) {
			if (Ask == null || Ask.isEmpty()) {
				pricePenalty = 0;
			} else {
				pricePenalty = order.getPrice() - this.getBestPriceAsk();
			}
		}
		long worstS = this.worstSpread();
		order.penalty = alpha * worstS + (1.0 - alpha) * pricePenalty;
		++orderID;

		if (direction == 1) {
			if (Bid == null || Bid.isEmpty()) { 
				Bid = new LinkedList<List<LocalOrder>>();
				Bid.add(new LinkedList<LocalOrder>());
				Bid.get(0).add(new LocalOrder(orderID, order));
			} else { 
				if (order.getPenalty() < Bid.get(0).get(0).getOrder().getPenalty()) { 
					Bid.add(0, new LinkedList<LocalOrder>());
					Bid.get(0).add(new LocalOrder(orderID, order));
				} else if (order.getPenalty() > Bid.get(Bid.size() - 1).get(0).getOrder().getPenalty()) { 
					Bid.add(new LinkedList<LocalOrder>());
					Bid.get(Bid.size() - 1).add(new LocalOrder(orderID, order));
				} else { 
					for (int i = 1; i < Bid.size() - 1; ++i) {
						if (Bid.get(i).get(0).getOrder().getPenalty() < order.getPenalty()
								&& order.getPenalty() < Bid.get(i + 1).get(0).getOrder().getPenalty()) {
							Bid.add(i + 1, new LinkedList<LocalOrder>());
							Bid.get(i + 1).add(new LocalOrder(orderID, order));
							break;
						} else if (Bid.get(i).get(0).getOrder().getPenalty() == order.getPenalty()) {
							Bid.get(i).add(new LocalOrder(orderID, order));
							break;
						} else if (Bid.get(i + 1).get(0).getOrder().getPenalty() == order.getPenalty()) {
							Bid.get(i + 1).add(new LocalOrder(orderID, order));
							break;
						}
					}
				}
			}
		} else if (direction == -1) {
			if (Ask == null || Ask.isEmpty()) { 
				Ask = new LinkedList<List<LocalOrder>>();
				Ask.add(new LinkedList<LocalOrder>());
				Ask.get(0).add(new LocalOrder(orderID, order));
			} else {
				if (order.getPenalty() < Ask.get(0).get(0).getOrder().getPenalty()) { // new best
					Ask.add(0, new LinkedList<LocalOrder>());
					Ask.get(0).add(new LocalOrder(orderID, order));
				} else if (order.getPenalty() > Ask.get(Ask.size() - 1).get(0).getOrder().getPenalty()) { // new worst
					Ask.add(new LinkedList<LocalOrder>());
					Ask.get(Ask.size() - 1).add(new LocalOrder(orderID, order));
				} else {
					for (int i = 1; i < Ask.size() - 1; ++i) {
						if (Ask.get(i).get(0).getOrder().getPenalty() < order.getPenalty()
								&& order.getPenalty() < Ask.get(i + 1).get(0).getOrder().getPenalty()) {
							Ask.add(i + 1, new LinkedList<LocalOrder>());
							Ask.get(i + 1).add(new LocalOrder(orderID, order));
							break;
						} else if (Ask.get(i).get(0).getOrder().getPenalty() == order.getPenalty()) {
							Ask.get(i).add(new LocalOrder(orderID, order));
							break;
						} else if (Ask.get(i + 1).get(0).getOrder().getPenalty() == order.getPenalty()) {
							Ask.get(i + 1).add(new LocalOrder(orderID, order));
							break;
						}
					}
				}
			}
		}
		return orderID;
	}

	
	public long removeObsolete(double vol) {
		long removed = 0;
		int i = 0;
		
		if (Ask != null && !Ask.isEmpty() && i != -1) {
			while (i < Ask.size()) {
				int j = 0;
				while (j < Ask.get(i).size()) {
					if (Ask.get(i).get(j).getOrder().time > Ask.get(i).get(j).getOrder().tMax
							|| random.nextDouble() > Math.max(Math.min(0.75, 1.0 - vol), 0.25)) {
						
						try {
							if (Ask.get(i).get(j).getOrder().getSpread() >= 0) { 
								
								this.removeSpreadList(Ask.get(i).get(j).getOrder().getSpread(),
										Ask.get(i).get(j).getOrder().getID());
								
							}
							
							Ask.get(i).remove(j);
						} catch (Exception e) {
							System.out.println("Failed removing expired quote!");
							return -1;
						}
						++removed;
					} else {
						++j;
					}
				}
				if (Ask.get(i).isEmpty()) {
					try {
						Ask.remove(i);
					} catch (Exception e) {
						System.out.println("Failed removing whole level!");
						return -1;
					}
				} else {
					++i;
				}
			}
		}
	
		i = 0;
		if (Bid != null && !Bid.isEmpty() && i != -1) {
			while (i < Bid.size()) {
				int j = 0;
				while (j < Bid.get(i).size()) {
					if (Bid.get(i).get(j).getOrder().time > Bid.get(i).get(j).getOrder().tMax
							|| random.nextDouble() > Math.max(Math.min(0.75, 1.0 - vol), 0.25)) {
						
						try {
							if (Bid.get(i).get(j).getOrder().getSpread() >= 0) { /
							
								this.removeSpreadList(Bid.get(i).get(j).getOrder().getSpread(),
										Bid.get(i).get(j).getOrder().getID());
								
							}
							
							Bid.get(i).remove(j);
						} catch (Exception e) {
							System.out.println("Failed removing expired quote!");
							return -1;
						}
						++removed;
					} else {
						++j;
					}
				}
				if (Bid.get(i).isEmpty()) {
					try {
						Bid.remove(i);
					} catch (Exception e) {
						System.out.println("Failed removing whole level!");
						return -1;
					}
				} else {
					++i;
				}
			}
		}
		return removed;
	}

	
	public boolean runTimeUpdate() {
		
		if (Ask != null && !Ask.isEmpty() && Ask.size() > 1) {
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					
					Ask.get(i).get(j).getOrder().time++;
				}
			}
		}
		
		if (Bid != null && !Bid.isEmpty() && Bid.size() > 1) {
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					
					Bid.get(i).get(j).getOrder().time++;
				}
			}
		}
		return true;
	}

	
	public void changeCaifuAll(Agent agent, long size, int direction, double mid, List<Agent> agents) {
		for (int i = 0; i < agents.size(); i++) {
			if (agent.getAgentId().equals(agents.get(i).getAgentId())) {
				agents.get(i).isExecute1 = true;
				agents.get(i).setCishu1(agents.get(i).getCishu1() + 1);
				agents.get(i).setCaifuPrev1(agents.get(i).getCaifu1());
				if (direction == 1) {
					agents.get(i).setGpNUm1(agents.get(i).getGpNUm1() + size);
					agents.get(i).setXjz1(agents.get(i).getXjz1() - size * mid);
				} else if (direction == -1) {
					if (agents.get(i).isFirst1) {
						agents.get(i).setGpNUm1(0);
					} else {
						agents.get(i).setGpNUm1(agents.get(i).getGpNUm1() - size);
					}
					agents.get(i).setXjz1(agents.get(i).getXjz1() + size * mid);
				}
				agents.get(i).setCaifu1(agents.get(i).getXjz1() + agents.get(i).getGpNUm1() * mid);
				if (agents.get(i).getCaifuPrev1() != 0) {
					agents.get(i).setBdl1((agents.get(i).getCaifu1() - agents.get(i).getCaifuPrev1())
							/ Math.abs(agents.get(i).getCaifuPrev1()));
				} else {
					agents.get(i).setBdl1(0);
				}
				
				agents.get(i).setBdlSum1(agents.get(i).getBdlSum1() + agents.get(i).getBdl1());
				if (agents.get(i).getCishu1() == 1) {
					agents.get(i).setFirstCaifu1(agents.get(i).getCaifu1());
				}
				
				agents.get(i).setBdlAve1(agents.get(i).getBdlSum1() / agents.get(i).getCishu1());
				
				agents.get(i).setCaifuCha1(agents.get(i).getCaifu1() - agents.get(i).getFirstCaifu1());
				agents.get(i).isFirst1 = false;
			} else {
				agents.get(i).setCishu1(agents.get(i).getCishu1() + 1);
				agents.get(i).setCaifuPrev1(agents.get(i).getCaifu1());
				agents.get(i).setCaifu1(agents.get(i).getXjz1() + agents.get(i).getGpNUm1() * mid);
				if (agents.get(i).getCaifuPrev1() != 0) {
					agents.get(i).setBdl1((agents.get(i).getCaifu1() - agents.get(i).getCaifuPrev1())
							/ Math.abs(agents.get(i).getCaifuPrev1()));
				} else {
					agents.get(i).setBdl1(0);
				}
				
				agents.get(i).setBdlSum1(agents.get(i).getBdlSum1() + agents.get(i).getBdl1());
				if (agents.get(i).getCishu1() == 1) {
					agents.get(i).setFirstCaifu1(agents.get(i).getCaifu1());
				}
				
				agents.get(i).setBdlAve1(agents.get(i).getBdlSum1() / agents.get(i).getCishu1());
				
				agents.get(i).setCaifuCha1(agents.get(i).getCaifu1() - agents.get(i).getFirstCaifu1());
			}
		}
	}

	public void changeCaifu(Agent agent, long size, int direction, double mid, List<Agent> agents) {
		for (int i = 0; i < agents.size(); i++) {
			if (agent.getAgentId().equals(agents.get(i).getAgentId())) {
				agents.get(i).isExecute = true;
				agents.get(i).setCishu(agents.get(i).getCishu() + 1);
				agents.get(i).setCaifuPrev(agents.get(i).getCaifu());
				if (direction == 1) {
					agents.get(i).setGpNUm(agents.get(i).getGpNUm() + size);
					
					agents.get(i).setXjz(agents.get(i).getXjz() - size * agent.targetPrice);
				} else if (direction == -1) {
					if (agents.get(i).isFirst) {
						agents.get(i).setGpNUm(0);
					} else {
						agents.get(i).setGpNUm(agents.get(i).getGpNUm() - size);
					}
					
					agents.get(i).setXjz(agents.get(i).getXjz() + size * agent.targetPrice);
				}
			//	agents.get(i).setCaifu(agents.get(i).getXjz() + agents.get(i).getGpNUm() * mid);
				agents.get(i).setCaifu(agents.get(i).getXjz() + agents.get(i).getGpNUm() * agent.targetPrice);
				if (agents.get(i).getCaifuPrev() != 0) {
					agents.get(i).setBdl((agents.get(i).getCaifu() - agents.get(i).getCaifuPrev())
							/ Math.abs(agents.get(i).getCaifuPrev()));
				} else {
					agents.get(i).setBdl(0);
				}
				
				agents.get(i).setBdlSum(agents.get(i).getBdlSum() + agents.get(i).getBdl());
				if (agents.get(i).getCishu() == 1) {
					agents.get(i).setFirstCaifu(agents.get(i).getCaifu());
				}
				
				agents.get(i).setBdlAve(agents.get(i).getBdlSum() / agents.get(i).getCishu());

				
				agents.get(i).setCaifuCha(agents.get(i).getCaifu() - agents.get(i).getFirstCaifu());

				WriteCSV.writrByStr("C:/test/wealth_record.csv", agents.get(i).getAgentId() + "," + direction + ","
						+ size + "," + mid + "," + agents.get(i).getGpNUm() + "," + agents.get(i).getGpNUm() * mid + ","
						+ agents.get(i).getXjz() + "," + agents.get(i).getCaifu() + "," + agents.get(i).getCaifuPrev()
						+ "," + agents.get(i).getBdl() + "," + agents.get(i).isFirst + "," + agents.get(i).getCishu()
						+ "," + agents.get(i).getBdlSum() + "," + agents.get(i).getBdlAve() + ","
						+ agents.get(i).getCaifuCha() + ","
								+ agent.getRole() + ","
										+ agent.targetPrice);

				agents.get(i).isFirst = false;
				break;
			}
		}
	}

	/* -- market execution (just returns the orders) -- */
	
	public List<LocalOrder> marketExecution(Agent agent, long size, int direction, double mid, List<Agent> agents) {
		// System.out.println("marketExecution==>" + size + ":" + direction + ":" +
		// mid);

		int index = -1;
		if (direction == 1) {
			if (Ask == null || Ask.isEmpty()) {
				System.out.println("Ask side empty, but wants execution!");
				this.askEmpty = true;
				return null;
			} else {
				
				changeCaifu(agent, size, direction, mid, agents);
				
				changeCaifuAll(agent, size, direction, mid, agents);
				
				List<LocalOrder> IDs = new LinkedList<LocalOrder>();
				
				for (int i = 0; i < Ask.size(); ++i) {
					for (int j = 0; j < Ask.get(i).size(); ++j) {
						if (size - Ask.get(i).get(j).getOrder().getSize() <= 0.0) {
							++index;
							IDs.add(new LocalOrder(Ask.get(i).get(j), agent.getAgentId(), size)); 
							IDs.get(index).getOrder().size = size; 
							size = 0;
							return IDs; 
						} else {
							++index;
							IDs.add(new LocalOrder(Ask.get(i).get(j), agent.getAgentId(), size));
							size -= IDs.get(index).getOrder().getSize();
						}
					}
				}
				if (size > 0) { 
					System.out.println("Partial Execution!"); 
					return IDs;
				}
			}
		} else if (direction == -1) {
			if (Bid == null || Bid.isEmpty()) {
				System.out.println("Bid side empty, but wants execution!");
				return null;
			} else {
				
				changeCaifu(agent, size, direction, mid, agents);
				
				changeCaifuAll(agent, size, direction, mid, agents);
				
				List<LocalOrder> IDs = new LinkedList<LocalOrder>();
				
				for (int i = 0; i < Bid.size(); ++i) {
					for (int j = 0; j < Bid.get(i).size(); ++j) {

						// WriteCSV.writrByStr(Bid.get(i).get(j).getOrder().getAgentId()+","+Bid.get(i).get(j).getOrder().getID()+","+direction
						// + "," + size + "," + mid+","+agent.getGpNUm()+","+agent.getXjz());

						if (size - Bid.get(i).get(j).getOrder().getSize() <= 0.0) {
							++index;
							IDs.add(new LocalOrder(Bid.get(i).get(j), agent.getAgentId(), size));
							IDs.get(index).getOrder().size = size;
							size = 0;
							return IDs;
						} else {
							++index;
							IDs.add(new LocalOrder(Bid.get(i).get(j), agent.getAgentId(), size));
							size -= IDs.get(index).getOrder().getSize();
						}
					}
				}
				if (size > 0) { 
					System.out.println("Partial Execution!"); 
					return IDs;
				}
			}
		}
		return null;
	}

	/* -- fill top quote (full or partial) -- */
	
	public boolean removeTop(LocalOrder order, int direction) {
		if (direction == -1) {
			bidLiq -= order.getOrder().size;
			if (Bid.isEmpty()) {
				System.out.println("Empty bid side!");
				return false;
			} else {
				if (Bid.get(0).get(0).getID() != order.getID()) {
					System.out.println("Failed matching top of the book!");
					return false;
				} else {
					if (order.getOrder().getSize() == Bid.get(0).get(0).getOrder().getSize()) {
						long id = Bid.get(0).get(0).getOrder().getID();
						try {
							if (Bid.get(0).get(0).getOrder().getSpread() >= 0) { 
								this.removeSpreadList(Bid.get(0).get(0).getOrder().getSpread(),
										Bid.get(0).get(0).getOrder().getID());
								// this.findOpposite(Bid.get(0).get(0).getOrder().getID(), -1);
							}
							Bid.get(0).remove(0);
						} catch (Exception e) {
							System.out.println("Failed removing top quote (bid)!");
							return false;
						}
						if (Bid.get(0).isEmpty()) {
							try {
								Bid.remove(0);
							} catch (Exception e) {
								System.out.println("Failed removing top level (bid)!");
								return false;
							}
						}
					} else if (order.getOrder().getSize() < Bid.get(0).get(0).getOrder().getSize()) {
						Bid.get(0).get(0).getOrder().size -= order.getOrder().getSize();
					} else {
						
						System.out.println("Removing top, but quoted size too big!");
						return false;
					}
					return true;
				}
			}
		}
		if (direction == 1) {
			askLiq -= order.getOrder().size;
			if (Ask.isEmpty()) {
				System.out.println("Empty ask side!");
				return false;
			} else {
				if (Ask.get(0).get(0).getID() != order.getID()) {
					System.out.println("Failed matching top of the book!");
					return false;
				} else {
					if (order.getOrder().getSize() == Ask.get(0).get(0).getOrder().getSize()) {
						long id = Ask.get(0).get(0).getOrder().getID();
						try {
							if (Ask.get(0).get(0).getOrder().getSpread() >= 0) { // notify opposite quote
								this.removeSpreadList(Ask.get(0).get(0).getOrder().getSpread(),
										Ask.get(0).get(0).getOrder().getID());
								// this.findOpposite(Ask.get(0).get(0).getOrder().getID(), 1);
							}
							Ask.get(0).remove(0);
						} catch (Exception e) {
							System.out.println("Failed removing top quote (bid)!");
							return false;
						}
						if (Ask.get(0).isEmpty()) {
							try {
								Ask.remove(0);
							} catch (Exception e) {
								System.out.println("Failed removing top level (bid)!");
								return false;
							}
						}
					} else if (order.getOrder().getSize() < Ask.get(0).get(0).getOrder().getSize()) {
						Ask.get(0).get(0).getOrder().size -= order.getOrder().getSize();
					} else {
						System.out.println("Removing top, but size too big!");
						return false;
					}
					return true;
				}
			}
		}
		System.out.println("ID equals 0 : " + order.ID);
		return false;
	}

	/* -- localized order removal -- */
	public boolean removeOrder(Orders order, int direction) {
		if (direction == -1) {
			askLiq -= order.getSize();
			if (this.askEmpty == true || Ask.isEmpty()) {
				this.askEmpty = true;
				return false;
			}
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					if (Ask.get(i).get(j).getID() == order.getID()) {
						if (Ask.get(i).get(j).getOrder().getSpread() <= -1) {
							System.out.println("Trying to remove one-sided order!");
							return false;
						}
						try {
							Ask.get(i).remove(j);
						} catch (Exception e) {
							System.out.println("Failed removing order/agent from LOB!");
							return false;
						}
						if (Ask.get(i).isEmpty()) {
							try {
								Ask.remove(i);
							} catch (Exception e) {
								System.out.println("Failed removing level!");
								return false;
							}
						}
						return true;
					}
				}
			}
			System.out.println("Failed matching ID/order to remove!");
			return false;
		} else if (direction == 1) {
			bidLiq -= order.getSize();
			if (this.bidEmpty || Bid.isEmpty()) {
				this.bidEmpty = true;
				return false;
			}
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					if (Bid.get(i).get(j).getID() == order.getID()) {
						if (Bid.get(i).get(j).getOrder().getSpread() <= -1) {
							System.out.println("Trying to remove one-sided order!");
							return false;
						}
						try {
							Bid.get(i).remove(j);
						} catch (Exception e) {
							System.out.println("Failed removing order/agent from LOB!");
							return false;
						}
						if (Bid.get(i).isEmpty()) {
							try {
								Bid.remove(i);
							} catch (Exception e) {
								System.out.println("Failed removing level!");
							}
						}
						return true;
					}
				}
			}
			System.out.println("Failed matching ID/order to remove!");
			return false;
		}
		return false;
	}

	
	long getBestPriceBid() {
		long price = 0;
		if (Bid == null || Bid.isEmpty()) {
			return -1;
		} else {
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					if (Bid.get(i).get(j).getOrder().getPrice() > price)
						price = Bid.get(i).get(j).getOrder().getPrice();
				}
			}
			return price;
		}
	}

	
	long getBestPriceAsk() {
		long price = 999999999;
		if (Ask == null || Ask.isEmpty()) {
			return -1;
		} else {
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					if (Ask.get(i).get(j).getOrder().getPrice() < price) {
						price = Ask.get(i).get(j).getOrder().getPrice();
					}
				}
			}
			return price;
		}
	}

	
	long getBidCount() {
		long bidCount = 0;
		if (Bid == null || Bid.isEmpty()) {
			return bidCount;
		} else {
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					bidCount += 1;
				}
			}
			return bidCount;
		}
	}

	
	long getAskCount() {
		long askCount = 0;
		if (Ask == null || Ask.isEmpty()) {

			return askCount;
		} else {
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					askCount += 1;
				}
			}
			return askCount;
		}
	}

	
	long getBidLiq() {
		long liq = 0;
		if (Bid == null || Bid.isEmpty()) {
			return 0;
		} else {
			
			for (int i = 0; i < Bid.size(); ++i) {
				for (int j = 0; j < Bid.get(i).size(); ++j) {
					
					liq += Bid.get(i).get(j).getOrder().getSize();
				}
			}
			return liq;
		}
	}

	
	long getAskLiq() {
		long liq = 0;
		if (Ask == null || Ask.isEmpty()) {
			return 0;
		} else {
			
			for (int i = 0; i < Ask.size(); ++i) {
				for (int j = 0; j < Ask.get(i).size(); ++j) {
					
					liq += Ask.get(i).get(j).getOrder().getSize();
				}
			}
			return liq;
		}
	}

}
