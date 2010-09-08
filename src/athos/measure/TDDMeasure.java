package athos.measure;

import jess.Batch;
import jess.Fact;
import jess.QueryResult;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;
import athos.model.Episode;

public class TDDMeasure {

	private Rete engine;
	
	private float numberOfTDDEpisodes;
	private float numberOfNonTDDEpisodes;

	public TDDMeasure() throws Exception {
		this.engine = new Rete();
	    Batch.batch("athos/measure/EpisodeTDDConformance.clp", this.engine);
	    Batch.batch("athos/measure/OneWayTDDHeuristicAlgorithm.clp", this.engine);

	}
	
	public void measure(Episode[] episodes) throws Exception {
		
		for (int i=0 ; i< episodes.length ; i++) {
			Episode e = episodes[i];
		    Fact f = new Fact("EpisodeTDDConformance", engine);
		    f.setSlotValue("index", new Value(i, RU.INTEGER));
		    f.setSlotValue("category", new Value(e.getCategory(), RU.STRING));
		    f.setSlotValue("subtype", new Value(e.getSubtype(), RU.STRING));

			engine.assertFact(f);
		}
		
		engine.run();
		
		numberOfNonTDDEpisodes = 0;
		numberOfTDDEpisodes = 0;

		for (int i=0 ; i< episodes.length ; i++) {
			QueryResult result = engine.runQueryStar("episode-tdd-conformance-query-by-index", 
					(new ValueVector()).add(new Value(i, RU.INTEGER)));
			
			
			if (result.next()) {
				episodes[i].setIsTDD("True".equals(result.getString("isTDD")));
				
				if (episodes[i].isTDD())
					numberOfTDDEpisodes += 1;
				else
					numberOfNonTDDEpisodes += 1;
			}
			
				
			
		}
		
	}

	public float getTDDPercentageByNumber() {
		return numberOfTDDEpisodes / (numberOfNonTDDEpisodes + numberOfTDDEpisodes);
	}

}
